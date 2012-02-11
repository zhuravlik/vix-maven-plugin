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
 * Time: 23:24
 * To change this template use File | Settings | File Templates.
 */

/**
 * Goal which launches program in guest
 *
 * @goal runProgram
 *
 */
public class VixRunProgramMojo extends VixAbstractMojo {

    /**
     * Fully qualified program path.
     * @parameter expression="${vix.path}"
     */
    private String path;

    /**
     * Command-line args.
     * @parameter expression="${vix.args}"
     */
    private String args;

    /**
     * Do not wait for operation to finish, return immediately.
     * @parameter expression="${vix.returnImmediately}"
     */
    private boolean returnImmediately;

    /**
     * Activate program window after start (Windows-only).
     * @parameter expression="${vix.activateWindow}"
     */
    private boolean activateWindow;

    public void execute() throws MojoExecutionException, MojoFailureException {
        initialize();
        login();

        getLog().info("Running program [" + path + "] in guest with args [" + args + "]");

        if (path == null) {
            throw new MojoExecutionException("Path not specified");
        }

        if (args == null) {
            args = "";
        }

        int jobHandle = Vix.VIX_INVALID_HANDLE;

        int options = 0;

        if (returnImmediately) options |= Vix.VIX_RUNPROGRAM_RETURN_IMMEDIATELY;
        if (activateWindow) options |= Vix.VIX_RUNPROGRAM_ACTIVATE_WINDOW;

        jobHandle = LibraryHelper.getInstance().VixVM_RunProgramInGuest(vmHandle, path, args,
                options, Vix.VIX_INVALID_HANDLE, null, null);

        int err = LibraryHelper.getInstance().VixJob_Wait(jobHandle, Vix.VIX_PROPERTY_NONE);
        LibraryHelper.getInstance().Vix_ReleaseHandle(jobHandle);

        logout();
        checkError(err);
        finish();
    }
}
