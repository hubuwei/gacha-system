import React, { useState, useEffect } from 'react';
import { Table, Button, Space, Tag, Modal, Form, Input, Select, message, Popconfirm, InputNumber, Switch, DatePicker } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import request from '../utils/request';
import dayjs from 'dayjs';

const Announcements = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [form] = Form.useForm();

  // 加载公告列表
  useEffect(() => {
    fetchAnnouncements();
  }, []);

  const fetchAnnouncements = async () => {
    setLoading(true);
    try {
      const response = await request.get('/announcements', {
        params: { page: 1, size: 100 }
      });
      if (response.code === 200) {
        setData(response.data || []);
      }
    } catch (error) {
      console.error('获取公告列表失败:', error);
      message.error('获取公告列表失败');
    } finally {
      setLoading(false);
    }
  };

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
            title="确定要删除这个公告吗？"
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
    setEditingItem(record);
    form.setFieldsValue({
      title: record.title,
      content: '', // 需要从详情接口获取
      type: record.type,
      priority: record.priority,
      isActive: record.isActive,
    });
    setModalVisible(true);
  };

  // 提交表单
  const handleSubmit = async (values) => {
    try {
      if (editingItem) {
        // 更新
        const response = await request.put(`/announcements/${editingItem.id}`, values);
        if (response.code === 200) {
          message.success('公告更新成功');
          setModalVisible(false);
          fetchAnnouncements();
        }
      } else {
        // 新增
        const response = await request.post('/announcements', values);
        if (response.code === 200) {
          message.success('公告创建成功');
          setModalVisible(false);
          fetchAnnouncements();
        }
      }
    } catch (error) {
      console.error('操作失败:', error);
      message.error(editingItem ? '更新失败' : '创建失败');
    }
  };

  // 删除公告
  const handleDelete = async (id) => {
    try {
      const response = await request.delete(`/announcements/${id}`);
      if (response.code === 200) {
        message.success('公告删除成功');
        fetchAnnouncements();
      }
    } catch (error) {
      console.error('删除失败:', error);
      message.error('删除失败');
    }
  };

  // 关闭模态框
  const handleCancel = () => {
    setModalVisible(false);
    setEditingItem(null);
    form.resetFields();
  };

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>活动公告</h2>
        <Button 
          type="primary" 
          icon={<PlusOutlined />}
          onClick={() => {
            setEditingItem(null);
            form.resetFields();
            setModalVisible(true);
          }}
        >
          发布公告
        </Button>
      </div>
      <Table 
        columns={columns} 
        dataSource={data} 
        loading={loading}
        rowKey="id"
      />

      {/* 新增/编辑模态框 */}
      <Modal
        title={editingItem ? '编辑公告' : '发布公告'}
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
            label="标题"
            name="title"
            rules={[{ required: true, message: '请输入标题' }]}
          >
            <Input placeholder="请输入公告标题" />
          </Form.Item>

          <Form.Item
            label="内容"
            name="content"
            rules={[{ required: true, message: '请输入内容' }]}
          >
            <Input.TextArea rows={6} placeholder="请输入公告内容" />
          </Form.Item>

          <Form.Item
            label="类型"
            name="type"
            rules={[{ required: true, message: '请选择类型' }]}
          >
            <Select>
              <Select.Option value="info">通知</Select.Option>
              <Select.Option value="activity">活动</Select.Option>
              <Select.Option value="maintenance">维护</Select.Option>
              <Select.Option value="update">更新</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            label="优先级"
            name="priority"
          >
            <InputNumber min={0} max={100} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            label="状态"
            name="isActive"
            valuePropName="checked"
          >
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                {editingItem ? '更新' : '发布'}
              </Button>
              <Button onClick={handleCancel}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Announcements;
