import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Table, Spin, Tag } from 'antd';
import {
  UserOutlined,
  ShoppingCartOutlined,
  DollarOutlined,
  AppstoreOutlined,
  RiseOutlined,
  FallOutlined,
  ClockCircleOutlined,
  AuditOutlined,
} from '@ant-design/icons';
import { Column } from '@ant-design/charts';
import request from '../utils/request';
import './Dashboard.css';

const Dashboard = () => {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalUsers: 0,
    todayNewUsers: 0,
    totalOrders: 0,
    todayOrders: 0,
    totalRevenue: 0,
    todayRevenue: 0,
    totalGames: 0,
    pendingOrders: 0,
    pendingReviews: 0,
  });
  const [revenueData, setRevenueData] = useState([]);
  const [popularGames, setPopularGames] = useState([]);

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

      // 获取热门游戏排行
      const popularGamesResponse = await request.get('/dashboard/popular-games?limit=5');
      if (popularGamesResponse.code === 200) {
        setPopularGames(popularGamesResponse.data);
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
      position: 'top',  // 修改为top
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
        {/* 今日核心指标 */}
        <Row gutter={[16, 16]} className="stats-row" style={{ marginBottom: 16 }}>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="今日新增用户"
                value={stats.todayNewUsers}
                prefix={<UserOutlined />}
                valueStyle={{ color: '#52c41a' }}  // 暂时保留，等待Ant Design更新
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="今日订单量"
                value={stats.todayOrders}
                prefix={<ShoppingCartOutlined />}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="今日销售额"
                value={stats.todayRevenue}
                precision={2}
                prefix={<DollarOutlined />}
                valueStyle={{ color: '#faad14' }}
                suffix="元"
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="待处理事项"
                value={stats.pendingOrders + stats.pendingReviews}
                prefix={<ClockCircleOutlined />}
                valueStyle={{ color: '#ff4d4f' }}
              />
              <div style={{ marginTop: 8, fontSize: 12, color: '#999' }}>
                待处理订单: {stats.pendingOrders} | 待审核评论: {stats.pendingReviews}
              </div>
            </Card>
          </Col>
        </Row>

        {/* 累计统计数据 */}
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
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      {revenueData.length > 0 && (
        <Card title="本周营收趋势" className="chart-card" style={{ marginTop: 16 }}>
          <Column {...config} />
        </Card>
      )}

      {/* 热门游戏排行 */}
      {popularGames.length > 0 && (
        <Card title="热门游戏排行 TOP5" className="chart-card" style={{ marginTop: 16 }}>
          <Table
            dataSource={popularGames}
            rowKey="id"
            pagination={false}
            columns={[
              {
                title: '排名',
                key: 'rank',
                width: 80,
                render: (_, __, index) => (
                  <Tag color={index === 0 ? 'gold' : index === 1 ? 'silver' : index === 2 ? 'bronze' : 'default'}>
                    #{index + 1}
                  </Tag>
                ),
              },
              {
                title: '游戏名称',
                dataIndex: 'title',
                key: 'title',
              },
              {
                title: '价格',
                dataIndex: 'price',
                key: 'price',
                width: 120,
                render: (price) => `¥${Number(price).toFixed(2)}`,
              },
              {
                title: '销量',
                dataIndex: 'sales_count',
                key: 'sales_count',
                width: 120,
                sorter: (a, b) => a.sales_count - b.sales_count,
              },
              {
                title: '销售额',
                dataIndex: 'total_revenue',
                key: 'total_revenue',
                width: 150,
                render: (revenue) => `¥${Number(revenue).toFixed(2)}`,
                sorter: (a, b) => a.total_revenue - b.total_revenue,
              },
            ]}
          />
        </Card>
      )}
      </Spin>
    </div>
  );
};

export default Dashboard;
