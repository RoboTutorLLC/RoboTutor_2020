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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.ByteBuffer;
import java.sql.Time;
import java.util.Date;
import java.util.Vector;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import cmu.xprize.robotutor.tutorengine.CMediaManager;

class AudioObject {
    String path = "";
    Date startDate = null;
    Date endDate = null;

    AudioObject(String path) {
        this.path = path;
    }

    AudioObject(String path, Date startDate) {
        this.path = path;
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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
    private String TAG = "ScreenRecorder";
    private String baseDirectory = "roboscreen";
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
    public void startRecording(String baseDirectory){
        this.videoTimeStamp = new Date();
        this.baseDirectory = baseDirectory;
        if (this.recorderInstance == null) {
            this.videoNamesIterator = 0; // creating a two name cycle and iterating between the cycle
            String videoName = videoNames[videoNamesIterator];
            this.recorderInstance = new ScreenRecordHelper(this.activity, null,
                    "/sdcard/"+this.baseDirectory+"/videos/", "final_video" );
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
        Log.d(TAG, "splicing " + audioFiles.size());
        for(int i=0; i<audioFiles.size(); i++) {
            AudioObject audioObject = audioFiles.get(i);

            if(audioObject.endDate!=null){
                Log.d(TAG, "audio object" +
                        audioObject.endDate);
                this.spliceSong(audioObject, i);
            }
            Log.d(TAG, "index "+i);
        }


        Log.d(TAG, "Merging all the songs");
        this.mergeSongs(new File("/sdcard/"+this.baseDirectory+"/audio123.mp3"));
        this.muxing();
        this.recorderInstance = null;
//        this.cleanUp();
    }

    private String createSilenceFile(Long duration){
        /**
         * this method is used to generate empty silence files required when we merge and fill the songs
         * in between
         * duration in seconds
        */


        File silenceSource = new File("/sdcard/"+this.baseDirectory+"/silence.mp3");
        File dest = new File("/sdcard/"+this.baseDirectory+"/TempAudio/finalSilence.mp3");
        String path = silenceSource.getAbsolutePath();
        Log.d(TAG, "createSilenceFile: silence file is originally present at $$"+ duration);
        duration = new Long(duration/10);
        String[] command = {"-f", "lavfi", "-i", "anullsrc=r=44100:cl=mono", "-t", duration.toString(), "-q:a", "9", "-acodec", "libmp3lame", dest.getAbsolutePath(), "-y"};

        FFmpeg ffmpeg = FFmpeg.getInstance(this.context);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler(){
                @Override
                public void onSuccess() {
                    super.onSuccess();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            Log.d(TAG, "createSilenceFile:  error in loading FFMPEG");
            e.printStackTrace();
        }

        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                    Log.d(TAG, "Work Started for silence ");
                }
                @Override
                public void onProgress(String message) {
                    Log.d(TAG, "Doing some work silence");
                }
                @Override
                public void onFailure(String message) {
                    Log.d(TAG, "Conversion failure silence " + message);
                }
                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "Silence created");
                }
                @Override
                public void onFinish() {
                    Log.d(TAG, "Silence created Work Dome");
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.d(TAG, "FFMPEG running bruhh " + e.toString());
            e.printStackTrace();
        }

        return dest.getAbsolutePath();
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    private void spliceSong(AudioObject audioObject, int index) {
        File folder = new File( "/sdcard/"+this.baseDirectory+"/TempAudio");
        if (!folder.exists()) {
            folder.mkdir();
        }

        String fileName = new File(audioObject.path).getName();
        File dest = new File(folder, fileName);
        Log.d(TAG, "Destination Filename is " + fileName);
        final String path = new File(audioObject.path).getAbsolutePath();

        Long duration = audioObject.endDate.getTime() - audioObject.startDate.getTime();
        Double durationInSeconds = (duration.doubleValue())/1000.0;
        Log.d(TAG, "File created " + dest.getAbsolutePath() + "  " + audioObject.endDate + "  " + audioObject.startDate + "  " + path + " Duration " + durationInSeconds);
        String[] command = { "-i", path, "-to", durationInSeconds.toString() , dest.getAbsolutePath(), "-y"};

        FFmpeg ffmpeg = FFmpeg.getInstance(this.context);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler(){
                @Override
                public void onSuccess() {
                    Log.d(TAG, "onSuccess: successfully spliced the song");
                    super.onSuccess();
                }

                @Override
                public void onFailure() {
                    Log.d(TAG, "onFailure: failed to splice the song " + path);
                    super.onFailure();
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

        // test op

        Vector <AudioObject> finalMP3files = new Vector<>();
        Vector <InputStream> streams = new Vector<>();
        for (AudioObject audioObject:mp3Files){
            finalMP3files.add(audioObject);
            finalMP3files.add(new AudioObject("/sdcard/RoboTutor/silence.mp3"));
            try {
                String fileName = new File(audioObject.path).getName();
                String path = "/sdcard/"+this.baseDirectory+"/TempAudio/"+fileName;
                streams.add(new FileInputStream(path));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        SequenceInputStream sistream = new SequenceInputStream(streams.elements());
        try {
            FileOutputStream fostream = new FileOutputStream(mergedFile);
            int temp;

            while( ( temp = sistream.read() ) != -1)
            {
                // System.out.print( (char) temp ); // to print at DOS prompt
                fostream.write(temp);   // to write to file
            }
            fostream.close();
            sistream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void muxing() {


        String audio = "/sdcard/"+this.baseDirectory+"/audio123.mp3";
        String video = "/sdcard/"+this.baseDirectory+"/videos/final_video.mp4";
//        this.activity.getLocalClassName()+new Date().toString()+Build.SERIAL
        String outputFile = "/sdcard/"+this.baseDirectory+"/testVideo123.mp4";

        String[] command = {"-i", video, "-i", audio, "-c", "copy", "-map", "0:v:0", "-map", "1:a:0", outputFile , "-y"};

        FFmpeg ffmpeg = FFmpeg.getInstance(this.context);

        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler(){
                @Override
                public void onSuccess() {
                    super.onSuccess();
                    Log.d(TAG, "onSuccess: merging binary loaded");
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
                public void onProgress(String message) {
                    Log.d(TAG, "onProgress: muxed this shizz");
                }

                @Override
                public void onFailure(String message) {
                    Log.d(TAG, "Error and failure " + message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "onSuccess: muxed this");
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "onSuccess: muxed this");
                }

            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }


    }

    private void cleanUp() {
        File root = new File("/sdcard/"+this.baseDirectory+"/TempAudio/");
        File[] Files = root.listFiles();
        if(Files != null) {
            int j;
            for(j = 0; j < Files.length; j++) {
                Log.d(TAG, "cleanUp: " + Files[j].getAbsolutePath());
                Log.d(TAG, "cleanUp: " + Files[j].delete());
            }
        }

//        audioFile.delete();

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
