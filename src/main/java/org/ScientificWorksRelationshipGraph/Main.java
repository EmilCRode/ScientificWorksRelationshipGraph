package org.ScientificWorksRelationshipGraph;

import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.util.*;

public class Main {

    /**
     *
     */
    public static void main(String[] args) throws IllegalAccessException {

        Neo4jHandler neo4JHandler = new Neo4jHandler();
        if ((args.length != 1) && (args.length != 2)) {
            System.err.println("usage: command process[header|citation] path-to-pdf-file");
            return;
        }
        String pdfPath = args[0];
        String[] dirs = pdfPath.split("/");
        String discipline = dirs[dirs.length - 2];
        String journal = dirs[dirs.length - 1];
        String consolidation = null;
        int consolidate = 0;

        System.out.print(pdfPath);
        if ((consolidation != null) && (consolidation.equals("1") || consolidation.equals("true")))
            consolidate = 1;
        if ((consolidation != null) && (consolidation.equals("2") ))
            consolidate = 2;

        File pdfFile = new File(pdfPath);

        if (!pdfFile.exists()) {
            System.err.println("Path does not exist: " + pdfPath);
            System.exit(0);
        }

        List<File> filesToProcess = new ArrayList<File>();
        if (pdfFile.isFile()) {
            filesToProcess.add(pdfFile);
        } else if (pdfFile.isDirectory()) {

            File[] refFiles = pdfFile.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".pdf") || name.endsWith(".PDF");
                }
            });

            if (refFiles == null) {
                System.err.println("No PDF file to be processed under directory: " + pdfPath);
                System.exit(0);
            }

            Collections.addAll(filesToProcess, refFiles);
        }
        toNeo4J(filesToProcess, consolidate, neo4JHandler, discipline, journal);
        neo4JHandler.closeSession();

    }
    private static void toNeo4J(List<File> filesToProcess, int consolidate, Neo4jHandler neo4jHandler, String discipline, String journal){
        GrobidCaller caller = new GrobidCaller();
        try {
            int numberOfFiles = filesToProcess.size();
            ProgressBar pb = new ProgressBar("", numberOfFiles).start();
            long startTime;
            for (int i = 0; i < numberOfFiles; i++) {
                pb.step();
                pb.setExtraMessage((filesToProcess.get(i).getName()));
                caller.grobidToObjects(filesToProcess.get(i), consolidate, neo4jHandler, discipline, journal);
            }
            pb.stop();
            neo4jHandler.closeSession();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
