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
import omero.model.IObject;
import omero.model.ImageI;
import omero.model.PlateI;
import omero.model.ProjectI;
import omero.model.ScreenI;
import omero.model.WellI;
import omero.sys.ParametersI;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Facility for loading basic objects. Note: These are shallow objects with just
 * one level of hierarchy loaded e.g. a project will have a list of datasets,
 * but the datasets only have basic properties like name and id loaded.
 */
public class LoadFacility extends Facility {

    private static String GET_DATASET_QUERY = "select ds from Dataset as ds " +
            "left join fetch ds.imageLinks as l " +
            "left join fetch l.child as i " +
            "where ds.id in (:ids)";

    private static String GET_PROJECT_QUERY = "select p from Project as p " +
            "left join fetch p.datasetLinks as l " +
            "left join fetch l.child as i " +
            "where p.id =in (:ids)";

    private static String GET_IMAGE_QUERY = "select i from Image as i " +
            "left join fetch i.pixels as p " +
            "left join fetch p.pixelsType as pt " +
            "where i.id in (:ids)";

    private static String GET_PLATE_QUERY = "select p from Plate as p " +
            "left join fetch p.wells as w " +
            "left join fetch p.plateAcquisitions as pa " +
            "where p.id in (:ids)";

    private static String GET_SCREEN_QUERY = "select s from Screen as s " +
            "left join fetch s.plateLinks as l " +
            "left join fetch l.child as p " +
            "where s.id in (:ids)";

