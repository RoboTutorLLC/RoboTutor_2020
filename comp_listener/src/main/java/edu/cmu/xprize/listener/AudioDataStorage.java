package edu.cmu.xprize.listener;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * audioData stores the metadata associated with audio (storydata.json stuff)
 * if you are looking for actual audio output, see AudioWriter
 */
public class AudioDataStorage {

    public static ArrayList<ListenerBase.HeardWord> segmentation = new ArrayList<ListenerBase.HeardWord>();
    static JSONObject                               storyData;

    static long decoderStartTime;
    static long writerStartTime;

    static FileOutputStream outputStream;
    static FileChannel outChannel;

    public static boolean contentCreationOn;

    public static void initStoryData(String jsonData) {
        try {
            storyData = new JSONObject(jsonData);
        } catch (Exception e) {
            Log.wtf("AudioDataStorage", "Could not load storydata");
        }
    }

    public static synchronized void clearAudioData() {
        Log.d("AudioDataStorage", "AudioData Cleared");
        // ? chirag what did you write here
    }

    public static JSONObject saveAudioData(String fileName, String assetLocation, int currLine, int currPara, int currPage, String sentenceWPunc, int currUtt, List<ListenerBase.HeardWord> seg) {
        // Todo: optimize this code (the process is being Duplicated)
            // where?
            Log.d("ADSSave", "attempting to save audiodata.");

            String completeFilePath = assetLocation + fileName + ".mp3";

            Log.d("ADSSave", completeFilePath);

            // write segmentation to .seg file
            try {
                FileOutputStream os = new FileOutputStream(assetLocation + "/" + fileName.toLowerCase().replace(" ", "_") + ".seg");
                StringBuilder segData = new StringBuilder("");

                Log.d("AudioDataStorage", "FileOutputStream Created at " + assetLocation + "/" + fileName + ".seg");
                int i = fileName.split(" ").length;
            for(ListenerBase.HeardWord word: seg) {
                if (i >= 0) {
                    segData.append(word.hypWord.toLowerCase() + "\t" + word.startFrame + "\t" + word.endFrame);
                    segData.append("\n");
                }
                i--;
            }
            segData.deleteCharAt(segData.lastIndexOf("\n"));

            byte[] segBytes = segData.toString().getBytes();
            os.write(segBytes);
            os.close();

            // AudioWriter.truncateNarration(completeFilePath, seg.get(seg.size() - 1).endFrame);
        } catch(IOException | NullPointerException e) {
            Log.wtf("AudioDataStorage", "Failed to write segmentation!");
            Log.d("SegmentationFail", Log.getStackTraceString(e));
        }

        Log.d("AudioDataStorage", "About to update storydata.json");

        // Update Storydata.json
        try {

            JSONObject rawData = storyData
                    .getJSONArray("data")
                    .getJSONObject(currPage)
                    .getJSONArray("text")
                    .getJSONArray(currPara)
                    .getJSONObject(currLine);

            JSONObject rawNarration;
            JSONArray narrationArray;
            try {
                narrationArray = rawData
                        .getJSONArray("narration");
            } catch (JSONException e) {
                narrationArray = new JSONArray();
                rawData.put("narration", narrationArray);
            }

            rawNarration = new JSONObject();

            JSONArray segm = new JSONArray();
            long finalEndTime = 0;
            int i = fileName.split(" ").length;
            for(ListenerBase.HeardWord heardWord : seg) {
                if (i >= 0) {
                /*
                if (i > 1) {
                    if (seg.get(i).iSentenceWord != heardWord.iSentenceWord - 1)
                        break;
                }
                 */
                    JSONObject segObj = new JSONObject();
                    segObj.put("end", heardWord.endFrame);
                    segObj.put("start", heardWord.startFrame);
                    segObj.put("word", heardWord.hypWord.toLowerCase());
                    segm.put(segObj);
                    finalEndTime = heardWord.endFrame;
                    i++;
                }
                i--;
            }
            rawNarration.put("segmentation", segm);
            rawNarration.put("from", seg.get(0).startFrame);
            rawNarration.put("audio", fileName.toLowerCase().replace(" ", "_") + ".mp3");
            rawNarration.put("until", finalEndTime);
            rawNarration.put("utterances", fileName.toLowerCase());


            narrationArray.put(rawNarration);

            FileOutputStream outJson = new FileOutputStream(assetLocation + "/storydata.json");
            Log.d("AudioDataStorage", "writing out audio to " + assetLocation + "/storydata.json");

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            String storyString = gson.toJson(
                    new JsonParser().parse(storyData.toString()).getAsJsonObject())
                    .replace("\"nameValuePairs\": ", "")
                    .replace("\"values\": ", "");

            outJson.write(storyString.getBytes());
            outJson.close();

            Log.d("AudioDataStorage", "CHIRAG, Storydata.json right here: " + storyData.toString());

            return storyData
                    .getJSONArray("data")
                    .getJSONObject(currPage);
        } catch(JSONException e) {
            Log.wtf("ADSStoryData", "Failed to update storyData!");
            Log.d("StoryDataFail", Log.getStackTraceString(e));

            return null;
        } catch(IOException e) {
            Log.d("ADSStoryDataFail", "Was not able to open file");

            return null;
        }

    }

    public static void updateHypothesis(ListenerBase.HeardWord[] heardWords) {
        Log.d("AudioDataStorageHyp", "Hypothesis Updated");
        segmentation.clear();
        if (heardWords != null) {
            segmentation.addAll(Arrays.asList(heardWords));
        }

    }

    static void setSampleRate(int samplerate) {
        // samplerate is always 16000 from my experience
    }

    public static void saveSegmentation() {

    }


}
