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
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.IOException;

public class AndroidProcess implements Parcelable {

  /**
   * Get the name of a running process.
   *
   * @param pid
   *     the process id.
   * @return the name of the process.
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   */
  static String getProcessName(int pid) throws IOException {
    String cmdline = null;
    try {
      cmdline = ProcFile.readFile(String.format("/proc/%d/cmdline", pid)).trim();
    } catch (IOException ignored) {
    }
    if (TextUtils.isEmpty(cmdline)) {
      return Stat.get(pid).getComm();
    }
    return cmdline;
  }

  /** the process name */
  public final String name;

  /** the process id */
  public final int pid;

  /**
   * AndroidProcess constructor
   *
   * @param pid
   *     the process id
   * @throws IOException
   *     if /proc/[pid] does not exist or we don't have read access.
   */
  public AndroidProcess(int pid) throws IOException {
    this.pid = pid;
    this.name = getProcessName(pid);
  }

  /**
   * Read the contents of a file in /proc/[pid]/[filename].
   *
   * @param filename
   *     the relative path to the file.
   * @return the contents of the file.
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   */
  public String read(String filename) throws IOException {
    return ProcFile.readFile(String.format("/proc/%d/%s", pid, filename));
  }

  /**
   * <p>/proc/[pid]/attr/current (since Linux 2.6.0)</p>
   *
   * <p>The contents of this file represent the current security attributes of the process.</p>
   *
   * <p>In SELinux, this file is used to get the security context of a process. Prior to Linux
   * 2.6.11, this file could not be used to set the security context (a write was always denied),
   * since SELinux limited process security transitions to execve(2) (see the description of
   * /proc/[pid]/attr/exec, below).  ince Linux 2.6.11, SELinux lifted this restriction and began
   * supporting "set" operations via writes to this node if authorized by policy, although use of
   * this operation is only suitable for applications that are trusted to maintain any desired
   * separation between the old and new security contexts.  Prior to Linux 2.6.28, SELinux did not
   * allow threads within a multi- threaded process to set their security context via this node as
   * it would yield an inconsistency among the security contexts of the threads sharing the same
   * memory space. Since Linux 2.6.28, SELinux lifted this restriction and began supporting "set"
   * operations for threads within a multithreaded process if the new security context is bounded
   * by the old security context, where the bounded relation is defined in policy and guarantees
   * that the new security context has a subset of the permissions of the old security context.
   * Other security modules may choose to support "set" operations via writes to this node.</p>
   *
   * @return the contents of /proc/[pid]/attr/current
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   */
  public String attr_current() throws IOException {
    return read("attr/current");
  }

  /**
   * <p>/proc/[pid]/cmdline</p>
   *
   * <p>This read-only file holds the complete command line for the process, unless the process is
   * a zombie. In the latter case, there is nothing in this file: that is, a read on this file will
   * return 0 characters. The command-line arguments appear in this file as a set of strings
   * separated by null bytes ('\0'), with a further null byte after the last string.</p>
   *
   * @return the name of the process. (note: process name may be empty. In case it is empty get
   * the process name from /proc/[pid]/stat).
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   * @see #name
   */
  public String cmdline() throws IOException {
    return read("cmdline");
  }

  /**
   * <p>/proc/[pid]/cgroup (since Linux 2.6.24)</p>
   *
   * <p>This file describes control groups to which the process/task belongs. For each cgroup
   * hierarchy there is one entry containing colon-separated fields of the form:</p>
   *
   * <p>5:cpuacct,cpu,cpuset:/daemons</p>
   *
   * <p>The colon-separated fields are, from left to right:</p>
   *
   * <ol>
   * <li>hierarchy ID number</li>
   * <li>set of subsystems bound to the hierarchy</li>
   * <li>control group in the hierarchy to which the process belongs</li>
   * </ol>
   *
   * <p>This file is present only if the CONFIG_CGROUPS kernel configuration option is enabled.</p>
   *
   * @return the {@link Cgroup} for this process
   * @throws IOException
   */
  public Cgroup cgroup() throws IOException {
    return Cgroup.get(pid);
  }

