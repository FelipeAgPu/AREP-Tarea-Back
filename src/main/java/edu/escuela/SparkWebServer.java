package edu.escuela;

import com.mongodb.client.*;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static spark.Spark.*;

public class SparkWebServer {

    public static void main(String... args){
        String uri = "mongodb://ec2-3-88-222-210.compute-1.amazonaws.com:27017/?maxPoolSize=20&w=majority";
        MongoClient mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase("admin");
        port(getPort());
        // Allow CORS
        options("/*",
                (request, response) -> {
                    String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
                    }

                    String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
                    }

                    return "OK";
                });
        staticFiles.location("/public");
        post("/save", (request, response) -> {
            System.out.println(request.body());
            saveLog(database, request.body());
            return getLastLogs(database);
        });
    }

    private static void saveLog(MongoDatabase db, String message) {
        Document doc = new Document();
        doc.append("Message", message);
        doc.append("Date", LocalDateTime.now());
        db.getCollection("logs").insertOne(doc);
    }

    private static List<String> getLastLogs(MongoDatabase db){
        System.out.println("===== GETTING LAST LOGS =====");
        ArrayList<String> messages = new ArrayList<>();
        FindIterable<Document> iterDoc = db.getCollection("logs").find();
        iterDoc.forEach((Consumer<? super Document>) document -> messages.add(document.toJson()));
        List<String> last = messages.subList(Math.max(messages.size() - 10, 0), messages.size());
        System.out.println(last.toString());
        return last;
    }



    private static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 35000;
    }

}