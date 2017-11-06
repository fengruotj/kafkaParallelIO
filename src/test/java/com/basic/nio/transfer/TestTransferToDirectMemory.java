package com.basic.nio.transfer;

import com.basic.core.channel.DirectMemoryChannel;
import org.junit.Test;

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
    @Test
    public void testTransferTo() throws IOException {
        final FileChannel inChannel = FileChannel.open(Paths.get("D:\\tmp\\kafka-core-logs\\controller.log"), StandardOpenOption.READ);
        DirectMemoryChannel directMemoryChannel =new DirectMemoryChannel();
        directMemoryChannel.open((int) inChannel.size());
        long count = inChannel.transferTo(0, 1024 * 1024, directMemoryChannel);
        System.out.println(count);
        ByteBuffer byteBuffer = directMemoryChannel.getByteBuffer();
        System.out.println(new String(byteBuffer.array(),0,byteBuffer.limit()));
    }

    @Test
    public void testTransferToClient() throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 9898));
        ByteBuffer buf = ByteBuffer.allocate(1024*1024);
        long length=0;
        try {
            while(socketChannel.read(buf) != -1){
                buf.flip();
                System.out.println(new String(buf.array(),0,buf.limit()));
                length+=buf.limit();
                buf.clear();
            }
            System.out.println("RecevieTask size: "+length);
            socketChannel.shutdownInput();
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTransferToServer() throws IOException {
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        FileChannel inChannel = FileChannel.open(Paths.get("D:\\tmp\\kafka-core-logs\\controller.log"), StandardOpenOption.READ);
        ssChannel.bind(new InetSocketAddress(9898));
        //3. 获取客户端连接的通道
        SocketChannel sChannel = ssChannel.accept();

        DirectMemoryChannel directMemoryChannel =new DirectMemoryChannel();
        directMemoryChannel.open((int) inChannel.size());
        long count = inChannel.transferTo(0,1024*1024, directMemoryChannel);
        System.out.println("transferToDirectByteChannel count: "+count);
        directMemoryChannel.sendToSocketChannel(sChannel);

        sChannel.shutdownOutput();
        sChannel.close();
        ssChannel.close();
    }

}
