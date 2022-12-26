package org.ScientificWorksRelationshipGraph;

import org.grobid.core.data.Affiliation;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Person;
import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.utilities.GrobidProperties;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class GrobidCaller{
    private static Engine engine = null;
    public GrobidCaller() {
        try {
            // context variable are read from the project property file grobid-example.properties
            Properties prop = new Properties();
            prop.load(new FileInputStream("grobid.properties"));
            String pGrobidHome = prop.getProperty("grobid.pGrobidHome");
            GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList(pGrobidHome));
            GrobidProperties.getInstance(grobidHomeFinder);

            System.out.println(">>>>>>>> GROBID_HOME=" + GrobidProperties.get_GROBID_HOME_PATH());

            engine = GrobidFactory.getInstance().createEngine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void grobidToObjects (File pdfFile, int consolidate, Neo4jHandler handler) {
        try {
            long startTimegrobidToObjects = System.currentTimeMillis();
                // Biblio object for the result
                BiblioItem resHeader = new BiblioItem();
                engine.processHeader(pdfFile.getPath(), consolidate, resHeader);
            String title = resHeader.getTitle();
            if (title == null || title.isBlank()) {
                System.out.println("Title not found for Document: " + pdfFile.getPath());
                return;
            }
            if (resHeader.getFullAuthors().isEmpty() || resHeader.getFullAuthors() == null) {
                System.out.println("Authors not found for Document: " + pdfFile.getPath());
                return;
            }
                Work work = Work.createUniqueWork(resHeader, handler);
                //Adding all the citations to the Work representing the PDF
                List<BibDataSet> citations = engine.processReferences(pdfFile, consolidate);
                Work currentWork;
                for (BibDataSet bib : citations) {
                    currentWork = Work.createUniqueWork(bib.getResBib(),handler);
                    if(currentWork.getTitle() != null) {
                        work.addCitation(currentWork);
                    }
                }
                handler.createOrUpdate(work);
                System.out.println("Created: '" + title + "' in " + (System.currentTimeMillis() - startTimegrobidToObjects) + " milliseconds");
        } catch (Exception e) {
            // If an exception is generated, print a stack trace
            e.printStackTrace();
        }
    }

    public void close() {
    }

}