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
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.crsh.cli.Command;
import org.crsh.groovy.GroovyCommand;

import java.util.logging.Logger;

@GroovyASTTransformation(phase= CompilePhase.SEMANTIC_ANALYSIS)
public class CommandTransformer implements ASTTransformation {

  /** . */
  private static final ClassNode CRASH_COMMAND = ClassHelper.make(GroovyCommand.class);

  /** . */
  private static final Logger log = Logger.getLogger(CommandTransformer.class.getName());

  public void visit(ASTNode[] nodes, final SourceUnit source) {

    for (ClassNode classNode : source.getAST().getClasses()) {
      out:
      for (MethodNode method : classNode.getMethods()) {
        for (AnnotationNode ann : method.getAnnotations()) {
          if (ann.getClassNode().getName().equals(Command.class.getName())) {
            ClassNode superClass = classNode.getSuperClass();
            if (superClass == null || superClass.getName().equals("java.lang.Object")) {
              classNode.setSuperClass(CRASH_COMMAND);
            }
            break out;
          }
        }
      }
    }
  }
}
