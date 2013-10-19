package org.vaadin.sbt.tasks

import sbt._
import sbt.Keys._
import org.vaadin.sbt.util.ForkUtil._
import org.vaadin.sbt.util.ProjectUtil._
import org.vaadin.sbt.VaadinPlugin._

/**
 * @author Henri Kerola / Vaadin
 */
object SuperDevModeTask {

  val superDevModeTask: Def.Initialize[Task[Unit]] = (dependencyClasspath in Compile, unmanagedSourceDirectories in Compile,
    resourceDirectories in Compile, vaadinWidgetsets in vaadinSuperDevMode, vaadinOptions in vaadinSuperDevMode,
    javaOptions in vaadinSuperDevMode, target, state, streams) map {
      (fullCp, sources, resources, widgetsets, args, jvmArgs, target, state, s) =>

        implicit val log = s.log

        forkWidgetsetCmd(
          jvmArgs,
          getClassPath(state, fullCp),
          "com.google.gwt.dev.codeserver.CodeServer",
          args,
          widgetsets,
          resources)
    }
}
