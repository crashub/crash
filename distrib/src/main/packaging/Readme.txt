The Common Reusable SHell is the shell for the Java Platform, this is the standalone distribution:

# Directory layout

/bin  - scripts for starting crash
/cmd  - contains the shell commands
/conf - the runtime configuration
/lib  - the virtual machine classpath

# Start options

usage: crash [--non-interactive] [-c | --cmd] [--conf] [-p | --property] [--cmd-folder] [--conf-folder] [-h | --help] <pid>...

   [--non-interactive] non interactive mode, the JVM io will not be used
   [-c | --cmd]        the command mounts
   [--conf]            the conf mounts
   [-p | --property]   set a property of the form a=b
   [--cmd-folder]      a folder in which commands should be extracted
   [--conf-folder]     a folder in which configuration should be extracted
   [-h | --help]       this help
   <pid>...            the optional list of JVM process id to attach to

# Documentation

* download http://www.crashub.org/
* online http://www.crashub.org/1.3/reference.html#_standalone