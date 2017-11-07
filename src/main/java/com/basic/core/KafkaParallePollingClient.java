package com.basic.core;

import com.basic.util.BenchmarkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * locate com.basic.core
 * Created by 79875 on 2017/11/7.
 * 多线程并行发送TransferTo到DirectMemoryChannel[],然后DirectMemoryChannel排序好后按顺序发送到SocketChannel KafkaParalleClient
 * 并且每当一个缓冲区缓冲完成后，线程开始缓冲下一个缓冲区
 * java -cp nioAction-1.0-SNAPSHOT.jar com.basic.core.KafkaParalleClient
 */
public class KafkaParallePollingClient {
    private static Logger logger= LoggerFactory.getLogger(KafkaParallePollingClient.class);

    public static void main(String[] args) throws IOException {
        long startTimeMills = System.currentTimeMillis();
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(BenchmarkConstants.hostname, 9898));

        ByteBuffer buf = ByteBuffer.allocate(1024*1024*10);
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

        long endTimeMills = System.currentTimeMillis();
        logger.info("delayTime: "+(endTimeMills-startTimeMills));
    }
}
