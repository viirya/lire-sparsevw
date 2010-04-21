
package org.viirya.lire;

import net.semanticmetadata.lire.*;
import net.semanticmetadata.lire.impl.*;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.*;
import java.util.*;

/**
 * 
 * Date: 18.04.2010
 * Time: 
 *
 * @author Viirya, viirya@gmail.com
 */
public class CommandLineSeacher {


    public static void search(String index_path, String identifier, String threshold) throws IOException {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(index_path)));
        SiftLocalFeatureHistogramSparseImageSearcher searcher = new SiftLocalFeatureHistogramSparseImageSearcher(1000);
        /*
        HashMap topDocs = searcher.searchByIdentifier(identifier, reader);
        Set set = topDocs.entrySet();

        Iterator i = set.iterator();

        while(i.hasNext()){
            Map.Entry me = (Map.Entry)i.next();
            System.out.println(me.getKey() + " : " + me.getValue() );
        }
        */
        List<Map.Entry<String, Double>> list = searcher.searchByIdentifier(identifier, reader, Double.parseDouble(threshold), true);
        for (Map.Entry<String, Double> entry: list) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
            //ret.put(entry.getKey(), entry.getValue().toString());
        }

    }

    public static void main (String[] args) throws IOException {
      if (args.length != 3) {
          System.err.println("Usage: CommandLineSeacher [index path] [docIdentifier]");
          System.exit(1);
      }
      search(args[0], args[1], args[2]);
    }

}
