/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smp_server;

/**
 *
 * @author Bartek
 */
public interface Events {
    void exit(int id);
    boolean send_to_user(int id,String msg);
    int find_user(String name);
    String get_user_pub_key(int id);
}
