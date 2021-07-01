/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2021 University of Dundee. All rights reserved.
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
 *------------------------------------------------------------------------------*/
package omero.gateway.model;

import omero.model.Length;
import omero.model.PlaneInfo;
import omero.model.PlaneInfoI;
import omero.model.Time;

public class PlaneInfoData extends DataObject {

    public PlaneInfoData() {
        setDirty(true);
        setValue(new PlaneInfoI());
    }

    public PlaneInfoData(PlaneInfo info) {
        if (info == null) {
            throw new IllegalArgumentException("The object cannot be null.");
        }
        setValue(info);
    }

    public Time getDeltaT() {
        return asPlaneInfo().getDeltaT();
    }

    public Time getExposureTime() {
        return asPlaneInfo().getExposureTime();
    }

    public Length getPositionX() {
        return asPlaneInfo().getPositionX();
    }

    public Length getPositionY() {
        return asPlaneInfo().getPositionY();
    }

    public Length getPositionZ() {
        return asPlaneInfo().getPositionZ();
    }

    public int getTheC() {
        return asPlaneInfo().getTheC().getValue();
    }

    public int getTheT() {
        return asPlaneInfo().getTheT().getValue();
    }

    public int getTheZ() {
        return asPlaneInfo().getTheZ().getValue();
    }


    public void setDeltaT(Time theDeltaT) {
        asPlaneInfo().setDeltaT(theDeltaT);
    }

    public void setExposureTime(Time theExposureTime) {
        asPlaneInfo().setExposureTime(theExposureTime);
    }

    public void setPositionX(Length thePositionX) {
        asPlaneInfo().setPositionX(thePositionX);
    }

    public void setPositionY(Length thePositionY) {
        asPlaneInfo().setPositionY(thePositionY);
    }

    public void setPositionZ(Length thePositionZ) {
        asPlaneInfo().setPositionZ(thePositionZ);
    }

    public void setTheC(int theTheC) {
        asPlaneInfo().setTheC(omero.rtypes.rint(theTheC));
    }

    public void setTheT(int theTheT) {
        asPlaneInfo().setTheT(omero.rtypes.rint(theTheT));
    }

    public void setTheZ(int theTheZ) {
        asPlaneInfo().setTheZ(omero.rtypes.rint(theTheZ));
    }
}
