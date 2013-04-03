/**
 * Print a message on the screen.
 *
 * @Author Damien Rieu
 */
public class echo {
    @Command
    @Usage("echo command (unix like)")
    public void main(
            @Usage('echo titi or echo $VAR')
            @Argument String echoStr) {
        def result = "";
        if (echoStr != null) {
            if (echoStr.startsWith('$')) {
                echoStr = echoStr.substring(1, echoStr.length())
                result = context.session[echoStr]
            } else {
                result = echoStr
            }
        }

        out.println(result)
    }
}