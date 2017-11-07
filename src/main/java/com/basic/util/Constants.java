package com.basic.util;

/**
 * locate com.basic.core
 * Created by 79875 on 2017/11/7.
 */
public class Constants {
    static {
        PropertiesUtil.init("/core.properties");
    }
    public static final int transferToBuffersNum=Integer.valueOf(PropertiesUtil.getProperties("transferToBuffersNum"));
    public static final int transferToBufferSize=Integer.valueOf(PropertiesUtil.getProperties("transferToBufferSize"));
    public static final String filePath=PropertiesUtil.getProperties("filePath");
    public static final String hostname=PropertiesUtil.getProperties("hostname");
}
