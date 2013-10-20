libraryDependencies ++= Seq(
  "com.vaadin" % "vaadin-themes" % System.getProperty("vaadin.version") % "provided",
  "com.vaadin" % "vaadin-theme-compiler" % System.getProperty("vaadin.version") % "provided",
  "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
)

vaadinWebSettings