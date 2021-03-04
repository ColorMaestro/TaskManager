## TaskManager
TaskManager plugin for Project Zearth, which makes easier task management.

### Compiling
I use [maven](https://maven.apache.org/) to build Java projects. Dependency management is very easy with it.
Maven is integrated into ItelliJ IDEA, so you can open project in this IDE or install maven to your computer.

To build the project you need to:

- do `mvn install` which installs all dependencies for project
- do `mvn build`, this builds the project, result JARs will be placed into `target` directory

For deployment take `TaskManager-<version>.jar` and place it into MC server's `plugin` folder

Next useful info:
- `mvn clean` command cleans `taget` directory
- in `pom.xml` there's `<dependencies>` scope, which contains all dependencies for this project.
One of them is `spigot-api`. Its version determines the version of MC server, at which will be the plugin ran.
  If MC server version change, the only thing you need to do is change version of this dependency and then proceed
  with build as described before.