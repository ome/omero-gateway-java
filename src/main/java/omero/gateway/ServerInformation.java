/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2016 University of Dundee. All rights reserved.
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
package omero.gateway;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Holds the network connection information of an OMERO server
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class ServerInformation {

    /** The URI */
    private URI uri;

    /**
     * Creates an empty instance
     */
    public ServerInformation() {
        try {
            this.uri = new URI(null, null, null,
                    -1, null, null, null);
        } catch (URISyntaxException e) {
        }
    }

    /**
     * Creates a new instance
     *
     * @param hostname
     *            The hostname or websocket URL
     */
    public ServerInformation(String hostname) {
        this(hostname, -1);
    }

    /**
     * Creates a new instance
     * 
     * @param hostname
     *            The hostname or websocket URL
     * @param port
     *            The port
     */
    public ServerInformation(String hostname, int port) {
        try {
            if ((new URI(hostname)).isAbsolute()) {
                // this is already a URI like wss://example.org
                this.uri = new URI(hostname);
                if (port < 0) {
                    this.uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
                            port, uri.getPath(), uri.getQuery(), uri.getFragment());
                }
            }
            else {
                this.uri = new URI(null,null, hostname,
                        port, null, null, null);
            }
        } catch (URISyntaxException e) {
        }
    }

    /**
     * Get the host information as required by the omero.client.
     * In case a websocket URL was specified the full
     * URL will be returned. If only a host name was
     * specified only the host name will be returned.
     * @return See above.
     */
    public String getHost() {
        if (this.uri.isAbsolute())
            return this.uri.toString();
        else
            return this.uri.getHost();
    }

    /**
     * Return the hostname.
     *
     * @return The hostname
     */
    public String getHostname() {
        return uri.getHost();
    }

    /**
     * Set the hostname or websocket URL
     * 
     * @param hostname
     *            See above
     */
    public void setHostname(String hostname) {
        try {
            if ((new URI(hostname)).isAbsolute())
                // this is already a URI like wss://example.org
                this.uri = new URI(hostname);
            else
                this.uri = new URI(uri.getScheme(), uri.getUserInfo(), hostname,
                        uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
        }
    }

    /**
     * Return the port
     * @return The port
     */
    public int getPort() {
        return uri.getPort();
    }

    /**
     * Set the port
     * 
     * @param port
     *            See above
     */
    public void setPort(int port) {
        try {
            this.uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
                    port, uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
        }
    }

    @Override
    public String toString() {
        return "ServerInformation [uri=" + uri.toString() + "]";
    }
    
}