  /**
   * <p>/proc/[pid]/oom_score (since Linux 2.6.11)</p>
   *
   * <p>This file displays the current score that the kernel gives to this process for the
   * purpose of selecting a process for the OOM-killer. A higher score means that the
   * process is more likely to be selected by the OOM-killer.</p>
   *
   * <p>The basis for this score is the amount of memory used by the process, with
   * increases (+) or decreases (-) for factors including:</p>
   *
   * <ul>
   * <li>whether the process creates a lot of children using fork(2)(+);</li>
   * <li>whether the process has been running a long time, or has used a lot of CPU time (-);</li>
   * <li>whether the process has a low nice value (i.e., &gt; 0) (+);</li>
   * <li>whether the process is privileged (-); and</li>
   * <li>whether the process is making direct hardware access (-).</li>
   * </ul>
   *
   * <p>The oom_score also reflects the adjustment specified by the oom_score_adj
   * or oom_adj setting for the process.</p>
   *
   * @return the oom_score value for this process
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   */
  public int oom_score() throws IOException {
    return Integer.parseInt(read("oom_score"));
  }

  /**
   * <p>/proc/[pid]/oom_adj (since Linux 2.6.11)</p>
   *
   * <p>This file can be used to adjust the score used to select which process should be killed in
   * an out-of-memory (OOM) situation. The kernel uses this value for a bit-shift operation of the
   * process's oom_score value: valid values are in the* range -16 to +15, plus the special value
   * -17, which disables OOM-killing altogether for this process. A positive score increases the
   * likelihood of this process being killed by the OOM-killer; a negative score decreases the
   * likelihood.</p>
   *
   * <p>The default value for this file is 0; a new process inherits its parent's oom_adj setting.
   * A process must be privileged (CAP_SYS_RESOURCE) to update this file.</p>
   *
   * <p>Since Linux 2.6.36, use of this file is deprecated in favor of
   * /proc/[pid]/oom_score_adj.</p>
   *
   * @return the oom_adj value for this process
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   */
  public int oom_adj() throws IOException {
    return Integer.parseInt(read("oom_adj"));
  }

  /**
   * <p>/proc/[pid]/oom_score_adj (since Linux 2.6.36)</p>
   *
   * <p>This file can be used to adjust the badness heuristic used to select which process gets
   * killed in out-of-memory conditions.</p>
   *
   * <p>The badness heuristic assigns a value to each candidate task ranging from 0 (never kill) to
   * 1000 (always kill) to determine which process is targeted. The units are roughly a proportion
   * along that range of allowed memory the process may allocate from, based on an estimation of
   * its current memory and swap use. For example, if a task is using all allowed memory, its
   * badness score will be 1000.  If it is using half of its allowed memory, its score will be
   * 500.</p>
   *
   * <p>There is an additional factor included in the badness score: root processes are given 3%
   * extra memory over other tasks.</p>
   *
   * <p>The amount of "allowed" memory depends on the context in which the OOM-killer was called.
   * If it is due to the memory assigned to the allocating task's cpuset being exhausted, the
   * allowed memory represents the set of mems assigned to that cpuset (see cpuset(7)). If it is
   * due to a mempolicy's node(s) being exhausted, the allowed memory represents the set of
   * mempolicy nodes. If it is due to a memory limit (or swap limit) being reached, the allowed
   * memory is that configured limit. Finally, if it is due to the entire system being out of
   * memory, the allowed memory represents all allocatable resources.</p>
   *
   * <p>The value of oom_score_adj is added to the badness score before it is used to determine
   * which task to kill.  Acceptable values range from -1000 (OOM_SCORE_ADJ_MIN) to +1000
   * (OOM_SCORE_ADJ_MAX). This allows user space to control the preference for OOM-killing, ranging
   * from always preferring a certain task or completely disabling it from OOM killing.  The lowest
   * possible value, -1000, is equivalent to disabling OOM- killing entirely for that task, since
   * it will always report a badness score of 0.</p>
   *
   * <p>Consequently, it is very simple for user space to define the amount of memory to consider
   * for each task.  Setting a oom_score_adj value of +500, for example, is roughly equivalent to
   * allowing the remainder of tasks sharing the same system, cpuset, mempolicy, or memory
   * controller resources to use at least 50% more memory.  A value of -500, on the other hand,
   * would be roughly equivalent to discounting 50% of the task's allowed memory from being
   * considered as scoring against the task.</p>
   *
   * <p>For backward compatibility with previous kernels, /proc/[pid]/oom_adj can still be used to
   * tune the badness score.  Its value is scaled linearly with oom_score_adj.</p>
   *
   * <p>Writing to /proc/[pid]/oom_score_adj or /proc/[pid]/oom_adj will change the other with its
   * scaled value.</p>
   *
   * @return the oom_score_adj value for this process
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   */
  public int oom_score_adj() throws IOException {
    return Integer.parseInt(read("oom_score_adj"));
  }

