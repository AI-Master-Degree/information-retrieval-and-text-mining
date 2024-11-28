package org.example;

import org.apache.lucene.queryparser.classic.ParseException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {
        Charset.defaultCharset();

        if (args.length == 0) {
            wrongArguments();
            return;
        }

        String command = args[0];
        switch (command) {
            case "-index":
                indexing(args);
                break;
            case "-search":
                searching(args);
                break;
            default:
                wrongArguments();
        }
    }

    private static void indexing(String[] args) {
        if (args.length != 3 || !args[1].equals("-directory")) {
            wrongArguments();
            return;
        }

        String pathToDocumentsDir = args[2];
        String pathToOutputIndex = "index";

        try {
            Indexer indexer = new Indexer(pathToOutputIndex);
            if (indexer.indexFiles(pathToDocumentsDir)) {
                System.out.println("Indexed at: " + pathToOutputIndex);
            } else {
                System.out.println("Failed. No files found in directory: " + pathToDocumentsDir);
            }
        } catch (Exception e) {
            System.err.println("Error while indexing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String decodeArgument(String arg) {
        return new String(arg.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    private static void searching(String[] args) {
        if (args.length != 3 || !args[1].equals("-query")) {
            wrongArguments();
            return;
        }

        String query = decodeArgument(args[2]);
        String pathToIndex = "index";

        try {
            Searcher searcher = new Searcher(pathToIndex);
            if (searcher.loadOk()) {
                query = Indexer.eliminateDiacritics(query);

                searcher.searching(query, 5);
            } else {
                System.out.println("Search failed. Index not found at: " + pathToIndex);
            }
        } catch (Exception e) {
            System.err.println("Error during searching: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void wrongArguments() {
        System.out.println("Invalid arguments. Use:");
        System.out.println("  To Index: java -jar target/docsearch-1.0-SNAPSHOT.jar -index -directory <path to docs>");
        System.out.println("  To Search: java -jar target/docsearch-1.0-SNAPSHOT.jar -search -query <keyword>");
    }
}




