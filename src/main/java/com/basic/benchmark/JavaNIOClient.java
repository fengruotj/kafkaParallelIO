package com.basic.benchmark;

import com.basic.util.BenchmarkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * locate com.basic.benchmark
 * Created by 79875 on 2017/11/5.
 * JavaNIO测试 JavaNIOClient
 * java -cp nioAction-1.0-SNAPSHOT.jar com.basic.benchmark.JavaNIOClient
 */
public class JavaNIOClient {
    private static Logger logger= LoggerFactory.getLogger(JavaNIOClient.class);

    public static void main(String[] args) throws IOException {
        SocketChannel sChannel = SocketChannel.open(new InetSocketAddress(BenchmarkConstants.hostname, 9898));

        long startTimeMills = System.currentTimeMillis();
        ByteBuffer buf = ByteBuffer.allocate(1024*1024);
        while(sChannel.read(buf) != -1){
            buf.flip();
            buf.clear();
        }

        long endTimeMills = System.currentTimeMillis();
        logger.info("delayTime: "+(endTimeMills-startTimeMills));
        sChannel.close();
    }
}
