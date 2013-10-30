package org.vaadin.sbt.tasks

import sbt._
import sbt.Keys._
import org.vaadin.sbt.util.ForkUtil._
import org.vaadin.sbt.util.ProjectUtil._
import org.vaadin.sbt.VaadinPlugin.{ compileVaadinWidgetsets, enableCompileVaadinWidgetsets, vaadinOptions, vaadinWidgetsets }

/**
 * @author Henri Kerola / Vaadin
 */
object CompileWidgetsetsTask {

  val compileWidgetsetsTask: Def.Initialize[Task[Seq[File]]] = (dependencyClasspath in Compile,
    resourceDirectories in Compile, vaadinWidgetsets in compileVaadinWidgetsets, vaadinOptions in compileVaadinWidgetsets,
    javaOptions in compileVaadinWidgetsets, target in compileVaadinWidgetsets, thisProject, enableCompileVaadinWidgetsets,
    state, streams) map widgetsetCompiler

  val compileWidgetsetsInResourceGeneratorsTask: Def.Initialize[Task[Seq[File]]] = (dependencyClasspath in Compile,
    resourceDirectories in Compile, vaadinWidgetsets in compileVaadinWidgetsets, vaadinOptions in compileVaadinWidgetsets,
    javaOptions in compileVaadinWidgetsets, target in compileVaadinWidgetsets, thisProject,
    enableCompileVaadinWidgetsets in resourceGenerators, state, streams) map CompileWidgetsetsTask.widgetsetCompiler

  private def addIfNotInArgs(args: Seq[String], param: String, value: String) =
    if (!args.contains(param)) Seq(param, value) else Nil

  def widgetsetCompiler(
    fullCp: Classpath,
    resources: Seq[File],
    widgetsets: Seq[String],
    args: Seq[String],
    jvmArguments: Seq[String],
    target: File,
    p: ResolvedProject,
    enabled: Boolean,
    state: State,
    s: TaskStreams): Seq[File] = {

    implicit val log = s.log

    if (!enabled) {
      log.info("Widgetset compilation disabled.")
      Seq[File]()
    } else {

      IO.createDirectory(target)

      val tmpDir = IO.createTemporaryDirectory

      val jvmArgs = Seq("-Dgwt.persistentunitcachedir=" + tmpDir.absolutePath) ++ jvmArguments

      val cmdArgs = Seq("-war", target absolutePath) ++
        addIfNotInArgs(args, "-extra", tmpDir absolutePath) ++
        addIfNotInArgs(args, "-deploy", tmpDir absolutePath) ++ args

      val exitValue = forkWidgetsetCmd(
        jvmArgs,
        getClassPath(state, fullCp),
        "com.vaadin.tools.WidgetsetCompiler",
        cmdArgs,
        widgetsets,
        resources)

      exitValue match {
        case Left(errorCode) => Nil
        case Right(widgetsets) => {
          log.debug("Deleting %s" format target / "WEB-INF")
          IO.delete(target / "WEB-INF")

          val generatedFiles: Seq[Seq[File]] = widgetsets map {
            widgetset => (target / widgetset ** ("*")).get
          }

          log.debug("Generated files: %s".format(generatedFiles.flatten.mkString(", ")))

          generatedFiles flatten
        }
      }
    }
  }

}
