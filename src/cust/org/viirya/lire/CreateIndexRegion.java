package org.viirya.lire;

import net.semanticmetadata.lire.*;
import net.semanticmetadata.lire.impl.*;

import net.semanticmetadata.lire.utils.FileUtils;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 
 * Date: 28.04.2010
 * Time: 
 *
 * @author Viirya, viirya@gmail.com
 */
public class CreateIndexRegion {

    public static void createIndex(String index_path, String feature_filepath, int threshold) throws IOException {
        RegionSparseVisualWordDocumentBuilder builder = DocumentBuilderFactory.getRegionSparseVisualWordDocumentBuilder();
        IndexWriter iw = new IndexWriter(FSDirectory.open(new File(index_path)), new WhitespaceAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);

        try {
            FileInputStream fstream = new FileInputStream(feature_filepath);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                StringTokenizer tokenizer = new StringTokenizer(strLine," %");
                String raw_feature_vector = null;
                String identifier = null;
                if (tokenizer.countTokens() == 1) {
                    raw_feature_vector = "";
                    identifier = tokenizer.nextToken();
                    //continue;
                } else {
                    raw_feature_vector = tokenizer.nextToken();
                    identifier = tokenizer.nextToken();
                }
                if (raw_feature_vector == null) { // || raw_feature_vector.length() == 0) {
                    System.out.println("Empty feature: " + identifier);
                    continue;
                }

                System.out.println("Indexing " + identifier + " ...");

                Document doc = builder.createDocument(raw_feature_vector, identifier, threshold);
                iw.addDocument(doc);
            }
            in.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        iw.optimize();
        iw.close();
    }

    public static void main (String[] args) throws IOException {
      if (args.length != 3) {
          System.err.println("Usage: CreateIndexRegion [index path] [feature filepath] [vw count threshold]");
          System.exit(1);
      }
      createIndex(args[0], args[1], Integer.parseInt(args[2]));
    }

}
