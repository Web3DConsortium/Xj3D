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

package org.chefx3d.view.awt.gt2d;

// External Imports
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

// Local imports
import org.chefx3d.model.Entity;
import org.chefx3d.model.SegmentableEntity;
import org.chefx3d.model.SegmentSequence;
import org.chefx3d.model.SegmentVertex;
import org.chefx3d.model.Segment;

/**
 * Renders a vertex or set of vertices to screen, with the option of custom
 * colouring.
 *
 * @author Russell Dodds
 * @version $Revision: 1.14 $
 */
public class VertexToolRenderer extends AbstractToolRenderer {

    /** Debugging flag to show the area that is responsible for being selected */
    private static final boolean DISPLAY_SELECTION_AREAS = false;

    /** The default width of the renderer in pixels */
    private static final int DEFAULT_WIDTH = 10;

    /** The default height of the renderer in pixels */
    private static final int DEFAULT_HEIGHT = 10;

    /** Pixel size of a vertex box */
    private static final int VERTEX_PIXEL_SIZE = 10;

    private static final Color DEFAULT_VERTEX_COLOR = Color.ORANGE;
    private static final Color DEFAULT_LINE_COLOR = Color.WHITE;
    private static final Color DEFAULT_HIGHLIGHT_COLOR = Color.YELLOW;

    /** The vertex highlight color */
    private Color highlightColor;

    /** The vertex color */
    private Color vertexColor;

    /** The line color */
    private Color lineColor;

    /**
     * Construct a default instance with default rendering colours.
     */
    public VertexToolRenderer() {
        this(DEFAULT_LINE_COLOR, DEFAULT_VERTEX_COLOR, DEFAULT_HIGHLIGHT_COLOR);
    }

