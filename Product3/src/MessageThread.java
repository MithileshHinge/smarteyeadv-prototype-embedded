import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class MessageThread extends Thread{
	
	private int port = 6676;
	private Socket socket;
	private ServerSocket serverSocket;
	
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
				case 1:
					break;
				case 3:
					break;
				case 4:
					System.out.println("@@@@@@@@@@@@@@@@@Live Feed off kela..........................");
					SendingFrame.livefeed = false;
					break;
				case 5:
					System.out.println("@@@@@@@@@@@@@@@@@Listen off kela.............................");
					Listen.listen = false;
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
