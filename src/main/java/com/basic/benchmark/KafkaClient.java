package com.basic.benchmark;

import com.basic.benchmark.task.ReceiverTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * locate com.basic.benchmark
 * Created by 79875 on 2017/11/4.
 * 并行发送TransferTo KafkaClient
 * java -cp nioAction-1.0-SNAPSHOT.jar com.basic.benchmark.KafkaClient
 */
public class KafkaClient {
    private static ExecutorService executorService = Executors.newFixedThreadPool(Constants.transferThreadNum);
    private static Logger logger= LoggerFactory.getLogger(KafkaClient.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        long startTimeMills = System.currentTimeMillis();

        for(int i=0;i<Constants.transferThreadNum;i++){
            SocketChannel sChannel = SocketChannel.open(new InetSocketAddress(Constants.hostname, 9898));
            ReceiverTask receiverTask=new ReceiverTask(sChannel);
            executorService.submit(receiverTask);
        }

        executorService.shutdown();
        executorService.awaitTermination(10000000, TimeUnit.SECONDS);
        long endTimeMills = System.currentTimeMillis();
        logger.info("delayTime: "+(endTimeMills-startTimeMills));
    }
}
