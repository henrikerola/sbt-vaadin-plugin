package org.vaadin.sbt.tasks

import sbt._
import sbt.Keys._
import org.vaadin.sbt.util.ForkUtil._
import org.vaadin.sbt.util.ProjectUtil._
import org.vaadin.sbt.VaadinPlugin.{ vaadinDevMode, vaadinOptions, vaadinWidgetsets }

/**
 * @author Henri Kerola / Vaadin
 */
object DevModeTask {

  private def addIfNotInArgs(args: Seq[String], param: String, value: String) =
    if (!args.contains(param)) Seq(param, value) else Nil

  val devModeTask: Def.Initialize[Task[Unit]] = (classDirectory in Compile, dependencyClasspath in Compile,
    resourceDirectories in Compile, vaadinWidgetsets in vaadinDevMode, vaadinOptions in vaadinDevMode,
    javaOptions in vaadinDevMode, target in vaadinDevMode, state, streams) map {
      (classDir, fullCp, resources, widgetsets, args, jvmArgs, target, state, s) =>

        implicit val log = s.log

        val cmdArgs = Seq("-noserver") ++
          addIfNotInArgs(args, "-war", target absolutePath) ++
          addIfNotInArgs(args, "-startupUrl", "http://localhost:8080") ++ args

        val result = forkWidgetsetCmd(
          jvmArgs,
          getClassPath(state, Seq(classDir) ++ fullCp.files),
          "com.google.gwt.dev.DevMode",
          cmdArgs,
          widgetsets,
          resources)

        for (error <- result.left) {
          sys.error(error)
        }
    }
}
