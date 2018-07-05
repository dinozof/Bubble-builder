package com.co.dinofox.bubblebuilder.fragments;


import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.Utils.UI.ToolbarActionItemTarget;
import com.co.dinofox.bubblebuilder.R;
import com.co.dinofox.bubblebuilder.activities.MainActivity;

import com.co.dinofox.bubblebuilder.singletons.MySingleton;
import com.co.dinofox.bubblebuilder.views.SimpleDrawingView;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.golshadi.orientationSensor.sensors.Orientation;
import com.golshadi.orientationSensor.utils.OrientationSensorInterface;

import java.util.Locale;


/**
 * Fragment di gestione della Bolla.
 *
 */
public class MainActivityFragment extends Fragment implements OrientationSensorInterface {



    private TextView pitch;
    private TextView roll;
    private TextView info;

    private Double rollOffset=0.0;
    private Double pitchOffset=0.0;
    private int calibrateButtonVisibility=0;

    private ImageButton calibrate;
    private ImageButton calibrated;
    private ImageButton rotate;
    private ImageButton rotated;

    private boolean toCalibrate;
    private boolean blocked=false;
    private boolean needTocalibrate;

    private Orientation orientationSensor;

    private SimpleDrawingView drawingView;
    private ToolbarActionItemTarget target;

    private CollapsingToolbarLayout collapsingToolbarLayout;

    private SharedPreferences prefs;
    private Toolbar toolbar;
    private boolean firstTime;
    private ShowcaseView showcaseView;
    private  boolean supported;
    private Double maxGrade=45.00;

    private static final String TAG = MainActivityFragment.class.getSimpleName();


    public MainActivityFragment() {


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);





    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

         prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        calibrate = (ImageButton) rootView.findViewById(R.id.calibrate_button);
        calibrated = (ImageButton) rootView.findViewById(R.id.calibrated);
        rotate = (ImageButton) rootView.findViewById(R.id.rotateButton);
        rotated = (ImageButton) rootView.findViewById(R.id.rotated);


        switchVisibility(calibrateButtonVisibility);


        ((MainActivity)getActivity()).lockAppBarClosed();
        toolbar= ((MainActivity) getActivity()).getToolbar();
        target = new ToolbarActionItemTarget(toolbar, R.id.action_new_project);
        ((MainActivity) getActivity()).getCollapsingToolbar().setTitle(getActivity().getApplicationInfo().name);


        info = (TextView) rootView.findViewById(R.id.calibrateTextView);

