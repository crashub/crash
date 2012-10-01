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

package org.crsh.groovy;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.crsh.cmdline.annotations.Argument;
import org.crsh.cmdline.annotations.Command;
import org.crsh.cmdline.annotations.Required;
import org.crsh.cmdline.annotations.Usage;
import org.crsh.cmdline.annotations.Man;
import org.crsh.cmdline.annotations.Option;
import org.crsh.command.CRaSHCommand;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;
import org.crsh.text.ui.Border;
import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GroovyASTTransformation(phase= CompilePhase.CONVERSION)
public class DefaultImportTransformer implements ASTTransformation {

  /** . */
  private static final Logger log = LoggerFactory.getLogger(DefaultImportTransformer.class);

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
    CRaSHCommand.class,
  };

  /** . */
  private static final Class<?>[] defaultStaticImports = {
    Color.class,
    Decoration.class,
    Style.class,
    Border.class
  };

  public void visit(ASTNode[] nodes, final SourceUnit source) {
    log.debug("Transforming source to add default import package");
    for (Class<?> defaultImport : defaultImports) {
      log.debug("Adding default import for class " + defaultImport.getName());
      if (source.getAST().getImport(defaultImport.getSimpleName()) == null) {
        source.getAST().addImport(defaultImport.getSimpleName(), ClassHelper.make(defaultImport));
      }
    }
    for (Class<?> defaultStaticImport : defaultStaticImports) {
      log.debug("Adding default static import for class " + defaultStaticImport.getName());
      if (!source.getAST().getStaticStarImports().containsKey(defaultStaticImport.getSimpleName())) {
        source.getAST().addStaticStarImport(defaultStaticImport.getSimpleName(), ClassHelper.make(defaultStaticImport));
      }
    }
  }
}
