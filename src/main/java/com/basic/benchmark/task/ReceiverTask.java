package com.basic.benchmark.task;

import com.basic.util.BenchmarkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * locate com.basic.benchmark.task
 * Created by 79875 on 2017/11/4.
 */
public class ReceiverTask implements Runnable {
    private static Logger logger= LoggerFactory.getLogger(ReceiverTask.class);

    private SocketChannel socketChannel;

    public ReceiverTask(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void run() {
        ByteBuffer buf = ByteBuffer.allocate((int)BenchmarkConstants.transferBufferSize);
        long length=0;
        try {
            while(socketChannel.read(buf) != -1){
                buf.flip();
                //System.out.println(new String(buf.array(),0,buf.limit()));
                length+=buf.limit();
                buf.clear();
            }
            logger.info("RecevieTask size: "+length);
            socketChannel.shutdownInput();
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
