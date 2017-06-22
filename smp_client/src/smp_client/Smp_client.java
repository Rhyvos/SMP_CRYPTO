/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smp_client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import static java.lang.Thread.sleep;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bartek
 */
public class Smp_client extends Thread{
    private BlockingQueue<String> msg_queue;
    private boolean open;
    Socket client;
    DataOutputStream out;
    DataInputStream in;
    int id;
    private MessageParser mp;
    String name;
    String aes_key;
    String reciver_pub_key;
    public boolean checker;
    public Smp_client(int port, String adress, String name){
        msg_queue = new ArrayBlockingQueue<String>(10);
        open = true;
        checker = false;
        mp = new MessageParser();
        mp.setType(MessageParser.TYPE.NAME);
        mp.setMsg(name);
        this.name=name;
        send(mp.GenerateMsg());
        send_pkey();
        try {
            Socket client = new Socket(adress, port);
            
            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            out = new DataOutputStream(outToServer);
            
            InputStream inFromServer = client.getInputStream();
            in = new DataInputStream(inFromServer);
        } catch (IOException ex) {
            Logger.getLogger(Smp_client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void send_pkey(){
        mp.setType(MessageParser.TYPE.PUBLIC_KEY);
        mp.setMsg("tmp"+name);
        send(mp.GenerateMsg());
    }
    
    public void close_session()
    {
        open = false;
    }
    
    private void parse(String msg){
        mp.ParseMessage(msg);
        switch(mp.what())
        {
            case NAME:
                break;
            case MESSAGE:
                //String tmp_msg = decrypt(mp.getMsg());
                parse(msg);
                break;
            case MESSAGE_P:
                //String tmp_msg = decrypt(mp.getMsg());
                //parse(msg);
                break;
            case COMMAND:
                break;
            case PUBLIC_KEY:
                break;
            case USER_PUBLIC_KEY:
                reciver_pub_key = mp.getMsg();
                System.out.println("reciver_pub_key set to "+reciver_pub_key);
                break;
            case AES_KEY:
                aes_key = mp.getMsg();
                System.out.println("AES_KEY set to "+aes_key);
                break;
            case ID:
                id = Integer.parseInt(mp.getMsg());
                System.out.println("ID set to "+id);
                break;
            case EXIT:
                break;
            case P:
                int p = Integer.parseInt(mp.getMsg());
                System.out.println("P set to "+id);
                break;
            case G:
                int g = Integer.parseInt(mp.getMsg());
                System.out.println("G set to "+id);
                break;
            case G2A:
                break;
            case G3A:
                break;
            case G2B:
                break;
            case G3B:
                break;
            case PB:
                break;
            case QB:
                break;
            case PA:
                break;
            case QA:
                break;
            case RA:
                break;
            case RB:
                break;
            case ERROR:
                System.out.println("Cant parse message:"+msg);
                break;
            default:
                throw new AssertionError(mp.what().name());
        }
        
    }
    
    
    public void run() {
        while(open)
        {
            try {
                if(in.available()>0){
                    String msg = in.readUTF();
                    System.out.println("from Server:"+msg);
                    parse(msg);
                }else{
                    if(!msg_queue.isEmpty())
                    {
                        String msg = "";
                        try {
                            msg = msg_queue.take();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Smp_client.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println("to Server: "+msg);
                        out.writeUTF(msg);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Smp_client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }   
    }
    
    synchronized public void send(String s){
        try {
            msg_queue.put(s);
        } catch (InterruptedException ex) {
            Logger.getLogger(Smp_client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String [] args) {
        try {
            int port = Integer.parseInt("33344");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String name;
            System.out.println("Select name");
            name = br.readLine();
             
            Smp_client ss = new Smp_client(port,"localhost",name);
            Thread t = new Thread(ss);
            t.start();

            String s;
            do{
                s = br.readLine();
                if(s.contains("CHECK_RANGE"))
                    ss.checker = true;
                ss.send(s);
            }while(!s.equals("exit"));
            try {
                ss.close_session();
                sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(Smp_client.class.getName()).log(Level.SEVERE, null, ex);
            }
             
        }catch(IOException ex) {
           Logger.getLogger(Smp_client.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
    
}
