import org.kohsuke.args4j.Argument;
import javax.jcr.query.Query;
import org.crsh.shell.ui.UIBuilder;
import org.kohsuke.args4j.Option;
import org.crsh.command.Description;
import javax.jcr.Node;
import org.crsh.command.CommandContext;

@Description("""Executes a query with the SQL dialect, by default results are limited to 5.\
All results matched by the query are produced by this command.""")
public class select extends org.crsh.command.BaseCommand<Void, Node> {

  @Option(name="-o",aliases=["--offset"],usage="The result offset")
  def Integer offset = 0;

  @Option(name="-l",aliases=["--limit"],usage="The result limit")
  def Integer limit = 5;

  @Option(name="-a",aliases=["--all"],usage="Ignore the limit argument")
  def Boolean all = false;

  @Argument(usage = "the query")
  def List<String> arguments;

  {
     unquoteArguments = false;
  }

  public void execute(CommandContext<Void, Node> context) throws ScriptException {
    assertConnected();

    //
    if (offset < 0) {
      throw new ScriptException("No negative offset accepted");
    }

    //
    if (limit < 0) {
      throw new ScriptException("No negative limit accepted");
    }

    //
    if (all) {
      limit = null;
    }

    //
    def queryMgr = session.workspace.queryManager;

    //
    def statement = "select";
    arguments.each { statement += " " + it };

    //
    def query = queryMgr.createQuery(statement, Query.SQL);

    //
    def result = query.execute();

    // Column we will display
    def columnNames = [];

    //
    def nodes = result.nodes;
    def total = nodes.size;
    if (offset > 0) {
      nodes.skip(offset);
    }

    //
    def builder = new UIBuilder();

    //
    builder.node("The query matched " + total + " nodes") {
      def index = 0;
      while (nodes.hasNext()) {
        def n = nodes.next();

        //
        if (limit != null && index >= limit)
          break;

        //
        formatNode(builder, n, 0, 1);

        //
        context.produce(n);

        //
        index++;
      }
    }

    //
    context.writer.print(builder);
  }
}