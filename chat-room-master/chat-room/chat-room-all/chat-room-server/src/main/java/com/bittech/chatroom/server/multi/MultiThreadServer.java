package com.bittech.chatroom.server.multi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadServer {
    
    public static void main(String[] args) {
        /*
          问题：端口号，线程数都是固定不变的
          通过配置使得用户可以自己传入端口号和线程数：
            1. args  命令行参数
            2. Properties  属性文件 (k=v)
            3. 数据库
            4. ....
          运用命令行参数的形式：
            java  主类  args1  args2 ...
            java MultiTreadServer 8080 10
            //Linux Window java
            java MultiThreadServer --port=8080 --thread=10
         */
        int defaultPort = 8080;//端口号
        int defaultThread = 10;//线程数
        int port = defaultPort;
        int thread = defaultThread;
        
        for (String arg : args) {

            if (arg.startsWith("--port=")) {
                String portStr = arg.substring("--port=".length());
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                    port = defaultPort;
                }
            }

            if (arg.startsWith("--thread=")) {
                String threadStr = arg.substring("--thread=".length());
                try {
                    thread = Integer.parseInt(threadStr);
                } catch (NumberFormatException e) {
                    thread = defaultThread;
                }
            }
        }

        try {

            final ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("服务器启动：" + serverSocket.getInetAddress() + " : " + serverSocket.getLocalPort() + ", 线程数：" + thread);
            
            //线程池调度器
            final ExecutorService executorService =
                    Executors.newFixedThreadPool(thread);
            
            while (true) {
                final Socket socket = serverSocket.accept();
                executorService.execute(new ClientHandler(socket));
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
