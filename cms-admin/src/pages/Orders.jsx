import React, { useState, useEffect } from 'react';
import { Table, Tag, Space, Button, Modal, Descriptions, Card, Form, Input, Select } from 'antd';
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons';
import request from '../utils/request';

const Orders = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentOrder, setCurrentOrder] = useState(null);
  const [searchForm] = Form.useForm();
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0
  });

  // 加载订单列表
  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async (page = 1, size = 20, filters = {}) => {
    setLoading(true);
    try {
      const params = { page, size, ...filters };
      
      // request baseURL已包含 /api/cms，所以只需 /orders
      const response = await request.get('/orders', { params });
      if (response.code === 200) {
        // API返回格式: { list: [...], total: 100, page: 1, size: 20 }
        setData(response.data?.list || []);
        setPagination({
          current: page,
          pageSize: size,
          total: response.data?.total || 0
        });
      }
    } catch (error) {
      console.error('获取订单列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 搜索
  const handleSearch = (values) => {
    const filters = {};
    if (values.orderNo) filters.orderNo = values.orderNo;
    if (values.paymentStatus) filters.paymentStatus = values.paymentStatus;
    if (values.orderStatus) filters.orderStatus = values.orderStatus;
    
    fetchOrders(1, pagination.pageSize, filters);
  };

  // 重置搜索
  const handleReset = () => {
    searchForm.resetFields();
    fetchOrders(1, pagination.pageSize, {});
  };

  // 分页变化
  const handleTableChange = (newPagination) => {
    const values = searchForm.getFieldsValue();
    const filters = {};
    if (values.orderNo) filters.orderNo = values.orderNo;
    if (values.paymentStatus) filters.paymentStatus = values.paymentStatus;
    if (values.orderStatus) filters.orderStatus = values.orderStatus;
    
    fetchOrders(newPagination.current, newPagination.pageSize, filters);
  };

  // 查看订单详情
  const handleViewDetail = async (record) => {
    try {
      const response = await request.get(`/cms/orders/${record.id}`);
      if (response.code === 200) {
        setCurrentOrder(response.data);
        setDetailVisible(true);
      }
    } catch (error) {
      console.error('获取订单详情失败:', error);
    }
  };

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
      width: 120,
      render: (_, record) => (
        <Space size="middle">
          <Button size="small" onClick={() => handleViewDetail(record)}>
            查看详情
          </Button>
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
          <Form.Item name="orderNo">
            <Input 
              placeholder="订单号" 
              prefix={<SearchOutlined />}
              allowClear
              style={{ width: 200 }}
            />
          </Form.Item>
          <Form.Item name="paymentStatus">
            <Select 
              placeholder="支付状态" 
              allowClear
              style={{ width: 150 }}
            >
              <Select.Option value="pending">待支付</Select.Option>
              <Select.Option value="paid">已支付</Select.Option>
              <Select.Option value="failed">失败</Select.Option>
              <Select.Option value="refunded">已退款</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="orderStatus">
            <Select 
              placeholder="订单状态" 
              allowClear
              style={{ width: 150 }}
            >
              <Select.Option value="pending">待处理</Select.Option>
              <Select.Option value="completed">已完成</Select.Option>
              <Select.Option value="cancelled">已取消</Select.Option>
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

      <h2>订单管理</h2>
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

      {/* 订单详情模态框 */}
      <Modal
        title="订单详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {currentOrder && (
          <Descriptions bordered column={2}>
            <Descriptions.Item label="订单号">{currentOrder.orderNo}</Descriptions.Item>
            <Descriptions.Item label="用户ID">{currentOrder.userId}</Descriptions.Item>
            <Descriptions.Item label="用户名">{currentOrder.username}</Descriptions.Item>
            <Descriptions.Item label="邮箱">{currentOrder.email}</Descriptions.Item>
            <Descriptions.Item label="订单金额">¥{currentOrder.totalAmount}</Descriptions.Item>
            <Descriptions.Item label="优惠金额">¥{currentOrder.discountAmount}</Descriptions.Item>
            <Descriptions.Item label="实付金额">¥{currentOrder.actualAmount}</Descriptions.Item>
            <Descriptions.Item label="支付方式">{currentOrder.paymentMethod || '-'}</Descriptions.Item>
            <Descriptions.Item label="支付状态">
              <Tag color={currentOrder.paymentStatus === 'paid' ? 'green' : 'orange'}>
                {currentOrder.paymentStatus === 'paid' ? '已支付' : '待支付'}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="订单状态">
              <Tag color={currentOrder.orderStatus === 'completed' ? 'green' : 'blue'}>
                {currentOrder.orderStatus === 'completed' ? '已完成' : '待处理'}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="创建时间">{currentOrder.createdAt}</Descriptions.Item>
            <Descriptions.Item label="支付时间">{currentOrder.paymentTime || '-'}</Descriptions.Item>
            <Descriptions.Item label="备注" span={2}>{currentOrder.remark || '-'}</Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
};

export default Orders;
