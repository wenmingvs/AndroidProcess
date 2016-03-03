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
 */
public final class Statm extends ProcFile {

  /**
   * Read /proc/[pid]/statm.
   *
   * @param pid
   *     the process id.
   * @return the {@link Statm}
   * @throws IOException
   *     if the file does not exist or we don't have read permissions.
   */
  public static Statm get(int pid) throws IOException {
    return new Statm(String.format("/proc/%d/statm", pid));
  }

  public final String[] fields;

  private Statm(String path) throws IOException {
    super(path);
    fields = content.split("\\s+");
  }

  private Statm(Parcel in) {
    super(in);
    this.fields = in.createStringArray();
  }

  /**
   * @return the total program size in bytes
   */
  public long getSize() {
    return Long.parseLong(fields[0]) * 1024;
  }

  /**
   * @return the resident set size in bytes
   */
  public long getResidentSetSize() {
    return Long.parseLong(fields[1]) * 1024;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeStringArray(this.fields);
  }

  public static final Parcelable.Creator<Statm> CREATOR = new Parcelable.Creator<Statm>() {

    @Override public Statm createFromParcel(Parcel source) {
      return new Statm(source);
    }

    @Override public Statm[] newArray(int size) {
      return new Statm[size];
    }
  };

}
