package com.co.dinofox.bubblebuilder.views;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import com.co.dinofox.bubblebuilder.R;
import com.co.dinofox.bubblebuilder.activities.MainActivity;
import com.co.dinofox.bubblebuilder.fragments.MainActivityFragment;
import com.co.dinofox.bubblebuilder.singletons.MySingleton;

import java.nio.charset.MalformedInputException;

/**
 * Created by stefano on 09/03/16.
 */
public class SimpleDrawingView extends View {

    // setup initial color
    private final int paintColor = getResources().getColor(R.color.bubble_background_border);
    // defines paint and canvas
    private Paint drawPaint;
    private Paint background;
    private ColorDrawable viewColor;


    int framesPerSecond;
    private SharedPreferences prefs;



    private Double xMov=0.0;
    private Double yMov=0.0;
    private float oldXComp=0;
    private float oldYComp=0;

    private Double maxP = 2/Math.sqrt(2);
    private float bubbleLandscapeMargin = 50;
    private float bubbleLandscapeBorderLeftMargin =45;
    private float bubbleLandscapeBorderRightMargin =55;
    private float bubbleportraitBorderLeftMargin =5;
    private float bubbleLandscapeRadius;
    private float bubblePortraitRadius;
    private float smallBubbleRadius;


    private static final String TAG = SimpleDrawingView.class.getSimpleName();