        pitch = (TextView) rootView.findViewById(R.id.pitch);
        roll = (TextView) rootView.findViewById(R.id.roll);
        drawingView = (SimpleDrawingView) rootView.findViewById(R.id.simpleDrawingView1);


        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blocked=!blocked;
                blockOrientation(getScreenOrientation(),getActivity(),blocked);
                rotate.setVisibility(View.GONE);
                rotated.setVisibility(View.VISIBLE);
            }
        });

        rotated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blocked=!blocked;
                blockOrientation(getScreenOrientation(),getActivity(),blocked);
                rotated.setVisibility(View.GONE);
                rotate.setVisibility(View.VISIBLE);
            }
        });



        calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.calibrationNeedStableSurface))
                        .setMessage(getString(R.string.calibrationNeedStableSurfaceMessage))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                toCalibrate = true;
                                ((MainActivity) getActivity()).showProgressDialog(getString(R.string.calibrating));
                                orientationSensor.calculateAccMagOrientation();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        calibrated.setVisibility(View.GONE);
                        calibrate.setVisibility(View.VISIBLE);
                    }
                }).show();
            }
        });

        if (getScreenOrientation() == Surface.ROTATION_90 || getScreenOrientation() == Surface.ROTATION_270) {
           roll.setVisibility(View.GONE);
        }else {
            roll.setVisibility(View.VISIBLE);
        }


        return rootView;

    }



    @Override
    public void onPause(){

        if(supported){
            orientationSensor.off();
        }
        if (showcaseView !=null)  showcaseView.hide();

        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();



        needTocalibrate = prefs.getBoolean("calibration_setting",true);
        switchVisibility(View.VISIBLE);

        Log.d(TAG,""+needTocalibrate);
        if(!needTocalibrate){

            switchVisibility(View.GONE);
            rollOffset = Double.parseDouble(prefs.getString(getString(R.string.calibrated_ROLL),"0.0"));
            pitchOffset = Double.parseDouble(prefs.getString(getString(R.string.calibrated_PITCH),"0.0"));

            Log.d(TAG,rollOffset +" "+ pitchOffset);

        }else{

            rollOffset = 0.0;
            pitchOffset = 0.0;

        }

        orientationSensor = new Orientation(this.getActivity(), this);


        // return true or false
        supported= orientationSensor.isSupport();

        if(!supported){
            MySingleton.getInstance().showMessage("Sorry we can't detect your Accelerometer",getActivity());
        }else{

            //------Turn Orientation sensor ON-------
            // set tolerance for any directions
            Double x_Tolerance = Double.parseDouble(prefs.getString("x_tolerance","0.5"));
            Double y_Tolerance = Double.parseDouble(prefs.getString("y_tolerance","0.5"));
            Double z_Tolerance = Double.parseDouble(prefs.getString("z_tolerance","1.0"));

            orientationSensor.init(x_Tolerance,y_Tolerance,z_Tolerance);


            // set output speed and turn initialized sensor on
            // 0 Normal
            // 1 UI
            // 2 GAME
            // 3 FASTEST
            int choosenValue = Integer.parseInt(prefs.getString("output_speed","3"));
            if (choosenValue > 3 ){
                choosenValue=3;
                prefs.edit().putString("output_speed","3").apply();
            }
            if(choosenValue < 0){
                choosenValue=0;
                prefs.edit().putString("output_speed","0").apply();
            }
            orientationSensor.on(choosenValue);
            ((MainActivity)getActivity()).getFab().hide();


            Log.d(TAG,x_Tolerance+" "+y_Tolerance+" "+z_Tolerance+" "+choosenValue);
        }


        firstTime=prefs.getBoolean("first_time_showcase",true);
        if(firstTime){

           if (showcaseView==null) {


               showcaseView = new ShowcaseView.Builder(getActivity()).withMaterialShowcase()
                       .setStyle(R.style.CustomShowcaseTheme)
                       .setTarget(target)
                       .setContentTitle(R.string.showcaseTitle)
                       .setContentText(R.string.showcaseDescription)
                       .setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               hideShowcase();
                           }
                       })
                       .build();
           } else{
               showcaseView.show();
           }
        }


    }

    private void hideShowcase(){
        showcaseView.hide();
    }

    @Override
    public void orientation(Double AZIMUTH, Double PITCH, Double ROLL) {
        String message=null;
        String message2=null;

        if(toCalibrate){
            toCalibrate=false;
            needTocalibrate=false;
            rollOffset=ROLL;
            pitchOffset =PITCH;
            calibrate.setVisibility(View.GONE);
            calibrated.setVisibility(View.VISIBLE);
            prefs.edit().putString(getString(R.string.calibrated_PITCH),pitchOffset.toString()).apply();
            prefs.edit().putString(getString(R.string.calibrated_ROLL),rollOffset.toString()).apply();
            prefs.edit().putBoolean("calibration_setting",needTocalibrate).apply();
            ((MainActivity) getActivity()).hideProgressDialog();
        }

        if(ROLL < maxGrade+0.5 && ROLL > -maxGrade+0.5){
            message2 =String.format(Locale.ITALIAN,"%.2f °", Math.abs((ROLL-rollOffset)));
            Double angle = ((ROLL-rollOffset)*Math.PI)/180;
            drawingView.setxMov(Math.sin(angle));
            //System.out.println("ROLL: "+Math.sin(angle));
        }

        message = String.format(Locale.ITALIAN,"%.2f °", Math.abs((PITCH- pitchOffset)));
        Double angle2 = ((PITCH- pitchOffset)*Math.PI)/180;
        drawingView.setyMov(Math.sin(angle2));
        //System.out.println("PITCH: "+Math.sin(angle));

        if ( ROLL>maxGrade+0.5 || ROLL < -maxGrade+0.5){
            message2 =maxGrade.toString();
        }

        pitch.setText(message);
        roll.setText(message2);

    }



    private void switchVisibility(int calibrateVisibility){
        if(calibrateVisibility==View.VISIBLE){
            calibrate.setVisibility(View.VISIBLE);
            calibrated.setVisibility(View.GONE);
        }
        else{
            calibrated.setVisibility(View.VISIBLE);
            calibrate.setVisibility(View.GONE);
        }

    }



    public int getScreenOrientation()
    {
        Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        return display.getRotation();
    }

    public void blockOrientation(int currentOrientation, Activity mActivity, boolean block){

        if(block){
            if (currentOrientation == Surface.ROTATION_90 || currentOrientation == Surface.ROTATION_270) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                drawingView.setMaxP(1.0);
                maxGrade=90.0;
                MySingleton.getInstance().showMessage(mActivity.getString(R.string.rotationLocked),mActivity);
            }
            else {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                drawingView.setMaxP(1.0);
                maxGrade=90.0;
                MySingleton.getInstance().showMessage(mActivity.getString(R.string.rotationLocked),mActivity);
            }

        }else{
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            drawingView.setMaxP(2/Math.sqrt(2));
            maxGrade=45.0;
            MySingleton.getInstance().showMessage(mActivity.getString(R.string.rotationEnabled),mActivity);
        }

    }
}
