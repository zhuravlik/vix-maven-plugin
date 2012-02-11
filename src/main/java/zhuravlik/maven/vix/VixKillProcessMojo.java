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
 * Time: 22:02
 * To change this template use File | Settings | File Templates.
 */

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which kills process in guest.
 *
 * @goal killProcess
 *
 */
public class VixKillProcessMojo extends VixAbstractMojo {
    /**
     * Process name.
     * @parameter expression="${vix.name}"
     */
    private String name;

    /**
     * Process command line.
     * @parameter expression="${vix.cmdline}"
     */
    private String cmdline;

    /**
     * Match strictly or as substring.
     * @parameter expression="${vix.strict}"
     */
    private boolean strict;


    public void execute() throws MojoExecutionException, MojoFailureException {

        initialize();
        login();

        if (name == null && cmdline == null)
            throw new MojoExecutionException("Process name or command line are not specified");

        if (name != null && cmdline != null)
            throw new MojoExecutionException("Please specify process name OR command line, both together are not supported");

        getLog().info("Killing process with " + (name != null ? "name " : "command line ")
                + (strict ? "" : "containing ")
                + "[" + (name != null ? name : cmdline) + "]");

        int jobHandle = Vix.VIX_INVALID_HANDLE;

        jobHandle = LibraryHelper.getInstance().VixVM_ListProcessesInGuest(vmHandle, 0, null, null);

        int err = LibraryHelper.getInstance().VixJob_Wait(jobHandle, Vix.VIX_PROPERTY_NONE);
        checkError(err);


        int num = LibraryHelper.getInstance().VixJob_GetNumProperties(jobHandle, Vix.VIX_PROPERTY_JOB_RESULT_ITEM_NAME);

        for (int j = 0; j < num; j++) {
            PointerByReference processName = new PointerByReference();
            PointerByReference cmdLinePtr = new PointerByReference();
            IntByReference pid = new IntByReference();

            err = LibraryHelper.getInstance().VixJob_GetNthProperties(jobHandle, j,
                    Vix.VIX_PROPERTY_JOB_RESULT_ITEM_NAME, processName,
                    Vix.VIX_PROPERTY_JOB_RESULT_PROCESS_COMMAND, cmdLinePtr,
                    Vix.VIX_PROPERTY_JOB_RESULT_PROCESS_ID, pid,
                    Vix.VIX_PROPERTY_NONE);
            checkError(err);

            String pname = processName.getValue().getString(0);
            LibraryHelper.getInstance().Vix_FreeBuffer(processName.getValue());

            String cmd = cmdLinePtr.getValue().getString(0);
            LibraryHelper.getInstance().Vix_FreeBuffer(cmdLinePtr.getValue());

            if (name != null ?
                    (strict && name.equals(pname) || name.contains(pname)) :
                    (strict && cmdline.equals(cmd) || cmdline.contains(cmd))
                    ) {
                int ijobHandle = LibraryHelper.getInstance().VixVM_KillProcessInGuest(vmHandle, pid.getValue(), 0, null, null);
                err = LibraryHelper.getInstance().VixJob_Wait(ijobHandle, Vix.VIX_PROPERTY_NONE);
                checkError(err);
                LibraryHelper.getInstance().Vix_ReleaseHandle(ijobHandle);
            }
        }

        LibraryHelper.getInstance().Vix_ReleaseHandle(jobHandle);

        logout();
        finish();
    }
}
