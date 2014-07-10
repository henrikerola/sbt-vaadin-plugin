package org.vaadin.sbt.util

import sbt._
import java.io.File

/**
 * @author Henri Kerola / Vaadin
 */
private[sbt] object ForkUtil {

  def forkJava(runJVMOptions: Seq[String], arguments: Seq[String])(implicit log: Logger): Int = {
    log.debug("runJVMOptions for fork: " + runJVMOptions)
    log.debug("arguments for fork: " + arguments)

    Fork.java(ForkOptions(runJVMOptions = runJVMOptions), arguments)
  }

  def forkWidgetsetCmd(
    jvmArgs: Seq[String],
    classPath: Seq[String],
    cmd: String,
    args: Seq[String],
    definedWidgetsets: Seq[String],
    resources: Seq[File])(implicit log: Logger): Either[String, Seq[String]] = {

    val widgetsets = if (definedWidgetsets.isEmpty) {
      log.debug("No widgetsets defined. Trying to find those from project's resource directory.")
      val foundWidgetsets = WidgetsetUtil.findWidgetsets(resources)
      log.debug("Found %d widgetset(s): %s." format (foundWidgetsets.size, foundWidgetsets.mkString(", ")))
      foundWidgetsets
    } else {
      log.debug("Using defined widgetsets: %s." format (definedWidgetsets mkString ", "))
      definedWidgetsets
    }

    if (widgetsets.isEmpty) {
      log.warn("No widgetsets defined or found. Nothing to to.")
      Right(Nil)
    } else {
      val forkArgs = Seq("-classpath", classPath.mkString(File.pathSeparator), cmd) ++ args ++ widgetsets

      val exitValue = forkJava(jvmArgs, forkArgs)
      if (exitValue == 0)
        Right(widgetsets)
      else
        Left("Non-zero return code (%d) from %s " format (exitValue, cmd))
    }

  }
}
