package com.basic.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * locate com.basic.benchmark
 * Created by 79875 on 2017/11/5.
 * 一次性串行发送TransferTo KafkaServer
 * java -cp nioAction-1.0-SNAPSHOT.jar com.basic.benchmark.KafkaSingelServer
 */
public class KafkaSingelServer {
    private static Logger logger= LoggerFactory.getLogger(KafkaSingelServer.class);

    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        ssChannel.bind(new InetSocketAddress(9898));

        FileChannel inChannel = null;
        SocketChannel socketChannel = ssChannel.accept();

        long startTimeMills = System.currentTimeMillis();
        try {
            inChannel = FileChannel.open(Paths.get(Constants.filePath), StandardOpenOption.READ);
            long position=0;
            long length=0;
            while (true){
                long remainCount=inChannel.size()-position;
                long count=Constants.singelTransferBufferSize<remainCount?Constants.singelTransferBufferSize:remainCount;
                long n=inChannel.transferTo(position,count,socketChannel);
                length+=n;
                position+=Constants.singelTransferBufferSize;
                if(n<=0||position>=inChannel.size())
                    break;
            }
            socketChannel.shutdownOutput();
            socketChannel.close();
            logger.info("SendTask size: "+length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTimeMills = System.currentTimeMillis();
        logger.info("delayTime: "+(endTimeMills-startTimeMills));
    }
}
