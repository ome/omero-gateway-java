/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2025-2026 University of Dundee. All rights reserved.
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
package omero.gateway.facility;

import omero.ServerError;
import omero.api.RenderingEnginePrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ImageData;
import omero.romio.PlaneDef;

import java.awt.image.BufferedImage;

public class RenderFacility extends Facility {
    /**
     * Creates a new instance
     *
     * @param gateway Reference to the {@link Gateway}
     */
    RenderFacility(Gateway gateway) {
        super(gateway);
    }

    /**
     * Get a RenderingEngine for an image.
     *
     * @param ctx                 The security context.
     * @param imageId             The image ID
     * @param initDefaultSettings Flag to create default rendering settings if the
     *                            image doesn't have any for the current user.
     * @return A RenderingEngine for the image
     * @throws DSOutOfServiceException If the connection is broken, or not logged in
     * @throws DSAccessException       If an error occurred while trying to retrieve data from OMERO
     *                                 service.
     */
    public RenderingEnginePrx getRenderingEngine(SecurityContext ctx, long imageId,
                                                 boolean initDefaultSettings)
            throws DSOutOfServiceException, DSAccessException, ServerError {
        return getRenderingEngine(ctx, imageId, initDefaultSettings, true);
    }

    /**
     * Gets a RenderingEngine for an image.
     *
     * @param ctx                 The security context.
     * @param imageId             The image ID
     * @param initDefaultSettings Flag to create default rendering settings if the
     *                            image doesn't have any for the current user (if set to false
     *                            and the image doesn't have any rendering settings, null is 
     *                            returned)
     * @param closeRE               Close the rendering engine again
     * @return A RenderingEngine for the image or null if RenderingEngine couldn't be initialised.
     * @throws DSOutOfServiceException If the connection is broken, or not logged in
     * @throws DSAccessException       If an error occurred while trying to retrieve data from OMERO
     *                                 service.
     */
    public RenderingEnginePrx getRenderingEngine(SecurityContext ctx, long imageId,
                                                 boolean initDefaultSettings, boolean closeRE)
            throws DSOutOfServiceException, DSAccessException {
        RenderingEnginePrx re = null;
        try {
            long pixelsId = gateway.getFacility(LoadFacility.class).getImage(ctx, imageId).getDefaultPixels().getId();
            re = gateway.getRenderingService(ctx, pixelsId);
            if (!re.lookupRenderingDef(pixelsId)) {
                if (initDefaultSettings) {
                    re.resetDefaultSettings(true);
                    re.lookupRenderingDef(pixelsId);
                } else {
                    return null;
                }
            }
            re.load();
            return re;
        } catch (Throwable t) {
            handleException(this, t, "Could not load RenderingEngine.");
        } finally {
            if (closeRE && re != null) {
                try {
                    re.close();
                } catch (ServerError e) {
                    // just ignore
                }
            }
        }
        return null;
    }

    /**
     * Tries to determine if an image is an RGB(A) image.
     * (Note: This does not necessarily give the same results as Bioformats isRGB()!
     * FormatReader.isRGB() only checks if the image has more than one channel)
     *
     * @param ctx                 The security context.
     * @param imageId             The image ID
     * @return True if the image is RGB(A)
     * @throws DSOutOfServiceException If the connection is broken, or not logged in
     * @throws DSAccessException       If an error occurred while trying to retrieve data from OMERO
     *                                 service.
     */
    public boolean isRGB(SecurityContext ctx, long imageId) throws DSOutOfServiceException, DSAccessException {
        try {
            ImageData img = gateway.getFacility(LoadFacility.class).getImage(ctx, imageId);
            int nChannels = img.getDefaultPixels().getSizeC();
            if (nChannels < 3 || nChannels > 4)
                return false;
            boolean r = false, g = false, b = false;
            RenderingEnginePrx re = getRenderingEngine(ctx, imageId, true);
            for (int i = 0; i < nChannels; i++) {
                int[] ch = re.getRGBA(i);
                if (!r && ch[0] == 255 && ch[1] == 0 && ch[2] == 0)
                    r = true;
                if (!g && ch[1] == 255 && ch[0] == 0 && ch[2] == 0)
                    g = true;
                if (!b && ch[2] == 255 && ch[0] == 0 && ch[1] == 0)
                    b = true;
            }
            return r && g && b;
        } catch (Throwable t) {
            handleException(this, t, "Could not check image.");
        }
        return false;
    }

