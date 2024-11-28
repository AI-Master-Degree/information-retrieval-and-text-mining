package org.example;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.analysis.Analyzer;
import java.util.ArrayList;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.lucene.store.FSDirectory;
import java.nio.file.Path;
import org.apache.lucene.document.Document;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.exception.TikaException;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.lucene.analysis.CharArraySet;
import java.text.Normalizer;
import java.util.List;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.lucene.document.TextField;
import java.io.File;
import org.apache.lucene.document.Field;
import org.apache.lucene.store.Directory;
import org.apache.lucene.index.IndexWriter;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.xml.sax.SAXException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;


public class Indexer {

    private static final String STOPWORDS = "./src/main/java/org/example/stop_words_romanian.txt";
    private static IndexWriter writeIndex = null;

    public Indexer(String outputDirectory) throws IOException {
        prepareIndex(outputDirectory);
    }

    private void prepareIndex(String directoryOut) throws IOException {
        Path pathToOutput = Paths.get(directoryOut);
        Directory dir = FSDirectory.open(pathToOutput);
        CharArraySet stopWds = getStopWords();
        Analyzer romAnalyzer = new RomanianAnalyzer(stopWds);
        IndexWriterConfig indexConfig = new IndexWriterConfig(romAnalyzer);
        indexConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writeIndex = new IndexWriter(dir, indexConfig);
    }

    private CharArraySet getStopWords() throws IOException {
        File fileStopWords = new File(STOPWORDS);

        if (!fileStopWords.exists()) {
            throw new IOException("File not found: " + STOPWORDS);
        }

        List<String> stopWds = new ArrayList<>();
        try (FileInputStream input = new FileInputStream(fileStopWords)) {
            BodyContentHandler content = new BodyContentHandler();
            Metadata docMetadata = new Metadata();
            ParseContext docContext = new ParseContext();

            TXTParser txtParser = new TXTParser();
            txtParser.parse(input, content, docMetadata, docContext);

            for (String word : content.toString().split("\n")) {
                stopWds.add(eliminateDiacritics(word.trim()));
            }
        } catch (TikaException | SAXException e) {
            throw new IOException("Error while parsing file.", e);
        }

        return new CharArraySet(stopWds, true);
    }

    private Document makeDocument(String docName, String docPath, String docContent) {
        FieldType typeOfField = new FieldType(TextField.TYPE_NOT_STORED);
        typeOfField.setStored(true);

        Document doc = new Document();
        doc.add(new Field("Filename", docName, typeOfField));
        doc.add(new Field("Path", docPath, typeOfField));
        doc.add(new Field("Content", eliminateDiacritics(docContent), typeOfField));

        return doc;
    }

    public boolean indexFiles(String filesDir) throws IOException {
        File dir = new File(filesDir);
        File[] files = dir.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("No files found in directory: " + filesDir);
            return false;
        }

        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }
            System.out.println("Indexing File: " + file.getCanonicalPath());

            try (FileInputStream input = new FileInputStream(file)) {
                String fileContent = getContent(file, input);
                if (fileContent != null) {
                    Document doc = makeDocument(file.getName(), file.getAbsolutePath(), fileContent);
                    writeIndex.addDocument(doc);
                }
            } catch (Exception e) {
                System.err.println("Error indexing file: " + file.getCanonicalPath());
                e.printStackTrace();
            }
        }

        writeIndex.close();
        return true;
    }

    private String getContent(File file, FileInputStream input) throws IOException {
        BodyContentHandler content = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        String extension = FileNameUtils.getExtension(file.getAbsolutePath()).toLowerCase();
        try {
            switch (extension) {
                case "txt":
                    new TXTParser().parse(input, content, metadata, context);
                    break;
                case "doc":
                case "docx":
                    new OOXMLParser().parse(input, content, metadata, context);
                    break;
                case "pdf":
                    new PDFParser().parse(input, content, metadata, context);
                    break;
                default:
                    System.out.println("Unsupported file type: " + extension);
                    return null;
            }
        } catch (TikaException | SAXException e) {
            System.err.println("Error parsing file: " + file.getName());
            return null;
        }

        return content.toString();
    }

    public static String eliminateDiacritics(String string) {
        if (string == null) {
            return null;
        }
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        return string.replaceAll("\\p{M}", "").replaceAll("[*?]", "");
    }
}
