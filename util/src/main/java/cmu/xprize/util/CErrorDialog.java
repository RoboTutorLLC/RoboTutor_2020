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

package cmu.xprize.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import cmu.xprize.common.R;

import static cmu.xprize.util.TCONST.QGRAPH_MSG;

public class CErrorDialog implements View.OnClickListener {

    private final Dialog   dialog;
    private final TextView errMsg;

    public CErrorDialog(Context context) {

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.error_layout);
        dialog.setCancelable(false);

        errMsg = (TextView)dialog.findViewById(R.id.SerrorMessage);

    }

    public void show(String msg) {

        errMsg.setText(msg);
        dialog.show();
    }

    public void hide() {
        dialog.dismiss();
    }

    public Boolean isShowing() {
        return dialog.isShowing();
    }


    @Override
    public void onClick(View v) {
        Log.v(QGRAPH_MSG, "event.click: " + " CErrorDialog:exit");

        System.exit(1);
    }
}
