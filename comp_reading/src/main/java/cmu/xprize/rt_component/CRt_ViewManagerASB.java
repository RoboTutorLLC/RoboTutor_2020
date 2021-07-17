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

package cmu.xprize.rt_component;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cmu.xprize.util.CPersonaObservable;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import edu.cmu.pocketsphinx.Segment;
import edu.cmu.xprize.listener.AudioDataStorage;
import edu.cmu.xprize.listener.AudioWriter;
import edu.cmu.xprize.listener.ListenerBase;

import static cmu.xprize.util.TCONST.FTR_USER_READ;
import static cmu.xprize.util.TCONST.FTR_USER_READING;
import static cmu.xprize.util.TCONST.QGRAPH_MSG;

/**
 * This view manager provides student UX for the African Story Book format used
 * in the CMU XPrize submission
 */
public class CRt_ViewManagerASB implements ICRt_ViewManager, ILoadableObject {

    private Context                 mContext;

    private ListenerBase            mListener;
    private IVManListener           mOwner;
    private String                  mAsset;

    private CRt_Component           mParent;
    private ImageView               mPageImage;
    private TextView                mPageText;

    private ImageButton             mPageFlip;
    private ImageButton             mSay;

    // ASB even odd page management

    private ViewGroup               mOddPage;
    private ViewGroup               mEvenPage;

    private int                     mOddIndex;
    private int                     mEvenIndex;
    private int                     mCurrViewIndex;

    // state for the current storyName - African Story Book

    private String                  mCurrHighlight = "";
    private int                     mCurrPage;
    private boolean                 mLastPage;
    private int                     mCurrPara;
    private int                     mCurrLine;
    private int                     mCurrWord;
    private int                     mHeardWord;                          // The expected location of mCurrWord in heardWords - see PLRT version of onUpdate below

    private String                  speakButtonEnable = "DISABLE";
    private String                  speakButtonShow   = "HIDE";
    private String                  pageButtonEnable  = "DISABLE";
    private String                  pageButtonShow    = "HIDE";

    private int                     mPageCount;
    private int                     mParaCount;
    private int                     mLineCount;
    private int                     mWordCount;
    private int                     attemptNum = 1;
    private boolean                 storyBooting;

    private String[]                wordsToDisplay;                      // current sentence words to display - contain punctuation
    private String[]                wordsToSpeak;                        // current sentence words to hear
    private ArrayList<String>       wordsToListenFor;                    // current sentence words to build language model
    private String                  hearRead;
    private Boolean                 echo = false;

    private CASB_Narration[]        rawNarration;                        // The narration segmentation info for the active sentence
    private String                  rawSentence;                         // currently displayed sentence that need to be recognized
    private CASB_Seg                narrationSegment;
    private String[]                splitSegment;
    private int                     splitIndex = TCONST.INITSPLIT;
    private boolean                 endOfSentence = false;
    private ArrayList<String>       spokenWords;
    private int                     utteranceNdx;
    private int                     segmentNdx;
    private String                  page_prompt;

    private int                     numUtterance;
    private CASB_Narration          currUtterance;
    private CASB_Seg[]              segmentArray;
    private int                     numSegments;
    private int                     utterancePrev;
    private int                     segmentPrev;
    private int                     segmentCurr;

    private String                  completedSentencesFmtd = "";
    private String                  completedSentences     = "";
    private String                  futureSentencesFmtd    = "";
    private String                  futureSentences        = "";
    private boolean                 showWords              = true;
    private boolean                 showFutureWords        = true;
    private boolean                 showFutureContent      = true;
    private boolean                 listenFutureContent    = false;
    private String                  assetLocation;

    private ArrayList<String>       wordsSpoken;
    private ArrayList<String>       futureSpoken;

    boolean                         alreadyNarrated;
    boolean                         keepOnlyRelevantAudio; // true if NARRATION CAPTURE MODE is deleting all audio that is wrong, false if the entire file including mistakes is to be kept
    boolean                         deleteRecording; // if there is a mistake in NARRATION CAPTURE MODE, then the current recording is deleted

    int                             currUtt;
    ArrayList<ListenerBase.HeardWord>       capturedUtt = new ArrayList<>();

    boolean narrationTracking;


    // json loadable
    // ZZZ where the money gets loaded

    public String        license;
    public String        story_name;
    public String        authors;
    public String        illustrators;
    public String        language;
    public String        status;
    public String        copyright;
    public String        titleimage;

    public String        prompt;
    public String        parser;
    // ZZZ the money
    public CASB_data[]   data;


    static final String TAG = "CRt_ViewManagerASB";


    public ImageButton backButton;
    public ImageButton forwardButton;

    public String buttonState;

    String narrationFileName;

    boolean isUserNarrating;
    ListenerBase.HeardWord[] allHeardWords;
    final int segmentGapLength = 100; // length of silence used to separate segments in narration capture mode
    ArrayList<Integer> seamIndices = new ArrayList<>(); // list of indices of seams for narration capture mode

    /**
     *
     * @param parent
     * @param listener
     */
    public CRt_ViewManagerASB(CRt_Component parent, ListenerBase listener) {

        mParent = parent;
        mContext = mParent.getContext();

        mOddPage = (ViewGroup) android.support.percent.PercentRelativeLayout.inflate(mContext, R.layout.asb_oddpage, null);
        mEvenPage = (ViewGroup) android.support.percent.PercentRelativeLayout.inflate(mContext, R.layout.asb_evenpage, null);

        mOddPage.setVisibility(View.GONE);
        mEvenPage.setVisibility(View.GONE);

        mOddIndex  = mParent.addPage(mOddPage );
        mEvenIndex = mParent.addPage(mEvenPage );

        mListener = listener;
    }


    /**
     *   The startup sequence for a new storyName is:
     *   Set - storyBooting flag to inhibit startListening so the script can complete whatever
     *   preparation is required before the listener starts.  Otherwise you get junk hypotheses.
     *
     *   Once the script has completed its introduction etc. it calls nextline to cause a line increment
     *   which resets storyBooting and enables the listener for the first sentence in the storyName.
     *
     * @param owner
     * @param assetPath
     */
    public void initStory(IVManListener owner, String assetPath, String location) {

        mOwner        = owner;
        mAsset        = assetPath; // ZZZ assetPath... TCONST.EXTERN
        storyBooting  = true;
        assetLocation = location;  // ZZZ assetLocation... contains storydata.json and images

        Log.d(TCONST.DEBUG_STORY_TAG, String.format("mAsset=%s -- assetLocation=%s", mAsset, assetLocation));

        if (mParent.testFeature(TCONST.FTR_USER_HIDE)) showWords = false;
        if (mParent.testFeature(TCONST.FTR_USER_REVEAL)) showFutureWords = showFutureContent = false;

        Log.d(TAG, "initStory: showWords = " + showWords + ", showFutureWords = " + showFutureWords + ", showFutureContent = " + showFutureContent);

        mParent.setFeature(TCONST.FTR_STORY_STARTING, TCONST.ADD_FEATURE);

        seekToPage(TCONST.ZERO);

        //TODO: CHECK
        mParent.animatePageFlip(true,mCurrViewIndex);

        deleteRecording = true;

    }

    boolean isNarrationCaptureMode;
    // NARRATION CAPTURE mode -- as in CONTENT CREATION and not NARRATING -- gets activated here
    public void enableNarrationCaptureMode(boolean capturingNarration, boolean keepExtraAudio) {
        if (capturingNarration) {
            mParent.setFeature(TCONST.FTR_NARRATION_CAPTURE, TCONST.ADD_FEATURE);
            Log.d("CRT_ViewManagerASB", "Narrate Mode has been activated through setNarrateMode()");
            isNarrationCaptureMode = true;
            isUserNarrating = true;

            AudioDataStorage.contentCreationOn = true;

            SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
            String date = formatter.format(new Date(System.currentTimeMillis()));
            String hyplogFile = TCONST.HYP_LOG_FILE_LOCATION + date + ".log";
            // Not working code below
            /* try {
                FileOutputStream fos = new FileOutputStream(hyplogFile);
                fos.close();
                AudioWriter.current_log_location = hyplogFile;
            } catch (IOException e) {
                isNarrationCaptureMode = false;
                Log.getStackTraceString(e);
            } */
        } else {
            isNarrationCaptureMode = false;
        }

        keepOnlyRelevantAudio = keepExtraAudio;
    }

    /**
     *  NOTE: we reset mCurrWord - last parm in seekToStoryPosition
     *
     */
    public void startStory() {

        // reset boot flag to inhibit future calls
        //
        if (storyBooting) {

            mParent.setFeature(TCONST.FTR_STORY_STARTING, TCONST.DEL_FEATURE);

            // Narration Mode (i.e. USER_HEAR) always narrates the story otherwise we
            // start with USER_READ where the student reads aloud and if USER_ECHO
            // is in effect we then toggle between READ and HEAR for each sentence.
            //
            if (mParent.testFeature(TCONST.FTR_USER_HEAR) || mParent.testFeature(TCONST.FTR_USER_HIDE) || mParent.testFeature(TCONST.FTR_USER_PARROT)) {

                hearRead = TCONST.FTR_USER_HEAR;
            } else {
                hearRead = FTR_USER_READ;
                mParent.publishFeature(FTR_USER_READING);
            }

            storyBooting = false;
            speakOrListen();
        }

        Log.d("InitStory", "Story has initialized");
    }


    public void speakOrListen() {

        if (hearRead.equals(TCONST.FTR_USER_HEAR)) {

            if(isNarrationCaptureMode) {
                isUserNarrating = true;
                endOfUtteranceCapture();
            }
            if(!hearRead.equals(FTR_USER_READ)) {
                mParent.applyBehavior(TCONST.NARRATE_STORY);
            }
        }
        if (hearRead.equals(FTR_USER_READ)) {

            startListening();
        }
    }


