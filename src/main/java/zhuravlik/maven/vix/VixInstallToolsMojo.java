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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Created by IntelliJ IDEA.
 * User: anton
 * Date: 11.02.12
 * Time: 21:57
 * To change this template use File | Settings | File Templates.
 */

/**
 * Goal which installs or upgrades Tools in guest.
 *
 * @goal installTools
 *
 */
public class VixInstallToolsMojo extends VixAbstractMojo {

    /**
     * If true, mount tools iso, otherwise, autoupgrade tools.
     * @parameter expression="${vix.mount}"
     */
    private boolean mount;

    /**
     * Do not wait for operation to finish, return immediately.
     * @parameter expression="${vix.returnImmediately}"
     */
    private boolean returnImmediately;


    public void execute() throws MojoExecutionException, MojoFailureException {

        initialize();

        getLog().info("Installing or upgrading Tools in guest");

        int jobHandle = Vix.VIX_INVALID_HANDLE;

        jobHandle = LibraryHelper.getInstance().VixVM_InstallTools(
                vmHandle,
                (mount ? Vix.VIX_INSTALLTOOLS_MOUNT_TOOLS_INSTALLER : Vix.VIX_INSTALLTOOLS_AUTO_UPGRADE) |
                        (returnImmediately ? Vix.VIX_INSTALLTOOLS_RETURN_IMMEDIATELY : 0),
                null,
                null,
                null
        );


        int err = LibraryHelper.getInstance().VixJob_Wait(jobHandle, Vix.VIX_PROPERTY_NONE);
        LibraryHelper.getInstance().Vix_ReleaseHandle(jobHandle);
        checkError(err);

        finish();
    }
}
