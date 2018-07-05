package com.co.dinofox.bubblebuilder.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.Utils.UI.FabBuilder;
import com.co.dinofox.bubblebuilder.R;
import com.co.dinofox.bubblebuilder.projects_design.MComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

/**
 * Adapter per la Recycler View del NewProjectFragment.
 * gli tems sono i componenti -> MComponents.
 * la View Holder è la Card.
 */
public class NewProjectAdapter extends RecyclerView.Adapter<NewProjectAdapter.ViewHolder>  implements ItemTouchHelperAdapter{

    private ArrayList<MComponent> items;
    private MComponent tmpComponent;
    private int tmpPosition;
    private RecyclerView rv;
    private Context context;
    private SharedPreferences preferences;

    @Override
    public void onItemDismiss(final int position) {
        tmpComponent =items.get(position);
        tmpPosition = position;
        items.remove(position);
        notifyItemRemoved(position);
        Snackbar.make(rv, "Delete", Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        items.add(tmpPosition,tmpComponent);
                        notifyItemInserted(tmpPosition);
                        rv.scrollToPosition(tmpPosition);
                    }
                })
                .show(); // Don’t forget to show!



    }

    //gestione del movimento una volta selezionata una card
    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    public void setRv(RecyclerView rv) {
        this.rv = rv;
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder  {

        public TextView userName;
        public EditText userNameEditable;
        public TextView cardText;
        public TextView pieceNumberText;
        public EditText pieceNumberTextEditable;
        public EditText cardTextEditable;
        public TextView pricetext;
        public boolean editmode=false;
        public FloatingActionButton editFab;
        public EditText pricetextEditable;
        public Button amazonButton;
        public CoordinatorLayout coordinator;
        private View view;




        public ViewHolder(final View itemView) {
            super(itemView);
            this.view=itemView;

            userName = (TextView) itemView.findViewById(R.id.Card_title);
            userNameEditable= (EditText) itemView.findViewById(R.id.Card_title_editable);
            cardText = (TextView) itemView.findViewById(R.id.Card_text_text_view);

            cardTextEditable= (EditText) itemView.findViewById(R.id.Card_text_edit_text);
            pieceNumberText = (TextView) itemView.findViewById(R.id.piece_number);
            pieceNumberTextEditable= (EditText) itemView.findViewById(R.id.piece_number_editable);
            cardTextEditable= (EditText) itemView.findViewById(R.id.Card_text_edit_text);
            pricetext = (TextView) itemView.findViewById(R.id.insert_cost_label);
            pricetextEditable= (EditText) itemView.findViewById(R.id.insert_cost_label_editable);
            coordinator=(CoordinatorLayout) itemView.findViewById(R.id.card_view_coordinator);
            amazonButton = (Button) itemView.findViewById(R.id.amazon_button);


        }




    }



    public NewProjectAdapter(ArrayList<MComponent> objects){

        items=objects;

    }

    @Override
    public NewProjectAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        final ViewHolder vh = new ViewHolder(v);

        context = parent.getContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);


        vh.editFab=new FabBuilder(parent.getContext())
                .withAnchorId(R.id.card_view)
                .withImageDrawable(ContextCompat.getDrawable(parent.getContext(),R.drawable.ic_mode_edit_24dp))
                .withCoordinatorLayout(vh.coordinator)
                .withPosition(FabBuilder.FabPosition.BOTTOM_LEFT)
                .withSize(FabBuilder.FabSize.FAB_MINI)
                .build();

        /**
         * gestione del click su EditText e TextBox
         * si alternano diventano visibili/invisibili a seconda dell'azione dell'utente.
         */

            vh.editFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!vh.editmode) {
                        vh.editmode = true;
                        vh.pricetext.setVisibility(View.INVISIBLE);
                        vh.userName.setVisibility(View.INVISIBLE);
                        vh.cardText.setVisibility(View.INVISIBLE);
                        vh.pieceNumberText.setVisibility(View.INVISIBLE);
                        vh.editFab.setImageDrawable(ContextCompat.
                                getDrawable(parent.getContext(), R.drawable.ic_check_24dp));
                        vh.pricetextEditable.setText(String.valueOf(items.get(vh.getAdapterPosition()).getComponentPrice()));
                        vh.userNameEditable.setText(String.valueOf(items.get(vh.getAdapterPosition()).getComponentName()));
                        vh.cardTextEditable.setText(String.valueOf(items.get(vh.getAdapterPosition()).getComponentDescription()));
                        vh.pieceNumberTextEditable.setText(String.valueOf(items.get(vh.getAdapterPosition()).getComponentNumber()));
                        vh.cardTextEditable.setVisibility(View.VISIBLE);
                        vh.pieceNumberTextEditable.setVisibility(View.VISIBLE);
                        vh.userNameEditable.setVisibility(View.VISIBLE);
                        vh.pricetextEditable.setVisibility(View.VISIBLE);
                    } else {
                        vh.editmode = false;
                        vh.editFab.setImageDrawable(ContextCompat.
                                getDrawable(context, R.drawable.ic_mode_edit_24dp));
                        String price = vh.pricetextEditable.getText().toString();
                        price = price.replace(',', '.');
                        price = price.replace(" ", "");

                        try {
                            items.get(vh.getAdapterPosition()).setComponentPrice(Float.parseFloat(price));
                            vh.pricetext.setText(String.valueOf(items.get(vh.getAdapterPosition()).getComponentPrice()));
                        } catch (NumberFormatException n) {
                            vh.pricetext.setText(context.getResources().getString(R.string.Card_view_cost));
                        }
                        vh.userNameEditable.setVisibility(View.INVISIBLE);
                        vh.pricetextEditable.setVisibility(View.INVISIBLE);
                        vh.pieceNumberTextEditable.setVisibility(View.INVISIBLE);
                        vh.cardTextEditable.setVisibility(View.INVISIBLE);

                        items.get(vh.getAdapterPosition()).setComponentName(vh.userNameEditable.getText().toString());
                        vh.userName.setText(String.valueOf( items.get(vh.getAdapterPosition()).getComponentName()));


                        items.get(vh.getAdapterPosition()).setComponentDescription(vh.cardTextEditable.getText().toString());
                        vh.cardText.setText(String.valueOf(items.get(vh.getAdapterPosition()).getComponentDescription()));

                        items.get(vh.getAdapterPosition()).setComponentNumber(Integer.parseInt(vh.pieceNumberTextEditable.getText().toString()));
                        vh.pieceNumberText.setText(String.valueOf(items.get(vh.getAdapterPosition()).getComponentNumber()));

                        vh.cardText.setVisibility(View.VISIBLE);
                        vh.pricetext.setVisibility(View.VISIBLE);
                        vh.userName.setVisibility(View.VISIBLE);
                        vh.pieceNumberText.setVisibility(View.VISIBLE);
                        if(! items.get(vh.getAdapterPosition()).getComponentName().equals(parent.getContext().getString(R.string.Card_view_Title))){
                            vh.amazonButton.setClickable(true);
                            vh.amazonButton.setEnabled(true);
                        }

                    }

                }
            });

        vh.amazonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmp = vh.userName.getText().toString();
                tmp = tmp.replaceAll(" ", "+");
                Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(v.getContext().getString(R.string.amazon_base_link)+tmp));
                v.getContext().startActivity(intent);

            }
        });




        return vh;
    }

    @Override
        public void onBindViewHolder(final NewProjectAdapter.ViewHolder holder, final int position) {

        holder.userName.setText(String.valueOf(items.get(position).getComponentName()));
        holder.cardText.setText(String.valueOf(items.get(position).getComponentDescription()));
        holder.pricetext.setText(String.valueOf( items.get(position).getComponentPrice()));
        holder.pieceNumberText.setText(String.valueOf(items.get(position).getComponentNumber()));

        if(!items.isEmpty() && items.get(position).getComponentName().equals(context.getString(R.string.Card_view_Title))){

            holder.amazonButton.setClickable(false);
            holder.amazonButton.setEnabled(false);

        }else{
            holder.amazonButton.setClickable(true);
            holder.amazonButton.setEnabled(true);

        }

    }



    @Override
    public int getItemCount() {
        return items.size();
    }

    public float getTotalCost(){
        float stepperPrice=0;
        int size= items.size();

        for ( int i=0; i< size; i++){

            stepperPrice+=(items.get(i).getComponentPrice()*items.get(i).getComponentNumber());

        }

        return stepperPrice;
    }
}
