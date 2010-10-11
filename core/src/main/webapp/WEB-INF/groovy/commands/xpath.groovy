import org.kohsuke.args4j.Argument;
import javax.jcr.query.Query;
import org.crsh.shell.ui.UIBuilder;
import org.kohsuke.args4j.Option;
import org.crsh.command.Description;

@Description("Executes a query with the XPATH dialect, by default results are limited to 5 ")
public class select extends org.crsh.command.ClassCommand {

  @Option(name="-o",aliases=["--offset"],usage="The result offset")
  def Integer offset = 0;

  @Option(name="-l",aliases=["--limit"],usage="The result limit")
  def Integer limit = 5;

  @Option(name="-a",aliases=["--all"],usage="Ignore the limit argument")
  def Boolean all = false;

  @Argument(required=true,index=0,usage="The xpath query to execute")
  def String xpath;

  {
     unquoteArguments = false;
  }

  public Object execute() throws ScriptException {
    assertConnected();

    //
    if (offset < 0) {
      return "No negative offset accepted";
    }

    //
    if (limit < 0) {
      return "No negative limit accepted";
    }

    //
    if (all) {
      limit = null;
    }

    //
    def queryMgr = session.workspace.queryManager;

    //
    def query = queryMgr.createQuery(xpath, Query.XPATH);

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
        if (limit != null && index >= limit)
          break;
        formatNode(builder, n, 0, 1);
        index++;
      }
    }

    //
    return builder;
  }
}