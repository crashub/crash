/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.command.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ArgumentParser<T> {

  /** . */
  private final CommandInfo<T> command;

  /** . */
  private List<OptionInfo> list;

  /** . */
  private Pattern optionPattern;

  public ArgumentParser(CommandInfo<T> command) {

    StringBuilder re = new StringBuilder("^");
    ArrayList<OptionInfo> list = new ArrayList<OptionInfo>();
    int index = 0;
    for (OptionInfo option : command.getOptions()) {
      if (index > 0) {
        re.append('|');
      }
      re.append("(?:(\\-[");
      for (Character opt : option.getOpts()) {
        re.append(opt);
      }
      re.append("])");

      //
      for (int i = 0;i < option.getArity();i++) {
        re.append(" ").append("([A-Aa-z0-9]*)");
      }

      //
      re.append(')');
      list.add(option);
      index++;
    }

    //
    String regex = re.toString();
    System.out.println("regex = " + regex);

    //
    this.command = command;
    this.list = list;
    this.optionPattern = Pattern.compile(regex);
  }

  public void parse(String... args) {
    parse(Arrays.asList(args));
  }

  public void parse(List<String> args) {

    StringBuilder sb = new StringBuilder();
    for (String arg : args) {
      if (sb.length() > 0) {
        sb.append(' ');
      }
      sb.append(arg.trim());
    }

    String s = sb.toString();
    System.out.println("s = " + s);
    Matcher matcher = optionPattern.matcher(s);
    if (matcher.find()) {
      int index = 1;
      OptionInfo matched = null;
      for (OptionInfo option : command.getOptions()) {
        if (matcher.group(index) != null) {
          matched = option;
          break;
        } else {
          index += 1 + option.getArity();
        }
      }

      //
      if (matched == null) {
        throw new AssertionError("Should not happen");
      }

      //
      String m = "Matched " + matched.getOpts();
      for (int j = 0;j < matched.getArity();j++) {
        m += " " + matcher.group(++index);
      }

      //
      System.out.println(m);
    } else {
      System.out.println("not matched");
    }

    System.out.println("rest = " + s.substring(matcher.end()));


  }

}
