package com.bittech.chatroom.client.multi;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class WriteDataToServerThread extends Thread {

    private final Socket socket;

    public WriteDataToServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        //客户端写数据
        try {
            OutputStream out = socket.getOutputStream();
            PrintStream printStream = new PrintStream(out);

            //从键盘读入数据
            Scanner scanner = new Scanner(System.in);
            System.out.println(helpInfo());
            while (true) {
                System.out.println("请输入>>");
                String message = scanner.nextLine();
                //输入发送到服务端
                printStream.println(message);
                printStream.flush();
                if ("quit".equals(message)) {
                    break;
                }
            }
            //关闭客户端
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String helpInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("使用指南：")
                .append("\n").append("1.注册：register:<name>  name是注册的名字")
                .append("\n").append("2.群聊：group:<message>  message是消息")
                .append("\n").append("3.私聊：private:<name>:<message>  name私聊的对象, message是消息")
                .append("\n").append("4.退出：quit  退出")
                .append("\n");
        return sb.toString();
    }
}
