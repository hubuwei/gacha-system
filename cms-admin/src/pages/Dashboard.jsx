import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Table } from 'antd';
import {
  UserOutlined,
  ShoppingCartOutlined,
  DollarOutlined,
  AppstoreOutlined,
} from '@ant-design/icons';
import { Column } from '@ant-design/charts';
import './Dashboard.css';

const Dashboard = () => {
  const [loading, setLoading] = useState(false);
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalOrders: 0,
    totalRevenue: 0,
    totalGames: 0,
  });

  // Mock data - replace with actual API calls
  useEffect(() => {
    // TODO: Call getDashboardStats() API
    setStats({
      totalUsers: 1234,
      totalOrders: 567,
      totalRevenue: 89012.50,
      totalGames: 89,
    });
  }, []);

  const revenueData = [
    { date: '周一', revenue: 12000 },
    { date: '周二', revenue: 15000 },
    { date: '周三', revenue: 18000 },
    { date: '周四', revenue: 14000 },
    { date: '周五', revenue: 22000 },
    { date: '周六', revenue: 28000 },
    { date: '周日', revenue: 25000 },
  ];

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

      <Card title="本周营收趋势" className="chart-card">
        <Column {...config} />
      </Card>
    </div>
  );
};

export default Dashboard;
