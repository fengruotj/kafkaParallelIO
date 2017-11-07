package com.basic.core.bufferinterface;

import com.basic.core.channel.DirectMemoryChannel;

import java.util.EventListener;

/**
 * HDFSBuffer DataOuput 输出接口
 * 编程人员自定义如何读取这样一个有序的顺序byteBuffer数据块
 */
public interface BufferdataOutputHandler extends EventListener {
    public int BufferdataOutput(DirectMemoryChannel directMemoryChannel, int bufferindex);
}
