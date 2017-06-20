/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smp_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bartek
 */


public class client_conn extends Thread {
    private Socket s;
    private int id;
    private static int conn_count;
    private boolean open;
    private DataInputStream in;
    private DataOutputStream out;
    private BlockingQueue<String> msg_queue;
    private Events event;
    public client_conn(Socket s, Events e)
    {   
        try {
            event = e;
            id = conn_count;
            conn_count ++;
            this.s=s;
            msg_queue = new ArrayBlockingQueue<String>(10);
            msg_queue.put("id="+id);
            open = true;
            try {
                in = new DataInputStream(s.getInputStream());
                out = new DataOutputStream(s.getOutputStream());
            }catch(IOException ex) {
                ex.printStackTrace();
            }
        }catch(InterruptedException ex) {
            Logger.getLogger(client_conn.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void close_session()
    {
        open = false;
    }
    
    public int get_id()
    {
        return id;
    }
    
    public void run()
    {
        try
        {
            while(open)
            {
                //System.out.println("isClosed:"+s.isClosed()+" isConnected"+s.isConnected());
                if(s.isClosed() || (!s.isConnected()))
                {
                    close_session();
                    event.exit(id);
                }
                if(in.available()>0){
                    System.out.println("from Clien["+id+"]:"+in.readUTF());
                    /* TO DO */
                }else{
                    if(!msg_queue.isEmpty())
                    {
                        String msg = "";
                        try {
                            msg = msg_queue.take();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(client_conn.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println("to Clien["+id+"]:"+msg);
                        out.writeUTF(msg);
                    }
                }

            }
            s.close();
        }catch(SocketTimeoutException s) {
            //System.out.println("Socket timed out!");
            close_session();
            event.exit(id);
        }catch(IOException e){
            System.out.println("Clien["+id+"]: cant close connectrion");
            //e.printStackTrace();
            close_session();
            event.exit(id);
        }
    }
    synchronized public void send(String s){
        try {
            msg_queue.put(s);
        } catch (InterruptedException ex) {
            Logger.getLogger(client_conn.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
