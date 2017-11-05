package com.basic.benchmark;
import com.basic.util.*;
/**
 * locate com.basic.benchmark
 * Created by 79875 on 2017/11/4.
 */
public class Constants {
    static {
        PropertiesUtil.init("/benchmark.properties");
    }
    public static final int transferBufferSize =Integer.valueOf(PropertiesUtil.getProperties("transferBufferSize"));
    public static final int transferThreadNum = Integer.valueOf(PropertiesUtil.getProperties("transferThreadNum"));
    public static final int singelTransferBufferSize =Integer.valueOf(PropertiesUtil.getProperties("singelTransferBufferSize"));
    public static final String filePath=PropertiesUtil.getProperties("filePath");
    public static final String hostname=PropertiesUtil.getProperties("hostname");
}
