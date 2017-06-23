/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smp_client;

/**
 *
 * @author Bartek
 */
public class MessageParser {
    public enum TYPE{
        NAME("NAME"),
        MESSAGE("MESSAGE"),
        MESSAGE_P("MESSAGE_P"),
        COMMAND("COMMAND"),
        PUBLIC_KEY("PUBLIC_KEY"),
        USER_PUBLIC_KEY("USER_PUBLIC_KEY"),
        DH_PUBLIC_KEY("DH_PUBLIC_KEY"),
        ID("ID"),
        EXIT("EXIT"),
        TEST_AES("TEST_AES"),
        CHECK_RANGE("CHECK_RANGE"),
        G("G"),
        HALPHA("HALPHA"),
        HA("HA"),
        GAMMA("GAMMA"),
        PB("PB"),
        QB("QB"),
        PA("PA"),
        QA("QA"),
        C1("C1"),
        C("C"),
        ERROR("ERROR");
        private final String text;
        private TYPE(final String text) {
            this.text = text;
        }
        @Override
        final public String toString() {
            return text;
        }
    }

    
    private TYPE t;
    private String msg;
    private String reciver;
    private String sender;
    public TYPE what(){
        return t;
    }
    
    public String getMsg(){
        return msg;
    }
    
    public String getReciver(){
        return reciver;
    }
    
    public String getSender(){
        return sender;
    }
    
    public void setMsg(String s){
        msg = s;
    }
    
    public void setReciver(String s){
        reciver = s;
    }
    
    public void setSender(String s){
        sender = s;
    }
    
    public void setType(TYPE t){
        this.t = t;
    }
    
    
    public String GenerateMsg(){
        if(t == TYPE.MESSAGE || t == TYPE.MESSAGE_P || t == TYPE.DH_PUBLIC_KEY || t == TYPE.TEST_AES){
            return t.toString()+";"
                   +sender+";"
                   +reciver+";"
                   +msg;
        }else if(t == TYPE.USER_PUBLIC_KEY){
            return t.toString()+";"
                   +sender+";"
                   +msg;
        }
        return t.toString()+";"+msg;
        
    }
    
    public void ParseMessage(String s){
        String tab[] = s.split(";");
        if(tab.length >= 2)
        {
            int i = 0;
            switch (tab[i].toUpperCase()){
                case "NAME":
                    t = TYPE.NAME;
                    break;
                case "MESSAGE":
                    t = TYPE.MESSAGE;
                    sender=tab[++i];
                    reciver = tab[++i];
                    break;
                case "MESSAGE_P":
                    t = TYPE.MESSAGE_P;
                    sender=tab[++i];
                    reciver = tab[++i];
                    break;
                case "COMMAND":
                    t = TYPE.COMMAND;
                    break;
                case "PUBLIC_KEY":
                    t = TYPE.PUBLIC_KEY;
                    break;
                case "USER_PUBLIC_KEY":
                    sender=tab[++i];
                    t = TYPE.USER_PUBLIC_KEY;
                    break;
                case "DH_PUBLIC_KEY":
                    sender=tab[++i];
                    reciver = tab[++i];
                    t = TYPE.DH_PUBLIC_KEY;
                    break;
                case "ID":
                    t = TYPE.ID;
                    break;
                case "EXIT":
                    t = TYPE.EXIT;
                    break;
                case "TEST_AES":
                    sender=tab[++i];
                    reciver = tab[++i];
                    t = TYPE.TEST_AES;
                    break;
                case "CHECK_RANGE":
                    t = TYPE.CHECK_RANGE;
                    break;
                case "G":
                    t = TYPE.G;
                    break;
                case "HALPHA":
                    t = TYPE.HALPHA;
                    break;
                case "HA":
                    t = TYPE.HA;
                    break;
                case "GAMMA":
                    t = TYPE.GAMMA;
                    break;
                case "PA":
                    t = TYPE.PA;
                    break;
                case "PB":
                    t = TYPE.PB;
                    break;
                case "QA":
                    t = TYPE.QA;
                    break;
                case "QB":
                    t = TYPE.QB;
                    break;
                case "C1":
                    t = TYPE.C1;
                    break;
                case "C":
                    t = TYPE.C;
                    break;
                default:
                    t = TYPE.ERROR;
                    break;
                    
            }
            msg = tab[++i];
        }
        
    }
    
}
