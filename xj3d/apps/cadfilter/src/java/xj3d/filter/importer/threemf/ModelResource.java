/*****************************************************************************
 *                        Shapeways Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/
package xj3d.filter.importer.threemf;

/**
 * A resource object of the Model.
 * All resources share a global unique ID system.
 *
 * @author Alan Hudson
 */
public interface ModelResource {
    int getID();
    void setID(int id);
}
