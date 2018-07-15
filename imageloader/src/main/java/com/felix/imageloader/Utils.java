package com.felix.imageloader;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Administrator
 */
public class Utils {

    /**
     * 将输入流中的数据导入到输出流中
     *
     * @param is 输入流
     * @param os 输出流
     */
    public static void copyStream(InputStream is, OutputStream os) {
        final int bufferSize = 1024;
        try {
            byte[] bytes = new byte[bufferSize];
            for (; ; ) {
                int count = is.read(bytes, 0, bufferSize);
                if (count == -1) {
                    break;
                }
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}