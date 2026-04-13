#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
游戏商城后端代码生成器
自动生成 Entity、Repository、Service、Controller 等基础代码
"""

import os
from datetime import datetime

# 表结构配置
tables = {
    "User": {
        "table_name": "users",
        "comment": "用户",
        "fields": [
            {"name": "id", "type": "Long", "jdbc": "BIGINT", "comment": "用户 ID", "primary": True},
            {"name": "username", "type": "String", "jdbc": "VARCHAR(50)", "comment": "用户名"},
            {"name": "passwordHash", "type": "String", "jdbc": "VARCHAR(255)", "comment": "密码哈希"},
            {"name": "phone", "type": "String", "jdbc": "VARCHAR(20)", "comment": "手机号"},
            {"name": "phoneVerified", "type": "Boolean", "jdbc": "TINYINT(1)", "comment": "手机号验证"},
            {"name": "email", "type": "String", "jdbc": "VARCHAR(100)", "comment": "邮箱"},
            {"name": "emailVerified", "type": "Boolean", "jdbc": "TINYINT(1)", "comment": "邮箱验证"},
            {"name": "avatarUrl", "type": "String", "jdbc": "VARCHAR(500)", "comment": "头像 URL"},
            {"name": "nickname", "type": "String", "jdbc": "VARCHAR(50)", "comment": "昵称"},
            {"name": "signature", "type": "String", "jdbc": "VARCHAR(200)", "comment": "个性签名"},
            {"name": "accountStatus", "type": "Integer", "jdbc": "TINYINT", "comment": "账号状态"},
            {"name": "userLevel", "type": "Integer", "jdbc": "INT", "comment": "用户等级"},
            {"name": "experiencePoints", "type": "Integer", "jdbc": "INT", "comment": "经验值"},
            {"name": "lastLoginTime", "type": "LocalDateTime", "jdbc": "DATETIME", "comment": "最后登录时间"},
            {"name": "lastLoginIp", "type": "String", "jdbc": "VARCHAR(50)", "comment": "最后登录 IP"},
            {"name": "loginType", "type": "String", "jdbc": "VARCHAR(20)", "comment": "登录方式"},
        ]
    },
    "Game": {
        "table_name": "games",
        "comment": "游戏",
        "fields": [
            {"name": "id", "type": "Long", "jdbc": "BIGINT", "comment": "游戏 ID", "primary": True},
            {"name": "title", "type": "String", "jdbc": "VARCHAR(200)", "comment": "游戏标题"},
            {"name": "shortDescription", "type": "String", "jdbc": "VARCHAR(500)", "comment": "简短描述"},
            {"name": "fullDescription", "type": "String", "jdbc": "TEXT", "comment": "完整描述"},
            {"name": "coverImage", "type": "String", "jdbc": "VARCHAR(500)", "comment": "封面图片"},
            {"name": "basePrice", "type": "BigDecimal", "jdbc": "DECIMAL(10,2)", "comment": "基础价格"},
            {"name": "currentPrice", "type": "BigDecimal", "jdbc": "DECIMAL(10,2)", "comment": "当前价格"},
            {"name": "discountRate", "type": "Integer", "jdbc": "INT", "comment": "折扣率"},
            {"name": "isFeatured", "type": "Boolean", "jdbc": "TINYINT(1)", "comment": "是否精选"},
            {"name": "isOnSale", "type": "Boolean", "jdbc": "TINYINT(1)", "comment": "是否在售"},
            {"name": "rating", "type": "BigDecimal", "jdbc": "DECIMAL(3,2)", "comment": "评分"},
            {"name": "totalSales", "type": "Integer", "jdbc": "INT", "comment": "总销量"},
            {"name": "totalReviews", "type": "Integer", "jdbc": "INT", "comment": "总评论数"},
        ]
    },
    "Order": {
        "table_name": "orders",
        "comment": "订单",
        "fields": [
            {"name": "id", "type": "Long", "jdbc": "BIGINT", "comment": "订单 ID", "primary": True},
            {"name": "orderNo", "type": "String", "jdbc": "VARCHAR(50)", "comment": "订单编号"},
            {"name": "userId", "type": "Long", "jdbc": "BIGINT", "comment": "用户 ID"},
            {"name": "totalAmount", "type": "BigDecimal", "jdbc": "DECIMAL(10,2)", "comment": "订单总金额"},
            {"name": "actualAmount", "type": "BigDecimal", "jdbc": "DECIMAL(10,2)", "comment": "实际支付金额"},
            {"name": "paymentMethod", "type": "String", "jdbc": "VARCHAR(20)", "comment": "支付方式"},
            {"name": "paymentStatus", "type": "String", "jdbc": "VARCHAR(20)", "comment": "支付状态"},
            {"name": "orderStatus", "type": "String", "jdbc": "VARCHAR(20)", "comment": "订单状态"},
        ]
    },
}

base_package = "com.cheng.mall"
output_dir = "mall-service/src/main/java/com/cheng/mall"

def generate_entity(class_name, config):
    """生成 Entity 类"""
    content = f"""package {base_package}.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
