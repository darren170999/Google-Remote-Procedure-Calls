package calculator.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

public class CalculatorServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50052;
        Server server = ServerBuilder
                .forPort(port)
                .addService(new CalculatorServiceImpl())
                .addService(ProtoReflectionService.newInstance())//allows us to tell which messages and services it has.
                .build();
        server.start();
        System.out.println("Server has started and is listening on port: " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("Received shutdown req");
            server.shutdown();
            System.out.println("Server stopped");
        }));
        server.awaitTermination();
    }
}
