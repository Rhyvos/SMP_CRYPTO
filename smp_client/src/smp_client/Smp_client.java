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
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;

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
    DHCryptoBox DH;
    CryptoBox CB;
    SMP smp;
    int coords;
    boolean g_b = false;
    boolean gamma_b = false;
    public boolean checker;
    public Smp_client(int port, String adress, String name,int c){
        DH = new DHCryptoBox();
        coords = c;
        DH.createKeyPair(true);
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
        String str = Base64.getEncoder().encodeToString(DH.getPublicKey());
        System.out.println(str);
        mp.setMsg(str);
        send(mp.GenerateMsg());
    }
    
    public void close_session()
    {
        open = false;
    }
    
    private void gen_aes_key(){
        if(checker){
            byte[] decodedKey = Base64.getDecoder().decode(reciver_pub_key);
            DH.setAdvPublicKey( decodedKey );
            CB =    new CryptoBox();
            CB.setKey( DH.generateSecret() );
        }else{
            DH = new DHCryptoBox();
            byte[] decodedKey = Base64.getDecoder().decode(reciver_pub_key);
            DH.setAdvPublicKey( decodedKey );
            DH.createKeyPair( false );
            CB =    new CryptoBox();
            CB.setKey( DH.generateSecret() );
        }
    }
    
    private void parse(String msg){
        System.out.println("Parse: "+msg);
        mp.ParseMessage(msg);
        String to;
        switch(mp.what())
        {
            case NAME:
                break;
            case MESSAGE:
                byte[] encoded_m = Base64.getDecoder().decode(mp.getMsg());
                parse(new String(CB.decrypt(encoded_m)));
                break;
            case MESSAGE_P:
                //String tmp_msg = decrypt(mp.getMsg());
                //parse(msg);
                break;
            case COMMAND:
                break;
            case PUBLIC_KEY:
                break;
            case DH_PUBLIC_KEY:
                reciver_pub_key = mp.getMsg();
                System.out.println("DH_PUBLIC_KEY:reciver_pub_key set to "+reciver_pub_key);
                if(checker)
                {
                    gen_aes_key();
                    mp.setType(MessageParser.TYPE.TEST_AES);
                    mp.setReciver(mp.getSender());
                    mp.setSender(name);
                    String str = Base64.getEncoder().encodeToString(CB.encrypt("TEST".getBytes()));
                    mp.setMsg(str);
                    send(mp.GenerateMsg());
                }
                break;
            case USER_PUBLIC_KEY:
                reciver_pub_key = mp.getMsg();
                System.out.println("USER_PUBLIC_KEY:reciver_pub_key set to "+reciver_pub_key);
                if(!checker){
                    gen_aes_key();
                    mp.setType(MessageParser.TYPE.DH_PUBLIC_KEY);
                    mp.setReciver(mp.getSender());
                    mp.setSender(name);
                    String pkay = Base64.getEncoder().encodeToString(DH.getPublicKey());
                    mp.setMsg(pkay);
                    send(mp.GenerateMsg());
                }  
                break;
            case TEST_AES:
                System.out.println(msg);
                if(!checker)
                {
                    byte[] encoded = Base64.getDecoder().decode(mp.getMsg());
                    if(new String(CB.decrypt(encoded)).compareTo("TEST")==0)
                    {
                        System.out.println("TEST PASSED");
                        smp = new SMP(false);
                        smp.set_y(""+coords);
                    }
                    else
                        System.out.println("TEST FAILED");
                    mp.setType(MessageParser.TYPE.TEST_AES);
                    mp.setReciver(mp.getSender());
                    mp.setSender(name);
                    String str = Base64.getEncoder().encodeToString(CB.encrypt("TEST".getBytes()));
                    mp.setMsg(str);
                    send(mp.GenerateMsg());
                }else{
                    byte[] encoded = Base64.getDecoder().decode(mp.getMsg());
                    if(new String(CB.decrypt(encoded)).compareTo("TEST")==0)
                    {
                        System.out.println("TEST PASSED");
                        smp = new SMP(true);
                        smp.set_x(""+coords);
                        to = mp.getSender();
                        send(Prepare_Encoded_Message(MessageParser.TYPE.HA, smp.get_ha().toString(),to));
                        send(Prepare_Encoded_Message(MessageParser.TYPE.HALPHA, smp.get_halpha().toString(),to));
                    }
                    else
                        System.out.println("TEST FAILED");
                }
                    
                
                break;
            case ID:
                id = Integer.parseInt(mp.getMsg());
                System.out.println("ID set to "+id);
                break;
            case EXIT:
                break;
            case HALPHA:
                to = mp.getSender();
                send(Prepare_Encoded_Message(MessageParser.TYPE.GAMMA, smp.get_gamma(mp.getMsg()).toString(),to));
                break;
            case HA:
                to = mp.getSender();
                send(Prepare_Encoded_Message(MessageParser.TYPE.G, smp.get_g(mp.getMsg()).toString(),to));
                break;
            case G:
                to = mp.getSender();
                smp.set_g(mp.getMsg());
                g_b=true;
                if(gamma_b){
                    if(checker){
                        send(Prepare_Encoded_Message(MessageParser.TYPE.PA, smp.get_pa().toString(),to));
                        send(Prepare_Encoded_Message(MessageParser.TYPE.QA, smp.get_qa().toString(),to));
                    }else{
                        send(Prepare_Encoded_Message(MessageParser.TYPE.PB, smp.get_pb().toString(),to));
                        send(Prepare_Encoded_Message(MessageParser.TYPE.QB, smp.get_qb().toString(),to));
                    }
                    g_b=false;
                }
                break;
            case GAMMA:
                to = mp.getSender();
                smp.set_gamma(mp.getMsg());
                gamma_b=true;
                if(g_b){
                    if(checker){
                        send(Prepare_Encoded_Message(MessageParser.TYPE.PA, smp.get_pa().toString(),to));
                        send(Prepare_Encoded_Message(MessageParser.TYPE.QA, smp.get_qa().toString(),to));
                    }else{
                        send(Prepare_Encoded_Message(MessageParser.TYPE.PB, smp.get_pb().toString(),to));
                        send(Prepare_Encoded_Message(MessageParser.TYPE.QB, smp.get_qb().toString(),to));
                    }
                    gamma_b=false;
                }
                break;
            case PB:
                smp.set_pb(mp.getMsg());
                break;
            case QB:
                to = mp.getSender();
                smp.set_qb(mp.getMsg());
                send(Prepare_Encoded_Message(MessageParser.TYPE.C1, smp.get_c1().toString(),to));
                break;
            case PA:
                smp.set_pa(mp.getMsg());
                break;
            case QA:
                smp.set_qa(mp.getMsg());
                to = mp.getSender();
                send(Prepare_Encoded_Message(MessageParser.TYPE.PB, smp.get_pb().toString(),to));
                send(Prepare_Encoded_Message(MessageParser.TYPE.QB, smp.get_qb().toString(),to));
                break;
            case C1:
                to = mp.getSender();
                send(Prepare_Encoded_Message(MessageParser.TYPE.C, smp.get_c(mp.getMsg()).toString(),to));
                System.out.println("Are you near "+to+" :"+smp.Test());
                break;
            case C:
                to = mp.getSender();
                smp.set_c(mp.getMsg());
                System.out.println("Are you near "+to+" :"+smp.Test());
                checker = false;
                break;
            case ERROR:
                System.out.println("Cant parse message:"+msg);
                break;
            default:
                throw new AssertionError(mp.what().name());
        }
        
    }
    
    public String Prepare_Encoded_Message(MessageParser.TYPE t, String msg, String reciv){
        mp.setType(t);
        mp.setMsg(msg);
        String tmp = Base64.getEncoder().encodeToString(CB.encrypt(mp.GenerateMsg().getBytes()));
        mp.setType(MessageParser.TYPE.MESSAGE);
        mp.setReciver(reciv);
        mp.setSender(name);
        mp.setMsg(tmp);   
        return mp.GenerateMsg();
    }
    
    
    public void run() {
        while(open)
        {
            try {
                sleep(100);
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
            } catch (InterruptedException ex) {
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
            int coords;
            System.out.println("Select name");
            name = br.readLine();
            System.out.println("Coordinates");
            coords = Integer.parseInt(br.readLine());
             
            Smp_client ss = new Smp_client(port,"localhost",name,coords);
            //ss.test();
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
   
    public void test()
    {
            SMP A = new SMP(true);
            SMP B = new SMP(false);
            String ha = A.get_ha().toString();
            String halpha = A.get_halpha().toString();
            String g = B.get_g(ha).toString();
            String gamma = B.get_gamma(halpha).toString();
            A.set_g(g);
            A.set_gamma(gamma);
            A.set_x("4");
            B.set_y("4");
            String pa = A.get_pa().toString();
            String pb = B.get_pb().toString();
            String qa = A.get_qa().toString();
            String qb = B.get_qb().toString();
            A.set_pb(pb);
            B.set_pa(pa);
            A.set_qb(qb);
            B.set_qa(qa);
            String c1 = A.get_c1().toString();
            String c = B.get_c(c1).toString();
            A.set_c(c);
            System.out.println("TEST A:"+A.Test());
            System.out.println("TEST B:"+B.Test());
    }
    
}
