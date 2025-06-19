/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.model;

// External Imports
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Internal Imports
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;
import org.w3c.dom.Document;

/**
 * A list of segments is a sequence.
 *
 * @author Russell Dodds
 * @version $Revision: 1.26 $
 */
public class SegmentSequence {

    /** Message when the user makes a request with an invalid vertex ID */
    private static final String INVALID_VERTEX_MSG =
        "Invalid vertex index passed: ";

    /** The properties of the segment */
    private Map<String, Document> segmentDefaults;

    /** The properties of the vertex */
    private Map<String, Document> vertexDefaults;

    /** The transform for the sequence */
    private AffineTransform transform;

    /** A list of vertexIDs */
    private List<Integer> vertexIDs;

    /** A list of segmentIDs */
    private List<Integer> segmentIDs;

    /** A list of vertices identified by an ID */
    private Map<Integer, SegmentVertex> vertices;

    /** A list of segments identified by an ID */
    private Map<Integer, Segment> segments;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /**
     * Create a new sequence of vertices that represents
     *  a series of lines
     *
     * @param segmentDefaults The tool used to create the sequence
     * @param vertexDefaults The properties of this sequence
     */
    public SegmentSequence(
            Map<String, Document> segmentDefaults,
            Map<String, Document> vertexDefaults) {

        transform = new AffineTransform();

        vertexIDs = new ArrayList<>();
        vertices = new HashMap<>();

        segmentIDs = new ArrayList<>();
        segments = new HashMap<>();

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        this.segmentDefaults = segmentDefaults;
        this.vertexDefaults = vertexDefaults;

    }

    /**
     * Add a segment.
     *
     * @param segmentID The segmentID to add
     * @param startVertexID The starting vertex ID
     * @param endVertexID The ending vertex ID
     */
    public void addSegment(int segmentID, int startVertexID, int endVertexID) {

        // clone all segmentProperties
        Map<String, Document> segmentProperties = new HashMap<>();
        Iterator<Map.Entry<String, Document>> itr =
            segmentDefaults.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry<String, Document> entry = itr.next();
            segmentProperties.put(entry.getKey(),
                    (Document) entry.getValue().cloneNode(true));
        }

        // create the vertex class
        Segment segment = new Segment(segmentID, startVertexID, endVertexID,
                    segmentProperties);

