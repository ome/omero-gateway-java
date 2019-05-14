/*
 *------------------------------------------------------------------------------
 * Copyright (C) 2019 University of Dundee. All rights reserved.
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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

@Test(groups = "unit")
public class ROIDataTest {

    /**
     * Test various combinations of the getShapes method
     */
    @Test
    public void testGetShapes()
    {
        ROIData rd = new ROIData();

        PointData p = new PointData(1,1);
        p.setText("all z t");
        rd.addShapeData(p);

        p = new PointData(1,1);
        p.setZ(5);
        p.setText("all t z=5");
        rd.addShapeData(p);

        p = new PointData(1,1);
        p.setT(5);
        p.setText("all z t=5");
        rd.addShapeData(p);

        p = new PointData(1,1);
        p.setZ(3);
        p.setT(3);
        p.setText("z=3 t=3");
        rd.addShapeData(p);

        Assert.assertEquals(rd.getShapeCount(), 4);

        // all
        List<ShapeData> shapes = rd.getShapes();
        Assert.assertEquals(shapes.size(), 4);

        // only 3/3 specific
        shapes = rd.getShapes(3, 3, true);
        Assert.assertEquals(shapes.size(), 1);

        // 1/1 specific doesn't have a shape
        shapes = rd.getShapes(1, 1, true);
        Assert.assertEquals(shapes.size(), 0);

        // 1/1 doesn't have shape, but includes the allZT
        shapes = rd.getShapes(1, 1);
        Assert.assertEquals(shapes.size(), 1);

        // 3/3 and allZT
        shapes = rd.getShapes(3, 3, false);
        Assert.assertEquals(shapes.size(), 2);

        // same as above
        shapes = rd.getShapes(3, 3);
        Assert.assertEquals(shapes.size(), 2);

        // allT for z=5 and allZT
        shapes = rd.getShapes(5, 3);
        Assert.assertEquals(shapes.size(), 2);

        // allZ for t=5 and allZT
        shapes = rd.getShapes(3, 5);
        Assert.assertEquals(shapes.size(), 2);

        // allZ for t=5
        shapes = rd.getShapes(-1, 5, true);
        Assert.assertEquals(shapes.size(), 1);

        // allT for z=5
        shapes = rd.getShapes(5, -1, true);
        Assert.assertEquals(shapes.size(), 1);

        // allZ for t=5 and allZT
        shapes = rd.getShapes(-1, 5);
        Assert.assertEquals(shapes.size(), 2);

        // allT for z=5 and allZT
        shapes = rd.getShapes(5, -1);
        Assert.assertEquals(shapes.size(), 2);

        // only allZT
        shapes = rd.getShapes(-1, -1);
        Assert.assertEquals(shapes.size(), 1);
    }
}
