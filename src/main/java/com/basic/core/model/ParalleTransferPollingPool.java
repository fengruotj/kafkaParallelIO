package com.basic.core.model;

import com.basic.core.bufferinterface.BufferdataOutputHandler;
import com.basic.core.channel.DirectMemoryChannel;
import com.basic.core.task.FileTransferToPollingMemoryTask;
import com.basic.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * locate com.basic.core.model
 * Created by 79875 on 2017/11/6.
 * 多线程并行发送TransferTo到DirectMemoryChannel[],然后DirectMemoryChannel排序好后按顺序发送到SocketChannel KafkaParalleServer
 * 并且每当一个缓冲区缓冲完成后，线程开始缓冲下一个缓冲区
 */
public class ParalleTransferPollingPool {
    private static Logger logger= LoggerFactory.getLogger(ParalleTransferPollingPool.class);

    private int directMemroyBufferNum;

    private DirectMemoryBuffer[] directMemoryBuffers;
    private List<InputSplit> inputSplits;
    private int positionSplit = 0;    //当前读取inputSplits文件下标
    private FileChannel fileChannel;    //FileChannel管道
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public ParalleTransferPollingPool(int directMemroyBufferNum) {
        this.directMemroyBufferNum = directMemroyBufferNum;
    }

    /**
     * 调用初始化工作
     */
    public void open(FileChannel fileChannel){
        this.fileChannel=fileChannel;
        directMemoryBuffers=new DirectMemoryBuffer[directMemroyBufferNum];
    }

    /**
     * 初始化InputSplit
     * @param position
     * @param count
     * @return
     */
    public List<InputSplit> initInputSplit(long position,long count){
        List<InputSplit> inputSplits=new ArrayList<>();
        long bufferSize = Constants.transferToBufferSize;
        long remainCount=count;
        long start=position;
        while (remainCount>0){
            long total= bufferSize<remainCount? bufferSize:remainCount;
            inputSplits.add(new InputSplit(start,start+total,total));
            remainCount-=total;
            start+=total;
        }
        return inputSplits;
    }

    /**
     * 初始化DirectoyrMemoryBuffers
     * @param inputSplits
     */
    private void initDirectoyrMemoryBuffers(List<InputSplit> inputSplits) {
        for(int i=0;i<directMemroyBufferNum;i++){
            DirectMemoryChannel directMemoryChannel=new DirectMemoryChannel();
            directMemoryChannel.open((int) inputSplits.get(i).getLength());
            directMemoryBuffers[i]=new DirectMemoryBuffer(directMemoryChannel,i);
        }
    }

    public void setInstance(int bufferindex, long count) throws IOException, InterruptedException {
        directMemoryBuffers[bufferindex].setBufferFinished(false);
        directMemoryBuffers[bufferindex].directMemoryChannel.setByteBuffer(ByteBuffer.allocateDirect((int) count));
    }

    /**
     * 是否buferBlock缓存输出完毕
     * @param blocknum blocknum下标
     * @return
     */
    public boolean isDirectMemoryOutFinished(int blocknum){
//        ByteBuffer byteBuffer = bufferArray[blocknum].byteBuffer;
//        if(byteBuffer.hasRemaining())
//            return false;
//        else return true;
        return directMemoryBuffers[blocknum].isBufferOutFinished();
    }

    /**
     *  是否buferBlock缓存完毕
     * @param blocknum blocknum下标
     * @return
     */
    public boolean isBufferDirectMemoryFinished(int blocknum){
        return directMemoryBuffers[blocknum].isBufferFinished();
    }

    /**
     * 缓冲区输出完毕继续缓存下一块Block
     * @param Num 当前输出缓冲块的下标
     */
    public void bufferNextDirectMemory(int Num) throws IOException, InterruptedException {
        int inputSplitNum=directMemoryBuffers[Num].getInputSplitNum();
        if(inputSplitNum<inputSplits.size()){
            long length=inputSplits.get(inputSplitNum).getLength();
            long start=inputSplits.get(inputSplitNum).getStart();
            logger.debug("bufferNextDirectMemory------------ NUM: "+Num+" inputSplitNum:"+inputSplitNum);
            directMemoryBuffers[Num].setBufferOutFinished(false);
            addFileTransferToMemoryTask(Num,start,length);
            directMemoryBuffers[Num].setInputSplitNum(inputSplitNum+directMemroyBufferNum);
            positionSplit++;
        }
    }

