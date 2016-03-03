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

import java.io.IOException;

/**
 * <p>/proc/[pid]/stat</p>
 *
 * <p>Status information about the process.  This is used by ps(1). It is defined in the kernel
 * source file fs/proc/array.c.</p>
 *
 * <p>The fields, in order, with their proper scanf(3) format specifiers, are:</p>
 *
 * <ol>
 * <li>pid  %d The process ID.</li>
 * <li>comm  %s The filename of the executable, in parentheses. This is visible whether or not
 * the executable is swapped out.</li>
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
 * <li>ppid %d The PID of the parent of this process.</li>
 * <li>pgrp %d The process group ID of the process.</li>
 * <li>session %d The session ID of the process.</li>
 * <li>tty_nr %d The controlling terminal of the process.  (The minor device number is contained
 * in the combination of bits 31 to 20 and 7 to 0; the major device number is in bits 15 to 8.)
 * </li>
 * <li>tpgid %d The ID of the foreground process group of the controlling terminal of the
 * process.</li>
 * <li>flags %u The kernel flags word of the process.  For bit meanings, see the PF_* defines in
 * the Linux kernel source file include/linux/sched.h.  Details depend on the kernel version.
 * The format for this field was %lu before Linux 2.6.</li>
 * <li>minflt %lu The number of minor faults the process has made which have not required
 * loading a memory page from disk.</li>
 * <li>cminflt %lu The number of minor faults that the process's waited-for children have
 * made</li>
 * <li>majflt  %lu The number of major faults the process has made which have required loading a
 * memory page from disk.</li>
 * <li>cmajflt  %lu The number of major faults that the process's waited-for children have
 * made</li>
 * <li>utime  %lu Amount of time that this process has been scheduled in user mode, measured in
 * clock ticks (divide by sysconf(_SC_CLK_TCK)).  This includes guest time,   guest_time (time
 * spent running a virtual CPU, see below), so that applications that are not aware of the guest
 * time field do not lose that time from their calculations.</li>
 * <li>stime  %lu Amount of time that this process has been scheduled in kernel mode, measured
 * in clock ticks (divide by sysconf(_SC_CLK_TCK)).</li>
 * <li>cutime  %ld Amount of time that this process's waited-for children have been scheduled in
 * user mode, measured in clock ticks (divide by sysconf(_SC_CLK_TCK)). (See also times(2).)
 * This includes guest time, cguest_time (time spent running a virtual CPU, see below).</li>
 * <li>cstime  %ld Amount of time that this process's waited-for children have been scheduled in
 * kernel mode, measured in clock ticks (divide by sysconf(_SC_CLK_TCK)).</li>
 * <li>priority  %ld (Explanation for Linux 2.6) For processes running a real-time scheduling
 * policy (policy below; see sched_setscheduler(2)), this is the negated scheduling priority,
 * minus one; that is, a number in the range -2 to -100, corresponding to real-time priorities 1
 * to 99.  For processes running under a non-real-time scheduling policy, this is the raw nice
 * value (setpriority(2)) as represented in the kernel.  The kernel stores nice values as numbers
 * in the range 0 (high) to 39 (low), corresponding to the user-visible nice range of -20 to 19.
 * Before Linux 2.6, this was a scaled value based on the scheduler weighting given to this
 * process</li>
 * <li>nice  %ld The nice value (see setpriority(2)), a value in the range 19 (low priority) to
 * -20 (high priority).</li>
 * <li>num_threads  %ld Number of threads in this process (since Linux 2.6). Before kernel 2.6,
 * this field was hard coded to 0 as a placeholder for an earlier removed field.</li>
 * <li>itrealvalue  %ld The time in jiffies before the next SIGALRM is sent to the process due
 * to an interval timer.  Since kernel 2.6.17, this field is no longer maintained, and is hard
 * coded as 0.</li>
 * <li>starttime  %llu The time the process started after system boot.  In kernels before Linux
 * 2.6, this value was expressed in jiffies.  Since Linux 2.6, the value is expressed in clock
 * ticks (divide by sysconf(_SC_CLK_TCK)).</li>
 * <li>The format for this field was %lu before Linux 2.6.  (23) vsize  %lu Virtual memory size
 * in bytes.</li>
 * <li>rss  %ld Resident Set Size: number of pages the process has in real memory.  This is just
 * the pages which count toward text, data, or stack space.  This does not include pages which
 * have not been demand-loaded in, or which are swapped out.</li>
 * <li>rsslim  %lu Current soft limit in bytes on the rss of the process; see the description of
 * RLIMIT_RSS in getrlimit(2).</li>
 * <li>startcode  %lu The address above which program text can run.</li>
 * <li>endcode  %lu The address below which program text can run.</li>
 * <li>startstack  %lu The address of the start (i.e., bottom) of the stack.</li>
 * <li>kstkesp  %lu The current value of ESP (stack pointer), as found in the kernel stack page
 * for the process.</li>
 * <li>kstkeip  %lu The current EIP (instruction pointer).</li>
 * <li>signal  %lu The bitmap of pending signals, displayed as a decimal number.  Obsolete,
 * because it does not provide information on real-time signals; use /proc/[pid]/status
 * instead</li>
 * <li>blocked  %lu The bitmap of blocked signals, displayed as a decimal number.  Obsolete,
 * because it does not provide information on real-time signals; use /proc/[pid]/status
 * instead</li>
 * <li>sigignore  %lu The bitmap of ignored signals, displayed as a decimal number. Obsolete,
 * because it does not provide information on real-time signals; use /proc/[pid]/status
 * instead</li>
 * <li>sigcatch  %lu The bitmap of caught signals, displayed as a decimal number. Obsolete,
 * because it does not provide information on real-time signals; use /proc/[pid]/status
 * instead.</li>
 * <li>wchan  %lu This is the "channel" in which the process is waiting.  It is the address of a
 * location in the kernel where the process is sleeping.  The corresponding symbolic name can be
 * found in /proc/[pid]/wchan.</li>
 * <li>nswap  %lu Number of pages swapped (not maintained).</li>
 * <li>cnswap  %lu Cumulative nswap for child processes (not maintained).</li>
 * <li>exit_signal  %d  (since Linux 2.1.22) Signal to be sent to parent when we die.</li>
 * <li>processor  %d  (since Linux 2.2.8) CPU number last executed on.</li>
 * <li>rt_priority  %u  (since Linux 2.5.19) Real-time scheduling priority, a number in the
 * range 1 to 99 for processes scheduled under a real-time policy, or 0, for non-real-time
 * processes (see sched_setscheduler(2)).</li>
 * <li>policy  %u  (since Linux 2.5.19) Scheduling policy (see sched_setscheduler(2)). Decode
 * using the SCHED_* constants in linux/sched.h.  The format for this field was %lu before Linux
 * 2.6.22.</li>
 * <li>delayacct_blkio_ticks  %llu  (since Linux 2.6.18) Aggregated block I/O delays, measured
 * in clock ticks (centiseconds).</li>
 * <li>guest_time  %lu  (since Linux 2.6.24) Guest time of the process (time spent running a
 * virtual CPU for a guest operating system), measured in clock ticks (divide by
 * sysconf(_SC_CLK_TCK)).</li>
 * <li>cguest_time  %ld  (since Linux 2.6.24) Guest time of the process's children, measured in
 * clock ticks (divide by sysconf(_SC_CLK_TCK)).</li>
 * <li>start_data  %lu  (since Linux 3.3) Address above which program initialized and
 * uninitialized (BSS) data are placed.</li>
 * <li>end_data  %lu  (since Linux 3.3) Address below which program initialized and
 * uninitialized (BSS) data are placed.</li>
 * <li>start_brk  %lu  (since Linux 3.3) Address above which program heap can be expanded with
 * brk(2).</li>
 * <li>arg_start  %lu  (since Linux 3.5) Address above which program command-line arguments
 * (argv) are placed.</li>
 * <li>arg_end  %lu  (since Linux 3.5) Address below program command-line arguments (argv) are
 * placed.</li>
 * <li>env_start  %lu  (since Linux 3.5) Address above which program environment is placed.</li>
 * <li>env_end  %lu  (since Linux 3.5) Address below which program environment is placed.</li>
 * <li>exit_code  %d  (since Linux 3.5) The thread's exit status in the form reported by
 * waitpid(2).</li>
 * </ol>
 */