  /**
   * <p>/proc/[pid]/stat</p>
   *
   * <p>Status information about the process.  This is used by ps(1). It is defined in the kernel
   * source file fs/proc/array.c.</p>
   *
   * <p>The fields, in order, with their proper scanf(3) format specifiers, are:</p>
   *
   * <ol>
   *
   * <li>pid  %d The process ID.</li>
   *
   * <li>comm  %s The filename of the executable, in parentheses. This is visible whether or not
   * the executable is swapped out.</li>
   *
   * <li>state  %c One of the following characters, indicating process state:
   * <ul>
   * <li>R  Running</li>
   * <li>S  Sleeping in an interruptible wait</li>
   * <li>D  Waiting in uninterruptible disk sleep</li>
   * <li>Z  Zombie</li>
   * <li>T  Stopped (on a signal) or (before Linux 2.6.33) trace stopped</li>
   * <li>t  Tracing stop (Linux 2.6.33 onward)</li>
   * <li>W  Paging (only before Linux 2.6.0)</li>
   * <li>X  Dead (from Linux 2.6.0 onward)</li>
   * <li>x  Dead (Linux 2.6.33 to 3.13 only)</li>
   * <li>K  Wakekill (Linux 2.6.33 to 3.13 only)</li>
   * <li>W  Waking (Linux 2.6.33 to 3.13 only)</li>
   * <li>P  Parked (Linux 3.9 to 3.13 only)</li>
   * </ul>
   * </li>
   *
   * <li>ppid %d The PID of the parent of this process.</li>
   *
   * <li>pgrp %d The process group ID of the process.</li>
   *
   * <li>session %d The session ID of the process.</li>
   *
   * <li>tty_nr %d The controlling terminal of the process.  (The minor device number is contained
   * in the combination of bits 31 to 20 and 7 to 0; the major device number is in bits 15 to 8.)
   * </li>
   *
   * <li>tpgid %d The ID of the foreground process group of the controlling terminal of the
   * process.</li>
   *
   * <li>flags %u The kernel flags word of the process.  For bit meanings, see the PF_* defines in
   * the Linux kernel source file include/linux/sched.h.  Details depend on the kernel version.
   * The format for this field was %lu before Linux 2.6.</li>
   *
   * <li>minflt %lu The number of minor faults the process has made which have not required
   * loading a memory page from disk.</li>
   *
   * <li>cminflt %lu The number of minor faults that the process's waited-for children have
   * made</li>
   *
   * <li>majflt  %lu The number of major faults the process has made which have required loading a
   * memory page from disk.</li>
   *
   * <li>cmajflt  %lu The number of major faults that the process's waited-for children have
   * made</li>
   *
   * <li>utime  %lu Amount of time that this process has been scheduled in user mode, measured in
   * clock ticks (divide by sysconf(_SC_CLK_TCK)).  This includes guest time,   guest_time (time
   * spent running a virtual CPU, see below), so that applications that are not aware of the guest
   * time field do not lose that time from their calculations.</li>
   *
   * <li>stime  %lu Amount of time that this process has been scheduled in kernel mode, measured
   * in clock ticks (divide by sysconf(_SC_CLK_TCK)).</li>
   *
   * <li>cutime  %ld Amount of time that this process's waited-for children have been scheduled in
   * user mode, measured in clock ticks (divide by sysconf(_SC_CLK_TCK)). (See also times(2).)
   * This includes guest time, cguest_time (time spent running a virtual CPU, see below).</li>
   *
   * <li>cstime  %ld Amount of time that this process's waited-for children have been scheduled in
   * kernel mode, measured in clock ticks (divide by sysconf(_SC_CLK_TCK)).</li>
   *
   * <li>priority  %ld (Explanation for Linux 2.6) For processes running a real-time scheduling
   * policy (policy below; see sched_setscheduler(2)), this is the negated scheduling priority,
   * minus one; that is, a number in the range -2 to -100, corresponding to real-time priorities 1
   * to 99.  For processes running under a non-real-time scheduling policy, this is the raw nice
   * value (setpriority(2)) as represented in the kernel.  The kernel stores nice values as numbers
   * in the range 0 (high) to 39 (low), corresponding to the user-visible nice range of -20 to 19.
   * Before Linux 2.6, this was a scaled value based on the scheduler weighting given to this
   * process</li>
   *
   * <li>nice  %ld The nice value (see setpriority(2)), a value in the range 19 (low priority) to
   * -20 (high priority).</li>
   *
   * <li>num_threads  %ld Number of threads in this process (since Linux 2.6). Before kernel 2.6,
   * this field was hard coded to 0 as a placeholder for an earlier removed field.</li>
   *
   * <li>itrealvalue  %ld The time in jiffies before the next SIGALRM is sent to the process due
   * to an interval timer.  Since kernel 2.6.17, this field is no longer maintained, and is hard
   * coded as 0.</li>
   *
   * <li>starttime  %llu The time the process started after system boot.  In kernels before Linux
   * 2.6, this value was expressed in jiffies.  Since Linux 2.6, the value is expressed in clock
   * ticks (divide by sysconf(_SC_CLK_TCK)).</li>
   *
   * <li>The format for this field was %lu before Linux 2.6.  (23) vsize  %lu Virtual memory size
   * in bytes.</li>
   *
   * <li>rss  %ld Resident Set Size: number of pages the process has in real memory.  This is just
   * the pages which count toward text, data, or stack space.  This does not include pages which
   * have not been demand-loaded in, or which are swapped out.</li>
   *
   * <li>rsslim  %lu Current soft limit in bytes on the rss of the process; see the description of
   * RLIMIT_RSS in getrlimit(2).</li>
   *
   * <li>startcode  %lu The address above which program text can run.</li>
   *
   * <li>endcode  %lu The address below which program text can run.</li>
   *
   * <li>startstack  %lu The address of the start (i.e., bottom) of the stack.</li>
   *
   * <li>kstkesp  %lu The current value of ESP (stack pointer), as found in the kernel stack page
   * for the process.</li>
   *
   * <li>kstkeip  %lu The current EIP (instruction pointer).</li>
   *
   * <li>signal  %lu The bitmap of pending signals, displayed as a decimal number.  Obsolete,
   * because it does not provide information on real-time signals; use /proc/[pid]/status
   * instead</li>
   *
   * <li>blocked  %lu The bitmap of blocked signals, displayed as a decimal number.  Obsolete,
   * because it does not provide information on real-time signals; use /proc/[pid]/status
   * instead</li>
   *
   * <li>sigignore  %lu The bitmap of ignored signals, displayed as a decimal number. Obsolete,
   * because it does not provide information on real-time signals; use /proc/[pid]/status
   * instead</li>
   *
   * <li>sigcatch  %lu The bitmap of caught signals, displayed as a decimal number. Obsolete,
   * because it does not provide information on real-time signals; use /proc/[pid]/status
   * instead.</li>
   *
   * <li>wchan  %lu This is the "channel" in which the process is waiting.  It is the address of a
   * location in the kernel where the process is sleeping.  The corresponding symbolic name can be
   * found in /proc/[pid]/wchan.</li>
   *
   * <li>nswap  %lu Number of pages swapped (not maintained).</li>
   *
   * <li>cnswap  %lu Cumulative nswap for child processes (not maintained).</li>
   *
   * <li>exit_signal  %d  (since Linux 2.1.22) Signal to be sent to parent when we die.</li>
   *
   * <li>processor  %d  (since Linux 2.2.8) CPU number last executed on.</li>
   *
   * <li>rt_priority  %u  (since Linux 2.5.19) Real-time scheduling priority, a number in the
   * range 1 to 99 for processes scheduled under a real-time policy, or 0, for non-real-time
   * processes (see sched_setscheduler(2)).</li>
   *
   * <li>policy  %u  (since Linux 2.5.19) Scheduling policy (see sched_setscheduler(2)). Decode
   * using the SCHED_* constants in linux/sched.h.  The format for this field was %lu before Linux
   * 2.6.22.</li>
   *
   * <li>delayacct_blkio_ticks  %llu  (since Linux 2.6.18) Aggregated block I/O delays, measured
   * in clock ticks (centiseconds).</li>
   *
   * <li>guest_time  %lu  (since Linux 2.6.24) Guest time of the process (time spent running a
   * virtual CPU for a guest operating system), measured in clock ticks (divide by
   * sysconf(_SC_CLK_TCK)).</li>
   *
   * <li>cguest_time  %ld  (since Linux 2.6.24) Guest time of the process's children, measured in
   * clock ticks (divide by sysconf(_SC_CLK_TCK)).</li>
   *
   * <li>start_data  %lu  (since Linux 3.3) Address above which program initialized and
   * uninitialized (BSS) data are placed.</li>
   *
   * <li>end_data  %lu  (since Linux 3.3) Address below which program initialized and
   * uninitialized (BSS) data are placed.</li>
   *
   * <li>start_brk  %lu  (since Linux 3.3) Address above which program heap can be expanded with
   * brk(2).</li>
   *
   * <li>arg_start  %lu  (since Linux 3.5) Address above which program command-line arguments
   * (argv) are placed.</li>
   *
   * <li>arg_end  %lu  (since Linux 3.5) Address below program command-line arguments (argv) are
   * placed.</li>
   *
   * <li>env_start  %lu  (since Linux 3.5) Address above which program environment is placed.</li>
   *
   * <li>env_end  %lu  (since Linux 3.5) Address below which program environment is placed.</li>
   *
   * <li>exit_code  %d  (since Linux 3.5) The thread's exit status in the form reported by
   * waitpid(2).</li>
   *
   * </ol>
   *
   * @return the {@link Stat} for this process
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   */
  public Stat stat() throws IOException {
    return Stat.get(pid);
  }

