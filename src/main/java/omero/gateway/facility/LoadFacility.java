/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2023 University of Dundee. All rights reserved.
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
import omero.api.IQueryPrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.WellData;
import omero.model.DatasetI;
import omero.model.ImageI;
import omero.model.PlateI;
import omero.model.ProjectI;
import omero.model.ScreenI;
import omero.model.WellI;
import omero.sys.ParametersI;

/**
 * A Facility for loading basic objects. Note: These are shallow objects with just
 * one level of hierarchy loaded e.g. a project will have a list of datasets,
 * but the datasets only have basic properties like name and id loaded.
 */
public class LoadFacility extends Facility {

    private static String GET_DATASET_QUERY = "select ds from Dataset as ds " +
            "left join fetch ds.imageLinks as l " +
            "left join fetch l.child as i " +
            "where ds.id = :id";

    private static String GET_PROJECT_QUERY = "select p from Project as p " +
            "left join fetch p.datasetLinks as l " +
            "left join fetch l.child as i " +
            "where p.id = :id";

    private static String GET_IMAGE_QUERY = "select i from Image as i " +
            "left join fetch i.pixels as p " +
            "left join fetch p.pixelsType as pt " +
            "where i.id = :id";

    private static String GET_PLATE_QUERY = "select p from Plate as p " +
            "left join fetch p.wells as w " +
            "left join fetch p.plateAcquisitions as pa " +
            "where p.id = :id";

    private static String GET_SCREEN_QUERY = "select s from Screen as s " +
            "left join fetch s.plateLinks as l " +
            "left join fetch l.child as p " +
            "where s.id = :id";

    private static String GET_WELL_QUERY = "select w from Well as w " +
            "left join fetch w.wellSamples as ws " +
            "left join fetch ws.plateAcquisition as pa " +
            "left join fetch ws.image as img " +
            "left join fetch img.pixels as pix " +
            "left join fetch pix.pixelsType as pt " +
            "where w.id = :id";

    /**
     * Creates a new instance
     *
     * @param gateway Reference to the {@link Gateway}
     */
    LoadFacility(Gateway gateway) {
        super(gateway);
    }

    /**
     * Get a dataset
     * @param ctx The SecurityContext
     * @param id The id of the dataset
     * @return The dataset or null if it can't be found
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public DatasetData getDataset(SecurityContext ctx, long id) throws DSOutOfServiceException,
            DSAccessException {
        try {
            IQueryPrx qs = gateway.getQueryService(ctx);
            ParametersI param = new ParametersI();
            param.addId(id);
            DatasetI tmp = (DatasetI) qs.findByQuery(GET_DATASET_QUERY, param);
            if (tmp != null)
                return new DatasetData(tmp);
        } catch (DSOutOfServiceException | ServerError e) {
            handleException(this, e, "Could not get dataset");
        }
        return null;
    }

    /**
     * Get a project
     * @param ctx The SecurityContext
     * @param id The id of the project
     * @return The project or null if it can't be found
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public ProjectData getProject(SecurityContext ctx, long id) throws DSOutOfServiceException,
            DSAccessException {
        try {
            IQueryPrx qs = gateway.getQueryService(ctx);
            ParametersI param = new ParametersI();
            param.addId(id);
            ProjectI tmp = (ProjectI) qs.findByQuery(GET_PROJECT_QUERY, param);
            if (tmp != null)
                return new ProjectData(tmp);
        } catch (DSOutOfServiceException | ServerError e) {
            handleException(this, e, "Could not get project");
        }
        return null;
    }

    /**
     * Get an image
     * @param ctx The SecurityContext
     * @param id The id of the image
     * @return The image or null if it can't be found
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public ImageData getImage(SecurityContext ctx, long id) throws DSOutOfServiceException,
            DSAccessException {
        try {
            IQueryPrx qs = gateway.getQueryService(ctx);
            ParametersI param = new ParametersI();
            param.addId(id);
            ImageI tmp = (ImageI) qs.findByQuery(GET_IMAGE_QUERY, param);
            if (tmp != null)
                return new ImageData(tmp);
        } catch (DSOutOfServiceException | ServerError e) {
            handleException(this, e, "Could not get image");
        }
        return null;
    }

    /**
     * Get a screen
     * @param ctx The SecurityContext
     * @param id The id of the screen
     * @return The screen or null if it can't be found
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public ScreenData getScreen(SecurityContext ctx, long id) throws DSOutOfServiceException,
            DSAccessException {
        try {
            IQueryPrx qs = gateway.getQueryService(ctx);
            ParametersI param = new ParametersI();
            param.addId(id);
            ScreenI tmp = (ScreenI) qs.findByQuery(GET_SCREEN_QUERY, param);
            if (tmp != null)
                return new ScreenData(tmp);
        } catch (DSOutOfServiceException | ServerError e) {
            handleException(this, e, "Could not get screen");
        }
        return null;
    }

    /**
     * Get a plate
     * @param ctx The SecurityContext
     * @param id The id of the plate
     * @return The plate or null if it can't be found
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public PlateData getPlate(SecurityContext ctx, long id) throws DSOutOfServiceException,
            DSAccessException {
        try {
            IQueryPrx qs = gateway.getQueryService(ctx);
            ParametersI param = new ParametersI();
            param.addId(id);
            PlateI tmp = (PlateI) qs.findByQuery(GET_PLATE_QUERY, param);
            if (tmp != null)
                return new PlateData(tmp);
        } catch (DSOutOfServiceException | ServerError e) {
            handleException(this, e, "Could not get plate");
        }
        return null;
    }

    /**
     * Get a well (Note: This is a slightly deeper object,
     * with wellsamples and images loaded)
     * @param ctx The SecurityContext
     * @param id The id of the well
     * @return The well or null if it can't be found
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public WellData getWell(SecurityContext ctx, long id) throws DSOutOfServiceException,
            DSAccessException {
        try {
            IQueryPrx qs = gateway.getQueryService(ctx);
            ParametersI param = new ParametersI();
            param.addId(id);
            WellI tmp = (WellI) qs.findByQuery(GET_WELL_QUERY, param);
            if (tmp != null)
                return new WellData(tmp);
        } catch (DSOutOfServiceException | ServerError e) {
            handleException(this, e, "Could not get well");
        }
        return null;
    }

}
