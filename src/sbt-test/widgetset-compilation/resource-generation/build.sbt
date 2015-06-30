libraryDependencies ++= Seq(
  "com.vaadin" % "vaadin-client-compiler" % System.getProperty("vaadin.version") % "provided"
)

vaadinWebSettings

javaOptions in compileVaadinWidgetsets := Seq("-Xss8M", "-Xmx512M", "-XX:MaxPermSize=512M")

// Not defining enableCompileWidgetsets in resourceGenerators here so widgetset
// should be compiled when package is called