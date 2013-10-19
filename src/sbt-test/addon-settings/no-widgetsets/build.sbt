vaadinAddOnSettings

val checkJar = taskKey[Unit]("checkJar")

checkJar := {
    val jar = new java.util.jar.JarFile((crossTarget / "no-widgetsets_2.10-0.1-SNAPSHOT.jar").value)
    val attributes = jar.getManifest.getMainAttributes
    if (attributes.getValue("Vaadin-Package-Version") != "1") error("Wrong value for 'Vaadin-Package-Version'")
    if (attributes.getValue("Implementation-Title") != "no-widgetsets") error("Wrong value for 'Implementation-Title'")
    if (attributes.getValue("Specification-Version") != "0.1-SNAPSHOT") error("Wrong value for 'Specification-Version'")
    if (attributes.getValue("Vaadin-Widgetsets") != null) error("'Vaadin-Widgetsets' should not be defined")
    if (jar.getJarEntry("Test.scala") == null) error("Jar doesn't contain 'Test.scala' source file")
}