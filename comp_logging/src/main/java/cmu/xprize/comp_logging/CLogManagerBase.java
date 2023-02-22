//*********************************************************************************
//
//    Copyright(c) 2016-2017  Kevin Willows All Rights Reserved
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//*********************************************************************************

package cmu.xprize.comp_logging;

import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class CLogManagerBase implements ILogManager {

    private static final int OBJ_PART       = 0;
    private static final int VAL_PART       = 1;

    //private static final String LOG_VERSION = "1.0.0";    // initial release
    private static final String LOG_VERSION = "1.0.1";      // Updated LTKPlus to use LTKPLUS tag

    private static String currenttutor      = "<undefined>";
    private String TERMINATING_PACKET       = "{\"end\":\"end\"}]}";
    private byte[] TERMINATE_BYTES          = TERMINATING_PACKET.getBytes();

    private LogThread      logThread;                   // background thread handling log data
    private String         log_Path;
    private String         log_Filename;
    private boolean        isLogging = false;

    private Handler logHandler;
    private HashMap queueMap    = new HashMap();
    private boolean mDisabled   = false;

    private File                       logFile;
    private FileOutputStream           logStream;
    private java.nio.channels.FileLock logLock;

    private FileWriter                 logWriter;
    private RandomAccessFile           seekableLogWriter;
    private boolean                    seekable = true;

    private boolean                    logWriterValid = false;


    // Datashop specific

    private boolean                    loggingDS = false;
    private File                       logDSFile;
    private FileOutputStream           logDSStream;
    private java.nio.channels.FileLock logDSLock;
    private FileWriter                 logDSWriter;
    private boolean                    logDSWriterValid = false;
    protected static String sessionStartTime;


    protected String TAG = "CLogManagerBase";


    protected CLogManagerBase() {
    }

    public void startLogging(String logPath, String logFilename) {

        log_Path = logPath;
        log_Filename = logFilename;
        sessionStartTime = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());

        // Restart the log if necessary
        //
        stopLogging();

        isLogging = true;
        mDisabled = false;

        logThread = new LogThread(TAG);
        logThread.start();

        try {
            logHandler = new Handler(logThread.getLooper());
        }
        catch(Exception e) {
            Log.e(TAG, "Handler Create Failed:" + e);
        }

        lockLog();
    }


    /**
     * Stop accepting new packets -
     * Causes the thread to flush the input queue and then exit
     *
     */
    public void stopLogging() {

        if(isLogging) {
            Log.i(TAG, "Shutdown begun");

            isLogging = false;
            mDisabled = true;

            // Terminate the log thread - flush the queue prior to exit
            //
            try {

                logThread.getLooper().quitSafely();

                logThread.join();            // waits until it finishes
                Log.i(TAG, "Shutdown complete");

            } catch (InterruptedException e) {
            }

            releaseLog();
        }
    }

    public void transferHotLogs(String hotPath, String readyPath) {

        File hotDir = new File(hotPath);

        // our first time...
        if (!hotDir.exists()) {
            return;
        }

        File readyDir = new File(readyPath);

        try {

            // make dir if necessary
            if (!readyDir.exists()) {
                readyDir.mkdir();
            }

            for (File f : hotDir.listFiles()) {
                Log.w("LOG_DEBUG", "Moving file " + f.getName() + " between folders.");

                if (f.isDirectory()) {
                    // do nothing... there should not be any directories
                } else {
                    File readyLog = new File(readyPath, f.getName());
                    InputStream in = new FileInputStream(f);
                    OutputStream out = new FileOutputStream(readyLog);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }

                    in.close();
                    out.close();

                    f.delete();
                }
            }
        } catch (IOException e) {
            CErrorManager.logEvent(TAG, "Moving file Error:", e, false);
        }

    }


    public static void setTutor(String tutorid) {
        currenttutor = tutorid;
    }


    /**
     *  This is a background thread on which to process all log data requests
     *
     */
    private final class LogThread extends HandlerThread {

        public LogThread(String name) {
            super(name);
        }

        public LogThread(String name, int priority) {
            super(name, priority);
        }
    }


    /**
     * This is the central processsing point of the data log - this runs on an independent thread
     * from the UI.
     */
    public class Queue implements Runnable {

        protected String dataPacket;
        protected String unEncodedPacket;
        protected String statePacket;
        protected String errorMsg;
        protected Exception exception;

        public Queue(String packet) {

            dataPacket      = packet;
            unEncodedPacket = null;
            exception = null;
            errorMsg = null;
        }

        public Queue(String _packet, String _target, String _state) {
            dataPacket      = _packet;
            unEncodedPacket = _target;
            statePacket     = _state;
            exception = null;
            errorMsg = null;
        }

        public Queue(String _packet, String msg, Exception e) {
            dataPacket      = _packet;
            unEncodedPacket = null;
            exception = e;
            errorMsg = msg;
        }

        // we can accept data with various object/value encodings (i.e. different delimiters)
        //
        private String parseData(String dataPacket, String delimiter) {

            String encodedPacket = "{";

            String[] objvalPairs = dataPacket.split(",");

            for(int pair = 0 ; pair < objvalPairs.length ; pair++) {

                String[] objval = objvalPairs[pair].split(delimiter);

                if(objval.length > 1) {
                    encodedPacket = encodedPacket + "\"" + objval[OBJ_PART] + "\":\"" + objval[VAL_PART] + "\"";
                }
                else {
                    encodedPacket = encodedPacket + "\"" + objval[OBJ_PART] + "\":\"" + "<empty>" + "\"";
                }

                if(pair < objvalPairs.length -1) {
                    encodedPacket = encodedPacket + ",";
                }
            }
            encodedPacket = encodedPacket + "}";

            return encodedPacket;
        }

        @Override
        public void run() {

            try {
                queueMap.remove(this);

                // Don't do this JSON encoding on the UI Thread -
                // if unEncodedPacket is not null then the packet is incomplete and unEncodedPacket
                // contains a String containing a comma delimited set of obj:value pairs
                //
                // For statePackets
                // e.g. "myobj1|itsvalue,myobj2|itsvalue"
                //
                // For unEncodedPacket
                // e.g. "myobj1:itsvalue,myobj2:itsvalue"
                //
                // These need to be encoded into a JSON data subobject.
                //
                if(statePacket != null) {

                    String encodedJSONPacket = parseData(statePacket, "#");

                    dataPacket = dataPacket + "\"data\":" + encodedJSONPacket + "},\n";
                }
                else if(unEncodedPacket != null) {

                    String encodedJSONPacket = parseData(unEncodedPacket, ":");

                    dataPacket = dataPacket + "\"data\":" + encodedJSONPacket + "},\n";
                }

                writePacketToLog(dataPacket);
                if(errorMsg != null){
                    addToErrorLog(errorMsg,exception);
                }

            } catch (Exception e) {
                CErrorManager.logEvent(TAG, "Write Error:", e, false);
            }
        }
    }

    private void addToErrorLog(String errorMsg, Exception e) {
        if(e == null){
            String report = errorMsg +"\n\n";
            createErrorFile(report,errorMsg);
            return;
        }
        StackTraceElement[] arr = e.getStackTrace();
        String report = e.toString()+"\n\n";
        report += "--------- Stack trace ---------\n\n";
        for (int i=0; i<arr.length; i++) {
            report += "    "+arr[i].toString()+"\n";
        }
        report += "-------------------------------\n\n";

        report += "--------- Cause ---------\n\n";
        Throwable cause = e.getCause();
        if(cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (int i=0; i<arr.length; i++) {
                report += "    "+arr[i].toString()+"\n";
            }
        }
        report += "-------------------------------\n\n";
        createErrorFile(report,errorMsg);
    }

    private void createErrorFile(String report,String msg) {
        try {
            String deviceId = Build.SERIAL;
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());
            String _directory = Environment.getExternalStorageDirectory() + "/RoboTutor/";
            File logFileDir = new File(_directory);
            if(!logFileDir.exists()){
                logFileDir.mkdirs(); // incase RoboTutor folder is nonexistent
            }
            _directory = _directory + "/RoboTutor_ERROR/";
            logFileDir = new File(_directory);
            if(!logFileDir.exists()){
                logFileDir.mkdirs(); // incase RoboTutor folder is nonexistent
            }


            File logFile = new File(_directory + "ERROR_RoboTutor_" + sessionStartTime +"_" + BuildConfig.BUILD_TYPE + "_" +
                    getSequenceId() + "_" +
                    timestamp + deviceId + ".txt");
            logFile.createNewFile();
            FileOutputStream trace = new FileOutputStream(logFile, false);
            trace.write(report.getBytes());
            trace.close();
        } catch(IOException ioe) {
            Log.d("CEF",ioe.getMessage());
            ioe.printStackTrace();
        }

    }

    private String getSequenceId() {
        //Example RoboTutor__3.5.0.1_000019_2023.02.15.22.05.05_unknown : in this example 000019 is being extracted which occurs between 3rd and 4th underscore.
       int cnt = 0;
       String sequenceId="";
       for(int i=0;i<log_Filename.length();i++){
           if(cnt == 3){
               for(int j=i;j<log_Filename.length();j++){
                   if(log_Filename.charAt(j) == '_') break;
                   sequenceId += log_Filename.charAt(j);
               }
               break;
           }
           if(log_Filename.charAt(i) == '_') cnt++;
       }
       return sequenceId;
    }


    /**
     * We use file locks to keep the logs around until we are finished.  The RoboTutor XPrize initiative
     * used a Google Drive-Sync utility App that required locking the files so they weren't deleted while in
     * use.  So this is not a requirement otherwise.
     *
     */
    private void lockLog() {

        // Release previous log file if still locked
        //
        if(logWriterValid) {
            releaseLog();
        }


        // String oldPath   = CPreferenceCache.getPrefID(TLOG_CONST.ENGINE_INSTANCE) + TLOG_CONST.JSONLOG;
        String newPath = log_Filename + TLOG_CONST.JSONLOG;
        // String oldDsPath = CPreferenceCache.getPrefID(TLOG_CONST.ENGINE_INSTANCE) + TLOG_CONST.DATASHOP + TLOG_CONST.JSONLOG;
        String newDsPath = log_Filename + TLOG_CONST.DATASHOP + TLOG_CONST.JSONLOG;

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {

            String outPath;
            String outDSPath;

            // Validate output folder
            outPath   = log_Path;
            outDSPath = log_Path;
            File outputFile = new File(outPath);

            if (!outputFile.exists())
                    outputFile.mkdir();

            // Generate a tutor instance-unique id for the log name
            //
            outPath += newPath;

            logFile = new File(outPath);

            try {
                logStream = new FileOutputStream(logFile);
                logLock   = logStream.getChannel().lock();

                if(seekable) {
                    seekableLogWriter = new RandomAccessFile(outPath, "rwd");
                }
                else {
                    logWriter = new FileWriter(outPath, TLOG_CONST.APPEND);
                }

                logWriterValid = true;

                // Begin the root JSON element
                postPacket("{\"RT_log_version\":\"" + LOG_VERSION + "\",\"RT_log_data\":[");

            } catch (Exception e) {
                Log.e(TAG, "lockLog Failed: " + e);
            }


            //**** DATASHOP

            if(loggingDS) {

                // Generate a tutor instance-unique id for DataShop
                //
                outDSPath += newDsPath;

                logDSFile = new File(outDSPath);

                try {
                    logDSStream = new FileOutputStream(logDSFile);
                    logDSLock = logDSStream.getChannel().lock();
                    logDSWriter = new FileWriter(outDSPath, TLOG_CONST.APPEND);

                    logDSWriterValid = true;

                } catch (Exception e) {
                    Log.e(TAG, "DataShop lockLog Failed: " + e);
                }
            }
        }
    }


    private void releaseLog() {

        try {
            if(logWriterValid) {

                if(seekable) {
                    logWriterValid = false;

                    seekableLogWriter.close();
                }
                else {
                    // Terminate the root JSON element
                    //
                    writePacketToLog(TERMINATING_PACKET);

                    logWriterValid = false;

                    logWriter.flush();
                    logWriter.close();
                }

                logLock.release();
                logStream.close();
            }
        }
        catch(Exception e) {
            Log.e(TAG, "releaseLog Failed: " + e);
        }

        //**** DATASHOP

        if(loggingDS) {
            try {
                if (logDSWriterValid) {

                    logDSWriterValid = false;

                    logDSWriter.flush();
                    logDSWriter.close();

                    logDSLock.release();
                    logDSStream.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "releaseLog Failed: " + e);
            }
        }
    }


    /**
     * Note that this is currently XPrize log specific.
     * TODO: make general Purpose
     */
    private void writePacketToLog(String jsonPacket) {

        // Append Glyph Data to file
        try {
            // Throws if there is a JSON serializatin error
            //
            if(logWriterValid) {

                if(seekable) {

                    if(seekableLogWriter.length() > TERMINATE_BYTES.length) {
                        seekableLogWriter.seek(seekableLogWriter.length() - TERMINATE_BYTES.length);
                    }

                    seekableLogWriter.writeBytes(jsonPacket);

                    seekableLogWriter.writeBytes(TERMINATING_PACKET);
                }
                else {
                    logWriter.write(jsonPacket);
                    logWriter.flush();
                }
            }
        }
        catch(Exception e) {
            Log.e(TAG, "Serialization Error: " + e);
        }
    }


    /**
     * Keep a mapping of pending messages so we can flush the queue if we want to terminate
     * the tutor before it finishes naturally.
     *
     * @param qCommand
     */
    private void enQueue(Queue qCommand) {

        if (!mDisabled) {
            queueMap.put(qCommand, qCommand);

            logHandler.post(qCommand);
        }
    }


    /**
     * Post a command to this scenegraph queue
     *
     * @param command
     */
    public void post(String command) {

        enQueue(new Queue(command));
    }


    private void post(String packet, String msg, Exception e) {
        enQueue(new Queue(packet,msg,e));
    }


    /**
     * Post a command to this scenegraph queue
     *
     * @param command
     */
    private void postUnencoded(String command, String target, String state) {

        enQueue(new Queue(command, target, state));
    }


    @Override
    public void postTutorState(String Tag, String Msg) {
        Log.i(Tag, postEvent_BASE("TUTORSTATE", Tag, Msg));
    }

    @Override
    public void postEvent_V(String Tag, String Msg) {
        Log.v(Tag, postEvent_BASE("VERBOSE", Tag, Msg));
    }
    @Override
    public void postEvent_D(String Tag, String Msg) {
        Log.d(Tag, postEvent_BASE("DEBUG", Tag, Msg));
    }
    @Override
    public void postEvent_I(String Tag, String Msg) {
        Log.i(Tag, postEvent_BASE("INFO", Tag, Msg));
    }
    @Override
    public void postEvent_W(String Tag, String Msg) {
        Log.w(Tag, postEvent_BASE("WARN", Tag, Msg));
    }
    @Override
    public void postEvent_E(String Tag, String Msg) {
        Log.e(Tag, postEvent_BASE("ERROR", Tag, Msg));
    }
    @Override
    public void postEvent_A(String Tag, String Msg) {
        Log.wtf(Tag, postEvent_BASE("ASSERT", Tag, Msg));
    }
    @Override
    public void postEvent_T(String Tag, String Msg) {
        Log.wtf(Tag, postEvent_BASE("TRIGGER", Tag, Msg));
    }

    // Note that we leave the Msg JSON encoding to the Log thread where it can be processed off the
    // UI thread.
    //
    private String postEvent_BASE(String classification, String Tag, String Msg) {

        String packet;

        packet = "{" +
                "\"type\":\"LOG_DATA\"," +
                "\"tutor\":\"" + currenttutor + "\"," +
                "\"class\":\"" + classification + "\"," +
                "\"tag\":\""   + Tag + "\"," +
                "\"time\":\""  + System.currentTimeMillis() + "\",";

        // Note that tutor state is encoded with | object|value delimiters while
        // Regular messages are delimited with : object:value delimiters.
        //
        switch(classification) {
            case "TUTORSTATE":
                postUnencoded(packet, null, Msg);
                break;
            case "VERBOSE":
            case "DEBUG":
                break;
            default:
                postUnencoded(packet, Msg, null);
                break;
        }

        return Msg;
    }


    @Override
    public void postDateTimeStamp(String Tag, String Msg) {

        String packet;

        // #Mod 331 add calendar time to timestamp
        //
        DateFormat df        = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String formattedDate = df.format(Calendar.getInstance().getTime());

        packet = "{" +
                "\"class\":\"VERBOSE\"," +
                "\"tag\":\"" + Tag + "\"," +
                "\"type\":\"TimeStamp\"," +
                "\"datetime\":\"" + formattedDate + "\"," +
                "\"time\":\"" + System.currentTimeMillis() + "\",";

        postUnencoded(packet, Msg, null);

        // Emit to logcat as info class message
        //
        Log.i(Tag, packet + Msg);
    }


    @Override
    public void postError(String Tag, String Msg) {

        String packet;

        packet = "{" +
                "\"class\":\"ERROR\"," +
                "\"tag\":\"" + Tag + "\"," +
                "\"type\":\"Error\"," +
                "\"time\":\"" + System.currentTimeMillis() + "\"," +
                "\"msg\":\"" + Msg + "\"" +
                "},\n";

        post(packet,Msg,null);
    }


    @Override
    public void postError(String Tag, String Msg, Exception e) {

        String packet;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString().replace('\n',';');

        packet = "{" +
                "\"class\":\"ERROR\"," +
                "\"tag\":\"" + Tag + "\"," +
                "\"type\":\"Exception\"," +
                "\"time\":\"" + System.currentTimeMillis() + "\"," +
                "\"msg\":\"" + Msg + "\"," +
                "\"exception\":\"" + e.toString() + "\"," +
                "\"stack_trace\":\"" + stackTrace + "\"" +
                "},\n";

        post(packet,Msg,e);
    }



    @Override
    public void postBattery(String Tag, String percent, String chargeType) {

        String packet;

        packet = "{" +
                "\"class\":\"BATTERY\"," +
                "\"tag\":\"" + Tag + "\"," +
                "\"time\":\"" + System.currentTimeMillis() + "\"," +
                "\"percent\":\"" + percent + "\"," +
                "\"chargeType\":\"" + chargeType + "\"" +
                "},\n";

        post(packet);

        Log.i(Tag, packet);
    }


    @Override
    public void postPacket(String packet) {
        post(packet);
    }
}
