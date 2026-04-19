package com.cheng.mall.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

/**
 * Elasticsearch 配置类
 * 通过 elasticsearch.enabled=true 显式启用
 */
@Configuration
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris:${ES_URIS:http://elasticsearch:9200}}")
    private String esUris;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        // 解析 URI
        String uri = esUris.replace("http://", "").replace("https://", "");
        String[] parts = uri.split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;
        
        return new RestHighLevelClient(
            RestClient.builder(new HttpHost(host, port, "http"))
        );
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate(RestHighLevelClient restHighLevelClient) {
        return new ElasticsearchRestTemplate(restHighLevelClient);
    }
}