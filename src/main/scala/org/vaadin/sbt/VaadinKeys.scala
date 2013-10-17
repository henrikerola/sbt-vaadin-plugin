package org.vaadin.sbt

import sbt._

/**
 * @author Henri Kerola / Vaadin
 */
private[sbt] trait VaadinKeys {

  val options = settingKey[Seq[String]]("List of command line arguments to be passed for a task.")

  val compileWidgetsets = taskKey[Seq[File]]("Compiles Vaadin widgetsets into JavaScript.")

  val widgetsets = settingKey[Seq[String]]("List of widgetset names to be compiled with the widgetset compiler.")

  val enableCompileWidgetsets = settingKey[Boolean]("If false, the compileWidgetsets task does nothing.")

  val compileThemes = taskKey[Seq[File]]("Compiles Vaadin SCSS themes into CSS.")

  val themes = settingKey[Seq[String]]("List of theme names to be compiled with the Vaadin SCSS compiler.")

  val themesDir = settingKey[Seq[File]]("Directory containing Vaadin themes.")

  val devMode = taskKey[Unit]("Run Development Mode to debug Vaadin client-side code and to avoid Java to JavaScript recompilation during development.")

  val superDevMode = taskKey[Unit]("Run Super Dev Mode to recompile client-side code in a browser. Also debugging in the browser using source maps is possible.")

  val packageDirectoryZip = taskKey[Option[File]]("Creates a zip file that can be uploaded to Vaadin Directory.")

}
