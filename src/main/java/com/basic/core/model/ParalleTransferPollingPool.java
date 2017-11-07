//package com.basic.core.model;
//
//import com.basic.core.channel.DirectMemoryChannel;
//import com.basic.core.task.FileTransferToMemoryTask;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.nio.channels.FileChannel;
//import java.nio.channels.SocketChannel;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * locate com.basic.core.model
// * Created by 79875 on 2017/11/6.
// */
//public class ParalleTransferPollingPool {
//    private static Logger logger= LoggerFactory.getLogger(ParalleTransferPollingPool.class);
//
//    private int directMemroyBufferNum;
//
//    private DirectMemoryBuffer[] directMemoryBuffers;
//    private List<InputSplit> inputSplits;
//    private int positionSplit = 0;    //当前读取inputSplits文件下标
//    private FileChannel fileChannel;    //FileChannel管道
//    private SocketChannel socketChannel;    //SocketChannel管道
//    private ExecutorService executorService = Executors.newCachedThreadPool();
//
//    public ParalleTransferPollingPool(int directMemroyBufferNum) {
//        this.directMemroyBufferNum = directMemroyBufferNum;
//    }
//
//    /**
//     * 调用初始化工作
//     */
//    public void open(FileChannel fileChannel,SocketChannel socketChannel){
//        this.fileChannel=fileChannel;
//        this.socketChannel=socketChannel;
//        directMemoryBuffers=new DirectMemoryBuffer[directMemroyBufferNum];
//    }
//
//    /**
//     * 初始化InputSplit
//     * @param position
//     * @param count
//     * @return
//     */
//    public List<InputSplit> initInputSplit(long position,long count){
//        List<InputSplit> inputSplits=new ArrayList<>();
//        long bufferSize = count / directMemroyBufferNum;
//        long remainCount=count;
//        long start=position;
//        while (remainCount<=0){
//            long total= bufferSize<remainCount? bufferSize:remainCount;
//            inputSplits.add(new InputSplit(start,start+total,total));
//            remainCount-=total;
//            start+=total;
//        }
//        return inputSplits;
//    }
//
//
//    /**
//     * 缓冲区输出完毕继续缓存下一块Block
//     * @param Num 当前输出缓冲块的下标
//     */
//    public void bufferNextDirectMemory(int Num) throws IOException, InterruptedException {
//        int inputSplitNum=directMemoryBuffers[Num].getInputSplitNum();
//        long length=inputSplits.get(Num).getLength();
//        if(inputSplitNum<inputSplits.size()){
//            logger.debug("bufferNextDirectMemory------------ NUM: "+Num+" inputSplitNum:"+inputSplitNum);
//            //bufferArray[Num].setBufferOutFinished(false);
//            addFileTransferToMemoryTask(Num,0,length);
//            directMemoryBuffers[Num].setInputSplitNum(inputSplitNum+directMemroyBufferNum);
//            positionSplit++;
//        }
//    }
//
//    /**
//     * 并行从FileChannel输出到SocketChannel中
//     * @param position 起始位置
//     * @param count 读取文件大小
//     * @return
//     * @throws Exception
//     */
//    public long paralleTransferToSocket(long position,long count) throws Exception {
//        long n = 0;
//        long start=position;
//        long bufferSize = count / directMemroyBufferNum;
//        inputSplits=initInputSplit(0,count);
//        for(int i=0;i<directMemroyBufferNum;i++){
//            addFileTransferToMemoryTask(i,start,bufferSize);
//            start+=bufferSize;
//        }
//
//        for(int i=0;i<directMemroyBufferNum;i++){
//            directMemoryBuffers[i].getBufferFinished().await();
//        }
//
//        n = outPutToSocketChannel(socketChannel);
//        return n;
//    }
//
//    /**
//     * 数据从FileChannel 调用TransferTo方法到MemoryChannel中
//     * @param index
//     * @param position
//     * @param count
//     * @throws IOException
//     */
//    public void addFileTransferToMemoryTask(int index, long position,long count) throws IOException {
//        DirectMemoryChannel directMemoryChannel=new DirectMemoryChannel();
//        directMemoryChannel.open((int) count);
//        directMemoryBuffers[index]=new DirectMemoryBuffer(directMemoryChannel,index);
//        FileTransferToMemoryTask task=new FileTransferToMemoryTask(position,count,fileChannel,directMemoryBuffers[index]);
//        executorService.submit(task);
//        logger.info("addFileTransferToMemoryTask index: "+index);
//    }
//
//    public class DirectMemoryPoolControlTask implements Runnable{
//
//        @Override
//        public void run() {
//            while (true){
//                try {
//                    for(int i=0;i<directMemroyBufferNum;i++){
//                        isBufferoutFinished(i);
//                        bufferNextDirectMemory(i);
//                    }
//                    if(positionSplit>=inputSplits.size()){
//                        //bufferoutputOver=true;
//                        executorService.shutdown();//顺序关闭，执行以前提交的任务，但不接受新任务
//                        if(executorService.isTerminated()){
//                            //所有子线程都结束任务
//                            logger.info("----------------dataInput over--------------");
//                            break;
//                        }
//                    }
//                    //Thread.sleep(100);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    /**
//     * DirectMemory 是否缓冲完成
//     * @param blocknum
//     * @return
//     */
//    public boolean isBufferoutFinished(int blocknum) {
//        return true;
//    }
//
//    /**
//     * 启动HdfsCachePool 缓充池
//     */
//    public void runHDFSCachePool(){
//        DirectMemoryPoolControlTask directMemoryPoolControlTask=new DirectMemoryPoolControlTask();
//        Thread thread2 = new Thread(directMemoryPoolControlTask);
//        thread2.start();
//    }
//
//    /**
//     * 将结果全部输出到SocketChannel中
//     * * @param socketChannel
//     */
//    public long outPutToSocketChannel(SocketChannel socketChannel) throws IOException {
//        long length=0L;
//        for(int i=0;i<directMemroyBufferNum;i++){
//            length+=directMemoryBuffers[i].getDirectMemoryChannel().sendToSocketChannel(socketChannel);
//        }
//        return length;
//    }
//
//    public ExecutorService getExecutorService() {
//        return executorService;
//    }
//}
