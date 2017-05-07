package org.classes;

import java.awt.*;
import java.io.*;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingold;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

/**
 * Created by henry on 4/27/17.
 */
public class GephiScript {

    File graphFile;

    public void loadFile(String pathname) throws URISyntaxException {
        try {
            this.graphFile = new File(getClass().getResource(pathname).toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void script(String pathname) {
        /**
         * A script that uses Gephi Toolkit to perform the operations necessary to layout a graph in Gephi
         **/

        //Initialize a project and a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get models and controllers for this new workspace - will be useful later
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        //Import a graph file
        Container container;
        try {
            loadFile(pathname);
            container = importController.importFile(this.graphFile);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);

        //Instantiate Spectral Layout
        SpectralLayout layout = new SpectralLayout(null);

        //associate the graphModel with the Layout
        layout.setGraphModel(graphModel);

        //ensure default properites
        layout.resetPropertiesValues();

        //prepare the layout algorithm
        layout.initAlgo();
        //run the layout
        layout.goAlgo();
        //close
        layout.endAlgo();

        //Export the layout to the a gexf file to be opened in Gephi
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("spectral_layout_output.gexf"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
