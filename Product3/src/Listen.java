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
	
	static int sampleRate = 16000; 
	static AudioFormat format;
	static DataLine.Info dataLineInfo;
	static TargetDataLine targetDataLine;
	
	public static boolean listen = true;
	
	
	public void run(){
		System.out.println(String.format("Listen thread started"));
		try {
			serverSocket = new ServerSocket(listen_handshake);
            //dataSocket.setSoTimeout(100);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		while(true){
			try{
		    //System.out.println("#############################while entered");	
			serverSocket.setSoTimeout(0);
			socket = serverSocket.accept();
		    //System.out.println("###################################################### SERVER SOCKET ACCEPT   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			out = socket.getOutputStream();
			
			format = new AudioFormat(sampleRate, 16, 1, true, false);
			dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
			targetDataLine.open(format);
			targetDataLine.start();
			
			int numBytesRead;
			byte[] data = new byte[4096];
			DatagramPacket dgp;
			InetAddress destination = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress();
			
			while(listen){
				try{
					
					dataSocket = new DatagramSocket(listenport);
					//if(out!= null) out.close();	
					/*out = socket.getOutputStream();
					out.write(3);
					out.flush();*/
					numBytesRead =  targetDataLine.read(data, 0, data.length);
					//System.out.println("n: " + numBytesRead);
					dgp = new DatagramPacket(data,data.length,destination,listenport);
					//System.out.println(data);
					dataSocket.send(dgp);
					System.out.println("..........................................................................................SENDING AUDIO DATA:" + data);
					dataSocket.close();
				}catch(IOException e){
					targetDataLine.close();
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!      IO Exception  BREAK ..........");
					//if(socket != null) socket.close();
					break;
				}		
			}
			socket.close();
			
			listen = true;
			
			
			}catch(IOException e){
				e.printStackTrace();
				
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
}