package ch.heigvd.dai.commands;

import picocli.CommandLine;

@CommandLine.Command(
        description = "Tronocol game made with protocol UDP.",
        version = "1.0.0",
        subcommands = {Server.class, Client.class},
        scope = CommandLine.ScopeType.INHERIT,
        mixinStandardHelpOptions = true)
public class Root {}