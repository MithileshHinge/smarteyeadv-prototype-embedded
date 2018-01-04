import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SendingVideo extends Thread {
	
	private int port = 6668;
	private ServerSocketChannel listener = null;
	private int beginIndex = Main.outputFilename4android.length();
	private ConcurrentHashMap<Integer, String> notifId2filepaths;
	
	public SendingVideo(ConcurrentHashMap<Integer, String> notifId2filepaths){
		this.notifId2filepaths = notifId2filepaths;
		try {
			InetSocketAddress listenAddr = new InetSocketAddress(port);
			listener = ServerSocketChannel.open();
			ServerSocket ssVdo = listener.socket();
			ssVdo.setReuseAddress(true);
			ssVdo.bind(listenAddr);
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	@Override
	public void run() {
		while(true){
			try {
				SocketChannel sc = listener.accept();
				sc.configureBlocking(true);
				Socket s = sc.socket();
				InputStream sIn = s.getInputStream();
				OutputStream sOut = s.getOutputStream();
				DataInputStream dIn = new DataInputStream(sIn);
								
				int notifId = dIn.readInt();
				System.out.println("...............vdo notif id received =  "+notifId);
				System.out.println("...............vdo notif id exists = "+ notifId2filepaths.keySet());
				String filepath = notifId2filepaths.get(Integer.valueOf(notifId));
				System.out.println("...............file path "+filepath);
				String filename = filepath.substring(beginIndex);
				DataOutputStream dOut = new DataOutputStream(sOut);
				dOut.writeInt(filename.length());
				dOut.flush();
				System.out.println(".........................length pathavli");
				sIn.read();
				sOut.write(filename.getBytes());
				sOut.flush();
				sIn.read();
				if (filepath != null){
					sendVideo(sc, filepath);
				}
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void sendVideo(SocketChannel sc, String filepath){
		
		/*FileInputStream fileIn = null;
		try {
			File file = new File(filepath);
			fileIn = new FileInputStream(file);
			byte[] buffer = new byte[16 * 1024];
			int count;
			while ((count = fileIn.read(buffer)) > 0) {
				out.write(buffer, 0, count);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileIn != null) fileIn.close();
				if (out != null) out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}*/
		
		try {
			FileChannel fc = new FileInputStream(filepath).getChannel();
			fc.transferTo(0, fc.size(), sc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
