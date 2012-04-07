package crash.commands.base

import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.command.InvocationContext
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.spi.Value
import org.crsh.cmdline.spi.Completer
import org.crsh.cmdline.ParameterDescriptor
import org.crsh.cmdline.annotations.Option
import org.crsh.cmdline.spi.CompletionResult

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@Usage("vm system properties commands")
class system extends CRaSHCommand implements Completer {

  // Properties command

  @Usage("list the vm system properties")
  @Command
  public void propls(
    InvocationContext<Void, Map.Entry> context) {

    def formatString = "%1\$-35s %2\$-20s\r\n";
    Formatter formatter = new Formatter(context.writer);
    formatter.format(formatString, "NAME", "VALUE");

    System.getProperties().each {
      if (it != null) {
        formatter.format formatString, it.key, it.value
        context.produce it
      }
    }
  }

  @Usage("set a system property")
  @Command
  public void propset(
    InvocationContext<Void, Void> context,
    @PropertyName PropName name,
    @PropertyValue String value) {
    System.setProperty name.toString(), value
  }

  @Usage("get a system property")
  @Command
  public void propget(
    InvocationContext<Void, Void> context,
    @PropertyName PropName name) {
    context.writer.print System.getProperty(name.toString())
  }

  @Usage("remove a system property")
  @Command
  public void proprm(
    InvocationContext<Void, Void> context,
    @PropertyName PropName name) {
    System.clearProperty name.toString()
  }

  CompletionResult<Boolean> complete(ParameterDescriptor<?> parameter, String prefix)
  {
    def c = CompletionResult.create();
    if (parameter.getJavaValueType() == PropName.class) {
      System.getProperties().each() {
        if (it.key.startsWith(prefix)) {
          c.put(it.key.substring(prefix.length()), true);
        }
      }
    }
    return c;
  }

  // Memory commands

  @Usage("call garbage collector")
  @Command
  public void gc(
      InvocationContext<Void, Map.Entry> context) {
    System.gc()
  }

  @Usage("show free memory")
  @Command
  public void freemem(
      InvocationContext<Void, Map.Entry> context,
      @UnitOpt Unit unit,
      @DecimalOpt Integer decimal) {
    if (unit == null) {
      unit = Unit.B;
    }
    context.writer.println(unit.compute(Runtime.getRuntime().freeMemory(), decimal) + unit.human)
  }

  @Usage("show total memory")
  @Command
  public void totalmem(
      InvocationContext<Void, Map.Entry> context,
      @UnitOpt Unit unit,
      @DecimalOpt Integer decimal) {
    if (unit == null) {
      unit = Unit.B;
    }
    context.writer.println(unit.compute(Runtime.getRuntime().totalMemory(), decimal) + unit.human)
  }
}

@Retention(RetentionPolicy.RUNTIME)
@Usage("the property name")
@Man("The name of the property")
@Argument(name = "name")
@interface PropertyName { }
class PropName extends Value {
  PropName(String string) {
    super(string)
  }
}

@Retention(RetentionPolicy.RUNTIME)
@Usage("the property value")
@Man("The value of the property")
@Argument(name = "value")
@interface PropertyValue { }

enum Unit { B(1, "b"), K(1024, "Kb"), M(1024 * 1024, "Mb"), G(1024 * 1024 * 1024, "Gb")
  final long unit;
  final String human;

  Unit(long unit, String human) {
    this.unit = unit;
    this.human = human;
  }

  public String compute(long space, Integer decimal) {
    if (decimal == null) {
      decimal = 0
    }
    return new BigDecimal(space / unit).setScale(decimal, BigDecimal.ROUND_HALF_UP).toPlainString();
  }

  public String getHuman() {
    return this.human;
  }
}

@Retention(RetentionPolicy.RUNTIME)
@Usage("the unit of the memory space size")
@Man("The unit of the memory space size {(B)yte, (O)ctet, (M)egaOctet, (G)igaOctet}")
@Option(names=["u","unit"],completer=org.crsh.cmdline.EnumCompleter)
@interface UnitOpt { }

@Retention(RetentionPolicy.RUNTIME)
@Usage("number of decimal")
@Man("The number of decimal (default 0)")
@Option(names=["d","decimal"])
@interface DecimalOpt { }
