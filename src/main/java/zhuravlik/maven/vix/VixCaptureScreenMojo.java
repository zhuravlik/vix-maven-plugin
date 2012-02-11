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

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: anton
 * Date: 11.02.12
 * Time: 20:27
 * To change this template use File | Settings | File Templates.
 */


/**
 * Goal which captures vm guest screenshot.
 *
 * @goal captureScreen
 *
 */
public class VixCaptureScreenMojo extends VixAbstractMojo {

    /**
     * Screenshot file path.
     * @parameter expression="${vix.path}"
     */
    private String path;

    public void execute() throws MojoExecutionException, MojoFailureException {

        initialize();
        login();


        int jobHandle = Vix.VIX_INVALID_HANDLE;
        jobHandle = LibraryHelper.getInstance().VixVM_CaptureScreenImage(vmHandle, Vix.VIX_CAPTURESCREENFORMAT_PNG,
                Vix.VIX_INVALID_HANDLE, null, null);

        IntByReference byteCount = new IntByReference();
        PointerByReference data = new PointerByReference();

        int err = LibraryHelper.getInstance().VixJob_Wait(jobHandle,
                Vix.VIX_PROPERTY_JOB_RESULT_SCREEN_IMAGE_DATA,
                byteCount, data,
                Vix.VIX_PROPERTY_NONE);
        LibraryHelper.getInstance().Vix_ReleaseHandle(jobHandle);

        logout();
        checkError(err);
        finish();

        Pointer p = data.getValue();
        byte[] bdata = p.getByteArray(0, byteCount.getValue());
        LibraryHelper.getInstance().Vix_FreeBuffer(p);

        try {
            new File(path).createNewFile();
            FileOutputStream fos = new FileOutputStream(new File(path));
            fos.write(bdata);
            fos.close();
        }
        catch (IOException e) {
            throw new MojoExecutionException("Unable to write screenshot to file " + path + ": " + e.getMessage());
        }
    }
}
