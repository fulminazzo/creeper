package it.fulminazzo.creeper.runner;

import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * Main entrypoint of the server runner.
 */
@Log
public final class ServerRunner {

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tT %4$-7s] %5$s%6$s%n");
    }

    public static void main(final @NotNull String @NotNull [] args) {
        if (args.length < 2) {
            log.severe("Not enough arguments. Usage: ServerRunner <port> <command> <command_arguments...>");
            return;
        }
        final String rawPort = args[0];
        final int port;
        try {
            port = Integer.parseInt(rawPort);
            if (port < 1 || port > 65535)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            log.severe(String.format("Invalid port '%s'. Expected a number between 1 and 65535", rawPort));
            return;
        }
        ProcessHandler handler = null;
        TcpServer server = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(Arrays.copyOfRange(args, 1, args.length));
            log.info("Starting process: " + builder.command());
            handler = ProcessHandler.of(builder);
            log.info(String.format("Starting server on port %d", port));
            server = TcpServer.of(handler, handler, port);
            server.start();
            handler.await();
            log.info("Process terminated, goodbye.");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error during execution", e);
        } finally {
            if (handler != null) handler.stop();
            if (server != null) server.close();
        }
    }

}
