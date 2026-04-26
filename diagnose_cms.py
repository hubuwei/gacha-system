#!/usr/bin/env python3
"""CMS系统诊断脚本"""

import subprocess
import sys

def run_command(cmd):
    """执行SSH命令并返回结果"""
    full_cmd = f'ssh root@111.228.12.167 "{cmd}"'
    result = subprocess.run(full_cmd, shell=True, capture_output=True, text=True)
    return result.stdout.strip(), result.stderr.strip()

def main():
    print("=" * 60)
    print("🔍 CMS系统完整诊断")
    print("=" * 60)
    print()
    
    # 1. 检查cms-service容器
    print("📦 步骤1: 检查cms-service容器状态...")
    stdout, _ = run_command("docker ps | grep gacha-cms-service")
    if stdout:
        print(f"✅ cms-service容器正在运行:\n{stdout}")
    else:
        print("❌ cms-service容器未运行")
        return
    print()
    
    # 2. 检查前端文件是否有硬编码
    print("🔍 步骤2: 检查前端文件是否有localhost:8085硬编码...")
    stdout, _ = run_command("find /usr/share/nginx/html/cms -name '*.js' -exec grep -l 'localhost:8085' {} \\; 2>/dev/null || echo 'NONE'")
    if stdout == 'NONE' or not stdout:
        print("✅ 前端文件没有localhost:8085硬编码")
    else:
        print(f"❌ 发现硬编码的文件:\n{stdout}")
    print()
    
    # 3. 检查Nginx配置
    print("🔍 步骤3: 检查Nginx配置...")
    stdout, _ = run_command("docker exec gacha-frontend cat /etc/nginx/conf.d/default.conf | grep -A 2 'upstream cms_service'")
    if 'cms_service' in stdout and 'gacha-cms-service:8085' in stdout:
        print("✅ Nginx配置中有cms_service upstream")
        print(stdout)
    else:
        print("❌ Nginx配置中没有正确的cms_service upstream")
        print(f"当前输出: {stdout}")
    print()
    
    # 检查/api/cms/的代理目标
    stdout, _ = run_command("docker exec gacha-frontend cat /etc/nginx/conf.d/default.conf | grep -A 2 'location /api/cms/'")
    if 'cms_service' in stdout:
        print("✅ /api/cms/ 代理到cms_service")
        print(stdout)
    elif 'mall_service' in stdout:
        print("❌ /api/cms/ 仍然代理到mall_service")
        print(stdout)
    else:
        print(f"⚠️  无法确定代理目标: {stdout}")
    print()
    
    # 4. 测试API
    print("🧪 步骤4: 测试API...")
    
    # 直接访问cms-service
    stdout, _ = run_command("curl -s -o /dev/null -w '%{http_code}' http://localhost:8085/api/cms/dashboard/stats")
    print(f"直接访问cms-service (localhost:8085): HTTP {stdout}")
    direct_ok = (stdout == '200')
    
    # 通过Nginx访问
    stdout, _ = run_command("curl -s -o /dev/null -w '%{http_code}' http://localhost/api/cms/dashboard/stats")
    print(f"通过Nginx访问 (localhost): HTTP {stdout}")
    nginx_ok = (stdout == '200')
    
    print()
    
    # 5. 总结
    print("=" * 60)
    print("📊 诊断结果总结")
    print("=" * 60)
    
    if direct_ok and nginx_ok:
        print("✅ 所有测试通过！CMS系统应该正常工作")
        print()
        print("💡 如果浏览器仍有问题，请：")
        print("   1. 按 Ctrl+Shift+R 强制刷新页面")
        print("   2. 清除浏览器缓存")
        print("   3. 检查浏览器控制台的Network标签")
    elif direct_ok and not nginx_ok:
        print("⚠️  cms-service正常，但Nginx代理有问题")
        print("   需要修复Nginx配置")
    else:
        print("❌ cms-service或Nginx配置有问题")
        print("   请检查上面的诊断信息")
    
    print("=" * 60)

if __name__ == '__main__':
    main()