public final class Stat extends ProcFile {

  /**
   * Read /proc/[pid]/stat.
   *
   * @param pid
   *     the process id.
   * @return the {@link Stat}
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   */
  public static Stat get(int pid) throws IOException {
    return new Stat(String.format("/proc/%d/stat", pid));
  }

  private final String[] fields;

  private Stat(String path) throws IOException {
    super(path);
    fields = content.split("\\s+");
  }

  private Stat(Parcel in) {
    super(in);
    this.fields = in.createStringArray();
  }

  /** The process ID. */
  public int getPid() {
    return Integer.parseInt(fields[0]);
  }

  /**
   * The filename of the executable, in parentheses. This is visible whether or not the
   * executable is swapped out.
   */
  public String getComm() {
    return fields[1].replace("(", "").replace(")", "");
  }

  /**
   * <p>One of the following characters, indicating process state:</p>
   *
   * <ul>
   * <li>'R'  Running</li>
   * <li>'S'  Sleeping in an interruptible wait</li>
   * <li>'D'  Waiting in uninterruptible disk sleep</li>
   * <li>'Z'  Zombie</li>
   * <li>'T'  Stopped (on a signal) or (before Linux 2.6.33) trace stopped</li>
   * <li>'t'  Tracing stop (Linux 2.6.33 onward)</li>
   * <li>'W'  Paging (only before Linux 2.6.0)</li>
   * <li>'X'  Dead (from Linux 2.6.0 onward)</li>
   * <li>'x'  Dead (Linux 2.6.33 to 3.13 only)</li>
   * <li>'K'  Wakekill (Linux 2.6.33 to 3.13 only)</li>
   * <li>'W'  Waking (Linux 2.6.33 to 3.13 only)</li>
   * <li>'P'  Parked (Linux 3.9 to 3.13 only)</li>
   * </ul>
   */
  public char state() {
    return fields[2].charAt(0);
  }