    private static String GET_WELL_QUERY = "select w from Well as w " +
            "left join fetch w.wellSamples as ws " +
            "left join fetch ws.plateAcquisition as pa " +
            "left join fetch ws.image as img " +
            "left join fetch img.pixels as pix " +
            "left join fetch pix.pixelsType as pt " +
            "where w.id in (:ids)";

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
        Collection<DatasetData> tmp = getDatasets(ctx, Collections.singletonList(id));
        if (tmp.isEmpty())
            return null;
        return tmp.iterator().next();
    }

    /**
     * Get datasets
     * @param ctx The SecurityContext
     * @param ids The ids of the datasets
     * @return Collection of datasets (can be empty)
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public Collection<DatasetData> getDatasets(SecurityContext ctx, Collection<Long> ids) throws DSOutOfServiceException,
            DSAccessException {
        try {
            IQueryPrx qs = gateway.getQueryService(ctx);
            ParametersI param = new ParametersI();
            param.addIds(ids);
            List<IObject> tmp = qs.findAllByQuery(GET_DATASET_QUERY, param);
            if (tmp != null)
                return tmp.stream().map(e -> new DatasetData((DatasetI) e)).collect(Collectors.toSet());
            return Collections.emptySet();
        } catch (DSOutOfServiceException | ServerError e) {
            handleException(this, e, "Could not get datasets");
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
        Collection<ProjectData> tmp = getProjects(ctx, Collections.singletonList(id));
        if (tmp.isEmpty())
            return null;
        return tmp.iterator().next();
    }

    /**
     * Get projects
     * @param ctx The SecurityContext
     * @param ids The ids of the projects
     * @return Collection of projects (can be empty)
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public Collection<ProjectData> getProjects(SecurityContext ctx, Collection<Long> ids) throws DSOutOfServiceException,
            DSAccessException {
        try {
            IQueryPrx qs = gateway.getQueryService(ctx);
            ParametersI param = new ParametersI();
            param.addIds(ids);
            List<IObject> tmp = qs.findAllByQuery(GET_PROJECT_QUERY, param);
            if (tmp != null)
                return tmp.stream().map(e -> new ProjectData((ProjectI) e)).collect(Collectors.toSet());
            return Collections.emptySet();
        } catch (DSOutOfServiceException | ServerError e) {
            handleException(this, e, "Could not get projects");
        }
        return null;
    }

    /**
     * Get a image
     * @param ctx The SecurityContext
     * @param id The id of the image
     * @return The image or null if it can't be found
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public ImageData getImage(SecurityContext ctx, long id) throws DSOutOfServiceException,
            DSAccessException {
        Collection<ImageData> tmp = getImages(ctx, Collections.singletonList(id));
        if (tmp.isEmpty())
            return null;
        return tmp.iterator().next();
    }

    /**
     * Get images
     * @param ctx The SecurityContext
     * @param ids The ids of the images
     * @return Collection of images (can be empty)
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public Collection<ImageData> getImages(SecurityContext ctx, Collection<Long> ids) throws DSOutOfServiceException,
            DSAccessException {
        try {
            IQueryPrx qs = gateway.getQueryService(ctx);
            ParametersI param = new ParametersI();
            param.addIds(ids);
            List<IObject> tmp = qs.findAllByQuery(GET_IMAGE_QUERY, param);
            if (tmp != null)
                return tmp.stream().map(e -> new ImageData((ImageI) e)).collect(Collectors.toSet());
            return Collections.emptySet();
        } catch (DSOutOfServiceException | ServerError e) {
            handleException(this, e, "Could not get images");
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
        Collection<ScreenData> tmp = getScreens(ctx, Collections.singletonList(id));
        if (tmp.isEmpty())
            return null;
        return tmp.iterator().next();
    }

    /**
     * Get screens
     * @param ctx The SecurityContext
     * @param ids The ids of the screens
     * @return Collection of screens (can be empty)
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public Collection<ScreenData> getScreens(SecurityContext ctx, Collection<Long> ids) throws DSOutOfServiceException,
            DSAccessException {
        try {
            IQueryPrx qs = gateway.getQueryService(ctx);
            ParametersI param = new ParametersI();
            param.addIds(ids);
            List<IObject> tmp = qs.findAllByQuery(GET_SCREEN_QUERY, param);
            if (tmp != null)
                return tmp.stream().map(e -> new ScreenData((ScreenI) e)).collect(Collectors.toSet());
            return Collections.emptySet();
        } catch (DSOutOfServiceException | ServerError e) {
            handleException(this, e, "Could not get screens");
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
        Collection<PlateData> tmp = getPlates(ctx, Collections.singletonList(id));
        if (tmp.isEmpty())
            return null;
        return tmp.iterator().next();
    }

    /**
     * Get plates
     * @param ctx The SecurityContext
     * @param ids The ids of the plates
     * @return Collection of plates (can be empty)
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public Collection<PlateData> getPlates(SecurityContext ctx, Collection<Long> ids) throws DSOutOfServiceException,
            DSAccessException {
        try {
            IQueryPrx qs = gateway.getQueryService(ctx);
            ParametersI param = new ParametersI();
            param.addIds(ids);
            List<IObject> tmp = qs.findAllByQuery(GET_PLATE_QUERY, param);
            if (tmp != null)
                return tmp.stream().map(e -> new PlateData((PlateI) e)).collect(Collectors.toSet());
            return Collections.emptySet();
        } catch (DSOutOfServiceException | ServerError e) {
            handleException(this, e, "Could not get screens");
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
        Collection<WellData> tmp = getWells(ctx, Collections.singletonList(id));
        if (tmp.isEmpty())
            return null;
        return tmp.iterator().next();
    }

    /**
     * Get wells (Note: These are slightly deeper objects,
     * with wellsamples and images loaded)
     * @param ctx The SecurityContext
     * @param ids The ids of the wells
     * @return Collection of wells (can be empty)
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public Collection<WellData> getWells(SecurityContext ctx, Collection<Long> ids) throws DSOutOfServiceException,
            DSAccessException {
        try {
            IQueryPrx qs = gateway.getQueryService(ctx);
            ParametersI param = new ParametersI();
            param.addIds(ids);
            List<IObject> tmp = qs.findAllByQuery(GET_WELL_QUERY, param);
            if (tmp != null)
                return tmp.stream().map(e -> new WellData((WellI) e)).collect(Collectors.toSet());
            return Collections.emptySet();
        } catch (DSOutOfServiceException | ServerError e) {
            handleException(this, e, "Could not get wells");
        }
        return null;
    }
}
