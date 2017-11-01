import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class SendingAudio extends Thread{

    private static AudioFormat format;
    private static int audioUdpPort = 6666, handshakePort = 6661;
    private static int sampleRate = 44100;
    private static ServerSocket serverSocket;
    private static Socket socket;
    private static DatagramSocket dataSocket;
    private static OutputStream out;
    private static InputStream in;

    private static DataLine.Info dataLineInfo;
    private static SourceDataLine sourceDataLine;

	public SendingAudio(){
        try {
            serverSocket = new ServerSocket(handshakePort);
            dataSocket = new DatagramSocket(audioUdpPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	@Override
	public void run(){
	    while (true){
            try {

                socket = serverSocket.accept();
                System.out.println("connection sapadla");
                out = socket.getOutputStream();
                in = socket.getInputStream();
                int p = in.read();

                if (p==1){
                    System.out.println(String.format("p=1 received"));
                    out.write(2);
                    socket.close();
                }else continue;

                byte[] receiveData = new byte[4096];

                format = new AudioFormat(sampleRate, 16, 1, true, false);
                dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
                sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                sourceDataLine.open(format);
                sourceDataLine.start();
                FloatControl volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
                volumeControl.setValue(1f);
                System.out.println(String.format("here"));
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                while (true){
                    try {
                        System.out.println(String.format("here"));


                        dataSocket.receive(receivePacket);
                        toSpeaker(receivePacket.getData());
                        System.out.println(String.format("here"));

                    } catch (IOException e){
                        e.printStackTrace();
                        break;
                    }

                }


            } catch (IOException | LineUnavailableException e) {
                e.printStackTrace();
            }

            sourceDataLine.drain();
            sourceDataLine.close();
        }
	}

    private void toSpeaker(byte[] soundbytes) {
        try {
            System.out.println(String.format("sending to speaker1"));
            System.out.println("format? :" + sourceDataLine.getFormat());
            sourceDataLine.write(soundbytes, 0, soundbytes.length);

        } catch (Exception e) {
            System.out.println("Not working in speakers...");
            e.printStackTrace();
        }
    }
}
