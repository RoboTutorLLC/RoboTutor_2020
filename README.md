[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# **RoboTutor**


Welcome to RoboTutor_2020:  XPRIZE's repo has the version of RoboTutor uploaded on 11/20/2018, but RoboTutor has been updated here since then.  **This is the newest version.**

For changes prior to 3/16/2020, see [https://github.com/RoboTutorLLC/RoboTutor_2019](https://github.com/RoboTutorLLC/RoboTutor_2019).  However, it's no longer the newest.

--- 
## Quick Installation
To quickly install the most recent version of RoboTutor without having to download the full source code, follow these steps:

1. Go to [this Google Drive folder](https://drive.google.com/drive/u/1/folders/1VyajTK_SShmBB4GXJ74737pBS_IKunL_)(updated 8/26/2020).

2. Download the APK to your tablet (do not install yet).

3. Download *config.json* and place it in the *Download* directory of your tablet.

4. Download the ZIP files for the version you would like to try (Swahili, English, or both), and place them in the *Download* directory of your tablet.

5. Install the RoboTutor APK on your tablet, and launch.

6. Upon launch, RoboTutor will unzip the ZIP assets.

---

## Contribution and Developer Practices

- Please read our best practices and the community guidelines in our [Dev Docs](https://github.com/RoboTutorLLC/RoboTutor_2020/blob/master/DEVDOCS.md).

## **Setup and Configuration:**

[Install Android Studio](http://developer.android.com/sdk/index.html)<br>

[Install GitHub Desktop](https://desktop.github.com/)<br>

RoboTutor uses a large volume of external assets at runtime.  To successfully run RoboTutor you must first install these assets on your target device: [English](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-EnglishAssets). [Swahili](https://github.com/XPRIZE/GLEXP-Team-RoboTutor-CodeDrop2-Assets). Once you have cloned and run the associated tools to push the data assets to your device you can proceed with building RoboTutor.



## **Building RoboTutor:**

1. Clone RoboTutor to your computer using Git/GitHub

2. Run the setup script `./setup.sh`

3. **Import** the RoboTutor project into Android Studio.

4. You may need to install different versions of the build tools and android SDKs.

5. There are a number of build variants you can select to generate versions that support static language selections and also vesions that permit dynamic language selection at runtime. In order to generate any flavor that depends on the key signature, you must generate your own keystore (see next steps). Note that the version used in the XPrize code drop 1 submission usees flavor *release_sw*, which depends on a signed APK.


6. If you do not already have one, follow the steps for Android Studio (https://stackoverflow.com/a/30254012) (for
(https://stackoverflow.com/questions/3997748/how-can-i-create-a-keystore)) to generate a keystore.
Sample command:

```
keytool -genkeypair -dname "cn=Mark Jones, ou=JavaSoft, o=Sun, c=US" -alias business -keypass kpi135 -keystore /working/android.keystore -storepass ab987c -validity 20000
```

- *dname* is a unique identifier for the application in the .keystore

    - *cn* the full name of the person or organization that generates the .keystore
    - *ou* Organizational Unit that creates the project, its a subdivision of the Organization that creates it. Ex. android.google.com
    - *o* Organization owner of the whole project. Its a higher scope than ou. Ex.: google.com
    - *c* The country short code. Ex: For United States is "US"

- *alias* Identifier of the app as an single entity inside the .keystore (it can have many)
- *keypass* Password for protecting that specific alias.
- *keystore* Path where the .keystore file shall be created (the standard extension is actually .ks)
- storepass Password for protecting the whole .keystore content.
- *validity* Amount of days the app will be valid with this .keystore

6. Add a file named "keystore.properties" to your root project directory, and give it the following contents. The values should be based on the values you used to generate the keystore.
```
storePassword=<your_store_password>
keyPassword=<your_key_password>
keyAlias=<your_key_alias>
storeFile=<path_to_location_of_keystore>
```

7. Use Android Studio or gradlew to generate a signed APK with the flavor *release_sw*. This will generate the file *robotutor.release_sw.1.8.8.1.apk*. This APK should be transferred to the apk in your local SystemBuild directory.

[Installation Standalone](./INSTALL-STANDALONE.md)

You can refer to the above Guide if you are still facing any errors.

## **Common Errors**

1. If you are getting an error `config.json not found`:
    - With your device/simulator running, write the command `adb push robot-tutor/RoboTutor_2020/app/src/sample_config_files/release_en.json /sdcard/Download/config.json`
2. If you are getting an error `debug.json not found`:
    - With your device/simulator running, write the command `adb push robot-tutor/RoboTutor_2020/app/src/sample_config_files/debug/config.json /sdcard/Download/debug.json`


---

## **XPrize Submission:**

The following repositories are part of the Team-RoboTutor entry:
 * XPRIZE/GLEXP-Team-RoboTutor-RoboTutor
 * XPRIZE/GLEXP-Team-RoboTutor-SystemBuild
 * XPRIZE/GLEXP-Team-RoboTutor-RTAsset_Publisher
 * XPRIZE/GLEXP-Team-RoboTutor-CodeDrop1-Assets
 * XPRIZE/GLEXP-Team-RoboTutor-RoboLauncher 
 * XPRIZE/GLEXP-Team-RoboTutor-RoboTransfer 


<br>
<br>
