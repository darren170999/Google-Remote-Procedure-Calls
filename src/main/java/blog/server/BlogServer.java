package blog.server;

import blog.server.BlogServiceImpl;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class BlogServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50051;
//Recap this is how to connect MongoDb to server from here.
//Change mongodb to localhost as we bound db port to localhost
        MongoClient client = MongoClients.create("mongodb://root:root@localhost:27017/");

        Server server = ServerBuilder
                .forPort(port)
                .addService(new BlogServiceImpl(client)) // we need client to do CRUD
                .build();

        server.start();
        System.out.println("server started");
        System.out.println("Listening on port: " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received shutdown req");
            server.shutdown();
            client.close();
            System.out.println("Server stopped");
        }));
        server.awaitTermination();
    }
}
