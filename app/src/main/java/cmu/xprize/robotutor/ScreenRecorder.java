package cmu.xprize.robotutor;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.nanchen.screenrecordhelper.ScreenRecordHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.nio.ByteBuffer;
import java.sql.Time;
import java.util.Date;
import java.util.Vector;

import cmu.xprize.robotutor.tutorengine.CMediaManager;

class AudioObject {
    String path = "";
    long startDate;
    long endDate;

    AudioObject(String path, Date startDate) {
        this.path = path;
        this.startDate = startDate.getTime();
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate.getTime();
    }

}

public class ScreenRecorder {
    /**
        * This is a singleton pattern that I have implemented.
        * Having more than one screen recorder instance is not a wise decision and will create a lot of leaks
        * if allowed.
        * If for any case, multiple screen recorders may be required in the future, use the unabstracted library.
        * And that should be used with caution.
     */

    static boolean isInstantiated = false;
    private RoboTutor activity = null;
    ScreenRecordHelper recorderInstance = null;
    static private String[] videoNames = new String[]{"video1.mp4"};
    private int videoNamesIterator = 0;
    private Date videoTimeStamp = null;
    private Vector<File> ve = new Vector<File>();
    private String TAG = "ScreenRecorder";
    Context context;



    static public Vector<AudioObject> audioFiles = new Vector();

//    File audioDataSource = null;


    static private String[] audioNames = new String[]{"audio1.mp4", "audio2.mp4"};
    // we need to create a method so that the Media manager can call to start the recording
    // create and store a timestamp and when the media manager is release or a new track is played, merged that file
    // when there is an edge case, stop and trim it
    // when the media manager is released, add an empty sound (create a 1 second silence sound) and duplicate that everywhere that will make a more legit sounding
    // when the video recording is stopped, stop the audio recording and stitch everything

    // we don't even need to encode and decode a mp3 file, we can just trim it out
    static public void addNewAudioFile(String filePath) {
        audioFiles.add(new AudioObject(filePath, new Date()));
    }

    static public void stopLastAudioFile() {
        if (audioFiles.size() == 0) {
            return;
        }
        audioFiles.lastElement().setEndDate(new Date());
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ScreenRecorder(RoboTutor activity, Context context) throws Exception {
        if (isInstantiated){
            // raise Error and quit
            throw new Exception("One instance already instantiated");
        }
        this.activity = activity;
        this.context = context;
        this.isInstantiated = true;
        this.videoTimeStamp = new Date();
    }

    /**
     * Start Recording
     * store it in the folder of /sdcard/roboscreen
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startRecording(){
        this.videoTimeStamp = new Date();
        if (this.recorderInstance == null) {
            this.videoNamesIterator = 0; // creating a two name cycle and iterating between the cycle
            String videoName = videoNames[videoNamesIterator];
            this.recorderInstance = new ScreenRecordHelper(this.activity, null,
                    "/sdcard/roboscreen/videos/", "final_video" );
//            this.recorderInstance.setRecordAudio(true);
        }
        this.recorderInstance.startRecord();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void endRecording(){
        /**
         * this method is used to generate empty silence files required when we merge and fill the songs
         * in between
         */
        // not nulling the instance here as we want to make a singleton and not unnecessary declaration

        Date currentTimeStamp = new Date();
        this.recorderInstance.stopRecord(0, 0, null);
        this.recorderInstance = null;
        Log.d(TAG, "splicing shizz" + audioFiles.size());
        for(int i=0; i<audioFiles.size(); i++) {
            AudioObject audioObject = audioFiles.get(i);


            if(audioObject.endDate!=0){
                Log.d(TAG, "audio object" +
                        audioObject.endDate);
                this.ve.add(new File(audioObject.path));
                this.spliceSong(audioObject);
                this.ve.add(new File("/sdcard/RoboTutor/silence.mp3"));
            }
            Log.d(TAG, "index "+i);
        }


        Log.d(TAG, "Merging all the songs");
        this.mergeSongs(new File("/sdcard/roboscreen/audio123.mp3"));
        this.muxing();
//        this.recorderInstance = null;
//        this.cleanUp();
    }

