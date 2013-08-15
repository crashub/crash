import org.crsh.golo.CRaSH

# Display current term contextual information using CRaSH API
function run = |arg| {
  return "width: " + width() + "\nheight: " + height() + "\nsession: " + session() + "\nattributes: " + attributes()
}
