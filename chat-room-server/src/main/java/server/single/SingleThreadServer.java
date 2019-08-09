package server.single;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class SingleThreadServer {
    public static void main(String[] args) {
        try {
            //0.通过命令行获取服务器端口
            int port = 6666;
            if (args.length > 0) {
                try {
                    port = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.out.println("端口参数不正确，采用默认端口" + port);
                }
            }

            //1.创建ServerSocket，监听port端口
            //前1000个为保留端口
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("服务器启动：" + serverSocket.getLocalSocketAddress());

            //2.等待客户端连接
            System.out.println("等待客户端连接...");
            //返回客户端Socket实例
            Socket clientSocket = serverSocket.accept();
            System.out.println("客户端信息：" + clientSocket.getRemoteSocketAddress());

            //3.接收和发送数据
            //3.1 接收客户端发来的数据
            InputStream clientInput = clientSocket.getInputStream();
            Scanner scanner = new Scanner(clientInput);
            String clientData = scanner.next();
            System.out.println("来自客户端的消息：" + clientData);

            //3.2 往客户端发送数据
            OutputStream clientOutput = clientSocket.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(clientOutput);
            writer.write("你好，欢迎连接服务器\n");
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
