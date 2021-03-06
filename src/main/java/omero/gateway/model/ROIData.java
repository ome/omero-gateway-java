/*
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2019 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import omero.UnloadedCollectionException;
import omero.UnloadedEntityException;
import omero.model.Ellipse;
import omero.model.FolderRoiLink;
import omero.model.Image;
import omero.model.Line;
import omero.model.Mask;
import omero.model.Point;
import omero.model.Polygon;
import omero.model.Polyline;
import omero.model.Rectangle;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.Shape;
import omero.model.Label;

/**
 * Converts the ROI object.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ROIData
    extends DataObject
{

    /** Map hosting the shapes per plane. */
    private TreeMap<ROICoordinate, List<ShapeData>> roiShapes;

    /** Is the object client side. */
    private boolean clientSide;

    /** The folders this ROI is part of */
    private Collection<FolderData> folders = new ArrayList<FolderData>();
    
    /** An optional UUID for the object */
    private String uuid = "";
    
    /** Initializes the map. */
    private void initialize()
    {
        roiShapes = new TreeMap<ROICoordinate, List<ShapeData>>
        (new ROICoordinate());
        Roi roi = (Roi) asIObject();
        List<Shape> shapes = null;
        try {
            shapes = roi.copyShapes();
        } catch(UnloadedEntityException e) {
            // shapes have not been loaded.
        }

        if (shapes == null) return;
        Iterator<Shape> i = shapes.iterator();
        ShapeData s;
        ROICoordinate coord;
        List<ShapeData> data;
        Shape shape;
        while (i.hasNext()) {
            shape = i .next();
            s = null;
            if (shape instanceof Rectangle)
                s = new RectangleData(shape);
            else if (shape instanceof Ellipse)
                s = new EllipseData(shape);
            else if (shape instanceof Point)
                s = new PointData(shape);
            else if (shape instanceof Polyline)
                s = new PolylineData(shape);
            else if (shape instanceof Polygon)
                s = new PolygonData(shape);
            else if (shape instanceof Label)
                s = new TextData(shape);
            else if (shape instanceof Line)
                s = new LineData(shape);
            else if (shape instanceof Mask)
                s = new MaskData(shape);
            if (s != null) {
                coord = new ROICoordinate(s.getZ(), s.getT());
                if (!roiShapes.containsKey(coord)) {
                    data = new ArrayList<ShapeData>();
                    roiShapes.put(coord, data);
                } else data = roiShapes.get(coord);
                data.add(s);
            }
        }
        
        try {
            List<FolderRoiLink> folderLinks = roi.copyFolderLinks();
            if (folderLinks != null) {
                for (FolderRoiLink fl : folderLinks) {
                    folders.add(new FolderData(fl.getParent()));
                }
            }
        }
        catch(UnloadedCollectionException e) {
            // folders haven't been loaded.
        }
     }

    /**
     * Creates a new instance.
     *
     * @param roi The ROI hosted by the component.
     */
    public ROIData(Roi roi)
    {
        super();
        setValue(roi);
        if (roi != null) initialize();
    }

    /** Create a new instance of an ROIData object. */
    public ROIData()
    {
        super();
        setValue(new RoiI());
        roiShapes = new TreeMap<ROICoordinate, List<ShapeData>>
        (new ROICoordinate());
    }

    /**
     * Sets the image for the ROI.
     *
     * @param image See above.
     */
    public void setImage(Image image)
    {
        Roi roi = (Roi) asIObject();
        if (roi == null) 
            throw new IllegalArgumentException("No Roi specified.");
        roi.setImage(image);
        setDirty(true);
    }

    /**
     * Returns the image for the ROI.
     *
     * @return See above.
     */
    public ImageData getImage()
    {
        Roi roi = (Roi) asIObject();
        if (roi == null) 
            throw new IllegalArgumentException("No Roi specified.");
        Image image = roi.getImage();
        if (image == null) return null;
        return new ImageData(image);
    }

    /**
     * Adds ShapeData object to ROIData.
     *
     * @param shape See above.
     */
    public void addShapeData(ShapeData shape)
    {
        Roi roi = (Roi) asIObject();
        if (roi == null) 
            throw new IllegalArgumentException("No Roi specified.");
        ROICoordinate coord = shape.getROICoordinate();
        List<ShapeData> shapeList;
        if (!roiShapes.containsKey(coord))
        {
            shapeList = new ArrayList<ShapeData>();
            roiShapes.put(coord, shapeList);
        }
        else
            shapeList = roiShapes.get(coord);
        shapeList.add(shape);
        roi.addShape((Shape) shape.asIObject());
        setDirty(true);
    }

    /**
     * Removes the ShapeData object from ROIData.
     *
     * @param shape See above.
     */
    public void removeShapeData(ShapeData shape)
    {
        Roi roi = (Roi) asIObject();
        if (roi == null) 
            throw new IllegalArgumentException("No Roi specified.");
        ROICoordinate coord = shape.getROICoordinate();
        List<ShapeData> shapeList = roiShapes.get(coord);
        if (shapeList != null) {
            shapeList.remove(shape);
            roi.removeShape((Shape) shape.asIObject());
            setDirty(true);
        }
    }

    /**
     * Returns the number of planes occupied by the ROI.
     *
     * @return See above.
     * @deprecated Will be removed in future. Does not work as 
     * expected if the ROI contains shapes which are associated 
     * with all planes (Z, C, T == -1)
     */
    @Deprecated
    public int getPlaneCount() { return roiShapes.size(); }

    /**
     * Returns the number of shapes in the ROI.
     * @return See above.
     */
    public int getShapeCount()
    {
        Iterator<ROICoordinate> i = roiShapes.keySet().iterator();
        int cnt = 0;
        List shapeList;
        while(i.hasNext())
        {
            shapeList = roiShapes.get(i.next());
            cnt += shapeList.size();
        }
        return cnt;
    }

    /**
     * Returns a list of all shapes.
     *
     * @return See above.
     */
    public List<ShapeData> getShapes() {
        List<ShapeData> res = new ArrayList<ShapeData>();
        roiShapes.values().stream().forEach(list -> res.addAll(list));
        return res;
    }

    /**
     * Returns a list of shapes on a given plane, including
     * shapes which are not specifically attached to the plane
     * (span over all z planes and/or timepoints).
     *
     * @param z The z-section (-1 to get only shapes which are
     *          available to all z planes).
     * @param t The timepoint (-1 to get only shapes which are
     *          available to all timepoints).
     * @return See above.
     */
    public List<ShapeData> getShapes(int z, int t) {
        return getShapes(z, t, false);
    }

    /**
     * Returns a list of shapes on a given plane.
     *
     * @param z
     *            The z-section (-1 to get only shapes which are
     *            available to all z planes).
     * @param t
     *            The timepoint (-1 to get only shapes which are
     *            available to all timepoints).
     * @param excludeUnspecific Pass true to only get shapes which
     *                          are specifically bound to the given z/t
     *                          plane, excluding shapes which are available
     *                          to all z planes and/or timepoints.
     * @return See above.
     */
    public List<ShapeData> getShapes(int z, int t, boolean excludeUnspecific) {
        List<ShapeData> res = getRoiShapes(z, t);
        if (res == null)
            res = new ArrayList<ShapeData>();
        if (!excludeUnspecific) {
            if (z != -1 || t != -1) {
                List<ShapeData> allZT = getRoiShapes(-1, -1);
                if (allZT != null)
                    res.addAll(allZT);
            }
            if (z != -1 && t != -1) {
                List<ShapeData> allZ = getRoiShapes(-1, t);
                if (allZ != null)
                    res.addAll(allZ);
                List<ShapeData> allT = getRoiShapes(z, -1);
                if (allT != null)
                    res.addAll(allT);
            }
        }
        return res;
    }

    /**
     * Get the ROI shapes of a specific coordinate as
     * new list.
     * The purpose of this method is mostly to avoid
     * accidental modification of the lists within the
     * roiShapes map.
     *
     * @param z The z plane
     * @param t The timepoint
     * @return A new list containing the shapes
     */
    private List<ShapeData> getRoiShapes(int z, int t) {
        ArrayList<ShapeData> res = new ArrayList<ShapeData>();
        List<ShapeData> shapes = roiShapes.get(new ROICoordinate(z, t));
        if (shapes != null)
            res.addAll(shapes);
        return res;
    }

    /**
     * Returns the iterator of the collection of the map.
     *
     * @return See above.
     */
    public Iterator<List<ShapeData>> getIterator()
    {
        return roiShapes.values().iterator();
    }

    /**
     * Return the first plane that the ROI starts on.
     *
     * @return See above.
     * @deprecated Will be removed in future. Does not work as 
     * expected if the ROI contains shapes which are associated 
     * with all planes (Z, C, T == -1)
     */
    @Deprecated
    public ROICoordinate firstPlane() {
        return roiShapes.firstKey();
    }

    /**
     * Returns the last plane that the ROI ends on.
     *
     * @return See above.
     * @deprecated Will be removed in future. Does not work as 
     * expected if the ROI contains shapes which are associated 
     * with all planes (Z, C, T == -1)
     */
    @Deprecated
    public ROICoordinate lastPlane() {
        return roiShapes.lastKey();
    }

    /**
     * Returns an iterator of the Shapes in the ROI in the range [start, end].
     *
     * @param start
     *            The starting plane where the Shapes should reside.
     * @param end
     *            The final plane where the Shapes should reside.
     * @return See above.
     * @deprecated Will be removed in future. Does not work as
     * expected if the ROI contains shapes which are associated
     * with all planes (Z, C, T == -1)
     */
    @Deprecated
    public Iterator<List<ShapeData>> getShapesInRange(ROICoordinate start,
            ROICoordinate end) {
        List<List<ShapeData>> res = new ArrayList<List<ShapeData>>();
        Collection<List<ShapeData>> inRange = roiShapes.subMap(start, end).values();
        if (inRange != null)
            res.addAll(inRange);
        List<ShapeData> allRanges = roiShapes.get(new ROICoordinate(-1, -1));
        if (allRanges != null)
            res.add(allRanges);
        return res.iterator();
    }

    /**
     * Returns <code>true</code> if the object a client-side object,
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean isClientSide() { return clientSide; }

    /**
     * Sets the flag indicating if the object is a client-side object or not.
     *
     * @param clientSide Passed <code>true</code> if it is a client-side object,
     *                   <code>false</code> otherwise.
     */
    public void setClientSide(boolean clientSide)
    {
        this.clientSide = clientSide;
    }

    /**
     * Get the folders this ROI is part of
     * @return See above.
     */
    public Collection<FolderData> getFolders() {
        return folders;
    }

    /**
     * Get the UUID
     * 
     * @return See above
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Set the UUID
     * 
     * @param uuid
     *            The UUID
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