    /**
     * Renders the selected z, t plane of the given image as RGB image.
     * @param ctx                 The security context.
     * @param imageId             The image ID
     * @param z                   The z plane
     * @param t                   The time point
     * @return An RGB image ready to be displayed on screen.
     * @throws DSOutOfServiceException If the connection is broken, or not logged in
     * @throws DSAccessException       If an error occurred while trying to retrieve data from OMERO
     *                                 service.
     */
    public int[] renderPlane(SecurityContext ctx, long imageId, int z, int t)
            throws DSOutOfServiceException, DSAccessException {
        return renderPlane(ctx, imageId, z, t, true);
    }

    /**
     * Renders the selected z, t plane of the given image as RGB image.
     * @param ctx                 The security context.
     * @param imageId             The image ID
     * @param z                   The z plane
     * @param t                   The time point
     * @param closeRE             Close the rendering engine again
     * @return An RGB image ready to be displayed on screen.
     * @throws DSOutOfServiceException If the connection is broken, or not logged in
     * @throws DSAccessException       If an error occurred while trying to retrieve data from OMERO
     *                                 service.
     */
    public int[] renderPlane(SecurityContext ctx, long imageId, int z, int t, boolean closeRE)
            throws DSOutOfServiceException, DSAccessException {
        RenderingEnginePrx re = null;
        try {
            re = getRenderingEngine(ctx, imageId, true, closeRE);
            PlaneDef plane = new PlaneDef(omeis.providers.re.data.PlaneDef.XY, 0,
                    0, z, t, null, -1);
            return re.renderAsPackedInt(plane);
        } catch (Throwable e) {
            handleException(this, e, "Could not render plane.");
        } finally {
            if (closeRE && re != null) {
                try {
                    re.close();
                } catch (ServerError e) {
                    // just ignore
                }
            }
        }
        return null;
    }

    /**
     * Renders the selected z, t plane of the given image as BufferedImage.
     * @param ctx                 The security context.
     * @param imageId             The image ID
     * @param z                   The z plane
     * @param t                   The time point
     * @return A BufferedImage ready to be displayed on screen.
     * @throws DSOutOfServiceException If the connection is broken, or not logged in
     * @throws DSAccessException       If an error occurred while trying to retrieve data from OMERO
     *                                 service.
     */
    public BufferedImage renderPlaneAsBufferedImage(SecurityContext ctx, long imageId, int z, int t)
            throws DSOutOfServiceException, DSAccessException {
        return renderPlaneAsBufferedImage(ctx, imageId, z, t, true);
    }

    /**
     * Renders the selected z, t plane of the given image as BufferedImage.
     * @param ctx                 The security context.
     * @param imageId             The image ID
     * @param z                   The z plane
     * @param t                   The time point
     * @param closeRE             Close the rendering engine again
     * @return A BufferedImage ready to be displayed on screen.
     * @throws DSOutOfServiceException If the connection is broken, or not logged in
     * @throws DSAccessException       If an error occurred while trying to retrieve data from OMERO
     *                                 service.
     */
    public BufferedImage renderPlaneAsBufferedImage(SecurityContext ctx, long imageId, int z, int t,
                                                    boolean closeRE)
            throws DSOutOfServiceException, DSAccessException {
        try {
            ImageData img = gateway.getFacility(LoadFacility.class).getImage(ctx, imageId);
            int[] pixels = renderPlane(ctx, imageId, 0, 0, closeRE);
            int w = img.getDefaultPixels().getSizeX();
            int h = img.getDefaultPixels().getSizeY();
            BufferedImage image = new BufferedImage(w, h,
                    BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, w, h, pixels, 0, w);
            return image;
        } catch (Throwable e) {
            handleException(this, e, "Could not render plane.");
        }
        return null;
    }
}
