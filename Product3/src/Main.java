import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;

public class Main {

	private static CascadeClassifier frontal_face_cascade;
	private static CascadeClassifier mouthCascade;
	static int frame_no = 0;
	private static boolean faceNotCovered = false;
	
	
	private static boolean LightRef = true;
    static int intialBlack;
    static int RefBlack;
    static int lightCount = 0;
	
    
	
	public static final String outputFilename = "//home//mithi//Desktop//videos//";
	public static VideoWriter writer;
	public static boolean startStoring = true;
	public static long startTime;
	public static long startTime4android;
	public static Date dNow;
	public static SimpleDateFormat ft = new SimpleDateFormat("yyyy_MM_dd'at'hh_mm_ss_a");
	public static boolean writer_close = false;
	public static String store_name;
	public static String store_file_name;
	static OutputStream out;
	public static int myNotifId = 1;
	
	public static final String outputFilename4android = "//home//mithi//Desktop//videos4android//";
	public static final byte BYTE_PEOPLE_VDOGENERATING = 1, BYTE_PEOPLE_VDOGENERATED = 2, BYTE_ALERT1 = 3, BYTE_ALERT2 = 4, BYTE_ABRUPT_END = 5;
	public static VideoWriter writer4android;
	public static String store_name4android;
	public static String store_activityname;
	
	static long timeNow1, timeNow2;
	static long time3, time4;
	public static long timeAndroidVdoStarted = -1;
	public static boolean j = false;
	
	public static Process proc;
	//Disable auto focus of camera through terminal
	
	public static boolean notif2given = false;
	public static boolean notif1given = false;
	public static int framesRead = 0;
	public static String fourcc = "MP4V";
	
	static BufferedImage camimg;
	
