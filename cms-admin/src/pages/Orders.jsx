import React, { useState } from 'react';
import { Table, Tag, Space, Button } from 'antd';

const Orders = () => {
  const [data, setData] = useState([
    {
      key: '1',
      orderNo: 'ORD20260425001',
      userId: 123,
      totalAmount: 298.00,
      actualAmount: 149.00,
      paymentStatus: 'paid',
      orderStatus: 'completed',
      createdAt: '2026-04-25 10:30:00',
    },
  ]);

  const columns = [
    {
      title: '订单号',
      dataIndex: 'orderNo',
    },
    {
      title: '用户ID',
      dataIndex: 'userId',
    },
    {
      title: '订单金额',
      dataIndex: 'totalAmount',
      render: (amount) => `¥${amount}`,
    },
    {
      title: '实付金额',
      dataIndex: 'actualAmount',
      render: (amount) => `¥${amount}`,
    },
    {
      title: '支付状态',
      dataIndex: 'paymentStatus',
      render: (status) => {
        const colorMap = {
          pending: 'orange',
          paid: 'green',
          failed: 'red',
          refunded: 'blue',
        };
        const textMap = {
          pending: '待支付',
          paid: '已支付',
          failed: '失败',
          refunded: '已退款',
        };
        return <Tag color={colorMap[status]}>{textMap[status]}</Tag>;
      },
    },
    {
      title: '订单状态',
      dataIndex: 'orderStatus',
      render: (status) => {
        const colorMap = {
          pending: 'orange',
          completed: 'green',
          cancelled: 'red',
        };
        const textMap = {
          pending: '待处理',
          completed: '已完成',
          cancelled: '已取消',
        };
        return <Tag color={colorMap[status]}>{textMap[status]}</Tag>;
      },
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
          <Button size="small">查看详情</Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <h2>订单管理</h2>
      <Table columns={columns} dataSource={data} />
    </div>
  );
};

export default Orders;
