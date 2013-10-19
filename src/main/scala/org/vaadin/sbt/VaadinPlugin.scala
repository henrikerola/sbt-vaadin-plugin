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

  val superDevModeTask = vaadinSuperDevMode <<= (dependencyClasspath in Compile, unmanagedSourceDirectories in Compile,
    resourceDirectories in Compile, vaadinWidgetsets in vaadinSuperDevMode, vaadinOptions in vaadinSuperDevMode, javaOptions in vaadinSuperDevMode,
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

  val compileThemesTask = compileVaadinThemes <<= (dependencyClasspath in Compile, vaadinThemesDir, vaadinThemes,
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

  val packageDirectoryZipTask = packageVaadinDirectoryZip <<= (baseDirectory, target, packageBin in Compile,
    mappings in packageVaadinDirectoryZip, name in Compile, version in Compile) map {
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

    compileVaadinWidgetsets <<= CompileWidgetsetsTask.compileWidgetsetsTask,
    vaadinWidgetsets := Nil,
    enableCompileVaadinWidgetsets := true,
    target in compileVaadinWidgetsets := (resourceManaged in Compile).value / "webapp" / "VAADIN" / "widgetsets",
    vaadinOptions in compileVaadinWidgetsets := Nil,
    javaOptions in compileVaadinWidgetsets := Nil,

    vaadinDevMode <<= DevModeTask.devModeTask,
    vaadinWidgetsets in vaadinDevMode := (vaadinWidgetsets in compileVaadinWidgetsets).value,
    target in vaadinDevMode := (target in compileVaadinWidgetsets).value,
    vaadinOptions in vaadinDevMode := Nil,
    javaOptions in vaadinDevMode := (javaOptions in compileVaadinWidgetsets).value,

    superDevModeTask,
    vaadinWidgetsets in vaadinSuperDevMode := (vaadinWidgetsets in compileVaadinWidgetsets).value,
    vaadinOptions in vaadinSuperDevMode := Nil,
    javaOptions in vaadinSuperDevMode := (javaOptions in compileVaadinWidgetsets).value,

    compileThemesTask,
    vaadinThemes := Nil,
    vaadinThemesDir <<= sourceDirectory(sd => Seq(sd / "main" / "webapp" / "VAADIN" / "themes")),
    target in compileVaadinThemes := (resourceManaged in Compile).value / "webapp" / "VAADIN" / "themes",

    packageDirectoryZipTask,
    mappings in packageVaadinDirectoryZip <<= (packageBin in Compile, packageSrc in Compile, packageDoc in Compile) map {
      (bin, src, doc) => Seq((bin, bin.name), (src, src.name), (doc, doc.name))
    }

  )

  val vaadinAddOnSettings = vaadinSettings ++ Seq(
    packageOptions in (Compile, packageBin) += {
      val manifest = new Manifest
      val mainAttributes = manifest.getMainAttributes
      val definedWidgetsets = (vaadinWidgetsets in compileVaadinWidgetsets).value
      val resources = (resourceDirectories in (Compile, vaadinWidgetsets)).value
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
    resourceGenerators in Compile <+= CompileWidgetsetsTask.compileWidgetsetsInResourceGeneratorsTask,
    resourceGenerators in Compile <+= compileVaadinThemes,
    webappResources in Compile <+= (resourceManaged in Compile)(sd => sd / "webapp")
  )

}