/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.crsh.ssh.term.scp;

import org.crsh.cli.Argument;
import org.crsh.cli.Option;
import org.crsh.cli.Required;

public class SCPAction implements Runnable {

  /** . */
  @Option(names="r")
  private Boolean recursive;

  /** . */
  @Option(names="v")
  private Boolean verbose;

  /** . */
  @Option(names="p")
  private Boolean preserve;

  /** . */
  @Option(names="f")
  private Boolean source;

  /** . */
  @Option(names="t")
  private Boolean sink;

  /** . */
  @Option(names="d")
  private Boolean directory;

  /** . */
  @Argument
  @Required
  private String target;

  public Boolean isRecursive() {
    return recursive;
  }

  public void setRecursive(Boolean recursive) {
    this.recursive = recursive;
  }

  public Boolean isVerbose() {
    return verbose;
  }

  public void setVerbose(Boolean verbose) {
    this.verbose = verbose;
  }

  public Boolean isPreserve() {
    return preserve;
  }

  public void setPreserve(Boolean preserve) {
    this.preserve = preserve;
  }

  public Boolean isSource() {
    return source;
  }

  public void setSource(Boolean source) {
    this.source = source;
  }

  public Boolean isSink() {
    return sink;
  }

  public void setSink(Boolean sink) {
    this.sink = sink;
  }

  public Boolean isDirectory() {
    return directory;
  }

  public void setDirectory(Boolean directory) {
    this.directory = directory;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public void run() {
    // Nothing to do as it is handled by SCPCommandPlugin
  }

  @Override
  public String toString() {
    return "SCPAction[recursive=" + recursive + ",verbose=" + verbose + ",preserve=" + preserve + ",source=" + source +
      ",sink=" + sink + ",directory=" + directory + ",target=" + target + "]";
  }
}
