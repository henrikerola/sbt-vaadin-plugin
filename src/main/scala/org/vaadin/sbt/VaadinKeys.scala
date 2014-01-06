package org.vaadin.sbt

import sbt._

/**
 * @author Henri Kerola / Vaadin
 */
private[sbt] trait VaadinKeys {

  val vaadinOptions = settingKey[Seq[String]]("List of command line arguments to be passed for a task.")

  val compileVaadinWidgetsets = taskKey[Seq[File]]("Compiles Vaadin widgetsets into JavaScript.")

  val vaadinWidgetsets = settingKey[Seq[String]]("List of widgetset names to be compiled with the widgetset compiler.")

  val enableCompileVaadinWidgetsets = settingKey[Boolean]("If false, the compileWidgetsets task does nothing.")

  val compileVaadinThemes = taskKey[Seq[File]]("Compiles Vaadin SCSS themes into CSS.")

  val vaadinThemes = settingKey[Seq[String]]("List of theme names to be compiled with the Vaadin SCSS compiler.")

  val vaadinThemesDir = settingKey[Seq[File]]("Directory containing Vaadin themes.")

  val vaadinDevMode = taskKey[Unit]("Run Development Mode to debug Vaadin client-side code and to avoid Java to JavaScript recompilation during development.")

  val vaadinSuperDevMode = taskKey[Unit]("Run Super Dev Mode to recompile client-side code in a browser. Also debugging in the browser using source maps is possible.")

  val packageVaadinDirectoryZip = taskKey[Option[File]]("Creates a zip file that can be uploaded to Vaadin Directory.")

  val vaadinAddonMappings = TaskKey[Seq[(File, String)]]("Defines the mappings from a addon jar file to a path used in 'packageVaadinDirectoryZip'. The paths are added to the 'Vaadin-Addon' entry in the zip's manifest.")

}
