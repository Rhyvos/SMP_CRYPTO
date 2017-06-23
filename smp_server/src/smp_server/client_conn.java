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
    private String name;
    private MessageParser mp;
    private String pub_key;
    public client_conn(Socket s, Events e)
    {   
        try {
            pub_key = "not defined";
            event = e;
            id = conn_count;
            conn_count ++;
            this.s=s;
            msg_queue = new ArrayBlockingQueue<String>(10);
            mp = new MessageParser();
            mp.setMsg(Integer.toString(id));
            mp.setType(MessageParser.TYPE.ID);
            msg_queue.put(mp.GenerateMsg());
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
                sleep(100);
                //System.out.println("isClosed:"+s.isClosed()+" isConnected"+s.isConnected());
                if(s.isClosed() || (!s.isConnected()))
                {
                    close_session();
                    event.exit(id);
                }
                if(in.available()>0){
                    String msg = in.readUTF();
                    System.out.println("from Clien["+id+"]:"+msg);
                    mp.ParseMessage(msg);
                    switch(mp.what())
                    {
                        case NAME:
                            name = mp.getMsg();
                            break;
                        case MESSAGE:
                            String dest_name = mp.getReciver();
                            int dest_id = event.find_user(dest_name);
                            if(dest_id >= 0)
                                event.send_to_user(dest_id, msg);
                            else{
                                mp.setType(MessageParser.TYPE.ERROR);
                                mp.setMsg("User not found:"+dest_name);
                                send(mp.GenerateMsg());
                            }
                            break;
                        case TEST_AES:
                            String ta_name = mp.getReciver();
                            int ta_id = event.find_user(ta_name);
                            if(ta_id >= 0)
                                event.send_to_user(ta_id, msg);
                            else{
                                mp.setType(MessageParser.TYPE.ERROR);
                                mp.setMsg("User not found:"+ta_name);
                                send(mp.GenerateMsg());
                            }
                            break;
                        case DH_PUBLIC_KEY:
                            String dh_name = mp.getReciver();
                            int dh_id = event.find_user(dh_name);
                            if(dh_id >= 0){
                                event.send_to_user(dh_id, msg);
                            }else{
                                mp.setType(MessageParser.TYPE.ERROR);
                                mp.setMsg("User not found:"+dh_name);
                                send(mp.GenerateMsg());
                            }
                            break;
                        case COMMAND:
                            break;
                        case PUBLIC_KEY:
                            pub_key = mp.getMsg();
                            break;
                        case CHECK_RANGE:
                            String cr_name = mp.getMsg();
                            int cr_id = event.find_user(cr_name);
                            if(cr_id >= 0){
                                mp.setType(MessageParser.TYPE.USER_PUBLIC_KEY);
                                mp.setSender(name);
                                mp.setMsg(pub_key);
                                event.send_to_user(cr_id, mp.GenerateMsg());
                                //mp.setMsg(event.get_user_pub_key(cr_id));
                                //send(mp.GenerateMsg());
                            } else{
                                mp.setType(MessageParser.TYPE.ERROR);
                                mp.setMsg("User not found:"+cr_id);
                                send(mp.GenerateMsg());
                            }
                            break;
                        case ID:
                            break;
                        case EXIT:
                            close_session();
                            event.exit(id);
                            break;
                        case ERROR:
                            break;
                        default:
                            throw new AssertionError(mp.what().name());
                    }
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
        } catch (InterruptedException ex) {
            Logger.getLogger(client_conn.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    synchronized public void send(String s){
        try {
            msg_queue.put(s);
        } catch (InterruptedException ex) {
            Logger.getLogger(client_conn.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String get_name(){
        return name;
    }
    
    public String get_pub_key(){
        return pub_key;
    }
}
