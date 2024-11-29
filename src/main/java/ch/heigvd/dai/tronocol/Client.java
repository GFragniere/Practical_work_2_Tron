package ch.heigvd.dai.tronocol;

import ch.heigvd.dai.game.Tronocol;
import ch.heigvd.dai.game.TronocolGraphics;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.raylib.Jaylib.Color;

public class Client {
    private final int PORT;
    private final String MULTICAST_ADDRESS;
    private final String HOST;
    private final String NETWORK_INTERFACE;
    private Tronocol tronocol;
    private final TronocolGraphics tronocolGraphics;

    public Client(int PORT, String MULTICAST_ADDRESS, String HOST, String NETWORK_INTERFACE) {
        this.PORT = PORT;
        this.MULTICAST_ADDRESS = MULTICAST_ADDRESS;
        this.HOST = HOST;
        this.NETWORK_INTERFACE = NETWORK_INTERFACE;
        this.tronocol = new Tronocol(1, TronocolGraphics.HEIGHT/TronocolGraphics.BLOCKSIZE,TronocolGraphics.WIDTH/TronocolGraphics.BLOCKSIZE);
        this.tronocolGraphics = new TronocolGraphics(tronocol);
    }

    public void start() {
        System.out.println("[Client] starting");
        System.out.println("[Client] transmitting to server via port " + PORT);
        Thread unicastThread = new Thread(new UnicastTransmission(PORT, HOST));
        unicastThread.start();

        Thread multicastThread = new Thread(new MulticastTransmission(MULTICAST_ADDRESS, PORT, NETWORK_INTERFACE));
        multicastThread.start();
    }

    class UnicastTransmission implements Runnable {
        private final int PORT;
        private final String HOST;

        public UnicastTransmission(int PORT, String HOST) {
            this.PORT = PORT;
            this.HOST = HOST;
        }

        @Override
        public void run() {
            try(DatagramSocket socket = new DatagramSocket()){
                String command = "JOIN";
                String username = "Habarosk";
                Color color = new Color(255,0,0,255);
                try
                {
                    InetAddress address = InetAddress.getByName(HOST);
                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream(10000);
                    ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                    os.flush();
                    os.writeObject(command);
                    os.writeObject(username);
                    os.writeObject(color);
                    os.flush();
                    //retrieves byte array
                    byte[] sendBuf = byteStream.toByteArray();
                    DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, PORT);
                    socket.send(packet);
                    os.close();
                }
                catch (UnknownHostException e)
                {
                    System.err.println("Exception:  " + e);
                    e.printStackTrace();
                }
                /*
                InetAddress serverAddress = InetAddress.getByName(HOST);

                // Example message in order to have an errorless file
                String MESSAGE = "2";

                // Transform the message into a byte array - always specify the encoding
                byte[] buffer = MESSAGE.getBytes(StandardCharsets.UTF_8);

                // Create a packet with the message, the server address and the port
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, PORT);

                // Send the packet
                socket.send(packet);

                byte[] responseBuffer = new byte[1024];

                // Create a packet for the incoming response
                DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);

                // Receive the packet - this is a blocking call
                socket.receive(responsePacket);

                // Treat the data corresponding to the received response
                */

            } catch (Exception e) {
                System.err.println("[Client] An error occurred: " + e.getMessage());
            }
        }
    }

    class MulticastTransmission implements Runnable {
        private final String MULTICAST_ADDRESS;
        private final int PORT;
        private final String NETWORK_INTERFACE;

        public MulticastTransmission(String MULTICAST_ADDRESS, int PORT, String NETWORK_INTERFACE) {
            this.MULTICAST_ADDRESS = MULTICAST_ADDRESS;
            this.PORT = PORT;
            this.NETWORK_INTERFACE = NETWORK_INTERFACE;
        }

        @Override
        public void run() {
            try (MulticastSocket socket = new MulticastSocket(PORT)) {
                // Join the multicast group
                InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
                InetSocketAddress multicastGroup = new InetSocketAddress(multicastAddress, PORT);
                NetworkInterface networkInterface = NetworkInterface.getByName(NETWORK_INTERFACE);
                socket.joinGroup(multicastGroup, networkInterface);

                while (!socket.isClosed()) {
                    // Create a buffer for the incoming message
                    byte[] buffer = new byte[1024];

                    // Create a packet for the incoming message
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    // Receive the packet - this is a blocking call
                    socket.receive(packet);

                    // Treat the data from the listened multicast here
                }

                // Quit the multicast group
                socket.leaveGroup(multicastGroup, networkInterface);

            } catch (Exception e) {
                System.err.println("[Client] An error occurred: " + e.getMessage());
            }
        }
    }
}
