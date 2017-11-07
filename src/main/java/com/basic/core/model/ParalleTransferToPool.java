package com.basic.core.model;

import com.basic.core.channel.DirectMemoryChannel;
import com.basic.core.task.FileTransferToMemoryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * locate com.basic.core.model
 * Created by 79875 on 2017/11/6.
 */
public class ParalleTransferToPool {
    private static Logger logger= LoggerFactory.getLogger(ParalleTransferToPool.class);

    private int directMemroyBufferNum;

    private DirectMemoryBuffer[] directMemoryBuffers;
    private CountDownLatch countDownLatch;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public ParalleTransferToPool(int directMemroyBufferNum) {
        this.directMemroyBufferNum = directMemroyBufferNum;
    }

    /**
     * 调用初始化工作
     */
    public void open(){
        directMemoryBuffers=new DirectMemoryBuffer[directMemroyBufferNum];
    }

    /**
     * 并行从FileChannel输出到SocketChannel中
     * @param position 起始位置
     * @param count 读取文件大小
     * @param fileChannel   FileChannel管道
     * @param socketChannel SocketChannel管道
     * @return
     * @throws Exception
     */
    public long paralleTransferToSocket(long position,long count,FileChannel fileChannel,SocketChannel socketChannel) throws Exception {
        countDownLatch=new CountDownLatch(directMemroyBufferNum);

        long n = 0;
        long start=position;
        long bufferSize = count / directMemroyBufferNum;
        for(int i=0;i<directMemroyBufferNum;i++){
            addFileTransferToMemoryTask(i,start,bufferSize,fileChannel);
            start+=bufferSize;
        }

        countDownLatch.await();
        n = outPutToSocketChannel(socketChannel);
        return n;
    }

    /**
     * 数据从FileChannel 调用TransferTo方法到MemoryChannel中
     * @param index
     * @param position
     * @param count
     * @param fileChannel
     * @throws IOException
     */
    private void addFileTransferToMemoryTask(int index, long position,long count,FileChannel fileChannel) throws IOException {
        DirectMemoryChannel directMemoryChannel=new DirectMemoryChannel();
        directMemoryChannel.open((int) count);
        directMemoryBuffers[index]=new DirectMemoryBuffer(directMemoryChannel,index);
        FileTransferToMemoryTask task=new FileTransferToMemoryTask(position,count,fileChannel,directMemoryBuffers[index],countDownLatch);
        executorService.submit(task);
        logger.debug("addFileTransferToMemoryTask index: "+index);
    }

    /**
     * 将结果全部输出到SocketChannel中
     * * @param socketChannel
     */
    public long outPutToSocketChannel(SocketChannel socketChannel) throws IOException {
        long length=0L;
        for(int i=0;i<directMemroyBufferNum;i++){
            length+=directMemoryBuffers[i].getDirectMemoryChannel().sendToSocketChannel(socketChannel);
        }
        return length;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
