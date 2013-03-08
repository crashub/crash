@Usage("Perform action on Guice singletons")
class guice extends CRaSHCommand {
  @Usage("display a Guice Singleton property")
  @Command
  Object print(@Usage("The full class name") @Required @Argument String type, @Usage("The property") @Option(names=["p", "property"]) String property) {
    def singleton = context.attributes.beans[type];
    if (singleton != null) {
    	if (property != null) {
    		return singleton[property];
    	} else {
    		return singleton;
    	} 
    }
    return "No such type : " + type;
  }
  
  @Usage("invoke a method on a Guice Singleton")
  @Command
  Object invoke(@Usage("The full class name") @Required @Argument String type, @Usage("Method name") @Required @Argument String name) {
    def singleton = context.attributes.beans[type];
    if (singleton != null) {
    	return singleton.invokeMethod(name, [] as Object[]);
    }
    return "No such type : " + type;
  }
}

