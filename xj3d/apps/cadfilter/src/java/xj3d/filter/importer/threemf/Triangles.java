/*
 * ****************************************************************************
 *  *                        Shapeways Copyright (c) 2015
 *  *                               Java Source
 *  *
 *  * This source is licensed under the GNU LGPL v2.1
 *  * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *  *
 *  * This software comes with the standard NO WARRANTY disclaimer for any
 *  * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *  *
 *  ****************************************************************************
 */

package xj3d.filter.importer.threemf;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Triangle Container.
 *
 * @author Alan Hudson
 */
public class Triangles implements ThreeMFElement {
    public static final int ELEMENT_ID = ThreeMFElementFactory.TRIANGLES;
    private static final int DEFAULT_SIZE = 1000;

    private int[] tris;
    private int[] pindex;
    private int[] pid;
    private int count;

    private boolean hasProperties;

    private Map<Integer, List<int[]>> triMap = new HashMap<>();

    public Triangles(Attributes atts) {

        tris = new int[DEFAULT_SIZE * 3];
        pindex = new int[DEFAULT_SIZE * 3];
        pid = new int[DEFAULT_SIZE];
    }

    public Set<Integer> getProperties() {

        return triMap.keySet();
    }
    
    public void addTriangle(Attributes atts) {
        String v1_st = atts.getValue("v1");
        String v2_st = atts.getValue("v2");
        String v3_st = atts.getValue("v3");

        int v1 = Integer.parseInt(v1_st);
        int v2 = Integer.parseInt(v2_st);
        int v3 = Integer.parseInt(v3_st);

        String p1_st = atts.getValue("p1");
        String p2_st = atts.getValue("p2");
        String p3_st = atts.getValue("p3");

        int p1 = -1;
        if (p1_st != null) {
            p1 = Integer.parseInt(p1_st);
            hasProperties = true;
        }
        int p2 = -1;
        if (p2_st != null) {
            p2 = Integer.parseInt(p2_st);
            hasProperties = true;
        }
        int p3 = -1;
        if (p3_st != null) {
            p3 = Integer.parseInt(p3_st);
            hasProperties = true;
        }

        int pid = -1;
        String pid_st = atts.getValue("pid");
        if (pid_st != null) pid = Integer.parseInt(pid_st);

        // Change pid to base(-1) if p1 is missing
        if (p1 == -1) {
            pid = -1;
        }

        addTriangle(v1,v2,v3,p1,p2,p3,pid);
    }

    public void addTriangle(int v1,int v2, int v3, int p1,int p2, int p3, int pid) {

        List<int[]> tlist = triMap.get(pid);
        if (tlist == null) {
            tlist = new ArrayList<>(DEFAULT_SIZE);
            triMap.put(pid,tlist);
        }
        tlist.add(new int[] {v1,v2,v3,p1,p2,p3});

        count++;
    }

    public List<int[]> getTris(int pid) {
        return triMap.get(pid);
    }

    public int getCount() {
        return count;
    }

    @Override
    public void addElement(ThreeMFElement el) {
    }

    @Override
    public int getElementID() {
        return ELEMENT_ID;
    }

    public boolean hasProperties() {
        return hasProperties;
    }
}
