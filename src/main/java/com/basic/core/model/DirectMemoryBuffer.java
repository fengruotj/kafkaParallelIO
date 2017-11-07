package com.basic.core.model;

import com.basic.core.channel.DirectMemoryChannel;

/**
 * locate com.basic.core.model
 * Created by 79875 on 2017/11/6.
 */
public class DirectMemoryBuffer {
    DirectMemoryChannel directMemoryChannel;

    private boolean bufferFinished=false;//directMemory是否缓冲完成
    private boolean bufferOutFinished=true;//directMemory是否缓冲输出完成

    private int inputSplitNum;//当前Buffer缓冲的是哪一块

    public DirectMemoryBuffer(DirectMemoryChannel directMemoryChannel, int inputSplitNum) {
        this.directMemoryChannel = directMemoryChannel;
        this.inputSplitNum = inputSplitNum;
    }

    public boolean isBufferFinished() {
        return bufferFinished;
    }

    public void setBufferFinished(boolean bufferFinished) {
        this.bufferFinished = bufferFinished;
    }

    public boolean isBufferOutFinished() {
        return bufferOutFinished;
    }

    public void setBufferOutFinished(boolean bufferOutFinished) {
        this.bufferOutFinished = bufferOutFinished;
    }

    public DirectMemoryChannel getDirectMemoryChannel() {
        return directMemoryChannel;
    }

    public void setDirectMemoryChannel(DirectMemoryChannel directMemoryChannel) {
        this.directMemoryChannel = directMemoryChannel;
    }

    public int getInputSplitNum() {
        return inputSplitNum;
    }

    public void setInputSplitNum(int inputSplitNum) {
        this.inputSplitNum = inputSplitNum;
    }
}
