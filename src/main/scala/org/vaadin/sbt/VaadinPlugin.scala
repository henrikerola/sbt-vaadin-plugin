package org.vaadin.sbt

import sbt._
import sbt.Keys._
import org.vaadin.sbt.util.WidgetsetUtil
import java.util.jar.Manifest
import com.earldouglas.xsbtwebplugin.WebPlugin.webSettings
import com.earldouglas.xsbtwebplugin.PluginKeys.webappResources
import org.vaadin.sbt.tasks._

object VaadinPlugin extends Plugin with VaadinKeys {

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

    vaadinSuperDevMode <<= SuperDevModeTask.superDevModeTask,
    vaadinWidgetsets in vaadinSuperDevMode := (vaadinWidgetsets in compileVaadinWidgetsets).value,
    vaadinOptions in vaadinSuperDevMode := Nil,
    javaOptions in vaadinSuperDevMode := (javaOptions in compileVaadinWidgetsets).value,

    compileVaadinThemes <<= CompileThemesTask.compileThemesTask,
    vaadinThemes := Nil,
    vaadinThemesDir <<= sourceDirectory(sd => Seq(sd / "main" / "webapp" / "VAADIN" / "themes")),
    target in compileVaadinThemes := (resourceManaged in Compile).value / "webapp" / "VAADIN" / "themes",

    packageVaadinDirectoryZip <<= PackageDirectoryZipTask.packageDirectoryZipTask,
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