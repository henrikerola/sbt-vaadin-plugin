package org.vaadin.sbt.tasks

import sbt._
import sbt.Keys._
import org.vaadin.sbt.util.ForkUtil._
import org.vaadin.sbt.util.ProjectUtil._
import org.vaadin.sbt.VaadinPlugin.{ devMode, options, widgetsets }

/**
 * @author Henri Kerola / Vaadin
 */
object DevModeTask {

  private def addIfNotInArgs(args: Seq[String], param: String, value: String) =
    if (!args.contains(param)) Seq(param, value) else Nil

  val devModeTask: Def.Initialize[Task[Unit]] = (dependencyClasspath in Compile, resourceDirectories in Compile,
    widgetsets in devMode, options in devMode, javaOptions in devMode, target in devMode, state, streams) map {
      (fullCp, resources, widgetsets, args, jvmArgs, target, state, s) =>

        implicit val log = s.log

        val cmdArgs = Seq("-noserver") ++
          addIfNotInArgs(args, "-war", target absolutePath) ++
          addIfNotInArgs(args, "-startupUrl", "http://localhost:8080") ++ args

        forkWidgetsetCmd(
          jvmArgs,
          getClassPath(state, fullCp),
          "com.google.gwt.dev.DevMode",
          cmdArgs,
          widgetsets,
          resources)
    }
}
