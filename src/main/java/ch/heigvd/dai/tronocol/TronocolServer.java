package ch.heigvd.dai.tronocol;

import ch.heigvd.dai.game.Player;
import ch.heigvd.dai.game.Tronocol;
import ch.heigvd.dai.game.TronocolGraphics;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class TronocolServer {
    private final String MULTICAST_ADDRESS;
    private final int PORT;
    private final int frequency;
    private final Tronocol game = new Tronocol(4,
            TronocolGraphics.HEIGHT / TronocolGraphics.BLOCKSIZE,
            TronocolGraphics.WIDTH / TronocolGraphics.BLOCKSIZE);

    public TronocolServer(String MULTICAST_ADRESS, int PORT, int frequency) {
        this.MULTICAST_ADDRESS = MULTICAST_ADRESS;
        this.PORT = PORT;
        this.frequency = frequency;
    }


    public void start() {
        System.out.println("[Server] starting");
        System.out.println("[Server] listening on port " + PORT);
        Thread clientThread = new Thread(new ClientsHandler(PORT));
        clientThread.start();

        Thread multicastThread = new Thread(new MulticastEmitter(MULTICAST_ADDRESS, frequency));
        multicastThread.start();
    }

    class ClientsHandler implements Runnable {

        private final int PORT;

        public ClientsHandler(int PORT) {
            this.PORT = PORT;
        }

        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket(PORT)) {
                while (!socket.isClosed()) {
                    // Create a buffer for the incoming request
                    byte[] requestBuffer = new byte[10000];

                    // Create a packet for the incoming request
                    DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length);

                    // Receive the packet - this is a blocking call
                    socket.receive(requestPacket);

                    // Sender address
                    InetAddress address = InetAddress.getByName(requestPacket.getAddress().getHostAddress());

                    ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(10000);
                    ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteOutputStream));

                    ByteArrayInputStream byteInputStream = new
                            ByteArrayInputStream(requestBuffer);
                    ObjectInputStream is = new
                            ObjectInputStream(new BufferedInputStream(byteInputStream));
                    String request = (String) is.readObject();

                    String response = "";
                    Integer error;

                    byte[] responseBuffer = request.getBytes(StandardCharsets.UTF_8);

                    switch (request){
                        case "JOIN":
                            System.out.println("[Server] joining");
                            String name = (String) is.readObject();
                            String color = (String) is.readObject();
                            for (Player player : game.getPlayer()) {
                                if (player.getName().equals(name)) {
                                    response = "ERROR";
                                    error = 1;
                                    os.writeObject(response);
                                    os.writeObject(error);
                                    break;
                                } else if (player.getColor().equals(color)) {
                                    response = "ERROR";
                                    error = 2;
                                    os.writeObject(response);
                                    os.writeObject(error);
                                    break;
                                }
                            }
                            if (!response.contentEquals("ERROR")) {
                                response = "OK";
                                // game.addPlayer(new Player(name, color,));
                                os.writeObject(response);
                            }
                            os.flush();
                            break;

                        case "UPDATE":
                            break;

                        default:
                            break;
                    }

                    is.close();

                    byte[] sendBuf = byteOutputStream.toByteArray();
                    DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, requestPacket.getPort());
                    socket.send(packet);
                    os.close();

                }
            } catch (Exception e) {
                System.err.println("[Server] An error occurred: " + e.getMessage());
            }
        }
    }

    class MulticastEmitter implements Runnable {
        private final String MULTICAST_ADDRESS;
        private final int FREQUENCY;
        // private final Data data;

        public MulticastEmitter(String MULTICAST_ADDRESS, int FREQUENCY /* Data data */) {
            this.MULTICAST_ADDRESS = MULTICAST_ADDRESS;
            this.FREQUENCY = FREQUENCY;
            /* this.data = data; */
        }

        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket()) {
                // Get the multicast address
                InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);

                // Creation of the data to be transmitted
                String example = "Hi, I am an example";

                // Transform the message into a byte array
                byte[] buffer = example.getBytes(StandardCharsets.UTF_8);
                //byte[] buffer = data.toBytes();

                // Create a packet with the message, the multicast address and the port
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastAddress, PORT);

                // Send the packet
                socket.send(packet);

                // Wait for the next message
                Thread.sleep(FREQUENCY);
            } catch (Exception e) {
                System.err.println("[Server] An error occurred: " + e.getMessage());
            }
        }
    }
}
