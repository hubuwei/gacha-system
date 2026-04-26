const axios = require('axios');

// 测试登录API
async function testLogin() {
  try {
    // 直接测试后端API
    console.log('Testing direct backend API...');
    const directResponse = await axios.post('http://111.228.12.167:8085/api/cms/auth/login', {
      username: 'admin',
      password: 'admin123'
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    console.log('Direct backend response:', directResponse.data);
  } catch (error) {
    console.error('Direct backend error:', error.response ? error.response.data : error.message);
  }

  try {
    // 测试通过Nginx代理的API
    console.log('\nTesting Nginx proxy API...');
    const proxyResponse = await axios.post('http://111.228.12.167/api/cms/auth/login', {
      username: 'admin',
      password: 'admin123'
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    console.log('Nginx proxy response:', proxyResponse.data);
  } catch (error) {
    console.error('Nginx proxy error:', error.response ? error.response.data : error.message);
  }

  try {
    // 测试获取仪表盘数据
    console.log('\nTesting dashboard stats...');
    const dashboardResponse = await axios.get('http://111.228.12.167:8085/api/cms/dashboard/stats');
    console.log('Dashboard stats:', dashboardResponse.data);
  } catch (error) {
    console.error('Dashboard error:', error.response ? error.response.data : error.message);
  }
}

testLogin();