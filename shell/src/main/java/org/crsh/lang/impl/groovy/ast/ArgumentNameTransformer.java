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
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.crsh.cli.Argument;

import java.util.List;

@GroovyASTTransformation(phase= CompilePhase.INSTRUCTION_SELECTION)
public class ArgumentNameTransformer implements ASTTransformation {

  public void visit(ASTNode[] nodes, final SourceUnit source) {
    for (ClassNode classNode : (List<ClassNode>)source.getAST().getClasses()) {

      //
      for (FieldNode field : classNode.getFields()) {
        String name = field.getName();
        handle(name, field);
      }

      //
      for (MethodNode method : classNode.getMethods()) {
        for (Parameter parameter : method.getParameters()) {
          String name = parameter.getName();
          handle(name, parameter);
        }
      }
    }
  }

  private void handle(String name, AnnotatedNode annotated) {
    for (AnnotationNode ann : (List<AnnotationNode>)annotated.getAnnotations()) {
      if (ann.getClassNode().getName().endsWith(Argument.class.getName())) {
        Expression expr = ann.getMember("name");
        if (expr == null) {
          expr = new ConstantExpression(name);
          ann.setMember("name", expr);
        }
      }
    }
  }
}
