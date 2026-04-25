import React, { useState } from 'react';
import { Table, Tag, Space, Button, Switch, message } from 'antd';

const Users = () => {
  const [data, setData] = useState([
    {
      key: '1',
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      balance: 1000.00,
      accountStatus: 1,
      createdAt: '2026-04-20 10:30:00',
    },
  ]);

  const handleStatusChange = (record, checked) => {
    message.success(`用户 ${record.username} 已${checked ? '解封' : '封禁'}`);
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
    },
    {
      title: '用户名',
      dataIndex: 'username',
    },
    {
      title: '邮箱',
      dataIndex: 'email',
    },
    {
      title: '余额',
      dataIndex: 'balance',
      render: (balance) => `¥${balance}`,
    },
    {
      title: '账号状态',
      dataIndex: 'accountStatus',
      render: (status) => (
        <Tag color={status === 1 ? 'green' : 'red'}>
          {status === 1 ? '正常' : '封禁'}
        </Tag>
      ),
    },
    {
      title: '注册时间',
      dataIndex: 'createdAt',
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="middle">
          <Button size="small">查看详情</Button>
          <Switch
            checked={record.accountStatus === 1}
            onChange={(checked) => handleStatusChange(record, checked)}
            checkedChildren="正常"
            unCheckedChildren="封禁"
          />
        </Space>
      ),
    },
  ];

  return (
    <div>
      <h2>用户管理</h2>
      <Table columns={columns} dataSource={data} />
    </div>
  );
};

export default Users;
