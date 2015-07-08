import sbt.Keys._

libraryDependencies ++= Seq(
  "com.vaadin" % "vaadin-themes" % System.getProperty("vaadin.version") % "provided",
  "com.vaadin" % "vaadin-sass-compiler" % "0.9.7" % "provided"
)

vaadinSettings

vaadinThemesDir <<= sourceDirectory(sd => Seq(sd / "main" / "custom-dir"))