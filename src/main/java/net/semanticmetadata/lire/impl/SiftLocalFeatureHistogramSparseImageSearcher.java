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


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * ...
 * Date: 04.18.2010
 * Time: 
 *
 * @author viirya, viirya@gmail.com
 */
public class SiftLocalFeatureHistogramSparseImageSearcher extends AbstractImageSearcher {
    private Logger logger = Logger.getLogger(getClass().getName());
    private TreeSet<SimpleResult> docs;
    private int maxHits;

    private QueryParser qp = null;
    private QueryParser qp_identifier = null;
    private IndexSearcher isearcher = null;
    private boolean inited = false;
 
    private double session_mag_query = 0.0d;

    private List<String> doc_list = null;
    private List<Double> doc_score_list = null;
 
    public SiftLocalFeatureHistogramSparseImageSearcher(int maxHits) {
        this.maxHits = maxHits;
        docs = new TreeSet<SimpleResult>();
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("Not feasible for local feature histograms.");
    }

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("Not feasible for local feature histograms.");
    }

    public List<Map.Entry<String, Double>> search(int docID, IndexReader reader, double threshold, boolean benchmark) throws IOException {
        String query = reader.document(docID).getValues(DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_SPARSE_VISUAL_WORDS)[0];

        return search(query, reader, threshold, benchmark);
    }

    public List<Map.Entry<String, Double>> searchByIdentifier(String docIdentifier, IndexReader reader, double threshold, boolean benchmark) throws IOException {
        System.out.println("identifier = " + docIdentifier);
        
        if (inited != true) { 
            qp_identifier = new QueryParser(Version.LUCENE_30, DocumentBuilder.FIELD_NAME_IDENTIFIER, new WhitespaceAnalyzer());
            isearcher = new IndexSearcher(reader);
        }

        /*
        QueryParser qp = new QueryParser(Version.LUCENE_30, DocumentBuilder.FIELD_NAME_IDENTIFIER, new WhitespaceAnalyzer());
        IndexSearcher isearcher = new IndexSearcher(reader);
        */

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


    public List<Map.Entry<String, Double>> searchByRawFeatures(String feature_vec, IndexReader reader, double threshold, boolean benchmark) throws IOException {
        SparseVisualWordDocumentBuilder builder = DocumentBuilderFactory.getSparseVisualWordDocumentBuilder();
        String features = builder.createStringRepresentation(feature_vec);
        if (features != null)
            return search(features, reader, threshold, benchmark);

        return null;
    }

    public void init(IndexReader reader) throws IOException {
        qp = new QueryParser(Version.LUCENE_30, DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_SPARSE_VISUAL_WORDS, new SimpleAnalyzer());
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
                //return 1.0f;  //To change body of implemented methods use File | Settings | File Templates.
                return (float) (1d+Math.log((double) numdocs/(double) docfreq));  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public float coord(int i, int i1) {
                return (float)i / i1; 
                //return 1;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
 
        inited = true;
    }

    public List<Map.Entry<String, Double>> search(String feature_vector, IndexReader reader, double threshold, boolean benchmark) throws IOException {

        BooleanQuery.setMaxClauseCount(100000);
        SparseVisualWordDocumentBuilder builder = DocumentBuilderFactory.getSparseVisualWordDocumentBuilder();
        String query = builder.createStringRepresentation(feature_vector, "", threshold);
        if (query == null || query.length() == 0)
            return null;
        System.out.println("query = " + query);

        if (inited != true) { 
            qp = new QueryParser(Version.LUCENE_30, DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_SPARSE_VISUAL_WORDS, new SimpleAnalyzer());
            isearcher = new IndexSearcher(reader);
        }
        
        /*
        BooleanQuery bq = new BooleanQuery();
        String[] terms = query.split(" ");
        TermQuery tq = null;
        for (String term: terms) {
            System.out.println("term: " + term);
            tq = new TermQuery(new Term(DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_SPARSE_VISUAL_WORDS, term)); 
            bq.add(tq, BooleanClause.Occur.SHOULD);
        }
        System.out.println("query = " + bq.toString());
        */

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
            
            //lucene_sorted_ret = new Vector<Map.Entry<String, Double>>(lucene_ret.entrySet());
            /*
            System.out.println("start to sort.....");
            java.util.Collections.sort(lucene_sorted_ret, new Comparator<Map.Entry<String, Double>>() {
                public int compare(Map.Entry<String, Double> entry, Map.Entry<String, Double> entry1) {
                    // Return 0 for a match, -1 for less than and +1 for more then
                    return (entry.getValue().equals(entry1.getValue()) ? 0 : (entry.getValue() > entry1.getValue() ? -1 : 1));
                }
            });
            */

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return lucene_sorted_ret; //calculateDistance(feature_vector, ret);

    }


    private List<Map.Entry<String, Double>> calculateDistance(String query_vec, HashMap topDocs) {

        StringTokenizer tokenizer = new StringTokenizer(query_vec, ":,");
         
        HashMap query = new HashMap();
        while (tokenizer.hasMoreTokens()) {
            query.put(tokenizer.nextToken(), tokenizer.nextToken());
        }
 
        System.out.println("analysing topDocs...");

        Set set = topDocs.entrySet();

        Iterator i = set.iterator();
        HashMap ret = new HashMap();
        while(i.hasNext()){
            Map.Entry me = (Map.Entry)i.next();
            String docID = (String)me.getKey(); 
            //System.out.println(docID);
            String doc_vec = (String)me.getValue();
            //System.out.println(doc_vec);
            tokenizer = new StringTokenizer(doc_vec, ":,");
            HashMap doc = new HashMap();
            while (tokenizer.hasMoreTokens()) {
                doc.put(tokenizer.nextToken(), tokenizer.nextToken());
            }

            double distance = calculateDistance(query, doc);
            //if (distance <= 0.1d)
            //    continue;
            ret.put(docID, new Double(distance));
        }

        session_mag_query = 0.0d;

        /* try to sort the results */
        List<Map.Entry<String, Double>> list = new Vector<Map.Entry<String, Double>>(ret.entrySet());
        java.util.Collections.sort(list, new Comparator<Map.Entry<String, Double>>(){
            public int compare(Map.Entry<String, Double> entry, Map.Entry<String, Double> entry1)
            {
                // Return 0 for a match, -1 for less than and +1 for more then
                return (entry.getValue().equals(entry1.getValue()) ? 0 : (entry.getValue() > entry1.getValue() ? -1 : 1));
            }
        });

        ret.clear();
        /*
        for (Map.Entry<String, Double> entry: list) {
            System.out.println(entry.getValue());
            //ret.put(entry.getKey(), entry.getValue().toString());
        }
        */ 

        return list;

    }

    private double calculateDistance(HashMap query, HashMap doc) {

        Set set = query.entrySet();
        Iterator i = set.iterator();
        double mag_query = 0.0d;
        double mag_doc = 0.0d;
        double dot_product = 0.0d;
        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            String feature_key = (String)me.getKey();
            double query_value = Double.parseDouble((String)me.getValue());
            double doc_value = 0.0d;
            if (doc.containsKey(feature_key)) {
                doc_value = Double.parseDouble((String)doc.get(feature_key));
                dot_product +=  query_value * doc_value;
                
            }
            if (session_mag_query <= 0.0d) 
                mag_query += Math.pow(query_value, 2.0d);
        }

        set = doc.entrySet();
        i = set.iterator();
        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            double doc_value = Double.parseDouble((String)me.getValue());
            mag_doc += Math.pow(doc_value, 2.0d);
        }
        
        if (session_mag_query <= 0.0d) {
            mag_query = Math.sqrt(mag_query);
            session_mag_query = mag_query;
        }
        mag_doc = Math.sqrt(mag_doc);

        return dot_product / (session_mag_query * mag_doc);
        
    }

    private int[] createHistogram(String cls) {
        String[] tmp = cls.split(" ");
        int[] hist = new int[tmp.length];
        for (int i = 0; i < hist.length; i++) {
            hist[i] = Integer.parseInt(tmp[i]);
        }
        return hist;
    }

    private float findSimilar(IndexReader reader, int[] hist) throws IOException {
        float maxDistance = -1f, overallMaxDistance = -1f;
        boolean hasDeletions = reader.hasDeletions();

        // clear result set ...
        docs.clear();

        int docs = reader.numDocs();
        for (int i = 0; i < docs; i++) {
            // bugfix by Roman Kern
            if (hasDeletions && reader.isDeleted(i)) {
                continue;
            }

            Document d = reader.document(i);
            float distance = getDistance(d, hist);
            assert (distance >= 0);
            // calculate the overall max distance to normalize score afterwards
            if (overallMaxDistance < distance) {
                overallMaxDistance = distance;
            }
            // if it is the first document:
            if (maxDistance < 0) {
                maxDistance = distance;
            }
            // if the array is not full yet:
            if (this.docs.size() < maxHits) {
                this.docs.add(new SimpleResult(distance, d));
                if (distance > maxDistance) maxDistance = distance;
            } else if (distance < maxDistance) {
                // if it is nearer to the sample than at least on of the current set:
                // remove the last one ...
                this.docs.remove(this.docs.last());
                // add the new one ...
                this.docs.add(new SimpleResult(distance, d));
                // and set our new distance border ...
                maxDistance = this.docs.last().getDistance();
            }
        }
        return maxDistance;
    }

    private float getDistance(Document d, int[] hist) {
        String[] cls = d.getValues(DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM);
        int[] hist2 = createHistogram(cls[0]);
        float result = 0f;
        for (int i = 0; i < hist2.length; i++) {
            int i1 = hist2[i] - hist[i];
            result += i1 * i1;

        }
        return (float) Math.sqrt(result);
    }


    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("Not feasible for local feature histograms.");
    }
}
