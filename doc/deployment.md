### Dependencies
TaskManager uses DecentHolograms plugin to make nice holograms as task lists on member's plots. Naturally, this is only icing
on the cake of whole functionality, so if you don't add DecentHolograms plugin on server, TaskManager will be okay with it.

TaskManager can serve as instance of discord bot (AI user). Notification about significant changes are automatically sent
to related members discord as direct messages if they are not online on server. This feature in built-in and can be
controlled by plugin's configuration. Notifications are sent in these situations:

- Adding new task
- Assigning prepared task
- Finishing task
- Approving task
- Returning task
- Transferring task

[Here](https://www.spigotmc.org/resources/decentholograms-1-8-1-20-1-papi-support-no-dependencies.96927/) is DecentHolograms
webpage at spigotmc.org. There's also a link at the beginning to GitHub repository, where you can find source code.

[Discord JDA](https://github.com/DV8FromTheWorld/JDA) library is used to communicate with discord.

### Compiling
You can find JAR files here in GitHub at releases, however currently we build for minecraft version 1.18. Compatibility
with older versions of minecraft is not guaranteed. Since ColorMaestro has little knowledge in this area here is guide
how to compile project yourself:

Project uses [maven](https://maven.apache.org/) as dependency management system for Java projects.
Maven is integrated into IntelliJ IDEA, so you can open project in this IDE or install maven standalone to your machine.

To build the project you need to:

- do `mvn install` which installs all dependencies for project
- do `mvn package`, this builds the project, result JARs will be placed into `target` directory

*Side note:* `mvn clean` command cleans `target` directory.

### Deployment

After build take `TaskManager-<version>.jar` in `target` directory and place it into MC server's `plugin` folder.
When the server starts, plugin creates `config.yml` and `db.sqlite` files.

Database file serves for storing data about players and tasks.

In the config, you can set secret token for discord bot. You need to create a bot before of course. However, it's not
hard to create one. Watch some YouTube's videos or seek some tutorial for it.

### Updating to new version

Basically you need to change 2 attributes in project and then recompile again:
- In `pom.xml` there's `<dependencies>` scope, which contains all dependencies for this project.
  Spigot API has `spigot-api` in `artifactId` key. Here's important `version` field. Its values may not be intuitive
  at first look, therefore I'm enclosing their [list](https://hub.spigotmc.org/nexus/content/repositories/snapshots/org/spigotmc/spigot-api/).
- Next seek for `plugin.yml` file which is located in `src/main/resources` folder.
  Inside is `api-version` key, which probably somehow tells the server, for what version is the plugin made for.
  Here is versioning simple 1.15, 1.16, 1.17 like versions of minecraft.

As for other things:
- DecentHologram plugin sends notification into chat if new version is published. You can also check GitHub page.
- Discord JDA 4.3.0_277 is enough.
