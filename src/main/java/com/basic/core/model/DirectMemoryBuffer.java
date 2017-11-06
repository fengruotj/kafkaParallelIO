package com.basic.core.model;

import java.util.concurrent.CountDownLatch;

/**
 * locate com.basic.core.model
 * Created by 79875 on 2017/11/6.
 */
public class DirectMemoryBuffer {
    DirectMemoryChannel directMemoryChannel;

    private CountDownLatch bufferFinished=new CountDownLatch(1);//directMemory是否缓冲完成
    private CountDownLatch bufferOutFinished=new CountDownLatch(1);//directMemory是否缓冲输出完成

    private int index;  //当前Buffer缓冲的是哪一块

    public DirectMemoryBuffer(DirectMemoryChannel directMemoryChannel, int index) {
        this.directMemoryChannel = directMemoryChannel;
        this.index = index;
    }

    public CountDownLatch getBufferFinished() {
        return bufferFinished;
    }

    public void setBufferFinished(CountDownLatch bufferFinished) {
        this.bufferFinished = bufferFinished;
    }

    public CountDownLatch getBufferOutFinished() {
        return bufferOutFinished;
    }

    public void setBufferOutFinished(CountDownLatch bufferOutFinished) {
        this.bufferOutFinished = bufferOutFinished;
    }

    public DirectMemoryChannel getDirectMemoryChannel() {
        return directMemoryChannel;
    }

    public void setDirectMemoryChannel(DirectMemoryChannel directMemoryChannel) {
        this.directMemoryChannel = directMemoryChannel;
    }
}
