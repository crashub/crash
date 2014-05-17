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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.crsh.lang.impl.groovy.command.GroovyScriptCommand;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * <p>A transformer that tags a CRaSH script class when it has an explicit return statement.
 * Indeed Groovy script last statement are returned by default and this value is sometimes
 * not desirable in CRaSH script commands unless they are explicitly returned by the script.</p>
 *
 * @author Julien Viet
 */
@GroovyASTTransformation(phase= CompilePhase.SEMANTIC_ANALYSIS)
public class ScriptLastStatementTransformer implements ASTTransformation {

  /** . */
  public static final String FIELD_NAME = "org_crsh_has_explicit_return";

  /** . */
  private static final ClassNode GROOVY_SCRIPT_COMMAND = ClassHelper.make(GroovyScriptCommand.class);

  @Override
  public void visit(ASTNode[] nodes, SourceUnit source) {
    for (ClassNode classNode : source.getAST().getClasses()) {
      if (classNode.isDerivedFrom(GROOVY_SCRIPT_COMMAND)) {
        MethodNode run = classNode.getMethod("run", new Parameter[0]);
        Statement code = run.getCode();
        if (code instanceof BlockStatement) {
          BlockStatement block = (BlockStatement)code;
          List<Statement> statements = block.getStatements();
          int size = statements.size();
          if (size > 0) {
            Statement last = statements.get(size - 1);
            if (last instanceof ReturnStatement) {
              classNode.addField(new FieldNode(
                  FIELD_NAME,
                  Modifier.PUBLIC | Modifier.STATIC,
                  ClassHelper.Boolean_TYPE,
                  classNode,
                  null));
            }
          }
        }
      }
    }
  }
}
