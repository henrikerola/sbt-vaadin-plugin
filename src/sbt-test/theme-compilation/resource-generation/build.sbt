libraryDependencies ++= Seq(
  "com.vaadin" % "vaadin-themes" % System.getProperty("vaadin.version") % "provided",
  "com.vaadin" % "vaadin-sass-compiler" % "0.9.7" % "provided",
  "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
)

vaadinWebSettings