package org.ScientificWorksRelationshipGraph;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class Main {

    /**
     *
     */
    public static void main(String[] args) {
        Neo4jHandler neo4JHandler = new Neo4jHandler();
        if ((args.length != 3) && (args.length != 4)) {
            System.err.println("usage: command process[header|citation] path-to-pdf-file path-to-bib-file");
            return;
        }

        String process = args[0];

        if (!process.equals("citation") && !process.equals("header")) {
            System.err.println("unknown process: " + process);
            System.err.println("usage: command process[header|citation] path-to-pdf-file(s) path-to-bib-file(s)");
            return;
        }

        String pdfPath = args[1];
        String bibPath = args[2];
        String consolidation = null;
        int consolidate = 0;
        if (args.length == 4)
            consolidation = args[3];

        System.out.print(process + " " + pdfPath + " " + bibPath);
        if ((consolidation != null) && (consolidation.equals("1") || consolidation.equals("true")))
            consolidate = 1;
        if ((consolidation != null) && (consolidation.equals("2") ))
            consolidate = 2;

        File pdfFile = new File(pdfPath);
        File bibFile = new File(bibPath);

        if (!pdfFile.exists()) {
            System.err.println("Path does not exist: " + pdfPath);
            System.exit(0);
        }

        List<File> filesToProcess = new ArrayList<File>();
        if (pdfFile.isFile()) {
            filesToProcess.add(pdfFile);
        } else if (pdfFile.isDirectory()) {
            if (!bibFile.exists()) {
                System.err.println("Path does not exist: " + bibPath);
                System.exit(0);
            }

            if (!bibFile.isDirectory()) {
                System.err.println("BibTex path is not a directory: " + bibPath);
                System.exit(0);
            }

            File[] refFiles = pdfFile.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".pdf") || name.endsWith(".PDF");
                }
            });

            if (refFiles == null) {
                System.err.println("No PDF file to be processed under directory: " + pdfPath);
                System.exit(0);
            }

            for (int i = 0; i < refFiles.length; i++) {
                filesToProcess.add(refFiles[i]);
            }
        }
        toBibTexFile(filesToProcess, process, consolidate, bibFile, neo4JHandler);
        neo4JHandler.closeSession();
    }
    private static void toBibTexFile(List<File> filesToProcess, String process, int consolidate, File bibFile, Neo4jHandler neo4jHandler){
        GrobidCaller caller = new GrobidCaller();
        try {
            for (File fileToProcess : filesToProcess) {
                //String result = caller.runGrobidToBibTex(fileToProcess, process, consolidate);
                String result = null;
                caller.grobidToObjects(fileToProcess, process, consolidate, neo4jHandler);
                if (!bibFile.exists() || bibFile.isFile())
                    FileUtils.writeStringToFile(bibFile, result, "UTF-8");
                else {
                    File theBibFile = new File(bibFile.getPath() + "/" +
                            fileToProcess.getName().replace(".pdf", ".bib").replace(".PDF", ".bib"));
                    FileUtils.writeStringToFile(theBibFile, result, "UTF-8");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