"""
    
    # 添加 BigDecimal 导入
    if any(f['type'] == 'BigDecimal' for f in config['fields']):
        content += "import java.math.BigDecimal;\n"
    
    content += f"""
/**
 * {config['comment']}实体类
 * 表名：{config['table_name']}
 */
@Data
@Entity
@Table(name = "{config['table_name']}")
public class {class_name} implements Serializable {{
    
    private static final long serialVersionUID = 1L;
    
"""
    
    for field in config['fields']:
        if field.get('primary'):
            content += f"""    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
"""
        content += f"""    @Column(name = "{field['name']}"{', nullable = false' if field['name'] in ['id'] else ''})
    private {field['type']} {field['name']};
    
"""
    
    content += """    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
"""
    
    return content

def generate_repository(class_name, config):
    """生成 Repository 接口"""
    content = f"""package {base_package}.repository;

import {base_package}.entity.{class_name};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * {config['comment']}Repository
 */
@Repository
public interface {class_name}Repository extends JpaRepository<{class_name}, Long> {{
    
    /**
     * 根据 ID 查询
     */
    Optional<{class_name}> findById(Long id);
    
    /**
     * 查询所有
     */
    List<{class_name}> findAll();
    
    /**
     * 根据条件查询
     */
    @Query("SELECT e FROM {class_name} e WHERE e.isDeleted = false")
    List<{class_name}> findActive();
}}
"""
    return content

def generate_service(class_name, config):
    """生成 Service 类"""
    content = f"""package {base_package}.service;

import {base_package}.entity.{class_name};
import {base_package}.repository.{class_name}Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * {config['comment']}Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class {class_name}Service {{
    
    @Autowired
    private final {class_name}Repository {class_name.lower()}Repository;
    
    /**
     * 根据 ID 查询
     */
    public Optional<{class_name}> findById(Long id) {{
        return {class_name.lower()}Repository.findById(id);
    }}
    
    /**
     * 查询所有
     */
    public List<{class_name}> findAll() {{
        return {class_name.lower()}Repository.findAll();
    }}
    
    /**
     * 创建
     */
    @Transactional
    public {class_name} create({class_name} {class_name.lower()}) {{
        log.info("创建{config['comment']}: {{}}", {class_name.lower()});
        return {class_name.lower()}Repository.save({class_name.lower()});
    }}
    
    /**
     * 更新
     */
    @Transactional
    public {class_name} update(Long id, {class_name} {class_name.lower()}) {{
        log.info("更新{config['comment']}: id={{}}, {{}}", id, {class_name.lower()});
        return {class_name.lower()}Repository.findById(id)
                .map(existing -> {{
                    // TODO: 更新逻辑
                    return {class_name.lower()}Repository.save(existing);
                }})
                .orElseThrow(() -> new RuntimeException("{config['comment']}不存在"));
    }}
    
    /**
     * 删除
     */
    @Transactional
    public void delete(Long id) {{
        log.info("删除{config['comment']}: id={{}}", id);
        {class_name.lower()}Repository.deleteById(id);
    }}
}}
"""
    return content

def generate_controller(class_name, config):
    """生成 Controller 类"""
    content = f"""package {base_package}.controller;

import {base_package}.entity.{class_name};
import {base_package}.service.{class_name}Service;
import {base_package}.dto.CommonResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {config['comment']}Controller
 */
@RestController
@RequestMapping("/{class_name.uncapitalize()}s")
@RequiredArgsConstructor
@Api(tags = "{config['comment']}管理")
public class {class_name}Controller {{
    
