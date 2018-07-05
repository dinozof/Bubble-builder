package com.co.dinofox.bubblebuilder.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.Utils.UI.FabBuilder;
import com.Utils.UI.SimpleItemTouchHelperCallback;
import com.co.dinofox.bubblebuilder.activities.MainActivity;
import com.co.dinofox.bubblebuilder.adapters.NewProjectAdapter;
import com.co.dinofox.bubblebuilder.R;
import com.co.dinofox.bubblebuilder.projects_design.MComponent;
import com.co.dinofox.bubblebuilder.projects_design.Project;
import com.co.dinofox.bubblebuilder.singletons.MySingleton;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.mlsdev.rximagepicker.RxImagePicker;
import com.mlsdev.rximagepicker.Sources;
import com.poliveira.parallaxrecyclerview.ParallaxRecyclerAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import rx.functions.Action1;

/**
 * Fragment destinato alla gestione del singolo progetto.
 * ne viene creato uno diverso per ogni progetto.
 * possiede un ID che combacia con quello del Draweritem a lui associato
 */
public class NewProjectFragment extends Fragment {


    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private NewProjectAdapter mAdapter;
    private CoordinatorLayout coordinator;

    private AppBarLayout.LayoutParams toolbarParams;

    private Project project;
    private int identifier;
    private FloatingActionButton fab;
    private ImageButton modTitle;
    private TextView costView;
    private boolean firstTime;
    private SharedPreferences prefs;
    private static ImageView mainActivityToolbarHeader;
    private ShowcaseView showcaseView;




    public NewProjectFragment() {



    }



