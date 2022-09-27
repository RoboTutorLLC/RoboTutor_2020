package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;

import cmu.xprize.robotutor.R;
import cmu.xprize.robotutor.RoboTutor;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 9/21/19.
 */

public class TStudentProfileModal extends RelativeLayout {

    Context _context;

    ImageView _profileImage;
    TextView _profileId;
    TextView _profileName;

    private boolean hasLoaded = false;


    public TStudentProfileModal(Context context) {
        super(context);
        init(context);
    }

    public TStudentProfileModal(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TStudentProfileModal(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this._context = context;

        inflate(getContext(), R.layout.student_profile, this);

        _profileImage = findViewById(R.id.SProfileImage);
        _profileId = findViewById(R.id.sId);
        _profileName = findViewById(R.id.sName);
    }

    // for when all the static things are ready
    public void loadInto() {
        if (hasLoaded) return;

        if (RoboTutor.STUDENT_INTERVENTION_PROFILE == null) return;

        _profileId.setText(RoboTutor.STUDENT_INTERVENTION_PROFILE.id);
        _profileName.setText(RoboTutor.STUDENT_INTERVENTION_PROFILE.photoFile);


        File imgFile = new File("/sdcard/intervention/" + RoboTutor.STUDENT_INTERVENTION_PROFILE.photoFile);

        // load image
        if (imgFile.exists()) {
            Bitmap myBit = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            _profileImage.setImageBitmap(myBit);
        }

        hasLoaded = true;
    }

}
