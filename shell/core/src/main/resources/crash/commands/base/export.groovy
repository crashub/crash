/**
 * Export command.
 * e.g : export PATH=value
 * then echo $PATH
 *
 * @author Damien Rieu.
 */
@Usage("Export command.")
public class export {

    @Command
    @Usage("Export command (unix like)")
    public void main(
            @Usage("export NAME=VALUE")
            @Argument String export) {

        if ((export != null) && (export.contains('='))) {

            def varName = export.substring(0, export.indexOf('='));
            def varValue = export.substring(export.indexOf('=') + 1, export.length() );
            context.session[varName] = varValue;
            out.println('You define the variable $' + varName);

        } else {
            out.println("Syntax error : export NAME=VALUE ");
        }

    }
}