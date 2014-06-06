// This script uploads the gvm zip in bintray account
// it follows the gvm versionning scheme
// version with empty qualifiers or "crXYZ" will be uploaded
// SNAPSHOT version will be truncated to remove the SNAPSHOT as bintray does not accept them
// SNAPSHOT should be used for testing purpose

import groovyx.net.http.HTTPBuilder
import org.apache.commons.compress.archivers.ArchiveOutputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger

import static groovyx.net.http.ContentType.BINARY
import static groovyx.net.http.Method.*

// Configure log4j
def console = new ConsoleAppender()
console.setLayout(new PatternLayout("%d [%p|%c|%C{1}] %m%n"));
console.setThreshold(Level.ALL);
console.activateOptions();
Logger.getRootLogger().addAppender(console);
Logger.getRootLogger().setLevel(Level.INFO);
Logger.getLogger(HTTPBuilder.class).setLevel(Level.ALL)

// Version check / change and so on
def pattern = ~/([0-9\.]+)(\-.+)?/
def matcher = pattern.matcher(project.version);
if (!matcher.matches()) {
  throw new Exception("Invalid version ${project.version} !")
}
def mmm = matcher.group(1);
def qualifier = matcher.group(2)
def version
if (qualifier != null) {
  def qualifierPattern = ~/\-cr([0-9]+)(?:-SNAPSHOT)?/
  def qualifierMatcher = qualifierPattern.matcher(qualifier);
  if (!qualifierMatcher.matches()) {
    // Only candidate releases
    return;
  }
  version = "${mmm}.RC${qualifierMatcher.group(1)}"
} else {
  version = mmm;
}

// Check we have the file to upload first
def file = new File(project.build.directory, "crash-${project.version}-gvm.zip")
if (!file.exists()) {
  throw new Exception("${file.absolutePath} does not exists");
}
if (!file.isFile()) {
  throw new Exception("${file.absolutePath} is not a file");
}

// Transform file to prefix
ByteArrayOutputStream baos = new ByteArrayOutputStream((int)file.length())
ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(file)));
ArchiveOutputStream zipOut = new ArchiveStreamFactory().createArchiveOutputStream("zip", baos);
byte[] buffer = new byte[512];
while (true) {
  ZipArchiveEntry entry = zipIn.getNextZipEntry();
  if (entry == null) {
    break;
  } else {
    def length = (int)entry.size
    entry.name = "crash-${version}/${entry.name}";
    if (entry.name.endsWith(".sh") || entry.name.endsWith(".bat")) {
      entry.setUnixMode(0100755);
    }
    zipOut.putArchiveEntry(entry);
    while (length > 0) {
      def amount = zipIn.read(buffer, 0, Math.min(length, buffer.length));
      zipOut.write(buffer, 0, amount);
      length -= amount;
    }
    zipOut.closeArchiveEntry();
  }
}
zipOut.close();

// Credentials
def bintrayUser = project.properties.bintrayUser;
def bintrayApiKey = project.properties.bintrayApiKey;
if (bintrayUser == null) {
  throw new Exception("Property bintrayUser not found");
}
if (bintrayApiKey == null) {
  throw new Exception("Property bintrayApiKey not found");
}

//
def repoPath = "crashub/crash"
def packageName = "gvm"
def packagePath = "$repoPath/gvm"
def uploadUri = "/content/$packagePath/${version}/crash-${version}.zip";

//
def http = new HTTPBuilder("https://api.bintray.com");
http.headers.Authorization = "Basic ${"$bintrayUser:$bintrayApiKey".toString().bytes.encodeBase64()}"

println("Uploading ${file.absolutePath} to ${uploadUri}");
def result = http.request(PUT) {
  uri.path = uploadUri
  requestContentType = BINARY
  body = new ByteArrayInputStream(baos.toByteArray());
  response.success = { resp ->
    return "Package ${packageName} uploaded.";
  }
  response.failure = { resp ->
    def msg = "Cannot upload: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}";
    def entity = resp.entity;
    if (entity != null) {
      msg += "\n" + org.apache.http.util.EntityUtils.toString(entity);
    }
    return new Exception(msg);
  }
}

if (result instanceof Exception) {
  throw result;
} else if (result != null) {
  println(result)
}