  /**
   * <p>Provides information about memory usage, measured in pages.</p>
   *
   * <p>The columns are:</p>
   *
   * <ul>
   * <li>size       (1) total program size (same as VmSize in /proc/[pid]/status)</li>
   * <li>resident   (2) resident set size (same as VmRSS in /proc/[pid]/status)</li>
   * <li>share      (3) shared pages (i.e., backed by a file)</li>
   * <li>text       (4) text (code)</li>
   * <li>lib        (5) library (unused in Linux 2.6)</li>
   * <li>data       (6) data + stack</li>
   * <li>dt         (7) dirty pages (unused in Linux 2.6)</li>
   * </ul>
   *
   * @return the {@link Statm} for this process
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   */
  public Statm statm() throws IOException {
    return Statm.get(pid);
  }

  /**
   * <p>/proc/[pid]/status</p>
   *
   * <p>Provides much of the information in /proc/[pid]/stat and /proc/[pid]/statm in a format
   * that's
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
   * <li>VmSwap: Swapped-out virtual memory size by anonymous private pages; shmem swap usage is
   * not
   * included (since Linux 2.6.34).</li>
   * <li>Threads: Number of threads in process containing this thread.</li>
   * <li>SigQ: This field contains two slash-separated numbers that relate to queued signals for
   * the
   * real user ID of this process.  The first of these is the number of currently queued signals
   * for
   * this real user ID, and the second is the resource limit on the number of queued signals for
   * this
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
   * voluntary_ctxt_switches, nonvoluntary_ctxt_switches: Number of voluntary and involuntary
   * context
   * switches (since Linux 2.6.23).</li>
   * </ol>
   *
   * @return the {@link Status} for this process
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   */
  public Status status() throws IOException {
    return Status.get(pid);
  }

  /**
   * The symbolic name corresponding to the location in the kernel where the process is sleeping.
   *
   * @return the contents of /proc/[pid]/wchan
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   */
  public String wchan() throws IOException {
    return read("wchan");
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.name);
    dest.writeInt(this.pid);
  }

  protected AndroidProcess(Parcel in) {
    this.name = in.readString();
    this.pid = in.readInt();
  }

  public static final Creator<AndroidProcess> CREATOR = new Creator<AndroidProcess>() {

    @Override public AndroidProcess createFromParcel(Parcel source) {
      return new AndroidProcess(source);
    }

    @Override public AndroidProcess[] newArray(int size) {
      return new AndroidProcess[size];
    }
  };

}
