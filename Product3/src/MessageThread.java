import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class MessageThread extends Thread{
	
	private int port = 6676;
	private Socket socket;
	private ServerSocket serverSocket;
	final byte BYTE_STOP_LIVEFEED = 4, BYTE_START_LISTEN = 5, BYTE_STOP_LISTEN = 6;
	
	public MessageThread(){
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run(){
		try{
			while(true){
				socket = serverSocket.accept();
				
				int p = socket.getInputStream().read();
				
				switch(p){
				case BYTE_STOP_LIVEFEED:
					System.out.println("@@@@@@@@@@@@@@@@@Live Feed off kela..........................");
					SendingFrame.livefeed = false;
					break;
				case BYTE_START_LISTEN:
					System.out.println("@@@@@@@@@@@@@@@@@Listen on kela.............................");
					SendingFrame.listen = true;
					break;
				case BYTE_STOP_LISTEN:
					System.out.println("@@@@@@@@@@@@@@@@@Listen off kela.............................");
					SendingFrame.listen = false;
					break;
				}
				
				socket.getOutputStream().write(1);
				socket.getOutputStream().flush();
				
				socket.close();
				
			}
		}catch (IOException e){
			e.printStackTrace();
		}
	}
}
