import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

import javax.imageio.ImageIO;

public class SendingFrame extends Thread {
    private static int udpPort = 6663;
    private static int port = 6666;
    private static ServerSocket serverSocket;
    private static DatagramSocket udpSocket;
    private static Socket socket;
    public BufferedImage frame;

    public void run() {
		
        try {
            serverSocket = new ServerSocket(port);
            udpSocket = new DatagramSocket(udpPort);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (frame == null) continue;
            new Thread(new Runnable(){
                @Override
                public void run() {
                    while(true) {

                        long time1 = System.currentTimeMillis();
                        try {
                            OutputStream out = socket.getOutputStream();
                            out.write(1);
                            out.flush();                       
                            //InputStream in = socket.getInputStream();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(frame, "jpg", baos);
                            byte[] buf = baos.toByteArray();

                            //DataOutputStream dout = new DataOutputStream(out);
                            //dout.writeInt(buf.length);
                            System.out.println(buf.length);
                            //in.read();

                            InetAddress serverAddress = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress();
                            DatagramPacket imgPacket = new DatagramPacket(buf, buf.length, serverAddress, udpPort);
                            udpSocket.send(imgPacket);

                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }


                        long time2 = System.currentTimeMillis();
                        System.out.println("time = " + (time2 - time1));
                    }
                }
            }).start();


        }

    }
}
