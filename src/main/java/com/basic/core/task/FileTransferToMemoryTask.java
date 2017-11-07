package com.basic.core.task;

import com.basic.core.model.DirectMemoryBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.CountDownLatch;

/**
 * locate com.basic.core.task
 * Created by 79875 on 2017/11/6.
 */
public class FileTransferToMemoryTask implements Runnable {
    private static Logger logger= LoggerFactory.getLogger(FileTransferToMemoryTask.class);

    private long position;
    private long count;
    private FileChannel fileChannel;
    private DirectMemoryBuffer directMemoryBuffer;
    private CountDownLatch countDownLatch;

    public FileTransferToMemoryTask(long position, long count, FileChannel fileChannel, DirectMemoryBuffer directMemoryBuffer,CountDownLatch countDownLatch) {
        this.position = position;
        this.count = count;
        this.fileChannel = fileChannel;
        this.directMemoryBuffer = directMemoryBuffer;
        this.countDownLatch=countDownLatch;
    }

    @Override
    public void run() {
        long size = 0;
        try {
            size = fileChannel.transferTo(position, count, directMemoryBuffer.getDirectMemoryChannel());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            logger.debug("FileTransferToMemoryTask success "+" size: "+size);
            countDownLatch.countDown();
        }
    }
}
