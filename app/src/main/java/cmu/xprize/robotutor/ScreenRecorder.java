package cmu.xprize.robotutor;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import android.util.Log;

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
import java.util.Date;
import java.util.Vector;
import java.text.SimpleDateFormat;

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
    private Boolean includeAudio = true;
    Context context;
    String saveName = "";



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


    //This is a method to return the instance of screenRecordHelper
    public ScreenRecordHelper getRecorderInstance(){
        return this.recorderInstance;
    }

    /**
     * Start Recording
     * store it in the folder of /sdcard/roboscreen
     */


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startRecording(String baseDirectory, Boolean includeAudio, String tutorId){
        this.videoTimeStamp = new Date();
        this.baseDirectory = baseDirectory;
        Long time =  new Date().getTime();
        // formatting this below as / and : not allowed in the libary
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String currDate = formatter.format(new Date()).replace('/','_').replace(':','_').replace(' ','_');
        String timeInString = Long.toString(time);
        String formattedTutorId = tutorId.replace(":","_").replace(".","_");
//        this.saveName = formattedTutorId+"_"+currDate+"_"+timeInString+"_"+RoboTutor.SESSION_ID;
        this.saveName = currDate + "_" + timeInString + "_" + RoboTutor.SESSION_ID + "_" + formattedTutorId;
        Log.d(TAG, "startRecording: "+this.saveName);
        if (this.recorderInstance == null) {
            this.recorderInstance = new ScreenRecordHelper(this.activity, null,
                    "/sdcard/"+this.baseDirectory+"/videos/",this.saveName);
        }
        this.recorderInstance.startRecord();
        //this.recorderInstance.onActivityResult(1024,-1, new Intent(this.));
        this.includeAudio = includeAudio;

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
        if(this.includeAudio) {
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
        }

        this.cleanUp();
        this.recorderInstance = null;

    }

    private String createSilenceFile(Double duration, int index){
        /**
         * this method is used to generate empty silence files required when we merge and fill the songs
         * in between
         * duration in seconds
        */

        File silenceSource = new File("/sdcard/"+this.baseDirectory+"/silence.mp3");
        final File dest = new File("/sdcard/"+this.baseDirectory+"/TempAudio/finalSilence"+index+".mp3");
        String path = silenceSource.getAbsolutePath();
        Log.d(TAG, "createSilenceFile: silence file is originally present at $$"+ duration);
        String[] command = {"-f", "lavfi", "-i", "anullsrc=r=44100:cl=mono", "-t", duration.toString(), "-q:a", "9", "-acodec", "libmp3lame", dest.getAbsolutePath(), "-y"};

        FFmpeg ffmpeg = FFmpeg.getInstance(this.context);

        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler(){
                @Override
                public void onSuccess() {
                    Log.d(TAG, "onSuccess: silence is created " + dest.getAbsolutePath());
                    super.onSuccess();
                }

                @Override
                public void onFailure() {
                    Log.d(TAG, "onFailure: Unable to create silence");
                    super.onFailure();
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


    private String spliceSong(String originalPath, int index, Double duration) { // duration in seconds
        File folder = new File( "/sdcard/"+this.baseDirectory+"/TempAudio");
        if (!folder.exists()) {
            folder.mkdir();
        }

        // this file s
        String originalFileName = new File(originalPath).getName();
        final String path = "/sdcard/"+this.baseDirectory+"/TempAudio/"+originalFileName;
        String dest = path.replace(".", index+".");
        String[] command = { "-i", originalPath, "-to", duration.toString() , dest, "-y"};

        Log.d(TAG, "spliceSong: the silence sound to be merged at "+ dest);
        FFmpeg ffmpeg = FFmpeg.getInstance(this.context);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler(){
                @Override
                public void onSuccess() {
                    Log.d(TAG, "onSuccess: successfully spliced silence the song");
                    super.onSuccess();
                }

                @Override
                public void onFailure() {
                    Log.d(TAG, "onFailure: failed to spliced silence the song " + path);
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

        return dest;
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

        Vector <InputStream> streams = new Vector<>();
        for(int i=0; i<mp3Files.size(); i++) {
            AudioObject audioObject = mp3Files.get(i);

            try {
                String fileName = new File(audioObject.path).getName();
                String path = "/sdcard/"+this.baseDirectory+"/TempAudio/"+fileName;
                Log.d(TAG, "mergeSongs: the stream path is silence"+path);
                streams.add(new FileInputStream(path));
                if(i==mp3Files.size()-1)
                    continue;

                AudioObject next = mp3Files.get(i+1);
                String pathOfSilence = "/sdcard/"+this.baseDirectory+"/silence.mp3";
                Double duration = (next.startDate.getTime()-audioObject.endDate.getTime())/1000.0;
                try {
                    String silenceStream = spliceSong(pathOfSilence, i, duration);
                    Log.d(TAG, "mergeSongs: the stream path is silence "+silenceStream+" "+duration.toString());
                    streams.add(new FileInputStream(silenceStream));

                } catch (Exception e) {
                    Log.d(TAG, "mergeSongs: the silence errror is brrr "+ Log.getStackTraceString(e));
                }

                i+=1;
            } catch (Exception e) {
                Log.d(TAG, "mergeSongs: the silence errror is brrr 2" + Log.getStackTraceString(e));
                e.printStackTrace();
            }
        }


        SequenceInputStream sistream = new SequenceInputStream(streams.elements());
        Log.d(TAG, "mergeSongs: the number of elements in the input stream is "+streams.size());
        try {
            FileOutputStream fostream = new FileOutputStream(mergedFile);
            int temp;

            while( ( temp = sistream.read() ) != -1)
            {
                Log.d(TAG, "mergeSongs: reading the songs");
                // System.out.print( (char) temp ); // to print at DOS prompt
                fostream.write(temp);   // to write to file
            }
            fostream.close();
            sistream.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "mergeSongs: error shizz");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "mergeSongs: error shizz");
            e.printStackTrace();
        }

    }


    private void muxing() {


        String audio = "/sdcard/"+this.baseDirectory+"/audio123.mp3";
        String video = "/sdcard/"+this.baseDirectory+"/videos/"+this.saveName+".mp4";
//        this.activity.getLocalClassName()+new Date().toString()+Build.SERIAL
        Log.d(TAG, "muxing: and finding the names of the shizz "+this.activity.getLocalClassName()+Build.getRadioVersion());
        String outputFile = "/sdcard/"+this.baseDirectory+"/testVideo123.mp4";

        String[] command = {"-i", video, "-i", audio, "-map", "0:v", "-map", "1:a", "-c:v", "copy", "-c:a", "copy", outputFile , "-y"};

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