    @Override
    public void onResume(){
        super.onResume();
        String imageUri = project.getImageUri();
        if(imageUri!=null){
            chargePhotoFromProject(imageUri);
        }else{
            Log.d("IMAGE CHARGING:"," NUlla");
            mainActivityToolbarHeader.setImageDrawable(getResources().getDrawable(R.drawable.grey_header));
        }
        updatePrice();
        fab.show();

        firstTime=prefs.getBoolean("first_time_showcase",true);
        Log.d("Showcase","read: "+firstTime);
        ViewTarget target = new ViewTarget(fab);
        if(firstTime){
            //prefs.edit().putBoolean("first_time_showcase",false).apply();

            if(showcaseView==null){
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                params.setMargins(50,0,50,50);
                showcaseView = new ShowcaseView.Builder(getActivity())
                        .withMaterialShowcase()
                        .setStyle(R.style.CustomShowcaseTheme)
                        .setTarget(target)
                        .setContentTitle(R.string.showcaseTitleNewProject)
                        .setContentText(R.string.showcaseDescriptionNewProject)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showcaseView.hide();
                            }
                        })
                        .build();
                showcaseView.setButtonPosition(params);
            }else{
                showcaseView.show();
            }
        }
    }



    //salvo l'ID del fragment
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("ID", identifier);
    }

    //mi riprendo l'id del fragment.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // keep the fragment and all its data across screen rotation
        //setRetainInstance(true);
        if(savedInstanceState!=null){
            identifier = savedInstanceState.getInt("ID");
        }

    }




    /**
     * Setup iniziale del Fragment. -> carico la recyclerView.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.new_project, container, false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.my_recycler_view);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.scrollToPosition(0);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // specify an adapter
        // se project è nullo (quindi il fragment è stato distrutto e poi ricreato) lo recupero
        // dal Singleton.
        if(project==null){
            project=MySingleton.getfragmentProjectByID(identifier);
            //project = MySingleton.getProjectByID(identifier);
            mAdapter = new NewProjectAdapter(project.getComponents());
            mAdapter.notifyDataSetChanged();
        }
        else{
            mAdapter = new NewProjectAdapter(project.getComponents());
        }


        //mAdapter.setHasStableIds(true);
        mAdapter.setRv(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);


        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ((MainActivity)getActivity()).unlockAppBarOpen();

        ((MainActivity)getActivity()).getCollapsingToolbar().setTitle(project.getNome());

        coordinator = ((MainActivity)getActivity()).getCoordinator();
        modTitle=((MainActivity)getActivity()).getModifyTitle();
        costView = ((MainActivity)getActivity()).getCostView();
        mainActivityToolbarHeader = ((MainActivity)getActivity()).getToolbarHeader();

        modTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.new_title_string));
                // I'm using fragment here so I'm using getView() to provide ViewGroup
                // but you can provide here any other instance of ViewGroup from your Fragment / Activity
                View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.changetitle, (ViewGroup) getView(), false);
                // Set up the input
                final EditText input = (EditText) viewInflated.findViewById(R.id.input);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                builder.setView(viewInflated);

                // Set up the buttons
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        project.setNome(input.getText().toString());
                        ((MainActivity) getActivity()).getCollapsingToolbar().setTitle(input.getText().toString());
                        ((MainActivity) getActivity()).updateDrawerItemTitle(input.getText().toString(),identifier);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);



        fab= ((MainActivity)getActivity()).getFab();
        fab.show();


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MComponent testcomponent = new MComponent();
                testcomponent.setComponentDescription(getResources().getString(R.string.Card_view_text_description));
                testcomponent.setComponentName(getResources().getString(R.string.Card_view_Title));
                testcomponent.setComponentPrice(Float.valueOf(getResources().getString(R.string.Card_view_cost)));
                project.addNewcomponent(testcomponent, 0);
                mAdapter.notifyItemInserted(0);
                mRecyclerView.scrollToPosition(0);
            }
        });



        updatePrice();
        return rootView;
    }


    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public Project getProject() {
        return project;
    }

    //assegno il progetto al fragment
    public void addProject(Project toadd){
        project=toadd;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item = menu.findItem(R.id.action_new_project);
        item.setVisible(false);
        MenuItem item2 = menu.findItem(R.id.action_update_price);
        MenuItem item3 = menu.findItem(R.id.action_add_photo);
        item2.setVisible(true);
        item3.setVisible(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_project:
                // Not implemented here
                return false;
            case R.id.action_update_price:
                // Do Fragment menu item stuff here
                updatePrice();
                return true;
            case R.id.action_add_photo:
                // Do Fragment menu item stuff here
                photoSourceSelection();
                return true;
            default:
                break;
        }

        return false;
    }

    private void updatePrice(){
        Float price = mAdapter.getTotalCost();
        String priceString = String.format(Locale.ITALIAN,": %.2f€ ",price);
        String string= getString(R.string.totalCost)+priceString;
        costView.setText(string);

    }



    private void chargePhotoFromProject(final String link){
        Picasso.with(mainActivityToolbarHeader.getContext()).load(Uri.parse(link))
                .networkPolicy(NetworkPolicy.OFFLINE)
                .noFade()
                .into(mainActivityToolbarHeader, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.v("CHARGING IMAGE:", " DONE");
                    }

                    @Override
                    public void onError() {
                        Picasso.with(getContext())
                                .load(Uri.parse(link))
                                .error(R.drawable.grey_header)
                                .into(mainActivityToolbarHeader, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Log.v("Picasso", "Could not fetch image");
                                        MySingleton.getInstance().showSimpleSnack(coordinator,getString(R.string.image_fetch_error));
                                    }
                                });

                    }
                });

    }

    private void photoSourceSelection(){
        final CharSequence[] items = { getString(R.string.take_Photo), getString(R.string.choose_from_Library), getString(R.string.add_external_link)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.select_image_source));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Log.d("Camera query: ","trying to use camera");
                    addPhotoToProject(Sources.CAMERA);
                } else if (items[item].equals("Choose from Library")){
                    Log.d("Camera query: ","trying to use gallery");
                    addPhotoToProject(Sources.GALLERY);
                }else if (items[item].equals("Add an external link")){
                    Log.d("Camera query: ","loading external link");
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(getString(R.string.add_external_link));
                    // I'm using fragment here so I'm using getView() to provide ViewGroup
                    // but you can provide here any other instance of ViewGroup from your Fragment / Activity
                    View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.changetitle, (ViewGroup) getView(), false);
                    // Set up the input
                    final EditText input = (EditText) viewInflated.findViewById(R.id.input);
                    final TextInputLayout layout=(TextInputLayout) viewInflated.findViewById(R.id.textInputLayout);
                    layout.setHint(getString(R.string.link));
                    input.setHint(getString(R.string.link));
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    builder.setView(viewInflated);

                    // Set up the buttons
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Log.d("Charging photo","Photo: "+input.getText());
                            project.setImageUri(Uri.parse(input.getText().toString()));
                            chargePhotoFromProject(input.getText().toString());
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
            }
        });
        builder.show();

    }

    private void addPhotoToProject(Sources source){ // Sources.CAMERA or Sources.GALLERY
        RxImagePicker.with(mainActivityToolbarHeader.getContext()).requestImage(source).subscribe(new Action1<Uri>() {
            @Override
            public void call(final Uri uri) {
                //Get image by uri Image will be charged in onResume() or programmatically
                project.setImageUri(uri);
            }
        });
    }





}
