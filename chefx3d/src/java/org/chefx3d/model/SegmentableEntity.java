/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2005-2007
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

// Internal Imports

/**
 * Defines whether an Entity is a Multi-Segment object
 *
 * @author Russell Dodds
 * @version $Revision: 1.2 $
 */
public interface SegmentableEntity extends Entity {

    /**
     * @return true if this entity is a line tool
     */
    boolean isLine();

    /**
     * @return true if this entity is fixed length
     */
    boolean isFixedLength();

    /**
     * Add a segment to this tool.
     * @param segmentID
     * @param startVertexID
     * @param endVertexID
     */
    void addSegment(int segmentID, int startVertexID, int endVertexID);

    /**
     * Split a segment with the provided vertex.
     *
     * @param segmentID
     * @param vertexID
     */
    void splitSegment(int segmentID, int vertexID);

    /**
     * Remove a segment from this tool.
     *
     * @param segmentID The position of the segment
     */
    void removeSegment(int segmentID);

    /**
     * Add a segment vertex to this tool.
     *
     * @param vertexID The vertexID
     * @param pos The position of the segment
     */
    void addSegmentVertex(int vertexID, double[] pos);

    /**
     * Move a vertex of this SegmentSequence.
     *
     * @param vertexID The vertexID
     * @param pos The position of the segment
     */
    void moveSegmentVertex(int vertexID, double[] pos);

    /**
     * Update a vertex of this SegmentSequence.
     *
     * @param vertexID The vertexID
     * @param paramSheet
     * @param propertyName
     * @param newValue
     */
    void updateSegmentVertex(int vertexID, String paramSheet,
            String propertyName, String newValue);

    /**
     * Remove a segment vertex from this tool.
     *
     * @param vertexID The segments ID
     */
    void removeSegmentVertex(int vertexID);

    @Override
    boolean isSegmentedEntity();

    /**
     * Set whether this tool is a segmentedEnity.
     *
     * @param isSegment
     */
    void setSegmentedEntity(boolean isSegment);

    /**
     * Set the currently selected vertex index
     *
     * @param vertexID
     */
    void setSelectedVertexID(int vertexID);

    /**
     * Get the currently selected vertex index, -1 if none selected
     *
     * @return selectedVertexID
     */
    int getSelectedVertexID();

    /**
     * Set the currently selected segment index
     *
     * @param segmentID
     */
    void setSelectedSegmentID(int segmentID);

    /**
     * Get the currently selected segment index, -1 if none selected
     *
     * @return selectedSegmentID
     */
    int getSelectedSegmentID();

    /**
     * Set the currently selected vertex index
     *
     * @param vertexID
     */
    void setHighlightedVertexID(int vertexID);

    /**
     * Get the currently selected vertex index, -1 if none selected
     *
     * @return selectedVertexID
     */
    int getHighlightedVertexID();

    /**
     * Get the currently selected vertex null if none selected
     *
     * @return SegmentVertex
     */
    SegmentVertex getSelectedVertex();

    /**
     * Set the currently selected segment index
     *
     * @param segmentID
     */
    void setHighlightedSegmentID(int segmentID);

    /**
     * Get the currently selected segment index, -1 if none selected
     *
     * @return selectedSegmentID
     */
    int getHighlightedSegmentID();

    /**
     * Get the currently selected segment null if none selected
     *
     * @return Segment
     */
    Segment getSelectedSegment();

    /**
     * Get the currently selected vertex position, null if none selected
     *
     * @return vertex position is World Coords
     */
    double[] getSelectedVertexPosition();

    /**
     * Return true if a vertex is currently selected, false otherwise
     *
     * @return selectedVertexID
     */
    boolean isVertexSelected();

    /**
     * Return true if a segment is currently selected, false otherwise
     *
     * @return selectedSegmentID
     */
    boolean isSegmentSelected();

    /**
     * Get the segment sequence for this tool. If it contains no segments it
     * will return null.
     *
     * @return The segments
     */
    SegmentSequence getSegmentSequence();

}