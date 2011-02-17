package crash.commands.base

import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.command.InvocationContext
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Retention
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Option

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@Usage("memory management commands")
class memory extends CRaSHCommand {
  @Usage("call garbage collector")
  @Command
  public void gc(
    InvocationContext<Void, Map.Entry> context) {
    System.gc()
  }

  @Usage("show free memory")
  @Command
  public void free(
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
  public void total(
    InvocationContext<Void, Map.Entry> context,
    @UnitOpt Unit unit,
    @DecimalOpt Integer decimal) {
    if (unit == null) {
      unit = Unit.B;
    }
    context.writer.println(unit.compute(Runtime.getRuntime().totalMemory(), decimal) + unit.human)
  }
  
}

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
