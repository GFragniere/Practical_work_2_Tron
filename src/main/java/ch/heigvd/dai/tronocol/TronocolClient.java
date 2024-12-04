package ch.heigvd.dai.tronocol;

import ch.heigvd.dai.game.Player;
import ch.heigvd.dai.game.Tronocol;
import ch.heigvd.dai.game.TronocolGraphics;

import java.awt.*;
import java.net.*;
import java.io.*;

public class TronocolClient {

    private static final Color[] COLORS = {
            new Color(255,0,0,255), new Color(255,0,255,255),
            new Color(0,255,0,255), new Color(0,0,255,255)};

    private final int PORT;
    private final String MULTICAST_ADDRESS;
    private final String HOST;
    private final String NETWORK_INTERFACE;
    private final String USERNAME;
    private final Color COLOR;
    private Tronocol tronocol;
    private final TronocolGraphics tronocolGraphics;
    private UnicastTransmission unicastTransmission;

    public TronocolClient(int PORT, String MULTICAST_ADDRESS, String HOST, String NETWORK_INTERFACE, String USERNAME, int COLORINDEX) {
        this.PORT = PORT;
        this.MULTICAST_ADDRESS = MULTICAST_ADDRESS;
        this.HOST = HOST;
        this.NETWORK_INTERFACE = NETWORK_INTERFACE;
        this.USERNAME = USERNAME;
        this.COLOR = COLORS[COLORINDEX];
        this.tronocol = new Tronocol(1, TronocolGraphics.HEIGHT/TronocolGraphics.BLOCKSIZE,TronocolGraphics.WIDTH/TronocolGraphics.BLOCKSIZE);
        this.tronocolGraphics = new TronocolGraphics(tronocol,this);
    }

    public void start() {
        System.out.println("[Client] starting");
        System.out.println("[Client] transmitting to server via port " + PORT);
        this.unicastTransmission = new UnicastTransmission(PORT, HOST);
        Thread unicastThread = new Thread(unicastTransmission);
        unicastThread.start();
        Thread multicastThread = new Thread(new MulticastTransmission(MULTICAST_ADDRESS, PORT, NETWORK_INTERFACE));
        multicastThread.start();
        tronocolGraphics.run();
    }

    public void send_update(Object ... objects){
        unicastTransmission.send(objects,USERNAME);
    }

    class UnicastTransmission implements Runnable {
        private final int PORT;
        private final String HOST;
        private final DatagramSocket socket;

        public UnicastTransmission(int PORT, String HOST){
            this.PORT = PORT;
            this.HOST = HOST;
            try{
                this.socket = new DatagramSocket();
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }

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
                        }
                        socket.close();
                        tronocolGraphics.exit();
                    }
        }
        private void send(Object... objects){
            try(ByteArrayOutputStream byteStreamOut = new ByteArrayOutputStream(10000);
                ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStreamOut));)
            {
                InetAddress address = InetAddress.getByName(HOST);
                Integer count = Integer.valueOf(objects.length);
                os.writeObject(count);
                for(Object object : objects){
                    os.writeObject(object);
                }
                os.flush();
                byte[] sendBuf = byteStreamOut.toByteArray();
                DatagramPacket sendpacket = new DatagramPacket(sendBuf, sendBuf.length, address, PORT);
                socket.send(sendpacket);
            } catch (Exception e) {
                System.err.println("[Client] ERROR: SENDING " + e);
            }
        }

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
            try (MulticastSocket socket = new MulticastSocket(42070)) {
                // Join the multicast group
                InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
                InetSocketAddress multicastGroup = new InetSocketAddress(multicastAddress, 42070);
                NetworkInterface networkInterface = NetworkInterface.getByName(NETWORK_INTERFACE);
                socket.joinGroup(multicastGroup, networkInterface);

                while (!socket.isClosed()) {
                    byte[] requestBuffer = new byte[10000];
                    DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length);
                    socket.receive(requestPacket);
                    ByteArrayInputStream byteStreamIn = new ByteArrayInputStream(requestBuffer);
                    ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStreamIn));

                    // Receive the packet - this is a blocking call
                    socket.receive(requestPacket);

                    // Treat the data from the listened multicast here
                    tronocol = (Tronocol) is.readObject();
                    System.out.println(tronocol);
                }
                // Quit the multicast group
                socket.leaveGroup(multicastGroup, networkInterface);

            } catch (Exception e) {
                System.err.println("[Client] [Multicast] An error occurred: " + e.getMessage());
            }
        }
    }
}
