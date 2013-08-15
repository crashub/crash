import org.crsh.golo.CRaSH
import java.lang.Thread

# List all JVM threads
function run = {

    # Iterate over all JVM threads
    foreach thread in getAllStackTraces():keySet() {

      # Send the object in the object pipe
      provide(thread)
    }
}
