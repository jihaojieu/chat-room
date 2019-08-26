package client.multi;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Description: 基于多线程的客户端
 * Author: admin
 * Create: 2019-06-07 16:26
 */
//读和写虽然是两个线程，但是共用了同一个Socket
class ReadFromServer implements Runnable {
    private Socket client;

    public ReadFromServer(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        //获取输入流来取得服务器发来的信息，而且需要不停的读取
        try {
            Scanner scanner = new Scanner(client.getInputStream());
            while (true) {
                if (scanner.hasNext()) {
                    String msgFromServer = scanner.nextLine();
                    System.out.println(msgFromServer);
                }
                if (client.isClosed()) {
                    System.out.println("客户端关闭...");
                    scanner.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class WriteToServer implements Runnable {
    private Socket client;

    public WriteToServer(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        //获取输出流向服务器发送信息
        try {
            PrintStream printStream = new PrintStream(client.getOutputStream(), true, "UTF-8");
            //动态的向服务器输入信息，通过Scanner输入
            Scanner scanner = new Scanner(System.in);
            System.out.println("请输入>>>");
            System.out.println("");
            while (true) {
                String strFromUser = "";
                if (scanner.hasNext()) {
                    strFromUser = scanner.nextLine();
                }
                //向服务器发送信息
                printStream.println(strFromUser);
                //判断退出
                if (strFromUser.contains("byebye")) {
                    System.out.println("您已下线...");
                    printStream.close();
                    scanner.close();
                    client.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class MultiThreadClient {
    public static void main(String[] args) throws IOException {
        //根据指定的IP和端口号建立连接
        Socket client = new Socket("127.0.0.1", 6666);
        //启动读线程与写线程
        Thread readThread = new Thread(new ReadFromServer(client));
        Thread writeThread = new Thread(new WriteToServer(client));
        readThread.start();
        writeThread.start();
    }
}
