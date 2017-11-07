package com.basic.nio.transfer;

import com.basic.core.channel.DirectMemoryChannel;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/**
 * locate com.basic.nio.transfer
 * Created by 79875 on 2017/9/24.
 */
public class TestTransferTo {
    private String filePath="G:\\\\GTA5\\\\3DMGAME-Grand_Theft_Auto_V.RLD.CHS.Green\\\\Grand Theft Auto V\\\\x64a.rpf";
//    private String fiePath=filePath;
    @Test
    public void testTransferTo() throws IOException {
        final FileChannel inChannel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ);
        DirectMemoryChannel directMemoryChannel =new DirectMemoryChannel();
        directMemoryChannel.open((int) inChannel.size());
        long count = inChannel.transferTo(0, 1024 * 1024, directMemoryChannel);
        System.out.println(count);
        ByteBuffer byteBuffer = directMemoryChannel.getByteBuffer();
        //System.out.println(new String(byteBuffer.array(),0,byteBuffer.limit()));
    }

    @Test
    public void testTransferToClient() throws IOException {
        FileChannel inChannel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ);
        SocketChannel sChannel = SocketChannel.open(new InetSocketAddress("localhost", 9898));
        inChannel.transferTo(0,1024*1024,sChannel);
        sChannel.shutdownOutput();
    }

    @Test
    public void testTransferToServerBlocking() throws IOException {
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        ssChannel.bind(new InetSocketAddress(9898));

        SocketChannel sChannel = ssChannel.accept();
        ByteBuffer buf = ByteBuffer.allocate(1024);
        while(sChannel.read(buf) != -1){
            buf.flip();
            System.out.println(new String(buf.array(),0,buf.limit()));
            buf.clear();
        }
        sChannel.close();
        ssChannel.close();
    }

    @Test
    public void testTransferToServerNonBlocking() throws IOException {
        ServerSocketChannel ssChannel = ServerSocketChannel.open();

        ssChannel.configureBlocking(false);

        ssChannel.bind(new InetSocketAddress(9898));

        Selector selector = Selector.open();

        ssChannel.register(selector, SelectionKey.OP_ACCEPT);

        while(selector.select() > 0){

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();

            while(it.hasNext()){
                SelectionKey sk = it.next();

                if(sk.isAcceptable()){

                    SocketChannel sChannel = ssChannel.accept();

                    sChannel.configureBlocking(false);

                    sChannel.register(selector, SelectionKey.OP_READ);
                }else if(sk.isReadable()){

                    SocketChannel sChannel = (SocketChannel) sk.channel();

                    ByteBuffer buf = ByteBuffer.allocate(1024);

                    int len = 0;
                    while((len = sChannel.read(buf)) > 0 ){
                        buf.flip();
                        System.out.println(new String(buf.array(), 0, len));
                        buf.clear();
                    }
                }

                it.remove();
            }
        }
    }
}
