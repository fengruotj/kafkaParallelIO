package com.basic.benchmark.task;

import com.basic.util.BenchmarkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by wang on 17-11-12.
 */
public class NIOTask implements Runnable{

    private SocketChannel socketChannel;
    private static Logger logger= LoggerFactory.getLogger(FileTransferToTask.class);

    private int taskIndex=0;

    public NIOTask(SocketChannel socketChannel,int taskIndex) {
        this.socketChannel = socketChannel;
        this.taskIndex=taskIndex;
    }

    @Override
    public void run() {
        FileChannel inChannel = null;
        try {
            inChannel = FileChannel.open(Paths.get(BenchmarkConstants.filePath), StandardOpenOption.READ);
            ByteBuffer buffer = ByteBuffer.allocate(1024*1024);
            long position=taskIndex*BenchmarkConstants.transferBufferSize;
            long length=0;
            while (true){
                long remainCount=inChannel.size()-position;
//                long count= BenchmarkConstants.transferBufferSize<remainCount? BenchmarkConstants.transferBufferSize:remainCount;
                long n = inChannel.read(buffer, position);
                if(n<=0||position>=inChannel.size())
                    break;
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
                length+=n;
                position+= BenchmarkConstants.transferThreadNum* BenchmarkConstants.transferBufferSize;

            }
            socketChannel.shutdownOutput();
            socketChannel.close();
            logger.info("SendTask size: "+length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
