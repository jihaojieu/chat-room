package server.multi;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description: 基于多线程的服务端
 * Author: admin
 * Create: 2019-06-07 17:07
 */
public class MultiThreadServer {

    //存储所有的注册到服务端的客户端userName和Socket，相当于用户列表
    private static Map<String, Socket> clientLists = new ConcurrentHashMap<>();

    //专门用来处理每个客户端的输入输出请求，每个子线程只处理自己的客户端
    private static class ExecuteClientRequest implements Runnable {
        private Socket client;

        private ExecuteClientRequest(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            //获取用户发来的信息
            try {
                Scanner scanner = new Scanner(client.getInputStream());
                String strFromClient = "";
                while (true) {
                    if (scanner.hasNext()) {
                        strFromClient = scanner.nextLine();
                        //windows下消除用户输入自带的\r，将\r替换为""
                        Pattern pattern = Pattern.compile("\r");
                        Matcher matcher = pattern.matcher(strFromClient);
                        strFromClient = matcher.replaceAll("");
                    }

                    //根据用户输入区别注册、群聊、私聊、退出
                    //注册  register:1
                    if (strFromClient.startsWith("register")) {
                        String userName = strFromClient.split("\\:")[1];
                        userRegister(userName, client);
                    }

                    //群聊  G:hello world
                    if (strFromClient.startsWith("G")) {
                        String groupMsg = strFromClient.split("\\:")[1];
                        groupChat(groupMsg);
                    }

                    //私聊  P:1-hello
                    if (strFromClient.startsWith("P")) {
                        String userName = strFromClient.split("\\:")[1].split("\\-")[0];
                        String privateMsg = strFromClient.split("\\:")[1].split("\\-")[1];
                        privateChat(userName, privateMsg);
                    }

                    //用户退出  1:byebye
                    if (strFromClient.contains("byebye")) {
                        String userName = strFromClient.split("\\:")[0];
                        userOffline(userName);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * description: 用户注册流程
         * param：userName 要注册的用户名
         * param：socket 用户名对应的Socket
         */
        private void userRegister(String userName, Socket socket) {
            try {
                PrintStream printStream = new PrintStream(socket.getOutputStream(), true, "UTF-8");
                if (clientLists.containsKey(userName)) {
                    printStream.println("您已经注册过了，无需重复注册");
                } else {
                    clientLists.put(userName, socket);
                    System.out.println("用户 " + userName + " 上线了");
                    System.out.println("当前聊天室人数为：" + clientLists.size());
                    printStream.println("恭喜您，注册成功！");
                    printStream.println("当前聊天室人数为：" + clientLists.size());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * description: 群聊流程
         * param:
         */
        public void groupChat(String groupMsg) {
            for (Map.Entry<String, Socket> entry : clientLists.entrySet()) {
                Socket socket = entry.getValue();
                //群聊不给自己发
                if (socket == this.client) {
                    continue;
                }
                try {
                    PrintStream printStream = new PrintStream(socket.getOutputStream(), true, "UTF-8");
                    printStream.println("收到 " + this.getCurrentUserName(clientLists) + " 的群聊消息：" + groupMsg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * description: 私聊流程
         * param: userName 要私聊的用户名
         * param: privateMsg 私聊信息
         */
        public void privateChat(String userName, String privateMsg) {
            //得到username对应的Socket
            Socket client = clientLists.get(userName);
            //获取输出流
            try {
                PrintStream printStream = new PrintStream(client.getOutputStream(), true, "UTF-8");
                printStream.println("收到 " + this.getCurrentUserName(clientLists) + " 的私聊消息：" + privateMsg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * description: 用户退出流程
         * params:
         */
        private void userOffline(String userName) {
            //删除Map中的实体
            System.out.println("用户 " + this.getCurrentUserName(clientLists) + " 已下线");
            //通知其他用户该用户已下线
            for (Map.Entry<String, Socket> entry : clientLists.entrySet()) {
                Socket socket = entry.getValue();
                if (socket.equals(this.client)) {
                    clientLists.remove(userName, socket);
                    System.out.println("当前聊天室人数为：" + clientLists.size());
                    break;
                }
                try {
                    PrintStream printStream = new PrintStream(socket.getOutputStream(), true, "UTF-8");
                    printStream.println("您的好友 " + this.getCurrentUserName(clientLists) + " 已下线");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //获得Socket对应的用户名
        private String getCurrentUserName(Map<String, Socket> clientLists) {
            for (Map.Entry<String, Socket> entry : clientLists.entrySet()) {
                Socket target = entry.getValue();
                if (target.equals(this.client)) {
                    return entry.getKey();
                }
            }
            return "";
        }
    }


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6666);
        //同时处理多个客户端连接，使用线程池，每当有一个客户端来就可以创建线程来处理
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        System.out.println("等待客户端连接...");
        for (int i = 0; i < 20; i++) {
            Socket client = serverSocket.accept();
            System.out.println("有新的客户端连接，端口号为 " + client.getPort());
            //执行一个线程，每当有一个客户端来把他包装成一个线程来处理
            executorService.execute(new ExecuteClientRequest(client));
        }
        //关闭线程池与服务端
        executorService.shutdown();
        serverSocket.close();
    }
}
