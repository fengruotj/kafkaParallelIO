package com.basic.benchmark.task;

import com.basic.benchmark.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * locate com.basic.benchmark.task
 * Created by 79875 on 2017/11/4.
 */
public class FileTransferToTask implements Runnable {

    private SocketChannel socketChannel;
    private static Logger logger= LoggerFactory.getLogger(FileTransferToTask.class);

    public FileTransferToTask(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void run() {
        FileChannel inChannel = null;
        try {
            inChannel = FileChannel.open(Paths.get(Constants.filePath), StandardOpenOption.READ);
            long position=0;
            long length=0;
            while (true){
                long remainCount=inChannel.size()-position;
                long count=Constants.transferBufferSize<remainCount?Constants.transferBufferSize:remainCount;
                long n=inChannel.transferTo(position,count,socketChannel);
                length+=n;
                position+=Constants.transferThreadNum*Constants.transferBufferSize;
                if(n<=0||position>=inChannel.size())
                    break;
            }
            socketChannel.shutdownOutput();
            socketChannel.close();
            logger.info("SendTask size: "+length);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
