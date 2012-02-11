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
 * Time: 21:09
 * To change this template use File | Settings | File Templates.
 */

/**
 * Goal which deletes snapshot with name specified.
 *
 * @goal deleteSnapshot
 *
 */
public class VixDeleteSnapshotMojo extends VixAbstractMojo {
    /**
     * Snapshot name.
     * @parameter expression="${vix.name}"
     */
    private String name;

    /**
     * Delete with children.
     * @parameter expression="${vix.withchildren}"
     */
    private boolean withChildren;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (name == null || name.length() == 0) {
            throw new MojoExecutionException("Snapshot name is not specified");
        }

        getLog().info("Removing snapshot [" + name + "]");

        int jobHandle = Vix.VIX_INVALID_HANDLE;
        IntByReference snapshotHandlePtr = new IntByReference();

        jobHandle = LibraryHelper.getInstance().VixVM_GetNamedSnapshot(vmHandle,
                name,
                snapshotHandlePtr);

        int err = LibraryHelper.getInstance().VixJob_Wait(jobHandle, Vix.VIX_PROPERTY_NONE);
        LibraryHelper.getInstance().Vix_ReleaseHandle(jobHandle);
        checkError(err);


        jobHandle = LibraryHelper.getInstance().VixVM_RemoveSnapshot(vmHandle,
                snapshotHandlePtr.getValue(),
                withChildren ? Vix.VIX_SNAPSHOT_REMOVE_CHILDREN : 0,
                null,
                null);

        LibraryHelper.getInstance().Vix_ReleaseHandle(snapshotHandlePtr.getValue());

        err = LibraryHelper.getInstance().VixJob_Wait(jobHandle, Vix.VIX_PROPERTY_NONE);
        LibraryHelper.getInstance().Vix_ReleaseHandle(jobHandle);
        checkError(err);
    }
}
