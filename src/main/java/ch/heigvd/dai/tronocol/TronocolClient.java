package ch.heigvd.dai.tronocol;

import ch.heigvd.dai.game.Tronocol;
import ch.heigvd.dai.game.TronocolGraphics;

import java.net.*;
import java.io.*;

/**
 * The class that manages the tronocol client for unicast and multicast connection
 */
public class TronocolClient {
    private final int PORT = 42069;
    private final int MULTICAST_PORT = 42070;
    private final String MULTICAST_ADDRESS;
    private final String HOST;
    private final String NETWORK_INTERFACE;
    private final String USERNAME;
    private final short COLOR;
    protected Tronocol tronocol;
    private final TronocolGraphics tronocolGraphics;
    private UnicastTransmission unicastTransmission;


    /**
     * The constructor for a TronocolClient instance.
     * @param MULTICAST_ADDRESS The multicast address for the group to join by the socket
     * @param NETWORK_INTERFACE The network interface used to listen for multicast data
     * @param USERNAME The Username of the player
     * @param COLORINDEX The Color choosen by the player as an index
     */
    public TronocolClient(String MULTICAST_ADDRESS, String HOST, String NETWORK_INTERFACE, String USERNAME, short COLORINDEX) {
        this.MULTICAST_ADDRESS = MULTICAST_ADDRESS;
        this.HOST = HOST;
        this.NETWORK_INTERFACE = NETWORK_INTERFACE;
        this.USERNAME = USERNAME;
        this.COLOR = COLORINDEX;
        this.tronocol = new Tronocol(2);
        this.tronocolGraphics = new TronocolGraphics(tronocol,this);
    }

    /**
     *The method to start the TronocolClient to launch the unicast transmitter and the multicast transmitter
     *It also starts the tronocol graphical instance
     */
    public void start() {
        System.out.println("[Client] starting");
        System.out.println("[Client] transmitting to server via port " + PORT);
        this.unicastTransmission = new UnicastTransmission();
        Thread unicastThread = new Thread(unicastTransmission);
        unicastThread.start();
        Thread multicastThread = new Thread(new MulticastTransmission());
        multicastThread.start();
        tronocolGraphics.run();
    }

    /**
     * The method used to send update from the graphical part to the socket to transmit
     * the updates of movement
     * @param objects Object transmitted to write into the datagram
     */
    public void send_update(Object ... objects){
        unicastTransmission.send(objects);
    }

    /**
     * The method used to get the username int the graphical part
     * @return the username of the player
     */
    public String getUsername(){
        return USERNAME;
    }

    /**
     * The class that manage the unicast transmission of the client
     */
    class UnicastTransmission implements Runnable {
        private final DatagramSocket socket;

        /**
         * The constructor for a UnicastTransmission instance.
         * It will instanciate the socket for the datagram communication using unicast
         */
        public UnicastTransmission(){
            try{
                this.socket = new DatagramSocket();
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * The run method that will be threaded
         * It handle the logic for unicast communicating with the server
         * For Joining the game
         */
        @Override
        public void run() {
                String command = "JOIN";
                send(command,USERNAME,COLOR);
                //receive datagram from server
                Object[] data = receive();
                switch((String) data[0]){
                    case "OK":
                        System.out.println("[Client] OK");
                        break;
                    case "ERROR":
                        System.out.println("[Client] ERROR:" + (Integer)data[1] + "\n");
                        switch ((Integer)data[1]) {
                            case 1:
                                System.out.println("[Client] username already taken");
                                break;
                            case 2:
                                System.out.println("[Client] color already taken");
                                break;
                            case 3:
                                System.out.println("[Client] party full");
                        }
                        socket.close();
                        tronocolGraphics.exit();
                    }
        }

        /**
         * The send method is used to send object over the network to the server
         * @param objects object to send to the server
         */
        private void send(Object... objects){
            try(
                ByteArrayOutputStream byteStreamOut = new ByteArrayOutputStream(1000);
                ObjectOutputStream objectOut = new ObjectOutputStream(new BufferedOutputStream(byteStreamOut));
                )
            {

                InetAddress address = InetAddress.getByName(HOST);
                Integer count = Integer.valueOf(objects.length);
                objectOut.writeObject(count);
                objectOut.close();
                for(Object object : objects){
                    objectOut.writeObject(object);
                }
                objectOut.flush();
                byte[] sendBuf = byteStreamOut.toByteArray();
                DatagramPacket sendpacket = new DatagramPacket(sendBuf, sendBuf.length, address, PORT);
                socket.send(sendpacket);
            } catch (Exception e) {
                System.err.println("[Client] ERROR: SENDING " + e);
            }
        }

        /**
         * The receive method handle receiving object from the server and returning them as an array of objects
         * @return the object received by the server
         */
        private Object[] receive(){
            try{
                byte[] requestBuffer = new byte[10000];
                DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length);
                socket.receive(requestPacket);
                ByteArrayInputStream byteStreamIn = new ByteArrayInputStream(requestBuffer);
                ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStreamIn));
                Integer numberObject = (Integer) is.readObject();
                Object[] data = new Object[numberObject];
                for(int i = 0; i < numberObject; i++){
                    data[i] = is.readObject();
                }
                return data;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * The class that manage the multicast transmission of the client
     */
    class MulticastTransmission implements Runnable {
        byte[] requestBuffer = new byte[65535];

        /**
         * The constructor for a MulticastTransmission instance.
         */
        public MulticastTransmission() {}

        /**
         * The run method handle receiving the game and setting it in the graphical part
         */
        @Override
        public void run() {
            try (MulticastSocket socket = new MulticastSocket(MULTICAST_PORT)) {
                // Join the multicast group
                InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
                NetworkInterface networkInterface = NetworkInterface.getByName(NETWORK_INTERFACE);
                InetSocketAddress multicastGroup = new InetSocketAddress(multicastAddress, MULTICAST_PORT);
                socket.joinGroup(multicastGroup,networkInterface);
                while (true) {
                    DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length);

                    socket.receive(requestPacket);
                    ByteArrayInputStream byteStreamIn = new ByteArrayInputStream(requestBuffer);
                    ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStreamIn));

                    // Treat the data from the listened multicast here
                    Tronocol trono = (Tronocol) is.readObject(); // crashes sometimes here
                    tronocolGraphics.setGame(trono);
                    is.close();
                    byteStreamIn.close();
                    if(trono.onePlayerWinner()){
                        break;
                    }
                }
                // Quit the multicast group
                socket.leaveGroup(multicastGroup,networkInterface);
                socket.close();
            } catch (Exception e) {
                System.err.println("[Client] [Multicast] An error occurred: " + e.getMessage());
            }
        }
    }
}
