package com.basic.nio.transfer;

import com.basic.core.channel.DirectMemoryChannel;
import com.basic.core.model.ParalleTransferToPool;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * locate com.basic.nio.transfer
 * Created by 79875 on 2017/11/6.
 */
public class TestTransferToDirectMemory {
    private static Logger logger= LoggerFactory.getLogger(TestTransferToDirectMemory.class);

    private String filePath="G:\\\\GTA5\\\\3DMGAME-Grand_Theft_Auto_V.RLD.CHS.Green\\\\Grand Theft Auto V\\\\x64a.rpf";
    @Test
    public void testTransferToClient() throws IOException {
        SocketChannel sChannel = SocketChannel.open(new InetSocketAddress("localhost", 9898));
        long startTimeMills = System.currentTimeMillis();
        ByteBuffer buf = ByteBuffer.allocate(1024);
        while(sChannel.read(buf) != -1){
            buf.flip();
            //System.out.println(new String(buf.array(),0,buf.limit()));
            buf.clear();
        }
        sChannel.shutdownOutput();
        long endTimeMills = System.currentTimeMillis();
        logger.info("delayTime: "+(endTimeMills-startTimeMills));
        sChannel.close();
    }

    //  [ INFO ] [2017-11-07 09:02:11] com.basic.nio.transfer.TestTransferToDirectMemory [main:0] - delayTime: 154
    //  [ INFO ] [2017-11-07 09:02:11] com.basic.nio.transfer.TestTransferToDirectMemory [main:0] - delayTime: 135
    //  [ INFO ] [2017-11-07 09:02:11] com.basic.nio.transfer.TestTransferToDirectMemory [main:0] - delayTime: 117
    @Test
    public void testTransferToServer() throws IOException {
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        ssChannel.bind(new InetSocketAddress(9898));
        FileChannel inChannel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ);

        SocketChannel sChannel = ssChannel.accept();
        long startTimeMills = System.currentTimeMillis();
        inChannel.transferTo(0,1024*1024*10,sChannel);
        sChannel.shutdownOutput();
        long endTimeMills = System.currentTimeMillis();
        logger.info("delayTime: "+(endTimeMills-startTimeMills));

        sChannel.close();
        ssChannel.close();
    }
    @Test
    public void testTransferToDirectMemory() throws IOException {
        final FileChannel inChannel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ);
        DirectMemoryChannel directMemoryChannel =new DirectMemoryChannel();
        directMemoryChannel.open((int) inChannel.size());
        long count = inChannel.transferTo(0, 1024 * 1024, directMemoryChannel);
        System.out.println(count);
        ByteBuffer byteBuffer = directMemoryChannel.getByteBuffer();
        System.out.println(new String(byteBuffer.array(),0,byteBuffer.limit()));
    }

    @Test
    public void testParalleTransferToClient() throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 9898));

        long startTimeMills = System.currentTimeMillis();
        ByteBuffer buf = ByteBuffer.allocate(1024);
        long length=0;
        try {
            while(socketChannel.read(buf) != -1){
                buf.flip();
                //System.out.println(new String(buf.array(),0,buf.limit()));
                length+=buf.limit();
                buf.clear();
            }
            logger.info("RecevieTask size: "+length);
            socketChannel.shutdownInput();

            long endTimeMills = System.currentTimeMillis();
            logger.info("delayTime: "+(endTimeMills-startTimeMills));
        } catch (IOException e) {
            e.printStackTrace();
        }
        socketChannel.close();
    }

    //    [ INFO ] [2017-11-07 09:02:11] com.basic.nio.transfer.TestTransferToDirectMemory [main:0] - delayTime: 216
    //    [ INFO ] [2017-11-07 09:02:11] com.basic.nio.transfer.TestTransferToDirectMemory [main:0] - delayTime: 178
    //    [ INFO ] [2017-11-07 09:02:11] com.basic.nio.transfer.TestTransferToDirectMemory [main:0] - delayTime: 169
    @Test
    public void testParalleTransferToServer() throws Exception {
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        ParalleTransferToPool paralleTransferToPool=new ParalleTransferToPool(10);
        FileChannel inChannel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ);
        ssChannel.bind(new InetSocketAddress(9898));
        //3. 获取客户端连接的通道
        SocketChannel sChannel = ssChannel.accept();

        long startTimeMills = System.currentTimeMillis();
        paralleTransferToPool.open();
        paralleTransferToPool.paralleTransferToSocket(0,1024*1024*10,inChannel,sChannel);

        sChannel.shutdownOutput();
        long endTimeMills = System.currentTimeMillis();
        logger.info("delayTime: "+(endTimeMills-startTimeMills));
        sChannel.close();
        ssChannel.close();
    }

}
