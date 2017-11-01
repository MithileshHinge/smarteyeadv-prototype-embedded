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

    private int yoloPort = 7777;
    private ServerSocket ss;
    private BufferedReader stdInput;

    public DetectPerson(){
        try {
            ss = new ServerSocket(yoloPort);
            ss.setSoTimeout(0);
            ss.setReuseAddress(true);

            Runtime rt = Runtime.getRuntime();
            String[] darknetComm = new String[]{"/Users/mithileshhinge/Documents/AI_hobby_projects/darknet/darknet", "detector", "demo", "/Users/mithileshhinge/Documents/AI_hobby_projects/darknet/cfg/coco.data", "/Users/mithileshhinge/Documents/AI_hobby_projects/darknet/cfg/yolo.cfg", "/Users/mithileshhinge/Documents/AI_hobby_projects/darknet/yolo.weights"};
            Process proc = rt.exec(darknetComm);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
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

            InputStream in = s.getInputStream();
            in.read();

            s.close();

            String output = "", line = null;
            while ((line = stdInput.readLine()) != null){
                output += line;
            }

            int index = output.indexOf("person");
            int count = 0;
            while (index != -1) {
                count++;
                output = output.substring(index + 1);
                index = output.indexOf("person");
            }

            return count;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
