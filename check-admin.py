import mysql.connector

# 数据库连接信息
db_config = {
    'host': '111.228.12.167',
    'port': 3306,
    'user': 'root',
    'password': 'Xc037417!',
    'database': 'gacha_system_prod'
}

try:
    # 连接数据库
    conn = mysql.connector.connect(**db_config)
    cursor = conn.cursor()
    
    # 检查admin表是否存在
    cursor.execute("SHOW TABLES LIKE 'admin'")
    table_exists = cursor.fetchone()
    
    if table_exists:
        print("Admin table exists")
        
        # 查看表结构
        cursor.execute("DESCRIBE admin")
        print("\nTable structure:")
        for column in cursor.fetchall():
            print(column)
        
        # 查看表数据
        cursor.execute("SELECT id, username, is_active FROM admin")
        admin_users = cursor.fetchall()
        print("\nAdmin users:")
        for user in admin_users:
            print(user)
    else:
        print("Admin table does not exist")
        
    # 关闭连接
    cursor.close()
    conn.close()
    
except Exception as e:
    print(f"Error: {e}")