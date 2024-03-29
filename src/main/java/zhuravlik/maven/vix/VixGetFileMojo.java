/*
   Copyright (C) 2012 Anton Lobov <zhuravlik> <ahmad200512[at]yandex.ru>

   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General
   Public License along with this library; if not, write to the
   Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
   Boston, MA 02110-1301 USA
 */

package zhuravlik.maven.vix;

/**
 * Created by IntelliJ IDEA.
 * User: anton
 * Date: 11.02.12
 * Time: 21:12
 * To change this template use File | Settings | File Templates.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which gets file from guest to host.
 *
 * @goal getFile
 *
 */
public class VixGetFileMojo extends VixAbstractMojo {

    /**
     * Path in guest.
     * @parameter expression="${vix.path}"
     */
    private String path;


    /**
     * Destination at host.
     * @parameter expression="${vix.destination}"
     */
    private String destination;

    public void execute() throws MojoExecutionException, MojoFailureException {

        initialize();
        login();

        getLog().info("Copying file [" + path + "] from guest to path [" + destination + "]");

        if (path == null) {
            throw new MojoExecutionException("Path not specified");
        }

        if (destination == null) {
            throw new MojoExecutionException("Destination not specified");
        }

        int jobHandle = Vix.VIX_INVALID_HANDLE;

        jobHandle = LibraryHelper.getInstance().VixVM_CopyFileFromGuestToHost(vmHandle, path, destination, 0,
                Vix.VIX_INVALID_HANDLE, null, null);


        int err = LibraryHelper.getInstance().VixJob_Wait(jobHandle, Vix.VIX_PROPERTY_NONE);
        LibraryHelper.getInstance().Vix_ReleaseHandle(jobHandle);

        logout();
        checkError(err);
        finish();
    }
}
