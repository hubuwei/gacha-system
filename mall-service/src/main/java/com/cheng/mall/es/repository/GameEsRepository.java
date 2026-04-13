package com.cheng.mall.es.repository;

import com.cheng.mall.es.document.GameDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 游戏 Elasticsearch Repository
 */
@Repository
public interface GameEsRepository extends ElasticsearchRepository<GameDocument, Long> {
    
    /**
     * 根据标题搜索（简单查询）
     */
    List<GameDocument> findByTitleContaining(String keyword);
    
    /**
     * 根据标签搜索
     */
    List<GameDocument> findByTagsContaining(String tag);
    
    /**
     * 删除索引
     */
    void deleteAll();
}
