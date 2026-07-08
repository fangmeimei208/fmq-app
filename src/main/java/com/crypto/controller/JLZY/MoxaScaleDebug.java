package com.crypto.controller.JLZY;

import java.io.InputStream;
import java.net.Socket;

public class MoxaScaleDebug {
    private static final String SERVER_IP = "10.180.248.160";
    private static final int SERVER_PORT = 950;
    private static final int BUFFER_SIZE = 1024;

    public static void tcpSerialRead() {
        Socket socket = null;
        InputStream in = null;
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = socket.getInputStream();
            System.out.println("TCP连接成功，正在读取地磅...");
            byte[] buf = new byte[BUFFER_SIZE];
            int len;
            while (true){
                len = in.read(buf);
                if(len <=0){
                    System.out.println("通道断开");
                    break;
                }
                // 打印原始16进制
                StringBuilder hexSb=new StringBuilder();
                for(int i=0;i<len;i++){
                    String s=Integer.toHexString(0xff&buf[i]);
                    if(s.length()==1) hexSb.append("0");
                    hexSb.append(s).append(" ");
                }
                System.out.println("原始HEX:"+hexSb);
                // 打印原始字符串
                String raw=new String(buf,0,len);
                System.out.println("原始字符:"+raw);
            }
        } catch (Exception e) {
            System.out.println("异常：" + e.getMessage());
        } finally {
            try {if(in!=null)in.close();if(socket!=null)socket.close();}catch(Exception e){}
        }
    }
    public static void main(String[] args) {
        tcpSerialRead();
    }
}