    /**
     * Contruct an instance with a specified set of colours.
     *
     * @param lineColor The line color
     * @param vertexColor The vertex color
     * @param highlightColor The vertex highlight color
     */
    public VertexToolRenderer(Color lineColor,
                              Color vertexColor,
                              Color highlightColor) {
        super(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        this.highlightColor = highlightColor;
        this.vertexColor = vertexColor;
        this.lineColor = lineColor;
    }

    //----------------------------------------------------------
    // Methods required by ToolRenderer
    //----------------------------------------------------------

    /**
     * Draw the entity icon to the screen
     *
     * @param g2d The graphics object used to draw
     * @param eWrapper The entity to draw
     */
    @Override
    public void draw(Graphics2D g2d, GTEntityWrapper eWrapper) {

        Color origColor = g2d.getColor();

        int screenPos[] = new int[2];
        int lastPos[] = new int[2];

        Entity entity = eWrapper.getEntity();
        if (entity instanceof SegmentableEntity) {

            SegmentSequence segments = ((SegmentableEntity)entity).getSegmentSequence();
            List<Segment> segmentList = segments.getSegments();

            // get the vertices
            List<SegmentVertex> vertices = segments.getVertices();

            // If we only have one vertex we won't have any segments defined yet.
            // This make sure that we will at least rendering that single vertex
            // to screen.
            if(vertices.size() == 1) {
                SegmentVertex vtx =  vertices.get(0);
                eWrapper.convertWorldPosToScreenPos(vtx.getPosition(), screenPos);

                // Draw the main entity
                g2d.setColor(vertexColor);
                g2d.fillOval(screenPos[0] - VERTEX_PIXEL_SIZE / 2,
                             screenPos[1] - VERTEX_PIXEL_SIZE / 2,
                             VERTEX_PIXEL_SIZE,
                             VERTEX_PIXEL_SIZE);
            } else {
                for (Segment segment : segmentList) {
                    int startVertexID = segment.getStartIndex();
                    int endVertexID = segment.getEndIndex();
                    SegmentVertex startVertex = segments.getVertex(startVertexID);
                    SegmentVertex endVertex = segments.getVertex(endVertexID);

                    eWrapper.convertWorldPosToScreenPos(startVertex.getPosition(), lastPos);
                    eWrapper.convertWorldPosToScreenPos(endVertex.getPosition(), screenPos);

                    // Draw the main entity
                    g2d.setColor(vertexColor);
                    g2d.fillOval(screenPos[0] - VERTEX_PIXEL_SIZE / 2,
                            screenPos[1] - VERTEX_PIXEL_SIZE / 2,
                            VERTEX_PIXEL_SIZE,
                            VERTEX_PIXEL_SIZE);

                    g2d.fillOval(lastPos[0] - VERTEX_PIXEL_SIZE / 2,
                            lastPos[1] - VERTEX_PIXEL_SIZE / 2,
                            VERTEX_PIXEL_SIZE,
                            VERTEX_PIXEL_SIZE);

                    g2d.setColor(lineColor);
                    g2d.drawLine(lastPos[0], lastPos[1], screenPos[0], screenPos[1]);

                    if (DISPLAY_SELECTION_AREAS) {
                        g2d.setColor(Color.RED);
                        int RECT_HEIGHT = 10;

                        int[] x = new int[4];
                        int[] y = new int[4];

                        float slope = ((float)screenPos[1] - lastPos[1]) /
                                ((float)screenPos[0] - lastPos[0]);

                        int xmod;
                        int ymod;

                        // TODO: Should really calculate perpendicular to the line
                        if ((Math.abs(slope)) > 0.5f) {
                            xmod = RECT_HEIGHT / 2 + Math.round(VERTEX_PIXEL_SIZE / 2);
                            ymod = 0;
                        } else {
                            xmod = 0;
                            ymod = RECT_HEIGHT / 2 + Math.round(VERTEX_PIXEL_SIZE / 2);
                        }

                        x[0] = lastPos[0] - xmod;
                        y[0] = lastPos[1] - ymod;

                        x[1] = lastPos[0] + xmod;
                        y[1] = lastPos[1] + ymod;

                        x[2] = screenPos[0] + xmod;
                        y[2] = screenPos[1] + ymod;

                        x[3] = screenPos[0] - xmod;
                        y[3] = screenPos[1] - ymod;

                        g2d.drawPolygon(x,y,x.length);
                    }
                }
            }
        }

        g2d.setColor(origColor);
    }

    /**
     * Draw the entity's selection representation to the screen. Overrides the
     * default behaviour because we don't want the selection box drawn, just
     * the vertex or segment.
     *
     * @param g2d The graphics object used to draw
     * @param eWrapper The entity to draw
     */
    @Override
    public void drawSelection(Graphics2D g2d, GTEntityWrapper eWrapper) {
        Color origColor = g2d.getColor();

        Entity entity = eWrapper.getEntity();

        int selectedVertex = ((SegmentableEntity)entity).getSelectedVertexID();
        int highlightedVertex = ((SegmentableEntity)entity).getHighlightedVertexID();
        int selectedSegment = ((SegmentableEntity)entity).getSelectedSegmentID();

        // First find the bounds of the path
        int screenPos[] = new int[2];

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        SegmentSequence segments = ((SegmentableEntity)entity).getSegmentSequence();
        List<SegmentVertex> vertices = segments.getVertices();

        for (SegmentVertex vtx : vertices) {
            eWrapper.convertWorldPosToScreenPos(vtx.getPosition(), screenPos);

            if (screenPos[0] < minX) {
                minX = screenPos[0];
            }
            if (screenPos[0] > maxX) {
                maxX = screenPos[0] + width;
            }
            if (screenPos[1] < minY) {
                minY = screenPos[1];
            }
            if (screenPos[1] > maxY) {
                maxY = screenPos[1] + height;
            }
        }

        // If no sub-objects are selected, just draw the bounding box.
        if ((selectedVertex == -1) && (((SegmentableEntity)entity).getSelectedSegmentID() == -1)) {
            g2d.setColor(SELECTION_COLOR);
            g2d.setStroke(SELECTION_STROKE);

            int width = maxX - minX;
            int height = maxY - minY;

            g2d.drawRect(minX - SELECTION_OFFSET,
                         minY - SELECTION_OFFSET,
                         width + SELECTION_OFFSET * 2,
                         height + SELECTION_OFFSET * 2);
        } else {
            if(selectedSegment != -1) {
                Segment segment = segments.getSegment(selectedSegment);
                int[] lastPos = new int[2];

                int startVertexID = segment.getStartIndex();
                int endVertexID = segment.getEndIndex();

                SegmentVertex startVertex = segments.getVertex(startVertexID);
                SegmentVertex endVertex = segments.getVertex(endVertexID);

                eWrapper.convertWorldPosToScreenPos(startVertex.getPosition(), lastPos);
                eWrapper.convertWorldPosToScreenPos(endVertex.getPosition(), screenPos);

                g2d.setColor(SELECTION_COLOR);
                g2d.setStroke(SELECTION_STROKE);
                g2d.drawLine(lastPos[0], lastPos[1], screenPos[0], screenPos[1]);
            }

            if(highlightedVertex != -1) {
                g2d.setColor(highlightColor);
                g2d.setStroke(SELECTION_STROKE);
                SegmentVertex vtx = vertices.get(highlightedVertex);
                eWrapper.convertWorldPosToScreenPos(vtx.getPosition(), screenPos);

                g2d.drawRect(screenPos[0] - SELECTION_OFFSET - VERTEX_PIXEL_SIZE / 2,
                             screenPos[1] - SELECTION_OFFSET - VERTEX_PIXEL_SIZE / 2,
                             VERTEX_PIXEL_SIZE + SELECTION_OFFSET * 2,
                             VERTEX_PIXEL_SIZE + SELECTION_OFFSET * 2);
            }

            if(selectedVertex != -1) {
                g2d.setColor(SELECTION_COLOR);
                g2d.setStroke(SELECTION_STROKE);
                SegmentVertex vtx = vertices.get(selectedVertex);
                eWrapper.convertWorldPosToScreenPos(vtx.getPosition(), screenPos);


                g2d.drawRect(screenPos[0] - SELECTION_OFFSET - (VERTEX_PIXEL_SIZE / 2),
                             screenPos[1] - SELECTION_OFFSET - (VERTEX_PIXEL_SIZE / 2),
                             VERTEX_PIXEL_SIZE + (SELECTION_OFFSET * 2),
                             VERTEX_PIXEL_SIZE + (SELECTION_OFFSET * 2));
            }
        }

        g2d.setColor(origColor);
    }
}
