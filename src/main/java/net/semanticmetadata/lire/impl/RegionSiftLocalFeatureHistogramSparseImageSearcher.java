package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractImageSearcher;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageDuplicates;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.imageanalysis.sift.Feature;
import net.semanticmetadata.lire.DocumentBuilderFactory;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.standard.StandardAnalyzer;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * ...
 * Date: 04.28.2010
 * Time: 
 *
 * @author viirya, viirya@gmail.com
 */
public class RegionSiftLocalFeatureHistogramSparseImageSearcher extends SiftLocalFeatureHistogramSparseImageSearcher {
 
    public RegionSiftLocalFeatureHistogramSparseImageSearcher(int maxHits) {
        super(maxHits);
        //this.maxHits = maxHits;
        //docs = new TreeSet<SimpleResult>();
    }


    public List<Map.Entry<String, Double>> searchByRawFeatures(String feature_vec, IndexReader reader, int threshold, boolean benchmark) throws IOException {
        RegionSparseVisualWordDocumentBuilder builder = DocumentBuilderFactory.getRegionSparseVisualWordDocumentBuilder();
        String features = builder.createStringRepresentation(feature_vec, "", "", threshold, false, false);
        if (features != null)
            return search(features, reader, threshold, benchmark);

        return null;
    }


    public List<Map.Entry<String, Double>> searchByIdentifier(String docIdentifier, IndexReader reader, int threshold, boolean benchmark) throws IOException {
        System.out.println("identifier = " + docIdentifier);
        
        if (inited != true) { 
            qp_identifier = new QueryParser(Version.LUCENE_30, DocumentBuilder.FIELD_NAME_IDENTIFIER, new WhitespaceAnalyzer());
            isearcher = new IndexSearcher(reader);
        }

        String feature_vector = null;
        try {
            TopDocs docs = isearcher.search(qp_identifier.parse('"' + docIdentifier + '"'), 1);
            if (docs != null && docs.totalHits > 0) {
                feature_vector = reader.document(docs.scoreDocs[0].doc).getValues(DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_SPARSE_VISUAL_WORDS_RAW)[0];
                System.out.println("found " + docIdentifier);
            } else
                System.out.println("not found.");

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (feature_vector != null)
            return search(feature_vector, reader, threshold, benchmark);

        return null; 
    }          

    public void init(IndexReader reader) throws IOException {
        qp = new QueryParser(Version.LUCENE_30, DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_SPARSE_VISUAL_WORDS, new WhitespaceAnalyzer());
        qp_identifier = new QueryParser(Version.LUCENE_30, DocumentBuilder.FIELD_NAME_IDENTIFIER, new WhitespaceAnalyzer());
        isearcher = new IndexSearcher(reader);
        
        isearcher.setSimilarity(new Similarity() {
            @Override
            public float lengthNorm(String s, int i) {
                return 1;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public float queryNorm(float v) {
                return 1;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public float sloppyFreq(int i) {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public float tf(float v) {
                return 1.0f;
                //return v;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public float idf(int docfreq, int numdocs) {
                return 1.0f;  //To change body of implemented methods use File | Settings | File Templates.
                //return (float) (1d+Math.log((double) numdocs/(double) docfreq));  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public float coord(int i, int i1) {
                //return (float)i / i1; 
                return 1;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        
        inited = true;
    }

    public List<Map.Entry<String, Double>> search(String feature_vector, IndexReader reader, int threshold, boolean benchmark) throws IOException {

        BooleanQuery.setMaxClauseCount(100000);
        RegionSparseVisualWordDocumentBuilder builder = DocumentBuilderFactory.getRegionSparseVisualWordDocumentBuilder();

        String query = builder.createStringRepresentation(feature_vector, "", " ", threshold, false,false);

        if (query == null || query.length() == 0)
            return null;
        System.out.println("query = " + query);

        if (inited != true) { 
            qp = new QueryParser(Version.LUCENE_30, DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_SPARSE_VISUAL_WORDS, new WhitespaceAnalyzer());
            isearcher = new IndexSearcher(reader);
        }
        
        HashMap ret = new HashMap();
        HashMap lucene_ret = new HashMap();
        List<Map.Entry<String, Double>> lucene_sorted_ret = new Vector<Map.Entry<String, Double>>();
        try {
            long startTime = System.currentTimeMillis();

            Sort sort = new Sort();
            TopDocs docs = isearcher.search(qp.parse(query), reader.numDocs()); //reader.numDocs());  

            /* do not compute similarity */
            //Sort sort = new Sort(SortField.FIELD_DOC);
            //TopDocs docs = isearcher.search(qp.parse(query), null, 5000, sort); //reader.numDocs());
 
            /* using boolean query */
            //TopDocs docs = isearcher.search(bq, maxHits); //reader.numDocs());
 
            long stopTime = System.currentTimeMillis();
            if (benchmark == true)
                System.out.println("Total time: " + (stopTime - startTime) + " ms");
            System.out.println("found: " + docs.scoreDocs.length + " docs.");
            for (int i = 0; i < docs.scoreDocs.length && i < maxHits; i++) {
                //System.out.println("ret " + i + " ");
                if (Float.isNaN(docs.scoreDocs[i].score))
                    continue;
                String doc_id = reader.document(docs.scoreDocs[i].doc).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                String raw_feature = reader.document(docs.scoreDocs[i].doc).getValues(DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_SPARSE_VISUAL_WORDS_RAW)[0];
                //System.out.println(raw_feature);
                //ret.put(doc_id, raw_feature);

                //lucene_ret.put(doc_id, new Double(docs.scoreDocs[i].score));
                //System.out.println(docs.scoreDocs[i].score);
                lucene_sorted_ret.add(new AbstractMap.SimpleEntry<String, Double>(doc_id, new Double(docs.scoreDocs[i].score)));
            }
            
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return lucene_sorted_ret;

    }

}
