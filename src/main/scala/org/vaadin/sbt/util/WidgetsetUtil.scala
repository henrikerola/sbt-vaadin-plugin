package org.vaadin.sbt.util

import sbt._
import java.io.File
import Path.relativeTo

/**
 * @author Henri Kerola / Vaadin
 */
private[sbt] object WidgetsetUtil {

  private val ModulePrefix = ".gwt.xml"

  def findWidgetsets(userDefined: Seq[String], folders: Seq[File]): Seq[String] =
    if (userDefined.isEmpty)
      findWidgetsets(folders)
    else
      userDefined

  def findWidgetsets(folders: Seq[File]): Seq[String] = {
    val foundModuleFiles = (folders ** ("*" + ModulePrefix)).get
    val relativePathsMap = foundModuleFiles x relativeTo(folders)
    relativePathsMap map { _._2.replace(File.separator, ".").dropRight(ModulePrefix length) }
  }

}
