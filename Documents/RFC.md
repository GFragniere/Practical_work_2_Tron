# The Tronocol Protocol

For the purpose of our studies, we need to create a protocol that will allow us to communicate over the network to implement our second laboratory.

## Section 1 - Overview

The Tronocol protocol is a communication protocol that allows the communication between a server and a client
to play the game Tron.

## Section 2 - Transport protocol

As for the transport protocol used, it uses UDP (User Datagram Protocol) in unicast and multicast mode
to ensure performance in communicating the data from the clients to the server and vice versa.
It uses the port `42069` for unicast communication between the client and the server, and `42070` for
multicast communication from the server to all clients.

Since the information is stored and transmitted as binary data, it does not use any encoding. However, considering the
data is transmitted as Objects considering the different types of signals sent, each message starts with
the number of objects in the message in order for the receiver to know the amount of data received.

Considering the maximum size of a UDP package, the application will send data one package at a time containing
all the data required.

The messages and actions will be treated binary so simplify data transfer between the client and the server.

The client is the one initiating and closing his communication between him and the server.

If an unexpected action occurs, the server will simply ignore the message and send an error to
the client that sent the action.

(Optional) The server broadcasts over the Tronocol Protocol that it provides its service.
(Optional) A client can hear the broadcast and lists all the server present on the same network.

Upon connecting to the server, the client send its username and color over the network.

The server checks if the username and the color are both available. In case it's not, it returns an error and
the client must try to connect again.

Upon having the specified number of player at the start of the server, the server broadcasts
over the network to start the game to everyone.

Each update the client sends the direction he desires to go to the server over the network. The server broadcasts the
world every `frequency` ms and updates the world according to the data received from each player.

When a client quits, it simply stops to send update requests to the server, and the server just treats
it as if the player never changed direction, thus making him run into a wall at some point and killing him.

## Section 3 - Messages

### Join the server

The client sends a message to the server with its username and color.

#### Request

```Tronocol
3 JOIN <username> <color>
```
- `username`: the user's name.
- `color`: the user's color.

#### Response

- `1 OK`: both the username and the color are available and the user is registered.
- `2 ERROR <code>`: The username or the color is not available.
  - `1` : The username is already used.
  - `2` : The color is already used.
  - `3` : The session is full.

### Ready

The server sends a broadcast message to all the client to start the games

#### Request

```Tronocol
1 READY
```

#### Response
None.

### Update from client

The client sends its player data to the server.

#### Request

```Tronocol
2 UPDATE <direction>
```
- `count`: the count of objects sent.
- `direction`: the direction the player desire to go to.

#### Response`

None.

### Update from server

The server sends the world to all clients.

#### Request

```Tronocol
2 UPDATE <world_data>
```
- `world_data`: the data of the world as binary data.

#### Response

None.

### Client quits the server

The client sends a request to tell the server it quits.

#### Request

```Tronocol
1 QUIT 
```

#### Response

None.

(Optional)

### Server info

The server broadcast its information so every client can see the available servers.

#### Request

```Tronocol
6 INFO <ip> <port> <number_of_player> <description> <status>
```
- `ip` : the server's ip.
- `port` : the serve's port.
- `number_of_player` : the number of player that join the session
- `description` : the description of the server
- `status` : the game status

#### Response

None.

## Section 4 - Examples

### Functional example

![Functional](images/Fonctional.png)

### Join errors
![Join Errors](images/Join_With_Error.png)