    @Override
    public void onDestroy() {
    }


    /**
     * From the script writers perspective there is only one say button and one pageflip button
     * Since there are actually two of each - one on each page view we share the state between them and
     * enforce updates so they are kept in sync with user expectations.
     *
     * @param control
     * @param command
     */
    public void setButtonState(View control, String command) {

        try {

            switch (command) {

                case "ENABLE":
                    control.setEnabled(true);
                    break;
                case "DISABLE":
                    control.setEnabled(false);
                    break;
                case "SHOW":
                    control.setVisibility(View.VISIBLE);
                    break;
                case "HIDE":
                    control.setVisibility(View.INVISIBLE);
                    break;
            }
        }
        catch(Exception e) {
            Log.d(TAG, "result:" + e);
        }
    }


    public void setSpeakButton(String command) {

        switch (command) {

            case "ENABLE":
                speakButtonEnable = command;
                break;
            case "DISABLE":
                speakButtonEnable = command;
                break;
            case "SHOW":
                speakButtonShow = command;
                break;
            case "HIDE":
                speakButtonShow = command;
                break;
        }

        // Ensure the buttons reflect the current states
        //
        updateButtons();
    }


    public void setPageFlipButton(String command) {

        switch (command) {

            case "ENABLE":
                Log.i("ASB", "ENABLE Flip Button");
                pageButtonEnable = command;
                break;
            case "DISABLE":
                Log.i("ASB", "DISABLE Flip Button");
                pageButtonEnable = command;
                break;
            case "SHOW":
                pageButtonShow = command;
                break;
            case "HIDE":
                pageButtonShow = command;
                break;
        }

        // Ensure the buttons reflect the current states
        //
        updateButtons();
    }


