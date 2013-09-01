import javax.jcr.query.Query;
import org.crsh.text.ui.UIBuilder;
import org.crsh.cli.Usage
import org.crsh.cli.Man
import org.crsh.cli.Command
import org.crsh.cli.Option
import org.crsh.cli.Argument;

public class xpath {

  @Usage("execute a JCR xpath query")
  @Command
  @Man("""Executes a JCR query with the xpath dialect, by default results are limited to 5.\
All results matched by the query are produced by this command.""")
  public Object main(
    @Option(names=["o","offset"])
    @Usage("the result offset")
    @Man("The offset of the first node to display")
    Integer offset,
    @Option(names=["l","limit"])
    @Usage("the result limit")
    @Man("The number of nodes displayed, by default this value is equals to 5")
    Integer limit,
    @Option(names=["a","all"])
    @Usage("ignore the limit argument")
    @Man("Display all the results by ignoring the limit argument, this should be used with care for large result set")
    Boolean all,
    @Argument
    @Usage("the query")
    @Man("The query")
    String query) {
    assertConnected();

    //
    offset = offset ?: 0;
    limit = limit ?: 5;
    all = all ?: false;

    //
    if (offset < 0) {
      throw new ScriptException("No negative offset accepted; $offset");
    }

    //
    if (limit < 0) {
      throw new ScriptException("No negative limit accepted: -limit");
    }

    //
    if (all) {
      limit = null;
    }

    //
    def queryMgr = session.workspace.queryManager;

    //
    def select = queryMgr.createQuery(query, Query.XPATH);

    //
    def result = select.execute();

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