	/*static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}*/
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public static void main(String[] args) {
		
		SendingFrame sendingFrame = new SendingFrame();
		sendingFrame.start();
		
		SendingVideo sendingVideo = new SendingVideo();
		sendingVideo.start();
		
		SendingAudio sendingAudio = new SendingAudio();
		sendingAudio.start();
		
		NotificationThread notifThread = new NotificationThread();
		notifThread.start();

		DetectPerson detectPerson = new DetectPerson();

		VideoCapture capture = new VideoCapture(0);
		if (!capture.isOpened()) {
			System.out.println("Error - cannot open camera!");
			return;
		}
		
		BackgroundSubtractorMOG2 backgroundSubtractorMOG = Video.createBackgroundSubtractorMOG2(333, 16, false);
		
		frontal_face_cascade = new CascadeClassifier("//home//mithi//Desktop//haarcascades//haarcascade_frontalface_alt.xml");
		if (frontal_face_cascade.empty()) {
			System.out.println("--(!)Error loading Front Face Cascade\n");
			return;
		} else System.out.println("Front Face classifier loaded");
		
		mouthCascade = new CascadeClassifier("//home//mithi//Desktop//haarcascades//Mouth.xml");
		if(mouthCascade.empty()){
			System.out.println("--(!)Error loading Mouth Cascade\n");
			return;
		}else System.out.println("Mouth classifier loaded");

        int nFaces = 0;
        float nFacesAvg = 0;
		int nPersonsFrames = 0, nPersons = 0, nFacesFrames = 0;
		float nPersonsAvg = 0;
		boolean startAverageCalc = false;

		while(true){
			timeNow1 = System.currentTimeMillis();
			Mat camImage = new Mat();
			
			capture.read(camImage);
			if (camImage.empty()){
				System.out.println(" --(!) No captured frame -- Break!");
				continue;
			}
			
			//Send frame via live-feed
			camimg = matToBufferedImage(camImage);
//			sendingFrame.frame = camimg;
			
			//Background subtraction without learning background
			Mat fgMask = new Mat();
			
			if (j) {
				backgroundSubtractorMOG.apply(camImage, fgMask, -1);
				j = false;
			}else backgroundSubtractorMOG.apply(camImage, fgMask, 0);
			
			//Calculate black percentage
			byte[] buff = new byte[(int) (fgMask.total() * fgMask.channels())];
			fgMask.get(0, 0, buff);
			int blackCount = 0;
			for (int i = 0; i < buff.length; i++) {
				if (buff[i] == 0) {
					blackCount++;
				}
			}
			final int blackCountPercent = 100*blackCount/buff.length;
			System.out.println("" + (blackCountPercent) + "%");
			Mat output = new Mat();
			camImage.copyTo(output, fgMask);
			
			/*
			 * TODO: Discussion to handle lighting change and peephole covering
			 */
			
			//Consider background change if black % is less that 97
			if (blackCountPercent < 97 && framesRead > 333) {
				
				//Start recording video just after bg changes
				if (startStoring){
					LightRef = true;
					System.out.println("..........................recording started...................................");
					time3 = System.currentTimeMillis();
					store_name = outputFilename + ft.format(dNow) + ".mp4";
					store_file_name = ft.format(dNow);
					writer = new VideoWriter(store_name, VideoWriter.fourcc( fourcc.charAt(0), fourcc.charAt(1), fourcc.charAt(2), fourcc.charAt(3)), 20, new Size(640,480), true);
					/*store_name4android = outputFilename4android + ft.format(dNow) + ".mp4";
					writer4android = ToolFactory.makeWriter(store_name4android);
					writer4android.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 640, 480);*/
					startTime = System.nanoTime();
					writer_close = true;
					startStoring = false;
				}
				
				//Write frame to video
				writer.write(camImage);

				//If writer4android is open, write frame to android video also
				if (writer4android != null){
					if(writer4android.isOpened()){
						 writer4android.write(camImage);
					}
				}
				frame_no++;

				//Detect person in 2nd frame
                if (frame_no==2) {
                    nPersons = detectPerson.findNumPeople(output);
                    nPersonsAvg = (float) (nPersonsAvg * nPersonsFrames + nPersons) / (float) (nPersonsFrames + 1);
                    nPersons = Math.round(nPersonsAvg);
                    nPersonsFrames++;
                }


				//Detect face every 3rd frame
				if (frame_no==3) {
                    frame_no = 0;
                    System.out.println("Face Detecting now!");

                    MatOfRect front_faces = detect(output);
                    Mat outputFaces = new Mat();
                    output.copyTo(outputFaces);

                    for (Rect rect : front_faces.toArray()) {
                        Point center = new Point(rect.x + rect.width * 0.5, rect.y + rect.height * 0.5);
                        Imgproc.ellipse(camImage, center, new Size(rect.width * 0.5, rect.height * 0.5), 0, 0, 360,
                                new Scalar(0, 255, 0), 4, 8, 0);
                    }

                    nFaces = front_faces.toArray().length;
                    if (notif1given && !startAverageCalc && nFaces>0){
                    	startAverageCalc = true;
                    }
                    
                    if (!notif1given || startAverageCalc){
                    	
                    	nFacesAvg = (float) (nFacesAvg * nFacesFrames + nFaces) / (float) (nFacesFrames + 1);
                    	nFacesFrames++;
                    	if (nFacesFrames > 7){
                    		nFaces = Math.round(nFacesAvg);
                    	}else {
                    		nFaces = 0;
                    	}
                    	System.out.println("nFacesAvg: "+nFacesAvg);
                        System.out.println("nFaces: "+nFaces);
                    }
                }


                // 1st notif after 4 seconds
                if ((System.currentTimeMillis() - time3) / 1000 >= 4   &&  !notif1given) {
                    if (nPersons == 0) {
                    	System.out.println("1st notif..................................................... Alert 1");
                    	
                        notifThread.notifFrame = camimg;
                        notifThread.p = BYTE_ALERT1;
                        notifThread.myNotifId = myNotifId;
                        notifThread.sendNotif = true;

                        //AudioPlaying audioPlaying = new AudioPlaying();
                        //audioPlaying.start();
                    }else {
                    	System.out.println("1st notif..................................................... People found Video Generating");
                        notifThread.notifFrame = camimg;
                        notifThread.p = BYTE_PEOPLE_VDOGENERATING;
                        notifThread.nPersons = nPersons;
                        notifThread.nFaces = nFaces;
                        notifThread.myNotifId = myNotifId;
                        notifThread.sendNotif = true;

                        if (nPersons > nFaces) {
                            //AudioPlaying audioPlaying = new AudioPlaying();
                            //audioPlaying.start();
                        }
                    }

                    notif1given = true;
                    
                    store_name4android = outputFilename4android + ft.format(dNow) + ".mp4";
                    store_activityname = ft.format(dNow);
                    writer4android = new VideoWriter(store_name4android, VideoWriter.fourcc( fourcc.charAt(0), fourcc.charAt(1), fourcc.charAt(2), fourcc.charAt(3)), 20, new Size(640,480), true);
					startTime4android = System.nanoTime();
                    time3 = System.currentTimeMillis();

                    nFaces = 0;
                    nFacesAvg = 0;
                    nPersons = 0;
                    nPersonsAvg = 0;
                    nPersonsFrames = 0;
                    nFacesFrames = 0;

                }

                // 2nd notif after every 15 seconds:
                if (((System.currentTimeMillis() - time3) / 1000) >= 15) {
                    if (nPersons == 0) {
                        // TODO: no people found but suspicious activity Ab kya kare???
                    	
                    	System.out.println("2nd notif..................................................... Alert 2");
                        notifThread.p = BYTE_ALERT2;
                        notifThread.myNotifId = myNotifId;
                        notifThread.sendNotif = true;
                        myNotifId++;
                    }else {
                    	System.out.println("2nd notif..................................................... People found Video generated");
                        notifThread.p = BYTE_PEOPLE_VDOGENERATED;
                        notifThread.nPersons = nPersons;
                        notifThread.nFaces = nFaces;
                        notifThread.myNotifId = myNotifId;
                        notifThread.sendNotif = true;
                        myNotifId++;
                        
                        if (nPersons > nFaces) {
                            // TODO: Sound ALARM!!!
                        }
                    }

                    if (!notif2given){
                    	notif2given = true;

                        writer4android.release();
                        sendingVideo.notifId2filepaths.put(new Integer(myNotifId), store_name4android);
                        SendMail.sendmail_notif = true;
                    }
                    
                    time3 = System.currentTimeMillis();

                    nFaces = 0;
                    nFacesAvg = 0;
                    nPersons = 0;
                    nPersonsAvg = 0;
                    nPersonsFrames = 0;
                    nFacesFrames = 0;
                }

                /*
				if( ((System.currentTimeMillis()-time3) > 2400) && (System.currentTimeMillis()-time3) < 4500 )
				{ 
					
					System.out.println("...............Detecting light change started");
					if(LightRef){
						RefBlack =blackCountPercent;
						LightRef=false;
						System.out.println("Refblack = " + RefBlack);
					}else {
					   
						int	diffBlack= Math.abs(RefBlack-blackCountPercent);
						if(diffBlack <3 && RefBlack < 75 )
							lightCount++;
						if(lightCount >= 6){
							lightCount = 0;
							notifThread.p=6;
							notifThread.sendNotif=true;
							framesRead=250;
							System.out.println("...............Light Change Confirmed");
						}
						
					}
					
				}
				*/
				
			}else {
				dNow = new Date();
				startStoring = true;
				
				
				
				if(notifThread.p==BYTE_PEOPLE_VDOGENERATING || notifThread.p==BYTE_ALERT1)
				{
					System.out.println("abrupt end...........................");
					writer4android.release();
					sendingVideo.notifId2filepaths.put(new Integer(myNotifId), store_name4android);
					notifThread.p = BYTE_ABRUPT_END;
					notifThread.myNotifId = myNotifId;
					notifThread.sendNotif = true;
					myNotifId++;
				}
				
				//Writer close once bg becomes normal
				if (writer_close){
					writer.release();
					writer_close = false;
					notif1given = false;
					notif2given = false;
					SendMail.sendmail_vdo = true;
					SendMail t3 = new SendMail();
					t3.start();
				}

				nFaces = 0;
				nFacesAvg = 0;
				nPersons = 0;
				nPersonsAvg = 0;
				nPersonsFrames = 0;
				nFacesFrames=0;
				startAverageCalc = false;

				frame_no = 0;
				
				//apply bgsubtraction while learning background
				backgroundSubtractorMOG.apply(camImage, fgMask, -1);
			}
			
			camimg = matToBufferedImage(camImage);
			BufferedImage camimg2 = timestampIt(camimg);
			sendingFrame.frame = camimg2;
			
			if (framesRead < 350) framesRead++;
			time4 = System.currentTimeMillis();
			timeNow2 = System.currentTimeMillis();
			System.out.println(timeNow2 - timeNow1);
			
			System.out.println("frmes_read" + framesRead);
			timeNow1 = timeNow2;
		}
	}
	
