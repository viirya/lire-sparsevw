
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
 * Date: 01.05.2010
 * Time: 
 *
 * @author Viirya, viirya@gmail.com
 */
public class CommandLineSeacherbyFeatureRegion {


    public static void search(String index_path, String feature, int threshold) throws IOException {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(index_path)));
        RegionSiftLocalFeatureHistogramSparseImageSearcher searcher = new RegionSiftLocalFeatureHistogramSparseImageSearcher(1000);

        /*
        HashMap topDocs = searcher.search(feature, reader);
        Set set = topDocs.entrySet();

        Iterator i = set.iterator();

        while(i.hasNext()){
            Map.Entry me = (Map.Entry)i.next();
            System.out.println(me.getKey() + " : " + me.getValue() );
        }
        */

        List<Map.Entry<String, Double>> list = searcher.search(feature, reader, threshold, true);
        for (Map.Entry<String, Double> entry: list) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
            //ret.put(entry.getKey(), entry.getValue().toString());
        }

    }

    public static void main (String[] args) throws IOException {
      if (args.length != 3) {
          System.err.println("Usage: CommandLineSeacherbyFeature [index path] [feature] [threshold]");
          System.exit(1);
      }
      search(args[0], args[1], Integer.parseInt(args[2]));
    }

}
