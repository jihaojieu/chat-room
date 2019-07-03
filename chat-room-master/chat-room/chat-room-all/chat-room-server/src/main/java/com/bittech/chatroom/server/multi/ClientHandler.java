package com.bittech.chatroom.server.multi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    
    //存储所有的注册到服务端的客户端name和Socket
    private static final Map<String, Socket> SOCKET_MAPS = new ConcurrentHashMap<>();

    private String name;

    private final Socket client;

    public ClientHandler(Socket client) {
        this.client = client;
    }
    
    @Override
    public void run() {

        //此处服务器socket和客户端socket进行数据传输业务逻辑
        try {
            //服务端读数据
            InputStream in = this.client.getInputStream();
            Scanner scanner = new Scanner(in);

            while (true) {
                String line = scanner.nextLine();

                //注册功能
                if (line.startsWith("register:")) {
                    //register:<name>
                    String[] segments = line.split(":");
                    if (segments[0].equals("register")) {
                        String name = segments[1];
                        this.register(name);
                    }
                    continue;
                }

                //群聊功能
                if (line.startsWith("group:")) {
                    String[] segments = line.split(":");
                    if (segments[0].equals("group")) {
                        String message = segments[1];
                        this.group(message);
                    }
                    continue;
                }

                //私聊功能
                if (line.startsWith("private:")) {
                    String[] segments = line.split(":");
                    if (segments[0].equals("private")) {
                        String targetName = segments[1];
                        String message = segments[2];
                        this.privateChat(targetName, message);
                    }
                    continue;
                }

                //退出功能
                if (line.equals("quit")) {
                    this.quit();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    //私聊（给谁发什么消息）
    private void privateChat(String targetName, String message) {
        Socket socket = SOCKET_MAPS.get(targetName);
        if (socket == null) {
            return;
        }
        this.sendMessage(socket, this.name + " 说：" + message);
    }

    //群聊（给除了自己的其他人发什么消息）
    private void group(String message) {
        //this.client  群聊给自己不发送
        for (Map.Entry<String, Socket> entry : SOCKET_MAPS.entrySet()) {
            Socket socket = entry.getValue();
            if (socket == this.client) {
                continue;
            }
            this.sendMessage(socket, this.name + " 说：" + message);
        }
    }

    //退出（完成之后并打印在线用户数）
    private void quit() {
        SOCKET_MAPS.remove(this.name);
        this.printOnlineClient();
    }

    //注册（成功之后并打印在线用户数）
    private void register(String name) {
        //this.name  表示当前客户端注册的名称
        //this.client  表示当前客户端连接的Socket
        this.name = name;
        SOCKET_MAPS.put(name, this.client);
        this.sendMessage(this.client, "恭喜" + name + "注册成功");
        
        //打印当前在线的所有客户端
        printOnlineClient();
    }

    //打印在线用户数
    private void printOnlineClient() {
        System.out.println("当前在线用户数：" + SOCKET_MAPS.size() + " 名称列表如下：");
        for (String name : SOCKET_MAPS.keySet()) {
            System.out.println(name);
        }
    }
    
    private void sendMessage(Socket socket, String message) {
        try {
            OutputStream out = socket.getOutputStream();
            PrintStream printStream = new PrintStream(out);
            printStream.println(message);
            printStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
