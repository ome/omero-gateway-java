package omero.gateway;

import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.MetadataFacility;
import omero.gateway.model.ChannelData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageAcquisitionData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlaneInfoData;
import omero.gateway.rnd.Plane2D;
import omero.log.SimpleLogger;
import omero.model.Length;
import omero.model.enums.UnitsLength;

import java.util.List;


public class Test {
    public static void main(String[] args) throws Exception {
        LoginCredentials lc = new LoginCredentials("user-3", "ome", "merge-ci-devspace.openmicroscopy.org");

        try(Gateway gw = new Gateway(new SimpleLogger())) {
            ExperimenterData e = gw.connect(lc);
            SecurityContext ctx = new SecurityContext(e.getGroupId());
            long imageId = 83221;
            BrowseFacility b = gw.getFacility(BrowseFacility.class);
            ImageData img = b.getImage(ctx, imageId);

            MetadataFacility mf = gw.getFacility(MetadataFacility.class);
            List<PlaneInfoData> pis = mf.getPlaneInfos(ctx, img.getDefaultPixels());
            for (PlaneInfoData pi : pis) {
                System.out.println("c="+pi.getTheC()+"z="+pi.getTheZ()+"t="+pi.getTheT()+" deltaT="+pi.getDeltaT());
            }
        }
    }
}
