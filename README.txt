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
    talk to to check for things.

    There is also a Command enum, useful for keeping track of all commands in a sane
    manner and for saving the arity of said commands. It is used to check against user
    input and to route commands to the appropriate methods.

    The User class is a simple bean class that keeps track of users, and is used
    to create User objects off of the user_pass.txt file when the program boots up.

    The MessageQueue class is used to deliver messages to users who might be offline.

    The Utils class has too methods: one for reading the users from the user_pass.txt
    file and creating User objects off those credentials and returning them to the
    auditor. The other method is a simple .join() method to output messages.


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


e. Additional features

  * Use of the Executor framework (from Java's Standard Library) to manage thread
    pooling.

