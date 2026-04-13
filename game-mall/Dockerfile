# ============================================
# Frontend (React) Dockerfile
# ============================================

# 阶段1: 构建
FROM node:20-alpine AS builder

WORKDIR /app

# 复制package.json并安装依赖
COPY game-mall/package*.json ./
RUN npm ci

# 复制源代码并构建
COPY game-mall/ .
RUN npm run build

# 阶段2: Nginx服务
FROM nginx:alpine

# 复制自定义Nginx配置
COPY game-mall/nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制静态文件
COPY --from=builder /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
