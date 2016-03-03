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

import android.os.Parcel;

import java.io.IOException;

/**
 * <p>/proc/[pid]/status</p>
 *
 * <p>Provides much of the information in /proc/[pid]/stat and /proc/[pid]/statm in a format that's
 * easier for humans to parse.</p>
 *
 * <p>Here's an example:</p>
 *
 * <pre>
 * $ cat /proc/$$/status
 * Name:   bash
 * State:  S (sleeping)
 * Tgid:   3515
 * Pid:    3515
 * PPid:   3452
 * TracerPid:      0
 * Uid:    1000    1000    1000    1000
 * Gid:    100     100     100     100
 * FDSize: 256
 * Groups: 16 33 100
 * VmPeak:     9136 kB
 * VmSize:     7896 kB
 * VmLck:         0 kB
 * VmPin:         0 kB
 * VmHWM:      7572 kB
 * VmRSS:      6316 kB
 * VmData:     5224 kB
 * VmStk:        88 kB
 * VmExe:       572 kB
 * VmLib:      1708 kB
 * VmPMD:         4 kB
 * VmPTE:        20 kB
 * VmSwap:        0 kB
 * Threads:        1
 * SigQ:   0/3067
 * SigPnd: 0000000000000000
 * ShdPnd: 0000000000000000
 * SigBlk: 0000000000010000
 * SigIgn: 0000000000384004
 * SigCgt: 000000004b813efb
 * CapInh: 0000000000000000
 * CapPrm: 0000000000000000
 * CapEff: 0000000000000000
 * CapBnd: ffffffffffffffff
 * Seccomp:        0
 * Cpus_allowed:   00000001
 * Cpus_allowed_list:      0
 * Mems_allowed:   1
 * Mems_allowed_list:      0
 * voluntary_ctxt_switches:        150
 * nonvoluntary_ctxt_switches:     545
 * </pre>
 *
 * <p>The fields are as follows:</p>
 *
 * <ol>
 * <li>Name: Command run by this process.</li>
 * <li>State: Current state of the process.  One of "R (running)", "S (sleeping)", "D (disk
 * sleep)",
 * "T (stopped)", "T (tracing stop)", "Z (zombie)", or "X (dead)".</li>
 * <li>Tgid: Thread group ID (i.e., Process ID).</li>
 * <li>Pid: Thread ID (see gettid(2)).</li>
 * <li>PPid: PID of parent process.</li>
 * <li>TracerPid: PID of process tracing this process (0 if not being traced).</li>
 * <li>Uid, Gid: Real, effective, saved set, and filesystem UIDs (GIDs).</li>
 * <li>FDSize: Number of file descriptor slots currently allocated.</li>
 * <li>Groups: Supplementary group list.</li>
 * <li>VmPeak: Peak virtual memory size.</li>
 * <li>VmSize: Virtual memory size.</li>
 * <li>VmLck: Locked memory size (see mlock(3)).</li>
 * <li>VmPin: Pinned memory size (since Linux 3.2).  These are pages that can't be moved because
 * something needs to directly access physical memory.</li>
 * <li>VmHWM: Peak resident set size ("high water mark").</li>
 * <li>VmRSS: Resident set size.</li>
 * <li>VmData, VmStk, VmExe: Size of data, stack, and text segments.</li>
 * <li>VmLib: Shared library code size.</li>
 * <li>VmPTE: Page table entries size (since Linux 2.6.10).</li>
 * <li>VmPMD: Size of second-level page tables (since Linux 4.0).</li>
 * <li>VmSwap: Swapped-out virtual memory size by anonymous private pages; shmem swap usage is not
 * included (since Linux 2.6.34).</li>
 * <li>Threads: Number of threads in process containing this thread.</li>
 * <li>SigQ: This field contains two slash-separated numbers that relate to queued signals for the
 * real user ID of this process.  The first of these is the number of currently queued signals for
 * this real user ID, and the second is the resource limit on the number of queued signals for this
 * process (see the description of RLIMIT_SIGPENDING in getrlimit(2)).</li>
 * <li>SigPnd, ShdPnd: Number of signals pending for thread and for process as a whole (see
 * pthreads(7) and signal(7)).</li>
 * <li>SigBlk, SigIgn, SigCgt: Masks indicating signals being blocked, ignored, and caught (see
 * signal(7)).</li>
 * <li>CapInh, CapPrm, CapEff: Masks of capabilities enabled in inheritable, permitted, and
 * effective sets (see capabilities(7)).</li>
 * <li>CapBnd: Capability Bounding set (since Linux 2.6.26, see capabilities(7)).</li>
 * <li>Seccomp: Seccomp mode of the process (since Linux 3.8, see seccomp(2)). 0 means
 * SECCOMP_MODE_DISABLED; 1 means SECCOMP_MODE_STRICT; 2 means SECCOMP_MODE_FILTER. This field is
 * provided only if the kernel was built with the CONFIG_SECCOMP kernel configuration option
 * enabled.</li>
 * <li>Cpus_allowed: Mask of CPUs on which this process may run (since Linux 2.6.24, see
 * cpuset(7)).</li>
 * <li>Cpus_allowed_list: Same as previous, but in "list format" (since Linux 2.6.26, see
 * cpuset(7)).</li>
 * <li>Mems_allowed: Mask of memory nodes allowed to this process (since Linux 2.6.24, see
 * cpuset(7)).</li>
 * <li>Mems_allowed_list: Same as previous, but in "list format" (since Linux 2.6.26, see
 * cpuset(7)).
 * voluntary_ctxt_switches, nonvoluntary_ctxt_switches: Number of voluntary and involuntary context
 * switches (since Linux 2.6.23).</li>
 * </ol>
 */
public final class Status extends ProcFile {

  /**
   * Read /proc/[pid]/status.
   *
   * @param pid
   *     the process id.
   * @return the {@link Status}
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   */
  public static Status get(int pid) throws IOException {
    return new Status(String.format("/proc/%d/status", pid));
  }

  private Status(String path) throws IOException {
    super(path);
  }

  private Status(Parcel in) {
    super(in);
  }

  /**
   * Get the value of one of the fields.
   *
   * @param fieldName
   *     the field name. E.g "PPid", "Uid", "Groups".
   * @return The value of the field or {@code null}.
   */
  public String getValue(String fieldName) {
    String[] lines = content.split("\n");
    for (String line : lines) {
      if (line.startsWith(fieldName + ":")) {
        return line.split(fieldName + ":")[1].trim();
      }
    }
    return null;
  }

  /**
   * @return The process' UID or -1 if parsing the UID failed.
   */
  public int getUid() {
    try {
      return Integer.parseInt(getValue("Uid").split("\\s+")[0]);
    } catch (Exception e) {
      return -1;
    }
  }

  /**
   * @return The process' GID or -1 if parsing the GID failed.
   */
  public int getGid() {
    try {
      return Integer.parseInt(getValue("Gid").split("\\s+")[0]);
    } catch (Exception e) {
      return -1;
    }
  }

  public static final Creator<Status> CREATOR = new Creator<Status>() {

    @Override public Status createFromParcel(Parcel source) {
      return new Status(source);
    }

    @Override public Status[] newArray(int size) {
      return new Status[size];
    }
  };

}
