import React, { useState, useEffect } from 'react';
import { Table, Tag, Space, Button, Switch, message, Popconfirm, Modal, Descriptions, Card, Form, Input, Select } from 'antd';
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons';
import request from '../utils/request';

const Users = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentUser, setCurrentUser] = useState(null);
  const [searchForm] = Form.useForm();
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0
  });

  // 加载用户列表
  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async (page = 1, size = 20, filters = {}) => {
    setLoading(true);
    try {
      const params = { page, size, ...filters };
      
      const response = await request.get('/users', { params });
      if (response.code === 200) {
        setData(response.data || []);
        setPagination({
          current: page,
          pageSize: size,
          total: response.data?.length || 0
        });
      }
    } catch (error) {
      console.error('获取用户列表失败:', error);
      message.error('获取用户列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 搜索
  const handleSearch = (values) => {
    const filters = {};
    if (values.username) filters.username = values.username;
    if (values.accountStatus !== undefined && values.accountStatus !== null) {
      filters.accountStatus = values.accountStatus;
    }
    
    fetchUsers(1, pagination.pageSize, filters);
  };

  // 重置搜索
  const handleReset = () => {
    searchForm.resetFields();
    fetchUsers(1, pagination.pageSize, {});
  };

  // 分页变化
  const handleTableChange = (newPagination) => {
    const values = searchForm.getFieldsValue();
    const filters = {};
    if (values.username) filters.username = values.username;
    if (values.accountStatus !== undefined && values.accountStatus !== null) {
      filters.accountStatus = values.accountStatus;
    }
    
    fetchUsers(newPagination.current, newPagination.pageSize, filters);
  };

  const handleStatusChange = async (record, checked) => {
    try {
      const newStatus = checked ? 1 : 0;
      const response = await request.put(`/users/${record.id}/status`, null, {
        params: { accountStatus: newStatus }
      });
      if (response.code === 200) {
        message.success(`用户 ${record.username} 已${checked ? '解封' : '封禁'}`);
        fetchUsers(); // 重新加载
      }
    } catch (error) {
      console.error('更新用户状态失败:', error);
      message.error('操作失败');
    }
  };

  // 查看用户详情
  const handleViewDetail = (record) => {
    setCurrentUser(record);
    setDetailVisible(true);
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
      width: 180,
      render: (_, record) => (
        <Space size="middle">
          <Button size="small" onClick={() => handleViewDetail(record)}>
            查看详情
          </Button>
          <Popconfirm
            title={`确定要${record.accountStatus === 1 ? '封禁' : '解封'}该用户吗？`}
            onConfirm={(e) => {
              e?.stopPropagation();
              handleStatusChange(record, record.accountStatus !== 1);
            }}
            okText="确定"
            cancelText="取消"
          >
            <Switch
              checked={record.accountStatus === 1}
              checkedChildren="正常"
              unCheckedChildren="封禁"
              onClick={(e) => e.stopPropagation()}
            />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      {/* 搜索区域 */}
      <Card style={{ marginBottom: 16 }}>
        <Form
          form={searchForm}
          layout="inline"
          onFinish={handleSearch}
        >
          <Form.Item name="username">
            <Input 
              placeholder="用户名" 
              prefix={<SearchOutlined />}
              allowClear
              style={{ width: 200 }}
            />
          </Form.Item>
          <Form.Item name="accountStatus">
            <Select 
              placeholder="账号状态" 
              allowClear
              style={{ width: 150 }}
            >
              <Select.Option value={1}>正常</Select.Option>
              <Select.Option value={0}>封禁</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
                搜索
              </Button>
              <Button onClick={handleReset} icon={<ReloadOutlined />}>
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <h2>用户管理</h2>
      <Table 
        columns={columns} 
        dataSource={data} 
        loading={loading}
        rowKey="id"
        pagination={{
          ...pagination,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条`,
          pageSizeOptions: ['10', '20', '50', '100']
        }}
        onChange={handleTableChange}
      />

      {/* 用户详情模态框 */}
      <Modal
        title="用户详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {currentUser && (
          <Descriptions bordered column={2}>
            <Descriptions.Item label="用户ID">{currentUser.id}</Descriptions.Item>
            <Descriptions.Item label="用户名">{currentUser.username}</Descriptions.Item>
            <Descriptions.Item label="邮箱">{currentUser.email || '-'}</Descriptions.Item>
            <Descriptions.Item label="手机号">{currentUser.phone || '-'}</Descriptions.Item>
            <Descriptions.Item label="昵称">{currentUser.nickname || '-'}</Descriptions.Item>
            <Descriptions.Item label="用户等级">Lv.{currentUser.userLevel || 1}</Descriptions.Item>
            <Descriptions.Item label="经验值">{currentUser.experiencePoints || 0}</Descriptions.Item>
            <Descriptions.Item label="账号状态">
              <Tag color={currentUser.accountStatus === 1 ? 'green' : 'red'}>
                {currentUser.accountStatus === 1 ? '正常' : '封禁'}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="最后登录时间">{currentUser.lastLoginTime || '-'}</Descriptions.Item>
            <Descriptions.Item label="注册时间">{currentUser.createdAt}</Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
};

export default Users;
