package com.co.dinofox.bubblebuilder.projects_design;

/**
 * Semplice classe per la gestione del singolo componente in un progetto.
 * Contiene i metodi necessari a gestire i componenti. Nelle
 *  Cards della RecyclerView.
 * */
public class MComponent {

    private String componentName;
    private float componentPrice;
    private String componentDescription;
    private int componentNumber=1;

    public MComponent() {
    }


    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public float getComponentPrice() {
        return componentPrice;
    }

    public void setComponentPrice(float componentPrice) {
        this.componentPrice = componentPrice;
    }

    public String getComponentDescription() {
        return componentDescription;
    }

    public void setComponentDescription(String componentDescription) {
        this.componentDescription = componentDescription;
    }

    public int getComponentNumber() {
        return componentNumber;
    }

    public void setComponentNumber(int componentNumber) {
        this.componentNumber = componentNumber;
    }

}
