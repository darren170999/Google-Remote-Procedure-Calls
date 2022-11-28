package greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.File;
import java.io.IOException;

//Setting SSL for server
public class GreetingServerTls {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50051;
        Server server = ServerBuilder
                .forPort(port)
                .useTransportSecurity(
                        new File("ssl/server.crt"),
                        new File("ssl/server.pem")

                )
                .addService(new GreetingServerImpl())
                .build();
        server.start();
        System.out.println("server started");
        System.out.println("Listening on port: " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received shutdown req");
            server.shutdown();
            System.out.println("Server stopped");
        }));

        server.awaitTermination();
    }
}
