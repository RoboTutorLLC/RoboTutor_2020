package cmu.xprize.robotutor;

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
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
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
                    "/sdcard/roboscreen/video1.mp4" );
            this.recorderInstance.setRecordAudio(true);
        }
        this.recorderInstance.startRecord();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void endRecording(){
        /**
         * this method is used to generate empty silence files required when we merge and fill the songs
         * in between
         */

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
//        this.spliceSong(audioFiles.firstElement());
        for(int i=0; i<audioFiles.size(); i++) {
            AudioObject audioObject = audioFiles.get(i);
//            this.ve.add(new File(audioObject.path));
//            this.ve.add(new File("/sdcard/robotutor/silence.mp3"));
            this.spliceSong(audioObject);
            Log.d("BBruhhh", audioObject.path);
        }

        mergeSongs(new File("/sdcard/roboscreen/audio123.mp3"));
        this.muxing();
    }

    private void createSilenceFile(Long duration){
        /**
         * this method is used to generate empty silence files required when we merge and fill the songs
         * in between
         * duration in seconds
        */
        String temp = "";

        for (int i = 0; i<Math.floor(duration); i++){
            temp+="silence.MP3|";
        }

        String[] command = {"-i", temp, "-acodec", "copy", "/sdcard/tempRoboScreen/finalSilence.mp3"};
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
        Vector<AudioObject> mp3Files = audioFiles;
        if (!mergedFile.exists()){
            try {
                mergedFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.i("TAG", "mergeSongs: merging shizz");
        FileInputStream fisToFinal = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mergedFile);
            fisToFinal = new FileInputStream(mergedFile);
            for(AudioObject temp:mp3Files){
                String fileName = new File(temp.path).getName();
                String path = "/sdcard/roboscreen/TempVideos/"+fileName; // this will concatenate

                File mp3File = new File(path);
                if(!mp3File.exists())
                    continue;

                if(mp3File.getAbsolutePath()=="/sdcard/robotutor/silence.mp3") {
                    int index = mp3Files.indexOf(temp);
                    // here there is an assumption that silence will not be added at the last and there will always be elements surrounding it
                    int prev = index - 1;
                    int next = index + 1;
                    Long duration = mp3Files.get(next).startDate - mp3Files.get(prev).endDate;
                    this.createSilenceFile(duration);
                    mp3File = new File("/sdcard/tempRoboScreen/finalSilence.mp3");
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
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


        String outputFile = "";

        try {

            String root = Environment.getExternalStorageDirectory().toString();
            String audio = "/sdcard/roboscreen/audio123.mp3";
            String video = "/sdcard/roboscreen/video1.mp4";


            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "final2.mp4");
            file.createNewFile();
            outputFile = file.getAbsolutePath();


            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(video);

            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(audio);

            Log.d(TAG, "Video Extractor Track Count " + videoExtractor.getTrackCount());
            Log.d(TAG, "Audio Extractor Track Count " + audioExtractor.getTrackCount());

            MediaMuxer muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            videoExtractor.selectTrack(0);
            MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
            int videoTrack = muxer.addTrack(videoFormat);

            audioExtractor.selectTrack(0);
            MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
            int audioTrack = muxer.addTrack(audioFormat);

            Log.d(TAG, "Video Format " + videoFormat.toString());
            Log.d(TAG, "Audio Format " + audioFormat.toString());

            boolean sawEOS = false;
            int frameCount = 0;
            int offset = 100;
            int sampleSize = 256 * 1024;
            ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
            ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();


            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

            muxer.start();

            while (!sawEOS) {
                videoBufferInfo.offset = offset;
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);


                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                    Log.d(TAG, "saw input EOS.");
                    sawEOS = true;
                    videoBufferInfo.size = 0;

                } else {
                    videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                    videoBufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                    muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                    videoExtractor.advance();


                    frameCount++;
                    Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs + " Flags:" + videoBufferInfo.flags + " Size(KB) " + videoBufferInfo.size / 1024);
                    Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs + " Flags:" + audioBufferInfo.flags + " Size(KB) " + audioBufferInfo.size / 1024);

                }
            }

//            Toast.makeText(getApplicationContext(), "frame:" + frameCount, Toast.LENGTH_SHORT).show();


            boolean sawEOS2 = false;
            int frameCount2 = 0;
            while (!sawEOS2) {
                frameCount2++;

                audioBufferInfo.offset = offset;
                audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                    Log.d(TAG, "saw input EOS.");
                    sawEOS2 = true;
                    audioBufferInfo.size = 0;
                } else {
                    audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                    audioBufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                    muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                    audioExtractor.advance();


                    Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs + " Flags:" + videoBufferInfo.flags + " Size(KB) " + videoBufferInfo.size / 1024);
                    Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs + " Flags:" + audioBufferInfo.flags + " Size(KB) " + audioBufferInfo.size / 1024);

                }
            }

//            Toast.makeText(getApplicationContext(), "frame:" + frameCount2, Toast.LENGTH_SHORT).show();

            muxer.stop();
            muxer.release();


        } catch (IOException e) {
            Log.d(TAG, "Mixer Error 1 " + e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, "Mixer Error 2 " + e.getMessage());
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
