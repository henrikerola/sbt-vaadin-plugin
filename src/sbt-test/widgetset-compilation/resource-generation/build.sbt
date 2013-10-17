libraryDependencies ++= Seq(
  "com.vaadin" % "vaadin-client-compiler" % "7.1.6" % "provided",
  "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
)

vaadinWebSettings

javaOptions in compileWidgetsets := Seq("-Xss8M", "-Xmx512M", "-XX:MaxPermSize=512M")

// Not defining enableCompileWidgetsets in resourceGenerators here so widgetset
// should be compiled when package is called