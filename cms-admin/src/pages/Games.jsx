import React, { useState } from 'react';
import { Table, Button, Space, Tag, Modal, Form, Input, InputNumber, Switch, message, Upload } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';

const Games = () => {
  const [data, setData] = useState([
    {
      key: '1',
      id: 1,
      title: '赛博朋克2077',
      basePrice: 298.00,
      currentPrice: 149.00,
      isOnSale: true,
      rating: 8.5,
      totalSales: 1234,
    },
  ]);

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
    },
    {
      title: '游戏名称',
      dataIndex: 'title',
    },
    {
      title: '原价',
      dataIndex: 'basePrice',
      render: (price) => `¥${price}`,
    },
    {
      title: '现价',
      dataIndex: 'currentPrice',
      render: (price) => `¥${price}`,
    },
    {
      title: '促销中',
      dataIndex: 'isOnSale',
      render: (onSale) => (
        <Tag color={onSale ? 'green' : 'default'}>
          {onSale ? '是' : '否'}
        </Tag>
      ),
    },
    {
      title: '评分',
      dataIndex: 'rating',
    },
    {
      title: '销量',
      dataIndex: 'totalSales',
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
        <h2>游戏管理</h2>
        <Button type="primary" icon={<PlusOutlined />}>
          新增游戏
        </Button>
      </div>
      <Table columns={columns} dataSource={data} />
    </div>
  );
};

export default Games;
