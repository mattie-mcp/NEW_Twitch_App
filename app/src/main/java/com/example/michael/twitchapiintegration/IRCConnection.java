package com.example.michael.twitchapiintegration;

import android.os.AsyncTask;
import android.widget.TextView;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class IRCConnection extends AsyncTask<Void, Void, Void>{

    private String host;
    private int port;
    private PrintStream out;
    private Thread t;
    private Socket socket;
    private String channel;
    private TextView chat;

    public IRCConnection(String h,int p,String c, TextView t) throws IOException{
        host = h;
        port = p;
        channel = c;
        chat = t;
    }
    public IRCConnection(String h,int p,String c) throws IOException{
        host = h;
        port = p;
        channel = c;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            socket = new Socket( host , port );
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out = new PrintStream( socket.getOutputStream() );
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.println("PASS oauth:6iyar63v16zbhjfutcn268lem1vswc");
        out.println("NICK fedaykin1200");
        Reader reader = new Reader();
        reader.start();
        out.println("JOIN #" + channel);
        return null;
    }

    private class Reader extends Thread{

        public void run(){

            try {
                BufferedReader input =
                        new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while(true){
                    String msg;
                    msg = input.readLine();
                    if(msg != null){
                        final String parsedMsg = parseMessage(msg);
                        chat.post(new Runnable() {
                            @Override
                            public void run() {
                                chat.append(parsedMsg+"\n");
                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String parseMessage(String msg){

        if(msg.indexOf("PRIVMSG") < 0)
            return msg;

        int userNameStop = msg.indexOf('!');
        String userName = msg.substring(1, userNameStop);

        int messageStart = msg.indexOf(':', 2);
        String message = msg.substring(messageStart+1);

        return userName + ": " + message;
    }
}