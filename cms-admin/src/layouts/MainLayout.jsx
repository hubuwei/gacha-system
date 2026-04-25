import React, { useState } from 'react';
import { Layout, Menu, Avatar, Dropdown } from 'antd';
import {
  DashboardOutlined,
  AppstoreOutlined,
  ShoppingCartOutlined,
  UserOutlined,
  NotificationOutlined,
  SettingOutlined,
  LogoutOutlined,
} from '@ant-design/icons';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import './MainLayout.css';

const { Header, Sider, Content } = Layout;

const MainLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);

  const adminInfo = JSON.parse(localStorage.getItem('admin_info') || '{}');

  const menuItems = [
    {
      key: '/',
      icon: <DashboardOutlined />,
      label: '数据看板',
    },
    {
      key: '/games',
      icon: <AppstoreOutlined />,
      label: '游戏管理',
    },
    {
      key: '/orders',
      icon: <ShoppingCartOutlined />,
      label: '订单管理',
    },
    {
      key: '/users',
      icon: <UserOutlined />,
      label: '用户管理',
    },
    {
      key: '/announcements',
      icon: <NotificationOutlined />,
      label: '活动公告',
    },
    {
      key: 'notifications',
      icon: <NotificationOutlined />,
      label: '消息通知',
      children: [
        {
          key: '/notifications/broadcast',
          label: '广播通知',
        },
        {
          key: '/notifications/discount',
          label: '折扣通知',
        },
      ],
    },
    {
      key: '/settings',
      icon: <SettingOutlined />,
      label: '系统设置',
    },
  ];

  const handleLogout = () => {
    localStorage.removeItem('admin_token');
    localStorage.removeItem('admin_info');
    navigate('/login');
  };

  const userMenuItems = [
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: handleLogout,
    },
  ];

  return (
    <Layout className="main-layout">
      <Sider trigger={null} collapsible collapsed={collapsed}>
        <div className="logo">
          {collapsed ? 'CMS' : '游戏商城 CMS'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header className="header">
          <div
            className="trigger"
            onClick={() => setCollapsed(!collapsed)}
          >
            {collapsed ? '☰' : '✕'}
          </div>
          <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
            <div className="user-info">
              <Avatar icon={<UserOutlined />} />
              <span className="username">{adminInfo.username || '管理员'}</span>
            </div>
          </Dropdown>
        </Header>
        <Content className="content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
