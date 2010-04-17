package net.semanticmetadata.lire;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
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
 * (c) 2002-2006 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/**
 * Abstract ImageSearcher, which uses javax.imageio.ImageIO to create a BufferedImage
 * from an InputStream.
 * <p/>
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 01.02.2006
 * <br>Time: 00:13:16
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public abstract class AbstractImageSearcher implements ImageSearcher {
    /**
     * Searches for images similar to the given image. This simple implementation uses
     * {@link ImageSearcher#search(java.awt.image.BufferedImage,org.apache.lucene.index.IndexReader)},
     * the image is read using javax.imageio.ImageIO.
     *
     * @param image  the example image to search for.
     * @param reader the IndexReader which is used to dsearch through the images.
     * @return a sorted list of hits.
     * @throws IOException in case the image could not be read from stream.
     */
    public ImageSearchHits search(InputStream image, IndexReader reader) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(image);
        return search(bufferedImage, reader);
    }

    public ImageSearchHits relevanceFeedback(ImageSearchHits originalSearch, Set<Document> positives, Set<Document> negatives) {
        throw new UnsupportedOperationException("Not implemented yet for this kind of searcher!");
    }
}
