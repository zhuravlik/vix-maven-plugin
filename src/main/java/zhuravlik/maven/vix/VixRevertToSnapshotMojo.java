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

import com.sun.jna.ptr.IntByReference;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Created by IntelliJ IDEA.
 * User: anton
 * Date: 11.02.12
 * Time: 23:21
 * To change this template use File | Settings | File Templates.
 */

/**
 * Goal which reverts to specified snapshot.
 *
 * @goal revertToSnapshot
 *
 */
public class VixRevertToSnapshotMojo extends VixAbstractMojo {

    /**
     * Snapshot name.
     * @parameter expression="${vix.name}"
     */
    private String name;

    /**
     * Suppress powered on state.
     * @parameter expression="${vix.suppressOn}"
     */
    private boolean suppressOn;

    /**
     * Launch GUI.
     * @parameter expression="${vix.launchGui}"
     */
    private boolean launchGui;

    public void execute() throws MojoExecutionException, MojoFailureException {
        initialize();

        if (name == null || name.length() == 0) {
            throw new MojoExecutionException("Snapshot name is not specified");
        }

        getLog().info("Reverting to snapshot [" + name + "]");

        //log("VM Handle: " + vmHandle);

        int jobHandle = Vix.VIX_INVALID_HANDLE;
        IntByReference snapshotHandlePtr = new IntByReference();

        int err = LibraryHelper.getInstance().VixVM_GetNamedSnapshot(vmHandle,
                name,
                snapshotHandlePtr);
        checkError(err);

        //log("Snapshot handle: " + snapshotHandlePtr.getValue(), Project.MSG_INFO);

        int options = 0;

        if (suppressOn) options |= Vix.VIX_VMPOWEROP_SUPPRESS_SNAPSHOT_POWERON;
        if (launchGui) options |= Vix.VIX_VMPOWEROP_LAUNCH_GUI;

        jobHandle = LibraryHelper.getInstance().VixVM_RevertToSnapshot(vmHandle,
                snapshotHandlePtr.getValue(),
                options,
                Vix.VIX_INVALID_HANDLE,
                null, null);

        LibraryHelper.getInstance().Vix_ReleaseHandle(snapshotHandlePtr.getValue());

        err = LibraryHelper.getInstance().VixJob_Wait(jobHandle, Vix.VIX_PROPERTY_NONE);
        LibraryHelper.getInstance().Vix_ReleaseHandle(jobHandle);
        checkError(err);

        finish();
    }
}
