package ch.heigvd.dai.commands;
import java.util.concurrent.Callable;
import picocli.CommandLine;

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
            names = {"-M", "--multicast-address"},
            description = "Multicast address to use (default: ${DEFAULT-VALUE}).",
            defaultValue = "230.1.2.3")
    protected String multicastAddress;

    @CommandLine.Option(
            names = {"-p", "--port"},
            description = "Port to use (default: ${DEFAULT-VALUE}).",
            defaultValue = "42069")
    protected int port;

    @Override
    public Integer call() {
        throw new UnsupportedOperationException(
                "Please remove this exception and implement this method.");
    }
}