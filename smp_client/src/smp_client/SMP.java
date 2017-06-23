/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smp_client;

import java.math.BigInteger;
import java.util.Random;

/**
 *
 * @author Bartek
 */
public class SMP {
    public BigInteger p;
    public BigInteger h;
    public BigInteger a;
    public BigInteger alpha;
    public BigInteger r;
    public BigInteger b;
    public BigInteger beta;
    public BigInteger s;
    public BigInteger g;
    public BigInteger gamma;
    public BigInteger x;
    public BigInteger y;
    public BigInteger qa;
    public BigInteger qb;
    public BigInteger pa;
    public BigInteger pb;
    public BigInteger c1;
    public BigInteger c;
    
    public SMP(boolean first){
        p = new BigInteger("2410312426921032588552076022197566074856950548502459942654116941958108831682612228890093858261341614673227141477904012196503648957050582631942730706805009223062734745341073406696246014589361659774041027169249453200378729434170325843778659198143763193776859869524088940195577346119843545301547043747207749969763750084308926339295559968882457872412993810129130294592999947926365264059284647209730384947211681434464714438488520940127459844288859336526896320919633919");
        //p = new BigInteger("23");
        h = new BigInteger("2");
        Random generator = new Random();
        if(first){
            a = new BigInteger(192 * 8,generator).mod(p);    
            alpha = new BigInteger(192 * 8,generator).mod(p);  
            r = new BigInteger(192 * 8,generator).mod(p);  
        }else{
            b = new BigInteger(192 * 8,generator).mod(p);    
            beta = new BigInteger(192 * 8,generator).mod(p);  
            s = new BigInteger(192 * 8,generator).mod(p);
        }
       
    }
    
            
    BigInteger get_ha(){
        return h.modPow(a, p);
    }      
    
    BigInteger get_halpha(){
        return h.modPow(alpha, p);
    }
    
    BigInteger get_g(String s){
        BigInteger ha = new BigInteger(s);
        g = ha.modPow(b, p);
        return g;
    }
    
    BigInteger get_gamma(String s){
        BigInteger halpha = new BigInteger(s);
        gamma = halpha.modPow(beta, p);
        return gamma;
    }
    
    void set_g(String s){
        g =  new BigInteger(s);
    }
    
    void set_x(String s){
        x =  new BigInteger(s);
    }
    
    void set_y(String s){
        y =  new BigInteger(s);
    }
    
    void set_gamma(String s){
        gamma =  new BigInteger(s);
    }
    
    void set_pa(String s){
        pa =  new BigInteger(s);
    }
    
    void set_pb(String s){
        pb =  new BigInteger(s);
    }
    
    void set_qa(String s){
        qa =  new BigInteger(s);
    }
    
    void set_qb(String s){
        qb =  new BigInteger(s);
    }
    void set_c(String s){
        c =  new BigInteger(s);
    }
    
    BigInteger get_pa(){
        pa = gamma.modPow(r, p);
        return pa;
    } 
    
    BigInteger get_pb(){
        pb = gamma.modPow(s, p);
        return pb;
    } 
    
    BigInteger get_qa(){
        qa = h.modPow(r, p).multiply(g.modPow(x, p)).mod(p);
        return qa;
    } 
    
    BigInteger get_qb(){
        qb = h.modPow(s, p).multiply(g.modPow(y, p)).mod(p);
        return qb;
    } 
    
    BigInteger get_c1(){
        c1 = qa.multiply(qb.modInverse(p)).modPow(alpha, p);
        return c1;
    } 
    
    BigInteger get_c(String s){
        BigInteger c1 = new BigInteger(s);
        c = c1.modPow(beta, p);
        return c;
    } 
    
    boolean Test(){
        return c.equals(pa.multiply(pb.mod(p)).mod(p));
    }
    
}
