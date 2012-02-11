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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;


public abstract class VixAbstractMojo
    extends AbstractMojo
{
    /**
     * Location of the vmx file.
     * @parameter expression="${vmx.file.path}"
     * @required
     */
    private String vmxpath;


    /**
     * VIX API service provider.
     * @parameter expression="${vix.provider}"
     * @required
     */
    private String provider;

    /**
     * VIX API host.
     * @parameter expression="${vix.host}"
     */
    private String host;

    /**
     * VIX API host's port.
     * @parameter expression="${vix.port}"
     */
    private int port;


    /**
     * VIX API host's username.
     * @parameter expression="${vix.user}"
     */
    private String user;

    /**
     * VIX API host's password.
     * @parameter expression="${vix.password}"
     */
    private String password;


    /**
     * VIX API shared library path.
     * @parameter expression="${vix.libpath}"
     */
    private String libpath;


    /**
     * VIX API shared library path.
     * @parameter expression="${vix.ignoreerror}"
     */
    private boolean ignoreError;
    
    
    protected int vmHandle;
    protected int hostHandle;


    public void checkError(int err) throws MojoExecutionException {
        if (Vix.VIX_OK != err) {

            if (!ignoreError)
                throw new MojoExecutionException("VMWare error: " + LibraryHelper.getInstance().Vix_GetErrorText(err, null));
            else
                getLog().error("VMWare error: " + LibraryHelper.getInstance().Vix_GetErrorText(err, null));
        }
    }
    
    
    public void initialize() throws MojoExecutionException {
        if (libpath != null && libpath.length() > 0) {
            LibraryHelper.path = libpath;
        }

        int sprovider;

        if (provider.equals("vi"))  {
            sprovider = Vix.VIX_SERVICEPROVIDER_VMWARE_VI_SERVER;
            getLog().info("VIX Service Provider is VI Server or VMWare Server 2.0");
        }
        else if (provider.equals("server")) {
            sprovider = Vix.VIX_SERVICEPROVIDER_VMWARE_SERVER;
            getLog().info("VIX Service Provider is VMWare Server 1.0");
        }
        else if (provider.equals("workstation")) {
            sprovider = Vix.VIX_SERVICEPROVIDER_VMWARE_WORKSTATION;
            getLog().info("VIX Service Provider is VMWare Workstation");
        }
        else if (provider.equals("workstation_shared")) {
            sprovider = Vix.VIX_SERVICEPROVIDER_VMWARE_WORKSTATION_SHARED;
            getLog().info("VIX Service Provider is VMWare Workstation (shared mode)");
        }
        else if (provider.equals("player")) {
            sprovider = Vix.VIX_SERVICEPROVIDER_VMWARE_PLAYER;
            getLog().info("VIX Service Provider is VMWare Player");
        }
        else {
            getLog().warn("Unknown service provider: " + provider + ", assuming workstation");
            sprovider = Vix.VIX_SERVICEPROVIDER_VMWARE_WORKSTATION;
        }

        if (host != null) {
            getLog().info("Running remote session for host " + host + ":" + port);
        }
        else {
            getLog().info("Running local session");
            port = 0;
            user = null;
            password = null;
        }

        int jobHandle = Vix.VIX_INVALID_HANDLE;
        IntByReference hostHandlePtr = new IntByReference();

        getLog().info("Connecting");

        jobHandle = LibraryHelper.getInstance().VixHost_Connect(Vix.VIX_API_VERSION, sprovider,
                host, port, user, password, 0, Vix.VIX_INVALID_HANDLE, null, null);

        int err = LibraryHelper.getInstance().VixJob_Wait(jobHandle,
                Vix.VIX_PROPERTY_JOB_RESULT_HANDLE,
                hostHandlePtr,
                Vix.VIX_PROPERTY_NONE);

        LibraryHelper.getInstance().Vix_ReleaseHandle(jobHandle);
        checkError(err);

        getLog().info("Opening VM [" + vmxpath + "]");


        jobHandle = LibraryHelper.getInstance().VixHost_OpenVM(hostHandlePtr.getValue(), vmxpath,
                Vix.VIX_VMOPEN_NORMAL, Vix.VIX_INVALID_HANDLE, null, null);

        IntByReference vmHandlePtr = new IntByReference();

        err = LibraryHelper.getInstance().VixJob_Wait(jobHandle,
                Vix.VIX_PROPERTY_JOB_RESULT_HANDLE,
                vmHandlePtr,
                Vix.VIX_PROPERTY_NONE);

        checkError(err);

		vmHandle = vmHandlePtr.getValue();
        hostHandle = hostHandlePtr.getValue();
    }

    public void finish() {
        LibraryHelper.getInstance().VixHost_Disconnect(hostHandle);
        LibraryHelper.getInstance().Vix_ReleaseHandle(hostHandle);
    }
}
