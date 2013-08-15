import org.crsh.golo.CRaSH
import org.crsh.text.Color

# Display hello world with different shell colors
function run = |arg| {

  # Get crash magic writer
  let writer = context():getWriter()

  # Print hello world with a color
  foreach color in Color.values() {
    writer:println("Hello World", color)
  }
}
