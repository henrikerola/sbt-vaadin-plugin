package org.vaadin.sbt.tasks

import sbt._
import sbt.Keys._
import java.util.jar.{ Attributes, Manifest }
import org.vaadin.sbt.VaadinPlugin.{ packageVaadinDirectoryZip, vaadinAddonMappings }

/**
 * @author Henri Kerola / Vaadin
 */
object PackageDirectoryZipTask {

  val packageDirectoryZipTask: Def.Initialize[Task[Option[File]]] = (baseDirectory, target,
    vaadinAddonMappings in packageVaadinDirectoryZip, mappings in packageVaadinDirectoryZip, name in Compile,
    version in Compile, streams) map {
      (base, target, addonMappings, mappings, name, version, s) =>

        implicit val log = s.log

        val manifest = new Manifest
        val mainAttributes = manifest.getMainAttributes
        // Manifest-Version is needed, see: http://bugs.sun.com/view_bug.do?bug_id=4271239
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0")
        mainAttributes.put(new Attributes.Name("Vaadin-Package-Version"), "1")
        mainAttributes.put(new Attributes.Name("Implementation-Title"), name)
        mainAttributes.put(new Attributes.Name("Implementation-Version"), version)
        mainAttributes.put(new Attributes.Name("Vaadin-Addon"), addonMappings.map(_._2).mkString(","))

        // Expecting that the first element in the addonJars defines the name of the zip
        val output = target / (addonMappings.head._2.replace(".jar", ".zip"))
        log.info("Packaging %s ..." format output)
        IO.jar(addonMappings ++ mappings, output, manifest)
        log.info("Done packaging.")

        Some(output)
    }
}
