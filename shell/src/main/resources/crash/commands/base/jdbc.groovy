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

package crash.commands.base

import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.util.Utils

import java.sql.Statement
import org.crsh.cli.Argument
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import javax.naming.InitialContext
import javax.naming.NoInitialContextException
import java.sql.DriverManager
import org.crsh.command.ScriptException
import org.crsh.cli.Option
import java.lang.reflect.InvocationTargetException
import org.crsh.command.InvocationContext
import java.sql.DatabaseMetaData
import org.crsh.text.ui.UIBuilder
import org.crsh.cli.spi.Completer
import org.crsh.cli.spi.Completion
import org.crsh.cli.descriptor.ParameterDescriptor
import org.crsh.util.JNDIHandler;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@Usage("JDBC connection")
class jdbc implements Completer{

  Completer c = new JNDIHandler.JNDICompleter("javax.sql.DataSource");
  
  @Usage("connect to database with a JDBC connection string")
  @Command
  public String connect(
          @Usage("The username") @Option(names=["u","username"]) String user,
          @Usage("The password") @Option(names=["p","password"]) String password,
          @Usage("The extra properties") @Option(names=["properties"]) Properties properties,
          @Usage("The connection string") @Argument String connectionString) {
    if (connection != null) {
      throw new ScriptException("Already connected");
    }
    if (connectionString == null) {
      return "Connection string is mandatory"
    }

    // Build connection properties
    Properties props = new Properties()
    if (user != null)
      props["user"] = user
    if (password != null)
      props["password"] = password
    if (properties != null) {
      properties.each {
        props[it.key] = it.value
      }
    }

    // We use this trick to work around the fact that the DriverManager#getConnection will not
    // use the thread context classloader because of the nasty DriverManager#getCallerClassLoader method
    try {
      try {
        def getConnection = DriverManager.class.getDeclaredMethod("getConnection", String.class, Properties.class, ClassLoader.class);
        getConnection.setAccessible(true);
        connection = getConnection.invoke(null, connectionString, props, Thread.currentThread().getContextClassLoader());
      }
      catch (NoSuchMethodException ignore) {
        // JDK8 does not have this method instead it has the same method but with Class as last argument
        // that we must invoke with null
        def getConnection = DriverManager.class.getDeclaredMethod("getConnection", String.class, Properties.class, Class.class);
        getConnection.setAccessible(true);
        connection = getConnection.invoke(null, connectionString, props, null);
      }
    }
    catch (InvocationTargetException ite) {
      throw ite.cause;
    }

    //
    return "Connected to data base : $connectionString\n"
  }

  @Usage("open a connection from JNDI bound datasource")
  @Command
  public String open(@Usage("The datasource JNDI name") @Argument(completer = jdbc.class) String globalName) {
    if (connection != null) {
      throw new ScriptException("Already connected");
    }

    if (globalName == null) {
      throw new ScriptException("Datasource is mandatory");
    }

    //
    def ctx = new InitialContext()
    try {
      def ds = ctx.lookup(globalName)
      if (ds == null) {
        return "Datasource $globalName not found in JNDI"
      }
      connection = ds.connection
      return "Connected to $globalName datasource\n"
    } catch (NoInitialContextException e) {
      throw new ScriptException("No initial context found", e)
    } finally {
      Utils.close(ctx);
    }
  }

  @Usage("execute a SQL statement")
  @Command
  public String execute(
    @Usage("The statement")
    @Argument(unquote = false)
    List<String> statement) {
    if (connection == null) {
      throw new ScriptException("You are not connected to database, please call jdbc open [JNDI DS]");
    } else {
      StringBuilder sb = new StringBuilder();
      statement.each { sb << " " << it };
      String sql = sb.toString().trim();
      if (sql.startsWith('"') && sql.endsWith('"') || sql.startsWith("'") && sql.endsWith("'"))
        sql = sql.substring(1, sql.length() - 1)
      Statement stmt = connection.createStatement();
      try {
        stmt.execute(sql)
        ResultSet resultSet = stmt.getResultSet();
        if (resultSet != null) {
          resultSet.close();
        }
        return "Statement executed successfully\n";
      }
      finally {
        Utils.close(stmt);
      }
    }
  }

