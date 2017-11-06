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
 * 一次性串行发送TransferTo KafkaClient
 * java -cp nioAction-1.0-SNAPSHOT.jar com.basic.benchmark.KafkaSingelClient
 */
public class KafkaSingelClient {
    private static Logger logger= LoggerFactory.getLogger(KafkaSingelClient.class);

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
