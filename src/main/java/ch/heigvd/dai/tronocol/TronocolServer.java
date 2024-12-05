package ch.heigvd.dai.tronocol;

import ch.heigvd.dai.game.*;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;

/**
 * The class that manages the tronocol server for unicast and multicast connection
 */
public class TronocolServer {
    private final String MULTICAST_ADDRESS;
    private final int PORT = 42069;
    private final int MULTICAST_PORT = 42070;
    private final int FREQUENCY;
    private final Tronocol tronocol;
    private static final Semaphore MUTEX  = new Semaphore(1);

    /**
     * The constructor for a TronocolServer instance.
     * @param MULTICAST_ADDRESS The multicast address for the datagramme packet
     * @param frequency The frequency for updating and sending the game
     * @param numberPlayer The number of players
     */
    public TronocolServer(String MULTICAST_ADDRESS, int frequency,int numberPlayer) {
        this.MULTICAST_ADDRESS = MULTICAST_ADDRESS;
        this.FREQUENCY = frequency;
        this.tronocol = new Tronocol(numberPlayer);
    }

    /**
     *The method to start the TronocolServer to launch the ClientsHandler and the MulticastEmitter
     */
    public void start() {
        System.out.println("[Server] starting");
        System.out.println("[Server] listening on port " + PORT);
        Thread clientThread = new Thread(new ClientsHandler());
        clientThread.start();
        Thread multicastThread = new Thread(new MulticastEmitter());
        multicastThread.start();
        try {
            multicastThread.join();
            clientThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("[Server] Server Closing");
    }

    /**
     * The class that manage all the client unicast datagramme
     */
    class ClientsHandler implements Runnable {

        private final DatagramSocket socket;
        private int NbPlayer = 0;

        /**
         * The constructor for a ClientsHandler instance.
         * It will instanciate the socket for the datagram communication using unicast
         */
        public ClientsHandler() {
            try {
                this.socket = new DatagramSocket(PORT);
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * The run method that will be threaded
         * It handle the logic for unicast communicating with the client
         * For Joining the game, Update direction and Quit game
         */
        @Override
        public void run() {
            try{
                while (!socket.isClosed()) {
                    Object[] data = receive();
                    String request = (String) data[2];
                    String response = "";
                    Integer error;

                    switch (request){
                        case "JOIN":
                            System.out.println("[Server] joining");
                            String name = (String) data[3];
                            short color = (short) data[4];
                            System.out.println("[Server] color: " + color);
                            System.out.println("[Server] name: " + name);

                            if (NbPlayer == 0) {
                                tronocol.addPlayer(new Player(color, name, Tronocol.POSITIONS[NbPlayer++], Direction.DOWN));
                            } else if (!tronocol.GameReady()){
                                Player[] players = tronocol.getPlayer();
                                for (int i = 0; i < NbPlayer; ++i) {
                                    if (players[i].getName().contentEquals(name)) {
                                        response = "ERROR";
                                        error = 1;
                                        send((InetAddress)data[0],(int)data[1], response, error);
                                        break;
                                    } else if (players[i].getColor() == color) {
                                        response = "ERROR";
                                        error = 2;
                                        send((InetAddress)data[0],(int)data[1], response, error);
                                        break;
                                    }
                                }
                                if (!response.contentEquals("ERROR")) {
                                    Direction playerDirection = NbPlayer % 3 == 0 ? Direction.DOWN : Direction.UP;
                                    tronocol.addPlayer(new Player(color, name, Tronocol.POSITIONS[NbPlayer++], playerDirection));
                                }
                            } else {
                                response = "ERROR";
                                error = 3;
                                send((InetAddress)data[0],(int)data[1], response, error);
                            }

                            if (!response.contentEquals("ERROR")) {
                                response = "OK";
                                send((InetAddress)data[0],(int)data[1], response);
                            }
                            break;
                        case "UPDATE":
                            Direction direction = Direction.values()[(Integer) data[3]];
                            String playerName = (String) data[4];
                            System.out.println("[Server] Updating " + playerName + " " + direction);
                            for (int i = 0; i < tronocol.getCurrentNumberOfPlayer(); i++) {
                                if (tronocol.getPlayer()[i].getName().contentEquals(playerName)) {
                                    MUTEX.acquire();
                                    tronocol.getPlayer()[i].setDirection(direction);
                                    MUTEX.release();
                                    break;
                                }
                            }
                            break;
                        case "QUIT":
                            if(--NbPlayer == 0){
                                socket.close();
                            }
                            break;
                    }
                }
            } catch (Exception e) {
                System.err.println("[Server] An error occurred in Unicast: " + e.getMessage());
            }
        }

        /**
         * The receive method handle receiving object from the server and returning them as an array of objects
         * @return the object received by the server
         */
        private Object[] receive() {
            try {
                // Create a buffer for the incoming request
                byte[] requestBuffer = new byte[65535];

                // Create a packet for the incoming request
                DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length);

                socket.receive(requestPacket);
                InetAddress address = requestPacket.getAddress();
                int port = requestPacket.getPort();

                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(65535);
                ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteOutputStream));

                ByteArrayInputStream byteInputStream = new ByteArrayInputStream(requestBuffer);
                ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteInputStream));

                Integer count = (Integer) is.readObject();
                count+=2;
                Object[] data = new Object[count];
                data[0] = address;
                data[1] = port;
                for(int i = 2; i < count; i++){
                    data[i] = is.readObject();
                }
                return data;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * The send method is used to send object over the network to the server
         * @param address the address of the client
         * @param port the port of the client
         * @param objects object to send to the server
         */
        private void send(InetAddress address,int port, Object... objects) {
            try(ByteArrayOutputStream byteStreamOut = new ByteArrayOutputStream(10000);
                ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStreamOut));)
            {
                Integer count = Integer.valueOf(objects.length);
                os.writeObject(count);
                for(Object object : objects){
                    os.writeObject(object);
                }
                os.flush();
                byte[] sendBuf = byteStreamOut.toByteArray();
                DatagramPacket sendpacket = new DatagramPacket(sendBuf, sendBuf.length, address, port);
                socket.send(sendpacket);
            } catch (Exception e) {
                System.err.println("[Server] ERROR: " + e);
            }
        }
    }
    /**
     * The class that manage all the MulticastEmitter
     */
    class MulticastEmitter implements Runnable {

        public MulticastEmitter() {
        }

        /**
         * This method is called when the thread starts. It is used to send the game's data periodically to all client
         * in order for them to update their game according to what has changed. The method runs until the game either
         * has all players dead or if one player is still alive whilst all others are dead. When the game
         */
        @Override
        public void run() {
                try (DatagramSocket  socket = new DatagramSocket()) {
                    InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
                    while(!socket.isClosed()) {
                        if(tronocol.GameReady()) {
                            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream(10000);
                            ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteOutStream));
                            MUTEX.acquire();
                            os.flush();
                            os.writeObject(tronocol);
                            os.flush();
                            byte[] sendBuf = byteOutStream.toByteArray();
                            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, multicastAddress,42070);
                            socket.send(packet);
                            MUTEX.release();
                            if (tronocol.allPlayersDead() || tronocol.onePlayerWinner()){
                                break;
                            }
                            tronocol.update();
                            Thread.sleep(FREQUENCY);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[Server] [Multicast] An error occurred in Multicast: " + e.getMessage());
                }
        }
    }
}
