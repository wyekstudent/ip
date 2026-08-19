# Console UI Test Plan

The test runner compiles the Java sources first, then runs each command in a
fresh console session. Expected output is exact apart from line-ending style,
line-end spaces, and one final newline.

## Case: add and list task types

### Aim
Verify that todo, deadline, and event commands are added and displayed in list order.

### Command
```powershell
java -cp build\classes Dingleberry
```

### Input
```
todo borrow book
list
deadline return book /by Sunday
event project meeting /from Mon 2pm /to 4pm
list
bye
```

### Expected output
```
____________________________________________________________
 ____  _             _      _                          
|  _ \(_)_ __   __ _| | ___| |__   ___ _ __ _ __ _   _ 
| | | | | '_ \ / _` | |/ _ \ '_ \ / _ \ '__| '__| | | |
| |_| | | | | | (_| | |  __/ |_) |  __/ |  | |  | |_| |
|____/|_|_| |_|\__, |_|\___|_.__/ \___|_|  |_|   \__, |
               |___/                              |___|

Hey There! I'm Dingleberry
What can I do for you?
____________________________________________________________

____________________________________________________________
Got it. I've added this task:
  [T][ ] borrow book
Now you have 1 tasks in the list.
____________________________________________________________
____________________________________________________________
1.[T][ ] borrow book
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [D][ ] return book (by: Sunday)
Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [E][ ] project meeting (from: Mon 2pm to: 4pm)
Now you have 3 tasks in the list.
____________________________________________________________
____________________________________________________________
1.[T][ ] borrow book
2.[D][ ] return book (by: Sunday)
3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
____________________________________________________________
Bya hope to see your berries again!
```

## Case: reject unsupported commands

### Aim
Verify that unsupported commands are rejected and the chatbot continues accepting valid commands.

### Command
```powershell
java -cp build\classes Dingleberry
```

### Input
```
todo write report
mark 1
list
bye
```

### Expected output
```
____________________________________________________________
 ____  _             _      _                          
|  _ \(_)_ __   __ _| | ___| |__   ___ _ __ _ __ _   _ 
| | | | | '_ \ / _` | |/ _ \ '_ \ / _ \ '__| '__| | | |
| |_| | | | | | (_| | |  __/ |_) |  __/ |  | |  | |_| |
|____/|_|_| |_|\__, |_|\___|_.__/ \___|_|  |_|   \__, |
               |___/                              |___|

Hey There! I'm Dingleberry
What can I do for you?
____________________________________________________________

____________________________________________________________
Got it. I've added this task:
  [T][ ] write report
Now you have 1 tasks in the list.
____________________________________________________________
____________________________________________________________
Oops, Dingleberry doesn't know that command: I don't recognize that command. Use 'todo', 'list', 'deadline', or 'event'.
Please try again with the correct command and parameters.
____________________________________________________________
____________________________________________________________
1.[T][ ] write report
____________________________________________________________
Bya hope to see your berries again!
```