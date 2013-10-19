vaadinAddOnSettings

vaadinWidgetsets := Seq("com.example.MyWidgetset")

val checkManifestWidgetsetsValue = taskKey[Unit]("checkManifestWidgetsetsValue")

checkManifestWidgetsetsValue := {
    val jar = new java.util.jar.JarFile((crossTarget / "widgetsets-defined_2.10-0.1-SNAPSHOT.jar").value)
    val attributes = jar.getManifest.getMainAttributes
    if (attributes.getValue("Vaadin-Widgetsets") != "com.example.MyWidgetset") error("Wrong value for 'Vaadin-Widgetsets'")
}