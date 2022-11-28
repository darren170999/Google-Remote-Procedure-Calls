package blog.server;
import com.google.api.ResourceProto;
import com.google.protobuf.Empty;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.print.Doc;

import java.util.stream.Stream;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;


public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {
//What we need to get from this Mongo Client is this mongo connection and this connection will be
//used throughout all API endpoints, used throughout all Endpoints in this section. allowing CRUD with DB
    private final MongoCollection<Document> mongoCollection;
    BlogServiceImpl(MongoClient client){
        MongoDatabase db = client.getDatabase("blogdb");//looks for blogdb else creates it
        mongoCollection = db.getCollection("blog");//looks for blog else creates it
    }

    @Override
    public void createBlog(Blog request, StreamObserver<BlogId> responseObserver){
        Document doc = new Document("author", request.getAuthor())
                .append("title", request.getTitle())
                .append("content", request.getContent());
        //we will try to insert the above into MongoDb
        InsertOneResult result;
        try{
            result = mongoCollection.insertOne(doc);

        } catch (MongoException e){
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getLocalizedMessage())
                    .asRuntimeException());
            return;
        }
        if(!result.wasAcknowledged() || result.getInsertedId()==null){
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Couldnt create")
                    .asRuntimeException());
            return;
        }
        String id = result.getInsertedId().asObjectId().getValue().toString();// getting UID as string
        responseObserver.onNext(BlogId.newBuilder().setId(id).build());
        responseObserver.onCompleted();
    }
    @Override
    public void readBlog(BlogId request, StreamObserver<Blog> responseObserver) {
        if(request.getId().isEmpty()){
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("The blog ID cannot be Empty")
                    .asRuntimeException());
            return;
        }
        String id = request.getId();
        Document result = mongoCollection.find(eq("_id" , new ObjectId(id))).first(); //return null or find first
        if(result == null ){
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Blog was not found")
                    .augmentDescription("BlogId: " + id)
                    .asRuntimeException());
            return;
        }
        responseObserver.onNext(Blog.newBuilder()
                .setAuthor(result.getString("author"))
                .setTitle(result.getString("title"))
                .setContent(result.getString("content"))
                .build());
        responseObserver.onCompleted();

    }
    @Override
    public void updateBlog(Blog request, StreamObserver<Empty> responseObserver){
        if(request.getId().isEmpty()){
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("The blog ID cannot be Empty")
                    .asRuntimeException());
            return;
        }
        String id = request.getId();
        Document result = mongoCollection.findOneAndUpdate(
                eq("_id", new ObjectId(id)),
                combine(
                        set("author", request.getAuthor()),
                        set("title", request.getTitle()),
                        set("content", request.getContent())
                )
        );
        if(result == null){
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Blog not found")
                    .augmentDescription("BlogId: " + id)
                    .asRuntimeException());
            return;
        }
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void listBlogs(Empty request, StreamObserver<Blog> responseObserver){
        for (Document document : mongoCollection.find()){
            responseObserver.onNext(Blog.newBuilder()
                    .setId(document.getObjectId("_id").toString())
                    .setAuthor(document.getString("author"))
                    .setTitle(document.getString("title"))
                    .setContent(document.getString("content"))
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBlog(BlogId request, StreamObserver<Empty> responseObserver){
        if(request.getId().isEmpty()){
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("The blog ID cannot be Empty")
                    .asRuntimeException());
            return;
        }
        String id = request.getId();
        DeleteResult result;
        try{
            result = mongoCollection.deleteOne(eq("_id", new ObjectId(id)));
        }catch (MongoException e){
            responseObserver.onError(Status.INTERNAL
                    .withDescription("The blog couldnt be deleted")
                    .asRuntimeException());
            return;
        }
        if (!result.wasAcknowledged()){
            responseObserver.onError(Status.INTERNAL
                    .withDescription("The blog couldnt be deleted")
                    .asRuntimeException());
            return;
        }
        if(result.getDeletedCount() == 0) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("The blog was not found")
                    .augmentDescription("BlogId: " + id)
                    .asRuntimeException());
            return;
        }
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

}
