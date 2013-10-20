# sbt-vaadin-plugin

sbt-vaadin-plugin is a SBT plugin that makes possible to use SBT to build Vaadin projects. The plugin is for Vaadin 7 and SBT 0.13.

The plugin has a dependency to the [xsbt-web-plugin](https://github.com/JamesEarlDouglas/xsbt-web-plugin) plugin.

Currently the plugin provides the following tasks:

 * `compileVaadinWidgetsets` Compiles Vaadin widgetsets into JavaScript.
 * `compileVaadinThemes` Compiles Vaadin SCSS themes into CSS.
 * `vaadinDevMode` Run Development Mode to debug Vaadin client-side code and to avoid Java to JavaScript recompilation during development.
 * `vaadinSuperDevMode` Run Super Dev Mode to recompile client-side code in a browser. Also debugging in the browser using source maps is possible.
 * `packageVaadinDirectoryZip` Creates a zip file that can be uploaded to Vaadin Directory.

## Discussion

Questions, problems or comments? [Vaadin Forum](https://vaadin.com/forum) is the place for discussion about the sbt-vaadin-plugin.

## Usage

Define the plugin as a dependency to your project by adding the following lines into `project/plugins.sbt`:

	resolvers += "sbt-vaadin-plugin repo" at "http://henrikerola.github.io/repository/snapshots"

    addSbtPlugin("org.vaadin.sbt" % "sbt-vaadin-plugin" % "0.1-SNAPSHOT")
    
After that you need to enabled the plugin on the projects you want to use it. This is done by including settings from `vaadinSettings`, `vaadinAddOnSettings` or `vaadinWebSettings`:

 * `vaadinSettings` contains all settings and tasks provided by the plugin.
 * `vaadinAddOnSettings` is an extension to `vaadinSettings` and it's suitable for projects that produce a Vaadin add-on jar.
 * `vaadinWebSettings` contains settings from `vaadinSettings` and [xsbt-web-plugin](https://github.com/JamesEarlDouglas/xsbt-web-plugin)'s `webSettings`.


The plugin doesn't add any Vaadin dependencies, those must be added explicitly to the projects using the plugin.

### Examples

 * A multi-module Vaadin Java add-on project: [PopupButton](https://github.com/henrikerola/PopupButton/tree/2.3)
 * A simple Scaladin application: [Scaladin chat](https://github.com/henrikerola/scaladin-chat/tree/scaladin-3.0)


### Compiling widgetsets

The task to compile Vaadin widgetsets is:

    compileVaadinWidgetsets
    
This task compiles widgetsets defined in `widgetsets` into `target in compileWidgetsets`. If `widgetsets` is an empty list the task tries to find widgetsets from project's resource directories (`resourceDirectories in Compile`).

Configuration examples:

     vaadinWidgetsets := Seq("com.example.MyWidgetset")

     // Widgetset compilation needs memory and to avoid an out of memory error it usually needs more memory:
     javaOptions in compileVaadinWidgetsets := Seq("-Xss8M", "-Xmx512M", "-XX:MaxPermSize=512M")
     
     vaadinOptions in compileVaadinWidgetsets := Seq("-logLevel", "DEBUG", "-strict")
     
     // Compile widgetsets into the source directory (by default themes are compiled into the target directory)
     target in compileVaadinWidgetsets := (sourceDirectory in Compile).value / "webapp" / "VAADIN" / "widgetsets"

### Compiling themes

Vaadin SCSS themes can be compiled into CSS by using a task called:

    compileVaadinThemes

This task compiles themes defined in `themes`. If `themes` is an empty list the task tries to find themes from folders defined in `themesDir`. Themes are compiled into folder defined by `target in compileThemes`.

Configuration examples:

    vaadinThemes := Seq("mytheme")

    // Compile themes into the source directory (by default themes are compiled into the target directory)
    target in compileVaadinThemes := (sourceDirectory in Compile).value / "webapp" / "VAADIN" / "themes"


### Development Mode

Start Development Mode by saying:

    vaadinDevMode
    

Configuration examples:

	// This makes possible to attach a remote debugger when development mode is started from the command line
    javaOptions in vaadinDevMode ++= Seq("-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005")


### SuperDevMode

SuperDevMode Mode can be started by saying:

    vaadinSuperDevMode


## Creating a Directory zip

A Vaadin Directory compatible zip can be created by using a task called

    packageVaadinDirectoryZip

By default the task creates a zip file that contains a file `META-INF/MANIFEST.MF` and three jars (binary, sources and JavaDoc). The manifest contains definitions needed by Vaadin Directory (`Implementation-Title`, 
`Implementation-Version`, `Vaadin-Package-Version` and `Vaadin-Addon`).

It's possible to define files to be included into the zip. The following overrides the default, and two files are included into the zip (binary and sources jars):

    mappings in packageVaadinDirectoryZip <<= (packageBin in Compile, packageSrc in Compile) map {
      (bin, src) => Seq((bin, bin.name), (src, src.name))
    }

This adds a file:

    mappings in packageVaadinDirectoryZip <+= baseDirectory map { base =>
      (base / "LICENSE-2.0.txt") -> "LICENSE"
    }

## License

sbt-vaadin-plugin is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
