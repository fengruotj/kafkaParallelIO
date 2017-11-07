package com.basic.core.task;

import com.basic.core.model.DirectMemoryBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * locate com.basic.core.task
 * Created by 79875 on 2017/11/6.
 */
public class FileTransferToPollingMemoryTask implements Runnable {
    private static Logger logger= LoggerFactory.getLogger(FileTransferToPollingMemoryTask.class);

    private long position;
    private long count;
    private FileChannel fileChannel;
    private DirectMemoryBuffer directMemoryBuffer;

    public FileTransferToPollingMemoryTask(long position, long count, FileChannel fileChannel, DirectMemoryBuffer directMemoryBuffer) {
        this.position = position;
        this.count = count;
        this.fileChannel = fileChannel;
        this.directMemoryBuffer = directMemoryBuffer;

    }

    @Override
    public void run() {
        long size = 0;
        directMemoryBuffer.getDirectMemoryChannel().getByteBuffer().clear();
        try {
            size = fileChannel.transferTo(position, count, directMemoryBuffer.getDirectMemoryChannel());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            logger.debug("FileTransferToMemoryTask success "+" size: "+size+" inputSplit: "+directMemoryBuffer.getInputSplitNum());
        }
        directMemoryBuffer.setBufferFinished(true);
    }
}
