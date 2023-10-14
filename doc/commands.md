### Commands and usage

This is the list of all commands. It can be obtained also in game with `/tasks help`:
- `/tasks help` - shows this help
- `/addmember <IGN>` - adds player as member
- `/dashboard` - shows tasks dashboard
- `/dashboard <IGN>` - jumps directly in dashboard to active tasks of selected member
- `/tasks given` - shows tasks, which you are advising
- `/tasks stats` - shows task statistics
- `/tasks prepared` - shows task which are prepared for members
- `/tasks idle` - shows task on which members work too long - which we consider 30 days
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
- `/establish` - establishes the hologram where is summary of players tasks.

When **member** (priorly added via `addmember` command) comes to the server, plugin automatically performs some actions:
- updating last login time of member
- checking if member has changes his name, if yes, it is updated
- checking if member has established hologram for tasks, if not, plugin informs him
- checking if member has linked discord for notifications, if not, plugin informs him
- checking whether there are some finished tasks, in which you play role of advisor (this is for project managers), if
  yes, plugin informs you