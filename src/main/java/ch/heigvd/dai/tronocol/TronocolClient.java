package ch.heigvd.dai.tronocol;

import ch.heigvd.dai.game.Tronocol;
import ch.heigvd.dai.game.TronocolGraphics;

import java.awt.*;
import java.net.*;
import java.io.*;

public class TronocolClient {
    private final int PORT;
    private final String MULTICAST_ADDRESS;
    private final String HOST;
    private final String NETWORK_INTERFACE;
    private Tronocol tronocol;
    private final TronocolGraphics tronocolGraphics;

    public TronocolClient(int PORT, String MULTICAST_ADDRESS, String HOST, String NETWORK_INTERFACE) {
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
        while(true);
        //Thread multicastThread = new Thread(new MulticastTransmission(MULTICAST_ADDRESS, PORT, NETWORK_INTERFACE));
        //multicastThread.start();
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
            try(DatagramSocket socket = new DatagramSocket();){
                String command = "JOIN";
                String username = "Habarosk";
                Color color = new Color(255,0,0,255);
                try
                {
                    //Create all the necessary buffered output/input
                    //output
                    ByteArrayOutputStream byteStreamOut = new ByteArrayOutputStream(10000);
                    ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStreamOut));

                    InetAddress address = InetAddress.getByName(HOST);

                    //write object to send
                    os.writeObject(command);
                    os.writeObject(username);
                    os.writeObject(color);
                    os.flush();

                    //send datagram to server
                    byte[] sendBuf = byteStreamOut.toByteArray();
                    DatagramPacket sendpacket = new DatagramPacket(sendBuf, sendBuf.length, address, PORT);
                    socket.send(sendpacket);

                    //receive datagram from server
                    byte[] requestBuffer = new byte[10000];
                    DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length);

                    socket.receive(requestPacket);

                    ByteArrayInputStream byteStreamIn = new ByteArrayInputStream(requestBuffer);
                    ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStreamIn));

                    String response = (String) is.readObject();

                    switch(response){
                        case "OK":
                            System.out.println("OK");
                            break;
                        case "ERROR":
                            Integer code = (Integer) is.readObject();
                            System.out.println("ERROR: " + code);
                    }
                }
                catch (UnknownHostException e)
                {
                    System.err.println("Exception:  " + e);
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.err.println("[Client] An error occurred: " + e.getMessage());
            }
        }
    }
    /*
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
    }*/
}
