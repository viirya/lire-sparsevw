package net.semanticmetadata.lire.impl;

import at.lux.imageanalysis.ColorLayoutImpl;
import at.lux.imageanalysis.EdgeHistogramImplementation;
import at.lux.imageanalysis.ScalableColorImpl;
import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import java.io.*;
import java.util.*;

/*
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net.
 *
 * Caliph & Emir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Caliph & Emir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caliph & Emir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2010 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/**
 * 
 * <br>Date: 28.04.2010
 * <br>Time: 
 *
 * @author viirya, viirya@gmail.com 
 */
public class RegionSparseVisualWordDocumentBuilder extends AbstractDocumentBuilder {
    private Logger logger = Logger.getLogger(RegionSparseVisualWordDocumentBuilder.class.getName());

    
    public RegionSparseVisualWordDocumentBuilder() {
    }
    
    private String arrayToVisualWordString(int[] vw_dimen, int[] vw_count, String prefix, String postfix, boolean with_quotes) {
        StringBuilder sb = new StringBuilder(1024);
        for (int i = 0; i < vw_dimen.length; i++) {
            if (vw_count[i] > 0) {
                for (int j = 0; j < vw_count[i]; j++) { 
                    if (sb.length() > 0)
                        sb.append(postfix);
                    if (with_quotes)
                        sb.append(prefix + "\"vec");
                    else
                        sb.append(prefix + "vec");
                    if (with_quotes)
                        sb.append(vw_dimen[i] + "\"");
                    else
                        sb.append(vw_dimen[i]);
 
                }
            }
        }
        return sb.toString();
    }                              
 
    private String arrayToVisualWordString(int[] vw_dimen, int[] vw_count, String prefix) {
        return arrayToVisualWordString(vw_dimen, vw_count, prefix, " ", false);
    }                              
 
    public String createStringRepresentation(String vec) {
        return createStringRepresentation(vec, "", " ", 0, true, false);
    }

    public String createStringRepresentation(String vec, String prefix, int threshold) {
        return createStringRepresentation(vec, prefix, " ", threshold, true, false);
    }
     
    public String createStringRepresentation(String vec, String prefix, String postfix, int threshold, boolean type_index, boolean with_quotes) {
        if (vec == null)
          return null;

        StringTokenizer tokenizer = new StringTokenizer(vec,":,");

        int[] dim_array = new int[tokenizer.countTokens() / 2];
        int[] count_array = new int[tokenizer.countTokens() / 2]; 
        int count = 0;
        while (tokenizer.hasMoreTokens()) {
            int dimension = Integer.parseInt(tokenizer.nextToken());
            if (tokenizer.hasMoreTokens()) {
                int vw_count = Integer.parseInt(tokenizer.nextToken());
                if (vw_count >= threshold) { 
                    dim_array[count] = dimension; 
                    if (type_index)
                        count_array[count++] = vw_count;
                    else
                        count_array[count++] = 1;
                }
            }
        }

        String index_feature = arrayToVisualWordString(dim_array, count_array, prefix, postfix, with_quotes);

        return index_feature;
    }

    public Document createDocument(BufferedImage image, String identifier) {
        return null;
    }

    public Document createDocument(String feature_vector, String identifier, int threshold) {
        assert (feature_vector != null);
        System.out.println("Starting to parse feature vector.");

        //StringTokenizer tokenizer = new StringTokenizer(feature_vector," %");
        //String raw_feature_vector = tokenizer.nextToken();
        //String identifier = tokenizer.nextToken();
        String raw_feature_vector = feature_vector;
        String index_feature = createStringRepresentation(raw_feature_vector, "", " ", threshold, false, false);
        String query_feature = createStringRepresentation(raw_feature_vector, "", " ", threshold, false, false);

        //System.out.println("index: " + index_feature);
        //System.out.println("query: " + query_feature);
        
        Document doc = new Document();

        if (index_feature != null) {
            doc.add(new Field(DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_SPARSE_VISUAL_WORDS, index_feature, Field.Store.NO, Field.Index.ANALYZED));
            doc.add(new Field(DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_SPARSE_VISUAL_WORDS_RAW, raw_feature_vector, Field.Store.YES, Field.Index.NO));
        }
             

        if (identifier != null)
            doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.NOT_ANALYZED));

        return doc;
    }
}
