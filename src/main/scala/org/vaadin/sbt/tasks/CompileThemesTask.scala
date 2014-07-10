package org.vaadin.sbt.tasks

import sbt._
import sbt.Keys._
import java.io.File
import org.vaadin.sbt.util.ForkUtil._
import org.vaadin.sbt.VaadinPlugin.{ compileVaadinThemes, vaadinThemes, vaadinThemesDir }

/**
 * @author Henri Kerola / Vaadin
 */
object CompileThemesTask {

  val compileThemesTask: Def.Initialize[Task[Seq[File]]] = (dependencyClasspath in Compile, vaadinThemesDir, vaadinThemes,
    target in compileVaadinThemes, streams) map { (dependencyCp, themesDir, themes, target, s) =>

      implicit val log = s.log

      val themeNames = if (themes.nonEmpty) {
        log.debug("Using themes %s defined in the build configuration" format themes.mkString(", "))
        themes
      } else {
        log.debug("No themes defined in the build configuration. Trying to find theme folders from %s" format themesDir)
        val themeFoldersFinder = (themesDir) * "*" filter { f => (f / "styles.scss").exists() }
        val foundThemeFolders = themeFoldersFinder.get.map(_.getName)

        log.debug("Found %d theme folder(s): %s" format (foundThemeFolders.size, foundThemeFolders.mkString(", ")))
        foundThemeFolders
      }

      IO.createDirectory(target)

      val generatedFiles = themeNames.map { themeName =>
        val inputFileSeq = (themesDir / themeName / "styles.scss").get
        inputFileSeq.headOption.fold[Option[File]] {
          log.error("Cannot compile theme '%s' because its file 'styles.scss' doesn't exist." format themeName)
          None
        } { inputFile =>
          val outputFile = target / themeName / "styles.css"
          // SassCompiler throws an IOException if theme folder doesn't exist
          IO.createDirectory(outputFile.getParentFile)

          log.info("Compiling theme '%s'" format themeName)

          val exitValue = forkJava(Nil, Seq(
            "-classpath", dependencyCp.map(_.data.absolutePath).mkString(File.pathSeparator),
            "com.vaadin.sass.SassCompiler",
            inputFile.absolutePath,
            outputFile.absolutePath
          ))

          if (exitValue != 0) {
            sys.error("Non-zero return code (%d) from com.vaadin.sass.SassCompiler" format (exitValue))
          }

          Option(outputFile)
        }
      }

      generatedFiles.flatten
    }

}
