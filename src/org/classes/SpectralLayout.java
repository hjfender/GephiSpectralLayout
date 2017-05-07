package org.classes;

import java.util.ArrayList;
import java.util.Iterator;

import org.gephi.graph.api.*;
import org.gephi.graph.spi.LayoutData;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.plugin.ForceVectorNodeLayoutData;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.NbBundle;

/**
 * Created by henry on 4/27/17.
 */
public class SpectralLayout extends AbstractLayout implements Layout {
    protected Graph graph;

    //this property allows us to uniformly scale the distances between nodes
    //effectively this is a zoom-in/zoom-out property
    private float display_distance = 1000f;

    public SpectralLayout(LayoutBuilder layoutBuilder) {
        super(layoutBuilder);
    }

    //initialization required by the parent class
    public void initAlgo() {
        //stub
    }

    public void goAlgo(){
        /**
         * Important method that acually performs the graph layout.
         **/
        //get the graph model and render it editable
        this.graph = this.graphModel.getGraphVisible();
        this.graph.readLock();
        //get the nodes and edges for access and manipulability
        Node[] nodes = this.graph.getNodes().toArray();
        Edge[] edges = this.graph.getEdges().toArray();
        //instantiate the data structures for compute layout
        int[] degreeList = new int[nodes.length];
        int[][] edgeList = new int[edges.length][2];

        //initialize the list of degrees for the layout computation
        for(Node node : nodes){
            NodeIterable neighbors = graph.getNeighbors(node);
            int numNeighbors = neighbors.toArray().length;
            degreeList[nodeIDasInt(node)] = numNeighbors;
        }

        //initialize the list of edges for the layout computation
        for(int i = 0; i < edges.length; i++){
            edgeList[i][0] = nodeIDasInt(edges[i].getSource());
            edgeList[i][1] = nodeIDasInt(edges[i].getTarget());
        }

        //initialize the layout computation
        ComputeLayout cmptr = new ComputeLayout(edgeList,degreeList);

        //compute the layout and store the resulting eigenvectors
        float[][] eigenvectors = cmptr.computeEigenvectors(3);

        //layout the graph with coordinats from the eigenvectors
        for(int i = 0; i < eigenvectors.length; i++){
            nodes[i].setX(display_distance*eigenvectors[i][1]);
            nodes[i].setY(display_distance*eigenvectors[i][2]);
        }
    }

    //a close method required by the parent class
    public void endAlgo() {
        Iterator i$ = this.graph.getNodes().iterator();

        while(i$.hasNext()) {
            Node n = (Node)i$.next();
            n.setLayoutData(null);
        }

    }

    //condition check required by the parent class
    public boolean canAlgo() {
        return true;
    }

    //required reset method
    public void resetPropertiesValues() {
        this.display_distance = 1000f;
    }

    //Getters and Setters for this classes properties lie bellow
    public LayoutProperty[] getProperties() {
        ArrayList properties = new ArrayList();
        //Right now just a placeholder from...
        String SPECTRAL_LAYOUT = "Degree Normalized Spectral Layout";

        try {
            properties.add(LayoutProperty.createProperty(this, Float.class, NbBundle.getMessage(SpectralLayout.class, "spectralLayout.display_distance.name"), "Spectral Layout", "spectralLayout.display_distance.name", NbBundle.getMessage(SpectralLayout.class, "spectralLayout.display_distance.desc"), "getDisplayDistance", "setDisplayDistance"));
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return (LayoutProperty[])properties.toArray(new LayoutProperty[0]);
    }

    public float getDisplayDistance() {
        return this.display_distance;
    }

    public void setDisplayDistance(Float distance) {
        this.display_distance = distance;
    }

    //Helper Methods

    //This method depends on how the ids are indexed
    //if the ids start from 0 don't change it
    //if the ids start from 1 subtract 1 from the answer before returning it
    //WATCH FOR INDEX OUT OF BOUNDS ERRORS
    public int nodeIDasInt(Node node){
        return (int) Float.parseFloat((String) node.getId())-1;
    }

}
