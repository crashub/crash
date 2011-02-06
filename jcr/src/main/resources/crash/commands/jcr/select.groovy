import javax.jcr.query.Query;
import org.crsh.shell.ui.UIBuilder;


import javax.jcr.Node;
import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Option;

public class select extends org.crsh.command.CRaSHCommand {

  @Usage("execute an JCR sql query")
  @Command
  @Man("""Executes a JCR query with the SQL dialect, by default results are limited to 5.\
All results matched by the query are produced by this command.""")
  public void main(
    InvocationContext<Void, Node> context,
    @Option(names=["o","offset"])
    @Usage("the result offset")
    Integer offset,
    @Option(names=["l","limit"])
    @Usage("the result limit")
    Integer limit,
    @Option(names=["a","all"])
    @Usage("ignore the limit argument")
    Boolean all,
    @Argument(unquote = false)
    @Usage("the query")
    List<String> query) throws ScriptException {
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
    def statement = "select";
    query.each { statement += " " + it };

    //
    def select = queryMgr.createQuery(statement, Query.SQL);

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