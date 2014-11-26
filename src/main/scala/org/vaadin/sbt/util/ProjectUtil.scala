package org.vaadin.sbt.util

import sbt._
import java.io.File
import scala.annotation.tailrec

/**
 * @author Henri Kerola / Vaadin
 */
private[sbt] object ProjectUtil {

  def getSourceDirectoriesRecursively(state: State) = {

    val extracted = Project.extract(state)
    val structure = extracted.structure
    val currentRef = extracted.currentRef

    def iter(projectRefs: Seq[ProjectRef]): Seq[File] = {
      projectRefs match {
        case Nil => Seq.empty
        case projectRef :: tail => {
          Project.getProject(projectRef, structure).map { p =>
            val srcDirs: Option[Seq[File]] = Keys.sourceDirectories in Compile in projectRef get structure.data
            val resourceDirs: Option[Seq[File]] = Keys.resourceDirectories in Compile in projectRef get structure.data
            resourceDirs.get ++ srcDirs.get ++ iter(p.dependencies.map(_.project)) ++ iter(tail)
          } getOrElse Seq.empty
        }
      }
    }

    iter(Seq(currentRef))
  }

  def getClassPath(state: State, fullCp: Seq[java.io.File]): Seq[String] = {
    val cp = getSourceDirectoriesRecursively(state).map(_.absolutePath) ++ fullCp.map(_.absolutePath)
    // To make sure that GWT finds correct classes from classpath, sort entries containing "vaadin-client"
    // first to the list and then the rest.
    val (vaadinClient, rest) = cp partition { _ contains "vaadin-client" }
    (vaadinClient ++ rest)
  }

}
