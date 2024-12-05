package ch.heigvd.dai.commands;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import ch.heigvd.dai.tronocol.*;

@CommandLine.Command(
        name = "client",
        description = "Start the client part of the Tronocol application"
)

public class Client implements Callable<Integer>{

    @CommandLine.Option(
            names = {"-H", "--host"},
            description = "Host to connect to.",
            required = true)
    protected String host;

    @CommandLine.Option(
            names = {"-U", "--username"},
            description = "Username to use",
            required = true)
    protected String username;

    @CommandLine.Option(
            names = {"-C", "--color"},
            description = "color to use: 0:Red,1:Purple,2:Green,3:Blue",
            required = true)
    protected short color;

    @CommandLine.Option(
            names = {"-I", "--interface"},
            description = "ip address of the interface to use",
            required = true)
    protected String interfaceName;

    @CommandLine.Option(
            names = {"-M", "--multicast-address"},
            description = "Multicast address to use (default: ${DEFAULT-VALUE}).",
            defaultValue = "239.0.0.0")
    protected String multicastAddress;

    @Override
    public Integer call() {
        TronocolClient tronocolClient = new TronocolClient(multicastAddress,host,interfaceName,username,color);
        tronocolClient.start();
        return 0;
    }
}