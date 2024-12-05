package ch.heigvd.dai.commands;
import java.net.InetAddress;
import java.util.concurrent.Callable;

import ch.heigvd.dai.tronocol.TronocolServer;
import picocli.CommandLine;

@CommandLine.Command(
        name = "Server",
        description =
                "Start the server part of the Tronocol application "
)

public class Server implements  Callable<Integer>{

    @CommandLine.Option(
            names = {"-M", "--multicast-address"},
            description = "Multicast address to use (default: ${DEFAULT-VALUE}).",
            defaultValue = "239.0.0.0")
    protected String multicastAddress;

    @CommandLine.Option(
            names = {"-F", "--frequency"},
            description =
                    "Frequency of sending the message (in milliseconds) (default: ${DEFAULT-VALUE}).",
            defaultValue = "100")
    protected int frequency;

    @CommandLine.Option(
            names = {"-PN", "--PlayerNumber"},
            description = "Number of player between 2-4 (default: ${DEFAULT-VALUE})",
            defaultValue = "2"
    )
    protected int numberOfPlayer;

    @Override
    public Integer call() {
        TronocolServer server = new TronocolServer(multicastAddress, frequency,numberOfPlayer);
        server.start();
        return 0;
    }
}




