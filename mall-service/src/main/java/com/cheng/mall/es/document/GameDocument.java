package com.cheng.mall.es.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 游戏 Elasticsearch 文档
 * 支持中文分词、拼音搜索、同义词
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "games")
@Setting(
    settingPath = "/elasticsearch/settings.json"
)
@Mapping(mappingPath = "/elasticsearch/mappings.json")
public class GameDocument {
    
    @Id
    private Long id;
    
    /**
     * 游戏标题 - 使用 ik_max_word 分词，支持拼音
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;
    
    /**
     * 简短描述 - 使用 ik_max_word 分词
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String shortDescription;
    
    /**
     * 完整描述 - 使用 ik_max_word 分词
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String fullDescription;
    
    /**
     * 开发商
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String developer;
    
    /**
     * 发行商
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String publisher;
    
    /**
     * 标签列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> tags;
    
    /**
     * 分类列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> categories;
    
    /**
     * 封面图片
     */
    @Field(type = FieldType.Keyword)
    private String coverImage;
    
    /**
     * 基础价格
     */
    @Field(type = FieldType.Double)
    private Double basePrice;
    
    /**
     * 当前价格
     */
    @Field(type = FieldType.Double)
    private Double currentPrice;
    
    /**
     * 折扣率
     */
    @Field(type = FieldType.Integer)
    private Integer discountRate;
    
    /**
     * 评分
     */
    @Field(type = FieldType.Double)
    private Double rating;
    
    /**
     * 评分人数
     */
    @Field(type = FieldType.Integer)
    private Integer ratingCount;
    
    /**
     * 总销量（用于热度排序）
     */
    @Field(type = FieldType.Long)
    private Long totalSales;
    
    /**
     * 总评论数
     */
    @Field(type = FieldType.Integer)
    private Integer totalReviews;
    
    /**
     * 是否精选
     */
    @Field(type = FieldType.Boolean)
    private Boolean isFeatured;
    
    /**
     * 是否在售
     */
    @Field(type = FieldType.Boolean)
    private Boolean isOnSale;
    
    /**
     * 发布日期
     */
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private LocalDateTime releaseDate;
    
    /**
     * 更新时间（用于增量同步）
     */
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss||epoch_millis")
    private LocalDateTime updatedAt;
    
    /**
     * 拼音字段（暂时使用IK分词器，等待pinyin插件安装）
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String titlePinyin;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String descriptionPinyin;
}
