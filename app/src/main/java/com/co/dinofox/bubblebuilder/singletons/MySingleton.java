package com.co.dinofox.bubblebuilder.singletons;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import com.co.dinofox.bubblebuilder.R;
import com.co.dinofox.bubblebuilder.activities.MainActivity;
import com.co.dinofox.bubblebuilder.fragments.NewProjectFragment;
import com.co.dinofox.bubblebuilder.projects_design.Project;
import com.co.dinofox.bubblebuilder.projects_design.ProjectContainer;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import java.util.ArrayList;

/**
 * Singleton per gestire i valori statici. Contiene i riferimenti al ProjectContainer,
 * e un array contenete i fragments di ogni progetto.
 */
public class MySingleton
{
    private static MySingleton instance;
    private static ArrayList<NewProjectFragment> projectFragmentsContainer;
    private String jsonProjects;
    private String driveID;
    private AsyncTask asyncTask;


    public static void initInstance()
    {
        if (instance == null)
        {
            // Create the instance
            instance = new MySingleton();

        }
    }

    public static MySingleton getInstance()
    {
        // Return the instance
        return instance;
    }

    private MySingleton()
    {

        projectFragmentsContainer = new ArrayList<NewProjectFragment>();

    }



    public static ArrayList<NewProjectFragment> getProjectFragmentsContainer() {
        return projectFragmentsContainer;
    }


    //passo l'ID del fragent e ne ottengo il rispettivo progetto
    public static Project getfragmentProjectByID(int id){
        for (int i=0; i< projectFragmentsContainer.size(); i++){
            NewProjectFragment possibleFragment = projectFragmentsContainer.get(i);
            if (possibleFragment.getIdentifier()==id){
                return possibleFragment.getProject();
            }
        }
        return null;
    }

    public static NewProjectFragment getfragmentByID(int id){
        for (int i=0; i< projectFragmentsContainer.size(); i++){
            NewProjectFragment possibleFragment = projectFragmentsContainer.get(i);
            if (possibleFragment.getIdentifier()==id){
                return possibleFragment;
            }
        }
        return null;
    }


    public static boolean removeFragmentByID(int id){
        for (int i=0; i< projectFragmentsContainer.size(); i++){
            NewProjectFragment possibleFragment= projectFragmentsContainer.get(i);
            if (possibleFragment.getIdentifier()==id){
                projectFragmentsContainer.remove(i);
                return true;
            }
        }
        return false;
    }




    public  void showMessage(String message, Context context){
        Toast.makeText(context,message,
                Toast.LENGTH_SHORT).show();
    }

    public  void showSimpleSnack(CoordinatorLayout coordinator, String myText){
        Snackbar.make(coordinator, myText, Snackbar.LENGTH_LONG)
                .show(); // Don’t forget to show!
    }


    public String getJsonProjects() {
        return jsonProjects;
    }

    public void setJsonProjects(String jsonProjects) {
        this.jsonProjects = jsonProjects;
    }

    public String getDriveID() {
        return driveID;
    }

    public void setDriveID(String driveID) {
        this.driveID = driveID;
    }

    public AsyncTask getAsyncTask() {
        return asyncTask;
    }

    public void setAsyncTask(AsyncTask asyncTask) {
        this.asyncTask = asyncTask;
    }
}
