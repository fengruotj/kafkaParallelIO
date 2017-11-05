package com.basic.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * locate com.basic.benchmark
 * Created by 79875 on 2017/11/5.
 * 普通IO测试 IOClient
 * java -cp nioAction-1.0-SNAPSHOT.jar com.basic.benchmark.IOClient
 */
public class IOClient {
    private static Logger logger= LoggerFactory.getLogger(IOClient.class);

    public static void main(String[] args) throws IOException {
        Socket socket=new Socket(Constants.hostname,8898);

        InputStream socketInputStream = socket.getInputStream();

        long startTimeMills = System.currentTimeMillis();
        byte[] buf=new byte[1024*1024];
        int count;
        long length=0L;
        while ((count=socketInputStream.read(buf))!=-1){
            length+=count;
        }

        long endTimeMills = System.currentTimeMillis();
        logger.info("SendTask size: "+length);
        logger.info("delayTime: "+(endTimeMills-startTimeMills));
        socketInputStream.close();
        socket.close();
    }
}