  /**
   * The PID of the parent of this process.
   */
  public int ppid() {
    return Integer.parseInt(fields[3]);
  }

  /**
   * The process group ID of the process.
   */
  public int pgrp() {
    return Integer.parseInt(fields[4]);
  }

  /**
   * The session ID of the process.
   */
  public int session() {
    return Integer.parseInt(fields[5]);
  }

  /**
   * The controlling terminal of the process. (The minor device number is contained in the
   * combination of bits 31 to 20 and 7 to 0; the major device number is in bits 15 to 8.)
   */
  public int tty_nr() {
    return Integer.parseInt(fields[6]);
  }

  /**
   * The ID of the foreground process group of the controlling terminal of the process.
   */
  public int tpgid() {
    return Integer.parseInt(fields[7]);
  }

  /**
   * <p>The kernel flags word of the process. For bit meanings, see the PF_* defines in the Linux
   * kernel source file include/linux/sched.h. Details depend on the kernel version.</p>
   *
   * <p>The format for this field was %lu before Linux 2.6.</p>
   */
  public int flags() {
    return Integer.parseInt(fields[8]);
  }

  /**
   * The number of minor faults the process has made which have not required loading a memory
   * page from disk.
   */
  public long minflt() {
    return Long.parseLong(fields[9]);
  }

  /**
   * The number of minor faults that the process's waited-for children have made.
   */
  public long cminflt() {
    return Long.parseLong(fields[10]);
  }

  /**
   * The number of major faults the process has made which have required loading a memory page
   * from disk.
   */
  public long majflt() {
    return Long.parseLong(fields[11]);
  }

  /**
   * The number of major faults that the process's waited-for children have made.
   */
  public long cmajflt() {
    return Long.parseLong(fields[12]);
  }

  /**
   * Amount of time that this process has been scheduled in user mode, measured in clock ticks
   * (divide by sysconf(_SC_CLK_TCK)).  This includes guest time, guest_time (time spent running
   * a virtual CPU, see below), so that applications that are not aware of the guest time field
   * do not lose that time from their calculations.
   */
  public long utime() {
    return Long.parseLong(fields[13]);
  }

  /**
   * Amount of time that this process has been scheduled in kernel mode, measured in clock ticks
   * (divide by sysconf(_SC_CLK_TCK)).
   */
  public long stime() {
    return Long.parseLong(fields[14]);
  }

  /**
   * Amount of time that this process's waited-for children have been scheduled in user mode,
   * measured in clock ticks (divide by sysconf(_SC_CLK_TCK)). (See also times(2).)  This
   * includes guest time, cguest_time (time spent running a virtual CPU, see below).
   */
  public long cutime() {
    return Long.parseLong(fields[15]);
  }

