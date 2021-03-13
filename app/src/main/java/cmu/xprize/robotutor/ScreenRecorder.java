package cmu.xprize.robotutor;

import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.nanchen.screenrecordhelper.ScreenRecordHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
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
    static private String[] videoNames = new String[]{"video1.mp4", "video2.mp4"};
    private int videoNamesIterator = 0;
    private Date videoTimeStamp = null;
    private Vector<File> ve = new Vector<File>();



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
    public ScreenRecorder(RoboTutor activity) throws Exception {
        if (isInstantiated){
            // raise Error and quit
            throw new Exception("One instance already instantiated");
        }
        this.activity = activity;
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
            this.videoNamesIterator = (videoNamesIterator + 1)%2; // creating a two name cycle and iterating between the cycle
            String videoName = videoNames[videoNamesIterator];
            this.recorderInstance = new ScreenRecordHelper(this.activity, null,
                    "/sdcard/roboscreen/" + videoName);
            this.recorderInstance.setRecordAudio(true);
        }
        this.recorderInstance.startRecord();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void endRecording(){
        Date currentTimeStamp = new Date();
        this.recorderInstance.stopRecord(0, 0, null);
        // not nulling the instance here as we want to make a singleton and not unnecessary declaration
        this.recorderInstance = null;

        // TODO: add here the code to merge all the audio files
//        audioFiles.forEach();

//        audioFiles.forEach(audioObjct -> Log.d("Screen Recorder", ""));
//      prefill it with silence and add add silence
        long startingTime = audioFiles.firstElement().startDate;
        for (AudioObject a: audioFiles) {
            a.startDate = a.startDate - startingTime;
            a.endDate = a.endDate - startingTime;
        }

//        Vector<File> ve = new Vector<File>();
        this.spliceSong(audioFiles.firstElement());
        for(int i=0; i<audioFiles.size(); i++) {
            AudioObject audioObject = audioFiles.get(i);
            this.ve.add(new File(audioObject.path));
            this.ve.add(new File("/sdcard/robotutor/silence.mp3"));
            Log.d("BBruhhh", audioObject.path);
        }




        mergeSongs(new File("/sdcard/roboscreen/audio123.mp3"));
    }

    private void createSilenceFile(AudioObject audioObject){
//        String[] command = {"-ss", ""+audioObject.endDate, audioObject.path.toString(), dest.toString()};
        //        ffmpeg -i "concat:20181021_080743.MP3|20181021_090745.MP3|20181021_100745.MP3" -acodec copy 20181021.mp3

        FFmpeg ffmpeg = FFmpeg.getInstance(null);
        // to execute "ffmpeg -version" command you just need to pass "-version"
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
                public void onSuccess(String message) {}

                @Override
                public void onFinish() {}

            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private void spliceSong(AudioObject audioObject) {
        File folder = new File(Environment.getExternalStorageDirectory()+"/TempVideos");
        if (!folder.exists()) {
            folder.mkdir();
        }

        String fileName = new File(audioObject.path).getName();
        String fileExtension = ".mp3";
        File dest = new File(folder, fileName + fileExtension);
        if (!dest.exists()){
            try {
                dest.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String[] command = {"-ss", ""+audioObject.endDate, audioObject.path.toString(), dest.toString()};

        FFmpeg ffmpeg = FFmpeg.getInstance(null);
        // to execute "ffmpeg -version" command you just need to pass "-version"
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
                public void onSuccess(String message) {}

                @Override
                public void onFinish() {}

            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }

//        return File
    }

    private void mergeSongs(File mergedFile){
        Vector<File> mp3Files = this.ve;
        if (!mergedFile.exists()){
            try {
                mergedFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        ffmpeg -i "concat:20181021_080743.MP3|20181021_090745.MP3|20181021_100745.MP3" -acodec copy 20181021.mp3

        Log.i("TAG", "mergeSongs: merging shizz");
        FileInputStream fisToFinal = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mergedFile);
            fisToFinal = new FileInputStream(mergedFile);
            for(File mp3File:mp3Files){
                if(!mp3File.exists())
                    continue;
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            this.ve = new Vector<File>();
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