    private void updateButtons() {

        // Make the button states insensitive to the page - So the script does not have to
        // worry about timing of setting button states.
        //
        setButtonState(mPageFlip, pageButtonEnable);
        setButtonState(mPageFlip, pageButtonShow);

        setButtonState(mSay, speakButtonEnable);
        setButtonState(mSay, speakButtonShow);

        mPageFlip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(QGRAPH_MSG, "event.click: " + " CRt_ViewManagerASB: PAGEFLIP");

                mParent.onButtonClick(TCONST.PAGEFLIP_BUTTON);
            }
        });

        mSay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(QGRAPH_MSG, "event.click: " + " CRt_ViewManagerASB:onButtonClick SPEAKBUTTON");

                mParent.onButtonClick(TCONST.SPEAK_BUTTON);
            }
        });
    }

    /**
     *  This configures the target display components to be populated with data.
     *
     *  mPageImage - mPageText
     *
     */
    public void flipPage() {

        // Note that we use zero based indexing so page zero is first page - i.e. odd
        //
        if (mCurrPage % 2 == 0) {

            mCurrViewIndex = mOddIndex;
            mPageImage = (ImageView) mOddPage.findViewById(R.id.SpageImage);
            mPageText  = (TextView) mOddPage.findViewById(R.id.SstoryText);

            mPageFlip = (ImageButton) mOddPage.findViewById(R.id.SpageFlip);
            mSay      = (ImageButton) mOddPage.findViewById(R.id.Sspeak);


        } else {

            mCurrViewIndex = mEvenIndex;
            mPageImage = (ImageView) mEvenPage.findViewById(R.id.SpageImage);
            mPageText  = (TextView) mEvenPage.findViewById(R.id.SstoryText);

            mPageFlip = (ImageButton) mEvenPage.findViewById(R.id.SpageFlip);
            mSay      = (ImageButton) mEvenPage.findViewById(R.id.Sspeak);
        }

        // Ensure the buttons reflect the current states
        //
        updateButtons();
    }


    private void configurePageImage() {

        InputStream in;

        try {
            if (assetLocation.equals(TCONST.EXTERN)) {

                Log.d(TCONST.DEBUG_STORY_TAG, "loading image " + mAsset + data[mCurrPage].image);
                in = new FileInputStream(mAsset + data[mCurrPage].image); // ZZZ load image

            } else if (assetLocation.equals(TCONST.EXTERN_SHARED)) {

                Log.d(TCONST.DEBUG_STORY_TAG, "loading shared image " + mAsset + data[mCurrPage].image);
                in = new FileInputStream(mAsset + data[mCurrPage].image); // ZZZ load image
            } else {

                Log.d(TCONST.DEBUG_STORY_TAG, "loading image from asset" + mAsset + data[mCurrPage].image);
                in = JSON_Helper.assetManager().open(mAsset + data[mCurrPage].image); // ZZZ load image
            }

            // ALAN_HILL (5) here is how to load the image...... NEXT NEXT NEXT
            mPageImage.setImageBitmap(BitmapFactory.decodeStream(in));

        } catch (IOException e) {

            mPageImage.setImageBitmap(null);
            e.printStackTrace();
        }
    }


    private String[] splitWordOnChar(String[] wordArray, String splitChar) {

        ArrayList<String> wordList = new ArrayList<>();

        for (String word : wordArray) {

            String[] wordSplit = word.split(splitChar);

            if (wordSplit.length > 1) {

                for (int i1 = 0 ; i1 < wordSplit.length-1 ; i1++) {
                    wordList.add(wordSplit[i1] + splitChar);
                }
                wordList.add(wordSplit[wordSplit.length-1]);
            } else {
                wordList.add(wordSplit[0]);
            }
        }

        return wordList.toArray(new String[wordList.size()]);
    }


    private String[] splitRawSentence(String rawSentence) {

        String  sentenceWords[];

        sentenceWords = rawSentence.trim().split("\\s+");

        sentenceWords = stripLeadingTrailing(sentenceWords, "'");
        sentenceWords = splitWordOnChar(sentenceWords, "-");
        sentenceWords = splitWordOnChar(sentenceWords, "'");

        return sentenceWords;
    }


    /**
     * This cleans a raw sentence from the ASB.  This is very idiosyncratic to the ASB content.
     * ASB contains some apostrophes used as single quotes that otherwise confuse the layout
     *
     * We also need to have true apostrophes and hyphenated words split to maintain alignment with
     * the listener.  i.e the displayed words and spoken word arrays should be kept in alignment.
     *
     * @param rawSentence
     * @return
     */
    private String processRawSentence(String rawSentence) {

        String[]      sentenceWords;
        StringBuilder sentence = new StringBuilder();

        sentenceWords = splitRawSentence(rawSentence);

        for (int i1 = 0 ; i1 < sentenceWords.length ; i1++) {

            if (sentenceWords[i1].endsWith("'") || sentenceWords[i1].endsWith("-")) {
                sentence.append(sentenceWords[i1]);
            } else {
                sentence.append(sentenceWords[i1] + ((i1 < sentenceWords.length-1)? TCONST.WORD_SPACE: TCONST.NO_SPACE));
            }
        }

        return sentence.toString();
    }


    private String stripLeadingTrailing(String sentence, String stripChar) {

        if (sentence.startsWith(stripChar)) {
            sentence = sentence.substring(1);
        }
        if (sentence.endsWith(stripChar)) {
            sentence = sentence.substring(0, sentence.length()-1);
        }

        return sentence;
    }


    private String[] stripLeadingTrailing(String[] wordArray, String stripChar) {

        ArrayList<String> wordList = new ArrayList<>();

        for (String word : wordArray) {

            if (word.startsWith(stripChar)) {
                word = word.substring(1);
            }
            if (word.endsWith(stripChar)) {
                word = word.substring(0, word.length()-1);
            }

            wordList.add(word);
        }

        return wordList.toArray(new String[wordList.size()]);
    }


    /**
     * Reconfigure for a specific page / paragraph / line (seeks to)
     *
     * @param currPage
     * @param currPara
     * @param currLine
     */
    private void seekToStoryPosition(int currPage, int currPara, int currLine, int currWord) {

        String otherWordsToSpeak[];

        completedSentencesFmtd = "";
        completedSentences     = "";
        futureSentencesFmtd    = "";
        futureSentences        = "";
        wordsSpoken            = new ArrayList<>();
        futureSpoken           = new ArrayList<>();

        Log.d(TAG, "seekToStoryPosition: Page: " + currPage + " - Paragraph: " + currPara + " - line: " + currLine + " - word: " + currWord);

        // Optimization - Skip If seeking to the very first line
        //
        // Otherwise create 2 things:
        //
        // 1. A visually formatted representation of the words already spoken
        // 2. A list of words already spoken - for use in the Sphinx language model
        //
        if (currPara > 0 || currLine > 0) {

            // First generate all completed paragraphs in their entirity
            //
            for (int paraIndex = 0 ; paraIndex < currPara ; paraIndex++) {

                for (CASB_Content rawContent : data[currPage].text[paraIndex]) {

                    otherWordsToSpeak = rawContent.sentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

                    // Add the previous line to the list of spoken words used to build the
                    // language model - so it allows all on screen words to be spoken
                    //
                    for (String word : otherWordsToSpeak)
                        wordsSpoken.add(word);

                    completedSentences += processRawSentence(rawContent.sentence) + TCONST.SENTENCE_SPACE;
                }
                if (paraIndex < currPara)
                    completedSentences += "<br><br>";
            }

            // Then generate all completed sentences from the current paragraph
            //
            for (int lineIndex = 0 ; lineIndex <  currLine ; lineIndex++) {

                rawSentence = data[currPage].text[currPara][lineIndex].sentence;
                otherWordsToSpeak = rawSentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

                // Add the previous line to the list of spoken words used to build the
                // language model - so it allows all on screen words to be spoken
                //
                for (String word : otherWordsToSpeak)
                    wordsSpoken.add(word);

                completedSentences += processRawSentence(rawSentence) + TCONST.SENTENCE_SPACE;
            }

            // Note that we add a space after the sentence.
            //
            completedSentencesFmtd = "<font color='#AAAAAA'>";
            completedSentencesFmtd += completedSentences;
            completedSentencesFmtd += "</font>";
        }


        // Generate the active line of text - target sentence
        // Reset the highlight
        mCurrHighlight = TCONST.EMPTY;

        mCurrPage = currPage;
        mCurrPara = currPara;
        mCurrLine = currLine;

        mPageCount = data.length;
        mParaCount = data[currPage].text.length;
        mLineCount = data[currPage].text[currPara].length;

        // WARNING: referring to sentences as "lines" is dangerously misleading
        rawNarration = data[currPage].text[currPara][currLine].narration;
        rawSentence  = data[currPage].text[currPara][currLine].sentence;
        if (data[currPage].prompt != null) page_prompt = data[currPage].prompt;

        // Words that are used to build the display text - include punctuation etc.
        //
        // NOTE: wordsToSpeak is used in generating the active ASR listening model
        // so it must reflect the current sentence without punctuation!
        //
        // To keep indices into wordsToSpeak in sync with wordsToDisplay we break the words to
        //        // display if they contain apostrophes or hyphens into sub "words" - e.g. "thing's" -> "thing" "'s"
        //        // these are reconstructed by the highlight logic without adding spaces which it otherwise inserts
        //        // automatically.
        //
        wordsToDisplay = splitRawSentence(rawSentence);


        // TODO: strip word-final or -initial apostrophes as in James' or 'cause.
        // Currently assuming hyphenated expressions split into two Asr words.
        //
        wordsToSpeak = rawSentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

        mCurrWord  = currWord;
        mWordCount = wordsToSpeak.length;

        // If we are showing future content - i.e. we want the entire page to be visible but
        // only the "current" line highlighted.
        // Note we need ...Count vars initialized here
        //
        // Create 2 things:
        //
        // 1. A visually formatted representation of the words not yet spoken
        // 2. A list of future words to be spoken - for use in the Sphinx language model
        //
        if (showFutureContent) {

            // Generate all remaining sentences in the current paragraph
            //
            // Then generate all future sentences from the current paragraph
            //
            for (int lineIndex = currLine+1 ; lineIndex <  mLineCount ; lineIndex++) {

                rawSentence = data[currPage].text[currPara][lineIndex].sentence;
                otherWordsToSpeak = rawSentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

                // Add the previous line to the list of spoken words used to build the
                // language model - so it allows all on screen words to be spoken
                //
                for (String word : otherWordsToSpeak)
                    futureSpoken.add(word);

                futureSentences += processRawSentence(rawSentence) + TCONST.SENTENCE_SPACE;
            }

            // First generate all completed paragraphs in their entirity
            //
            for (int paraIndex = currPara+1 ; paraIndex < mParaCount ; paraIndex++) {

                // Add the paragraph break if not at the end
                //
                futureSentences += "<br><br>";

                for (CASB_Content rawSentence : data[currPage].text[paraIndex]) {

                    otherWordsToSpeak = rawSentence.sentence.replace('-', ' ').replaceAll("['.!?,:;\"\\(\\)]", " ").toUpperCase(Locale.US).trim().split("\\s+");

                    // Add the previous line to the list of spoken words used to build the
                    // language model - so it allows all on screen words to be spoken
                    //
                    for (String word : otherWordsToSpeak)
                        futureSpoken.add(word);

                    futureSentences += processRawSentence(rawSentence.sentence) + TCONST.SENTENCE_SPACE;
                }
            }

            // TODO : parameterize the color

            futureSentencesFmtd = "<font color='#AAAAAA'>";
            futureSentencesFmtd += futureSentences;
            futureSentencesFmtd += "</font>";
        }


        // Publish the state out to the scripting scope in the tutor
        //
        publishStateValues();

        // Update the sentence display
        //
        UpdateDisplay();

        if (isNarrationCaptureMode)
            feedSentence(mCurrWord);

        // Once past the storyName initialization stage - Listen for the target word -
        //
        if (!storyBooting)
            speakOrListen();

    }


    private void initSegmentation(int _uttNdx, int _segNdx) {

        utteranceNdx  = _uttNdx;
        numUtterance  = rawNarration.length;
        currUtterance = rawNarration[utteranceNdx];
        segmentArray  = rawNarration[utteranceNdx].segmentation;

        segmentNdx    = _segNdx;
        numSegments   = segmentArray.length;
        utterancePrev = utteranceNdx == 0 ? 0 : rawNarration[utteranceNdx - 1].until;
        segmentPrev   = utterancePrev;

        mParent.post(TCONST.STOP_AUDIO, new Long(currUtterance.until * 10));


        // Clean the extension off the end - could be either wav/mp3
        //
        String filename = currUtterance.audio.toLowerCase();

        if (filename.endsWith(".wav") || filename.endsWith(".mp3")) {
            filename = filename.substring(0,filename.length()-4);
        }

        // Publish the current utterance within sentence
        //
        mParent.publishValue(TCONST.RTC_VAR_UTTERANCE,  filename);


        // NOTE: Due to inconsistencies in the segmentation data, you cannot depend on it
        // having precise timing information.  As a result the segment may timeout before the
        // audio has completed. To avoid this we use oncomplete in type_audio to push an
        // TRACK_SEGMENT back to this components queue.
        // Tell the script to speak the new uttereance
        //
        //        mParent.applyBehavior(TCONST.SPEAK_UTTERANCE);
    }


    private void trackNarration(boolean start) {

        narrationTracking = true;

        if (start) {

            mHeardWord    = 0;
            splitIndex    = TCONST.INITSPLIT;
            endOfSentence = false;

            initSegmentation(0, 0);

            spokenWords   = new ArrayList<String>();

                // Tell the script to speak the new uttereance
                // when its not in narrate mode, that
                //
                mParent.applyBehavior(TCONST.SPEAK_UTTERANCE);


            postDelayedTracker();
        } else {

            // NOTE: The narration mode uses the AS R logic to simplify operation.  In doing this
            /// it uses the wordsToSpeak array to progressively highlight the on screen text based
            /// on the timing found in the segmentation data.
            //
            // Special processing to account for apostrophes and hyphenated words
            // Note the system listens for e.g. "WON'T" as [WON] [T] two words so if we provide "won't" then it "won't" match :)
            // and the narration will freeze
            // This is a kludge to account for the fact that segmentation data does not split words with
            // hyphens or apostrophes into separate "words" the way the wordstospeak does.
            // Without this the narration will get out of sync
            //
            if (splitIndex == TCONST.INITSPLIT) {
                splitSegment = narrationSegment.word.toUpperCase().split("[\\-']");

                splitIndex = 0;
                spokenWords.add(splitSegment[splitIndex++]);

            } else if (splitIndex < splitSegment.length){

                spokenWords.add(splitSegment[splitIndex++]);
            } else {

                Log.d(TAG, "HERE");
            }

            // Update the display
            //
            onUpdate(spokenWords.toArray(new String[spokenWords.size()]));

            // If the segment word is complete continue to the next segment - note that this is
            // generally the case.  Words are not usually split by punctuation
            //
            if (splitIndex >= splitSegment.length) {

                splitIndex = TCONST.INITSPLIT;

                // sentences are built from an array of utterances which are build from an array
                // of segments (i.e. timed words)
                //
                // Note the last segment is not timed.  It is driven by the TRACK_COMPLETE event
                // from the audio mp3 playing.  This is required as the segmentation data is not
                // sufficiently accurate to ensure we don't interrupt a playing utterance.
                //track
                segmentNdx++;
                if (segmentNdx >= numSegments) {

                    // If we haven't consumed all the utterances (i.e "narrations") in the
                    // sentence prep the next
                    //
                    // NOTE: Prep the state and wait for the TRACK_COMPLETE event to invoke
                    // trackSegment to continue or terminate
                    //
                    utteranceNdx++;
                    if (utteranceNdx < numUtterance) {

                        initSegmentation(utteranceNdx, 0);

                    } else {

                        endOfSentence = true;
                        narrationTracking = false;
                    }
                }
                // All the segments except the last one are timed based on the segmentation data.
                // i.e. the audio plays and this highlights words based on prerecorded durations.
                //
                else {
                    postDelayedTracker();
                }
            }
            // If the segment word is split due to apostrophes or hyphens then consume them
            // before continuing to the next segment.
            //
            else {
                mParent.post(TCONST.TRACK_NARRATION, 0);
            }
        }
    }


    private void postDelayedTracker() {
        narrationSegment = rawNarration[utteranceNdx].segmentation[segmentNdx];

        segmentCurr = utterancePrev + narrationSegment.end;

        mParent.post(TCONST.TRACK_NARRATION, new Long((segmentCurr - segmentPrev) * 10));

        segmentPrev = segmentCurr;
    }


    private void trackSegment() {

        if (!endOfSentence) {

            // Tell the script to speak the new utterance
            //
            mParent.applyBehavior(TCONST.SPEAK_UTTERANCE);
            postDelayedTracker();
        } else {
            mParent.applyBehavior(TCONST.UTTERANCE_COMPLETE_EVENT);
        }
    }


    public void execCommand(String command, Object target ) {

        long    delay  = 0;

        switch (command) {

            case TCONST.START_NARRATION:

                trackNarration(true);
                break;

            case TCONST.NEXT_WORD:
                generateVirtualASRWord();
                break;

            case TCONST.NEXT_PAGE:
                nextPage();
                break;

            case TCONST.NEXT_SCENE:
                mParent.nextScene();
                break;

            case TCONST.TRACK_NARRATION:

                trackNarration(false);
                break;

            case TCONST.TRACK_SEGMENT:

                trackSegment();
                break;

            case TCONST.NEXT_NODE:

                mParent.nextNode();
                break;

            case TCONST.STOP_AUDIO:
                mParent.stopAudio();
                break;

            case TCONST.SPEAK_EVENT:
            case TCONST.UTTERANCE_COMPLETE_EVENT:

                mParent.applyBehavior(command);
                break;

        }
    }


    /**
     * Push the state out to the tutor domain.
     *
     */
    private void publishStateValues() {

        Log.d(TAG, "publishStateValues: mCurrWord = " + mCurrWord + ", mWordCount = " + mWordCount);

        String cummulativeState = TCONST.RTC_CLEAR;

        // ensure echo state has a valid value.
        //
        mParent.publishValue(TCONST.RTC_VAR_ECHOSTATE, TCONST.FALSE);
        mParent.publishValue(TCONST.RTC_VAR_PARROTSTATE, TCONST.FALSE);
        mParent.publishValue(TCONST.RTC_VAR_NARRATESTATE, TCONST.FALSE);
        mParent.publishValue(TCONST.RTC_VAR_NARRATECOMPLETESTATE, TCONST.FALSE);

        if (prompt != null) {
            mParent.publishValue(TCONST.RTC_VAR_PROMPT, prompt);
            mParent.publishFeature((TCONST.FTR_PROMPT));
        }

        if (page_prompt != null) {
            mParent.publishValue(TCONST.RTC_VAR_PAGE_PROMPT, page_prompt);
            mParent.publishFeature((TCONST.FTR_PAGE_PROMPT));
        }

        // Set the scriptable flag indicating the current state.
        //
        if (mCurrWord >= mWordCount) {

                    // In echo mode - After line has been echoed we switch to Read mode and
                    // read the next sentence.
                    //
                    if (mParent.testFeature(TCONST.FTR_USER_ECHO) || mParent.testFeature(TCONST.FTR_USER_REVEAL) || mParent.testFeature(TCONST.FTR_USER_PARROT)) {

                        // Read Mode - When user finishes reading switch to Narrate mode and
                        // narrate the same sentence - i.e. echo
                        //
                        if (hearRead.equals(FTR_USER_READ)) {

                            if(mParent.testFeature(TCONST.FTR_NARRATION_CAPTURE)) {
                                mParent.publishValue(TCONST.RTC_VAR_NARRATECOMPLETESTATE, TCONST.TRUE);
                            }

                            if (!mParent.testFeature(TCONST.FTR_USER_PARROT))
                                mParent.publishValue(TCONST.RTC_VAR_ECHOSTATE, TCONST.TRUE);

                            hearRead = TCONST.FTR_USER_HEAR;
                            mParent.retractFeature(FTR_USER_READING);

                            Log.d("ISREADING", "NO");

                            cummulativeState = TCONST.RTC_LINECOMPLETE;
                            mParent.publishValue(TCONST.RTC_VAR_WORDSTATE, TCONST.LAST);

                            mListener.setPauseListener(true);
                        }
                        // Narrate mode - swithc back to READ and set line complete flags
                        //
                        else {
                            hearRead = FTR_USER_READ;
                            mParent.publishFeature(FTR_USER_READING);

                            if (mParent.testFeature(TCONST.FTR_USER_PARROT)) mParent.publishValue(TCONST.RTC_VAR_PARROTSTATE, TCONST.TRUE);

                            Log.d("ISREADING", "YES");

                            cummulativeState = TCONST.RTC_LINECOMPLETE;
                            mParent.publishValue(TCONST.RTC_VAR_WORDSTATE, TCONST.LAST);
                        }
            } else {
                cummulativeState = TCONST.RTC_LINECOMPLETE;
                mParent.publishValue(TCONST.RTC_VAR_WORDSTATE, TCONST.LAST);
            }
        } else
            mParent.publishValue(TCONST.RTC_VAR_WORDSTATE, TCONST.NOT_LAST);

        if (mCurrLine >= mLineCount-1) {
            cummulativeState = TCONST.RTC_PARAGRAPHCOMPLETE;
            mParent.publishValue(TCONST.RTC_VAR_LINESTATE, TCONST.LAST);
        } else
            mParent.publishValue(TCONST.RTC_VAR_LINESTATE, TCONST.NOT_LAST);

        if (mCurrPara >= mParaCount-1) {
            cummulativeState = TCONST.RTC_PAGECOMPLETE;
            mParent.publishValue(TCONST.RTC_VAR_PARASTATE, TCONST.LAST);
        } else
            mParent.publishValue(TCONST.RTC_VAR_PARASTATE, TCONST.NOT_LAST);

        if (mCurrPage >= mPageCount-1) {
            cummulativeState = TCONST.RTC_STORYCMPLETE;
            mParent.publishValue(TCONST.RTC_VAR_PAGESTATE, TCONST.LAST);
        } else
            mParent.publishValue(TCONST.RTC_VAR_PAGESTATE, TCONST.NOT_LAST);


        // Publish the cumulative state out to the scripting scope in the tutor
        //
        mParent.publishValue(TCONST.RTC_VAR_STATE, cummulativeState);
        buttonState = cummulativeState;
    }


    /**
     *  Configure for specific Page
     *  Assumes current storyName
     *
     * @param pageIndex
     */
    @Override
    public void seekToPage(int pageIndex) {

        mCurrPage = pageIndex;

        if (mCurrPage > mPageCount-1) mCurrPage = mPageCount-1;
        if (mCurrPage < TCONST.ZERO)  mCurrPage = TCONST.ZERO;

        incPage(TCONST.ZERO);
    }

    @Override
    public void nextPage() {

        if (mCurrPage < mPageCount-1) {
            incPage(TCONST.INCR);
        }

        // Actually do the page animation
        //
        mParent.animatePageFlip(true, mCurrViewIndex);

    }
    @Override
    public void prevPage() {

        if (mCurrPage > 0) {
            incPage(TCONST.DECR);
        }

        //TODO: CHECK
        mParent.animatePageFlip(false, mCurrViewIndex);
    }

    private void incPage(int direction) {

        mCurrPage += direction;

        // This configures the target display components to be populated with data.
        // mPageImage - mPageText
        //
        flipPage();

        configurePageImage();

        // Update the state vars
        // Note that this must be done after flip and configure so the target text and image views
        // are defined
        // NOTE: we reset mCurrPara, mCurrLine and mCurrWord
        //
        seekToStoryPosition(mCurrPage, TCONST.ZERO, TCONST.ZERO, TCONST.ZERO);
        updateButtonPositions();
    }

    private void updateButtonPositions() {
       Log.d("CRt_ViewManager", "isNarrateMode is" + isNarrationCaptureMode);
        if (mCurrPage % 2 == 0) {
            backButton = (ImageButton) mOddPage.findViewById(R.id.backButton);
            forwardButton = (ImageButton) mOddPage.findViewById(R.id.forwardButton);
        } else {
            backButton = (ImageButton) mEvenPage.findViewById(R.id.backButton);
            forwardButton = (ImageButton) mEvenPage.findViewById(R.id.forwardButton);
        }

        if (isNarrationCaptureMode) {
            try {
                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "Back Button Pressed");
                        if (hearRead != null) {
                            if (hearRead.equals(TCONST.FTR_USER_READ)) {
                                AudioWriter.abortOperation();
                                acceptedList.clear();
                        /*
                        } else if (hearRead.equals(TCONST.FTR_USER_HEAR)) {
                            mParent.post(TCONST.STOP_AUDIO, new Long(currUtterance.until * 10));
                            hearRead = TCONST.FTR_USER_HEAR;
                        }

                         */

                                if (mCurrLine > 0) {
                                    seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine - 1, 0);
                                } else if (mCurrPara > 0) {
                                    seekToStoryPosition(mCurrPage, mCurrPara - 1, 0, 0);
                                } else if (mCurrPage > 0) {
                                    Log.d(TAG, "mCurrPage: " + mCurrPage + " mCurrPara: " + mCurrPara + " mCurrLine: " + mCurrLine);
                                    seekToStoryPosition(mCurrPage - 1, 0, 0, 0);
                                }
                            }
                        }


                    }
                });

                forwardButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "Skip Button Pressed");

                        Log.d(TAG, "hearRead: " + hearRead);
                        if (hearRead != null) {
                            if (hearRead.equals(TCONST.FTR_USER_READ)) {
                                AudioWriter.abortOperation();
                                acceptedList.clear();
                        /*
                        } else if (hearRead.equals(TCONST.FTR_USER_HEAR)) {
                            mParent.post(TCONST.STOP_AUDIO, new Long(currUtterance.until * 10));

                        }

                         */
                                if (mCurrLine < mLineCount - 1) {
                                    seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine + 1, 0);
                                } else if (mCurrPara < mParaCount - 1) {
                                    seekToStoryPosition(mCurrPage, mCurrPara + 1, 0, 0);

                                } else if (mCurrPage < mPageCount - 1) {
                                    seekToStoryPosition(mCurrPage + 1, 0, 0, 0);

                                }
                            }
                        }

                        UpdateDisplay();


                    }
                });

                forwardButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
            } catch (NullPointerException e) {
                Log.d("CRt_ViewManagerASB", "Couldn't find forwardbutton and backbutton");
            }
        } else {
            // make buttons invisible
            forwardButton.setVisibility(View.GONE);
            backButton.setVisibility(View.GONE);
        }


    }


    /**
     *  Configure for specific Paragraph
     *  Assumes current page
     *
     * @param paraIndex
     */
    @Override
    public void seekToParagraph(int paraIndex) {

        mCurrPara = paraIndex;

        if (mCurrPara > mParaCount-1) mCurrPara = mParaCount-1;
        if (mCurrPara < TCONST.ZERO)  mCurrPara = TCONST.ZERO;

        incPara(TCONST.ZERO);
    }

    @Override
    public void nextPara() {

        if (mCurrPara < mParaCount-1) {
            incPara(TCONST.INCR);
        }

    }

    @Override
    public void prevPara() {

        if (mCurrPara > 0) {
            incPara(TCONST.DECR);
        }

    }

    // NOTE: we reset mCurrLine and mCurrWord
    private void incPara(int incr) {

        mCurrPara += incr;

        // Update the state vars
        //
        seekToStoryPosition(mCurrPage, mCurrPara, TCONST.ZERO, TCONST.ZERO);
    }


    /**
     *  Configure for specific line
     *  Assumes current page and paragraph
     *
     * @param lineIndex
     */
    @Override
    public void seekToLine(int lineIndex) {

        mCurrLine = lineIndex;

        if (mCurrLine > mLineCount-1) mCurrLine = mLineCount-1;
        if (mCurrLine < TCONST.ZERO)  mCurrLine = TCONST.ZERO;

        incLine(TCONST.ZERO);
    }

    @Override
    public void nextLine() {

        if (mCurrLine < mLineCount-1) {
            incLine(TCONST.INCR);
        }

    }
    @Override
    public void prevLine() {

        if (mCurrLine > 0 ) {
            incLine(TCONST.DECR);
        }
    }

    /**
     *  NOTE: we reset mCurrWord - last parm in seekToStoryPosition
     *
     */
    private void incLine(int incr) {

        // reset boot flag to
        //
        if (storyBooting) {

            storyBooting = false;
            speakOrListen();
        } else {

            mCurrLine += incr;

            // Update the state vars
            //
            seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine, TCONST.ZERO);
        }
    }


    /**
     *  NOTE: we reset mCurrWord - last parm in seekToStoryPosition
     *
     */
    @Override
    public void echoLine() {

        isUserNarrating = true;

        if (isNarrationCaptureMode) {

            // endOfUtteranceCapture();
             // sets data narration
        }
        // reset the echo flag
        //
        mParent.publishValue(TCONST.RTC_VAR_ECHOSTATE, TCONST.FALSE);

        // Update the state vars
        //
        seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine, TCONST.ZERO); //
    }


    /**
     *
     */
    @Override
    public void parrotLine() {

        mParent.publishValue(TCONST.RTC_VAR_PARROTSTATE, TCONST.FALSE);

        Log.d(TAG, "parrotLine");

        seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine, TCONST.ZERO);
    }


    /**
     *  Configure for specific word
     *  Assumes current page, paragraph and line
     *
     * @param wordIndex
     */
    @Override
    public void seekToWord(int wordIndex) {

        mCurrWord = wordIndex;
        mHeardWord = 0;

        if (mCurrWord > mWordCount-1) mCurrWord = mWordCount-1;
        if (mCurrWord < TCONST.ZERO)  mCurrWord = TCONST.ZERO;

        // Update the state vars
        //
        seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine, wordIndex);

        incWord(TCONST.ZERO);

        // Start listening from the new position
        //
        speakOrListen();
    }


    @Override
    public void nextWord() {

        if (mCurrWord < mWordCount) {
            incWord(TCONST.INCR);
        }
    }
    @Override
    public void prevWord() {

        if (mCurrWord > 0) {
            incWord(TCONST.DECR);
        }
    }

    /**
     * We assume this has been bounds checked prior to call
     *
     * There is one optimization here - when simply moving through the sentence we do not rebuild
     * the entire state.  We just publish any change of state
     *
     * @param incr
     */
    private void incWord(int incr) {

        mCurrWord += incr;

        // For instances where we are advancing the word manually through a script it is required
        // that you reset the highlight and the FTR_WRONG so the next word is highlighted correctly
        //
        setHighLight(TCONST.EMPTY, false);
        mParent.UpdateValue(true);

        // Publish the state out to the scripting scope in the tutor
        //
        publishStateValues();

        // Update the sentence display
        //
        UpdateDisplay();
    }

    /**
     * This picks up listening from the last word - so it seeks to wherever we are in the
     * current sentence and listens from there.
     */
    public void continueListening() {
        speakOrListen();
    }


    private void startListening() {

        // We allow the user to say any of the onscreen words but set the priority order of how we
        // would like them matched  Note that if the listener is not explicitly listening for a word
        // it will just ignore it if spoken.
        //
        // for the current target word.
        // 1. Start with the target word on the target sentence
        // 2. Add the words from there to the end of the sentence - just to permit them
        // 3. Add the words already spoken from the other lines - just to permit them
        //
        // "Permit them": So the language model is listening for them as possibilities.
        //
        wordsToListenFor = new ArrayList<>();

        for (int i1 = mCurrWord; i1 < wordsToSpeak.length; i1++) {
            wordsToListenFor.add(wordsToSpeak[i1]);
        }
        for (int i1 = 0; i1 < mCurrWord; i1++) {
            wordsToListenFor.add(wordsToSpeak[i1]);
        }
        for (String word : wordsSpoken) {
            wordsToListenFor.add(word);
        }

        // If we want to listen for all the words that are visible
        //
        if (listenFutureContent) {
            for (String word : futureSpoken) {
                wordsToListenFor.add(word);
            }
        }

        // Start listening
        //
        if (mListener != null) {

            // reset the relative position of mCurrWord in the incoming PLRT heardWords array
            mHeardWord = 0;
            mListener.reInitializeListener(true);
            mListener.updateNextWordIndex(mHeardWord);

            mListener.listenFor(wordsToListenFor.toArray(new String[wordsToListenFor.size()]), 0);
            mListener.setPauseListener(false);
        }
    }


    /**
     * Scipting mechanism to update target word highlight
     * @param highlight
     */
    @Override
    public void setHighLight(String highlight, boolean update) {

        mCurrHighlight = highlight;

        // Update the sentence display
        //
        if (update)
            UpdateDisplay();
    }


    /**
     *  Update the displayed sentence
     */
    private void UpdateDisplay() {
        Log.d(TAG, "Updating display");

        if (showWords) {
            String fmtSentence = "";

            for (int i = 0; i < wordsToDisplay.length; i++) {

                String styledWord = wordsToDisplay[i];                           // default plain

                if (i < mCurrWord) {
                    styledWord = "<font color='#00B600'>" + styledWord + "</font>";
                }

                if (i == mCurrWord) {// style the next expected word

                    if (!mCurrHighlight.equals(TCONST.EMPTY))
                        styledWord = "<font color='" + mCurrHighlight + "'>" + styledWord + "</font>";

                    styledWord = "<u>" + styledWord + "</u>";
                }

                if (showFutureWords || i < mCurrWord) {
                    if (wordsToDisplay[i].endsWith("'") || wordsToDisplay[i].endsWith("-")) {
                        fmtSentence += styledWord;
                    } else {
                        fmtSentence += styledWord + ((i < wordsToDisplay.length - 1) ? TCONST.WORD_SPACE : TCONST.NO_SPACE);
                    }
                }
            }

            // Generate the text to be displayed
            //
            String content = completedSentencesFmtd + fmtSentence;

            if (showFutureContent)
                content += TCONST.SENTENCE_SPACE + futureSentencesFmtd;



            if (isNarrationCaptureMode) {

                int progress = 0;
                for(CASB_Narration narration : data[mCurrPage].text[mCurrPara][mCurrLine].narration) {
                    acceptedList.add(new int[]{progress, progress + narration.segmentation.length - 1});
                    progress = narration.segmentation.length;
                }

                boolean cyan = true;
                SpannableStringBuilder preHighlight = new SpannableStringBuilder(Html.fromHtml(fmtSentence));
                Log.d("Highlighting", "Updating narration highlighting");

                // check for bugs with segments
                StringBuilder s = new StringBuilder();
                for(int[] segment : acceptedList) {
                    s.append(segment[0] + " " + segment[1] + ", ");
                }
                Log.d("Highlighting", "Segments start and end as follows: " + s.toString());

                for(int[] segment : acceptedList) {
                    if (segment[1] >= segment[0]) {
                        StringBuilder textBuilder = new StringBuilder();
                        if (segment[0] != 0)
                            textBuilder.append(" ");
                        for (int i = segment[0]; i <= segment[1]; i++) {
                            try {
                            if(wordsToDisplay.length > i)
                                textBuilder.append(wordsToDisplay[i]);


                                if(!(wordsToDisplay[i].endsWith("-") || wordsToDisplay[i].endsWith("'")))
                                    textBuilder.append(" ");
                            } catch (ArrayIndexOutOfBoundsException ignored) {}
                        }
                        textBuilder.deleteCharAt(textBuilder.length() -1);

                        StringBuilder precedingTextBuilder = new StringBuilder();
                        for (int i = 0; i < segment[0]; i++) {
                            precedingTextBuilder.append(wordsToDisplay[i]);
                            if(!wordsToDisplay[i].endsWith("-") || !wordsToDisplay[i].endsWith("'")  )
                                precedingTextBuilder.append(" ");
                        }
                        if (precedingTextBuilder.toString().endsWith(" ")) {
                            precedingTextBuilder.deleteCharAt(precedingTextBuilder.length() - 1);
                        }
                        String text = textBuilder.toString();

                        int index = precedingTextBuilder.toString().length();

                        Log.d("Highlighting_TAG", "Segment[0]: " + segment[0] + "segment[1]" + segment[1]);
                        Log.d(TAG, "Highlight text: " + text + ". index: " + index + ". text length: " + text.length());

                        preHighlight.setSpan(new BackgroundColorSpan((cyan ? Color.parseColor("#00FFFF") : Color.parseColor("#96e3ff"))), index, index + text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        if(segment[1] == wordsToDisplay.length - 1)
                            break;

                        cyan = !cyan;
                    }
                }

                fmtSentence = preHighlight.toString();

                preHighlight.insert(0, Html.fromHtml(completedSentencesFmtd));
                preHighlight.append(TCONST.SENTENCE_SPACE + Html.fromHtml(futureSentencesFmtd));
                mPageText.setText(preHighlight);

            } else {

                mPageText.setText(Html.fromHtml(content));
            }


            Log.d(TAG, "Story Sentence Text: " + content);
        }

        if (showWords && (showFutureWords || mCurrWord > 0)) broadcastActiveTextPos(mPageText, wordsToDisplay);

        // Publish the current word / sentence / remaining words for use in scripts
        //
        if (mCurrWord < wordsToSpeak.length) {
            mParent.publishValue(TCONST.RTC_VAR_WORDVALUE, wordsToSpeak[mCurrWord]);

            String remaining[] = Arrays.copyOfRange(wordsToSpeak, mCurrWord, wordsToSpeak.length);

            mParent.publishValue(TCONST.RTC_VAR_REMAINING, TextUtils.join(" ", remaining));
            mParent.publishValue(TCONST.RTC_VAR_SENTENCE,  TextUtils.join(" ", wordsToSpeak));
        }
    }


    /**
     *
     * @param text
     * @param words
     * @return
     */
    private PointF broadcastActiveTextPos(TextView text, String[] words){

        PointF  point   = new PointF(0,0);
        int     charPos = 0;
        int     maxPos;

        try {
            Layout layout = text.getLayout();

            if (layout != null && mCurrWord < words.length) {

                // Point to the start of the Target sentence (mCurrLine)
                charPos  = completedSentences.length();

                // Find the starting character of the current target word
                for (int i1 = 0; i1 <= mCurrWord; i1++) {
                    charPos += words[i1].length() + 1;
                }

                // Look at the end of the target word
                charPos -= 1;

                // Note that sending a value greater than maxPos will corrupt the textView - so
                // guarantee this will never happen.
                //
                maxPos  = text.getText().length();
                charPos = (charPos > maxPos) ? maxPos : charPos;

                point.x = layout.getPrimaryHorizontal(charPos);

                int y = layout.getLineForOffset(charPos);
                point.y = layout.getLineTop(y);

                CPersonaObservable.broadcastLocation(text, TCONST.LOOKAT, point);
            }

        } catch (Exception e) {
            Log.d(TAG, "broadcastActiveTextPos: " + e.toString());
        }

        return point;
    }


    /**
     * This is where we process words being narrated (by the pre-made narration, not the live user)
     * VMC_QA why does this get called twice for the last word???
     */
    @Override
    public void onUpdate(String[] heardWords) {

        boolean result    = true;
        String  logString = "";

        for (int i = 0; i < heardWords.length; i++) {
            logString += heardWords[i].toLowerCase() + " | ";
        }
        Log.i("ASR", "Update Words Spoken: " + logString);

        while (mHeardWord < heardWords.length) {

            Log.d("CRt_ViewManager", "Heardwords length: " + heardWords.length + ". Current heardWord: " + mHeardWord
             + "wordsToSpeak length: " + wordsToSpeak.length + "current word " + mCurrWord);

            // todo: (chirag) figure out the root cause of the problem instead of this bandaid solution
            boolean match;
            try {
                match = wordsToSpeak[mCurrWord].equals(heardWords[mHeardWord]);
            } catch(ArrayIndexOutOfBoundsException e) {
                match = true;
            }


            //if (wordsToSpeak[mCurrWord].equals(heardWords[mHeardWord])) { // VMC_QA these are not equal. one of these is out of bounds (probably wordsToSpeak)
                // wordsToSpeak is in fact out of bounds here. It is 1 shorter than heardWords */
                // the above was commented out by Chirag in order to ensure that if heardwords is too long (because it often accidentally contains an extra word)
            if(match) {
                nextWord();
                mHeardWord++;

                Log.i("ASR", "RIGHT");
                attemptNum = 0;
                result = true;
            } else {
                Log.e(TAG, "Input Error in narrator no match found - mCurrWord ->" + wordsToSpeak[mCurrWord] + " -> heardWords: " + heardWords[mHeardWord]);

                nextWord();
                mHeardWord++;

                attemptNum = 0;
                result = true;
            }


            mParent.updateContext(rawSentence, mCurrLine, wordsToSpeak, mCurrWord - 1, heardWords[mHeardWord - 1], attemptNum, false, result);
        }

        // Publish the outcome
        mParent.publishValue(TCONST.RTC_VAR_ATTEMPT, attemptNum);
        mParent.UpdateValue(result);
    }


    /**
     * This is where the incoming PLRT ASR data is processed.
     *
     * Provided the input matches the model sentence it continues in sequence through the sentence
     * words. If there is an error it seeks the listener to only "hear" words form the error to the
     * end of sentence and continues like this iteratively as required.  The goal is to eliminate the
     * word shuffling that Multi-Match does and simplify the process. Ultimately this should migrate
     * to using 2 simultaneous decoders one for the correct sentence and one for any other "distractor"
     * words. i.e. other words in the sentence in this case.
     *
     *  TODO: check if it is possible for the hypothesis to chamge between last update and final hyp
     */
    @Override
    public void onUpdate(ListenerBase.HeardWord[] heardWords, boolean finalResult) {
        allHeardWords = heardWords;
        AudioDataStorage.updateHypothesis(heardWords);
        boolean result    = true;
        String  logString = "";

        try {
            for (int i = 0; i < heardWords.length; i++) {
                if (heardWords[i] != null) {
                    logString += heardWords[i].hypWord.toLowerCase() + ":" + heardWords[i].iSentenceWord + " | ";
                } else {
                    logString += "VIRTUAL | ";
                }
            }

            while ((mCurrWord < wordsToSpeak.length) && (mHeardWord < heardWords.length)) {

                // wordIndex is the sentence index of the first heard word, and is used to deduce the position in the sentence in narration capture mode
                int wordIndex = wordsToSpeak.length;
                for (int i = 0; i < wordsToSpeak.length; i++) {
                    if(wordsToSpeak[i].equals(heardWords[mHeardWord].hypWord)) {
                        wordIndex = i;
                        break;
                    }
                }

                if (wordsToSpeak[mCurrWord].equals(heardWords[mHeardWord].hypWord)) {

                    nextWord();
                    mHeardWord++;

                    mListener.updateNextWordIndex(mHeardWord);

                    Log.i("ASR", "RIGHT");
                    attemptNum = 0;
                    result = true;
                    mParent.updateContext(rawSentence, mCurrLine, wordsToSpeak, mCurrWord - 1, heardWords[mHeardWord - 1].hypWord, attemptNum, heardWords[mHeardWord - 1].utteranceId == "", result);

                 // In narration capture mode, it is acceptable if the narrator says the wrong word because they may be starting from a different point than predicted
                } else if (isNarrationCaptureMode && wordIndex <= mCurrWord) {

                    boolean isAtSeam = false;
                    for(int i : seamIndices) {
                        if(wordIndex == i) {
                            isAtSeam = true;
                            break;
                        }
                    }

                    if(isAtSeam) {

                        incWord(wordIndex+1 - mCurrWord);
                        mHeardWord++;
                        mParent.updateContext(rawSentence, mCurrLine, wordsToSpeak, mCurrWord - 1, heardWords[mHeardWord - 1].hypWord, attemptNum, heardWords[mHeardWord - 1].utteranceId == "", result);
                        mListener.updateNextWordIndex(mHeardWord); // what does this do ?? - chirag

                        Log.i("ASR", "Wrong But Continuing");
                        attemptNum = 0;
                        result = true;

                    } else {
                        mListener.setPauseListener(true);

                        Log.i("ASR", "WRONG");
                        attemptNum++;
                        result = false;
                        mParent.updateContext(rawSentence, mCurrLine, wordsToSpeak, mCurrWord, heardWords[mHeardWord].hypWord, attemptNum, heardWords[mHeardWord].utteranceId == "", result);
                        break;
                    }


                } else {

                    mListener.setPauseListener(true);

                    Log.i("ASR", "WRONG");
                    attemptNum++;
                    result = false;
                    mParent.updateContext(rawSentence, mCurrLine, wordsToSpeak, mCurrWord, heardWords[mHeardWord].hypWord, attemptNum, heardWords[mHeardWord].utteranceId == "", result);
                    break;
                }
            }

            Log.i("ASR", "Update New HypSet: " + logString + " - Attempt: " + attemptNum);

            // Publish the outcome
            mParent.publishValue(TCONST.RTC_VAR_ATTEMPT, attemptNum);
            mParent.UpdateValue(result);

            mParent.onASREvent(TCONST.RECOGNITION_EVENT);

        } catch (Exception e) {

            Log.e("ASR", "onUpdate Fault: " + e);
        }
    }


    public void generateVirtualASRWord() {

        mListener.setPauseListener(true);

        ListenerBase.HeardWord words[] = new ListenerBase.HeardWord[mHeardWord+1];

        words[mHeardWord] = new ListenerBase.HeardWord(wordsToSpeak[mCurrWord]);

        onUpdate(words, false);

        mListener.setPauseListener(false);
//        startListening();
    }


    /**
     * This is where incoming JSGF ASR data would be processed.
     *
     *  TODO: check if it is possible for the hypothesis to change between last update and final hyp
     */
    @Override
    public void onUpdate(String[] heardWords, boolean finalResult) {

//        String logString = "";
//
//        for (String hypWord :  heardWords) {
//            logString += hypWord.toLowerCase() + ":" ;
//        }
//        Log.i("ASR", "New JSGF HypSet: "  + logString);
//
//
//        mParent.publishValue(TCONST.RTC_VAR_ATTEMPT, attemptNum++);
//
//        mParent.onASREvent(TCONST.RECOGNITION_EVENT);

    }


    @Override
    public boolean endOfData() {
        return false;
    }


    //************ Serialization


    /**
     * Load the data source
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {

        JSON_Helper.parseSelf(jsonData, this, CClassMap.classMap, scope);
    }

    /**
     * In Narration capture mode, the audio recording data is saved to the storydata file and updated by RoboTutor in order to then echo the file
     */
    @Override
    public void constructAudioStoryData() {

        if (isUserNarrating) {

            // runtime greedy tiling algorithm to build narrations
            int startSegment = 0;
            ArrayList<Integer> prev_i_loc = new ArrayList<Integer>();
            int endSegment = wordsToSpeak.length - 1;

            ArrayList<CASB_Narration> narrationList = new ArrayList<>();

            boolean narrationCovered = false;

            do {

                CASB_Narration narration = new CASB_Narration();
                ArrayList<CASB_Seg> segList = new ArrayList<>();

                // build phrase file name
                StringBuilder fileName = new StringBuilder(mAsset);
                for (int i = startSegment; i <= endSegment; i++) {
                    fileName.append(wordsToSpeak[i].toLowerCase()).append("_");
                }
                fileName.deleteCharAt(fileName.lastIndexOf("_"));
                String fileString = fileName.toString() + ".mp3";

                File audioFile = new File(fileString);

                Log.d(TAG, "Greedy algorithm " + "endsegment: " + endSegment + " startsegment: " + startSegment);
                Log.d(TAG, "Greedy algorithm searching for narration " + fileString);

                if(audioFile.exists()) {
                    Log.d(TAG, "Narration Construction: found file: " + fileString);
                    prev_i_loc.add(startSegment);
                    startSegment = endSegment + 1;
                    endSegment = wordsToSpeak.length - 1;

                    // add segmentation data
                    try {
                        narration.audio = fileString.replace(mAsset, "");

                        FileInputStream segmentationDataFile = new FileInputStream(new File(fileName.toString() + ".seg"));
                        BufferedReader reader = new BufferedReader(new InputStreamReader(segmentationDataFile));
                        String line;

                        while ((line = reader.readLine()) != null) {
                            String[] data = line.split("\t");

                            segList.add(new CASB_Seg(Integer.parseInt(data[1]), Integer.parseInt(data[2]), data[0]));
                        }

                        narration.from = segList.get(0).start;
                        narration.until = segList.get(segList.size()-1).end;
                        narration.utterances = fileName.toString().replace("_", " ");
                        narration.segmentation = segList.toArray(new CASB_Seg[segList.size()]);

                        narrationList.add(narration);

                    } catch (FileNotFoundException e) {
                        Log.wtf(TAG, "Unable to construct narration for " + fileString + " because " + fileName.toString() + ".seg does not exist");
                        return;
                    } catch (IOException e) {
                        Log.wtf(TAG, "Unable to construct narration for " + fileString);
                        Log.getStackTraceString(e);
                        return;
                    }

                } else if(endSegment > startSegment) {
                    // if the file doesn't exist, look for a file that is 1 word shorter
                    endSegment--;

                } else {
                    // if we are down to the last word then an adequate recording doesn't exist. Start search again, but start from a shorter recording than last time
                    try {
                        narrationList.remove(narrationList.size() - 1);
                        endSegment = startSegment - 2;
                        if (prev_i_loc.size() > 0) {
                            startSegment = prev_i_loc.get(prev_i_loc.size() - 1);
                            prev_i_loc.remove(prev_i_loc.size() - 1);
                        } else {
                            Log.d(TAG, "Could not create narration");
                            break;
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.d(TAG, "Narration Capture: There is no possible narration that can be built");
                        break;
                    }
                }


                if(startSegment == wordsToSpeak.length)
                    narrationCovered = true;
            } while(!narrationCovered);

            data[mCurrPage].text[mCurrPara][mCurrLine].narration = narrationList.toArray(new CASB_Narration[narrationList.size()]);

            // todo (chirag): Updated internal representation. now update storydata.json



            isUserNarrating = false;

            resetNarration();
        }

    }

    /**
     * for an incorrect utterance in NARRATION CAPTURE MODE
     * Obsolete
     */
    @Override
    public void clearAudioData() {
        //AudioDataStorage.clearAudioData();
        //AudioWriter.destroyContent();
    }

    /**
     * Line is restarted after a wrong narration in NARRATION CAPTURE MODE
     */
    @Override
    public void startLine() {
        deleteRecording = keepOnlyRelevantAudio;
        seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine, TCONST.ZERO);
        deleteRecording = true;
    }

    int narrateWordsToDelete;
    ArrayList<String> acceptedUtterances = new ArrayList<>();
    ArrayList<int[]> acceptedList = new ArrayList<>();

    private ArrayList<ListenerBase.HeardWord> determineUtterance(boolean lastUtteranceOfSentence) {
        // creates a 2d HeardWord list of all continuous utterances spoken
        if(allHeardWords == null) {
            return new ArrayList<ListenerBase.HeardWord>();
        }
        AudioDataStorage.updateHypothesis(allHeardWords);
        for(ListenerBase.HeardWord word : allHeardWords) {
            Log.d("HeardWords", word.hypWord);
        }

        ArrayList<ArrayList<ListenerBase.HeardWord>> uttMap = new ArrayList<>();

        uttMap.add(new ArrayList<ListenerBase.HeardWord>());

        int start = 1;
        for(int i = 0; i < AudioDataStorage.segmentation.size(); i++) {
            if(AudioDataStorage.segmentation.get(i).matchLevel == ListenerBase.HeardWord.MATCH_EXACT) {
                uttMap.get(0).add(AudioDataStorage.segmentation.get(i));
                start = i + 1;
                break;
            }
        }

        for (int i = start; i < AudioDataStorage.segmentation.size(); i++) {

            // heardword currently being 'investigated'
            ListenerBase.HeardWord currHeardWord = AudioDataStorage.segmentation.get(i);
            // if the word is not an exact match, ignore
            if (currHeardWord.matchLevel == ListenerBase.HeardWord.MATCH_EXACT) {
                ArrayList<ListenerBase.HeardWord> latestSubSeq = uttMap.get(uttMap.size() - 1);

                if (latestSubSeq.size() == 0) {
                    latestSubSeq.add(currHeardWord);
                    // check word is the next sentence word
                } else if (latestSubSeq.get(latestSubSeq.size() - 1).iSentenceWord == currHeardWord.iSentenceWord - 1) {
                    latestSubSeq.add(currHeardWord);
                } else {
                    ArrayList<ListenerBase.HeardWord> nextSubseq = new ArrayList<>();
                    nextSubseq.add(currHeardWord);
                    uttMap.add(nextSubseq);
                }
            } else {
                uttMap.add(new ArrayList<ListenerBase.HeardWord>());
            }
        }

        StringBuilder uttMapDiagram = new StringBuilder("");

        ArrayList<ListenerBase.HeardWord> latestUtterance = new ArrayList<>();

        int maxSeqLength = wordsToSpeak.length - prevStartFrom;

        for (ArrayList<ListenerBase.HeardWord> seq : uttMap) {
            uttMapDiagram.append("\n");
            for (ListenerBase.HeardWord hd : seq) {
                uttMapDiagram.append(hd.hypWord).append(" (").append(hd.iSentenceWord).append(") ");
            }

            if (seq.size() > 0) {
                int firstindex = seq.get(0).iSentenceWord;
                // The sequence must pick up where the narrator left off
                if (firstindex == TCONST.ZERO || firstindex > (wordsToSpeak.length - prevStartFrom - 1)/* ||  firstindex == prevStartFrom || startPoints.contains(firstindex) */ ) {

                    if(!lastUtteranceOfSentence) {
                        // Truncate this subsequence so that it ends in a silence
                        // Start at the end of the subsequence and travel backwards to find the *last silence*
                        for (int i = seq.size() - 1; i > 0; i--) {

                            if (seq.size() < 2) break; // no use trying to test a 1 word utterance
                            if (seq.get(i).startFrame - seq.get(i - 1).endFrame < segmentGapLength) { // 100 centiseconds gap is treated as a silence
                                seq.remove(i);
                            } else if (seq.get(i).hypWord.contains("START_")) {
                                seq.remove(i);
                            } else if (seq.size() > maxSeqLength) {
                                seq.remove(i);
                            } else {
                                seq.remove(i);
                                break; // break if silence found
                            }
                        }
                    }

                    // If this is the farthest reaching subsequence so far save it
                    if (latestUtterance.size() > 0) {
                        if (seq.size() > 1 && seq.get(seq.size() - 1).iSentenceWord > latestUtterance.get(latestUtterance.size() - 1).iSentenceWord) {
                            latestUtterance.clear();
                            latestUtterance.addAll(seq);
                        }
                    } else {
                        latestUtterance.addAll(seq);
                    }

                }
            }
        }
        Log.d("Utterance_Map", uttMapDiagram.toString());
        Log.d(TAG, Integer.toString(latestUtterance.size()));

        Log.d("CRt_ViewmanagerASB", "Utterance Map");


        return latestUtterance;
    }

    /**
     * Restart utterance if the narration is wrong in Narration Capture Mode, preventing the user from having to restart the entire sentence
     */
    @Override
    public void restartUtterance() {
        Log.d(TAG, "restarting utterance");
        AudioDataStorage.updateHypothesis(allHeardWords);
        ArrayList<ListenerBase.HeardWord> latestUtterance = determineUtterance(false);

        if (latestUtterance.size() > 0) {
            StringBuilder newFileName = new StringBuilder();
            for (ListenerBase.HeardWord h : latestUtterance) {
                newFileName.append(h.hypWord.toLowerCase()).append(" ");
            }
            newFileName.deleteCharAt(newFileName.length() - 1);
            Log.d(TAG, "Narration file name: " + newFileName.toString());
            String oldFileName = narrationFileName; // make a copy to be safe
            renameNarration(oldFileName, newFileName.toString());
            narrationFileName = newFileName.toString();

            isUserNarrating = true;
            narrateWordsToDelete = mCurrWord - (latestUtterance.get(latestUtterance.size() - 1).iSentenceWord + prevStartFrom + 1);
            capturedUtt = latestUtterance;

            // output a .seg file
            StringBuilder withPunctuation = new StringBuilder();
            for (ListenerBase.HeardWord w : latestUtterance) {
                withPunctuation.append(w.hypWord).append(" ");
            }
            withPunctuation.deleteCharAt(withPunctuation.length() - 1);
            AudioDataStorage.saveAudioData(narrationFileName, mAsset, mCurrLine, mCurrPara, mCurrPage, withPunctuation.toString(), currUtt, latestUtterance);

            acceptedUtterances.add(narrationFileName);
            acceptedList.add(new int[]{prevStartFrom, latestUtterance.get(latestUtterance.size() - 1).iSentenceWord + prevStartFrom});
            int[] segment = acceptedList.get(acceptedList.size() -1 );
            Log.d(TAG, "Added to acceptedList: segment[0] " + segment[0] + " segment[1] " + segment[1] + ". AcceptedList size is " + acceptedList.size());
            // acceptedList.get(acceptedList.size() - 1)[1] = mCurrWord - 1;

            seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine, prevStartFrom + latestUtterance.get(latestUtterance.size() - 1).iSentenceWord + 1);
            UpdateDisplay();
        } else {
            seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine, prevStartFrom);
        }

    }


    public void endOfUtteranceCapture() {

        capturedUtt = AudioDataStorage.segmentation;

        // creates a 2d HeardWord list of all continuous utterances spoken
        // todo (chirag): write segmentation data to file also

        Log.d(TAG, "end of utterance capture");
        AudioDataStorage.updateHypothesis(allHeardWords);
        ArrayList<ListenerBase.HeardWord> latestUtterance = determineUtterance(true);

        if (latestUtterance.size() > 0) {

            while (!latestUtterance.get(latestUtterance.size() - 1).hypWord.equals(wordsToSpeak[wordsToSpeak.length - 1])) {
                if(latestUtterance.get(latestUtterance.size() - 1).hypWord.equals(wordsToSpeak[wordsToSpeak.length - 2])) {

                    Log.d(TAG, "Sentence end reached, however last word omitted.");
                    hearRead = TCONST.FTR_USER_READ;
                    seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine, TCONST.ZERO);
                    return;

                }
                latestUtterance.remove(latestUtterance.size() - 1);
            }

            StringBuilder newFileName = new StringBuilder();
            for (ListenerBase.HeardWord h : latestUtterance) {
                newFileName.append(h.hypWord.toLowerCase()).append(" ");
            }
            newFileName.deleteCharAt(newFileName.length() - 1);
            String oldFileName = narrationFileName; // make a copy to be thread safe
            renameNarration(oldFileName, newFileName.toString());
            narrationFileName = newFileName.toString();

            isUserNarrating = true;
            narrateWordsToDelete = mCurrWord - (latestUtterance.get(latestUtterance.size() - 1).iSentenceWord + 1);
            capturedUtt = latestUtterance;

            // output a .seg file
            StringBuilder withPunctuation = new StringBuilder();
            for (ListenerBase.HeardWord w : latestUtterance) {
                withPunctuation.append(w.hypWord).append(" ");
            }
            withPunctuation.deleteCharAt(withPunctuation.length() - 1);
            AudioDataStorage.saveAudioData(narrationFileName, mAsset, mCurrLine, mCurrPara, mCurrPage, withPunctuation.toString(), currUtt, latestUtterance);
            acceptedList.add(new int[] {prevStartFrom, wordsToSpeak.length - 1});

            boolean narrationCovered = false;
            int startSegment = 0;
            int endSegment = wordsToSpeak.length - 1;
            ArrayList<Integer> seams = new ArrayList();
            seams.add(-1);
            /*
            while(!narrationCovered) {
                boolean segmentCovered = false;
                for(int[] segment : acceptedList) {
                    if (segment[0] == startSegment && segment[1] == endSegment) {
                        segmentCovered = true;
                        seams.add(endSegment);
                        if(endSegment == wordsToSpeak.length - 1) {
                            narrationCovered = true;
                        } else {
                            startSegment = endSegment + 1;
                            endSegment = wordsToSpeak.length - 1;
                            break;
                        }
                    }
                }
                if(!segmentCovered) {
                    if((endSegment - startSegment) < 2) {
                        if (startSegment > 0) {
                            startSegment = seams.get(seams.size() - 2) + 1;
                            endSegment = seams.get(seams.size() - 1) - 1;
                        } else  {
                            Log.d(TAG, "Sentence end reached however narration was not complete.");
                            hearRead = TCONST.FTR_USER_READ;
                            seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine, TCONST.ZERO);
                            return;
                        }
                    } else {
                        endSegment--;
                    }
                }
            }

             */
            Log.d(TAG, "End Of Utterance Capture. Successfully captured narration of sentence");
            UpdateDisplay();
            acceptedList.clear();
            seamIndices.clear();
            mParent.publishValue(TCONST.RTC_VAR_NARRATECOMPLETESTATE,TCONST.FALSE);
            constructAudioStoryData();
        } else {
            Log.wtf(TAG, "Unable to save final part of narration");
        }


    }


    void renameNarration(String oldFileName, String newFileName) {
        AudioWriter.pauseNRename(oldFileName, newFileName, mAsset);
    }

    int prevStartFrom = 0;

    /**
     * Gives the file location to save the current recording to the AudioWriter so that the data collected is immediately written to file
     * @param startFrom is the location of the sentence to start the recording from
     */
    public void feedSentence(int startFrom) {
        seamIndices.add(startFrom);
        // acceptedList.add(new int[]{mCurrWord, 0});
        Log.d(TAG, "Chirag: currUtt is " + currUtt);
        Log.d(TAG, "Chirag: prevStartFrom will be" + startFrom);

        if (!storyBooting) {
            if (hearRead.equals(TCONST.FTR_USER_HEAR)) {
                AudioWriter.pauseRecording();
                Log.d("Narrate-Debug", "Recording paused (in hear mode)");
                return;
            }
        }

        StringBuilder fileNameBuilder = new StringBuilder();
        int i = 0;
        for (String word : wordsToSpeak) {
            // This uses the wordsToSpeak String[] because it does not contain punctuation
            //if (i >= startFrom) {
                fileNameBuilder.append(word).append(" ");
            //}
            //i++;
        }
        fileNameBuilder.setLength(fileNameBuilder.length() - 1);

        fileNameBuilder.append("TEMP");
        String fileName = fileNameBuilder.toString();

        Log.d("CRt_ saveToFile", "Telling Audiowriter to being writing file. File is: " + fileName);
        AudioWriter.initializePath(fileName, mAsset);

        narrationFileName = fileName;

        prevStartFrom = startFrom;
    }

    /**
     * DO NOT CONFUSE THIS WITH prevLine().
     * THIS IS USED TO GO BACK A SENTENCE REGARDLESS OF CURRENT STORY POSITION. IT'S ORIGINAL PURPOSE IS TO ALLOW FOR NAVIGATION WITHIN THE STORY
     * prevLine() GOES BACK TO THE PREVIOUS SENTENCE IF AND ONLY IF THERE IS A SENTENCE WITHIN THE PARAGRAPH BEFORE IT
     */
    @Override
    public void prevSentence() {

        if (!narrationTracking) {

            Log.d("NavButton", "Back Button has been pressed");
            AudioWriter.abortOperation();
            hearRead = TCONST.FTR_USER_HEAR;
            // It is zero-index
            if (mCurrWord > prevStartFrom) {
                seekToStoryPosition(mCurrPage, mCurrPara, mCurrLine, prevStartFrom);
            } else if (mCurrLine > 0) {
                prevLine();
            } else if (mCurrPara > 0) {
                prevPara();
            } else if (mCurrPage > 0) {
                prevPage();
            }
        }
    }

    @Override
    public void skipSentence() {
        if (!narrationTracking) {
            Log.d("NavButton", "Forward Button has been pressed");
            AudioWriter.abortOperation();
            mCurrWord = mWordCount; // go to the end of the sentence
            publishStateValues();
            hearRead = TCONST.FTR_USER_HEAR;
            // mParent.applyBehavior(TCONST.UTTERANCE_COMPLETE_EVENT);
            /*
            if (buttonState.equals(TCONST.RTC_PAGECOMPLETE)) {

                nextPage();
            } else if (buttonState.equals(TCONST.RTC_PARAGRAPHCOMPLETE)) {
                nextPara();
            } else if (buttonState.equals(TCONST.RTC_LINECOMPLETE)) {
                nextLine();
            }
             */
        }
    }

    private void resetNarration() {
        rawNarration = data[mCurrPage].text[mCurrPara][mCurrLine].narration;
        rawSentence  = data[mCurrPage].text[mCurrPara][mCurrLine].sentence;
        if (data[mCurrPage].prompt != null) page_prompt = data[mCurrPage].prompt;
    }

}