  /**
   * Amount of time that this process's waited-for children have been scheduled in kernel mode,
   * measured in clock ticks (divide by sysconf(_SC_CLK_TCK)).
   */
  public long cstime() {
    return Long.parseLong(fields[16]);
  }

  /**
   * <p>(Explanation for Linux 2.6) For processes running a real-time scheduling policy (policy
   * below; see sched_setscheduler(2)), this is the negated scheduling priority, minus one; that
   * is,
   * a number in the range -2 to -100, corresponding to real-time priorities 1 to 99.  For
   * processes
   * running under a non-real-time scheduling policy, this is the raw nice value (setpriority(2))
   * as
   * represented in the kernel.  The kernel stores nice values as numbers in the range 0 (high) to
   * 39 (low), corresponding to the user-visible nice range of -20 to 19.</p>
   *
   * <p>Before Linux 2.6, this was a scaled value based on the scheduler weighting given to this
   * process.</p>
   */
  public long priority() {
    return Long.parseLong(fields[17]);
  }

  /**
   * The nice value (see setpriority(2)), a value in the range 19 (low priority) to -20 (high
   * priority).
   */
  public int nice() {
    return Integer.parseInt(fields[18]);
  }

  /**
   * Number of threads in this process (since Linux 2.6). Before kernel 2.6, this field was hard
   * coded to 0 as a placeholder for an earlier removed field.
   */
  public long num_threads() {
    return Long.parseLong(fields[19]);
  }

  /**
   * The time in jiffies before the next SIGALRM is sent to the process due to an interval timer.
   * Since kernel 2.6.17, this field is no longer maintained, and is hard coded as 0.
   */
  public long itrealvalue() {
    return Long.parseLong(fields[20]);
  }

  /**
   * <p>The time the process started after system boot. In kernels before Linux 2.6, this value was
   * expressed in jiffies.  Since Linux 2.6, the value is expressed in clock ticks (divide by
   * sysconf(_SC_CLK_TCK)).</p>
   *
   * <p>The format for this field was %lu before Linux 2.6.</p>
   */
  public long starttime() {
    return Long.parseLong(fields[21]);
  }

  /**
   * Virtual memory size in bytes.
   */
  public long vsize() {
    return Long.parseLong(fields[22]);
  }

  /**
   * Resident Set Size: number of pages the process has in real memory.  This is just the pages
   * which count toward text, data, or stack space.  This does not include pages which have not
   * been demand-loaded in, or which are swapped out.
   */
  public long rss() {
    return Long.parseLong(fields[23]);
  }

  /**
   * Current soft limit in bytes on the rss of the process; see the description of RLIMIT_RSS in
   * getrlimit(2).
   */
  public long rsslim() {
    return Long.parseLong(fields[24]);
  }

  /**
   * The address above which program text can run.
   */
  public long startcode() {
    return Long.parseLong(fields[25]);
  }

  /**
   * The address below which program text can run.
   */
  public long endcode() {
    return Long.parseLong(fields[26]);
  }

  /**
   * The address of the start (i.e., bottom) of the stack.
   */
  public long startstack() {
    return Long.parseLong(fields[27]);
  }

  /**
   * The current value of ESP (stack pointer), as found in the kernel stack page for the process.
   */
  public long kstkesp() {
    return Long.parseLong(fields[28]);
  }

  /**
   * The current EIP (instruction pointer).
   */
  public long kstkeip() {
    return Long.parseLong(fields[29]);
  }

  /**
   * The bitmap of pending signals, displayed as a decimal number.  Obsolete, because it does not
   * provide information on real-time signals; use /proc/[pid]/status instead.
   */
  public long signal() {
    return Long.parseLong(fields[30]);
  }

  /**
   * The bitmap of blocked signals, displayed as a decimal number.  Obsolete, because it does not
   * provide information on real-time signals; use /proc/[pid]/status instead.
   */
  public long blocked() {
    return Long.parseLong(fields[31]);
  }

  /**
   * The bitmap of ignored signals, displayed as a decimal number.  Obsolete, because it does not
   * provide information on real-time signals; use /proc/[pid]/status instead.
   */
  public long sigignore() {
    return Long.parseLong(fields[32]);
  }

