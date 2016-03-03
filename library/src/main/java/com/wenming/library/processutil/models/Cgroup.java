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
import java.util.ArrayList;

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
 * @see ControlGroup
 */
public final class Cgroup extends ProcFile {

  /**
   * Read /proc/[pid]/cgroup.
   *
   * @param pid
   *     the processes id.
   * @return the {@link Cgroup}
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   */
  public static Cgroup get(int pid) throws IOException {
    return new Cgroup(String.format("/proc/%d/cgroup", pid));
  }

  /** the process' control groups */
  public final ArrayList<ControlGroup> groups;

  private Cgroup(String path) throws IOException {
    super(path);
    String[] lines = content.split("\n");
    groups = new ArrayList<>();
    for (String line : lines) {
      try {
        groups.add(new ControlGroup(line));
      } catch (Exception ignored) {
      }
    }
  }

  private Cgroup(Parcel in) {
    super(in);
    this.groups = in.createTypedArrayList(ControlGroup.CREATOR);
  }

  public ControlGroup getGroup(String subsystem) {
    for (ControlGroup group : groups) {
      String[] systems = group.subsystems.split(",");
      for (String name : systems) {
        if (name.equals(subsystem)) {
          return group;
        }
      }
    }
    return null;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeTypedList(groups);
  }

  public static final Creator<Cgroup> CREATOR = new Creator<Cgroup>() {

    @Override public Cgroup createFromParcel(Parcel source) {
      return new Cgroup(source);
    }

    @Override public Cgroup[] newArray(int size) {
      return new Cgroup[size];
    }
  };

}
