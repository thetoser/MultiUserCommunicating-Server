package com.zhljava.qqserver.service;

import com.zhljava.qqcommon.Message;
import com.zhljava.qqcommon.MessageType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * 和某个客户端保持通信
 */
public class ServerConnectClientThread extends Thread {
    private Socket socket;
    private String userId;

    public ServerConnectClientThread(Socket socket, String userId) {
        this.socket = socket;
        this.userId = userId;
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("服务端和客户端" + userId + "保持通信");
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) ois.readObject();

                //用户接收端不在线
                if (!QQServer.isOnline(message.getGetter()) &&
                        !message.getMesType().equals(MessageType.MESSAGE_CLIENT_EXIT)) {
                    ArrayList<Message> arrayList = QQServer.getArrayList();
                    arrayList.add(message);
                    QQServer.getChm().put(message.getGetter(), arrayList);
                    continue;
                }

                if (message.getMesType().equals(MessageType.MESSAGE_GET_ONLINE_FRIEND)) {
                    System.out.println(message.getSender() + " 需要在线用户列表");
                    String onlineUsers = ManageClientThreads.getOnlineUsers();
                    //返回Message
                    Message message2 = new Message();
                    message2.setMesType(MessageType.MESSAGE_RET_ONLINE_FRIEND);
                    message2.setContent(onlineUsers);
                    message2.setGetter(message.getSender());
                    //返回给客户端
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(message2);

                } else if (message.getMesType().equals(MessageType.MESSAGE_COMM_MES)) { //普通聊天信息
                    ServerConnectClientThread serverConnectClientThread =
                            ManageClientThreads.getServerConnectClientThread(message.getGetter());
                    ObjectOutputStream oos = new ObjectOutputStream
                            (serverConnectClientThread.socket.getOutputStream());
                    oos.writeObject(message);

                } else if (message.getMesType().equals(MessageType.MESSAGE_TO_ALL_MES)) { //群发聊天信息
                    //得到所有线程
                    HashMap<String, ServerConnectClientThread> map = ManageClientThreads.getMap();
                    Set<String> keySet = map.keySet();
                    for (String onlineUserId : keySet) {
                        if (!onlineUserId.equals(message.getSender())) {
                            ObjectOutputStream oos = new ObjectOutputStream
                                    (map.get(onlineUserId).socket.getOutputStream());
                            oos.writeObject(message);
                        }
                    }

                } else if (message.getMesType().equals(MessageType.MESSAGE_FILE_MES)) { //文件
                    ServerConnectClientThread serverConnectClientThread =
                            ManageClientThreads.getServerConnectClientThread(message.getGetter());
                    ObjectOutputStream oos =
                            new ObjectOutputStream(serverConnectClientThread.socket.getOutputStream());
                    oos.writeObject(message);

                } else if (message.getMesType().equals(MessageType.MESSAGE_CLIENT_EXIT)) {
                    //客户端退出
                    System.out.println(message.getSender() + " 退出");
                    ManageClientThreads.removeServerConnectClientThread(message.getSender());
                    socket.close();
                    break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