        // Add the segment to the lists
        segments.put(segmentID, segment);
        segmentIDs.add(segmentID);

    }

    /**
     * Splits a segment
     *
     * @param segmentID The segmentID to add
     * @param vertexID The starting vertex ID
     */
    public void splitSegment(int segmentID, int vertexID) {

        // increments all segments ID's above this one by one
        int len = segments.size();
        Segment segment;

        for(int i = len-1; i >= segmentID+1; i--) {
            segment = segments.get(i);
            int newSegmentID = i + 1;
            segment.setSegmentID(newSegmentID);
            segments.put(newSegmentID, segment);
        }

        segment = segments.get(segmentID);
        //System.out.println("Changing end index of: " + segmentID + " new segment: start: " + segment.getStartIndex() + " end: " + segment.getEndIndex());
        int oldEndID = segment.getEndIndex();
        segment.setEndIndex(vertexID);

        //System.out.println("seg 0: " + segment.getSegmentID() + " start: " + segment.getStartIndex() + " end: " + segment.getEndIndex());
        // get the segment properties to use

        // clone all segmentProperties
        Map<String, Document> segmentProperties = new HashMap<>();
        Iterator<Map.Entry<String, Document>> itr =
            segmentDefaults.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry<String, Document> entry = itr.next();
            segmentProperties.put(entry.getKey(),
                    (Document) entry.getValue().cloneNode(true));
        }

        // create the segment class
        segment = new Segment(segmentID+1, vertexID, oldEndID,
                    segmentProperties);

        //System.out.println("seg 1: " + segment.getSegmentID() + " start: " + segment.getStartIndex() + " end: " + segment.getEndIndex());

        // Need to reorder?

        //System.out.println("Put new segment: " + (segmentID+1));
        // Add the segment to the lists
        segments.put(segmentID+1, segment);

        // Dump the list and regenerate
        segmentIDs.clear();

        len = segments.size();

        //System.out.println("recreating ID list: " + len);
        for(int i=0; i < len ; i++) {
            segment = segments.get(i);
            segmentIDs.add(segment.getSegmentID());
        }

    }

    /**
     * Add a new vertex to the end of the list
     *
     * @param vertexID The segment line to add
     * @param pos
     */
    public void addVertex(int vertexID, double[] pos) {

//System.out.println("SegmentSequence.addVertex()");

        //if (!contains(pos)) {

            // clone all vertexProperties
            Map<String, Document> vertexProperties = new HashMap<>();
            Iterator<Map.Entry<String, Document>> itr =
                vertexDefaults.entrySet().iterator();

            while (itr.hasNext()) {
                Map.Entry<String, Document> entry = itr.next();
                vertexProperties.put(entry.getKey(),
                        (Document) entry.getValue().cloneNode(true));
            }

            // create the vertex class
            SegmentVertex vertex = new SegmentVertex(vertexID, pos,
                        new float[] {0 ,1, 0, 0}, vertexProperties);

            // Add the vertex to the lists
            vertices.put(vertexID, vertex);
            vertexIDs.add(vertexID);
        //}

    }

    /**
     * Move a vertex to a new position
     *
     * @param vertexID The vertexID
     * @param position The world position
     * @return the the vertextID
     */
    public int moveVertex(int vertexID, double[] position) {

        if (vertexIDs.contains(vertexID)) {

            SegmentVertex vertex = vertices.get( vertexID);
            vertex.setPosition(position);

        }

        return vertexID;
    }

    /**
     * Update a vertex with new property value
     *
     * @param vertexID The vertexID
     * @param propertySheet
     * @param propertyName
     * @param newValue
     */
    public void updateVertex(int vertexID, String propertySheet,
            String propertyName, String newValue) {

        if (vertexIDs.contains(vertexID)) {

            SegmentVertex vertex = vertices.get( vertexID);
            vertex.setProperties(propertySheet, propertyName, newValue);
        }

    }

    /**
     * Remove a vertex from the list
     *
     * @param vertexID The point to remove from the sequence
     */
    public void removeVertex(int vertexID) {

//System.out.println("SegmentSequence.removeVertex()");
//System.out.println("    vertexID: " + vertexID);

        if (vertexIDs.contains(vertexID)) {

            vertices.remove( vertexID);
            vertexIDs.remove( vertexID);
        }

        // now find all the segments this vertex was a part of.
        ArrayList<Integer> removeList = new ArrayList<>();

        Iterator<Segment> itr = segments.values().iterator();
        Segment segment;
        while(itr.hasNext()) {
            segment = itr.next();

            if ((segment.getStartIndex() == vertexID) || (segment.getEndIndex() == vertexID)) {
                removeList.add(segment.getSegmentID());
            }
        }

        // remove them
        Iterator<Integer> itr1 = removeList.iterator();
        Integer checkID;
        while( itr1.hasNext() ) {
            checkID = itr1.next();
            removeSegment(checkID);
        }

        // finally, remove any orphaned vertices
        removeList = new ArrayList<>();
        itr1 = vertexIDs.iterator();
        boolean attached = false;

        while( itr1.hasNext() ) {
            checkID = itr1.next();
            itr = segments.values().iterator();

            while(itr.hasNext()) {
                segment = itr.next();

                if ((segment.getStartIndex() == checkID) || (segment.getEndIndex() == checkID)) {
                    attached = true;
                    break;
                }
            }

            if ((!attached) && (vertexIDs.contains(checkID))) {
                removeList.add( checkID);
            }
        }

        // remove them
        itr1 = removeList.iterator();
        while( itr1.hasNext() ) {
            checkID = itr1.next();
            vertices.remove( checkID);
            vertexIDs.remove( checkID);
        }

    }


    /**
     * Remove a segment from the list
     *
     * @param segmentID The segment to remove from the sequence
     */
    public void removeSegment(int segmentID) {

//System.out.println("SegmentSequence.removeSegment()");

        if (segmentIDs.contains(segmentID)) {
            segments.remove(segmentID);
            segmentIDs.remove(segmentID);
        }
    }

    /**
     * Return the ordered list of vertices based on vertex ID.
     *
     * @return List of positions
     */
    public List<SegmentVertex> getVertices() {

         List<SegmentVertex> orderedList = new ArrayList<>();

        for (Integer vertexID : vertexIDs) {
            SegmentVertex vertex = vertices.get(vertexID);
            orderedList.add(vertex);
        }

        return orderedList;

    }

    /**
     * Return the ordered list of vertices based on segment ordering.
     * Note that this assumes that the entire sequence is assuming a single
     * contiguous line and not a network. If there are no segments yet
     * defined, this will return an empty list;
     *
     * @return List of vertex positions for segments
     */
    public List<SegmentVertex> getSegmentOrderedVertices() {
        List<SegmentVertex> orderedList = new ArrayList<>();

        for (Integer segmentID : segmentIDs) {
            Segment segment = segments.get(segmentID);
            int vtx = segment.getStartIndex();
            orderedList.add(vertices.get(vtx));
        }

        // And add the last vertex as the above loop only adds the first
        // from each segment and assumes that the segment are contiguous.
        if(!segmentIDs.isEmpty()) {
            int last = segmentIDs.size() - 1;

            Segment segment = segments.get(segmentIDs.get(last));
            int vtx = segment.getEndIndex();

            orderedList.add(vertices.get(vtx));
        }

        return orderedList;
    }

    /**
     * Return the ordered list of segments
     *
     * @return List of segments
     */
    public List<Segment> getSegments() {

        List<Segment> orderedList = new ArrayList<>();

        for (Integer segmentID : segmentIDs) {
            Segment segment = segments.get(segmentID);
            orderedList.add(segment);
        }

        return orderedList;
    }

    /**
     * Return the index of the vertex
     *
     * @param vertexId
     * @return The index of the vertex
     */
    public int indexOf(int vertexId) {
        return vertexIDs.indexOf(vertexId);
    }

    /**
     * Get the vertex for the vertexId specified
     *
     * @param vertexID
     * @return The vertex
     */
    public SegmentVertex getVertex(int vertexID) {
        return vertices.get(vertexID);
    }

    /**
     * Get the segment for the segmentID specified
     *
     * @param segmentID
     * @return The segment
     */
    public Segment getSegment(int segmentID) {
        return segments.get(segmentID);
    }

    /**
     * Get the vertexId for the position specified,
     *  returns the ID of the first vertex matched
     *
     * @param pos position
     * @return The vertexId
     */
    public int getVertexID(double[] pos) {

        int vertexId = -1;

        Iterator<Map.Entry<Integer, SegmentVertex>> index =
            vertices.entrySet().iterator();

        while (index.hasNext()) {

            Map.Entry<Integer, SegmentVertex> mapEntry = index.next();

            SegmentVertex vertex = mapEntry.getValue();
            double[] position = vertex.getPosition();

            if ((pos[0] == position[0]) &&
                    (pos[1] == position[1]) &&
                    (pos[2] == position[2])) {

                vertexId = mapEntry.getKey();
                break;
            }

        }

        return vertexId;

    }

    /**
     * Does the sequence contain the position provided
     *
     * @param pos
     * @return True if the position exists
     */
    public boolean contains(double[] pos) {

        int vertexID = getVertexID(pos);

        return (vertexID >= 0);
    }

    /**
     * Get the number of vertices in the sequence
     * @return
     */
    public int getLength() {
        return vertexIDs.size();
    }

    /**
     * Get the position of the vertex
     *
     * @param vertexID
     * @return The position
     * @throws IllegalArgumentException When the vertex ID is less than zero
     *   or greater than the number of vertices in this segment sequence.
     */
    public double[] getVertexPosition(int vertexID)
        throws IllegalArgumentException {

        if(vertexID < 0 || vertexID >= vertices.size())
            throw new IllegalArgumentException(INVALID_VERTEX_MSG + vertexID);

        SegmentVertex vtx = vertices.get(vertexID);
        return vtx.getPosition();

    }

    /**
     * Return true is the vertexID matches the starting ID
     *
     * @param vertexID
     * @return
     */
    public boolean isStart(int vertexID) {
        int startVertexID = vertexIDs.get(0);

        return (startVertexID == vertexID);
    }

    /**
     * Get the first vertex of the sequence
     *
     * @return The position or null if no start
     */
    public double[] getStartPosition() {

        if (vertexIDs.size() <= 0)
            return null;

        int vertexID = vertexIDs.get(0);
        return getVertexPosition(vertexID);

    }

    /**
     * Return true is the vertexID matches the ending ID
     *
     * @param vertexID
     * @return
     */
    public boolean isEnd(int vertexID) {
        int endVertexID = vertexIDs.get(vertexIDs.size() - 1);

        return (endVertexID == vertexID);
    }

    /**
     * Get the last vertex of the sequence
     *
     * @return The position or null if no vertices
     */
    public double[] getEndPosition() {
        int pos = vertexIDs.size();

        // Make start the end if necessary
        if (pos == 0)
            return null;

        int vertexID = vertexIDs.get(pos - 1);
        return getVertexPosition(vertexID);
    }

    /**
     * Get the spacial transformation object
     *
     * @return
     */
    public AffineTransform getTransform() {
        return transform;
    }

    /**
     * Set the spacial transformation object
     *
     * @param transform
     */
    public void setTransform(AffineTransform transform) {
        this.transform = transform;
    }

    /**
     * Get the ID of the last vertex of the sequence. If no verticies are
     * defined, -1 is returned
     *
     * @return The ID of the last vertex of the sequence
     */
    public int getLastVertexID() {

        int lastId;

        if (vertexIDs.isEmpty()) {
            lastId = -1;
        } else {
            lastId = vertexIDs.get(vertexIDs.size() - 1);
        }

        return(lastId);
    }

    /**
     * Generate a new vertex ID that is 1 greater than the last one.
     *
     * @return The next ID available
     */
    public int getNextVertexID() {

        int lastID = getLastVertexID();

        if (lastID < 0) {
            lastID = 0;
        } else {
            lastID++;
        }

        return lastID;
    }

    /**
     * Get the ID of the last segment of the sequence. If no segments are
     * defined, -1 is returned
     *
     * @return The ID of the last vertex of the sequence
     */
    public int getLastSegmentID() {

        int lastID;

        if (segmentIDs.isEmpty()) {
            lastID = -1;
        } else {
            lastID = segmentIDs.get(segmentIDs.size() - 1);
        }

        return lastID;
    }

    /**
     * Generate a new segment ID that is 1 greater than the last one.
     *
     * @return The next ID available
     */
    public int getNextSegmentID() {

        int lastID = getLastSegmentID();

        if (lastID < 0) {
            lastID = 0;
        } else {
            lastID++;
        }

        return lastID;
    }

    /**
     * Register an error reporter with the command instance
     * so that any errors generated can be reported in a nice manner.
     *
     * @param reporter The new ErrorReporter to use.
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

}
