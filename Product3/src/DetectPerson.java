import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by mithileshhinge on 07/09/17.
 */
public class DetectPerson {

    private int yoloPort = 7777, numPeoplePort = 7776;
    private ServerSocket ss, ss2;

    public DetectPerson(){
        try {
            ss = new ServerSocket(yoloPort);
            ss.setSoTimeout(0);
            ss.setReuseAddress(true);
            ss2 = new ServerSocket(numPeoplePort);
            ss2.setSoTimeout(0);
            ss2.setReuseAddress(true);

            Runtime rt = Runtime.getRuntime();
            String[] darknetComm = new String[]{"./darknet", "detector", "demo", "cfg/voc.data", "cfg/yolo-voc.cfg", "yolo-voc.weights"};
            Process proc = rt.exec(darknetComm, null, new File("/home/mithi/Desktop/darknet"));
        } catch (IOException e) {
            //e.printStackTrace();
        }

    }
    public int findNumPeople(Mat img){
        try {
            Socket s = ss.accept();
            Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2RGB);
            int width = img.width();
            int height = img.height();
            int dataSize = img.rows() * img.cols() * img.channels();

            byte[] data = new byte[img.rows() * img.cols() * img.channels()];
            img.get(0, 0, data);

            System.out.println("width " + width);
            System.out.println("height " + height);
            System.out.println("dataSize " + dataSize);

            //DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            OutputStream dout = s.getOutputStream();
            byte[] bWidth = ByteBuffer.allocate(4).putInt(width).array();
            byte[] bHeight = ByteBuffer.allocate(4).putInt(height).array();
            byte[] bSize = ByteBuffer.allocate(4).putInt(dataSize).array();

            dout.write(bWidth);
            dout.write(bHeight);
            dout.write(bSize);
            dout.flush();

            int totSent = 0;
            while (totSent < dataSize) {
                dout.write(data, totSent, Math.min(1024, dataSize - totSent));
                totSent += 1024;
            }
            dout.flush();
            
            s.close();
            
            Socket s2 = ss2.accept();
            
            byte[] bCount = new byte[4];
    		InputStream in = s2.getInputStream();
    		
    		for (int i=0; i<4; i++){
    			bCount[i] = (byte) in.read();
    		}
    		
    		int count = ByteBuffer.wrap(bCount).getInt();
    		
    		s2.close();
    		System.out.println("People found: " + count);
            return count;

        } catch (IOException e) {
            //e.printStackTrace();
            return 0;
        }

        
    }
}
