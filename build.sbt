name := "sbt-vaadin-plugin"

description := "Vaadin Plugin for sbt"

version := "0.1-SNAPSHOT"

organization := "org.vaadin.sbt"

sbtPlugin := true

sbtVersion in Global := "0.13.0"

scalaVersion in Global := "2.10.2"

scalariformSettings

// sbt -Dsbt-vaadin-plugin.repository.path=../henrikerola.github.io/repository/releases publish
publishTo := Some(Resolver.file("GitHub", file(Option(System.getProperty("sbt-vaadin-plugin.repository.path")).getOrElse("../henrikerola.github.io/repository/snapshots"))))

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.4.2")

ScriptedPlugin.scriptedSettings

scriptedBufferLog := false
