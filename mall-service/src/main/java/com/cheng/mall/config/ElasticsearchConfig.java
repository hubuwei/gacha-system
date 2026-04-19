package com.cheng.mall.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

/**
 * Elasticsearch 配置类
 * 只有当 spring.elasticsearch.uris 配置了有效的非本地地址时才启用
 */
@Configuration
@ConditionalOnExpression("'${spring.elasticsearch.uris:}'.length() > 0 && '${spring.elasticsearch.uris:}' != 'http://localhost:9200'")
@ConditionalOnClass(RestHighLevelClient.class)
public class ElasticsearchConfig {

    @Bean
    public ElasticsearchOperations elasticsearchTemplate(RestHighLevelClient elasticsearchClient) {
        return new ElasticsearchRestTemplate(elasticsearchClient);
    }
}