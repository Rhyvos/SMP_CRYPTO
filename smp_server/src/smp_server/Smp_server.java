/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smp_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bartek
 */


public class Smp_server extends Thread implements Events{
    private ServerSocket serverSocket;
    private Vector<client_conn> client_list;
    private Events ei;
    private boolean open;
    public Smp_server(int port) throws IOException {

        serverSocket = new ServerSocket(port);
        //serverSocket.setSoTimeout(10000);
        client_list = new Vector<client_conn>();
        open = true;
    }
    public void stop_server()
    {
        open = false;
    }
    public void close_all_connections()
    {
        for(int i =0 ;i<client_list.size();i++)
            client_list.get(i).close_session();
        client_list.clear();
    }
    
    public void run() {
        
        System.out.println("Waiting for client on " +serverSocket.getInetAddress()+":"+serverSocket.getLocalPort() + "...");
        while(open) {
            try {
           
                Socket s = serverSocket.accept();
                System.out.println("accepted new connection");
                client_conn cn = new  client_conn(s,this);
                client_list.add(cn);
                Thread t = new Thread(cn);
                t.start();
                
                
            }catch(SocketTimeoutException s) {
               System.out.println("Socket timed out!");
               break;
            }catch(IOException e) {
               e.printStackTrace();
               break;
            }
      }
    }
    public void send_all(String s)
    {
        for(int i =0 ;i<client_list.size();i++)
            client_list.get(i).send(s);

    }
    
    public static void main(String[] args) {
         int port = Integer.parseInt("33344");
         try {
            Smp_server ss = new Smp_server(port);
            Thread t = new Thread(ss);
            t.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String s;
            do{
                s = br.readLine();
                ss.send_all(s);
            }while(!s.equals("exit"));
             try {
                 sleep(500);
             } catch (InterruptedException ex) {
                 Logger.getLogger(Smp_server.class.getName()).log(Level.SEVERE, null, ex);
             }
            ss.close_all_connections();
            }catch(IOException e) {
            e.printStackTrace();
         }
    }

    @Override
    public void exit(int id) {
        for(int i =0 ;i<client_list.size();i++)
            if(client_list.get(i).get_id() == id)
                client_list.remove(i);
    }
    
}
