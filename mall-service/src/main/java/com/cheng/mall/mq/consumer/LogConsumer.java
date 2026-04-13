package com.cheng.mall.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.cheng.mall.config.RabbitMQConfig.*;

/**
 * 日志收集消费者
 */
@Slf4j
@Component
public class LogConsumer {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 审计日志文件路径
    private static final String AUDIT_LOG_DIR = "logs/audit";
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LOG_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * 处理审计日志消息
     */
    @RabbitListener(queues = LOG_AUDIT_QUEUE)
    public void handleAuditLog(String message) {
        try {
            Map<String, Object> logData = objectMapper.readValue(message, Map.class);
            
            Long userId = ((Number) logData.get("userId")).longValue();
            String action = (String) logData.get("action");
            String target = (String) logData.get("target");
            String details = (String) logData.get("details");
            Long timestamp = ((Number) logData.get("timestamp")).longValue();
            
            LocalDateTime logTime = LocalDateTime.now();
            String dateStr = logTime.format(DATE_FORMATTER);
            String timeStr = logTime.format(LOG_FORMATTER);
            
            // 构建日志内容
            String logContent = String.format("[%s] userId=%d, action=%s, target=%s, details=%s",
                timeStr, userId, action, target, details);
            
            log.info("收到审计日志: {}", logContent);
            
            // 写入审计日志文件
            writeAuditLog(dateStr, logContent);
            
        } catch (Exception e) {
            log.error("处理审计日志失败: {}", message, e);
        }
    }
    
    /**
     * 写入审计日志到文件
     */
    private void writeAuditLog(String dateStr, String logContent) {
        try {
            // 确保目录存在
            Path dirPath = Paths.get(AUDIT_LOG_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            // 按日期分文件
            String fileName = String.format("audit-%s.log", dateStr);
            Path filePath = dirPath.resolve(fileName);
            
            // 追加写入
            try (FileWriter writer = new FileWriter(filePath.toFile(), true)) {
                writer.write(logContent + System.lineSeparator());
            }
            
            log.debug("审计日志已写入: {}", filePath);
            
        } catch (IOException e) {
            log.error("写入审计日志文件失败", e);
        }
    }
}
