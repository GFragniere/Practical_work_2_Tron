package ch.heigvd.dai.tronocol;

import ch.heigvd.dai.game.Direction;
import ch.heigvd.dai.game.Player;
import ch.heigvd.dai.game.Tronocol;
import ch.heigvd.dai.game.TronocolGraphics;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TronocolServer {
    private final String MULTICAST_ADDRESS;
    private final int PORT;
    private final int frequency;
    private final Tronocol tronocol = new Tronocol(1,
            TronocolGraphics.HEIGHT / TronocolGraphics.BLOCKSIZE,
            TronocolGraphics.WIDTH / TronocolGraphics.BLOCKSIZE);
    private boolean running = true;


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
        while (running) {}
    }

    class ClientsHandler implements Runnable {

        private final int PORT;
        private final DatagramSocket socket;

        public ClientsHandler(int PORT) {
            this.PORT = PORT;
            try {
                this.socket = new DatagramSocket(PORT);
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            try{
                while (!socket.isClosed()) {
                    Object[] data = receive();
                    String request = (String) data[2];
                    String response = "";
                    Integer error;
                    Integer count;

                    switch (request){
                        case "JOIN":
                            System.out.println("[Server] joining");
                            String name = (String) data[3];
                            Color color = (Color) data[4];
                            System.out.println("[Server] color: " + color);
                            System.out.println("[Server] name: " + name);

                            int currentPlayer = tronocol.getCurrentNumberOfPlayer();
                            if (currentPlayer == 0) {
                                tronocol.addPlayer(new Player(color, name, Tronocol.POSITIONS[currentPlayer], Direction.DOWN));
                            } else if (!tronocol.GameReady()){
                                Player[] players = tronocol.getPlayer();
                                for (int i = 0; i < currentPlayer; ++i) {
                                    if (players[i].getName().contentEquals(name)) {
                                        response = "ERROR";
                                        error = 1;
                                        count = Integer.valueOf(2);
                                        send((InetAddress)data[0],(int)data[1], response, error);
                                        break;
                                    } else if (players[i].getColor().equals(color)) {
                                        response = "ERROR";
                                        error = 2;
                                        count = Integer.valueOf(2);

                                        send((InetAddress)data[0],(int)data[1],count, response, error);
                                        break;
                                    }
                                }
                                if (!response.contentEquals("ERROR"))
                                    tronocol.addPlayer(new Player(color, name, Tronocol.POSITIONS[currentPlayer], Direction.DOWN));
                            } else {
                                response = "ERROR";
                                error = 3;
                                count = Integer.valueOf(2);
                                send((InetAddress)data[0],(int)data[1], count, response, error);
                            }

                            if (!response.contentEquals("ERROR")) {
                                response = "OK";
                                count = Integer.valueOf(1);
                                send((InetAddress)data[0],(int)data[1], count, response);
                            }
                            break;
                        case "UPDATE":
                            Direction direction = (Direction) data[3];
                            String playerName = (String) data[4];
                            System.out.println("[Server] updating " + playerName + " " + direction);
                            for (int i = 0; i < tronocol.getCurrentNumberOfPlayer(); i++) {
                                if (tronocol.getPlayer()[i].getName().contentEquals(playerName)) {
                                    tronocol.getPlayer()[i].setDirection(direction);
                                    break;
                                }
                            }
                            break;
                    }
                }
            } catch (Exception e) {
                System.err.println("[Server] An error occurred in Unicast: " + e.getMessage());
            }
            running = false;
        }
        private Object[] receive() {
            try {
                // Create a buffer for the incoming request
                byte[] requestBuffer = new byte[10000];

                // Create a packet for the incoming request
                DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length);

                socket.receive(requestPacket);
                InetAddress address = requestPacket.getAddress();
                int port = requestPacket.getPort();

                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(10000);
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

        private void send(InetAddress address,int port, Object... objects) {
            try(ByteArrayOutputStream byteStreamOut = new ByteArrayOutputStream(10000);
                ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStreamOut));)
            {
                for(Object object : objects){
                    os.writeObject(object);
                }
                os.flush();
                byte[] sendBuf = byteStreamOut.toByteArray();
                DatagramPacket sendpacket = new DatagramPacket(sendBuf, sendBuf.length, address, port);
                socket.send(sendpacket);
            } catch (Exception e) {
                System.err.println("[Client] ERROR: " + e);
            }
        }
    }

    class MulticastEmitter implements Runnable {
        private final String MULTICAST_ADDRESS;
        private final int frequency;

        public MulticastEmitter(String MULTICAST_ADDRESS, int FREQUENCY) {
            this.MULTICAST_ADDRESS = MULTICAST_ADDRESS;
            this.frequency = FREQUENCY;
        }

        @Override
        public void run() {
                try (MulticastSocket socket = new MulticastSocket();
                     ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream(10000);
                     ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteOutStream));) {
                    InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
                    while(true) {
                        System.out.println("[Server] not ready");
                        if(tronocol.GameReady()) {
                            System.out.println("[Server] ready");

                            os.writeObject(tronocol);
                            os.flush();

                            byte[] sendBuf = byteOutStream.toByteArray();

                            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, multicastAddress, 42070);
                            socket.send(packet);
                            Thread.sleep(frequency);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[Server] An error occurred in Multicast: " + e.getMessage());
                }

        }
    }
}
