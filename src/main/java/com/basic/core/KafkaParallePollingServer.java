package com.basic.core;

import com.basic.core.bufferinterface.BufferdataOutputHandler;
import com.basic.core.channel.DirectMemoryChannel;
import com.basic.core.model.ParalleTransferPollingPool;
import com.basic.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * locate com.basic.benchmark
 * Created by 79875 on 2017/11/4.
 * 多线程并行发送TransferTo到DirectMemoryChannel[],然后DirectMemoryChannel排序好后按顺序发送到SocketChannel KafkaParalleServer
 * java -cp nioAction-1.0-SNAPSHOT.jar com.basic.core.KafkaParallePollingServer
 */
public class KafkaParallePollingServer {
    private static Logger logger= LoggerFactory.getLogger(KafkaParallePollingServer.class);

    public static void main(String[] args) throws IOException {
        //ServerSocketChannel ssChannel = ServerSocketChannel.open();
        //ssChannel.bind(new InetSocketAddress(9898));

        FileChannel fileChannel = null;
        //final SocketChannel socketChannel = ssChannel.accept();

        long startTimeMills = System.currentTimeMillis();
        long length=0;

        fileChannel = FileChannel.open(Paths.get(Constants.filePath), StandardOpenOption.READ);

        ParalleTransferPollingPool pollingPool=new ParalleTransferPollingPool(4);
        pollingPool.open(fileChannel);
        try {
            pollingPool.paralleTransferToDirectMemory(0,fileChannel.size());
            pollingPool.directMemoryDataOutputOrder(new BufferdataOutputHandler() {
                @Override
                public void BufferdataOutput(DirectMemoryChannel directMemoryChannel, int bufferindex) {
                    try {
                       // directMemoryChannel.sendToSocketChannel(socketChannel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            //socketChannel.shutdownOutput();
            //socketChannel.close();
            logger.info("SendTask size: "+length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTimeMills = System.currentTimeMillis();
        logger.info("delayTime: "+(endTimeMills-startTimeMills));
    }
}
