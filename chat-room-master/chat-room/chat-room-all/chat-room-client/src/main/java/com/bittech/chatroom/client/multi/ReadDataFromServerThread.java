package com.bittech.chatroom.client.multi;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;

public class ReadDataFromServerThread extends Thread {

    private final Socket socket;

    public ReadDataFromServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        //客户端读数据
        try {
            InputStream in = this.socket.getInputStream();
            Scanner scanner = new Scanner(in);

            while (true) {
                String message = scanner.nextLine();
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
