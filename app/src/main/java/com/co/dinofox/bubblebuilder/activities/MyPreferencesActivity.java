package com.co.dinofox.bubblebuilder.activities;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.co.dinofox.bubblebuilder.services.MyIntentService;
import com.co.dinofox.bubblebuilder.fragments.MyPreferenceFragment;
import com.co.dinofox.bubblebuilder.R;
import com.co.dinofox.bubblebuilder.singletons.MySingleton;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;


public class MyPreferencesActivity extends AppCompatActivity  implements
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {




    private static GoogleApiClient mGoogleApiClient;

    private static GoogleSignInResult result;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private SignInButton signInButton;
    private Button signOutButton;
    private Toolbar toolbar;
    private CoordinatorLayout mCoordinator;
    private MyPreferenceFragment preferenceFragment;
    private SharedPreferences prefs;
    private MyPreferencesActivity myPreferencesActivity;
    private ProgressDialog mProgressDialog=null;
    private OptionalPendingResult<GoogleSignInResult> opr;
    final static int MY_PERMISSIONS_REQUEST_INTERNET=278;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.preference_activity);

        preferenceFragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(R.id.container,preferenceFragment ).commit();

        toolbar = (Toolbar) findViewById(R.id.setting_tollbar);
        mCoordinator = (CoordinatorLayout) findViewById(R.id.preferenceCoordinator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        myPreferencesActivity = MyPreferencesActivity.this;

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.

        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Drive.API)
                .addConnectionCallbacks(this)
                .addApi(AppIndex.API).build();

        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
        signOutButton = (Button) findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    //avverto l'utente che ho fallito la connessione

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        MySingleton.getInstance().showSimpleSnack(mCoordinator,"Connection Failed");
    }

    private void signIn() {
        showProgressDialog(MyPreferencesActivity.this,"Loggin in");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);



    }

    //gestione avvenuto login
    //lancio anche il sync con drive in Asyncrono
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        // mi sono loggato, cambio i pulsanti e gestisco il token
        if (requestCode == RC_SIGN_IN) {
            new SyncWithDriveTask().execute(mGoogleApiClient);
            result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);

        }else{
            hideProgressDialog();
            MySingleton.getInstance().showSimpleSnack(mCoordinator,"Connection Failed");
        }
    }


    //gestione token login
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());

        if (result.isSuccess()) {
            boolean syncWithDriveSucceded = prefs.getBoolean(getString(R.string.drive_correctly_synchronised),true);
            if(syncWithDriveSucceded){
                preferenceFragment.enableSaveOnDrive();
            }

            MySingleton.getInstance().showSimpleSnack(mCoordinator,getString(R.string.signedInAS)+result.getSignInAccount().getEmail());
            //rendo possibile selezionare l'opzione save on Drive


        }
    }

    //procedura di disconnessione
    private void signOut() {
        if (mGoogleApiClient.isConnected()){
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]

                        signOutButton.setVisibility(View.GONE);
                        signInButton.setVisibility(View.VISIBLE);
                        prefs.edit().putBoolean(getString(R.string.save_on_drive_preference),false).apply();
                        preferenceFragment.disableSaveOnDrive();

                        prefs.edit().putBoolean(getString(R.string.sync_with_drive_programmed),false).apply();
                        MySingleton.getInstance().showSimpleSnack(mCoordinator,"Signed out successfully");
                        prefs.edit().putString(getString(R.string.priority_saving),"0").apply();
                        // [END_EXCLUDE]
                    }
                });
        }else{
            signOutButton.setVisibility(View.GONE);
            signInButton.setVisibility(View.VISIBLE);
            prefs.edit().putBoolean(getString(R.string.save_on_drive_preference),false).apply();
            prefs.edit().putString(getString(R.string.priority_saving),"0").apply();
            prefs.edit().putBoolean(getString(R.string.sync_with_drive_programmed),false).apply();
            preferenceFragment.disableSaveOnDrive();
            MySingleton.getInstance().showSimpleSnack(mCoordinator,"Signed out successfully");
        }
    }

    //provo a fare il silent login
    @Override
    public void onStart() {
        super.onStart();


        //mGoogleApiClient.connect();
         opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
            handleSignInResult(result);
        } else {
            Log.d(TAG,"no login");
            MySingleton.getInstance().showSimpleSnack(mCoordinator,"Log in To Unlock more features");

        }
    }


    public  void showProgressDialog(Context context, String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            Log.d("progressDialog","Showing");
        }
        mProgressDialog.show();
    }

    public  void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
            Log.d("progressDialog","hidden");
            mProgressDialog.dismiss();
        }

    }


    @Override
    public void onConnected(Bundle bundle) {
       if(mGoogleApiClient.isConnected()){
            Log.d(TAG,"Connected!");
            boolean syncWithDriveSucceded = prefs.getBoolean(getString(R.string.drive_correctly_synchronised),true);
            boolean syncWithDriveProgrammed = prefs.getBoolean(getString(R.string.sync_with_drive_programmed),true);
            if(!syncWithDriveSucceded && !syncWithDriveProgrammed){
                Log.d(TAG,"Trying again to sync");
                new SyncWithDriveTask().execute(mGoogleApiClient);
            }
       }
    }



    @Override
    public void onConnectionSuspended(int i) {

        Log.d(TAG, "onConnectionFailed:" + i);
        MySingleton.getInstance().showMessage(getString(R.string.connectionInterruptedMessage),this);

    }

    /**
     * classe per la gestione della sincronizzazione in asincrono con Drive.
     * controlla che sia connesso e sincronizzato con il cloud.
     * se così non fosse, schedula un nuovo sinc dopo un minuto.
     */

    private  class SyncWithDriveTask extends AsyncTask<Object, Integer, String>{

        private com.google.android.gms.common.api.Status stats;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(mProgressDialog!=null) mProgressDialog.setMessage("Sync with Drive...");
            else showProgressDialog(getApplicationContext(),"Sync with Drive...");

            if (ContextCompat.checkSelfPermission(MyPreferencesActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
            {
                MySingleton.getInstance().showMessage(getString(R.string.needPermissionText),MyPreferencesActivity.this);
                ActivityCompat.requestPermissions(MyPreferencesActivity.this,
                        new String[]{Manifest.permission.INTERNET},
                        MY_PERMISSIONS_REQUEST_INTERNET);
            }
        }

        @Override
        protected String doInBackground(Object... params) {

             GoogleApiClient _myGoogleApiClient = (GoogleApiClient) params[0];
             _myGoogleApiClient.connect();


            if(_myGoogleApiClient.isConnected() &&
                    ContextCompat.checkSelfPermission(MyPreferencesActivity.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)
            {
                Log.d(TAG,"era connesso!");
                stats = Drive.DriveApi.requestSync(_myGoogleApiClient).await();
            }
            return null;
        }

        protected void onPostExecute(String result) {

            hideProgressDialog();
            if(stats!=null && stats.isSuccess()){
                Log.d(TAG, "Sync succeded");
                prefs.edit().putBoolean(getString(R.string.drive_correctly_synchronised),true).apply();
                prefs.edit().putBoolean(getString(R.string.sync_with_drive_programmed),false).apply();
                prefs.edit().putBoolean(getString(R.string.backup_restore_from_Drive),true).apply();
                MySingleton.getInstance().showMessage(getString(R.string.sync_succed), getApplicationContext());
                preferenceFragment.enableSaveOnDrive();
            }
            else{
                Log.d(TAG, "Sync failed");
                MySingleton.getInstance().showMessage(getString(R.string.sync_failed), getApplicationContext());
                prefs.edit().putBoolean(getString(R.string.drive_correctly_synchronised),false).apply();
                prefs.edit().putBoolean(getString(R.string.sync_with_drive_programmed),true).apply();
                prefs.edit().putBoolean(getString(R.string.save_on_drive_preference),false).apply();
                Intent intent = new Intent(getApplicationContext(),MyIntentService.class);
                getApplicationContext().startService(intent);
                preferenceFragment.disableSaveOnDrive();
                prefs.edit().putString(getString(R.string.priority_saving),"0").apply();

            }

        }


    }



}

