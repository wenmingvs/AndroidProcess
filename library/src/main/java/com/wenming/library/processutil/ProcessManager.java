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

package com.wenming.library.processutil;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Helper class to get a list of processes on Android.
 * <p/>
 * <p><b>Note:</b> Every method in this class should <i>not</i> be executed on the main thread.</p>
 */
public class ProcessManager {

    private ProcessManager() {
        throw new AssertionError("no instances");
    }

    /**
     * @return a list of <i>all</i> processes running on the device.
     */
    public static List<AndroidProcess> getRunningProcesses() {
        List<AndroidProcess> processes = new ArrayList<>();
        File[] files = new File("/proc").listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                int pid;
                try {
                    pid = Integer.parseInt(file.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                try {
                    processes.add(new AndroidProcess(pid));
                } catch (IOException e) {
                    // If you are running this from a third-party app, then system apps will not be
                    // readable on Android 5.0+ if SELinux is enforcing. You will need root access or an
                    // elevated SELinux context to read all files under /proc.
                    // See: https://su.chainfire.eu/#selinux
                }
            }
        }
        return processes;
    }

    /**
     * @return a list of all running app processes on the device.
     */
    public static List<AndroidAppProcess> getRunningAppProcesses() {
        List<AndroidAppProcess> processes = new ArrayList<>();
        File[] files = new File("/proc").listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                int pid;
                try {
                    pid = Integer.parseInt(file.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                try {
                    processes.add(new AndroidAppProcess(pid));
                } catch (AndroidAppProcess.NotAndroidAppProcessException ignored) {
                } catch (IOException e) {
                    // If you are running this from a third-party app, then system apps will not be
                    // readable on Android 5.0+ if SELinux is enforcing. You will need root access or an
                    // elevated SELinux context to read all files under /proc.
                    // See: https://su.chainfire.eu/#selinux
                }
            }
        }
        return processes;
    }

