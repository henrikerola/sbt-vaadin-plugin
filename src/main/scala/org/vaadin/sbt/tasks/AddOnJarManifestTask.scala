package org.vaadin.sbt.tasks

import sbt._
import sbt.Keys._
import java.util.jar.Manifest
import org.vaadin.sbt.util.WidgetsetUtil
import org.vaadin.sbt.VaadinPlugin.{ compileVaadinWidgetsets, vaadinWidgetsets }

/**
 * @author Henri Kerola / Vaadin
 */
object AddOnJarManifestTask {

  val addOnJarManifestTask: Def.Initialize[Task[PackageOption]] = (vaadinWidgetsets in compileVaadinWidgetsets,
    resourceDirectories in (Compile, vaadinWidgetsets), name in Compile, version in Compile) map {
      (definedWidgetsets, resources, name, version) =>

        val manifest = new Manifest
        val mainAttributes = manifest.getMainAttributes
        val widgetsetsValue = WidgetsetUtil.findWidgetsets(definedWidgetsets, resources).mkString(",")

        mainAttributes.putValue("Vaadin-Package-Version", "1")
        mainAttributes.putValue("Implementation-Title", name)
        mainAttributes.putValue("Implementation-Version", version)
        if (widgetsetsValue != "") {
          mainAttributes.putValue("Vaadin-Widgetsets", widgetsetsValue)
        }

        Package.JarManifest(manifest)
    }
}
