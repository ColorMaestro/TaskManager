## TaskManager
A plugin for Project Zearth, which makes easier task management.

### Dependencies
TaskManager uses Holograms plugin to make nice holograms as task lists on member's plots. Naturally, this is only icing
on the cake of whole functionality, so if you don't add Holograms plugin on server, TaskManager will be okay with it.

TaskManager can hold the post of discord bot (AI user). This means that notification about significant changes
(new task/finished task/approved task/changed assignee/returned task for redoing) are automatically sent to related
players discord as direct messages if they are not online on server. This feature in built-in and can be controlled by
plugin's configuration.

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
app. YouTube's videos are enough tutorial for it. Database file serves for storing data about players and tasks.

### Updating to new version

Basically you need to change 2 attributes in project and then recompile again.
- In `pom.xml` there's `<dependencies>` scope, which contains all dependencies for this project.
  Spigot API has `spigot-api` in `artifactId` key. Here's important `version` field. Its values may not be intuitive
  at first look, therefore I'm enclosing their [list](https://hub.spigotmc.org/nexus/content/repositories/snapshots/org/spigotmc/spigot-api/).
- Next seek for `plugin.yml` file which is located in `src/main/resources` folder.
  Inside is `api-version` key, which probably somehow tells the server, for what version is the plugin made for.
  Here is versioning simple 1.15, 1.16, 1.17 like versions of minecraft.
  
As for other things:
- Hologram plugin needs to be watched for updates. Check GitHub because they may not update info at their spigot page.
- Discord JDA 4.3.0_277 is enough. (Currently newest 28.7.2021)

### Commands and usage

This is the list of all commands. It can be obtained also in game with `/tasks help`:
- `/tasks help` - shows this help
- `/tasks given` - shows tasks, which you are advising
- `/tasks stats` - shows task statistics
- `/tasks [IGN]` - shows your or other player tasks
- `/visittask <id>` - teleports to the task workplace
- `/taskinfo <id>` - obtains info in book for related task
- `/addtask <IGN>` - creates task assignment book with blank description
- `/addtask <IGN> [id]` - creates task assignment book, description is taken from selected task
- `/finishtask <id>` - marks task as finished
- `/approvetask <id> [force]` - approves the finished task
- `/returntask <id> [force]` - returns task back to given (unfinished) state
- `/transfertask <id> <IGN>` - changes the assignee of the task
- `/settaskplace <id>` - sets spawning point for this task, only assignee and advisor of the task can do this
- `/linkdiscord` - links discord account for notifications
- `/establish` - establishes the Hologram where is summary of players tasks.

When new player comes to the server, plugin automatically stores him into database, so you can start giving task
straight away :) This is done by one of four background jobs. Remaining 3 are:
- checking if player has established hologram for tasks, if not, plugin informs him
- checking if player has linked discord for notifications, if not, plugin informs him
- checking whether there are some finished tasks, in which you play role of advisor (this is for project managers), if
yes plugin forms you