    private void createSilenceFile(Long duration){
        /**
         * this method is used to generate empty silence files required when we merge and fill the songs
         * in between
         * duration in seconds
        */

//        https://itectec.com/superuser/ffmpeg-command-for-concatenate-two-mp3-files/
        String temp = "";

        for (int i = 0; i<Math.floor(duration); i++){
            temp+="/sdcard/RoboTutor/silence.mp3|";
        }

        String[] command = {"-i", temp.substring(0,temp.length()-1), "-acodec", "copy", "/sdcard/roboscreen/finalSilence.mp3", "-y"};

        FFmpeg ffmpeg = FFmpeg.getInstance(this.context);

        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler(){
                @Override
                public void onSuccess() {
                    super.onSuccess();
                    Log.d(TAG, "onSuccess: silence binary loaded");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }

        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Log.d("Internal Testing", "final work done");
                }

                @Override
                public void onProgress(String message) {}

                @Override
                public void onFailure(String message) {}

                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "onSuccess: finally loaded andorid ffmpeg binary");
                }

                @Override
                public void onFinish() {}

            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }


    private void spliceSong(AudioObject audioObject) {
        File folder = new File( "/sdcard/roboscreen/TempAudio");
        if (!folder.exists()) {
            folder.mkdir();
        }

        String fileName = new File(audioObject.path).getName();
        File dest = new File(folder, fileName);
        Log.d(TAG, "Destination Filename is " + fileName);
        String path = new File(audioObject.path).getAbsolutePath();
        Long duration = (Math.abs(audioObject.endDate - audioObject.startDate)/1000)%60;
        Log.d(TAG, "File created " + dest.getAbsolutePath() + "  " + audioObject.endDate + "  " + audioObject.startDate + "  " + path + " Duration " + duration);
        String[] command = { "-i", path, "-to", duration.toString() , dest.getAbsolutePath(), "-y"};

        FFmpeg ffmpeg = FFmpeg.getInstance(this.context);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler(){
                @Override
                public void onSuccess() {
                    super.onSuccess();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }

        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                    Log.d(TAG, "Work Started");
                }
                @Override
                public void onProgress(String message) {
                    Log.d(TAG, "Doing some work");
                }
                @Override
                public void onFailure(String message) {
                    Log.d(TAG, "Conversion failure " + message);
                }
                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "Conversion Successful");
                }
                @Override
                public void onFinish() {
                    Log.d(TAG, "Work Dome");
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.d(TAG, "FFMPEG running bruhh " + e.toString());
            e.printStackTrace();
        }
    }

    private void mergeSongs(File mergedFile){
        Vector<AudioObject> mp3Files = audioFiles;
        if (!mergedFile.exists()){
            try {
                mergedFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.i(TAG, "mergeSongs: merging shizz");
        FileInputStream fisToFinal = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mergedFile);
            fisToFinal = new FileInputStream(mergedFile);
            for(AudioObject temp:mp3Files){
                String fileName = new File(temp.path).getName();
                String path = "/sdcard/roboscreen/TempAudio/"+fileName; // this will concatenate
                Log.d(TAG, "mergeSongs: merging songs with " + fileName + " the path is " + path);
                File mp3File = new File(path);

                if (!mp3File.exists()) continue;

                if(mp3File.getAbsolutePath()=="/sdcard/RoboTutor/silence.mp3") {
                    int index = mp3Files.indexOf(temp);
                    Log.d(TAG, "mergeSongs: silence is detected and silece is being created");
                    // here there is an assumption that silence will not be added at the last and there will always be elements surrounding it
                    int prev = index - 1;
                    int next = index + 1;
                    Long duration = mp3Files.get(next).startDate - mp3Files.get(prev).endDate;
                    this.createSilenceFile(duration);
                    mp3File = new File("/sdcard/roboscreen/finalSilence.mp3");
                }

                FileInputStream fisSong = new FileInputStream(mp3File);
                SequenceInputStream sis = new SequenceInputStream(fisToFinal, fisSong);
                byte[] buf = new byte[1024];
                try {
                    for (int readNum; (readNum = fisSong.read(buf)) != -1;)
                        fos.write(buf, 0, readNum);
                } finally {
                    if(fisSong!=null){
                        fisSong.close();
                    }
                    if(sis!=null){
                        sis.close();
                    }
                }

                Log.d(TAG, "file merged at " + mergedFile.getAbsolutePath());
            }
        }
        catch (Exception e){
            Log.d(TAG, "mergeSongs: Error is "+ e.getMessage());
        }
        finally{
            mp3Files = new Vector<AudioObject>();
            try {
                if(fos!=null){
                    fos.flush();
                    fos.close();
                }
                if(fisToFinal!=null){
                    fisToFinal.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void muxing() {


//        String outputFile = "";

        String audio = "/sdcard/roboscreen/audio123.mp3";
        String video = "/sdcard/roboscreen/videos/final_video.mp4";


        String outputFile = "/sdcard/roboscreen/final2.mp4";

        String[] command = {"-i", video, "-i", audio, "-c", "copy", "-map", "0:v:0", "-map", "1:a:0", outputFile , "-y"};

        FFmpeg ffmpeg = FFmpeg.getInstance(this.context);

        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler(){
                @Override
                public void onSuccess() {
                    super.onSuccess();
                    Log.d(TAG, "onSuccess: silence binary loaded");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }

        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Log.d(TAG, "final work done");
                }

                @Override
                public void onProgress(String message) {}

                @Override
                public void onFailure(String message) {
                    Log.d(TAG, "Error and failure " + message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "onSuccess: muxed this shizz");
                }

                @Override
                public void onFinish() {}

            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }


    }

    private void cleanUp() {
        File root = new File("/sdcard/roboscreen/TempAudio");
        File[] Files = root.listFiles();
        if(Files != null) {
            int j;
            for(j = 0; j < Files.length; j++) {
                Log.d(TAG, "cleanUp: " + Files[j].getAbsolutePath());
                Log.d(TAG, "cleanUp: " + Files[j].delete());
            }
        }

        File audioFile = new File("/sdcard/roboscreen/final2.mp4");
        audioFile.delete();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (recorderInstance != null) {
            recorderInstance.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void finalize() {
        this.isInstantiated = false;
        this.recorderInstance = null;
    }


}
