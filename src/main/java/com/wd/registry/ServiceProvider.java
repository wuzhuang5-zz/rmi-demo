package com.wd.registry;


import com.wd.common.Constant;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.CountDownLatch;

public class ServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProvider.class);
    /**
     * 用于等待 SyncConnected 事件触发后继续执行当前线程
     */
    private CountDownLatch latch = new CountDownLatch(1);

    /**
     * 发布RMI服务并注册RMI地址到Zookeeper中
     * @param remote
     * @param host
     * @param port
     */
    public void publish(Remote remote, String host, int port) {
        String url = publishService(remote,host,port);
        if(url != null) {
            //连接Zookeeper服务器并获取zk对象
            ZooKeeper zk = connectServer();
            if(zk != null) {
                //创建ZNode并将RMI地址放入ZNode上
                createNode(zk,url);
            }
        }
    }

    /**
     * 发布RMI服务
     * @param remote
     * @param host
     * @param port
     * @return
     */
    private String publishService(Remote remote, String host, int port) {
        String url = null;
        try {
            url = String.format("rmi://%s:%d/%s", host, port, remote.getClass().getName());
            LocateRegistry.createRegistry(port);
            Naming.rebind(url, remote);
            logger.debug("publish rmi service (url: {})", url);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * 连接zookeeper服务器
     * @return
     */
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(Constant.ZK_CONNECTION_STRING, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if(watchedEvent.getState() == Event.KeeperState.SyncConnected);
                    //唤醒当前正在执行的线程
                    latch.countDown();
                }
            });
        } catch (IOException e){
            e.printStackTrace();
        }
        return zk;
    }

    /**
     * 创建ZNode
     * @param zk
     * @param url
     */
    private void createNode(ZooKeeper zk, String url) {
        try {
            byte[] data = url.getBytes();
            /**
             * 创建一个临时性且有序的ZNode
             */
            String path = zk.create(Constant.ZK_PROVIDER_PATH, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
            logger.debug("create zookeeper node ({} => {})", path, url);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
}
