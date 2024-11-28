package org.example;

import org.apache.lucene.search.Query;
import org.apache.lucene.analysis.Analyzer;
import java.nio.file.Path;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.DirectoryReader;
import java.io.IOException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryparser.classic.QueryParser;
import java.nio.file.Paths;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;


public class Searcher {

    private static boolean loadOk = false;
    private final Analyzer analyzer;
    private static IndexSearcher searchIndex = null;


    public Searcher(String dirIndex) throws IOException {
        this.analyzer = new RomanianAnalyzer();
        initializeSearcher(dirIndex);
    }

    private void initializeSearcher(String dirIndex) throws IOException {
        Path pathToIndex = Paths.get(dirIndex);
        try (Directory dir = FSDirectory.open(pathToIndex)) {
            loadOk = DirectoryReader.indexExists(dir);
            if (loadOk) {
                DirectoryReader dr = DirectoryReader.open(dir);
                searchIndex = new IndexSearcher(dr);
            }
        }
    }

    public boolean loadOk() {
        return loadOk;
    }

    public void searching(String query, int hits) throws ParseException, IOException {
        if (!loadOk) {
            System.out.println("Index not loaded.");
            return;
        }

        Query q = makeQuery(query);
        TopDocs docs = executingSearch(q, hits);
        printResults(docs);
    }

    private Query makeQuery(String qs) throws ParseException {
        QueryParser queryParser = new QueryParser("Content", analyzer);
        return queryParser.parse(qs);
    }

    private TopDocs executingSearch(Query q, int hits) throws IOException {
        return searchIndex.search(q, hits);
    }

    private void printResults(TopDocs docs) throws IOException {
        if (docs == null || docs.scoreDocs.length == 0) {
            System.out.println("No results found.");
            return;
        }

        System.out.println("Top 5 results:");
        for (ScoreDoc hit : docs.scoreDocs) {
            Document doc = getDoc(hit.doc);
            if (doc != null) {
                System.out.println(doc.get("Filename"));
            }
        }
    }


    private Document getDoc(int docId) throws IOException {
        for (LeafReaderContext leaf : searchIndex.getIndexReader().leaves()) {
            if (docId >= leaf.docBase && docId < leaf.docBase + leaf.reader().maxDoc()) {
                return leaf.reader().document(docId - leaf.docBase);
            }
        }
        return null;
    }
}
