package com.zhljava.qqserver.service;

import java.util.HashMap;
import java.util.Set;

/**
 * 用于管理和客户端通信的线程
 */
public class ManageClientThreads {
    private static HashMap<String, ServerConnectClientThread> map = new HashMap<>();

    //返回HashMap
    public static HashMap<String, ServerConnectClientThread> getMap() {
        return map;
    }

    public static void addClientThread(String userId, ServerConnectClientThread serverConnectClientThread) {
        map.put(userId, serverConnectClientThread);
    }

    public static ServerConnectClientThread getServerConnectClientThread(String userId) {
        return map.get(userId);
    }

    //从集合中移除线程
    public static void removeServerConnectClientThread(String userId) {
        map.remove(userId);
    }

    //返回用户列表
    public static String getOnlineUsers() {
        Set<String> strings = map.keySet();
        String onlineUserList = "";
        for (String string : strings) {
            onlineUserList += string + " ";
        }
        return onlineUserList;
    }

}
