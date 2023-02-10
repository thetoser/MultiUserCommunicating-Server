package com.zhljava.qqserver.service;

import com.zhljava.qqcommon.Message;
import com.zhljava.qqcommon.MessageType;
import com.zhljava.utils.Utility;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class SendNewsToAllService implements Runnable {

    @Override
    public void run() {

        while (true) {
            System.out.println("输入服务器要推送的新闻(exit表示退出推送服务): ");
            String news = Utility.readString(100);
            if ("exit".equals(news)) {
                break;
            }
            Message message = new Message();
            message.setSender("服务器");
            message.setMesType(MessageType.MESSAGE_TO_ALL_MES);
            message.setContent(news);
            message.setSendTime(new Date().toString());
            System.out.println("服务器推送消息给所有人 : " + news);

            //遍历所有通信线程
            HashMap<String, ServerConnectClientThread> map = ManageClientThreads.getMap();
            Set<String> keySet = map.keySet();
            for (String onlineUserId : keySet) {
                try {
                    ObjectOutputStream oos = new ObjectOutputStream
                            (ManageClientThreads.
                                    getServerConnectClientThread(onlineUserId).
                                    getSocket().getOutputStream());
                    oos.writeObject(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
