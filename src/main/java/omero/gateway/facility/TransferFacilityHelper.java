/*
 * Copyright (C) 2015-2023 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.gateway.facility;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import omero.RType;
import omero.api.IQueryPrx;
import omero.api.RawFileStorePrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ImageData;
import omero.model.Fileset;
import omero.model.FilesetEntry;
import omero.model.OriginalFile;
import omero.sys.ParametersI;

import org.apache.commons.collections.CollectionUtils;

/**
 * Encapsulates some functionality needed by the {@link TransferFacility}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class TransferFacilityHelper {

    /** Maximum size of bytes read at once. */
    private static final int INC = 262144;//

    private BrowseFacility browse;

    private Gateway gateway;

    /**
     * Creates a new instance.
     *
     * @param gateway
     *            Reference to the gateway.
     * @throws ExecutionException
     */
    TransferFacilityHelper(Gateway gateway) throws ExecutionException {
        this.gateway = gateway;
        this.browse = gateway.getFacility(BrowseFacility.class);
    }

    /**
     * Downloads the original file of an image from the server.
     *
     * @param context
     *            The security context.
     * @param targetPath
     *            Path to the file.
     * @param imageId
     *            The identifier of the image.
     * @return See above
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    List<File> downloadImage(SecurityContext context, String targetPath,
            long imageId) throws DSAccessException, DSOutOfServiceException {
        List<File> files = new ArrayList<File>();
        ImageData image = browse.findObject(context, ImageData.class, imageId,
                true);

        String query;
        List<?> filesets;
        try {
            IQueryPrx service = gateway.getQueryService(context);
            ParametersI param = new ParametersI();
            long id;
            if (image.isFSImage()) {
                id = image.getId();
                List<RType> l = new ArrayList<RType>();
                l.add(omero.rtypes.rlong(id));
                param.add("imageIds", omero.rtypes.rlist(l));
                query = createFileSetQuery();
            } else {// Prior to FS
                if (image.isArchived()) {
                    StringBuffer buffer = new StringBuffer();
                    id = image.getDefaultPixels().getId();
                    buffer.append("select ofile from OriginalFile as ofile ");
                    buffer.append("join fetch ofile.hasher ");
                    buffer.append("left join ofile.pixelsFileMaps as pfm ");
                    buffer.append("left join pfm.child as child ");
                    buffer.append("where child.id = :id");
                    param.map.put("id", omero.rtypes.rlong(id));
                    query = buffer.toString();
                } else
                    return null;
            }
            filesets = service.findAllByQuery(query, param);
        } catch (Exception e) {
            throw new DSAccessException("Cannot retrieve original file", e);
        }

        Map<Boolean, Object> result = new HashMap<Boolean, Object>();
        if (CollectionUtils.isEmpty(filesets))
            return files;
        List<File> downloaded = new ArrayList<File>();
        List<String> notDownloaded = new ArrayList<String>();
        result.put(Boolean.valueOf(true), downloaded);
        result.put(Boolean.valueOf(false), notDownloaded);

        if (image.isFSImage()) {
            for (Object tmp : filesets) {
                Fileset fs = (Fileset) tmp;
                String fs_dir = "Fileset_"+fs.getId().getValue();
                String repoPath = fs.getTemplatePrefix().getValue();
                for (FilesetEntry fse: fs.copyUsedFiles()) {
                    OriginalFile of = fse.getOriginalFile();
                    String ofDir = of.getPath().getValue().replace(repoPath, "");
                    File outDir = new File(targetPath+File.separator+ofDir+File.separator+fs_dir);
                    outDir.mkdirs();
                    File saved = saveOriginalFile(context, of, outDir);
                    if (saved != null)
                        downloaded.add(saved);
                    else
                        notDownloaded.add(of.getName().getValue());
                }
            }
        }
        else { //Prior to FS
            for (Object tmp : filesets) {
                OriginalFile of = (OriginalFile) tmp;
                File outDir = new File(targetPath);
                File saved = saveOriginalFile(context, of, outDir);
                if (saved != null)
                    downloaded.add(saved);
                else
                    notDownloaded.add(of.getName().getValue());
            }
        }

        return downloaded;
    }

    /**
     * Save an OriginalFile of into directory dir
     * @param ctx The SecurityContext
     * @param of The OriginalFile
     * @param dir The output directory
     * @return The File if the operation was successfull, null if it wasn't.
     */
    private File saveOriginalFile(SecurityContext ctx, OriginalFile of, File dir) {
        File out = new File(dir, of.getName().getValue());
        if (out.exists()) {
            return null;
        }

        try {
            RawFileStorePrx store = gateway.getRawFileService(ctx);
            store.setFileId(of.getId().getValue());

            long size = of.getSize().getValue();
            long offset = 0;
            try (FileOutputStream stream = new FileOutputStream(out))
            {
                for (offset = 0; (offset+INC) < size;) {
                    stream.write(store.read(offset, INC));
                    offset += INC;
                }
                stream.write(store.read(offset, (int) (size-offset)));
            }
        } catch (Exception e) {

            return null;
        }
        return out;
    }

    /**
     * Creates the query to load the file set corresponding to a given image.
     *
     * @return See above.
     */
    private String createFileSetQuery() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("select fs from Fileset as fs ");
        buffer.append("join fetch fs.images as image ");
        buffer.append("left outer join fetch fs.usedFiles as usedFile ");
        buffer.append("join fetch usedFile.originalFile as f ");
        buffer.append("join fetch f.hasher ");
        buffer.append("where image.id in (:imageIds)");
        return buffer.toString();
    }
}