    /**
     * 并行从FileChannel输出到SocketChannel中
     * @param position 起始位置
     * @param count 读取文件大小
     * @return
     * @throws Exception
     */
    public void paralleTransferToDirectMemory(long position,long count) throws Exception {
        inputSplits=initInputSplit(position,count);
        initDirectoyrMemoryBuffers(inputSplits);

        DirectMemoryPoolControlTask directMemoryPoolControlTask=new DirectMemoryPoolControlTask();
        Thread thread2 = new Thread(directMemoryPoolControlTask);
        thread2.start();
    }

    /**
     * 数据从FileChannel 调用TransferTo方法到MemoryChannel中
     * @param NUM 当前输出缓冲块的下标
     * @param position
     * @param count
     * @throws IOException
     */
    public void addFileTransferToMemoryTask(int NUM, long position,long count) throws IOException, InterruptedException {
        setInstance(NUM,count);
        FileTransferToPollingMemoryTask task=new FileTransferToPollingMemoryTask(position,count,fileChannel,directMemoryBuffers[NUM]);
        executorService.submit(task);
        logger.debug("addFileTransferToMemoryTask index: "+NUM);
    }

    public class DirectMemoryPoolControlTask implements Runnable{

        @Override
        public void run() {
            while (true){
                try {
                    for(int i=0;i<directMemroyBufferNum;i++){
                        if(isDirectMemoryOutFinished(i)){
                            bufferNextDirectMemory(i);
                        }
                    }
                    if(positionSplit>=inputSplits.size()){
                        //bufferoutputOver=true;
                        executorService.shutdown();//顺序关闭，执行以前提交的任务，但不接受新任务
                        if(executorService.isTerminated()){
                            //所有子线程都结束任务
                            logger.info("----------------dataInput over--------------");
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * HdfsDataOutPut的辅助函数 得到当前hdfsDataOutput可以读取的blockNum
     * @param blockPosition
     * @return
     */
    private int getActiveBufferNum(int blockPosition){
        int bufferNum=directMemoryBuffers.length;
        List<InputSplit> inputSplitList=inputSplits;
        return  (inputSplitList.size()-blockPosition) < bufferNum ?inputSplitList.size() % bufferNum : bufferNum;
    }

    /**
     * HdfsBufferDataOutput 保持数据的有序性
     * bufferdataOutputHandler 用来处理顺序的读取的ByteBuffer
     * @param bufferdataOutputHandler
     */
    public void directMemoryDataOutputOrder(BufferdataOutputHandler bufferdataOutputHandler){
        int blockPosition=0;
        long length=0L;
        while (true){
            //if(hdfsCachePool.isIsbufferfinish()){
            //可以开始读取HdfsCachePool
            int activeBufferNum = getActiveBufferNum(blockPosition);
            for(int i=0;i<activeBufferNum;i++){
                while (!isBufferDirectMemoryFinished(i)){
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                directMemoryBuffers[i].setBufferOutFinished(false);
                DirectMemoryChannel directMemoryChannel = directMemoryBuffers[i].directMemoryChannel;
                logger.debug("---------start--------"+ directMemoryChannel.getByteBuffer() +" num:"+i+" blockPosition: "+blockPosition);

                length +=bufferdataOutputHandler.BufferdataOutput(directMemoryChannel, i);

                directMemoryBuffers[i].setBufferOutFinished(true);
                directMemoryBuffers[i].setBufferFinished(false);
                blockPosition++;
            }
            if(blockPosition>=inputSplits.size()){
                logger.info("----------------dataOuput over--------------");
                logger.info("SendTask size: "+length);
                break;
            }
        }
    }

    /**
     * 将结果全部输出到SocketChannel中
     * * @param socketChannel
     */
    private long outPutToSocketChannel(SocketChannel socketChannel) throws IOException {
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
