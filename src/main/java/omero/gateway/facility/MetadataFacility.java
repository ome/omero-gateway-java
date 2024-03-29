/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2023 University of Dundee. All rights reserved.
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.Arrays;

import omero.ServerError;
import omero.api.IQueryPrx;
import omero.cmd.CmdCallbackI;
import omero.cmd.OriginalMetadataRequest;
import omero.cmd.OriginalMetadataResponse;
import omero.gateway.model.FilesetData;
import omero.gateway.model.PixelsData;
import omero.gateway.model.PlaneInfoData;
import omero.gateway.util.OriginalMetadataParser;
import omero.model.FilesetI;
import omero.model.PlaneInfo;
import org.apache.commons.collections.CollectionUtils;

import omero.api.IMetadataPrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.model.Channel;
import omero.model.IObject;
import omero.model.Pixels;
import omero.sys.ParametersI;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;
import omero.gateway.model.ImageAcquisitionData;
import omero.gateway.model.ImageData;
import omero.gateway.util.PojoMapper;
import omero.gateway.util.Pojos;


/**
 * A {@link Facility} to access the metadata.
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class MetadataFacility extends Facility {

    private BrowseFacility browse;

    /**
     * Creates a new instance.
     *
     * @param gateway Reference to the gateway.
     * @throws ExecutionException
     */
    MetadataFacility(Gateway gateway) throws ExecutionException {
        super(gateway);
        this.browse = gateway.getFacility(BrowseFacility.class);
    }

    /**
     * Loads the {@link ImageAcquisitionData} for a specific image
     *
     * @param ctx
     *            The {@link SecurityContext}
     * @param imageId
     *            The imageId
     * @return See above
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public ImageAcquisitionData getImageAcquisitionData(SecurityContext ctx,
            long imageId) throws DSOutOfServiceException, DSAccessException {

        if (imageId < 0) {
            return null;
        }

        ParametersI params = new ParametersI();
        params.acquisitionData();
        ImageData img = browse.getImage(ctx, imageId, params);
        return new ImageAcquisitionData(img.asImage());
    }

    /**
     * Get the {@link ChannelData} for a specific image
     *
     * @param ctx
     *            The {@link SecurityContext}
     * @param imageId
     *            The imageId to get the ChannelData for
     * @return List of ChannelData
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public List<ChannelData> getChannelData(SecurityContext ctx, long imageId)
            throws DSOutOfServiceException, DSAccessException {

        List<ChannelData> result = new ArrayList<ChannelData>();
        if (imageId < 0) {
            return result;
        }

        try {
            ImageData img = browse.getImage(ctx, imageId);

            long pixelsId = img.getDefaultPixels().getId();
            Pixels pixels = gateway.getPixelsService(ctx)
                    .retrievePixDescription(pixelsId);
            List<Channel> l = pixels.copyChannels();
            for (int i = 0; i < l.size(); i++)
                result.add(new ChannelData(i, l.get(i)));

        } catch (Throwable t) {
            handleException(this, t, "Cannot load channel data.");
        }

        return result;
    }

    /**
     * Get all annotations for the given {@link DataObject}
     * @param ctx The {@link SecurityContext}
     * @param object The {@link DataObject} to load the annotations for
     * @return See above
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public List<AnnotationData> getAnnotations(SecurityContext ctx,
            DataObject object) throws DSOutOfServiceException,
            DSAccessException {
        return getAnnotations(ctx, object, null, null);
    }

    /**
     * Get the annotations for the given {@link DataObject}
     *
     * @param ctx
     *            The {@link SecurityContext}
     * @param object
     *            The {@link DataObject} to load the annotations for
     * @param annotationTypes
     *            The type of annotations to load (can be <code>null</code>)
     * @param userIds
     *            Only load annotations of certain users (can be
     *            <code>null</code>, i. e. all users)
     * @return See above
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public List<AnnotationData> getAnnotations(SecurityContext ctx,
            DataObject object,
            List<Class<? extends AnnotationData>> annotationTypes,
            List<Long> userIds) throws DSOutOfServiceException,
            DSAccessException {
        if (!Pojos.hasID(object))
            return Collections.emptyList();

        Map<DataObject, List<AnnotationData>> result = getAnnotations(ctx,
                Arrays.asList(new DataObject[] { object }), annotationTypes,
                userIds);
        return result.get(object);
    }

    /**
     * Get the annotations for the given {@link DataObject}s
     *
     * @param ctx
     *            The {@link SecurityContext}
     * @param objects
     *            The {@link DataObject}s to load the annotations for (have to
     *            be all of the same type)
     * @param annotationTypes
     *            The type of annotations to load (can be <code>null</code>)
     * @param userIds
     *            Only load annotations of certain users (can be
     *            <code>null</code>, i. e. all users)
     * @return Lists of {@link AnnotationData} mapped to the {@link DataObject}
     *         they are attached to.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public Map<DataObject, List<AnnotationData>> getAnnotations(
            SecurityContext ctx, List<? extends DataObject> objects,
            List<Class<? extends AnnotationData>> annotationTypes,
            List<Long> userIds) throws DSOutOfServiceException,
            DSAccessException {
        Map<DataObject, List<AnnotationData>> result = new HashMap<DataObject, List<AnnotationData>>();
        if (CollectionUtils.isEmpty(objects))
            return result;

        String type = null;
        List<Long> ids = new ArrayList<Long>();
        for (DataObject obj : objects) {
            if (type == null)
                type = PojoMapper.getModelType(obj.getClass()).getName();
            else if (!type.equals(PojoMapper.getModelType(obj.getClass())
                    .getName()))
                throw new IllegalArgumentException(
                        "All objects have to be the same type");
            ids.add(obj.getId());
        }

        try {
            IMetadataPrx proxy = gateway.getMetadataService(ctx);
            List<String> annoTypes = null;
            if (annotationTypes != null) {
                annoTypes = new ArrayList<String>(annotationTypes.size());
                for (Class c : annotationTypes)
                    annoTypes.add(PojoMapper.getModelType(c).getName());
            }
            Map<Long, List<IObject>> annos = proxy.loadAnnotations(type, ids,
                    annoTypes, userIds, null);
            for (Entry<Long, List<IObject>> e : annos.entrySet()) {
                long id = e.getKey();
                DataObject dobj = null;
                for (DataObject o : objects) {
                    if (o.getId() == id) {
                        dobj = o;
                        break;
                    }
                }
                List<AnnotationData> list = new ArrayList<AnnotationData>();
                for (IObject a : e.getValue()) {
                    list.add((AnnotationData) PojoMapper.asDataObject(a));
                }
                result.put(dobj, list);
            }
        } catch (Throwable t) {
            handleException(this, t, "Cannot get annotations.");
        }

        return result;
    }

    /**
     * Get the file paths of the image in the managed repository
     * @param ctx The SecurityContext
     * @param img The image
     * @return See above
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public List<String> getManagedRepositoriesPaths(SecurityContext ctx, ImageData img) throws DSOutOfServiceException, DSAccessException {
        try {
            FilesetData fs = loadFileset(ctx, img);
            if (fs != null)
                return fs.getAbsolutePaths();
        } catch (Throwable t) {
            handleException(this, t, "Could not get the file paths.");
        }
        return Collections.emptyList();
    }

    /**
     * Get the original file paths where the image was imported from.
     * @param ctx The SecurityContext
     * @param img The image
     * @return See above
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public List<String> getOriginalPaths(SecurityContext ctx, ImageData img) throws DSOutOfServiceException, DSAccessException {
        try {
            FilesetData fs = loadFileset(ctx, img);
            if (fs != null)
                return fs.getUsedFilePaths();
        } catch (Throwable t) {
            handleException(this, t, "Could not get the file paths.");
        }
        return Collections.emptyList();
    }

    /**
     * Get the plane infos.
     * @param ctx The SecurityContext
     * @param pix The PixelsData
     * @return See above
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    public List<PlaneInfoData> getPlaneInfos(SecurityContext ctx, PixelsData pix) throws DSOutOfServiceException,
            DSAccessException {
        List<PlaneInfoData> result = new ArrayList<>();
        try {
            ParametersI p = new ParametersI();
            p.addLong("id", pix.getId());
            List<IObject> planeinfos = gateway.getQueryService(ctx)
                .findAllByQuery("select info from PlaneInfo as info " +
                                "join fetch info.deltaT as dt " +
                                "join fetch info.exposureTime as et " +
                                "where info.pixels.id = :id",
                        p);
            for (IObject obj : planeinfos) {
                result.add(new PlaneInfoData((PlaneInfo) obj));
            }
        } catch (Throwable th) {
            handleException(this, th, "Could not get the planeinfos.");
        }
        return result;
    }

    /**
     * Load the Fileset for an image
     * @param ctx The SecurityContext
     * @param img The image
     * @return The fileset
     * @throws ServerError
     * @throws DSOutOfServiceException
     */
    private FilesetData loadFileset(SecurityContext ctx, ImageData img) throws ServerError, DSOutOfServiceException {
        String query = "select fs from Fileset as fs " +
                "join fetch fs.images as image " +
                "left outer join fetch fs.usedFiles as usedFile " +
                "join fetch usedFile.originalFile as f " +
                "join fetch f.hasher " +
                "where image.id = :id";

        IQueryPrx service = gateway.getQueryService(ctx);
        ParametersI param = new ParametersI();
        param.addId(img.getId());
        FilesetI fs = (FilesetI) service.findByQuery(query, param);
        if (fs != null)
            return new FilesetData(fs);
        return null;
    }

    /**
     * Submits an OriginalMetadataRequest and waits for response
     * @param ctx The SecurityContext
     * @param imageId The image ID
     * @return The OriginalMetadataResponse or null if something went wrong
     */
    private OriginalMetadataResponse requestOriginalMetadata(SecurityContext ctx, long imageId) {
        OriginalMetadataRequest cmd = new OriginalMetadataRequest();
        cmd.imageId = imageId;
        try {
            CmdCallbackI cb = gateway.submit(ctx, cmd);
            if (cb.block(10000)) {
                return (OriginalMetadataResponse) cb.getResponse();
            }
            else {
                logError(this, "Could not request original metadata", null);
            }
        } catch (Throwable t) {
            logError(this, "Could not request original metadata", t);
        }
        return null;
    }

    /**
     * Get the original metadata of an image and write it into a file
     * @param ctx The SecurityContext
     * @param imageId The image id
     * @param output The output file
     * @throws Throwable
     */
    public void getOriginalMetadata(SecurityContext ctx, long imageId, File output) throws Exception {
        if (output == null)
            return;
        OriginalMetadataResponse response = requestOriginalMetadata(ctx, imageId);
        if (response != null)
            (new OriginalMetadataParser(output)).read(response);
    }

    /**
     * Get the original metadata of an image and write it a StringBuilder
     * @param ctx The SecurityContext
     * @param imageId The image id
     * @param buffer The buffer
     * @throws Throwable
     */
    public void getOriginalMetadata(SecurityContext ctx, long imageId, StringBuffer buffer) {
        if (buffer == null)
            return;
        OriginalMetadataResponse response = requestOriginalMetadata(ctx, imageId);
        if (response != null) {
            try {
                (new OriginalMetadataParser(buffer)).read(response);
            } catch (Exception e) {
                // ignore; calling the method with a StringBuilder can't throw an exception
            }
        }
    }

}
