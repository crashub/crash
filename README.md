
<pre><code>   ______
 .~      ~. |`````````,       .'.                   ..'''' |         |
|           |'''|'''''      .''```.              .''       |_________|
|           |    `.       .'       `.         ..'          |         |
 `.______.' |      `.   .'           `. ....''             |         |</code></pre>

The Common Reusable SHell (CRaSH) is a shell designed for extending Java programs and the Java Virtual Machine.

- Website : http://vietj.github.com/crash
- Downloads: http://code.google.com/p/crsh/downloads/list
- JIRA: http://jira.exoplatform.org/browse/CRASH
- Documentation: http://vietj.github.com/crash/
- Continuous Integration: https://vietj.ci.cloudbees.com/job/CRaSH/

# How to build CRaSH

## Obtaining CRaSH source code

CRaSH can be obtained by cloning the Git repository `git@github.com:vietj/crash.git`

<pre><code>git clone git@github.com:vietj/crash.git</code></pre>

## Building CRaSH

CRaSH is built with Maven.

<pre><code>mvn install</code></pre>

it will produce several files:

- `shell/core/target/crsh-core-${version}-standalone.jar` : the standalone jar
- `shell/packaging/target/crsh-${version}.tar.gz` : the standalone tar gz
- `shell/packaging/target/crsh-${version}.war` : the web archive package
- `exo/jackrabbit/target/crsh.jcr.jackrabbit-${version}.war` : the exo jcr web archive
- `jcr/jackrabbit/target/crsh.jcr.jackrabbit-${version}.war` : the jackrabbit jcr web archive

The maven release is build with the release profile.

<pre><code>mvn install -Prelease</code></pre>

It will produce:

- `distrib/target/crsh-${version}.tar.gz` : the full distribution
