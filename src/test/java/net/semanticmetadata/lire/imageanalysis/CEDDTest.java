package net.semanticmetadata.lire.imageanalysis;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/*
 * This file is part of Caliph & Emir.
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
 * http://www.juggle.at, http://caliph-emir.sourceforge.net
 */
public class CEDDTest extends TestCase {
    private String[] testFiles = new String[]{"img01.jpg", "img02.jpg", "img03.jpg", "img04.jpg", "img05.jpg", "img06.jpg", "img07.jpg", "img08.jpg", "img09.jpg", "img10.jpg"};
    private String testFilesPath = "../lire/src/test/resources/small/";

    public void testExtraction() throws IOException {
        CEDD sch = new CEDD();
        BufferedImage image = ImageIO.read(new FileInputStream(testFilesPath + testFiles[0]));
        System.out.println("image = " + image.getWidth() + " x " + image.getHeight());
        sch.extract(image);
        System.out.println("sch = " + sch.getStringRepresentation());
    }

    public void testRetrieval() throws Exception {
        CEDD[] acc = new CEDD[testFiles.length];
        LinkedList<String> vds = new LinkedList<String>();
        for (int i = 0; i < acc.length; i++) {
            System.out.println("Extracting from number " + i);
            acc[i] = new CEDD();
            acc[i].extract(ImageIO.read(new FileInputStream(testFilesPath + testFiles[i])));
            vds.add(acc[i].getStringRepresentation());
        }

        System.out.println("Calculating distance for " + testFiles[5]);
        for (int i = 0; i < acc.length; i++) {
            float distance = acc[i].getDistance(acc[5]);
            System.out.println(testFiles[i] + " distance = " + distance);
        }
        int count = 0;
        for (Iterator<String> iterator = vds.iterator(); iterator.hasNext();) {
            String s = iterator.next();
            SimpleColorHistogram a = new SimpleColorHistogram();
            a.setStringRepresentation(s);
            float distance = acc[count].getDistance(a);
            System.out.println(testFiles[count] + " distance = " + distance);
            count++;
        }
    }

    public void testSerialization() throws IOException {
        CEDD[] c = new CEDD[testFiles.length];
        LinkedList<String > vds = new LinkedList<String>();
        LinkedList<byte[] > vdb = new LinkedList<byte[]>();
        for (int i = 0; i < c.length; i++) {
            System.out.println("Extracting from number " + i);
            c[i] = new CEDD();
            c[i].extract(ImageIO.read(new FileInputStream(testFilesPath + testFiles[i])));
            vds.add(c[i].getStringRepresentation());
            vdb.add(c[i].getByteArrayRepresentation());
        }

        for (int i = 0; i < c.length; i++) {
            CEDD a = new CEDD(), b= new CEDD();
            a.setByteArrayRepresentation(vdb.get(i));
            b.setStringRepresentation(vds.get(i));
            assertTrue(a.getDistance(b)==0);
        }
    }

}