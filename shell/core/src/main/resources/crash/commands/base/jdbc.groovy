package crash.commands.base

import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.command.CRaSHCommand
import java.sql.Statement
import org.crsh.cmdline.annotations.Argument
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import javax.naming.InitialContext
import javax.naming.NoInitialContextException
import java.sql.DriverManager
import org.crsh.command.ScriptException
import org.crsh.cmdline.annotations.Option
import java.lang.reflect.InvocationTargetException
import org.crsh.cmdline.spi.Value
import org.crsh.command.InvocationContext
import org.crsh.util.Safe
import java.sql.DatabaseMetaData
import org.crsh.text.ui.UIBuilder
import org.crsh.cmdline.completers.JNDICompleter;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@Usage("JDBC connection")
class jdbc extends CRaSHCommand {
  
  @Usage("connect to database with a JDBC connection string")
  @Command
  public String connect(
          @Usage("The username") @Option(names=["u","username"]) String user,
          @Usage("The password") @Option(names=["p","password"]) String password,
          @Usage("The extra properties") @Option(names=["properties"]) Value.Properties properties,
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
    def getConnection = DriverManager.class.getDeclaredMethod("getConnection", String.class, Properties.class, ClassLoader.class)
    if (!getConnection.accessible)
      getConnection.accessible = true

    //
    try {
      connection = getConnection.invoke(null, connectionString, props, Thread.currentThread().getContextClassLoader());
    }
    catch (InvocationTargetException ite) {
      throw ite.cause;
    }

    //
    return "Connected to data base : $connectionString\n"
  }

  @Usage("open a connection from JNDI bound datasource")
  @Command
  public String open(@Usage("The datasource JNDI name") @Argument(completer = JNDICompleter.class) String globalName) {
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
      Safe.close(ctx);
    }
  }

  @Usage("execute SQL statement")
  @Command
  public String execute(
    InvocationContext<Void, Map> context,
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
        try {
          if (resultSet == null) {
            if (context.piped) {
              return null;
            } else {
              return "Query executed successfully\n";
            }
          } else {
            if (context.piped) {
              ResultSetMetaData metaData = resultSet.getMetaData();
              int columnCount = resultSet.getMetaData().getColumnCount()
              while (resultSet.next()) {
                LinkedHashMap row = new LinkedHashMap();
                (1..columnCount).each{ row[metaData.getColumnName(it)] = resultSet.getObject(it) }
                context.produce(row)
              }
              return null;
            } else {
              StringBuilder res = new StringBuilder()

              // Construct format
              def formatString = "";
              Formatter formatter = new Formatter(res);
              ResultSetMetaData metaData = resultSet.getMetaData();
              int columnCount = resultSet.getMetaData().getColumnCount()
              (1..columnCount).each{ formatString += "%$it\$-20s " }
              formatString += "\r\n"

              // Print header
              String[] header = new String[metaData.getColumnCount()];
              (1..columnCount).each{ header[it-1] = metaData.getColumnName(it) }
              formatter.format(formatString, header);

              // Print conent
              String[] content = new String[metaData.getColumnCount()];
              while (resultSet.next()) {
                (1..columnCount).each{ content[it-1] = resultSet.getString(it) }
                formatter.format(formatString, content);
              }

              //
              return res;
            }
          }
        }
        finally {
          Safe.close(resultSet)
        }
      }
      finally {
        Safe.close(stmt);
      }
    }
  }

  @Usage("show the database properties")
  @Command
  public Object props() {
    if (connection == null) {
      throw new ScriptException("Not connected");
    }
    DatabaseMetaData md = connection.getMetaData();
    def ui = new UIBuilder();
    ui.table(weights: [1,4]) {
      header(bold: true, fg: black, bg: white) {
        label("NAME")
        label("VALUE")
      }
      md.properties.each { key, value ->
        row {
          label(foreground: red, key)
          label(value)
        }
      }
    }
    return ui;
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
      ui.table(weights: [1,1,1]) {
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
      Safe.close(rs)
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
    ui.table(weights: [2,2,1,1], border: dashed) {
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
          Safe.close(rs)
        }
      }
    }
    return ui;
  }

  @Usage("describe the database")
  @Command
  public Object info() {
    if (connection == null) {
      throw new ScriptException("Not connected");
    }
    DatabaseMetaData md = connection.getMetaData();
    def ui = new UIBuilder();
    ui.table(weights: [1,2,2,1,1]) {
      header(bold: true, fg: black, bg: white) {
        label("TYPE");
        label("NAME");
        label("VERSION");
        label("MAJOR");
        label("MINOR") }
      row {
        label("Product")
        label("${md.databaseProductName}")
        label("${md.databaseProductVersion}")
        label("${md.databaseMajorVersion}")
        label("${md.databaseMinorVersion}")
      }
      row {
        label("Driver")
        label("${md.driverName}")
        label("${md.driverVersion}")
        label("${md.driverMajorVersion}")
        label("${md.driverMinorVersion}")
      }
    }
    return ui
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
}