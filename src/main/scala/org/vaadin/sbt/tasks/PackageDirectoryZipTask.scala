package org.vaadin.sbt.tasks

import sbt._
import sbt.Keys._
import scala.Some
import java.util.jar.{ Attributes, Manifest }
import org.vaadin.sbt.VaadinPlugin.packageVaadinDirectoryZip

/**
 * @author Henri Kerola / Vaadin
 */
object PackageDirectoryZipTask {

  val packageDirectoryZipTask: Def.Initialize[Task[Option[File]]] = (baseDirectory, target, packageBin in Compile,
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
}
