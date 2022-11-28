package greeting.client;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    private static void doGreet(ManagedChannel channel) {
        System.out.println("Enter doGreet");
//Here we make a STUB! An object that can do dots and name of the function
//it feels like calling a function directly on server.
//In future use future stub for async functions
        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);
//This sure feels like a function call but its actually matching client call "greet" to server call "greet"
        GreetingResponse response = stub.greet(GreetingRequest.newBuilder().setFirstName("Darren").build());
        System.out.println("Greeting: " + response.getResult());
    }
    private static void doGreetManyTimes(ManagedChannel channel) {
        System.out.println("Enter doGreetManyTimes");
        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);
//So inside for each remaining, it takes a consumer which means it takes a lambda
        stub.greetManyTimes(GreetingRequest.newBuilder().setFirstName("Darren").build()).forEachRemaining(
                response -> {
                    System.out.println(response.getResult());
                }
        );
    }
//A streaming Client will require an async setup.
    private static void doLongGreet(ManagedChannel channel) throws InterruptedException {
        System.out.println("Enter doLongGreet");
//This is how to make an async stub
        GreetingServiceGrpc.GreetingServiceStub stub = GreetingServiceGrpc.newStub(channel);
        List<String> names = new ArrayList<>();
//Takes a value 1, since we are an async, the server response can come anytime. Use latch to wait for it.
        CountDownLatch latch = new CountDownLatch(1);
//Next we add all to the collection.
        Collections.addAll(names, "Darren", "Soh", "Test");
//StreamObserver is used to send req to server. LongGreet returns SteamObserver.
//The Stub passed StreamObserver is used by client.
        StreamObserver<GreetingRequest> stream = stub.longGreet(new StreamObserver<GreetingResponse>() {
            @Override
            public void onNext(GreetingResponse response) {
//The on next is merely just the next response the client receives, and it should be 1
                System.out.println(response.getResult());
            }
            @Override
            public void onError(Throwable t) {
            }
            @Override
            public void onCompleted() {
                latch.countDown();//sets latch to 0, to be able to quit program
            }
        });
// We need to iterate over names calling on next function as the above is not going to do that by itself
        for(String name: names){
            //need to pass a greeting request to on next
            stream.onNext(GreetingRequest.newBuilder().setFirstName(name).build());
        }
        stream.onCompleted();
        latch.await(3, TimeUnit.SECONDS);
    }
    public static void doGreetEveryone(ManagedChannel channel) throws InterruptedException {
        System.out.println("Enter doGreetEveryone");
        GreetingServiceGrpc.GreetingServiceStub stub = GreetingServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetingRequest> stream = stub.greetEveryone(new StreamObserver<GreetingResponse>() {
            @Override
            public void onNext(GreetingResponse response) {
                System.out.println(response.getResult());
            }

            @Override
            public void onError(Throwable t) {

            }
            @Override
            public void onCompleted() {
                latch.countDown();

            }
        });
        Arrays.asList("Darren", "Soh", "Test").forEach(name ->
                stream.onNext(GreetingRequest.newBuilder().setFirstName(name).build())
                );
        stream.onCompleted();
        latch.await(3, TimeUnit.SECONDS);



    }
    private static void doGreetWithDeadline(ManagedChannel channel){
        System.out.println("Enter doGreetWithDeadline");
        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);
        GreetingResponse response = stub.withDeadline(Deadline.after(3, TimeUnit.SECONDS))
                .greetWithDeadline(GreetingRequest.newBuilder().setFirstName("Darren").build());
        System.out.println("Greeting within deadline: " + response.getResult());
        try{
            response = stub.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
                    .greetWithDeadline(GreetingRequest.newBuilder().setFirstName("Darren").build());
            System.out.println("Greeting Deadline exceeded: " + response.getResult());
        } catch(StatusRuntimeException e) {
            if(e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED){
                System.out.println("deadline exceeded");
            } else {
                System.out.println("Got an Exception in greetWithDeadline");
                e.printStackTrace();
            }
        }

    }
// The args here should be used to call 1..* rpc endpoints
    public static void main(String[] args) throws InterruptedException {
        if(args.length == 0){
            System.out.println("Need one argument to work");
            return;
        }
//here we make channels objects. They create the TCP connection between client and server
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();
//Brilliantly used switch after checking length for 0
        switch(args[0]){
            case "greet":
                doGreet(channel); break;
            case "greet_many_times": // name of case is what you define Program Argument to be in configuration
                doGreetManyTimes(channel); break;
            case "long_greet":
                doLongGreet(channel); break;
            case "greet_everyone":
                doGreetEveryone(channel);break;
            case "greet_with_deadline":
                doGreetWithDeadline(channel);break;
            default:
                System.out.println("Keyword Invalid" + args[0]);
        }

        System.out.println("Shutting Down");
        channel.shutdown();

    }
}
