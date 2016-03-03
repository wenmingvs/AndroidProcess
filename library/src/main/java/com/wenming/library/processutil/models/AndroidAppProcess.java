/*
 * Copyright (C) 2015. Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.wenming.library.processutil.models;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;

import com.wenming.library.processutil.ProcessManager;

import java.io.File;
import java.io.IOException;

public class AndroidAppProcess extends AndroidProcess {

    private static final boolean SYS_SUPPORTS_SCHEDGROUPS = new File("/dev/cpuctl/tasks").exists();

    /**
     * {@code true} if the process is in the foreground
     */
    public final boolean foreground;

    /**
     * The user id of this process.
     */
    public final int uid;

    public AndroidAppProcess(int pid) throws IOException, NotAndroidAppProcessException {
        super(pid);
        final boolean foreground;
        int uid;

        if (SYS_SUPPORTS_SCHEDGROUPS) {
            Cgroup cgroup = cgroup();
            ControlGroup cpuacct = cgroup.getGroup("cpuacct");
            ControlGroup cpu = cgroup.getGroup("cpu");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (cpu == null || cpuacct == null || !cpuacct.group.contains("pid_")) {
                    throw new NotAndroidAppProcessException(pid);
                }
                foreground = !cpu.group.contains("bg_non_interactive");
                try {
                    uid = Integer.parseInt(cpuacct.group.split("/")[1].replace("uid_", ""));
                } catch (Exception e) {
                    uid = status().getUid();
                }
                ProcessManager.log("name=%s, pid=%d, uid=%d, foreground=%b, cpuacct=%s, cpu=%s",
                        name, pid, uid, foreground, cpuacct.toString(), cpu.toString());
            } else {
                if (cpu == null || cpuacct == null || !cpu.group.contains("apps")) {
                    throw new NotAndroidAppProcessException(pid);
                }
                foreground = !cpu.group.contains("bg_non_interactive");
                try {
                    uid = Integer.parseInt(cpuacct.group.substring(cpuacct.group.lastIndexOf("/") + 1));
                } catch (Exception e) {
                    uid = status().getUid();
                }
                ProcessManager.log("name=%s, pid=%d, uid=%d foreground=%b, cpuacct=%s, cpu=%s",
                        name, pid, uid, foreground, cpuacct.toString(), cpu.toString());
            }
        } else {
            // this is a really ugly way to check if the process is an application.
            // we could possibly check the UID name (starts with "app_" or "u<USER_ID>_a")
            if (name.startsWith("/") || !new File("/data/data", getPackageName()).exists()) {
                throw new NotAndroidAppProcessException(pid);
            }
            Stat stat = stat();
            Status status = status();
            // https://github.com/android/platform_system_core/blob/jb-mr1-release/libcutils/sched_policy.c#L245-256
            foreground = stat.policy() == 0; // SCHED_NORMAL
            uid = status.getUid();
            ProcessManager.log("name=%s, pid=%d, uid=%d foreground=%b", name, pid, uid, foreground);
        }

        this.foreground = foreground;
        this.uid = uid;
    }

    /**
     * @return the app's package name
     * @see #name
     */
    public String getPackageName() {
        return name.split(":")[0];
    }

    /**
     * Retrieve overall information about the application package.
     * <p/>
     * <p>Throws {@link PackageManager.NameNotFoundException} if a package with the given name can
     * not be found on the system.</p>
     *
     * @param context the application context
     * @param flags   Additional option flags. Use any combination of
     *                {@link PackageManager#GET_ACTIVITIES}, {@link PackageManager#GET_GIDS},
     *                {@link PackageManager#GET_CONFIGURATIONS}, {@link PackageManager#GET_INSTRUMENTATION},
     *                {@link PackageManager#GET_PERMISSIONS}, {@link PackageManager#GET_PROVIDERS},
     *                {@link PackageManager#GET_RECEIVERS}, {@link PackageManager#GET_SERVICES},
     *                {@link PackageManager#GET_SIGNATURES}, {@link PackageManager#GET_UNINSTALLED_PACKAGES}
     *                to modify the data returned.
     * @return a PackageInfo object containing information about the package.
     */
    public PackageInfo getPackageInfo(Context context, int flags)
            throws PackageManager.NameNotFoundException {
        return context.getPackageManager().getPackageInfo(getPackageName(), flags);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (foreground ? 0x01 : 0x00));
        dest.writeInt(uid);
    }

    protected AndroidAppProcess(Parcel in) {
        super(in);
        foreground = in.readByte() != 0x00;
        uid = in.readInt();
    }

    public static final Creator<AndroidAppProcess> CREATOR = new Creator<AndroidAppProcess>() {

        @Override
        public AndroidAppProcess createFromParcel(Parcel source) {
            return new AndroidAppProcess(source);
        }

        @Override
        public AndroidAppProcess[] newArray(int size) {
            return new AndroidAppProcess[size];
        }
    };

    public static final class NotAndroidAppProcessException extends Exception {

        public NotAndroidAppProcessException(int pid) {
            super(String.format("The process %d does not belong to any application", pid));
        }
    }

}
