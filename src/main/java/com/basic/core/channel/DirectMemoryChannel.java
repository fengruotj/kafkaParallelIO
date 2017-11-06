package com.basic.core.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * locate com.basic.core
 * Created by 79875 on 2017/11/6.
 * 存放在页面内存中的DirectMemoryChannel
 */
public class DirectMemoryChannel implements WritableByteChannel {
    private static Logger logger= LoggerFactory.getLogger(DirectMemoryChannel.class);

    private boolean isOpen;

    private ByteBuffer byteBuffer;

    /**
     * open DirectMemoryChannel
     */
    public void open(int byteBufferSize){
        isOpen=true;
        prepare(byteBufferSize);
    }

    private void prepare(int byteBufferSize){
        byteBuffer =ByteBuffer.allocateDirect(byteBufferSize);
        //byteBuffer =ByteBuffer.allocate(byteBufferSize);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        byteBuffer.put(src);
        src.clear();
        return src.limit();
    }

    @Override
    public boolean isOpen() {
        logger.debug("isOpen()");
        return isOpen;
    }

    @Override
    public void close() throws IOException {
        logger.debug("close()");
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public int sendToSocketChannel(SocketChannel socketChannel) throws IOException {
        byteBuffer.flip();
        int length = socketChannel.write(byteBuffer);
        return length;
    }
}
