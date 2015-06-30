name := "sbt-vaadin-plugin"

description := "Vaadin Plugin for sbt"

version := "1.2-SNAPSHOT"

organization := "org.vaadin.sbt"

sbtPlugin := true

sbtVersion in Global := "0.13.8"

scalaVersion in Global := "2.10.5"

scalariformSettings

// sbt -Dsbt-vaadin-plugin.repository.path=../henrikerola.github.io/repository/releases publish
publishTo := Some(Resolver.file("GitHub", file(Option(System.getProperty("sbt-vaadin-plugin.repository.path")).getOrElse("../henrikerola.github.io/repository/snapshots"))))

vaadinSettings

packageOptions in (Compile, packageBin) <+= org.vaadin.sbt.tasks.AddOnJarManifestTask.addOnJarManifestTask

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "1.1.1")

ScriptedPlugin.scriptedSettings

scriptedBufferLog := false

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Dplugin.version=" + version.value, "-Dvaadin.version=7.5.0")
}
