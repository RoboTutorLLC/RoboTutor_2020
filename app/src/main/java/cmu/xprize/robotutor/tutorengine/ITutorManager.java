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

package cmu.xprize.robotutor.tutorengine;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
/*
This is an interface to provide the base for the Master Animation Layout and other Layout Classes.
This is used to ensure that the classes dealing with subviews have addView/removeView/addAndShow
methods implemented

 */
public interface ITutorManager extends ITutorSceneImpl  {

    public void setOnTouchListener(View.OnTouchListener l);

    public void addView(ITutorSceneImpl newView);
    public void addView(View newView);

    public void addAndShow(ITutorSceneImpl newView);
    public void addAndShow(View newView);

    public void removeView(ITutorSceneImpl oldView);
    public void removeView(View oldView);

    public void setAnimationListener(Animation.AnimationListener callback);

}
