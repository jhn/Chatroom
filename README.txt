Johan Mena

a. Description

    My project consists of two main packages: client and server.

Client:

    The main client program operates by creating a few threads that help manage the
    input from the server as well as user input and keep everything clean. It also
    implements a Terminator class that is essentially a timer that goes off after
    TIME_OUT seconds but resets itself on user input. Exception handling was done
    too.

Server:

    The server package contains a few classes that help modularize functionality and
    reuse constants accross the whole project.

    This program uses the concept of a thread pool, which essentially means that we
    have threads preallocated to do some work. The threads wait for their turn in
    the pool. Once a new task arrives, a task is submitted to the pool and a thread
    takes care of it. Once the thread finishes taking care of the task, it is not
    terminated. Instead, it goes back to the pool and waits for another task. The
    rationally behind this is to not create too many threads, but instead reuse them.
    The number of threads I chose was 15, but you can change this number to something
    higher.

    On a new connection, a thread is executed and starts handling a client through
    the ClientHandler class. This class is basically the meat of the server.
    It authenticates users with the help of a Validator class. This class'
    responsability is to check with the Auditor (the one keeping audits of everything
    that happens in the server) if users exist, if they can authenticate successfully,
    etc.

    The Auditor client has a lot of the shared resources among the rest of the
    threads. It contains information like which users are in record, logged in users,
    blocked users, etc. He is the guy who the Client Handler and Validator threads
    talk to to check for things. Everything is maintained synchronized. It also
    contains a couple of structures that are used to back a couple of new features
    I added (see the last section of this readme for more info on this).

    There is also a Command enum, useful for keeping track of all commands in a sane
    manner and for saving the arity of said commands. It is used to check against user
    input and to route commands to the appropriate methods.

    The User class is a simple bean class that keeps track of users, and is used
    to create User objects off of the user_pass.txt file when the program boots up.

    The MessageQueue class is used to deliver messages to users who might be offline.

    The Utils class has too methods: one for reading the users from the user_pass.txt
    file and creating User objects off those credentials and returning them to the
    auditor. The other method is a simple .join() method to output messages.

    The HTTCall is part of the extra features. This calls composes HTTP requests
    that are sent to a couple of hosts that provide information like weather
    and ip addresses, which is then displayed to the user via intuitive commands.

    The Main class simply runs the whole program.


    Locations of constants:

    TIME_OUT: client/Client.java
    BLOCK_TIME: server/Validator.java
    LAST_HOUR: server/Validator.java

    All times are in seconds.


b. Details on development environment

    I chose Java as my development language.


c. How to run my code

    Compiling:
        From the root of my project issue one of the following:

            To compile everything
                $ make
            To compile only the server
                $ make server
            To compile only the client
                $ make client
            To clean all .class files
                $ make clean
            To clean and recompile everything
                $ make all

        Since my project uses packages for better organization of the clases, the make
        commands will generate the .class files in the src/mn/jhn/client/ and
        src/mn/jhn/server/ directories, not in the root folder.

    Running:
        After .class files have been generated, issue the following commands from
        the root directory:

            To run the server
                $ java -cp src mn.jhn.server.Main [PORT]
            To run the client
                $ java -cp src mn.jhn.client.Client [IPADDRESS] [PORT]


d. Sample commands to invoke my code

    $ java -cp src mn.jhn.server.Main 5050
    $ java -cp src mn.jhn.client.Client localhost 5050


e. Additional features and other cool stuff

Commands
========

  I made a few additions to the available commands:

  The Weather and MyIp commands compose an http request, open a new connection to
  the specified web API, send a request, and get back a payload that is displayed
  to the user in the chat console. You can check out the HTTPCall class.

  * Weather - Syntax: weather <city>
    This commands prints the weather for the given city. Try it with any city:
      weather newyork
      weather austin
      weather paris
    Since we weren't allowed to use external libraries, the output that the user
    sees is the direct payload that we get from the content provider.

  * My Ip - Syntax: myip
    This commands lets the user know its external IP address. It was also
    implemented using web apis and a new socket connection.

    Try it like this:
      myip
      >Your ip:
      {"ip":"69.203.115.168","about":"/about","Pro!":"http://getjsonip.com"}

  * Away - Syntax: away [optional away message]
    This command sets the user status as AWAY and optionally an away message to
    display to the users who try to contact them. For example:

    User wikipedia sets his status as AWAY with an optional message:
    away I'm going to the bathroom, brb!

    User facebook tries to direct message user wikipedia, and sees wikipedia's msg:
    message wikipedia yo wiki, what cha doin boy
    >wikipedia is away. Auto-response: I'm going to the bathroom, brb!

    Naturally, this message is only displayed only when the 'message' command is
    used.

  * Back - Syntax: back
    This command sets the user status from AWAY to ONLINE, and stops displaying
    messages when someone tries to contact the user.

  * Statuses - Syntax: statuses
    This command displays all available users and their statuses. A user can either
    be ONLINE, AWAY, or OFFLINE.
    This command is useful to see who's currently not away and will be reading your
    private messages right away.
    It was implemented with an enum to hold all the statuses (in Auditor) and a
    shared, synchronized map so that everyone can see up-to-date statuses.

    Try it like this:
      statuses
      >facebook online
      >wikipedia away

  * Help - syntax: help
    This command displays all available commands and their descriptions. It's useful
    to see which commands are available. To invoke it, simply type 'help'.

In addition to these features, I also made extensive use of Java's Executor
framework (from Java's Standard Library) to manage thread pooling and to implement
the timers. The result was a highly readabla and manageable code base.
