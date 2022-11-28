package greeting.server;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;


// gRPC actually creates a base class for the implementation of server
public class GreetingServerImpl extends GreetingServiceGrpc.GreetingServiceImplBase{
//So we override the basic implementation of greet in greeting service.
//A greeting response is set up and it will be returned to client.
    @Override
    public void greet(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
        responseObserver.onNext(GreetingResponse.newBuilder().setResult("Hello " + request.getFirstName()).build());
        // here we need the below to say service is completed
        responseObserver.onCompleted();
    }
    @Override
    public void greetManyTimes(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver){
        GreetingResponse response = GreetingResponse.newBuilder().setResult("Hello " + request.getFirstName()).build();
//Loop to have 10 times from the server side
        for(int i = 0; i<10; ++i){
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }
    //Note what is interesting about the function below is that it returns a streamObserver
    @Override
    public StreamObserver<GreetingRequest> longGreet(StreamObserver<GreetingResponse> responseObserver) {
        //String concatenation was used to return one response
        StringBuilder sb = new StringBuilder();
        return new StreamObserver<GreetingRequest>() {
            @Override
            public void onNext(GreetingRequest request) {
                sb.append("Hello ");
                sb.append(request.getFirstName());
                sb.append("!\n");
            }

            @Override
            public void onError(Throwable t) {
//Gives back the error t
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(GreetingResponse.newBuilder().setResult(sb.toString()).build());
                responseObserver.onCompleted();
            }
        };
    }
    @Override
    public StreamObserver<GreetingRequest> greetEveryone(StreamObserver<GreetingResponse> responseObserver){
        //unlike before, we will not use string concatenation to return response
        return new StreamObserver<GreetingRequest>() {
            @Override
            public void onNext(GreetingRequest request) {
                responseObserver.onNext(GreetingResponse.newBuilder().setResult("Hello " + request.getFirstName()).build());
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }
//difference between this and long greet is that on next returns a response everytime while in long greet only
//one response was return, in the onCompleted.
            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void greetWithDeadline(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
        // will wait for 300ms but we need to check if the req is cancelled. Use Context.
        Context context = Context.current();
        try{
            for(int i=0;i<3;++i){
                if(context.isCancelled()){
                    return;
                }
                Thread.sleep(100);
            }
            responseObserver.onNext(GreetingResponse.newBuilder().setResult("Hello " + request.getFirstName()).build());
            responseObserver.onCompleted();
        } catch(InterruptedException e) {
            responseObserver.onError(e);

        }
    }
}
