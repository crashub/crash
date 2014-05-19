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

package org.crsh.lang.impl.groovy.ast;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Required;
import org.crsh.cli.Usage;
import org.crsh.cli.Man;
import org.crsh.cli.Option;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;
import org.crsh.groovy.GroovyCommand;
import org.crsh.text.ui.BorderStyle;
import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.Style;

import java.util.logging.Level;
import java.util.logging.Logger;

@GroovyASTTransformation(phase= CompilePhase.CONVERSION)
public class DefaultImportTransformer implements ASTTransformation {

  /** . */
  private static final Logger log = Logger.getLogger(DefaultImportTransformer.class.getName());

  /** . */
  private static final Class<?>[] defaultImports = {
    Required.class,
    Man.class,
    Usage.class,
    Argument.class,
    Option.class,
    Command.class,
    ScriptException.class,
    InvocationContext.class,
    GroovyCommand.class,
  };

  /** . */
  private static final Class<?>[] defaultStaticImports = {
    Color.class,
    Decoration.class,
    Style.class,
    BorderStyle.class
  };

  public void visit(ASTNode[] nodes, final SourceUnit source) {
    log.log(Level.FINE, "Transforming source to add default import package");
    for (Class<?> defaultImport : defaultImports) {
      log.log(Level.FINE, "Adding default import for class " + defaultImport.getName());
      if (source.getAST().getImport(defaultImport.getSimpleName()) == null) {
        source.getAST().addImport(defaultImport.getSimpleName(), ClassHelper.make(defaultImport));
      }
    }
    for (Class<?> defaultStaticImport : defaultStaticImports) {
      log.log(Level.FINE, "Adding default static import for class " + defaultStaticImport.getName());
      if (!source.getAST().getStaticStarImports().containsKey(defaultStaticImport.getSimpleName())) {
        source.getAST().addStaticStarImport(defaultStaticImport.getSimpleName(), ClassHelper.make(defaultStaticImport));
      }
    }
  }
}
