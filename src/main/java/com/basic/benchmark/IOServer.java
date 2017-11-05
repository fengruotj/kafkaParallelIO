package com.basic.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * locate com.basic.benchmark
 * Created by 79875 on 2017/11/5.
 * 普通IO测试 IOServer
 * java -cp nioAction-1.0-SNAPSHOT.jar com.basic.benchmark.IOServer
 */
public class IOServer {
    private static Logger logger= LoggerFactory.getLogger(IOServer.class);

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket=new ServerSocket(8898);
        FileInputStream inputStream=new FileInputStream(new File(Constants.filePath));

        Socket socket = serverSocket.accept();
        OutputStream socketOutputStream = socket.getOutputStream();

        long startTimeMills = System.currentTimeMillis();
        byte[] buf=new byte[1024*1024];
        int count;
        long length=0L;
        while ((count=inputStream.read(buf))!=-1){
            socketOutputStream.write(buf,0,count);
            length+=count;
        }

        long endTimeMills = System.currentTimeMillis();
        logger.info("SendTask size: "+length);
        logger.info("delayTime: "+(endTimeMills-startTimeMills));
        inputStream.close();
        socket.close();
        serverSocket.close();
    }
}
