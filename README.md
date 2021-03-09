## TaskManager
A plugin for Project Zearth, which makes easier task management.

### Dependencies
This plugin uses Holograms plugin as dependency, which is only icing on the cake. Normally it can operate without holograms,
for that case dependency plugin would be needed to remove from `plugin.yml` and code would be needed to changed a bit.

[Here](https://www.spigotmc.org/resources/holograms.4924/) is plugin's webpage at spigotmc.org. There's also a link at
the beginning to GitHub repository, where you can find source code.

### Compiling
I use [maven](https://maven.apache.org/) to build Java projects. Dependency management is very easy with it.
Maven is integrated into ItelliJ IDEA, so you can open project in this IDE or install maven to your computer.

To build the project you need to:

- do `mvn install` which installs all dependencies for project
- do `mvn package`, this builds the project, result JARs will be placed into `target` directory

For deployment take `TaskManager-<version>.jar` and place it into MC server's `plugin` folder

Next useful info:
- `mvn clean` command cleans `target` directory
- in `pom.xml` there's `<dependencies>` scope, which contains all dependencies for this project.
One of them is `spigot-api`. Its version determines the Spigot API, which is used in this Java project. There's also
  `plugin.yml` file where's `api-version` key, which probably tells the server, for what version is the plugin made for.
  If MC server version change, the only thing you need to do is change version these fields and then proceed
  with build as described before.