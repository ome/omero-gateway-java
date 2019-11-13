/*
 * Copyright (C) 2019 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.gateway;

import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import omero.log.SimpleLogger;

/**
 * Unit tests for the Java Gateway.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.5.5
 */
@Test(groups = "unit")
public class GatewayTest {

    private final Gateway gateway = new Gateway(new SimpleLogger());

    /**
     * @param major major version
     * @param minor minor version
     * @return if semantic versioning applies for that version
     */
    private boolean isSemanticVersioning(String major, String minor) {
        switch (major) {
        case "A":
        case "4":
            return false;
        case "5":
            switch (minor) {
            case "A":
            case "4":
                return false;
            case "5":
            case "6":
                return true;
            }
        case "6":
            return true;
        }
        Assert.fail("unexpected");
        return false;
    }

    /**
     * Check that server-client compatibility is determined correctly for the given version numbers.
     * @param serverMajor server major version
     * @param serverMinor server minor version
     * @param clientMajor client major version
     * @param clientMinor client minor version
     */
    @Test(dataProvider = "version combinations")
    public void testVersionCombination(String serverMajor, String serverMinor, String clientMajor, String clientMinor) {
        final boolean isCompatibleVersion;
        if (isSemanticVersioning(serverMajor, serverMinor) && isSemanticVersioning(clientMajor, clientMinor)) {
            isCompatibleVersion = serverMajor.equals(clientMajor);
        } else {
            isCompatibleVersion = serverMajor.equals(clientMajor) && serverMinor.equals(clientMinor);
        }
        final String[] serverVersion = new String[] {serverMajor, serverMinor};
        final String[] clientVersion = new String[] {clientMajor, clientMinor};
        Assert.assertEquals(gateway.isCompatibleVersion(serverVersion, clientVersion), isCompatibleVersion);
    }

    /**
     * @return test cases for {@link #testVersionCombination(String, String, String, String)}
     */
    @DataProvider(name = "version combinations")
    public Object[][] provide() {
        final List<String> versions = ImmutableList.of("4", "5", "6", "A");
        return Lists.cartesianProduct(Collections.nCopies(4, versions))
                .stream().map(args -> args.stream().toArray(String[]::new)).toArray(String[][]::new);
    }
}
