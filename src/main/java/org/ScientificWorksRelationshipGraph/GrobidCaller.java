package org.ScientificWorksRelationshipGraph;

import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Person;
import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.utilities.GrobidProperties;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

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

    public void runGrobidToObjects (File pdfFile, String process, int consolidate, Neo4jHandler neo4jHandler) {
        try {
            if (process.equals("header")) {
                // Biblio object for the result
                BiblioItem resHeader = new BiblioItem();
                engine.processHeader(pdfFile.getPath(), consolidate, resHeader);

                Work work = new Work();
                String title = resHeader.getTitle();
                List<Person> authors = resHeader.getFullAuthors();
                resHeader.getNormalizedPublicationDate();

                if(title == null){
                    System.out.println("Title not found for Document: " + pdfFile.getName());
                    return;
                }
                if(authors.isEmpty()){
                    System.out.println("Authors not found for Document: " + pdfFile.getName());
                    return;
                }

                Author currentAuthor;
                for (int i = 0; i < authors.size(); i++) {
                    currentAuthor = new Author(authors.get(i));
                }
                
                work.setTitle(title);
                work.setAuthors(authors);
                //needs other attributes
                neo4jHandler.createOrUpdate(work);
            } else if (process.equals("citation")) {
                List<BibDataSet> citations = engine.processReferences(pdfFile, consolidate);
                for (BibDataSet bib : citations) {
                    if (bib.getResBib() != null){}
                        //bibtex.append(bib.getResBib().toBibTeX());
                }
            } else {
                System.err.println("Unknown selected process: " + process);
                System.err.println("Usage: command process[header,citation] path_to_pdf");
            }
        } catch (Exception e) {
            // If an exception is generated, print a stack trace
            e.printStackTrace();
        }
    }

    public void close() {
    }

}