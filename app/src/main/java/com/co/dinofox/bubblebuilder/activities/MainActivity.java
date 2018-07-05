package com.co.dinofox.bubblebuilder.activities;


import android.Manifest;
import android.app.ProgressDialog;
import android.app.backup.BackupAgent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.Utils.UI.FabBuilder;
import com.co.dinofox.bubblebuilder.fragments.MainActivityFragment;
import com.co.dinofox.bubblebuilder.fragments.NewProjectFragment;
import com.co.dinofox.bubblebuilder.R;
import com.co.dinofox.bubblebuilder.projects_design.Project;
import com.co.dinofox.bubblebuilder.projects_design.ProjectContainer;
import com.co.dinofox.bubblebuilder.singletons.MyBackupAgent;
import com.co.dinofox.bubblebuilder.singletons.MySingleton;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.ExecutionOptions;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;
import com.google.gson.Gson;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;

import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import hugo.weaving.DebugLog;


public class MainActivity extends AppCompatActivity  implements
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{

    final MainActivityFragment mainfragment = new MainActivityFragment();
    protected static int IDENTIFIER=0;
    protected Drawer projectsDrawer;
    protected PrimaryDrawerItem goToSetting;
    protected PrimaryDrawerItem goToInfo;
    protected PrimaryDrawerItem homefragment;
    protected AccountHeader accountHeader;
    protected ProjectContainer mcontainer;
    protected Project tmpProject;
    protected int tmpPosition;
    protected int tmpDrawerItemPos;
    protected NewProjectFragment tmpFragment;
    private FloatingActionButton fab;
    protected NewProjectFragment newProjectFragment;
    protected Toolbar toolbar;
    private boolean newUser=true;
    protected CollapsingToolbarLayout collapsingToolbar;
    protected AppBarLayout mAppBarLayout;
    protected CoordinatorLayout coordinator;
    private TextView costView;
    private ImageButton modifyTitle;
    private ImageView toolbarHeader;
    private static GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount myaccount;
    private InputStream myRestoredDataFromDrive;
    private SharedPreferences prefs;
    private boolean loadFromDrive =false;
    private Context context;
    private static String driveID;
    private boolean firstTime;
    private ProgressDialog mProgressDialog;
    private ProfileDrawerItem baseitem;
    private boolean backPressedCount=false;






    private MenuItem item;

    private static GoogleSignInResult result;

    private static final String TAG = "Main Activity";


    @Override
    @DebugLog
    public void onBackPressed(){
        if(backPressedCount){
            super.onBackPressed();
            finish();
        }

            MySingleton.getInstance().showSimpleSnack(coordinator,"Press back again to exit");


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                backPressedCount=!backPressedCount;
            }
        }, Snackbar.LENGTH_LONG);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        NewProjectFragment id= getActiveFragment();
        if (id==null) {
            outState.putInt("IDENTIFIER", 0);
        }
        else{
            outState.putInt("IDENTIFIER", id.getIdentifier());
        }


    }


    /**
     * provo il silent login a Google...
     */
    @Override
    public void onStart(){
        super.onStart();


        firstTime=prefs.getBoolean("first_time_toread",true);

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            result = opr.get();

            myaccount=result.getSignInAccount();



        } else {
           Log.d(TAG,"nessuna informazione sull'utente\n");
            myaccount=null;
        }
        accountHeader.removeProfile(0);
        baseitem = new ProfileDrawerItem().withIcon(R.mipmap.ic_launcher).withName(getString(R.string.app_name));

        accountHeader.addProfiles(baseitem);
        accountHeader.setActiveProfile(baseitem);
        newUser=true;

    }

    /**
     * aggiorna sistematicamente l'header del Drawer inserendo le informazioni dell'utente loggato
     * o lasciandolo vuoto nel caso non ci siano utenti loggati
     */

    public void updateaccountHeader(){



        if (  newUser && myaccount!=null){
            accountHeader.removeProfile(baseitem);
            newUser=false;
            ProfileDrawerItem profile;
            try{
                profile = new ProfileDrawerItem().withName(myaccount.getDisplayName())
                        .withEmail(myaccount.getEmail())
                        .withIcon(myaccount.getPhotoUrl());

                // Log.d(TAG,"URL: "+myaccount.getPhotoUrl().toString());

            }catch (java.lang.NullPointerException k){
                profile = new ProfileDrawerItem().withName(myaccount.getDisplayName())
                        .withEmail(myaccount.getEmail());
            }
            accountHeader.addProfiles(profile);
            accountHeader.setActiveProfile(profile);
        }
    }


    /**
     * Salvo su file il Json contenete la classe ProjectContainer.
     * e nel caso carico su Drive il salvataggio
     * notifico il backupManager di salvare le shared preferences
     */


    @Override
    @DebugLog
    public void onPause(){

        super.onPause();
        Gson gson = new Gson();
        if(loadFromDrive){
            new OnlineSaveFilesTask().executeOnExecutor(OnlineSaveFilesTask.THREAD_POOL_EXECUTOR,mGoogleApiClient,gson.toJson(mcontainer));
        }
        new LocalSaveFilesTask().executeOnExecutor(LocalSaveFilesTask.THREAD_POOL_EXECUTOR,"");


    }


    @Override
    @DebugLog
    public void onResume(){
        super.onResume();
        updateaccountHeader();


    }


    /**
     * setup iniziale dell'Activity.
     * @param savedInstanceState
     */

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (savedInstanceState != null) {
            IDENTIFIER=savedInstanceState.getInt("IDENTIFIER",0);
        }
        else getSupportFragmentManager().beginTransaction().replace(R.id.container, mainfragment, "0").commit();


        mcontainer=new ProjectContainer();
        if(MySingleton.getInstance().getDriveID()!=null){
            driveID=MySingleton.getInstance().getDriveID();
        }
        context = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getApplicationInfo().name);

        //SERVONO NEI FRAGMENT PER AGGIORNARE LE INFO DELLA TOOLBAR
        modifyTitle = (ImageButton) findViewById(R.id.modifyTitleButton);
        costView = (TextView) findViewById(R.id.totalCostView);
        toolbarHeader= (ImageView) findViewById(R.id.header);


        mAppBarLayout= (AppBarLayout) findViewById(R.id.appbar);
        coordinator = (CoordinatorLayout) findViewById(R.id.coordinator);

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);


        prefs = PreferenceManager.getDefaultSharedPreferences(this);


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER)) //
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




        goToSetting= new PrimaryDrawerItem()
                .withName(getString(R.string.drawer_item_setting)).withIcon(GoogleMaterial.Icon.gmd_settings);
        goToInfo= new PrimaryDrawerItem()
                .withName(getString(R.string.drawer_item_info)).withIcon(GoogleMaterial.Icon.gmd_info);
        homefragment= new PrimaryDrawerItem()
                .withName(getString(R.string.drawer_item_home)).withIcon(GoogleMaterial.Icon.gmd_home);




        //initialize and create the image loader logic
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(final ImageView imageView, final Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(placeholder).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(getApplication())
                                .load(uri)
                                .error(R.drawable.grey_header)
                                .into(imageView, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Log.v("Picasso", "Could not fetch image");
                                    }
                                });
                    }
                });

            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }

            /*
            @Override
            public Drawable placeholder(Context ctx) {
                return super.placeholder(ctx);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                return super.placeholder(ctx, tag);
            }
            */
        });

        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.grey_header)
                .build();



        updateaccountHeader();

        projectsDrawer= new DrawerBuilder().
                withActivity(this).
                withToolbar(toolbar)
                .withAccountHeader(accountHeader).
                addDrawerItems(
                        new SectionDrawerItem().withName(R.string.general),
                        homefragment,
                        goToSetting,
                        goToInfo,
                        new SectionDrawerItem().withName(R.string.my_projects).withDivider(true)

                ).build();


        projectsDrawer.setOnDrawerItemClickListener(new ProjectDrawerItemListener());
        projectsDrawer.setOnDrawerItemLongClickListener(new ProjectDrawerItemLongListener());

        fab= new FabBuilder(this)
                .withAnchorId(R.id.appbar)
                .withAutoHide(true)
                .withImageDrawable(getResources().getDrawable(R.drawable.ic_add_24dp))
                .withCoordinatorLayout(coordinator)
                .withPosition(FabBuilder.FabPosition.BOTTOM_RIGHT)
                .withSize(FabBuilder.FabSize.FAB_NORMAL)
                .build();
        fab.hide();

    }

    /**
     * funzione chiamata alla creazione dell'activiy. Riempie il drawer di progetti e per
     * ciascun progetto aggiunge i componenti stabiliti.
     * in questo caso ciclo sulla dimensione del contenitore di progetti.
     * se è vuoto non creo nulla.
     */

    protected void populateProjects(){

        if (MySingleton.getProjectFragmentsContainer().isEmpty()){
            for (int i=0; i< mcontainer.getContainerSize(); i++){
                Log.d(TAG,"Fragment container empty\n");
                Project toadd = mcontainer.getContainerItem(i);
                PrimaryDrawerItem newItem= new PrimaryDrawerItem().withName(toadd.getNome()).withBadge(DateFormat.format("yyyy-MM-dd hh:mm",toadd.getDataCreazione()).toString());
                projectsDrawer.addItem(newItem);
                toadd.setAssociatedDrawerItemID(newItem);
                newProjectFragment= new NewProjectFragment();
                newProjectFragment.setIdentifier(toadd.getAssociatedDrawerItemID());
                newProjectFragment.addProject(toadd);
                MySingleton.getProjectFragmentsContainer().add(newProjectFragment);

            }

        }else{

            mcontainer=new ProjectContainer();
            for (int i=0; i< MySingleton.getProjectFragmentsContainer().size(); i++){

                newProjectFragment = MySingleton.getProjectFragmentsContainer().get(i);
                Project toadd = MySingleton.getProjectFragmentsContainer().get(i).getProject();
                mcontainer.addProject(toadd);
                PrimaryDrawerItem newItem= new PrimaryDrawerItem()
                        .withName(toadd.getNome()).withIdentifier(toadd.getAssociatedDrawerItemID()).withBadge(DateFormat.format("yyyy-MM-dd hh:mm",toadd.getDataCreazione()).toString());
                projectsDrawer.addItem(newItem);
            }


        }
        projectsDrawer.getAdapter().notifyDataSetChanged();
    }

    public TextView getCostView() {
        return costView;
    }


    public ImageButton getModifyTitle() {
        return modifyTitle;
    }

    public CoordinatorLayout getCoordinator() {
        return coordinator;
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        MySingleton.getInstance().showSimpleSnack(coordinator,"Connection Failed");
        if(MySingleton.getInstance().getJsonProjects()!=null && mcontainer.getContainerSize()==0){
            Gson json = new Gson();
            Log.d(TAG,"HO OTTENUTO IL JSON DAL SINGLETON");
            mcontainer=json.fromJson(MySingleton.getInstance().getJsonProjects(),ProjectContainer.class);
            populateProjects();
        }else{
            loadFromInternalMemory();
        }

    }

    /**
     * funzione chiamata automaticamente una volta che è stato verificato il login a Google
     * controlla che il salvataggio su drive sia abilitato
     * infine si preoccupa di caricare i progetti, prima cercando in locale e poi, se abilitato anche su Drive
     * chiamando la query
     * Se ci sono discrepanze, perchè ad esempio l'utente ha creato dei progetti che non sono nel backup,
     * si preoccupa di gestirli e fare un merge delle informazioni, avvisando prima l'utente. Se l'utente rifiuta, il backuo su Drive
     * viene sovrascritto
     * @param bundle
     */
    @Override
    @DebugLog
    public void onConnected(Bundle bundle) {

        loadFromDrive= prefs.getBoolean(getString(R.string.saveOnDrivePreference),false);
        if (mcontainer.getContainerSize()==0){
            String memoryOption = prefs.getString(getString(R.string.priority_saving),"Internal Memory");

            if(MySingleton.getProjectFragmentsContainer().isEmpty()) {

                if (memoryOption.matches("1")) {
                    Log.d(TAG, "loading from Drive First");
                    if (loadFromDrive) {
                        if (driveID == null) {
                            queryDriveFile(true);
                        } else {

                            queryDriveFile(false);
                        }
                    } else {
                        loadFromInternalMemory();
                    }

                } else {
                    Log.d(TAG, "loading from Internal Memory First");
                    loadFromInternalMemory();
                }
            }else{
                Log.d(TAG,"uso il fragment container del Singleton");
                populateProjects();
                if(IDENTIFIER!=0){
                    String TAG = Integer.toString(IDENTIFIER);
                    NewProjectFragment infocus = (NewProjectFragment) getSupportFragmentManager().findFragmentByTag(TAG);
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, infocus,TAG)
                            .addToBackStack(TAG).commit();
                }else getSupportFragmentManager().beginTransaction().replace(R.id.container, mainfragment, "0").commit();
                updateaccountHeader();
            }

        }else{

            boolean backupRestore = prefs.getBoolean(getString(R.string.backup_restore_from_Drive),true);
            if(backupRestore) {
                prefs.edit().putBoolean(getString(R.string.backup_restore_from_Drive),false).apply();
                String memoryOption = prefs.getString(getString(R.string.priority_saving), "Internal Memory");
                if (memoryOption.matches("1")) {
                    Log.d(TAG, "loading from Drive First");

                    if (loadFromDrive) {

                        new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.overvriteCurrentInfo))
                                .setMessage(getString(R.string.overvriteCurrentInfoMessage))
                                .setPositiveButton(getString(R.string.overwrite), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        queryDriveFile(true);
                                    }
                                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();


                    }

                }
            }

        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionFailed:" + i);
        MySingleton.getInstance().showMessage(getString(R.string.connectionInterruptedMessage),context);
        loadFromInternalMemory();
    }

    public ImageView getToolbarHeader() {
        return toolbarHeader;
    }

    /**
     * Onclick del Drawer:
     * home -> vado alla bolla
     * setting -> impostazioni
     * info -> informazionis
     * else: in questo caso sto cliccando su un progetto. voglio quindi andare a recuperare
     * il rispettivo fragment che contiene anche la lista dei componenti, e aggiungerlo alla vista.
     */
    public class ProjectDrawerItemListener implements Drawer.OnDrawerItemClickListener {



        public ProjectDrawerItemListener(){


        }


        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {


            if (drawerItem.equals(goToSetting.getIdentifier())) {

                Intent intent = new Intent(MainActivity.this, MyPreferencesActivity.class);
                startActivity(intent);
                projectsDrawer.closeDrawer();

            }
            else if (drawerItem.equals(goToInfo.getIdentifier())) {

                new LibsBuilder()
                        //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .withFields(R.string.class.getFields())
                                //start the activity
                        .start(MainActivity.this);
            }

            else if (drawerItem.equals(homefragment.getIdentifier())) {

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, mainfragment,"0")
                        .addToBackStack("0").commit();

                projectsDrawer.closeDrawer();
            }
            else {//in questo caso cerco nel Singleton il fragment rispettivo e lo aggiungo
                for (int i=0; i< MySingleton.getProjectFragmentsContainer().size(); i++){

                    newProjectFragment= MySingleton.getProjectFragmentsContainer().get(i);


                    if (drawerItem.equals(newProjectFragment.getIdentifier())){


                        String TAG =Integer.toString(newProjectFragment.getIdentifier());

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, newProjectFragment,TAG).addToBackStack(TAG).commit();



                    }

                }


                projectsDrawer.closeDrawer();
            }


            return true;

        }
    }

    /**
     * gestione del longclick su un elemento del drawer.
     * Se clicco un progetto -> si elimina
     */
    public class ProjectDrawerItemLongListener implements Drawer.OnDrawerItemLongClickListener {


        @Override
        public boolean onItemLongClick(View view, int position, IDrawerItem drawerItem) {


            if (drawerItem.equals(goToSetting.getIdentifier())) {

            }
            else if (drawerItem.equals(goToInfo.getIdentifier())) {

            }

            else if (drawerItem.equals(homefragment.getIdentifier())) {


            }
            else {//cerco fra tutti i progetti quello associato al draweritem selezionato
                for (int i=0; i< mcontainer.getContainerSize(); i++){

                    Project el = mcontainer.getContainerItem(i);
                    if (drawerItem.equals(el.getAssociatedDrawerItemID())){
                        //se esiste recupero il rispettivo fragment (hanno lo stesso id per comodita')

                        for(int x=0; x<MySingleton.getProjectFragmentsContainer().size(); x++){

                            if (drawerItem.equals(MySingleton.getProjectFragmentsContainer().get(x).getIdentifier())){
                                tmpFragment=MySingleton.getProjectFragmentsContainer().get(x);
                                MySingleton.removeFragmentByID(tmpFragment.getIdentifier());
                                getSupportFragmentManager().beginTransaction().remove(tmpFragment).commit();
                            }

                        }
                        //rimuovo il fragment e mi sposto sulla home

                        tmpProject=el;
                        tmpPosition=i;


                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, mainfragment, "0")
                                .addToBackStack("0").commit();


                        //elimino il progetto dal contenitore
                        mcontainer.deleteProject(i);
                        //elimino l'etichetta dal drawer
                        projectsDrawer.removeItem(el.getAssociatedDrawerItemID());
                        tmpDrawerItemPos=position;
                        projectsDrawer.closeDrawer();

                        Snackbar.make(coordinator, R.string.snackbar_card_delete, Snackbar.LENGTH_LONG)
                                .setAction(R.string.snackbar_card_undo_action, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        addExistingProjectAtPosition(tmpProject, tmpPosition, tmpDrawerItemPos);
                                    }
                                })
                                .show();


                        Log.d(TAG,"Csize:" + mcontainer.getContainerSize());
                        Log.d(TAG,"NFragment"+MySingleton.getProjectFragmentsContainer().size());
                    }

                }

            }

            return true;

        }
    }

    /**
     * funzione che utilizzo quando creo un nuovo progetto attraverso il pulsante della toolbar.
     * @param toAdd
     */

    public void addNewProject(Project toAdd){
        newProjectFragment= new NewProjectFragment();
        PrimaryDrawerItem newItem= new PrimaryDrawerItem();

        Integer pos = mcontainer.getContainerSize() +1;
        toAdd.setNome("project: " + pos.toString());
        toAdd.setDataCreazione(Calendar.getInstance().getTime());
        newItem.withName(toAdd.getNome()).withBadge(DateFormat.format("yyyy-MM-dd hh:mm",toAdd.getDataCreazione()).toString());
        projectsDrawer.addItem(newItem);
        toAdd.setAssociatedDrawerItemID(newItem);
        newProjectFragment.setIdentifier(toAdd.getAssociatedDrawerItemID());
        MySingleton.getProjectFragmentsContainer().add(newProjectFragment);
        newProjectFragment.addProject(toAdd);
        mcontainer.addProject(toAdd);
        Log.d(TAG,"Csize:" + mcontainer.getContainerSize());
        Log.d(TAG,"NFragment" + MySingleton.getProjectFragmentsContainer().size());
    }

    public void addExistingProjectAtPosition(Project toAdd, Integer pos, int drawerPos){
        newProjectFragment= new NewProjectFragment();
        PrimaryDrawerItem newItem= new PrimaryDrawerItem();

        newItem.withName(toAdd.getNome()).withBadge(DateFormat.format("yyyy-MM-dd hh:mm",toAdd.getDataCreazione()).toString());
        projectsDrawer.addItemAtPosition(newItem, drawerPos);
        toAdd.setAssociatedDrawerItemID(newItem);
        newProjectFragment.setIdentifier(toAdd.getAssociatedDrawerItemID());
        MySingleton.getProjectFragmentsContainer().add(newProjectFragment);
        newProjectFragment.addProject(toAdd);

        mcontainer.addProjectAtPosition(pos, tmpProject);
        System.out.println("Csize:" + mcontainer.getContainerSize());
        System.out.println("NFragment" + MySingleton.getProjectFragmentsContainer().size());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_project, menu);

         item = menu.findItem(R.id.action_new_project);
        item.setVisible(true);
        MenuItem item2 = menu.findItem(R.id.action_update_price);
        item2.setVisible(false);
        MenuItem item3 = menu.findItem(R.id.action_add_photo);
        item3.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new_project) {
            addNewProject(new Project());
            Snackbar.make(coordinator, R.string.snackbar_card_new_project, Snackbar.LENGTH_LONG)
                    .show(); // Don’t forget to show!
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_update_price) {
            //do nothing here
            return false;
        }

        if (id == R.id.action_add_photo) {
            //do nothing here
            return false;
        }
        

        return super.onOptionsItemSelected(item);
    }

    /**
     * salvo il file con i progetti ed i rispettivi componenti nella memoria interna.
     * @param fileName
     * @param toSave
     */

    private void saveProjects(String fileName, String toSave){

        try {
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(toSave.getBytes("UTF8"));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * carico dalla memoria interna il file.
     * può generare eccezioni.
     * @param filename
     * @return
     * @throws IOException
     */

    private String readProjects(String filename) throws IOException {
        FileInputStream fis = null;
        String line;
        StringBuilder out = new StringBuilder();

        fis = openFileInput(filename);
        InputStreamReader dataIO = new InputStreamReader(fis, "UTF8");

        BufferedReader reader = new BufferedReader(dataIO);


        while ((line = reader.readLine()) != null) {
            out.append(line);
        }

        reader.close();
        dataIO.close();
        fis.close();


        return out.toString();
    }

    public CollapsingToolbarLayout getCollapsingToolbar() {
        return collapsingToolbar;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public FloatingActionButton getFab() {
        return fab;
    }

    /**
     *
     * @return il fragment attualmente visualizzato
     */

    public NewProjectFragment getActiveFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            return null;
        }
        String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
        if (tag.equals("0")) return null;
        return (NewProjectFragment) getSupportFragmentManager().findFragmentByTag(tag);
    }




    /**
     *
     * @param title Nuovo titolo da inserire nel Drawer Item
     * @param ID del drawer Item
     */

    public void updateDrawerItemTitle(String title, int ID){

        PrimaryDrawerItem item =(PrimaryDrawerItem) projectsDrawer.getDrawerItem(ID);
        item.withName(title);
        projectsDrawer.getAdapter().notifyDataSetChanged();
    }

    /**
     * blocco la tollbar impedendole di scorrere
     */

    public void lockAppBarClosed() {
        mAppBarLayout.setExpanded(false, false);
        mAppBarLayout.setActivated(false);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)mAppBarLayout.getLayoutParams();
        final TypedArray styledAttributes = getApplicationContext().getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        lp.height = (int) styledAttributes.getDimension(0, 0);
    }

    /**
     * sblocco la toolbar per farla scorrere
     */

    public void unlockAppBarOpen() {
        mAppBarLayout.setExpanded(true, false);
        mAppBarLayout.setActivated(true);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)mAppBarLayout.getLayoutParams();
        lp.height = (int) getResources().getDimension(R.dimen.toolbar_expand_height);
    }


    /**
     * classe AsyncTask che si preoccupa di scaricare da Internet il file Drive precedentemente backuppato
     * per evitare qualsivoglia problema, la classe fa partire un progressDialog appena prima di inziare
     * il download e lo stoppa quando questo è terminato.
     *
     */
    @DebugLog
    private  class DownloadFilesTask extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            updateaccountHeader();
            // Shows Progress Bar Dialog and then call doInBackground method
            showProgressDialog(getString(R.string.standardProgressBarInformation));
        }

        protected void onProgressUpdate(String... progress) {
            Log.d(TAG,"You are in progress update ... ");
        }

        @Override
        protected String doInBackground(String... params) {
            //guardo se ho i permessi
            Log.d("DoINBackGround","On doInBackground...");
            publishProgress("Sleeping...");
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                Log.d("DoINBackGround","got internet permissions");
                //provo a caricare il file da drive, tramite metodo getFileIS();
                try {
                    DriveId myDriveID = DriveId.decodeFromString(driveID);
                    /********************/
                   return getFileIs(mGoogleApiClient, myDriveID);
                    /*******************/

                } catch (java.lang.IllegalArgumentException j) {
                    Log.d(TAG, "no file id usable");
                }catch (java.lang.NullPointerException x){
                    Log.d(TAG, "no file id");
                }
            }
            return null;
        }

        /**
         * ho ottenuto qualcosa prendo la stringa e me la converto nella classe Container.
         * se rimane vuoto -> c'è stato qualche problema nel leggere il fiele: istanzio nuovamente il container parto da zero
         * se non è vuoto -> popolo i progetti
         *
         */
        protected void onPostExecute(String result) {
            Log.d("OnPostExecute","OnPostExecute....");

            Gson gson = new Gson();
            if(mcontainer.getContainerSize()!=0) {
                Log.d(TAG,"aveo dei progetti in memoria, provo a fondere");
                ProjectContainer tmpContainer = new ProjectContainer();
                int unsavedProjects = mcontainer.getContainerSize();
                Log.d(TAG,"size: "+unsavedProjects);
                for(int i=0; i<unsavedProjects; i++){
                    tmpContainer.addProject(mcontainer.getContainerItem(i));
                    //elimino il progetto dal contenitore
                    projectsDrawer.removeItem(mcontainer.getContainerItem(i).getAssociatedDrawerItemID());
                    MySingleton.removeFragmentByID(mcontainer.getContainerItem(i).getAssociatedDrawerItemID());
                }
                mcontainer = gson.fromJson(result, ProjectContainer.class);
                if (mcontainer != null) {
                    Log.d(TAG,"aggiungo vecchi progetti");
                    for(int i=0; i<unsavedProjects; i++){
                        mcontainer.addProject(tmpContainer.getContainerItem(i));
                    }
                    populateProjects();
                } else {
                    Log.d(TAG,"nessun nuovo progetto da aggiungere, tengo quelli vecchi");
                    mcontainer =new ProjectContainer();
                   populateProjects();

                }
            }else{
                Log.d(TAG,"nessun vecchio progetto, inserisco i nuovi");
                prefs.edit().putBoolean(getString(R.string.backup_restore_from_Drive),false).apply();
                mcontainer = gson.fromJson(result, ProjectContainer.class);
                if (mcontainer != null) {
                    populateProjects();
                } else {
                    mcontainer =new ProjectContainer();
                }

            }



            if(IDENTIFIER!=0){
                String TAG = Integer.toString(IDENTIFIER);
                NewProjectFragment infocus = (NewProjectFragment) getSupportFragmentManager().findFragmentByTag(TAG);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, infocus,TAG)
                        .addToBackStack(TAG).commit();
            }else getSupportFragmentManager().beginTransaction().replace(R.id.container, mainfragment, "0").commit();

            hideProgressDialog();
        }
    }

    /**
     *
     * @param gac GoogleApiClient da cui ottenere il file
     * @param drvId Id del file da recuperare. lo ottenfo con la query
     * @return la stringa contenete le informazioni sui progetti
     */

    private String getFileIs(final GoogleApiClient gac, final DriveId drvId) {
                    DriveFile df = Drive.DriveApi.getFile(gac, drvId);
                    DriveApi.DriveContentsResult rslt = df.open(gac, DriveFile.MODE_READ_ONLY, null).await();
                    if (rslt.getStatus().isSuccess()){
                        Log.d("getFileIs","file obtained!");
                        String json;
                        myRestoredDataFromDrive= rslt.getDriveContents().getInputStream();
                        try{
                            byte[] data = new byte[myRestoredDataFromDrive.available()];
                            int read = myRestoredDataFromDrive.read(data);
                            Log.d("getFileIs","read bytes: "+read);
                            json = new String(data);
                            return json;
                        }catch (java.io.IOException e){
                            Log.d("TAG","Errore nella conversione del buffer - lettura da drive");
                        }
                    }
                    return null;

    }


    /**
     * Classe con cui interrogo Drive per trovare l'ID del file contenete il salvateggio
     * restituisce un sacco di progetti. Carico l'ultimo creato.
     *
     */

    private void queryDriveFile(boolean showoption){

        SortOrder sortOrder = new SortOrder.Builder()
                .addSortDescending(SortableField.MODIFIED_DATE).build();

        Query query = new Query.Builder().addFilter(Filters.and(
                Filters.eq(SearchableField.MIME_TYPE, getString(R.string.savedFileProjectType)),
                Filters.contains(SearchableField.TITLE,getString(R.string.savedFileProjectName)))).setSortOrder(sortOrder).build();

        if(showoption){
            Drive.DriveApi.query(mGoogleApiClient, query)
                    .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {

                        @Override
                        public void onResult(DriveApi.MetadataBufferResult result) {
                            // Iterate over the matching Metadata instances in mdResultSet
                            if (!result.getStatus().isSuccess()) {
                                Log.d(TAG,"Problem while retrieving files");

                            }else{
                                int count =result.getMetadataBuffer().getCount();
                                int tmpcount=0;
                                Log.d(TAG,"Files Retrieved: "+count);
                                if (count!=0) {
                                    if(count > 20) {
                                        tmpcount=count;
                                        count=20;
                                    }
                                    final MetadataBuffer buffer = result.getMetadataBuffer();
                                    final String [] resultSet = new String [count];
                                    final String [] driveidList= new String[count];

                                    for (int i=0; i<count; i++){
                                        resultSet[i]=DateFormat.format("yyyy-MM-dd hh:mm:ss", buffer.get(i).getCreatedDate()).toString();
                                        driveidList[i]=buffer.get(i).getDriveId().encodeToString();
                                    }
                                    //evito di tenere in memoria più di 20 backup...
                                    for (int j=20; j<tmpcount; j++){
                                        DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient,
                                                DriveId.decodeFromString(buffer.get(j).getDriveId().encodeToString()));
                                        // Call to delete file.
                                        driveFile.delete(mGoogleApiClient);
                                    }
                                    buffer.release();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle(getString(R.string.select_backup_to_restore));
                                    builder.setItems(resultSet, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int item) {

                                            driveID = driveidList[item];
                                            Log.d(TAG,"Metadata obtained: "+driveID);
                                            new DownloadFilesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
                                        }
                                    });
                                    builder.show();

                                }

                            }

                        }
                    });
        }else{
            Drive.DriveApi.query(mGoogleApiClient, query)
                    .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {

                        @Override
                        public void onResult(DriveApi.MetadataBufferResult result) {
                            // Iterate over the matching Metadata instances in mdResultSet
                            if (!result.getStatus().isSuccess()) {
                                Log.d(TAG,"Problem while retrieving files");
                            }else{
                                int count =result.getMetadataBuffer().getCount();
                                Log.d(TAG,"Files Retrieved: "+count);
                                if (count!=0) {
                                    final MetadataBuffer buffer = result.getMetadataBuffer();

                                    driveID = buffer.get(0).getDriveId().encodeToString();
                                    Log.d(TAG,"Metadata obtained: "+driveID);
                                    new DownloadFilesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
                                    buffer.close();
                                }

                            }

                        }
                    });

        }
    }


    public  void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(true);
        }

        mProgressDialog.show();
    }

    public  void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
            mProgressDialog.dismiss();
            Log.d("progressDialog","hidden");
        }
    }

    /**
     * legge dalla memoria interna i progetti... se fallisce la lettura, prova a leggere da Drive
     * Lettura da memoria Interna -> readProjects(getString(R.string.savedFileProjectName));
     * Lettura da drive -> queryDriveFile();
     */

    private void loadFromInternalMemory(){
        //se ho letto qualcosa , lo carico in memoria == avevo salvato dei progetti
        Gson gson = new Gson();
        String json;
        try {
            json = readProjects(getString(R.string.savedFileProjectName));
            Log.d(TAG,"load from internal memory");
            mcontainer = gson.fromJson(json, ProjectContainer.class);

            populateProjects();

            // ricarico la scena dall'ultimo fragment in cui mi trovavo
            if (IDENTIFIER==0){
                getSupportFragmentManager().beginTransaction().replace(R.id.container, mainfragment, "0")
                        .addToBackStack("0").commit();
            }
            else{
                String TAG = Integer.toString(IDENTIFIER);
                NewProjectFragment infocus = (NewProjectFragment) getSupportFragmentManager().findFragmentByTag(TAG);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, infocus,TAG)
                        .addToBackStack(TAG).commit();
            }
            updateaccountHeader();

        } catch (IOException e) {
            Log.d(TAG,"niente da caricare provo con Drive\n");

            if(loadFromDrive) {
                if(driveID==null){
                    queryDriveFile(true);
                }else{
                    queryDriveFile(false);
                }
            }

        }
    }




    @DebugLog
    private  class LocalSaveFilesTask extends AsyncTask<String, Integer, String>{
        @Override
        protected String doInBackground(String... params) {
            Gson gson = new Gson();
            String json = gson.toJson(mcontainer);
            saveProjects(getString(R.string.savedFileProjectName), json);
            return null;
        }

        protected void onPostExecute(String result) {
            Log.d("LOCAL SAVING TASK","ENDED SAVING");
        }

    }

    @DebugLog
    private  class OnlineSaveFilesTask extends AsyncTask<Object, Integer, String> {
        @Override
        protected String doInBackground(Object... params) {
            GoogleApiClient googleApiClient = (GoogleApiClient) params[0];
            String json = (String) params[1];
            DriveFile drfl=null;

                try{
                    DriveFolder dfl = Drive.DriveApi.getAppFolder(googleApiClient);

                    String title = getString(R.string.savedFileProjectName);
                    String mime = getString(R.string.savedFileProjectType);
                    byte[] buff = (json).getBytes();
                    DriveApi.DriveContentsResult rslt = Drive.DriveApi.newDriveContents(mGoogleApiClient).await();
                    if (rslt.getStatus().isSuccess()) {
                        DriveContents cont = rslt.getDriveContents();
                        cont.getOutputStream().write(buff);
                        MetadataChangeSet meta = new MetadataChangeSet.Builder().setTitle(title).setMimeType(mime).build();
                        drfl= dfl.createFile(mGoogleApiClient, meta, cont).await().getDriveFile();
                    }

                }catch (java.lang.IllegalStateException e){
                    Log.d(TAG,"client not connected");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            if(drfl!=null) return drfl.getDriveId().encodeToString();

            return null;

        }

        protected void onPostExecute(String result) {
            Log.d("ONLINE SAVING TASK","ENDED SAVING");
            if(result!=null) MySingleton.getInstance().setDriveID(result);
        }

    }
}
