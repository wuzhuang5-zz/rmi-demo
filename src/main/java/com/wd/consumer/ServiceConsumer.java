package com.wd.consumer;

import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ServiceConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ServiceConsumer.class);
    /**
     * 用于等待SyncConnected事件触发后继续执行当前线程
     */
    private CountDownLatch latch = new CountDownLatch(1);
    /**
     * 定义一个volatile成员变量，用于保存最新的RMI地址，考虑到该变量或许会被其它线程所修改，一旦修改后，该变量的值会影响到所有线程）
     */
    private volatile List<String> urlList = new ArrayList<>();

    public ServiceConsumer() {
        ZooKeeper zk = null;
    }

//    private Zookeeper conn
}
