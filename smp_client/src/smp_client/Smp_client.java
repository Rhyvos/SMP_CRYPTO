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
import java.util.Base64;
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
    DHCryptoBox DH;
    CryptoBox CB;
    public boolean checker;
    public Smp_client(int port, String adress, String name){
        DH = new DHCryptoBox();
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
        System.out.println("decodedKey: " + DH.getPublicKey().length);
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
            System.out.println("decodedKey: " + decodedKey.length);
            DH.setAdvPublicKey( decodedKey );
            CB =    new CryptoBox();
            CB.setKey( DH.generateSecret() );
        }else{
            DH = new DHCryptoBox();
            byte[] decodedKey = Base64.getDecoder().decode(reciver_pub_key);
            System.out.println("decodedKey: " + decodedKey.length);
            DH.setAdvPublicKey( decodedKey );
            DH.createKeyPair( false );
            CB =    new CryptoBox();
            CB.setKey( DH.generateSecret() );
        }
        String str = Base64.getEncoder().encodeToString(DH.generateSecret().getEncoded());
        System.out.println("AES_KEY: " + str);
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
                    System.out.println("decodedKey: " + DH.getPublicKey().length);
                    mp.setMsg(pkay);
                    send(mp.GenerateMsg());
                }  
                break;
            case TEST_AES:
                System.out.println(msg);
                if(!checker)
                {
                    byte[] encoded = Base64.getDecoder().decode(mp.getMsg());
                    if(Base64.getEncoder().encodeToString(CB.decrypt(encoded)).compareTo("TEST")==0)
                        System.out.println("TEST PASSED");
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
                    if(Base64.getEncoder().encodeToString(CB.decrypt(encoded)).compareTo("TEST")==0)
                        System.out.println("TEST PASSED");
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
            ss.test();
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
		DHCryptoBox aliceDH = new DHCryptoBox();
		DHCryptoBox bobDH = new DHCryptoBox();
		
		// Alicja generuje pare kluczy
		aliceDH.createKeyPair( true );
		// Alicja wysyla do Boba swoj klucz publiczny
		byte[] apk = aliceDH.getPublicKey();
		// Bob odbiera klucz publiczny Alicji i na jego podstawie generuje swoja pare kluczy
		bobDH.setAdvPublicKey( apk );
		bobDH.createKeyPair( false );
		// Bob wysyla swoj klucz publiczny
		byte[] bpk = bobDH.getPublicKey();
		// Alicja odbiera klucz publiczny Boba
		aliceDH.setAdvPublicKey( bpk );
		
		// Bob i Alicja moga wyliczyc klucze sesyjne (te same)
		// i tym samym zakonczyc protokol DH
		CryptoBox alice = new CryptoBox();
		alice.setKey( aliceDH.generateSecret() );
		
		CryptoBox bob = new CryptoBox();
		bob.setKey( bobDH.generateSecret() );
		
		// Alicja i Bob uzywaja klucza sesyjnego do szyfrowania i deszyfrowania danych
		System.out.println( new String( bob.decrypt( alice.encrypt( "Hello, world!".getBytes() ) ) ) );
		System.out.println( new String( bob.decrypt( alice.encrypt( "qwerty".getBytes() ) ) ) );
		System.out.println( new String( alice.decrypt( bob.encrypt( "some, test.".getBytes() ) ) ) );
	}
    
}
