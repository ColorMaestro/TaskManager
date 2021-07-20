## TaskManager
A plugin for Project Zearth, which makes easier task management.

### Dependencies
This plugin uses Holograms plugin as dependency, which is only icing on the cake. Normally it can operate without holograms,
for that case dependency plugin would be needed to remove from `plugin.yml` and code would be needed to changed a bit.
Plugin can run also instance of discord bot for sending messages also there.

[Here](https://www.spigotmc.org/resources/holograms.4924/) is Holograms webpage at spigotmc.org. There's also a link at
the beginning to GitHub repository, where you can find source code.

[Discord JDA](https://github.com/DV8FromTheWorld/JDA) is a simple library, which is only added to the project.

### Compiling
I use [maven](https://maven.apache.org/) as dependency management system for Java projects.
Maven is integrated into ItelliJ IDEA and possibly Eclipse too, so you can open project in these IDEs or install
maven alone to your computer.

To build the project you need to:

- do `mvn install` which installs all dependencies for project
- do `mvn package`, this builds the project, result JARs will be placed into `target` directory

*Side note:* `mvn clean` command cleans `target` directory.

### Deployment

After build seek for `TaskManager-<version>.jar` in `target` directory and place it into MC server's `plugin` folder.
When the server starts, plugin creates `config.yml` and `db.sqlite` files. In the config, you can set secret token
for discord bot. I have registered discord applications, so I own the secrets. However, it's not hard to create Discord
app. Youtube videos are enough tutorial for it. Database file serves for storing data about players and tasks.

### Updating to new version

Basically you need to change 2 attributes in project and then recompile again.
- In `pom.xml` there's `<dependencies>` scope, which contains all dependencies for this project.
  Spigot API has `spigot-api` in `artifactId` key. Here's important `version` field. Its values may not be intuitive
  at first look, therefore I'm enclosing their [list](https://hub.spigotmc.org/nexus/content/repositories/snapshots/org/spigotmc/spigot-api/).
- Next seek for `plugin.yml` file which is located in `src/main/resources` folder.
  Inside is `api-version` key, which probably somehow tells the server, for what version is the plugin made for.
  Here is versioning simple 1.15, 1.16, 1.17 like versions of minecraft.
  
As for other things:
- Hologram plugin needs to be watched for updates. Check github because they may not update info at their spigot page.
- Discord JDA 4.2.1 is enough. 
