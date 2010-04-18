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
 * <br>Date: 17.04.2010
 * <br>Time: 
 *
 * @author viirya, viirya@gmail.com 
 */
public class SparseVisualWordDocumentBuilder extends AbstractDocumentBuilder {
    private Logger logger = Logger.getLogger(SparseVisualWordDocumentBuilder.class.getName());

    
    public SparseVisualWordDocumentBuilder() {
    }
    

    private String arrayToVisualWordString(int[] iarray, String prefix) {
        StringBuilder sb = new StringBuilder(1024);
        for (int i = 0; i < iarray.length; i++) {
            sb.append(prefix + 'v');
            sb.append(iarray[i]);
            sb.append(' ');
        }
        return sb.toString();
    }                              
 
    public String createStringRepresentation(String vec) {
        return createStringRepresentation(vec, "");
    }
 
    public String createStringRepresentation(String vec, String prefix) {
        if (vec == null)
          return null;

        StringTokenizer tokenizer = new StringTokenizer(vec,":,");

        int[] dim_array = new int[tokenizer.countTokens() / 2];
        int count = 0;
        while (tokenizer.hasMoreTokens()) {
            dim_array[count++] = Integer.parseInt(tokenizer.nextToken()); 
            if (tokenizer.hasMoreTokens())
                tokenizer.nextToken();  
        }

        String index_feature = arrayToVisualWordString(dim_array, prefix); 

        return index_feature;
    }

    public Document createDocument(BufferedImage image, String identifier) {
        return null;
    }

    public Document createDocument(String feature_vector, String identifier) {
        assert (feature_vector != null);
        System.out.println("Starting to parse feature vector.");

        //StringTokenizer tokenizer = new StringTokenizer(feature_vector," %");
        //String raw_feature_vector = tokenizer.nextToken();
        //String identifier = tokenizer.nextToken();
        String raw_feature_vector = feature_vector;
        String index_feature = createStringRepresentation(raw_feature_vector);
        
        Document doc = new Document();

        if (index_feature != null) {
            doc.add(new Field(DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_SPARSE_VISUAL_WORDS, index_feature, Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field(DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_SPARSE_VISUAL_WORDS_RAW, raw_feature_vector, Field.Store.YES, Field.Index.NOT_ANALYZED));
        }
             

        if (identifier != null)
            doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.NOT_ANALYZED));

        return doc;
    }
}
