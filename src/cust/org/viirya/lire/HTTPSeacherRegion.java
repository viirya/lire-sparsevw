
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.AbstractHandler;

import java.io.IOException;
import java.io.*;
import java.util.*;

/**
 * 
 * Date: 28.04.2010
 * Time: 
 *
 * @author Viirya, viirya@gmail.com
 */
public class HTTPSeacherRegion {

    private String index_path = null;
    private IndexReader reader = null;
    private RegionSiftLocalFeatureHistogramSparseImageSearcher searcher = null;

    private int threshold = 0;

    public List<Map.Entry<String, Double>> search(String identifier) throws IOException {
        /*
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(index_path)));
        SiftLocalFeatureHistogramSparseImageSearcher searcher = new SiftLocalFeatureHistogramSparseImageSearcher(maxHits);
        */
        /*
        HashMap topDocs = searcher.searchByIdentifier(identifier, reader);
        Set set = topDocs.entrySet();

        Iterator i = set.iterator();

        while(i.hasNext()){
            Map.Entry me = (Map.Entry)i.next();
            System.out.println(me.getKey() + " : " + me.getValue() );
        }
        */

        List<Map.Entry<String, Double>> list = null;
        if (reader != null && identifier != null)
            list = searcher.searchByIdentifier(identifier, reader, threshold, true);
        
        return list;

    }

    private String toXML(List<Map.Entry<String, Double>> topDocs, int sessionMaxHits) {
        if (topDocs == null)
            return "";

        StringBuilder sb = new StringBuilder(1024);
        sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><results>");

        for (Map.Entry<String, Double> entry: topDocs) {
            if (sessionMaxHits-- == 0)
                break;
            System.out.println(entry.getKey() + ": " + entry.getValue());
            sb.append("<image><id>" + entry.getKey() + "</id><score>" + ((Double)entry.getValue()).toString() + "</score></image>");
        }
        sb.append("</results>");
        return sb.toString();
    }

    public class ServiceHandler extends AbstractHandler {
    
        private HTTPSeacherRegion seacher = null;
        
        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
                Request base_request = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();

                String identifier = base_request.getParameter("id");
                
                int sessionMaxHits = 100;
                String param_Max = base_request.getParameter("max");
                if (param_Max != null) 
                    sessionMaxHits = Integer.parseInt(param_Max);
        
                base_request.setHandled(true);
        
                response.setContentType("text/xml");
                response.setStatus(HttpServletResponse.SC_OK);
        
                String retXML = toXML(search(identifier), sessionMaxHits);
        
                response.getWriter().println(retXML);
        
        }
        
        public void setService(HTTPSeacherRegion instance) {
                seacher = instance;
        }
    }



    public void startService(String port) throws Exception {
        
        Server server = new Server();
        Connector connector = new SocketConnector();
        connector.setPort(Integer.parseInt(port));
        server.setConnectors(new Connector[]{connector});
        
        Handler handler = new ServiceHandler();
        ((ServiceHandler)handler).setService(this);
        server.setHandler(handler);
        
        server.start();
        server.join();

    }

    public void init(String path, int th) throws IOException {
        index_path = path;
        reader = IndexReader.open(FSDirectory.open(new File(index_path)));
        searcher = new RegionSiftLocalFeatureHistogramSparseImageSearcher(100);
        searcher.init(reader);

        threshold = th;
    }


    public static void main (String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: HTTPSeacherRegion [index path] [port] [vw count threshold]");
            System.exit(1);
        }
        
        int threshold = 0;
        if (args.length == 3)
            threshold = Integer.parseInt(args[2]);

        System.out.println("Starting service in port " + args[1] + " with threshold " + threshold);

        HTTPSeacherRegion seacher = new HTTPSeacherRegion();
        seacher.init(args[0], threshold);
        seacher.startService(args[1]);
    }

}
