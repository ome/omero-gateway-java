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
            if (hostname.contains(":/")) {
                // this is already a URI like wss://example.org
                this.uri = new URI(hostname);
                if (port >= 0 && this.uri.getPort() < 0) {
                    this.uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
                            port, uri.getPath(), uri.getQuery(), uri.getFragment());
                }
            }
            else {
                if (port < 0 && hostname.contains(":")) {
                    try {
                        String[] parts = hostname.split(":");
                        port = Integer.parseInt(parts[parts.length-1]);
                        hostname = parts[parts.length-2];
                    } catch (Exception e) {}
                }
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
        return this.uri.getHost();
    }

    /**
     * Set the hostname or websocket URL
     *
     * @param host
     *            See above
     */
    public void setHost(String host) {
        try {
            if (host.contains(":/")) {
                // this is already a URI like wss://example.org
                int port = this.uri.getPort();
                this.uri = new URI(host);
                if (port >= 0 && this.uri.getPort() < 0) {
                    this.uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
                            port, uri.getPath(), uri.getQuery(), uri.getFragment());
                }
            }
            else {
                int port = uri.getPort();
                if (port < 0 && host.contains(":")) {
                    try {
                        String[] parts = host.split(":");
                        port = Integer.parseInt(parts[parts.length-1]);
                        host = parts[parts.length-2];
                    } catch (Exception e) {}
                }
                this.uri = new URI(uri.getScheme(), uri.getUserInfo(), host,
                        port, uri.getPath(), uri.getQuery(), uri.getFragment());
            }
        } catch (URISyntaxException e) {
        }
    }

    /**
     * Return the hostname. Even if a websocket URL
     * was specified only the hostname part will
     * be returned by this method. Use {@link #getHost()}
     * to get the full websocket URL.
     *
     * @return The hostname
     */
    public String getHostname() {
        return uri.getHost();
    }

    /**
     * @deprecated Renamed to {@link #setHost(String)}
     *
     * Set the hostname or websocket URL
     * 
     * @param hostname
     *            See above
     */
    @Deprecated
    public void setHostname(String hostname) {
        setHost(hostname);
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

    /**
     * Returns <code>true</code> if a websocket
     * URL was specified.
     * @return See above.
     */
    public boolean isURL() {
        return this.uri.isAbsolute();
    }

    /**
     * Returns the protocol (lower case) if a websocket URL was specified
     * (empty String otherwise).
     * @return See above.
     */
    public String getProtocol() {
        if (isURL())
            return this.uri.getScheme().toLowerCase();
        return "";
    }

    @Override
    public String toString() {
        return "ServerInformation [uri=" + uri.toString() + "]";
    }
}