	public static MatOfRect detect(Mat inputframe) {
		faceNotCovered=false;
		Mat mRgba = new Mat();
		Mat mGrey = new Mat();
		MatOfRect front_faces = new MatOfRect();
		// MatOfRect side_faces = new MatOfRect();
		inputframe.copyTo(mRgba);
		inputframe.copyTo(mGrey);
		Imgproc.cvtColor(mRgba, mGrey, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist(mGrey, mGrey);
		frontal_face_cascade.detectMultiScale(mGrey, front_faces, 1.1, 3, 0, new Size(30, 30), new Size());

		
		Rect[] facesArray = front_faces.toArray();

    	for (int i = 0; i < facesArray.length; i++) {
    	    Point centre1 = new Point(facesArray[i].x + facesArray[i].width * 0.5,facesArray[i].y + facesArray[i].height * 0.5);
    	    //Core.ellipse(mRgba, centre1, new Size(facesArray[i].width * 0.5, facesArray[i].height * 0.5), 0, 0, 360,new Scalar(192, 202, 235), 4, 8, 0);
    	    //Core.ellipse(front_faces, centre1, new Size(facesArray[i].width * 0.5, facesArray[i].height * 0.5), 0, 0, 360,new Scalar(192, 202, 235), 4, 8, 0);
    	    Mat faceROI = mGrey.submat(facesArray[i]);
    	    MatOfRect mouth = new MatOfRect();

    	    //mouthCascade.detectMultiScale(faceROI, mouth, 1.1, 2, 0, new Size(30, 30), new Size());
    	    mouthCascade.detectMultiScale(faceROI, mouth);
    	    Rect[] mouthArray = mouth.toArray();

    	    for (int k = 0 ; k < mouthArray.length; k++) {
    	        Point centre3 = new Point(facesArray[i].x + mouthArray[k].x + mouthArray[k].width * 0.5,
    	                facesArray[i].y + mouthArray[k].y + mouthArray[k].height * 0.5);
    	        if (centre3.y > centre1.y ){
    	        	faceNotCovered=true;
    	        //Core.ellipse(mRgba, centre3, new Size(mouthArray[k].width * 0.5, mouthArray[k].height * 0.5), 0, 0, 360,new Scalar(177, 138, 255), 4, 8, 0);
    	        //Core.ellipse(front_faces, centre3, new Size(mouthArray[k].width * 0.5, mouthArray[k].height * 0.5), 0, 0, 360,new Scalar(177, 138, 255), 4, 8, 0);
    	        //System.out.println(String.format("Detected %s Mouth(s)", mouth.toArray().length));
    	        }
    	    }
    	    //if(faceNotCovered){
    	    	System.out.println(String.format("Detected %s face(s)", front_faces.toArray().length));
    	    	//FacenotCovered=false;
    	    //}else{
    	    	//System.out.println(String.format("Detected people = 0"));
    	    	//break;                                                                    ///add break for multiple faces or else no need	
    	    //}
    	}
    	return front_faces;
    	//return mRgba;
	}
	
	private static BufferedImage matToBufferedImage(Mat frame) {
		int type = 0;
		if (frame.channels() == 1) {
			type = BufferedImage.TYPE_BYTE_GRAY;
		} else if (frame.channels() == 3) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
		WritableRaster raster = image.getRaster();
		DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
		byte[] data = dataBuffer.getData();
		frame.get(0, 0, data);
		return image;
		
	}
	
	private static BufferedImage timestampIt(BufferedImage toEdit){
		BufferedImage dest = new BufferedImage(toEdit.getWidth(), toEdit.getHeight(),  BufferedImage.TYPE_3BYTE_BGR);
		
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    String dateTime = sdf.format(Calendar.getInstance().getTime()); // reading local time in the system
	    
	    Graphics2D g2 = dest.createGraphics();
	    //Color darkgreen= new Color(28,89,71);
	    Color darkgreen= new Color(0,0,0);
	    g2.drawImage(toEdit, 0, 0, toEdit.getWidth(), toEdit.getHeight(), null);
	    g2.setColor(darkgreen);
	    g2.setFont(new Font("TimesRoman", Font.PLAIN, 25)); 
	    g2.drawString(dateTime, 350, 450);
	    return dest;
	}
	

}
