# CMS Maven 构建问题修复指南

## ❌ 原始错误信息

```
[ERROR] Non-resolvable parent POM for com.cheng:cms-service:1.0-SNAPSHOT
[ERROR] The following artifacts could not be resolved: com.cheng:gacha-system:pom:1.0-SNAPSHOT
[ERROR] 'parent.relativePath' points at wrong local POM
```

## 🔍 问题分析

这个错误是因为 **Maven 父子项目配置不一致**导致的。具体原因：

1. **父项目 (pom.xml)** 的 `groupId` 是 `com.example`
2. **子项目 (cms-service/pom.xml)** 引用的父项目 `groupId` 是 `com.cheng`
3. **版本号不匹配**：父项目是 `1.0.0-SNAPSHOT`，子项目是 `1.0-SNAPSHOT`

Maven 在构建时会查找父项目，如果 groupId 或 version 不匹配，就会报错。

## ✅ 已修复的内容

### 1. 修复 cms-service/pom.xml

**修改前：**
```xml
<parent>
    <groupId>com.cheng</groupId>
    <artifactId>gacha-system</artifactId>
    <version>1.0-SNAPSHOT</version>
</parent>

<dependency>
    <groupId>com.cheng</groupId>
    <artifactId>common</artifactId>
    <version>${project.version}</version>
</dependency>
```

**修改后：**
```xml
<parent>
    <groupId>com.example</groupId>
    <artifactId>gacha-system</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</parent>

<dependency>
    <groupId>com.example</groupId>
    <artifactId>common</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 将 cms-service 添加到父项目 modules

**文件：** `pom.xml`（根目录）

**修改前：**
```xml
<modules>
    <module>auth-service</module>
    <module>common</module>
    <module>game-service</module>
    <module>gacha-service</module>
    <module>mall-service</module>
</modules>
```

**修改后：**
```xml
<modules>
    <module>auth-service</module>
    <module>common</module>
    <module>game-service</module>
    <module>gacha-service</module>
    <module>mall-service</module>
    <module>cms-service</module>  <!-- 新增 -->
</modules>
```

## 📋 Maven 多模块项目结构

```
gacha-system/                    ← 父项目 (com.example:gacha-system:1.0.0-SNAPSHOT)
├── pom.xml                      ← 父 POM，定义所有子模块
├── common/                      ← 公共模块
│   └── pom.xml                  ← 引用父项目
├── auth-service/                ← 认证服务
│   └── pom.xml                  ← 引用父项目
├── cms-service/                 ← CMS 服务（新增）
│   └── pom.xml                  ← 引用父项目
└── ...其他模块
```

### 关键规则

1. **所有子项目的 parent 必须一致**
   - groupId: `com.example`
   - artifactId: `gacha-system`
   - version: `1.0.0-SNAPSHOT`

2. **父项目必须在 modules 中声明所有子模块**
   ```xml
   <modules>
       <module>模块名称</module>
   </modules>
   ```

3. **子项目之间的依赖使用 `${project.version}`**
   ```xml
   <dependency>
       <groupId>com.example</groupId>
       <artifactId>common</artifactId>
       <version>${project.version}</version>
   </dependency>
   ```

## 🚀 正确的构建顺序

### 方法一：从根目录构建（推荐）

```bash
cd E:\CFDemo\gacha-system
mvn clean install -DskipTests
```

这会按照以下顺序构建：
1. common（被其他模块依赖）
2. auth-service、cms-service 等（依赖 common）

### 方法二：分步构建

```bash
# Step 1: 构建父项目和 common 模块
cd E:\CFDemo\gacha-system\common
mvn clean install -DskipTests

# Step 2: 构建 cms-service
cd E:\CFDemo\gacha-system\cms-service
mvn clean package -DskipTests
```

## ⚠️ 常见错误及解决方案

### 错误1：找不到父项目

**错误信息：**
```
Non-resolvable parent POM
Could not find artifact com.example:gacha-system:pom:1.0.0-SNAPSHOT
```

**原因：**
- 父项目未安装到本地 Maven 仓库
- groupId 或 version 不匹配

**解决：**
```bash
# 从根目录执行，会先安装父项目
cd E:\CFDemo\gacha-system
mvn clean install -DskipTests
```

### 错误2：找不到依赖模块

**错误信息：**
```
Could not resolve dependencies for project com.example:cms-service
Could not find artifact com.example:common:jar:1.0.0-SNAPSHOT
```

**原因：**
- common 模块未编译安装

**解决：**
```bash
# 先编译 common 模块
cd E:\CFDemo\gacha-system\common
mvn clean install -DskipTests
```

### 错误3：模块未在父项目中声明

**现象：**
- 从根目录执行 `mvn clean install` 时，某些模块没有被构建

**原因：**
- 该模块未在父 pom.xml 的 `<modules>` 中声明

**解决：**
在父 pom.xml 中添加：
```xml
<modules>
    <module>你的模块名</module>
</modules>
```

## 🔧 验证配置是否正确

### 检查1：验证父项目配置

```bash
cd E:\CFDemo\gacha-system
cat pom.xml | Select-String -Pattern "groupId|artifactId|version" | Select-Object -First 3
```

应该看到：
```
<groupId>com.example</groupId>
<artifactId>gacha-system</artifactId>
<version>1.0.0-SNAPSHOT</version>
```

### 检查2：验证子项目配置

```bash
cd E:\CFDemo\gacha-system\cms-service
cat pom.xml | Select-String -Pattern "parent" -Context 3
```

应该看到：
```xml
<parent>
    <groupId>com.example</groupId>
    <artifactId>gacha-system</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</parent>
```

### 检查3：测试编译

```bash
# 测试 common 模块
cd E:\CFDemo\gacha-system\common
mvn clean compile

# 测试 cms-service
cd E:\CFDemo\gacha-system\cms-service
mvn clean compile
```

如果没有错误输出，说明配置正确！✅

## 📝 最佳实践

1. **统一使用相同的 groupId**
   - 整个项目使用同一个 groupId（如 `com.example`）
   - 避免混用不同的 groupId

2. **统一版本号**
   - 父项目定义版本
   - 子项目使用 `${project.version}` 引用

3. **及时更新 modules 列表**
   - 每添加一个新模块，立即在父 pom.xml 中声明

4. **优先从根目录构建**
   - 使用 `mvn clean install` 从根目录构建
   - Maven 会自动处理模块间的依赖关系

5. **定期清理本地仓库**
   ```bash
   # 如果遇到奇怪的依赖问题
   mvn dependency:purge-local-repository
   ```

## 🎯 快速修复命令

如果遇到 Maven 构建问题，按以下步骤操作：

```powershell
# Step 1: 清理所有 target 目录
cd E:\CFDemo\gacha-system
Get-ChildItem -Recurse -Directory -Filter "target" | Remove-Item -Recurse -Force

# Step 2: 从根目录重新构建
mvn clean install -DskipTests

# Step 3: 验证 cms-service
cd cms-service
mvn spring-boot:run
```

---

**提示：** 如果问题仍然存在，请检查：
1. Maven 是否正确安装（`mvn -version`）
2. Java 版本是否为 11（`java -version`）
3. 网络连接是否正常（首次构建需要下载依赖）
