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
 * Time: 21:16
 * To change this template use File | Settings | File Templates.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which powers on VM.
 *
 * @goal powerOn
 *
 */
public class VixPowerOnMojo extends VixAbstractMojo {

    /**
     * Power on mode (normal or launch_gui).
     * @parameter expression="${vix.mode}"
     */
    private String mode;

    public void execute() throws MojoExecutionException, MojoFailureException {
		initialize();

		getLog().info(String.valueOf(vmHandle));

        getLog().info("Sending powerOn command");

        if (!mode.equals("normal") && !mode.equals("launch_gui")) {
            getLog().info("Unknown mode: " + mode + ", assumed normal");
        }

        int jobHandle = Vix.VIX_INVALID_HANDLE;

        jobHandle = LibraryHelper.getInstance().VixVM_PowerOn(vmHandle,
                mode.equals("normal") ? Vix.VIX_VMPOWEROP_NORMAL : Vix.VIX_VMPOWEROP_LAUNCH_GUI,
                Vix.VIX_INVALID_HANDLE, null, null);

        int err = LibraryHelper.getInstance().VixJob_Wait(jobHandle, Vix.VIX_PROPERTY_NONE);
        LibraryHelper.getInstance().Vix_ReleaseHandle(jobHandle);
        checkError(err);

		finish();
    }
}
