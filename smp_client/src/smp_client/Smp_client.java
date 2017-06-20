/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smp_client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Bartek
 */
public class Smp_client extends Thread{
    
    public static void main(String [] args) {
      String serverName = "localhost";
      int port = Integer.parseInt("33344");
      try {
         System.out.println("Connecting to " + serverName + " on port " + port);
         Socket client = new Socket(serverName, port);
         
         System.out.println("Just connected to " + client.getRemoteSocketAddress());
         OutputStream outToServer = client.getOutputStream();
         DataOutputStream out = new DataOutputStream(outToServer);
         
         out.writeUTF("Hello from " + client.getLocalSocketAddress());
         InputStream inFromServer = client.getInputStream();
         DataInputStream in = new DataInputStream(inFromServer);
         String s="";
         
         while(!s.equals("exit"))
         {
             s = in.readUTF();
             System.out.println("Server says " + s);
         }
         client.close();
      }catch(IOException e) {
         e.printStackTrace();
      }
   }
    
}
