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
  MessageOutlined,
  FileTextOutlined,
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
      label: '首页数据看板',
    },
    {
      key: 'games',
      icon: <AppstoreOutlined />,
      label: '游戏商品管理',
      children: [
        {
          key: '/games',
          label: '游戏列表',
        },
        {
          key: '/games/banners',
          label: '首页Banner配置',
        },
      ],
    },
    {
      key: '/users',
      icon: <UserOutlined />,
      label: '用户账号管理',
    },
    {
      key: '/orders',
      icon: <ShoppingCartOutlined />,
      label: '订单财务管理',
    },
    {
      key: '/reviews',
      icon: <MessageOutlined />,
      label: '评论内容审核',
    },
    {
      key: '/announcements',
      icon: <FileTextOutlined />,
      label: '公告资讯发布',
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
