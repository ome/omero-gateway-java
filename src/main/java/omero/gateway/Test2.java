package omero.gateway;

import omero.api.IQueryPrx;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.MetadataFacility;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ROIData;
import omero.gateway.model.RectangleData;
import omero.gateway.model.ShapeData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.util.PojoMapper;
import omero.log.SimpleLogger;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Roi;
import omero.model.TagAnnotation;
import omero.sys.ParametersI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Test2 {
    public static void main(String[] args) throws Exception {
        System.out.println("Test");
        LoginCredentials lc = new LoginCredentials("user-3", "ome",
                "merge-ci-devspace.openmicroscopy.org");
        Gateway gw = new Gateway(new SimpleLogger());
        ExperimenterData e = gw.connect(lc);
        SecurityContext ctx = new SecurityContext(e.getGroupId());

        long imageID = 169565;
        BrowseFacility b = gw.getFacility(BrowseFacility.class);
        ImageData img = b.getImage(ctx, imageID);
        List<Long> tmp = new ArrayList<>();
        tmp.add(imageID);
        System.out.println(img.getName());
        System.out.println(img.getDatasets());

        IQueryPrx qs = gw.getQueryService(ctx);
        ParametersI p = new ParametersI();
        p.addLong("id", imageID);
        List<IObject> objs = qs.findAllByQuery("select l.parent from DatasetImageLink as l " +
                "where l.child.id = :id", p);
        for (IObject obj : objs) {
            DatasetData ds = new DatasetData((Dataset) obj);
            System.out.println(ds.getName()+","+ds.getId());
        }

        gw.disconnect();

//        ROIData roi = new ROIData();
//        roi.setImage(img.asImage());
//        RectangleData r1 = new RectangleData(10, 10, 10, 10);
//        r1.setText("First");
//        roi.addShapeData(r1);
//        RectangleData r2 = new RectangleData(20, 20, 10, 10);
//        r2.setText("Second");
//        roi.addShapeData(r2);
//
//        long roiId = rf.saveROIs(ctx, img.getId(), Arrays.asList(roi)).iterator().next().getId();
//
//        roi = rf.loadROI(ctx, roiId).getROIs().iterator().next();
//        System.out.println(roi.getShapeCount());
//        ShapeData toRemove = null;
//        for (ShapeData sh : roi.getShapes()) {
//            if (((RectangleData)sh).getText().equals("Second")) {
//                toRemove = sh;
//                break;
//            }
//        }
//        roi.removeShapeData(toRemove);
//        System.out.println(roi.getShapeCount());
//
//        roi = new ROIData((Roi)gw.getUpdateService(ctx).saveAndReturnObject(roi.asIObject()));
//        System.out.println(roi.getShapeCount());
    }
}
