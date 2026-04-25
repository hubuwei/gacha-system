import React, { useState, useEffect } from 'react';
import { Table, Button, Space, Tag, Modal, Form, Input, InputNumber, Switch, message, Popconfirm, Card, Row, Col } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined, ReloadOutlined } from '@ant-design/icons';
import request from '../utils/request';

const Games = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingGame, setEditingGame] = useState(null);
  const [form] = Form.useForm();
  const [searchForm] = Form.useForm();
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0
  });

  // 加载游戏列表
  useEffect(() => {
    fetchGames();
  }, []);

  const fetchGames = async (page = 1, size = 20, keyword = '') => {
    setLoading(true);
    try {
      const params = { page, size };
      if (keyword) {
        params.keyword = keyword;
      }
      
      const response = await request.get('/games', { params });
      if (response.code === 200) {
        setData(response.data || []);
        setPagination({
          current: page,
          pageSize: size,
          total: response.data?.length || 0
        });
      }
    } catch (error) {
      console.error('获取游戏列表失败:', error);
      message.error('获取游戏列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 搜索
  const handleSearch = (values) => {
    fetchGames(1, pagination.pageSize, values.keyword);
  };

  // 重置搜索
  const handleReset = () => {
    searchForm.resetFields();
    fetchGames(1, pagination.pageSize, '');
  };

  // 分页变化
  const handleTableChange = (newPagination) => {
    const keyword = searchForm.getFieldValue('keyword');
    fetchGames(newPagination.current, newPagination.pageSize, keyword);
  };

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
      width: 150,
      render: (_, record) => (
        <Space size="middle">
          <Button 
            icon={<EditOutlined />} 
            size="small"
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个游戏吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button icon={<DeleteOutlined />} danger size="small">
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // 打开新增/编辑模态框
  const handleEdit = (record) => {
    setEditingGame(record);
    form.setFieldsValue({
      title: record.title,
      basePrice: record.basePrice,
      currentPrice: record.currentPrice,
      isOnSale: record.isOnSale,
      rating: record.rating,
      totalSales: record.totalSales,
      description: record.description,
    });
    setModalVisible(true);
  };

  // 提交表单
  const handleSubmit = async (values) => {
    try {
      if (editingGame) {
        // 更新
        const response = await request.put(`/games/${editingGame.id}`, values);
        if (response.code === 200) {
          message.success('游戏更新成功');
          setModalVisible(false);
          fetchGames();
        }
      } else {
        // 新增
        const response = await request.post('/games', values);
        if (response.code === 200) {
          message.success('游戏创建成功');
          setModalVisible(false);
          fetchGames();
        }
      }
    } catch (error) {
      console.error('操作失败:', error);
      message.error(editingGame ? '更新失败' : '创建失败');
    }
  };

  // 删除游戏
  const handleDelete = async (id) => {
    try {
      const response = await request.delete(`/games/${id}`);
      if (response.code === 200) {
        message.success('游戏删除成功');
        fetchGames();
      }
    } catch (error) {
      console.error('删除失败:', error);
      message.error('删除失败');
    }
  };

  // 关闭模态框
  const handleCancel = () => {
    setModalVisible(false);
    setEditingGame(null);
    form.resetFields();
  };

  return (
    <div>
      {/* 搜索区域 */}
      <Card style={{ marginBottom: 16 }}>
        <Form
          form={searchForm}
          layout="inline"
          onFinish={handleSearch}
        >
          <Form.Item name="keyword">
            <Input 
              placeholder="搜索游戏名称" 
              prefix={<SearchOutlined />}
              allowClear
              style={{ width: 300 }}
              onPressEnter={() => searchForm.submit()}
            />
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

      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>游戏管理</h2>
        <Button 
          type="primary" 
          icon={<PlusOutlined />}
          onClick={() => {
            setEditingGame(null);
            form.resetFields();
            setModalVisible(true);
          }}
        >
          新增游戏
        </Button>
      </div>
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

      {/* 新增/编辑模态框 */}
      <Modal
        title={editingGame ? '编辑游戏' : '新增游戏'}
        open={modalVisible}
        onCancel={handleCancel}
        footer={null}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Form.Item
            label="游戏名称"
            name="title"
            rules={[{ required: true, message: '请输入游戏名称' }]}
          >
            <Input placeholder="请输入游戏名称" />
          </Form.Item>

          <Form.Item
            label="原价"
            name="basePrice"
            rules={[{ required: true, message: '请输入原价' }]}
          >
            <InputNumber 
              min={0} 
              step={0.01} 
              style={{ width: '100%' }}
              placeholder="请输入原价"
            />
          </Form.Item>

          <Form.Item
            label="现价"
            name="currentPrice"
            rules={[{ required: true, message: '请输入现价' }]}
          >
            <InputNumber 
              min={0} 
              step={0.01} 
              style={{ width: '100%' }}
              placeholder="请输入现价"
            />
          </Form.Item>

          <Form.Item
            label="促销中"
            name="isOnSale"
            valuePropName="checked"
          >
            <Switch />
          </Form.Item>

          <Form.Item
            label="评分"
            name="rating"
          >
            <InputNumber 
              min={0} 
              max={10} 
              step={0.1}
              style={{ width: '100%' }}
              placeholder="请输入评分（0-10）"
            />
          </Form.Item>

          <Form.Item
            label="销量"
            name="totalSales"
          >
            <InputNumber 
              min={0} 
              style={{ width: '100%' }}
              placeholder="请输入销量"
            />
          </Form.Item>

          <Form.Item
            label="描述"
            name="description"
          >
            <Input.TextArea rows={4} placeholder="请输入游戏描述" />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                {editingGame ? '更新' : '创建'}
              </Button>
              <Button onClick={handleCancel}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Games;
