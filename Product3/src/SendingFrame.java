import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class SendingFrame extends Thread {
    private static int udpPort = 6663;
    private static int port = 6666;
    private static ServerSocket serverSocket;
    private static DatagramSocket udpSocket;
    public static Socket socket;
    public BufferedImage frame;
    
    //static int listen_handshake = 6675;
    static int listenport =6673;
    static TargetDataLine targetDataLine;
    static AudioFormat format;
    static DataLine.Info dataLineInfo;
    static int sampleRate = 16000;
    static int listenMinBufSize = 4096;
    //static ServerSocket listenServerSocket;
    //static Socket listenSocket;
    static DatagramSocket listenDataSocket;
    static OutputStream out;
    
    
    public static boolean livefeed = true;
    static boolean listen = false;

    public void run() {
        
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        try {
            //listenServerSocket = new ServerSocket(listen_handshake);
            listenDataSocket = new DatagramSocket(listenport);
            serverSocket.setSoTimeout(0);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        
        while (true) {
            
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            DatagramPacket dgp;
            byte[] data = new byte[listenMinBufSize];
            
            try{
                format = new AudioFormat(sampleRate, 16, 1, true, false);
                dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
                targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
                targetDataLine.open(format);
                targetDataLine.start();
            }catch (LineUnavailableException e){
                e.printStackTrace();
            }
            
            InetAddress destination = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress();

            if (frame == null) continue;
            
            while(livefeed) {

                long time1 = System.currentTimeMillis();
                try {
                    
                    //System.out.println("####################Live feed started sending frame");
                    /*OutputStream out = socket.getOutputStream();
                    out.write(1);
                    System.out.println("####################Live feed");
                    out.flush(); */             
                    
                    if (!socket.getInetAddress().isReachable(100)){
                        System.out.println("................ Live feed break");
                        continue;
                    }else {
                        //OutputStream out = socket.getOutputStream();
                        //out.write(1);
                        System.out.println("####################Live feed");
                        //out.flush();
                    }
                    
                    //InputStream in = socket.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(frame, "jpg", baos);
                    byte[] buf = baos.toByteArray();
                    
                    udpSocket = new DatagramSocket(udpPort);

                    //DataOutputStream dout = new DataOutputStream(out);
                    //dout.writeInt(buf.length);
                    //System.out.println(buf.length);
                    //in.read();
                  
                    InetAddress serverAddress = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress();
                    DatagramPacket imgPacket = new DatagramPacket(buf, buf.length, serverAddress, udpPort);
                    udpSocket.send(imgPacket);
                    udpSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    
                    System.out.println("####################Live feed Exception Catch");
                    break;
                }
                    
                long time2 = System.currentTimeMillis();
                //System.out.println("time = " + (time2 - time1));
                
                if (listen){
                    try{
                        targetDataLine.read(data, 0, data.length);
                        dgp = new DatagramPacket(data,data.length,destination,listenport);
                        listenDataSocket.send(dgp);
                        System.out.println("..........................................................................................SENDING AUDIO DATA:" + String.valueOf(data));
                    }catch(IOException e){
                        targetDataLine.close();
                        break;
                    }
                }
            }
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            livefeed = true;
            listen = true;
        }
    }
}
