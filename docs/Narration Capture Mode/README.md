# Documentation for Narration Capture Mode

Narration capture mode exists to create narrations of existing stories and store the relevant data for RoboTutor to use. To use narration capture mode, a storydata.json file should already be created for a given story, but the necessary narrations and audio file information should be missing. 

## Usage

Narration capture mode requires 3 files to be modified: debug.json, config.json, and storydata.json.

To see how to set it up, visit [this pull request](https://github.com/RoboTutorLLC/RoboTutor_2020/pull/48).

For more information on storydata.json and config.json, visit the [github authoring documentation](https://drive.google.com/drive/u/0/folders/0B7kzHmZs33scWjF3Qkw2MFo1Wm8?resourcekey=0-ZK-ICf9-mziZzT1Atavx0A). Information on debug.json can be found [here](https://docs.google.com/document/d/1qF4lGDrR7wzWOTY7lcwdhF6ttJegUvQxSEVUF-s8UoI/edit).

## Narration Audio Input

Audio input is captured by "intercepting" the audio data where it is fed to the recognizer and saving it into a temporary file. The handling of audio data is done by the [AudioWriter.java](../comp_listener/src/main/java/edu/cmu/xprize/listener/AudioWriter.java) class, using the method `addAudio()`. The SpeechRecognizer passes the audio buffer to this class, as well as recognizing it. 

From [SpeechRecognizer.java](../comp_listener/src/main/java/edu/cmu/xprize/listener/SpeechRecognizer.java):
```
decoder.processRaw(buffer, nread, false, false);
                            //Log.d("ASR", "Time in processRaw: " + (System.currentTimeMillis() - ASRTimer));

AudioWriter.addAudio(nread, buffer);

nSamples += nread;
```
### Storing the input 

The audio is written to a temporary file. The filepath is initialized via `AudioWriter.initializePath()` when each successive segment begins. This generally occurs in [CRt_ViewmanagerASB.java](../comp_reading/src/main/java/cmu/xprize/rt_component/CRt_ViewManagerASB.java) in the method `seekToStoryPosition()`. When seeking to the next point in the story, the new path is initialized. 

When the narration is stopped earlier than anticipated (see [Tiling](#tiling) for more information about why this happens) the narration temporary file is renamed and then converted to an mp3, as RoboTutor's playback mechanism can only work with mp3 files. This happens by calling `AudioWriter.pauseRecording()`, which adds a header to the raw audio, making it a playable wav file, and then converting the wav to an mp3. Additionally, the mp3 is renamed to adequately reflect the usable contents of the file. That is, the file is named exactly what the utterance in it contains with all lowercase characters and underscores for spaces. (A file containing the words "I row a boat" becomes the file "i_row_a_boat.mp3")

Finally, the process is restarted as a new path is initialized and more data is fed.

## Using the Tutor Logic and ASR Output

Narration capture mode builds on the current logic of the reading tutor. The user creating a narration has an experience very similar to that of a student using the platform to practice their reading skills.

Most tutor logic is handled in the class [CRt_ViewmanagerASB.java](../comp_reading/src/main/java/cmu/xprize/rt_component/CRt_ViewManagerASB.java), so this is where the narration capture mode logic exists as well. Additionally, some changes are made to the [animator graph](../app/src/main/assets/tutors/story_reading/animator_graph.json) for reading. 

*Note: The word "cursor" is used for the current point in the story that the tutor is listening for (and that the user is expected to say). It is the word that is being prompted and would be underlined during story reading.*

Every time the cursor moves to a point that is not the next word in the current sentence, a new path to write audio is generated through the method `feedSentence()`. It takes a single parameter - the word in the sentence to being listening from. The utterance can be "completed" in 2 ways - either the narrator says the wrong word, in which case the utterance is completed before the end of the sentence and the method `restartUtterance()` is called, or the narrator states the correct word and `endOfUtteranceCapture()` is called. 

When the narrator states an incorrect word, the method `restartUtterance()` is called, as the WRONG node in the animator graph shows. (this only occurs in narration capture mode). Here, an incorrect word is any word that is not the word after the current sentence, as well as not being the first word of the sentence. We allow the user to start at the beginning of the sentence at any time to allow for smoother narrations. This behavior can be seen in the method `onUpdate()` in CRt_ViewManagerASB.java, where the recognizer passes every updated hypothesis after multimatch output. 

`restartUtterance()` separates the recorded audio into an **utterance map**, where the different continuous subsequences of the sentence are separated from non. For example (where each subsequence in the utterance map is denoted by a new line):
&nbsp;&nbsp;&nbsp;&nbsp;*Sentence text: "Once upon a time a beautiful princess"*
&nbsp;&nbsp;&nbsp;&nbsp; *Narrator: " Once upon a... Once... upon... a time... Once upon a time a beautiful frog*
&nbsp;&nbsp;&nbsp;&nbsp; *Utterance map:*
&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp; *Once upon a*
&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp; *Once upon a time*
&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp; *Once upon a time a beautiful*
&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp; *frog*

Next, it determines the usable portion of the subsequences - A subsequence that does not end in a silence is not usable, as [tiling](#tiling) recordings using such subsequences would be prone to inaccuracy. It thus tries to find the longest subsequence possible that ends in a silence. If that means shortening one of the existing subsequences, that is fine. Finally, it moves the cursor to the location in the text analogous to the word after the accepted subsequence, for the narrator to begin narrating again. 

If the narrator reaches the end of the sentence, a similar process via `endOfUtteranceCapture()` is used to find the portion of the recording when the narrator narrated until the end of the sentence. 

*NOTE: a bug sometimes causes RoboTutor to act as if that the narrator has reached the end of the sentence even when they have not (according to the ASR output). This does not interfere with other parts of the program becuase they do not rely on ASR precision as much as narration capture mode does*

The segmentation data for the corresponding part of the sentence is fed to the `AudioDataStorage` class, which writes such data to a .seg file named identically to the mp3 file in the story folder and also edits the storydata.json file at runtime to add in the reent segmentation data (although this latter function does not happen at the same time, but rather later during narration tiling).

Notably, the recordings are not edited at all (no trimming), meaning that they contain all the mistakes that the narrator made as well as attempts to restart. For this reason, only part of the file is played back - the usable "phrase". Phrases begin at a "seam" - either the word after the previous phrase ended OR the beginning of the sentence. Additionally, a phrase begins and ends with silence.

## Tiling

Narration capture mode seeks to tile multiple recordings of partial narrations to form a full narration of the sentence. These recordings are referred to as "utterances". The word "phrase" refers to the usable portion of these utterances. 

The tiling algorithm, `constructAudioStoryData()` of CRt_ViewManagerASB.java tiles the utterances in the story folder. It uses the names of the utterances to infer their contents, and determines the resulting sequence of utterances to play back as a narration of words. It simply concatenates non-overlapping utterances to "cover" the whole sentence. Due to the difficulty of finding an optimal solution, narration capture mode uses a greedy tiling heuristic: It attempts to find a narration for words 1..n for the largest possible n. Next it attempts to find the longest possible narration from words n..m. If no utterance exists for any n..m where m >=n, it starts from scratch, after reducing the max possible n to n-1. It continues this pattern until it reaches the end of the sentence. 

Say you have the following utterances for the sentence "My father buys meat on Saturday.":
&nbsp;&nbsp;&nbsp;&nbsp; *my_father.mp3*
&nbsp;&nbsp;&nbsp;&nbsp; *buys_meat_on_saturday.mp3*
&nbsp;&nbsp;&nbsp;&nbsp; *my_father_buys.mp3*
&nbsp;&nbsp;&nbsp;&nbsp; *meat_on.mp3*
&nbsp;&nbsp;&nbsp;&nbsp; *saturday.mp3*
The narration tiling algorithm will tile the utterances so playback of the sentence would involve playing back the following utterances in order: *my_father_buys.mp3, meat_on.mp3, saturday.mp3.* This is not optimal but easiest to compute. In its current state, tiling does not allow for overlapping phrases.

The tiling mechanism occurs after the sentence has been completely narrated (the narrator reached the end of the sentence), and modifies the internal representation of the story by adding in the information about the recordings and the segmentation (gathered from the names of the recordings themselves and the .seg files associated with each recording). 

## Narration Behavior

Narration capture mode uses the reading tutor's "echo" mode to play back audio. Functionality is almost exactly the same, except that the playback has been modified to start at the beginning of the first word and end at the end of the last word, instead of playing the entire file back at once. This can be seen through the posted behaviors `TCONST.START_LATER` AND `TCONST.STOP_EARLY`. `START_LATER` instantaneously forwards the recording to a specified start time as soon as playback begins (see [`TRtComponent.startLate()`](../app/src/main/java/cmu/xprize/robotutor/tutorengine/widgets/core/TRtComponent.java)) while stop early sets a timer for the length of playback and instantly forwards to the end of the file once that timer goes off. The timer uses the existing `post` mechanism in CRt_Component.java.

Additionally, the highlighting of text has been modified during playback. The utterance being currently narrated has a <span style="background:cyan">cyan background</span> in addition to the default text effects.

## Issues

1. **ASR inaccuracy**
The ASR is extremely faulty and turns otherwise fluent readers into robotic, monotone machines. Becuase of false rejections and outright lack of speech detection at times, the ASR forces the narrator to pause too long between words or over-enunciate. This causes obvious issues with developing a good narration, as an ideal narration is smooth and reflects speaking in real life.

## Still to do

1. **UI Enhancement**
   Currently, narratin capture mode has a limited UI identical to the reading tutor. This means that it auto-advances at the end of sentences. 2 features should be implemented to give the narrator more control
   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; a. Better navigation (buttons are being implemented have not been robustly tested)
   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; b. Tap to play back existing narrations
   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; c. Start and stop narrations at will

2. **Testing**
   Tiling should be tested on canned speech. 