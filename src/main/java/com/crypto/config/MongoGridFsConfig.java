package com.crypto.config;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoGridFsConfig {
    
    @Bean
    public GridFSBucket gridFSBucket(MongoTemplate mongoTemplate) {
        return GridFSBuckets.create(mongoTemplate.getDb());
    }
}
