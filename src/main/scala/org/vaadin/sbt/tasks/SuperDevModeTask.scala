package org.vaadin.sbt.tasks

import sbt._
import sbt.Keys._
import org.vaadin.sbt.util.ForkUtil._
import org.vaadin.sbt.util.ProjectUtil._
import org.vaadin.sbt.VaadinPlugin.{ vaadinOptions, vaadinSuperDevMode, vaadinWidgetsets }

/**
 * @author Henri Kerola / Vaadin
 */
object SuperDevModeTask {

  val superDevModeTask: Def.Initialize[Task[Unit]] = (classDirectory in Compile, dependencyClasspath in Compile,
    unmanagedSourceDirectories in Compile, resourceDirectories in Compile, vaadinWidgetsets in vaadinSuperDevMode,
    vaadinOptions in vaadinSuperDevMode, javaOptions in vaadinSuperDevMode, target, state, streams) map {
      (classDir, fullCp, sources, resources, widgetsets, args, jvmArgs, target, state, s) =>

        implicit val log = s.log

        val result = forkWidgetsetCmd(
          jvmArgs,
          getClassPath(state, Seq(classDir) ++ fullCp.files),
          "com.google.gwt.dev.codeserver.CodeServer",
          args,
          widgetsets,
          resources)

        for (error <- result.left) {
          sys.error(error)
        }
    }
}
