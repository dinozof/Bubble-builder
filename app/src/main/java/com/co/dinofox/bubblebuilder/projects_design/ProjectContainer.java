package com.co.dinofox.bubblebuilder.projects_design;

import com.co.dinofox.bubblebuilder.fragments.NewProjectFragment;

import java.util.ArrayList;

/**
 *  Semplice classe per contenere tutti i progetti e semplificarne il salvataggio.
 *  Contiene alcuni metodi per la gestione dei progetti.
 */
public class ProjectContainer {


    private ArrayList<Project> projectContainerList;



    public ProjectContainer() {
        // Exists only to defeat instantiation.
        projectContainerList= new ArrayList<>();
    }

    public void addProjectAtPosition(int position, Project toAdd){

        projectContainerList.add(position,toAdd);

    }

    public void addProject(Project toAdd){

        projectContainerList.add(toAdd);

    }

    public void deleteProject(int position){

            projectContainerList.remove(position);

    }

    public Integer getContainerSize(){

        return projectContainerList.size();

    }

    public Project getContainerItem(int index){

        return projectContainerList.get(index);
    }

    public ArrayList getProjectContainerList(){

        return projectContainerList;
    }

    public void setProjectList(ArrayList<Project> list){
         projectContainerList=list;
    }

}
