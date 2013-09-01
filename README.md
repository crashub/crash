<pre><code>   ______
 .~      ~. |`````````,       .'.                   ..'''' |         |
|           |'''|'''''      .''```.              .''       |_________|
|           |    `.       .'       `.         ..'          |         |
 `.______.' |      `.   .'           `. ....''             |         |</code></pre>

The Common Reusable SHell (CRaSH) is a shell designed for extending Java programs and the Java Virtual Machine.

- Website : http://www.crashub.org
- Downloads: http://code.google.com/p/crsh/downloads/list
- JIRA: http://jira.exoplatform.org/browse/CRASH
- Documentation: http://www.crashub.org
- Continuous Integration: https://vietj.ci.cloudbees.com/job/CRaSH/

# How to build CRaSH

## Obtaining CRaSH source code

CRaSH can be obtained by cloning the Git repository `git@github.com:crashub/crash.git`

<pre><code>git clone git@github.com:crashub/crash.git</code></pre>

## Building CRaSH

CRaSH is built with Maven.

<pre><code>mvn package</code></pre>

The build produces several archives ready to use:

- `crsh.shell-${version}-standalone.jar` : a minimalistic standalone jar (to run with `java -jar crsh.shell-${version}-standalone.jar`)
- `packaging/target/crsh-${version}-spring.war` : the Spring war
- `packaging/target/crsh-${version}.war` : the web app war
- `plugins/jcr/exo/target/crsh.plugins.jcr.exo-${version}.war` : the exo JCR war
- `plugins/jcr/jackrabbit/target/crsh.plugins.jcr.jackrabbit-${version}.war` : the Jackrabbit war

It also produce the distribution:

- `distrib/target/crash-${version}-docs.tar.gz` : the documentation
- `distrib/target/crash-${version}.tar.gz` : the standalone distribution
- `distrib/target/crash-${version}-war.tar.gz` : the web app distribution
- `distrib/target/crash-${version}-spring.tar.gz` : the Spring distribution
- `distrib/target/crash-${version}-mule-app.tar.gz` : the Mule distribution
- `distrib/target/crash-${version}-gatein.tar.gz` : the GateIn distribution
