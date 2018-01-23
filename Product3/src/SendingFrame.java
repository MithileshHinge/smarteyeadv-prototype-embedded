import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

import javax.imageio.ImageIO;

public class SendingFrame extends Thread {
    private static int udpPort = 6663;
    private static int port = 6666;
    private static ServerSocket serverSocket;
    private static DatagramSocket udpSocket;
    public static Socket socket;
    public BufferedImage frame;
    
    public static boolean livefeed = true;

    public void run() {
		
        try {
            serverSocket = new ServerSocket(port);
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
                    while(!Main.notifThread.sendNotif && livefeed) {

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
                        
                        try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
                    }
	                try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                
	                livefeed = true;
                }
            }).start();


        }

    }
}
