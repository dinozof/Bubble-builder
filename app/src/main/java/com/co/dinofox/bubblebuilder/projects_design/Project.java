package com.co.dinofox.bubblebuilder.projects_design;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

import java.util.ArrayList;
import java.util.Date;

/**
 * Classe per la gestione del singolo progetto
 * presenta una lista contenente i componenti (rappresentati in schede)
 * mantiene in memoria anche l'ID dei drawerItem rispettivi associati al Fragment che contiene
 * il progetto
 */
public class Project {

    private String nome;
    private Date dataCreazione;
    private ArrayList<MComponent> components= new ArrayList<>();
    private String imageUri;
    private int associatedDrawerItemID;


    public void deleteComponents(){

        components.clear();
    }

    public Project(){


    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Date getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(Date dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public ArrayList<MComponent> getComponents() {
        return  components;
    }

    public void addNewcomponent(MComponent newcomponent, int index){
        components.add(index,newcomponent);
    }


    public int getAssociatedDrawerItemID() {
        return associatedDrawerItemID;
    }

    public void setAssociatedDrawerItemID(PrimaryDrawerItem associatedDrawerItemID) {
        this.associatedDrawerItemID = associatedDrawerItemID.getIdentifier();
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri.toString();
    }
}