  /**
   * The bitmap of caught signals, displayed as a decimal number.  Obsolete, because it does not
   * provide information on real-time signals; use /proc/[pid]/status instead.
   */
  public long sigcatch() {
    return Long.parseLong(fields[33]);
  }

  /**
   * This is the "channel" in which the process is waiting.  It is the address of a location in the
   * kernel where the process is sleeping. The corresponding symbolic name can be found in
   * /proc/[pid]/wchan.
   */
  public long wchan() {
    return Long.parseLong(fields[34]);
  }

  /**
   * Number of pages swapped (not maintained).
   */
  public long nswap() {
    return Long.parseLong(fields[35]);
  }

  /**
   * Cumulative nswap for child processes (not maintained).
   */
  public long cnswap() {
    return Long.parseLong(fields[36]);
  }

  /**
   * (since Linux 2.1.22)
   * Signal to be sent to parent when we die.
   */
  public int exit_signal() {
    return Integer.parseInt(fields[37]);
  }

  /**
   * (since Linux 2.2.8)
   * CPU number last executed on.
   */
  public int processor() {
    return Integer.parseInt(fields[38]);
  }

  /**
   * (since Linux 2.5.19)
   * Real-time scheduling priority, a number in the range 1 to 99 for processes scheduled under a
   * real-time policy, or 0, for non-real-time processes (see sched_setscheduler(2)).
   */
  public int rt_priority() {
    return Integer.parseInt(fields[39]);
  }

  /**
   * <p>(since Linux 2.5.19) Scheduling policy (see sched_setscheduler(2)). Decode using the
   * SCHED_*
   * constants in linux/sched.h.</p>
   *
   * <p>The format for this field was %lu before Linux 2.6.22.</p>
   */
  public int policy() {
    return Integer.parseInt(fields[40]);
  }

  /**
   * (since Linux 2.6.18)
   * Aggregated block I/O delays, measured in clock ticks (centiseconds).
   */
  public long delayacct_blkio_ticks() {
    return Long.parseLong(fields[41]);
  }

  /**
   * (since Linux 2.6.24)
   * Guest time of the process (time spent running a virtual CPU for a guest operating system),
   * measured in clock ticks (divide by sysconf(_SC_CLK_TCK)).
   */
  public long guest_time() {
    return Long.parseLong(fields[42]);
  }

  /**
   * (since Linux 2.6.24)
   * Guest time of the process's children, measured in clock ticks (divide by
   * sysconf(_SC_CLK_TCK)).
   */
  public long cguest_time() {
    return Long.parseLong(fields[43]);
  }

  /**
   * (since Linux 3.3)
   * Address above which program initialized and uninitialized (BSS) data are placed.
   */
  public long start_data() {
    return Long.parseLong(fields[44]);
  }

  /**
   * (since Linux 3.3)
   * Address below which program initialized and uninitialized (BSS) data are placed.
   */
  public long end_data() {
    return Long.parseLong(fields[45]);
  }

  /**
   * (since Linux 3.3)
   * Address above which program heap can be expanded with brk(2).
   */
  public long start_brk() {
    return Long.parseLong(fields[46]);
  }

  /**
   * (since Linux 3.5)
   * Address above which program command-line arguments (argv) are placed.
   */
  public long arg_start() {
    return Long.parseLong(fields[47]);
  }

  /**
   * (since Linux 3.5)
   * Address below program command-line arguments (argv) are placed.
   */
  public long arg_end() {
    return Long.parseLong(fields[48]);
  }

  /**
   * (since Linux 3.5)
   * Address above which program environment is placed.
   */
  public long env_start() {
    return Long.parseLong(fields[49]);
  }

  /**
   * (since Linux 3.5)
   * Address below which program environment is placed.
   */
  public long env_end() {
    return Long.parseLong(fields[50]);
  }

  /**
   * (since Linux 3.5)
   * The thread's exit status in the form reported by waitpid(2).
   */
  public int exit_code() {
    return Integer.parseInt(fields[51]);
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeStringArray(fields);
  }

  public static final Parcelable.Creator<Stat> CREATOR = new Parcelable.Creator<Stat>() {

    @Override public Stat createFromParcel(Parcel source) {
      return new Stat(source);
    }

    @Override public Stat[] newArray(int size) {
      return new Stat[size];
    }
  };

}
