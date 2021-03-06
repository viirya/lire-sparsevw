package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractImageSearcher;
import net.semanticmetadata.lire.ImageDuplicates;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 01.02.2006
 * <br>Time: 00:17:02
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ParallelImageSearcher extends AbstractImageSearcher {

    private Logger logger = Logger.getLogger(getClass().getName());
    Class descriptorClass;
    String fieldName;
    private int maxHits = 10;
    // private TreeSet<SimpleResult> docs;
    private TreeSet<SimpleResult>[] parDocs;

    public ParallelImageSearcher(int maxHits, Class descriptorClass, String fieldName) {
        this.maxHits = maxHits;
//        docs = new TreeSet<SimpleResult>();
        this.descriptorClass = descriptorClass;
        this.fieldName = fieldName;
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("Not implemented in this searcher");
    }

    public ImageSearchHits[] search(BufferedImage[] image, IndexReader reader) throws IOException {
        logger.finer("Starting extraction.");
        LireFeature[] lireFeature = new LireFeature[image.length];
        SimpleImageSearchHits[] searchHits = new SimpleImageSearchHits[image.length];
        for (int i = 0; i < image.length; i++) {
            BufferedImage img = image[i];
            try {
                lireFeature[i] = (LireFeature) descriptorClass.newInstance();
                // Scaling image is especially with the correlogram features very important!
                BufferedImage bimg = img;
                if (Math.max(img.getHeight(), img.getWidth()) > GenericDocumentBuilder.MAX_IMAGE_DIMENSION) {
                    bimg = ImageUtils.scaleImage(img, GenericDocumentBuilder.MAX_IMAGE_DIMENSION);
                }
                lireFeature[i].extract(bimg);
                logger.fine("Extraction from image finished");

            } catch (InstantiationException e) {
                logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
            } catch (IllegalAccessException e) {
                logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
            }
        }
        float[] distance = findSimilar(reader, lireFeature);
        for (int i = 0; i < distance.length; i++) {
            searchHits[i] = new SimpleImageSearchHits(parDocs[i], distance[i]);
        }
        return searchHits;

    }

    public ImageSearchHits[] search(Document[] doc, IndexReader reader) throws IOException {
        LireFeature[] lireFeature = new LireFeature[doc.length];
        SimpleImageSearchHits[] searchHits = new SimpleImageSearchHits[doc.length];
        for (int i = 0; i < doc.length; i++) {
            Document doc_ = doc[i];
            try {
                lireFeature[i] = (LireFeature) descriptorClass.newInstance();
                String[] cls = doc_.getValues(fieldName);
                if (cls != null && cls.length > 0) {
                    lireFeature[i].setStringRepresentation(cls[0]);
                }
            } catch (InstantiationException e) {
                logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
            } catch (IllegalAccessException e) {
                logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
            }
        }
        float[] distance = findSimilar(reader, lireFeature);
        for (int i = 0; i < distance.length; i++) {
            searchHits[i] = new SimpleImageSearchHits(parDocs[i], distance[i]);
        }
        return searchHits;

    }

    /**
     * @param reader
     * @param lireFeature
     * @return the maximum distance found for normalizing.
     * @throws java.io.IOException
     */
    private float[] findSimilar(IndexReader reader, LireFeature[] lireFeature) throws IOException {
        float[] maxDistance = new float[lireFeature.length];
        float[] overallMaxDistance = new float[lireFeature.length];

        for (int i = 0; i < overallMaxDistance.length; i++) {
            overallMaxDistance[i] = -1f;
            maxDistance[i] = -1f;
        }

        parDocs = new TreeSet[lireFeature.length];
        for (int i = 0; i < parDocs.length; i++) {
            parDocs[i] = new TreeSet<SimpleResult>();

        }

        boolean hasDeletions = reader.hasDeletions();

        // clear result set ...

        int docs = reader.numDocs();
        for (int i = 0; i < docs; i++) {
            // bugfix by Roman Kern
            if (hasDeletions && reader.isDeleted(i)) {
                continue;
            }

            Document d = reader.document(i);
            float[] distance = getDistance(d, lireFeature);
            // calculate the overall max distance to normalize score afterwards
            for (int j = 0; j < distance.length; j++) {
                float f = distance[j];
                if (overallMaxDistance[j] < f) {
                    overallMaxDistance[j] = f;
                }
                // if it is the first document:
                if (maxDistance[j] < 0) {
                    maxDistance[j] = f;
                }
                // if the array is not full yet:
                if (this.parDocs[j].size() < maxHits) {
                    this.parDocs[j].add(new SimpleResult(f, d));
                    if (f > maxDistance[j]) {
                        maxDistance[j] = f;
                    }
                } else if (f < maxDistance[j]) {
                    // if it is nearer to the sample than at least on of the current set:
                    // remove the last one ...
                    this.parDocs[j].remove(this.parDocs[j].last());
                    // add the new one ...
                    this.parDocs[j].add(new SimpleResult(f, d));
                    // and set our new distance border ...
                    maxDistance[j] = this.parDocs[j].last().getDistance();
                }

            }
        }
        return maxDistance;
    }

    private float[] getDistance(Document d, LireFeature[] lireFeature) {
        float[] distance = new float[lireFeature.length];
        LireFeature lf;
        try {
            lf = (LireFeature) descriptorClass.newInstance();
            String[] cls = d.getValues(fieldName);
            if (cls != null && cls.length > 0) {
                lf.setStringRepresentation(cls[0]);
                for (int i = 0; i < lireFeature.length; i++) {
                    distance[i] = lireFeature[i].getDistance(lf);
                }

            } else {
                logger.warning("No feature stored in this document!");
            }
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
        }

        return distance;
    }

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("Not implemented in this searcher");

    }

    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("Not implemented in this searcher");

    }

    public String toString() {
        return "GenericSearcher using " + descriptorClass.getName();
    }
}