    @Autowired
    private final {class_name}Service {class_name.uncapitalize()}Service;
    
    @GetMapping
    @ApiOperation("查询所有{config['comment']}")
    public CommonResponse<List<{class_name}>> findAll() {{
        return CommonResponse.success({class_name.uncapitalize()}Service.findAll());
    }}
    
    @GetMapping("/{{id}}")
    @ApiOperation("根据 ID 查询{config['comment']}")
    public CommonResponse<{class_name}> findById(@PathVariable Long id) {{
        return {class_name.uncapitalize()}Service.findById(id)
                .map(CommonResponse::success)
                .orElse(CommonResponse.error("{config['comment']}不存在"));
    }}
    
    @PostMapping
    @ApiOperation("创建{config['comment']}")
    public CommonResponse<{class_name}> create(@RequestBody {class_name} {class_name.uncapitalize()}) {{
        return CommonResponse.success({class_name.uncapitalize()}Service.create({class_name.uncapitalize()}));
    }}
    
    @PutMapping("/{{id}}")
    @ApiOperation("更新{config['comment']}")
    public CommonResponse<{class_name}> update(@PathVariable Long id, @RequestBody {class_name} {class_name.uncapitalize()}) {{
        return CommonResponse.success({class_name.uncapitalize()}Service.update(id, {class_name.uncapitalize()}));
    }}
    
    @DeleteMapping("/{{id}}")
    @ApiOperation("删除{config['comment']}")
    public CommonResponse<Void> delete(@PathVariable Long id) {{
        {class_name.uncapitalize()}Service.delete(id);
        return CommonResponse.success();
    }}
}}
"""
    return content

def main():
    """主函数"""
    print("=" * 50)
    print("游戏商城后端代码生成器")
    print("=" * 50)
    
    for class_name, config in tables.items():
        print(f"\n正在生成 {class_name} 相关代码...")
        
        # 生成 Entity
        entity_content = generate_entity(class_name, config)
        entity_path = f"{output_dir}/entity/{class_name}.java"
        os.makedirs(os.path.dirname(entity_path), exist_ok=True)
        with open(entity_path, 'w', encoding='utf-8') as f:
            f.write(entity_content)
        print(f"  ✓ Entity: {entity_path}")
        
        # 生成 Repository
        repo_content = generate_repository(class_name, config)
        repo_path = f"{output_dir}/repository/{class_name}Repository.java"
        os.makedirs(os.path.dirname(repo_path), exist_ok=True)
        with open(repo_path, 'w', encoding='utf-8') as f:
            f.write(repo_content)
        print(f"  ✓ Repository: {repo_path}")
        
        # 生成 Service
        service_content = generate_service(class_name, config)
        service_path = f"{output_dir}/service/{class_name}Service.java"
        os.makedirs(os.path.dirname(service_path), exist_ok=True)
        with open(service_path, 'w', encoding='utf-8') as f:
            f.write(service_content)
        print(f"  ✓ Service: {service_path}")
        
        # 生成 Controller
        controller_content = generate_controller(class_name, config)
        controller_path = f"{output_dir}/controller/{class_name}Controller.java"
        os.makedirs(os.path.dirname(controller_path), exist_ok=True)
        with open(controller_path, 'w', encoding='utf-8') as f:
            f.write(controller_content)
        print(f"  ✓ Controller: {controller_path}")
    
    print("\n" + "=" * 50)
    print("代码生成完成！")
    print("=" * 50)
    print("\n下一步:")
    print("1. 检查生成的代码并根据实际需求修改")
    print("2. 补充 DTO 类")
    print("3. 配置 Redis、Elasticsearch、RabbitMQ")
    print("4. 实现具体的业务逻辑")
    print("5. 运行并测试")

if __name__ == "__main__":
    main()
