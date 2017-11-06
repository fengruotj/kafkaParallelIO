package com.basic.core;

import com.basic.core.model.ParalleTransferToPool;
import com.basic.util.BenchmarkConstants;
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
 * Created by 79875 on 2017/11/4.
 * 多线程并行发送TransferTo到DirectMemoryChannel[],然后DirectMemoryChannel排序好后按顺序发送到SocketChannel KafkaParalleServer
 * java -cp nioAction-1.0-SNAPSHOT.jar com.basic.core.KafkaParalleServer
 */
public class KafkaParalleServer {
    private static Logger logger= LoggerFactory.getLogger(KafkaParalleServer.class);

    public static void main(String[] args) throws IOException {
        ParalleTransferToPool paralleTransferToPool =new ParalleTransferToPool(10);
        paralleTransferToPool.open();

        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        ssChannel.bind(new InetSocketAddress(9898));

        FileChannel fileChannel = null;
        SocketChannel socketChannel = ssChannel.accept();

        long startTimeMills = System.currentTimeMillis();
        try {
            fileChannel = FileChannel.open(Paths.get(BenchmarkConstants.filePath), StandardOpenOption.READ);
            long position=0;
            long length=0;
            while (true){
                long remainCount=fileChannel.size()-position;
                long count= BenchmarkConstants.paralleTransferBufferSize<remainCount? BenchmarkConstants.paralleTransferBufferSize:remainCount;
                long n= paralleTransferToPool.paralleTransferToSocket(position,count,fileChannel,socketChannel);
                length+=n;
                position+= BenchmarkConstants.paralleTransferBufferSize;
                if(n<=0||position>=fileChannel.size())
                    break;
            }
            socketChannel.shutdownOutput();
            socketChannel.close();
            logger.info("SendTask size: "+length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        paralleTransferToPool.getExecutorService().shutdown();
        long endTimeMills = System.currentTimeMillis();
        logger.info("delayTime: "+(endTimeMills-startTimeMills));
    }
}