    /**
     * Get a list of user apps running in the foreground.
     *
     * @param ctx the application context
     * @return a list of user apps that are in the foreground.
     */
    public static List<AndroidAppProcess> getRunningForegroundApps(Context ctx) {
        List<AndroidAppProcess> processes = new ArrayList<>();
        File[] files = new File("/proc").listFiles();
        PackageManager pm = ctx.getPackageManager();
        for (File file : files) {
            if (file.isDirectory()) {
                int pid;
                try {
                    pid = Integer.parseInt(file.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                try {
                    AndroidAppProcess process = new AndroidAppProcess(pid);
                    if (!process.foreground) {
                        // Ignore processes not in the foreground
                        continue;
                    } else if (process.uid >= 1000 && process.uid <= 9999) {
                        // First app user starts at 10000. Ignore system processes.
                        continue;
                    } else if (process.name.contains(":")) {
                        // Ignore processes that are not running in the default app process.
                        continue;
                    } else if (pm.getLaunchIntentForPackage(process.getPackageName()) == null) {
                        // Ignore processes that the user cannot launch.
                        // TODO: remove this block?
                        continue;
                    }
                    processes.add(process);
                } catch (AndroidAppProcess.NotAndroidAppProcessException ignored) {
                } catch (IOException e) {
                    // If you are running this from a third-party app, then system apps will not be
                    // readable on Android 5.0+ if SELinux is enforcing. You will need root access or an
                    // elevated SELinux context to read all files under /proc.
                    // See: https://su.chainfire.eu/#selinux
                }
            }
        }
        return processes;
    }

    /**
     * @return {@code true} if this process is in the foreground.
     */
    public static boolean isMyProcessInTheForeground() {
        List<AndroidAppProcess> processes = getRunningAppProcesses();
        int myPid = android.os.Process.myPid();
        for (AndroidAppProcess process : processes) {
            if (process.pid == myPid && process.foreground) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of application processes that are running on the device.
     * <p/>
     * <p><b>NOTE:</b> On Lollipop (SDK 22) this does not provide
     * {@link RunningAppProcessInfo#pkgList},
     * {@link RunningAppProcessInfo#importance},
     * {@link RunningAppProcessInfo#lru},
     * {@link RunningAppProcessInfo#importanceReasonCode},
     * {@link RunningAppProcessInfo#importanceReasonComponent},
     * {@link RunningAppProcessInfo#importanceReasonPid},
     * etc. If you need more process information try using
     * {@link #getRunningAppProcesses()} or {@link android.app.usage.UsageStatsManager}</p>
     *
     * @return a list of RunningAppProcessInfo records, or null if there are no
     * running processes (it will not return an empty list).  This list ordering is not
     * specified.
     */
    public static List<RunningAppProcessInfo> getRunningAppProcessInfo(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            List<AndroidAppProcess> runningAppProcesses = ProcessManager.getRunningAppProcesses();
            List<RunningAppProcessInfo> appProcessInfos = new ArrayList<>();
            for (AndroidAppProcess process : runningAppProcesses) {
                RunningAppProcessInfo info = new RunningAppProcessInfo(
                        process.name, process.pid, null
                );
                info.uid = process.uid;
                // TODO: Get more information about the process. pkgList, importance, lru, etc.
                appProcessInfos.add(info);
            }
            return appProcessInfos;
        }
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        return am.getRunningAppProcesses();
    }

    /**
     * Get a list of running processes based on name, pid, ppid, or other conditions.
     * <p/>
     * <p>Example usage:</p>
     * <p/>
     * <pre>
     *   // Get all processes that contain the name "google"
     *   Filter filter = new ProcessManager.Filter().setName("google");
     *   List&lt;AndroidProcess&gt; processes = filter.run();
     * </pre>
     */
    @Deprecated
    public static class Filter {

        private String name;
        private int pid = -1;
        private int ppid = -1;
        private boolean apps;

        /**
         * @param name The name of the process to filter
         * @return This Filter object to allow for chaining of calls to set methods
         */
        @Deprecated
        public Filter setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * @param pid The process id to filter
         * @return This Filter object to allow for chaining of calls to set methods
         */
        @Deprecated
        public Filter setPid(int pid) {
            this.pid = pid;
            return this;
        }

        /**
         * @param ppid The parent process id to filter
         * @return This Filter object to allow for chaining of calls to set methods
         */
        @Deprecated
        public Filter setPpid(int ppid) {
            this.ppid = ppid;
            return this;
        }

        /**
         * @param apps {@code true} to only filter app processes
         * @return This Filter object to allow for chaining of calls to set methods
         */
        @Deprecated
        public Filter setApps(boolean apps) {
            this.apps = apps;
            return this;
        }

        /**
         * @return a List of processes based on the filter options.
         */
        @Deprecated
        public List<AndroidProcess> run() {
            List<AndroidProcess> processes = new ArrayList<>();
            File[] files = new File("/proc").listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    int pid;
                    try {
                        pid = Integer.parseInt(file.getName());
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    if (this.pid != -1 && pid != this.pid) {
                        continue;
                    }
                    try {
                        AndroidProcess process;
                        if (this.apps) {
                            process = new AndroidAppProcess(pid);
                        } else {
                            process = new AndroidProcess(pid);
                        }
                        if (this.name != null && !process.name.contains(this.name)) {
                            continue;
                        }
                        if (this.ppid != -1 && process.stat().ppid() != this.ppid) {
                            continue;
                        }
                        processes.add(process);
                    } catch (IOException e) {
                        // If you are running this from a third-party app, then system apps will not be
                        // readable on Android 5.0+ if SELinux is enforcing. You will need root access or an
                        // elevated SELinux context to read all files under /proc.
                        // See: https://su.chainfire.eu/#selinux
                    } catch (AndroidAppProcess.NotAndroidAppProcessException ignored) {
                    }
                }
            }
            return processes;
        }

    }

    /**
     * Comparator to list processes by name
     */
    public static final class ProcessComparator implements Comparator<AndroidProcess> {

        @Override
        public int compare(AndroidProcess p1, AndroidProcess p2) {
            return p1.name.compareToIgnoreCase(p2.name);
        }

    }

}
