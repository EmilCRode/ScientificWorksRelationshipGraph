package org.ScientificWorksRelationshipGraph;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Main {

    /**
     *
     */
    public static void main(String[] args) throws IllegalAccessException {

       // if (true) {return;};
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
        toNeo4J(filesToProcess, process, consolidate, bibFile, neo4JHandler);
        neo4JHandler.closeSession();
    }
    private static void toNeo4J(List<File> filesToProcess, String process, int consolidate, File bibFile, Neo4jHandler neo4jHandler){
        GrobidCaller caller = new GrobidCaller();
        try {
            for (int i = 0; i < filesToProcess.size(); i++) {
                String result = null;
                caller.grobidToObjects(filesToProcess.get(i), consolidate, neo4jHandler);
                System.out.println("Processing file nr.: " + i + " [" + filesToProcess.get(i).getPath() + "]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void fieldTest() throws IllegalAccessException{
        Neo4jHandler neo4JHandler = new Neo4jHandler();Entity entity = new Work();
        Field[] entityAttributes = entity.getClass().getDeclaredFields();
        for (Field currentField: entityAttributes) {
            currentField.setAccessible(true);
        }
        for(Entity currentEntity: neo4JHandler.findAll(entity.getClass())){
            System.out.println("### Entity: ###");
            for(int i = 0; i < entityAttributes.length; i++) {
                Field field = entityAttributes[i];
                System.out.println(field.getName() + ": "+ field.get(currentEntity));
            }
            System.out.println("### End of Entity ###");
        }
        for (Field currentField: entityAttributes) {
            currentField.setAccessible(false);
        }
        neo4JHandler.closeSession();
    }
}
