package edu.escuela;

import com.mongodb.client.*;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;

import static spark.Spark.*;

public class SparkWebServer {

    public static void main(String... args){
        String uri = "mongodb://127.0.0.1:27017/?maxPoolSize=20&w=majority";
        MongoClient mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase("admin");
        port(getPort());
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

    private static String getLastLogs(MongoDatabase db){
        System.out.println("===== GETTING LAST LOGS =====");
        FindIterable<Document> iterDoc = db.getCollection("logs").find();
        Iterator<Document> it = iterDoc.iterator();
        int cont = 0;
        while (it.hasNext() && cont<10) {
            System.out.println(it.next());
            cont++;
        }
        return "Done";
    }



    private static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 35000;
    }

}