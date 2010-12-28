import org.crsh.jcr.command.PathArg;

import org.crsh.cmdline.ParameterDescriptor;
import javax.jcr.Session;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Item;

public class commit extends org.crsh.jcr.command.JCRCommand {

  @Command(description="Change the current directory")
  public Object main(@PathArg String path) throws ScriptException {
    assertConnected();
    def node = findNodeByPath(path);
    setCurrentNode(node);
  }

  public List<String> complete(ParameterDescriptor<?> parameter, String prefix) throws Exception {
    if (parameter.getAnnotation() instanceof PathArg) {

      String path = (String)getProperty("currentPath");
      Session session = (Session)getProperty("session");

      //
      if (session != null) {

        Node relative = null;

        if (prefix.length() == 0 || prefix.charAt(0) != '/') {
          if (path != null) {
            Item item = session.getItem(path);
            if (item instanceof Node) {
              relative = (Node)item;
            }
          }
        } else {
          relative = session.getRootNode();
          prefix = prefix.substring(1);
        }

        // Now navigate using the prefix
        if (relative != null) {
          for (int index = prefix.indexOf('/');index != -1;index = prefix.indexOf('/')) {
            String name = prefix.substring(0, index);
            if (relative.hasNode(name)) {
              relative = relative.getNode(name);
              prefix = prefix.substring(index + 1);
            } else {
              return Collections.emptyList();
            }
          }
        }

        // Compute the next possible completions
        List<String> completions = new ArrayList<String>();
        for (NodeIterator i = relative.getNodes(prefix + '*');i.hasNext();) {
          Node child = i.nextNode();
          completions.add(child.getName().substring(prefix.length()));
        }

        //
        if (completions.size() == 1) {
          String abc = prefix + completions[0];
          Node n = relative.getNode(abc);
          int s = n.getPath().length() - completions[0].length();

          completions.clear();
          for (NodeIterator i = n.getNodes();i.hasNext();) {
            Node child = i.nextNode();
            completions.add(child.getPath().substring(s));
          }
        }

        //
        return completions;
      }

      //

    }

    //
    return Collections.emptyList();
  }

}

