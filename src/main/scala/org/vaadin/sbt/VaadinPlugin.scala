package org.vaadin.sbt

import sbt._
import sbt.Keys._
import org.vaadin.sbt.util.WidgetsetUtil
import java.util.jar.{ Attributes, Manifest }
import com.earldouglas.xsbtwebplugin.WebPlugin.webSettings
import com.earldouglas.xsbtwebplugin.PluginKeys.webappResources
import java.io.File
import org.vaadin.sbt.util.ProjectUtil._
import org.vaadin.sbt.util.ForkUtil._
import org.vaadin.sbt.tasks._

object VaadinPlugin extends Plugin with VaadinKeys {

  def addIfNotInArgs(args: Seq[String], param: String, value: String) =
    if (!args.contains(param)) Seq(param, value) else Nil

  val compileWidgetsetsTask = compileWidgetsets <<= (dependencyClasspath in Compile, resourceDirectories in Compile,
    widgetsets in compileWidgetsets, options in compileWidgetsets, javaOptions in compileWidgetsets,
    target in compileWidgetsets, thisProject, enableCompileWidgetsets, state,
    streams) map CompileWidgetsetsTask.compileWidgetsets

  val devModeTask = devMode <<= (dependencyClasspath in Compile, resourceDirectories in Compile, widgetsets in devMode,
    options in devMode, javaOptions in devMode, target in devMode, state, streams) map {
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

  val superDevModeTask = superDevMode <<= (dependencyClasspath in Compile, unmanagedSourceDirectories in Compile,
    resourceDirectories in Compile, widgetsets in superDevMode, options in superDevMode, javaOptions in superDevMode,
    target, state, streams) map { (fullCp, sources, resources, widgetsets, args, jvmArgs, target, state, s) =>
      implicit val log = s.log

      forkWidgetsetCmd(
        jvmArgs,
        getClassPath(state, fullCp),
        "com.google.gwt.dev.codeserver.CodeServer",
        args,
        widgetsets,
        resources)
    }

  val compileThemesTask = compileThemes <<= (dependencyClasspath in Compile, themesDir, themes,
    target in compileThemes, streams) map { (dependencyCp, themesDir, themes, target, s) =>
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

          forkJava(Nil, Seq(
            "-classpath", dependencyCp.map(_.data.absolutePath).mkString(File.pathSeparator),
            "com.vaadin.sass.SassCompiler",
            inputFile.absolutePath,
            outputFile.absolutePath
          ))

          Option(outputFile)
        }
      }

      generatedFiles.flatten
    }

  val packageDirectoryZipTask = packageDirectoryZip <<= (baseDirectory, target, packageBin in Compile,
    mappings in packageDirectoryZip, name in Compile, version in Compile) map {
      (base, target, addonBin, mappings, name, version) =>
        val manifest = new Manifest
        val mainAttributes = manifest.getMainAttributes
        // Manifest-Version is needed, see: http://bugs.sun.com/view_bug.do?bug_id=4271239
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0")
        mainAttributes.put(new Attributes.Name("Vaadin-Package-Version"), "1")
        mainAttributes.put(new Attributes.Name("Implementation-Title"), name)
        mainAttributes.put(new Attributes.Name("Implementation-Version"), version)
        mainAttributes.put(new Attributes.Name("Vaadin-Addon"), addonBin.name)

        val output = target / (addonBin.name.replace(".jar", ".zip"))
        IO.jar(mappings, output, manifest)

        Some(output)
    }

  val vaadinSettings = Seq(

    compileWidgetsetsTask,
    widgetsets := Nil,
    enableCompileWidgetsets := true,
    target in compileWidgetsets := (resourceManaged in Compile).value / "webapp" / "VAADIN" / "widgetsets",
    options in compileWidgetsets := Nil,
    javaOptions in compileWidgetsets := Nil,

    devModeTask,
    widgetsets in devMode := (widgetsets in compileWidgetsets).value,
    target in devMode := (target in compileWidgetsets).value,
    options in devMode := Nil,
    javaOptions in devMode := (javaOptions in compileWidgetsets).value,

    superDevModeTask,
    widgetsets in superDevMode := (widgetsets in compileWidgetsets).value,
    options in superDevMode := Nil,
    javaOptions in superDevMode := (javaOptions in compileWidgetsets).value,

    compileThemesTask,
    themes := Nil,
    themesDir <<= sourceDirectory(sd => Seq(sd / "main" / "webapp" / "VAADIN" / "themes")),
    target in compileThemes := (resourceManaged in Compile).value / "webapp" / "VAADIN" / "themes",

    packageDirectoryZipTask,
    mappings in packageDirectoryZip <<= (packageBin in Compile, packageSrc in Compile, packageDoc in Compile) map {
      (bin, src, doc) => Seq((bin, bin.name), (src, src.name), (doc, doc.name))
    }

  )

  val vaadinAddOnSettings = vaadinSettings ++ Seq(
    packageOptions in (Compile, packageBin) += {
      val manifest = new Manifest
      val mainAttributes = manifest.getMainAttributes
      val definedWidgetsets = (widgetsets in compileWidgetsets).value
      val resources = (resourceDirectories in (Compile, widgetsets)).value
      val widgetsetsValue = WidgetsetUtil.findWidgetsets(definedWidgetsets, resources).mkString(",")

      mainAttributes.putValue("Vaadin-Package-Version", "1")
      mainAttributes.putValue("Implementation-Title", (name in Compile).value)
      mainAttributes.putValue("Implementation-Version", (version in Compile).value)
      if (widgetsetsValue != "") {
        mainAttributes.putValue("Vaadin-Widgetsets", widgetsetsValue)
      }

      Package.JarManifest(manifest)
    },

    // Include source files into the binary jar file. Widgetset compiler needs those.
    mappings in (Compile, packageBin) <++= (unmanagedSources in Compile, unmanagedSourceDirectories in Compile,
      baseDirectory in Compile) map { (srcs, sdirs, base) =>
        ((srcs --- sdirs --- base) pair (relativeTo(sdirs) | relativeTo(base) | flat)) toSeq
      }
  )

  val vaadinWebSettings = vaadinSettings ++ webSettings ++ Seq(
    resourceGenerators in Compile <+= (dependencyClasspath in Compile, resourceDirectories in Compile,
      widgetsets in compileWidgetsets, options in compileWidgetsets, javaOptions in compileWidgetsets,
      target in compileWidgetsets, thisProject, enableCompileWidgetsets in resourceGenerators, state,
      streams) map CompileWidgetsetsTask.compileWidgetsets,
    resourceGenerators in Compile <+= compileThemes,
    webappResources in Compile <+= (resourceManaged in Compile)(sd => sd / "webapp")
  )

}