    public SimpleDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        setFocusableInTouchMode(true);
        setupPaint();



    }

    // Setup paint with color and stroke styles
    private void setupPaint() {

        viewColor = (ColorDrawable) this.getBackground();
        drawPaint = new Paint();


        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(10);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        background = new Paint();

        background.setAntiAlias(true);
        background.setStyle(Paint.Style.FILL);


    }

    @Override
    protected void onDraw(Canvas canvas) {


        framesPerSecond = Integer.parseInt(prefs.getString("FPS_bubble","120"));
        if (framesPerSecond < 30){
            framesPerSecond=30;
            prefs.edit().putString("FPS_bubble","30").apply();
        }
        if(framesPerSecond > 999){
            framesPerSecond=999;
            prefs.edit().putString("FPS_bubble","999").apply();
        }


        if(getScreenOrientation()== Surface.ROTATION_0 || getScreenOrientation()==Surface.ROTATION_180) {
            bubblePortraitRadius = (getWidth()/2)-50;
            smallBubbleRadius=bubblePortraitRadius /8;

            Float xComp = (((bubblePortraitRadius - smallBubbleRadius) * maxP.floatValue() * xMov.floatValue()));
            Float yComp = (((bubblePortraitRadius - smallBubbleRadius) * maxP.floatValue() * yMov.floatValue()));
            Double angle = Math.atan2(yComp, xComp);
            //controllo che i valori registrati di x e y non escano dalla sfera -> teorema di pitagora X e Y al quadrato sommati devono sempre stare sotto il raggio
            Double vector = Math.sqrt(((xComp.doubleValue() * xComp.doubleValue()) + (yComp.doubleValue() * yComp.doubleValue())));
            if ((bubblePortraitRadius - smallBubbleRadius) < (vector.floatValue())) {

                oldXComp = Double.valueOf(((bubblePortraitRadius - smallBubbleRadius) * Math.cos(angle))).floatValue();
                oldYComp = Double.valueOf(((bubblePortraitRadius - smallBubbleRadius) * Math.sin(angle))).floatValue();
            } else {
                oldXComp = xComp;
                oldYComp = yComp;
            }

            background.setColor(getResources().getColor(R.color.bubble_background));
            // disegno il bacground della bolla
            canvas.drawCircle(getWidth() / 2, (3 * getHeight() / 7), bubblePortraitRadius, background);
            // disegno il bordo del background della bolla
            drawPaint.setColor(paintColor);
            canvas.drawCircle(getWidth() / 2, (3 * getHeight() / 7), bubblePortraitRadius +3, drawPaint);
            background.setColor(getResources().getColor(R.color.bubble_ball));
            // disegno la bolla
            canvas.drawCircle((getWidth() / 2) - oldXComp,(3 * getHeight() / 7) +oldYComp, smallBubbleRadius, background);
            background.setColor(getResources().getColor(R.color.bubble_placeholders));
            // disegno i margini di calibrazione a croce
            canvas.drawRect((getWidth() / 2) - bubbleportraitBorderLeftMargin, (3 * getHeight() / 7) - smallBubbleRadius, (getWidth() / 2) + bubbleportraitBorderLeftMargin, (3 * getHeight() / 7) + smallBubbleRadius, background);
            canvas.drawRect((getWidth() / 2) - smallBubbleRadius, (3 * getHeight() / 7) - bubbleportraitBorderLeftMargin, (getWidth() / 2) + smallBubbleRadius, (3 * getHeight() / 7) + bubbleportraitBorderLeftMargin, background);
        }
        else if (getScreenOrientation()== Surface.ROTATION_90){
            bubbleLandscapeRadius = getHeight() / 6;
            // ottengo il valore dello spostamento lungo Y compreso fra -45 e 45
            Float xComp = ((getWidth()/2 - bubbleLandscapeBorderRightMargin - bubbleLandscapeRadius)* maxP.floatValue() *yMov.floatValue()); // in realtà sto lavorando con i valori di y
            Float maxExtension = getWidth()/2 - bubbleLandscapeMargin - bubbleLandscapeRadius;

            if(xComp > maxExtension){
                xComp=maxExtension;
            }
            if( xComp < -maxExtension){
                xComp=-maxExtension;
            }

            background.setColor(getResources().getColor(R.color.bubble_background_landscape));
            // disegno il background della bolla
            drawPaint.setColor(paintColor);
            canvas.drawRect(getWidth()/2 -(getWidth()/2) + bubbleLandscapeMargin,getHeight()/2 - (getHeight()/4),getWidth()/2 + (getWidth()/2) - bubbleLandscapeMargin,getHeight()/2 + (getHeight()/4), background);
            background.setColor(viewColor.getColor());
            // disegno la bolla
            canvas.drawCircle((getWidth() / 2)+ xComp,getHeight()/2 - (getHeight()/4), bubbleLandscapeRadius, background);
            background.setColor(getResources().getColor(R.color.bubble_placeholders));
            // disegno il bordo destro
            canvas.drawRect(getWidth()/2 -(getWidth()/2) + bubbleLandscapeBorderLeftMargin,getHeight()/2 - (getHeight()/4), getWidth()/2 - (getWidth()/2) + bubbleLandscapeBorderRightMargin, getHeight()/2 + (getHeight()/4), background);
            // disegno il bordo sinistro
            canvas.drawRect(getWidth()/2 +(getWidth()/2) - bubbleLandscapeBorderRightMargin,getHeight()/2 - (getHeight()/4), getWidth()/2 + (getWidth()/2) - bubbleLandscapeBorderLeftMargin, getHeight()/2 + (getHeight()/4), background);
            // disegno il margine di calibrazione destro
            canvas.drawRect(getWidth()/2 - bubbleLandscapeRadius - 5,getHeight()/2 - (getHeight()/4), getWidth()/2 - bubbleLandscapeRadius, getHeight()/2 + (getHeight()/4), background);
            // disegno il margine di calibrazione sinistro
            canvas.drawRect(getWidth()/2 + bubbleLandscapeRadius,getHeight()/2 - (getHeight()/4), getWidth()/2 + bubbleLandscapeRadius +5, getHeight()/2 + (getHeight()/4), background);

        }
        else {

            bubbleLandscapeRadius = getHeight() / 6;
            // ottengo il valore dello spostamento lungo Y compreso fra -45 e 45
            Float xComp = ((getWidth()/2 - bubbleLandscapeBorderRightMargin - bubbleLandscapeRadius)* maxP.floatValue() *yMov.floatValue()); // in realtà sto lavorando con i valori di y
            Float maxExtension = getWidth()/2 - bubbleLandscapeMargin - bubbleLandscapeRadius;

            if(xComp > maxExtension){
                xComp=maxExtension;
            }
            if( xComp < -maxExtension){
                xComp=-maxExtension;
            }

            background.setColor(getResources().getColor(R.color.bubble_background_landscape));
            // disegno il background della bolla
            drawPaint.setColor(paintColor);
            canvas.drawRect(getWidth()/2 -(getWidth()/2) + bubbleLandscapeMargin,getHeight()/2 - (getHeight()/4),getWidth()/2 + (getWidth()/2) - bubbleLandscapeMargin,getHeight()/2 + (getHeight()/4), background);
            background.setColor(viewColor.getColor());
            // disegno la bolla
            canvas.drawCircle((getWidth() / 2)- xComp,getHeight()/2 - (getHeight()/4), bubbleLandscapeRadius, background);
            background.setColor(getResources().getColor(R.color.bubble_placeholders));
            // disegno il bordo destro
            canvas.drawRect(getWidth()/2 -(getWidth()/2) + bubbleLandscapeBorderLeftMargin,getHeight()/2 - (getHeight()/4), getWidth()/2 - (getWidth()/2) + bubbleLandscapeBorderRightMargin, getHeight()/2 + (getHeight()/4), background);
            // disegno il bordo sinistro
            canvas.drawRect(getWidth()/2 +(getWidth()/2) - bubbleLandscapeBorderRightMargin,getHeight()/2 - (getHeight()/4), getWidth()/2 + (getWidth()/2) - bubbleLandscapeBorderLeftMargin, getHeight()/2 + (getHeight()/4), background);
            // disegno il margine di calibrazione destro
            canvas.drawRect(getWidth()/2 - bubbleLandscapeRadius - 5,getHeight()/2 - (getHeight()/4), getWidth()/2 - bubbleLandscapeRadius, getHeight()/2 + (getHeight()/4), background);
            // disegno il margine di calibrazione sinistro
            canvas.drawRect(getWidth()/2 + bubbleLandscapeRadius,getHeight()/2 - (getHeight()/4), getWidth()/2 + bubbleLandscapeRadius +5, getHeight()/2 + (getHeight()/4), background);



        }
        this.postInvalidateDelayed(1000 / framesPerSecond);

    }

    public void setxMov(Double xMov) {
        this.xMov = xMov;
    }


    public void setyMov(Double yMov) {
        this.yMov = yMov;
    }


    public int getScreenOrientation()
    {
            Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

            return display.getRotation();

    }

    public void setMaxP(Double maxP) {
        this.maxP = maxP;
    }

}
