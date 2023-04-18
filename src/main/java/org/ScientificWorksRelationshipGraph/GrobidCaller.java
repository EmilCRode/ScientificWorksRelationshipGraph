package org.ScientificWorksRelationshipGraph;

import org.apache.commons.lang3.StringUtils;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

            engine = GrobidFactory.getInstance().createEngine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void grobidToObjects (File pdfFile, int consolidate, Neo4jHandler handler, String discipline, String journal) {
        try {
                // Biblio object for the result
            BiblioItem resHeader = new BiblioItem();
            engine.processHeader(pdfFile.getPath(), consolidate, resHeader);
            if(pdfFile.getName() != null){ resHeader.setTitle(pdfFile.getName().replace(".pdf","").trim()); }
            String title = resHeader.getTitle();
            if (title == null || title.isBlank()) {
                System.out.println("Title not found for Document: " + pdfFile.getName());
                return;
            }
            if (resHeader.getFullAuthors() == null) {
                System.out.println("Authors not found for Document: " + pdfFile.getName());
                return;
            }
            if (resHeader.getFullAuthors().isEmpty()) {
                System.out.println("Authors not found for Document: " + pdfFile.getName());
                return;
            }
            Work work = Work.createUniqueWork(resHeader, handler, pdfFile.getName());
            if(work == null){return;}//Remove when MERGE is implemented
            work.setDiscipline(discipline);
            work.setJournal(journal);
            //Adding all the citations to the Work representing the PDF
            List<BibDataSet> citations = engine.processReferences(pdfFile, consolidate);
            Work currentWork;
            for (BibDataSet bib : citations) {
                currentWork = Work.createUniqueWork(bib.getResBib(),handler, pdfFile.getName());
                if( currentWork != null) {
                    work.addCitation(currentWork);
                }
            }
            handler.createOrUpdate(work);
        } catch (Exception e) {
            // If an exception is generated, print a stack trace
            e.printStackTrace();
        }
    }

    public void close() {
    }

}