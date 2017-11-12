package com.basic.benchmark;

import com.basic.benchmark.task.FileTransferToTask;
import com.basic.benchmark.task.NIOTask;
import com.basic.util.BenchmarkConstants;
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
 * Created by wang on 17-11-12.
 * 并行发送JavaNIO Server
 * java -cp nioAction-1.0-SNAPSHOT.jar com.basic.benchmark.JavaNIOParallelServer
 */
public class JavaNIOParallelServer {
    private static Logger logger= LoggerFactory.getLogger(FileTransferToTask.class);
    private static ExecutorService executorService = Executors.newFixedThreadPool(BenchmarkConstants.transferThreadNum);

    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        ssChannel.bind(new InetSocketAddress(9898));

        for(int i = 0; i< BenchmarkConstants.transferThreadNum; i++){
            SocketChannel sChannel = ssChannel.accept();
            NIOTask fileNIOTask=new NIOTask(sChannel,i);
            executorService.submit(fileNIOTask);
        }
        long startTimeMills = System.currentTimeMillis();

        executorService.shutdown();
        executorService.awaitTermination(10000000, TimeUnit.SECONDS);
        long endTimeMills = System.currentTimeMillis();
        logger.info("delayTime: "+(endTimeMills-startTimeMills));
    }

}
