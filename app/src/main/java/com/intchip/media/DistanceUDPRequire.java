package com.intchip.media;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2018/1/12.
 */

public class DistanceUDPRequire {

    public void distanceUDP(){

//        String msg = "Hello, World";
        String msg = "S";
        byte[] buf = msg.getBytes();
        try {
            InetAddress address = InetAddress.getByName("192.168.0.1");  //服务器地址
            int port = 8045;  //服务器的端口号
            //创建发送方的数据报信息
            DatagramPacket dataGramPacket = new DatagramPacket(buf, buf.length, address, port);
            DatagramSocket socket = new DatagramSocket();  //创建套接字
            socket.send(dataGramPacket);  //通过套接字发送数据

            //接收服务器反馈数据
//            byte[] backbuf = new byte[1024];
            byte[] backbuf = new byte[256];
            DatagramPacket backPacket = new DatagramPacket(backbuf, backbuf.length);
            socket.receive(backPacket);  //接收返回数据
            String backMsg = new String(backbuf, 0, backPacket.getLength());
            Log.d("TIEJIANG", "DistanceUDPRequire---distanceUDP receive data= "+backMsg);
//            System.out.println("服务器返回的数据为:" + backMsg);
            socket.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.d("TIEJIANG", "DistanceUDPRequire---distanceUDP UnknownHostException= "+e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("TIEJIANG", "DistanceUDPRequire---distanceUDP IOException= "+e.getMessage());
        }
    }
}
