import React, { useState } from 'react';
import { Table, Button, Space, Tag, Modal, Form, Input, Select, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';

const Announcements = () => {
  const [data, setData] = useState([
    {
      key: '1',
      id: 1,
      title: '五一促销活动预告',
      type: 'activity',
      priority: 90,
      isActive: true,
      createdAt: '2026-04-25 10:30:00',
    },
  ]);

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
    },
    {
      title: '标题',
      dataIndex: 'title',
    },
    {
      title: '类型',
      dataIndex: 'type',
      render: (type) => {
        const colorMap = {
          info: 'blue',
          activity: 'green',
          maintenance: 'orange',
          update: 'purple',
        };
        const textMap = {
          info: '通知',
          activity: '活动',
          maintenance: '维护',
          update: '更新',
        };
        return <Tag color={colorMap[type]}>{textMap[type]}</Tag>;
      },
    },
    {
      title: '优先级',
      dataIndex: 'priority',
    },
    {
      title: '状态',
      dataIndex: 'isActive',
      render: (active) => (
        <Tag color={active ? 'green' : 'default'}>
          {active ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="middle">
          <Button icon={<EditOutlined />} size="small">
            编辑
          </Button>
          <Button icon={<DeleteOutlined />} danger size="small">
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>活动公告</h2>
        <Button type="primary" icon={<PlusOutlined />}>
          发布公告
        </Button>
      </div>
      <Table columns={columns} dataSource={data} />
    </div>
  );
};

export default Announcements;
