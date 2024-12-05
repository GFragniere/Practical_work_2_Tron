# DAI_Practical_Work_2_Tron
This project was conceptualized and realised by Guillaume Fragnière and Killian Viquerat in the context of the DAI class in HEIG-VD.
## Introduction
This program is a small project coded in Java, and it's purpose is play a simple game of Tron with up to 4 players over the network.
The project supports 2 version of execution, namely:
- Server: in order to run the server that will host the game.
- Client: in order to play the game from a given client.

The implementation choices as well as functioning of the program will be detailed and explained later in this document.
## Implementation
This project was created with the intent of being launchable in the command line. Therefore, we used the picocli framework to do so.
To package the application, we also used Maven for the creation of a `.jar` executable file.

Considering the application must communicate through the network, we had to choose a communication protocol to allow for such a task.
We chose to communicate using the UDP communication protocol, allowing for both Unicast and Multicast.

The Unicast communication takes place when a client attempts to connect to the server, or when he sends data in order to update its direction to the server.
In case of a request to join the server, the said server will reply either with `OK` to let him know he joined, or with `ERROR`, followed by the corresponding error code (detailed in the [RFC](Documents/RFC.md)) depending on what happened.
If the player sends an `UPDATE` request, the server will simply process it and not send a response.

The Multicast communication is taking place when the server broadcasts the game info on the network.
All clients connected to the server receive the full game information, and update their player according to the choice they make.

In order for data to be transmitted over the network and considering the variety of data we need to communicate, we use an ObjectOutputStream to write and read objects from.
To know what type of objects to read, the packet always contains, in this order:
- the amount of objects written to the stream by the sender.
- the type of message sent (i.e. `JOIN`, `UPDATE` or `ERROR`).
- the objects needed to correctly treat the request.

## GitHub
To create and organize our workflow, we used a GitHub repository.
### Work separation
In order to properly work on this project, we used a workflow which consisted of:
1. Creating an issue regarding a new feature/problem.
2. Creating a branch locally in order to implement the idea/fix the problem.
3. Make the changes locally, and then commit the changes onto the branch in the GitHub repository.
4. Create a pull request to merge the branch created and the main branch, and link it to the corresponding issue in order to close it.
5. The pull request must be approved by a person that did not work on the feature in order to force reviews to make sure that unnecessary features/coding problems would not be inserted into the main branch.

We used different branches every time we worked on a new aspect of the program, or when we had to fix some issues when merging some code (for example when merging the UDP communication with the game data).
## How to contribute

### Git Clone
Your first step is to clone the project from the repo to your computer

```bash
git clone git@github.com:GFragniere/Practical_work_2_Tron.git
```

### IntelliJ Idea

If your using IntelliJ IDEA, the configuration for the packaging of the application will be automatically available inside the IDE.
As well as the running one, although it is recommended that you first package the application in order to run it properly.

The running configuration will just not have parameters precised, so you will need to add them to use the different command available.

## Maven

For those using only the command line, to package the application you will need to run this command:

```bash
mvn dependency:go-offline clean compile package
```

## Running the application

After packaging the application you can run this command to print the help message:
```bash
java -jar ./target/DAI_Practical_Work_2_Tron-1.0-SNAPSHOT.jar -h

Usage: DAI_Practical_Work_1_Magic_Image_CLI-1.0-SNAPSHOT.jar [-hV] [COMMAND]
Tronocol game made with protocol UDP.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  Server  Start the server part of the Tronocol application
  client  Start the client part of the Tronocol application
```

## Functioning of the program
As stated before, this program can be used both as server and client for connecting to a game of Tron.

### Server
The server usage is basically used to create a server and host the game on it. If launched using java, you will need to use the following command in bash:
```bash
java -jar ./target/DAI_Practical_Work_2_Tron-1.0-SNAPSHOT.jar Server -h

Usage: DAI_Practical_Work_1_Magic_Image_CLI-1.0-SNAPSHOT.jar Server [-hV]
       [-F=<frequency>] [-M=<multicastAddress>] [-P=<port>] -PN=<numberOfPlayer>
Start the server part of the Tronocol application
  -F, --frequency=<frequency>
                      Frequency of sending the message (in milliseconds)
                        (default: 100).
  -h, --help          Show this help message and exit.
  -M, --multicast-address=<multicastAddress>
                      Multicast address to use (default: 224.0.0.1).
      -PN, --PlayerNumber=<numberOfPlayer>
                      Number of player between 1-4
  -V, --version       Print version information and exit.
```

with the argument <count> being the number of player in the game.

Example of usage for starting a server on default port, default multicast address and default frequency:
```bash
java -jar DAI_Practical_Work_2_Tron-1.0-SNAPSHOT.jar Server -PN=2
[Server] starting
[Server] listening on port 42069
```

After the server is created, it will wait for the specified number of players to be connected to the server. Following the player count reached, it will start emitting the game info on the network using the multicast address to make the players update their displayed world.

### Client
The pepper reduction feature reduces the pepper noise on a gray-scaled image. In order to force the treatment of a gray-scaled image, we apply the `GrayScale` feature first to the input file, no matter if it is gray-scaled or not as a security measure.

```bash
java -jar ./target/DAI_Practical_Work_2_Tron-1.0-SNAPSHOT.jar client -H=<hostname> -C=<color> -U=<username>
```
with the argument <hostname> being the host address, <color> being the desired color (to be chosen from 0 to 3) and <username> being the user's name to be used.

In order to know which colors are possible, you can run the following command:
```bash
java -jar ./target/DAI_Practical_Work_2_Tron-1.0-SNAPSHOT.jar client -h

Usage: DAI_Practical_Work_1_Magic_Image_CLI-1.0-SNAPSHOT.jar client [-hV]
       -C=<color> -H=<host> -I=<interfaceName> [-M=<multicastAddress>]
       [-p=<port>] -U=<username>
Start the client part of the Tronocol application
  -C, --color=<color>   color to use: 0:Red,1:Purple,2:Green,3:Blue
  -h, --help            Show this help message and exit.
  -H, --host=<host>     Host to connect to.
  -I, --interface=<interfaceName>
                        ip address of the interface to use
  -M, --multicast-address=<multicastAddress>
                        Multicast address to use (default: 224.0.0.1).
  -U, --username=<username>
                        Username to use
  -V, --version         Print version information and exit.
```

When the client launches the program, a window will appear on his screen. This window is the board game.

Before the game launches, the window will appear completely black, and when it starts, it will display all the players represented by a colored dot according to the color each player choose (only one player per color).

## Docker
The server can also be run using Docker. In order to build the Docker, you will need to run the following command:
```bash
docker build -t tronocol_server .
```
In order to start the server, you will have to run the following command:
```bash
docker run -p 42069:42069/udp -p 42070:42070/udp --rm tronocol_server
```
This will run the application from docker with the default values for the server.

It is possible to change the values of the docker by modifying values in the [Dockerfile](Dockerfile) in the corresponding emplacements, although not recommended.

Please note that only the server is dockerized. Considering the amount of parameters the client has to give when connecting to the server, and the presence of a graphical user interface, the dockerization of the project for the client is tedious and necessary.
## Dependencies

The main dependencies we have in this project are:
### Game logic and window display made available in java by the uk.co.electronstudio.jaylib package
- in order to process game logic and display, we used the jaylib library which allows us to display the game window to each client.
### UDP transmission made available in java by the java.net package
- we use the java.net package to handle transmission between the server and the clients.