  @Usage("select SQL statement")
  @Command
  public void select(
      InvocationContext<Map> context,
      @Usage("The statement")
      @Argument(unquote = false)
      List<String> statement) {
    if (connection == null) {
      throw new ScriptException("You are not connected to database, please call jdbc open [JNDI DS]");
    } else {
      StringBuilder sb = new StringBuilder("select ");
      statement.each { sb << " " << it };
      String sql = sb.toString().trim();
      if (sql.startsWith('"') && sql.endsWith('"') || sql.startsWith("'") && sql.endsWith("'"))
        sql = sql.substring(1, sql.length() - 1)
      Statement stmt = connection.createStatement();
      try {
        stmt.execute(sql)
        ResultSet resultSet = stmt.getResultSet();
        try {
          if (resultSet != null) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = resultSet.getMetaData().getColumnCount()
            while (resultSet.next()) {
              LinkedHashMap row = new LinkedHashMap();
              (1..columnCount).each{ row[metaData.getColumnName(it)] = resultSet.getString(it) }
              context.provide(row)
            }
            out << "Query executed successfully\n";
          }
        }
        catch (IOException e) {
          e.printStackTrace()
        }
        finally {
          Utils.close(resultSet)
        }
      }
      finally {
        Utils.close(stmt);
      }
    }
  }

  @Usage("show the database properties")
  @Command
  public void props(InvocationContext<Map> context) {
    if (connection == null) {
      throw new ScriptException("Not connected");
    }
    DatabaseMetaData md = connection.getMetaData();
    md.properties.each { key, value ->
      try {
        context.provide([NAME: key, VALUE: value] as LinkedHashMap)
      }
      catch (IOException e) {
        e.printStackTrace()
      };
    }
  }

  @Usage("describe the tables")
  @Command
  public Object tables() {
    if (connection == null) {
      throw new ScriptException("Not connected");
    }
    DatabaseMetaData md = connection.getMetaData();
    ResultSet rs = md.getTables(null, null, "%", null);
    def ui = new UIBuilder();
    try {
      ui.table(columns: [1,1,1]) {
        header(bold: true, fg: black, bg: white) {
          label("NAME");
          label("CAT");
          label("TYPE");
        }
        while (rs.next()) {
          row {
            label(rs.getString("TABLE_NAME"))
            label(rs.getString("TABLE_SCHEM"))
            label(rs.getString("TABLE_TYPE"))
          }
        }
      }
    }
    finally {
      Utils.close(rs)
    }
    return ui;
  }

  @Usage("describe the tables")
  @Command
  public Object table(@Argument @Usage("the table names") List<String> tableNames) {
    if (connection == null) {
      throw new ScriptException("Not connected");
    }
    DatabaseMetaData md = connection.getMetaData();
    def ui = new UIBuilder();
    ui.table(columns: [2,2,1,1], border: dashed) {
      header(bold: true, fg: black, bg: white) {
        label("COLUMN")
        label("TYPE")
        label("SIZE")
        label("NULLABLE")
      }
      tableNames.each {
        // Save it here because it seems to go away for some scoping reason in the header
        def res = "" + it;
        ResultSet rs = md.getColumns(null, null, it, null);
        header(fg: black, bg: white) {
          label(res)
        }
        try {
          while (rs.next()) {
            row {
              label(rs.getString("COLUMN_NAME"))
              label("${rs.getString('TYPE_NAME')} (${rs.getString('DATA_TYPE')})")
              label(rs.getString("COLUMN_SIZE"))
              label(rs.getString("IS_NULLABLE"))
            }
          }
        } finally {
          Utils.close(rs)
        }
      }
    }
    return ui;
  }

  @Usage("describe the database")
  @Command
  public void info(InvocationContext<Map> context) {
    if (connection == null) {
      throw new ScriptException("Not connected");
    }
    // columns : 1,2,2,1,1
    DatabaseMetaData md = connection.getMetaData();

    //
    try {
      context.provide([
          TYPE: "Product",
          NAME: "${md.databaseProductName}",
          VERSION: "${md.databaseProductVersion}",
          MAJOR: "${md.databaseMajorVersion}",
          MINOR: "${md.databaseMinorVersion}"
      ] as LinkedHashMap)
    }
    catch (IOException e1) {
      e1.printStackTrace()
    };

    //
    try {
      context.provide([
          TYPE: "Product",
          NAME: "${md.driverName}",
          VERSION: "${md.driverVersion}",
          MAJOR: "${md.driverMajorVersion}",
          MINOR: "${md.driverMinorVersion}"
      ] as LinkedHashMap)
    }
    catch (IOException e) {
      e.printStackTrace()
    };
  }

  @Usage("close the current connection")
  @Command
  public String close() {
    if (connection == null) {
      throw new ScriptException("Not connected");
    } else {
      connection.close();
      connection = null;
      return "Connection closed\n"
    }
  }

  Completion complete(ParameterDescriptor parameter, String prefix) {
    return c.complete(parameter, prefix);
  }

}