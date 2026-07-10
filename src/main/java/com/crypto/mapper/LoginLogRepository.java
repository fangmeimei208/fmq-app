package com.crypto.mapper;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class LoginLogRepository {

    private final MongoTemplate mongoTemplate;

    public LoginLogRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    private MongoDatabase getDatabase() {
        return mongoTemplate.getDb();
    }

    private MongoCollection<Document> getCollection() {
        return getDatabase().getCollection("sys_login_log");
    }

    public void insert(Long userId, String username, String ip, String userAgent, String status, String failReason) {
        Document doc = new Document()
            .append("user_id", userId)
            .append("username", username)
            .append("login_ip", ip)
            .append("user_agent", userAgent)
            .append("login_time", new Date())
            .append("status", status)
            .append("fail_reason", failReason);
        getCollection().insertOne(doc);
    }

    public List<Document> findRecent(int limit) {
        List<Document> list = new ArrayList<>();
        getCollection().find()
            .sort(Sorts.descending("login_time"))
            .limit(limit)
            .into(list);
        return list;
    }

    public long countByDate(String dateStr) {
        Document filter = Document.parse(
            "{\"login_time\": {\"$gte\": {\"$date\": \"" + dateStr + "T00:00:00Z\"}, " +
            "\"$lt\": {\"$date\": \"" + dateStr + "T23:59:59Z\"}}}");
        return getCollection().countDocuments(filter);
    }
}
