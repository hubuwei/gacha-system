import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Table, Spin } from 'antd';
import {
  UserOutlined,
  ShoppingCartOutlined,
  DollarOutlined,
  AppstoreOutlined,
} from '@ant-design/icons';
import { Column } from '@ant-design/charts';
import request from '../utils/request';
import './Dashboard.css';

const Dashboard = () => {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalOrders: 0,
    totalRevenue: 0,
    totalGames: 0,
  });
  const [revenueData, setRevenueData] = useState([]);

  // 获取统计数据
  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      
      // 获取统计数据
      const statsResponse = await request.get('/dashboard/stats');
      if (statsResponse.code === 200) {
        setStats(statsResponse.data);
      }

      // 获取本周营收数据
      const revenueResponse = await request.get('/dashboard/weekly-revenue');
      if (revenueResponse.code === 200) {
        setRevenueData(revenueResponse.data);
      }
    } catch (error) {
      console.error('获取数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const config = {
    data: revenueData,
    xField: 'date',
    yField: 'revenue',
    label: {
      position: 'middle',
      style: {
        fill: '#FFFFFF',
        opacity: 0.6,
      },
    },
    xAxis: {
      label: {
        autoHide: true,
        autoRotate: false,
      },
    },
    meta: {
      revenue: {
        alias: '营收',
      },
    },
  };

  return (
    <div className="dashboard">
      <h2>数据看板</h2>
      
      <Spin spinning={loading}>
        <Row gutter={[16, 16]} className="stats-row">
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="总用户数"
              value={stats.totalUsers}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="总订单数"
              value={stats.totalOrders}
              prefix={<ShoppingCartOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="总营收"
              value={stats.totalRevenue}
              precision={2}
              prefix={<DollarOutlined />}
              valueStyle={{ color: '#cf1322' }}
              suffix="元"
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="游戏总数"
              value={stats.totalGames}
              prefix={<AppstoreOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
      </Row>

      {revenueData.length > 0 && (
        <Card title="本周营收趋势" className="chart-card">
          <Column {...config} />
        </Card>
      )}
      </Spin>
    </div>
  );
};

export default Dashboard;
