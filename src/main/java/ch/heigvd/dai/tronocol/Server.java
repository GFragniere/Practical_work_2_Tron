package calculator.operator.tronocol;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class Server {
    private final String MULTICAST_ADDRESS;
    private final int PORT;
    private final int frequency;

    public Server(String MULTICAST_ADRESS, int PORT, int frequency) {
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
                    byte[] requestBuffer = new byte[1024];

                    // Create a packet for the incoming request
                    DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length);

                    // Receive the packet - this is a blocking call
                    socket.receive(requestPacket);

                    // Treat the received data here


                    // Prepare the response
                    String response = "Hello, client! I'm the server. 👻";

                    // Transform the message into a byte array - always specify the encoding
                    byte[] responseBuffer = response.getBytes(StandardCharsets.UTF_8);

                    // Create a packet with the message, the client address and the client port
                    DatagramPacket responsePacket =
                            new DatagramPacket(
                                    responseBuffer,
                                    responseBuffer.length,
                                    requestPacket.getAddress(),
                                    requestPacket.getPort());

                    // Send the packet
                    socket.send(responsePacket);

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
