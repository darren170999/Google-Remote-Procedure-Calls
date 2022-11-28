package greeting.server;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
//this package is where our server will sit
public class GreetingServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50051;
//Here we register the service instance of greetingService
        Server server = ServerBuilder
                .forPort(port)
                .addService(new GreetingServerImpl()) // this is how to instantiate a service
                .build();

        server.start();
//Let's add some log messages to have feedback from our code.
        System.out.println("server started");
        System.out.println("Listening on port: " + port);
//Whatever is defined ion this hook will happen when the program is killed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received shutdown req");
            server.shutdown();
            System.out.println("Server stopped");
        }));

        server.awaitTermination();
    }
}
