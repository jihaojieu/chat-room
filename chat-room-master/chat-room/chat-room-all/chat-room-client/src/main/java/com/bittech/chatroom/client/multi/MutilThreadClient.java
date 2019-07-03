package com.bittech.chatroom.client.multi;

import java.io.IOException;
import java.net.Socket;

public class MutilThreadClient {

    public static void main(String[] args) {

        String defaultHost = "127.0.0.1";
        int defaultPort = 8080;
        String host = defaultHost;
        int port = defaultPort;

        for (String arg : args) {
            if (arg.startsWith("--port=")) {
                String portStr = arg.substring("--port=".length());
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                    port = defaultPort;
                }
            }
            if (arg.startsWith("--host=")) {
                host = arg.substring("--host=".length());
            }
        }

        try {
            System.out.println("客户端连接服务器：" + host + " : " + port);
            Socket socket = new Socket(host, port);
            //读数据
            new ReadDataFromServerThread(socket).start();
            //写数据
            new WriteDataToServerThread(socket).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
