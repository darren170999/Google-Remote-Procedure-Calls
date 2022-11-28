package blog.client;

import blog.server.BlogServer;
import blog.server.BlogServiceImpl;
import com.google.protobuf.Empty;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class BlogClient {

    private static BlogId createBlog(BlogServiceGrpc.BlogServiceBlockingStub stub){
        try{
            BlogId createResponse = stub.createBlog(
                    Blog.newBuilder()
                            .setAuthor("Darren")
                            .setTitle("New Blog!")
                            .setContent("Hello world!")
                            .build()
            );
            System.out.println("Blog created: " + createResponse.getId());
            return createResponse;
        } catch (StatusRuntimeException e ){
            System.out.println("Couldnt create the blog");
            e.printStackTrace();
            return null;
        }
    }
    private static void raedBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, BlogId blogId){
        try{
            Blog readResponse = stub.readBlog(blogId);
            System.out.println("Blog read: " + readResponse);
        } catch(StatusRuntimeException e){
            System.out.println("Couldnt read blog");
            e.printStackTrace();
        }
    }
    private static void updateBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, BlogId blogId){
        try{
            Blog newBlog = Blog.newBuilder().setId(blogId.getId()).setAuthor("Darren").setTitle("New Blog Changed!")
                    .setContent("Hello World i added some content").build();
            stub.updateBlog(newBlog);
            System.out.println("blog updated: " + newBlog);
        } catch(StatusRuntimeException e) {
            System.out.println("Couldnt update the blog");
            e.printStackTrace();
        }
    }

    private static void listBlogs(BlogServiceGrpc.BlogServiceBlockingStub stub){
        stub.listBlogs(Empty.getDefaultInstance()).forEachRemaining(System.out::println);
    }

    private static void deleteBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, BlogId blogId){
        try{
            stub.deleteBlog(blogId);
            System.out.println("Blog Deleted: " + blogId.getId());
        } catch(StatusRuntimeException e){
            System.out.println("The blog couldnt be deleted");
            e.printStackTrace();
        }
    }

    private static void run(ManagedChannel channel){
        BlogServiceGrpc.BlogServiceBlockingStub stub = BlogServiceGrpc.newBlockingStub(channel);
        BlogId blogId = createBlog(stub);

        if(blogId == null ){
            return;
        }
        raedBlog(stub, blogId);
        updateBlog(stub, blogId);
        listBlogs(stub);
        deleteBlog(stub, blogId);
    }

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        run(channel);
        System.out.println("shutting down");
        channel.shutdown();
    }
}
