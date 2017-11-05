package com.basic.benchmark;

import com.basic.benchmark.task.FileTransferToTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * locate com.basic.benchmark
 * Created by 79875 on 2017/11/4.
 * 并行发送TransferTo KafkaServer
 * java -cp nioAction-1.0-SNAPSHOT.jar com.basic.benchmark.KafkaServer
 */
public class KafkaServer {
    private static Logger logger= LoggerFactory.getLogger(FileTransferToTask.class);
    private static ExecutorService executorService = Executors.newFixedThreadPool(Constants.transferThreadNum);

    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        ssChannel.bind(new InetSocketAddress(9898));

        for(int i=0;i<Constants.transferThreadNum;i++){
            SocketChannel sChannel = ssChannel.accept();
            FileTransferToTask fileTransferToTask=new FileTransferToTask(sChannel);
            executorService.submit(fileTransferToTask);
        }
        long startTimeMills = System.currentTimeMillis();

        executorService.shutdown();
        executorService.awaitTermination(10000000, TimeUnit.SECONDS);
        long endTimeMills = System.currentTimeMillis();
        logger.info("delayTime: "+(endTimeMills-startTimeMills));
    }
}
