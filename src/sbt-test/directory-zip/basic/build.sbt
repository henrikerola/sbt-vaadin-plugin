vaadinAddOnSettings

val checkZip = taskKey[Unit]("checkZip")

checkZip := {
  val zip = new java.util.jar.JarFile((target / "basic_2.10-0.1-SNAPSHOT.zip").value)
  val attributes = zip.getManifest.getMainAttributes
  def checkAttribute(name: String, value: String): Unit = {
    if (attributes.getValue(name) != value) sys.error(s"Wrong value for '${name}'")
  }
  def fileExists(name: String): Unit = {
    if (zip.getEntry(name) == null) sys.error(s"File '${name}' doesn't exist in the generated zip")
  }
  checkAttribute("Manifest-Version", "1.0")
  checkAttribute("Implementation-Title", "basic")
  checkAttribute("Implementation-Version", "0.1-SNAPSHOT")
  checkAttribute("Vaadin-Package-Version", "1")
  checkAttribute("Vaadin-Addon", "basic_2.10-0.1-SNAPSHOT.jar")
  fileExists("basic_2.10-0.1-SNAPSHOT.jar")
  fileExists("basic_2.10-0.1-SNAPSHOT-sources.jar")
  fileExists("basic_2.10-0.1-SNAPSHOT-javadoc.jar")
}