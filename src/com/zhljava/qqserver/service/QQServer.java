package com.zhljava.qqserver.service;

import com.zhljava.qqcommon.Message;
import com.zhljava.qqcommon.MessageType;
import com.zhljava.qqcommon.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class QQServer {
    private ServerSocket serverSocket = null;
    //存放多个用户
    private static HashMap<String, User> validUsers = new HashMap<>();
    private static ConcurrentHashMap<String, ArrayList<Message>> chm = new ConcurrentHashMap<>();
    private static ArrayList<Message> messages = new ArrayList<>();

    static { //初始化 validUsers
        validUsers.put("100", new User("100", "123456"));
        validUsers.put("200", new User("200", "123456"));
        validUsers.put("300", new User("300", "123456"));
    }

    public static ArrayList<Message> getArrayList() {
        return messages;
    }

    public static ConcurrentHashMap<String, ArrayList<Message>> getChm() {
        return chm;
    }

    //判断发送给的用户是否在线
    public static boolean isOnline(String getterId) {
        if (ManageClientThreads.getServerConnectClientThread(getterId) == null) {
            return false;
        }
        return true;
    }

    //验证用户是否有效
    private boolean checkUser(String userId, String passwd) {
        User user = validUsers.get(userId);
        if (user == null) {
            return false;
        }
        if (!user.getPasswd().equals(passwd)) {
            return false;
        }
        return true;
    }

    public QQServer() {
        System.out.println("服务端在9999端口监听...");
        //启动推送新闻的线程
        new Thread(new SendNewsToAllService()).start();
        try {
            serverSocket = new ServerSocket(9999);
            while (true) {
                Socket socket = serverSocket.accept();
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                User user = (User) ois.readObject();
                Message message = new Message();
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                //验证
                if (checkUser(user.getUserId(), user.getPasswd())) {
                    message.setMesType(MessageType.MESSAGE_LOGIN_SUCCEED);
                    oos.writeObject(message);
                    //创建线程
                    ServerConnectClientThread serverConnectClientThread =
                            new ServerConnectClientThread(socket, user.getUserId());

                    serverConnectClientThread.start();
                    //把线程放入集合
                    ManageClientThreads.addClientThread(user.getUserId(), serverConnectClientThread);
                    //接收离线消息
                    ArrayList<Message> messages = chm.get(user.getUserId());
                    if (messages != null) {
                        for (Message m : messages) {
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream
                                    (ManageClientThreads.getServerConnectClientThread
                                            (user.getUserId()).getSocket().getOutputStream());
                            objectOutputStream.writeObject(m);
                        }
                        chm.remove(user.getUserId());
                    }

                } else { //登录失败
                    System.out.println("用户 id=" + user.getUserId() + " pwd=" + user.getPasswd() + " 验证失败");
                    message.setMesType(MessageType.MESSAGE_LOGIN_FAIL);
                    oos.writeObject(message);
                    socket.close();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
