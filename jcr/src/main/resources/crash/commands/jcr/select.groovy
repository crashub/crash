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

  @Usage("execute a JCR sql query")
  @Command
  @Man("""Queries in SQL format are possible via the ##select## command. You can write a query with the same syntax defined
by the specification and add options to control the number of results returned. By default the number of nodes is limited
to 5 results:

[/]% select * from nt:base
The query matched 1114 nodes
+-/
| +-properties
| | +-jcr:primaryType: nt:unstructured
| | +-jcr:mixinTypes: [exo:owneable,exo:privilegeable]
| | +-exo:owner: '__system'
| | +-exo:permissions: [any read,*:/platform/administrators read,*:/platform/administrators add_node,*:/platform/administratorsset_property,*:/platform/administrators remove]
+-/workspace
| +-properties
| | +-jcr:primaryType: mop:workspace
| | +-jcr:uuid: 'a69f226ec0a80002007ca83e5845cdac'
...

Display 20 nodes from the offset 10:

[/]% select * from nt:base -o 10 -l 20
The query matched 1114 nodes
...

It is possible also to remove the limit of displayed nodes with the -a option (you should use this option with care) :

[/]% select * from nt:base -a
The query matched 1114 nodes
...

select is a <Void,Node> command producing all the matched nodes.""")
  public void main(
    InvocationContext<Void, Node> context,
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
    @Argument(unquote = false)
    @Usage("the query")
    @Man("The query, as is")
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