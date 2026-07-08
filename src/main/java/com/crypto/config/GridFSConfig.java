package com.crypto.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

/**
 * MongoDB GridFS 配置
 */
@Configuration
public class GridFSConfig {

    /**
     * 默认的 GridFsTemplate（bucket: fs）
     */
    @Bean
    @Primary
    public GridFsTemplate gridFsTemplate(MongoDatabaseFactory dbFactory, MongoConverter converter) {
        return new GridFsTemplate(dbFactory, converter);
    }

    /**
     * PG EDI 文件专用 GridFsTemplate（bucket: PG.DATAHUB_EDI_FILE）
     */
    @Bean("pgEdiGridFsTemplate")
    public GridFsTemplate pgEdiGridFsTemplate(MongoDatabaseFactory dbFactory, MongoConverter converter) {
        return new GridFsTemplate(dbFactory, converter, "PG.DATAHUB_EDI_FILE");
    }
}