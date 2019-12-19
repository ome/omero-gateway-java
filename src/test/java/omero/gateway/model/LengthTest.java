/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2019 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway.model;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ome.model.units.BigResult;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * Test the conversion of length units
 * @since 5.6
 */
@Test(groups = "unit")
public class LengthTest {

    /**
     * null as a target unit returns the original value.
     */
    @DataProvider(name = "returnOrig")
    public static Object[][] returnOrig() {
        return new Object[][]{
                {null, null},
                {UnitsLength.PIXEL, null},
                {UnitsLength.MICROMETER, null},
        };
    }

    @Test(dataProvider = "returnOrig")
    public void testReturnsOriginal(UnitsLength source, UnitsLength target) throws BigResult {
        PixelsData pixels = new PixelsData();
        LengthI l = new LengthI(1.0, source);
        pixels.setPixelSizeX(l);
        if (pixels.getPixelSizeX(target) != l) {
            throw new RuntimeException("expected orig");
        }
    }

    /**
     * other invalid mappings will return null.
     */
    @DataProvider(name = "returnNull")
    public static Object[][] returnNull() {
        return new Object[][]{
                {null, UnitsLength.PIXEL},
                {null, UnitsLength.MICROMETER},
                {UnitsLength.MICROMETER, UnitsLength.PIXEL},
                {UnitsLength.PIXEL, UnitsLength.MICROMETER},
        };
    }

    @Test(dataProvider = "returnNull")
    public void testDoesNotThrow(UnitsLength source, UnitsLength target) throws BigResult {
        PixelsData pixels = new PixelsData();
        LengthI l = new LengthI(1.0, source);
        pixels.setPixelSizeX(l);
        if (pixels.getPixelSizeX(target) != null) {
            throw new RuntimeException("expected null");
        }
    }

}
