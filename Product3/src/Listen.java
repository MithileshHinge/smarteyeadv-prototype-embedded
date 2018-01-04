import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Listen extends Thread {
	static int listen_handshake = 6675;
	static int listenport =6673;
	static ServerSocket serverSocket;
	static Socket socket;
	static OutputStream out;
	static InputStream in;
	static DatagramSocket dataSocket;
	
	static int sampleRate = 44100; 
	static AudioFormat format;
	static DataLine.Info dataLineInfo;
	static TargetDataLine targetDataLine;
	
	
	public void run(){
		System.out.println(String.format("Listen thread started"));
		try {
			serverSocket = new ServerSocket(listen_handshake);
            dataSocket = new DatagramSocket(listenport);        
            //dataSocket.setSoTimeout(100);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		while(true){
			try{
		    System.out.println("#############################while entered");	
			serverSocket.setSoTimeout(0);
			socket = serverSocket.accept();
		    System.out.println("############################### SERVER SOCKET ACCEPT   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			out = socket.getOutputStream();
			in = socket.getInputStream();
			int p=in.read();
			
			if(p==1)
			{
				p = 0;
				System.out.println(String.format(".................p=1 received"));
				out.write(2);
				out.flush();
			}
			else{
				continue;
			}
			
			format = new AudioFormat(sampleRate, 16, 1, true, false);
			dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
			targetDataLine.open(format);
			targetDataLine.start();
			
			int numBytesRead;
			byte[] data = new byte[4096];
			DatagramPacket dgp;
			//InetAddress destination = InetAddress.getByName(host);
			InetAddress destination = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress();
			
			while(true){
				try{
				out = socket.getOutputStream();
				out.write(3);
				out.flush();
				numBytesRead =  targetDataLine.read(data, 0, data.length);
				//System.out.println("n: " + numBytesRead);
				dgp = new DatagramPacket(data,data.length,destination,listenport);
				//System.out.println(data);
				dataSocket.send(dgp);
				System.out.println("..........................................................................................SENDING AUDIO DATA:" + data);
				}catch(IOException e){
					targetDataLine.close();
					break;
				}		
			}
			
			
			}catch(IOException e){
				e.printStackTrace();
				
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
}