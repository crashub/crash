package crash.commands.base

import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.command.InvocationContext
import org.crsh.command.CRaSHCommand
import java.sql.Statement
import org.crsh.cmdline.annotations.Argument
import java.sql.SQLException
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import javax.naming.InitialContext
import javax.naming.NoInitialContextException
import java.sql.DriverManager

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@Usage("JDBC connection")
class jdbc extends CRaSHCommand {
  
  @Usage("connect to database with JDBC connection string")
  @Command
  public void connect(
    InvocationContext<Void, Void> context,
    @Usage("The connection string") @Argument String connectionString) {

    //
    if (connection != null) {
      context.writer.println("Already connected")
      return;
    }

    //
    if (connectionString == null) {
      if (isInTestMode) {
        connectionString = "jdbc:derby:EmbeddedDB;create=true"
      } else {
        context.writer.println("Connection string is mandatory")
        return;
      }
    }

    //
    connection = DriverManager.getConnection(connectionString)
    context.writer.println("Connected to data base : $connectionString")
  }

  @Usage("open connection from datasource")
  @Command
  public void open(
    InvocationContext<Void, Void> context,
    @Usage("The datasource") @Argument String datasource) {

    //
    if (connection != null) {
      context.writer.println("Already connected")
      return
    }

    if (datasource == null) {
      context.writer.println("Datasource is mandatory")
      return
    }

    //
    try {
      def ic = new InitialContext()
      def ctx = ic.lookup("java:")
      def ds = ctx.lookup(datasource)
      if (ds == null) {
        context.writer.println("Datasource $datasource not found in JNDI")
        return
      }
      connection = ds.connection
      context.writer.println "Connected to $datasource datasource"
    } catch (NoInitialContextException e) {
      context.writer.println "No initial context found"
    }
  }

  @Usage("execute SQL query")
  @Command
  public void query(
    InvocationContext<Void, Void> context,
    @Usage("The query") @Argument(unquote = false) List<String> sqlQuery) {
    if (connection == null) {
      context.writer.println("You are not connected to database, please call jdbc open [JNDI DS]")
    } else {
      Statement statement = connection.createStatement();
      String sql = "";
      sqlQuery.each { sql += " " + it };
      try {
        statement.execute(sql)
        ResultSet resultSet = statement.getResultSet();
        if (resultSet == null) {
          context.writer.println("Query executed successfully")
        } else {

          // Construct format
          def formatString = "";
          Formatter formatter = new Formatter(context.writer);
          ResultSetMetaData metaData = resultSet.getMetaData();
          int columnCount = resultSet.getMetaData().getColumnCount()
          (1..columnCount).each{ formatString += "%$it\$-20s " }
          formatString += "\r\n"

          // print header
          String[] header = new String[metaData.getColumnCount()];
          (1..columnCount).each{ header[it-1] = metaData.getColumnName(it) }
          formatter.format(formatString, header);

          // print conent
          String[] content = new String[metaData.getColumnCount()];
          while (resultSet.next()) {
            (1..columnCount).each{ content[it-1] = resultSet.getString(it) }
            formatter.format(formatString, content);
          }
        }
      } catch (SQLException e) {
        context.writer.println(e.getMessage())
      }
    }
  }

  @Usage("close the current connection")
  @Command
  public void close(
    InvocationContext<Void, Void> context) {
    if (connection == null) {
      context.writer.println("Not connected")
    } else {
      connection.close();
      connection = null;
      context.writer.println("Connection closed")
    }
  }
}