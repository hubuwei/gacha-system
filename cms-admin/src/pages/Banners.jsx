import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Space, Modal, Form, Input, Upload, message, Switch, Image } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, UploadOutlined } from '@ant-design/icons';
import request from '../utils/request';

const Banners = () => {
  const [loading, setLoading] = useState(false);
  const [banners, setBanners] = useState([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingBanner, setEditingBanner] = useState(null);
  const [form] = Form.useForm();

  // 获取Banner列表
  const fetchBanners = async () => {
    try {
      setLoading(true);
      const response = await request.get('/cms/banners');
      if (response.code === 200) {
        setBanners(response.data || []);
      }
    } catch (error) {
      message.error('获取Banner列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBanners();
  }, []);

  // 打开新增/编辑弹窗
  const showModal = (banner = null) => {
    setEditingBanner(banner);
    if (banner) {
      form.setFieldsValue({
        title: banner.title,
        imageUrl: banner.imageUrl,
        linkUrl: banner.linkUrl,
        sortOrder: banner.sortOrder,
        isActive: banner.isActive,
      });
    } else {
      form.resetFields();
      form.setFieldsValue({
        sortOrder: 0,
        isActive: true,
      });
    }
    setModalVisible(true);
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      if (editingBanner) {
        // 编辑
        const response = await request.put(`/cms/banners/${editingBanner.id}`, values);
        if (response.code === 200) {
          message.success('更新成功');
          setModalVisible(false);
          fetchBanners();
        }
      } else {
        // 新增
        const response = await request.post('/cms/banners', values);
        if (response.code === 200) {
          message.success('创建成功');
          setModalVisible(false);
          fetchBanners();
        }
      }
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 删除Banner
  const handleDelete = (id) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这个Banner吗？',
      onOk: async () => {
        try {
          const response = await request.delete(`/cms/banners/${id}`);
          if (response.code === 200) {
            message.success('删除成功');
            fetchBanners();
          }
        } catch (error) {
          message.error('删除失败');
        }
      },
    });
  };

  // 切换状态
  const handleToggleStatus = async (id, currentStatus) => {
    try {
      const response = await request.put(`/cms/banners/${id}/status`, {
        isActive: !currentStatus,
      });
      if (response.code === 200) {
        message.success('状态已更新');
        fetchBanners();
      }
    } catch (error) {
      message.error('操作失败');
    }
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '预览',
      dataIndex: 'imageUrl',
      key: 'imageUrl',
      width: 200,
      render: (imageUrl) => (
        <Image src={imageUrl} alt="Banner" width={150} height={60} style={{ objectFit: 'cover' }} />
      ),
    },
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
    },
    {
      title: '链接',
      dataIndex: 'linkUrl',
      key: 'linkUrl',
      ellipsis: true,
    },
    {
      title: '排序',
      dataIndex: 'sortOrder',
      key: 'sortOrder',
      width: 100,
    },
    {
      title: '状态',
      dataIndex: 'isActive',
      key: 'isActive',
      width: 100,
      render: (isActive) => (
        <Switch checked={isActive} disabled size="small" />
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => showModal(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record.id)}
          >
            删除
          </Button>
          <Button
            type="link"
            onClick={() => handleToggleStatus(record.id, record.isActive)}
          >
            {record.isActive ? '下架' : '上架'}
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>首页Banner配置</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => showModal()}>
          新增Banner
        </Button>
      </div>
      
      <Card>
        <Table
          dataSource={banners}
          columns={columns}
          rowKey="id"
          loading={loading}
          pagination={false}
        />
      </Card>

      {/* 新增/编辑弹窗 */}
      <Modal
        title={editingBanner ? '编辑Banner' : '新增Banner'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="title"
            label="标题"
            rules={[{ required: true, message: '请输入标题' }]}
          >
            <Input placeholder="请输入Banner标题" />
          </Form.Item>

          <Form.Item
            name="imageUrl"
            label="图片URL"
            rules={[{ required: true, message: '请输入图片URL' }]}
          >
            <Input placeholder="请输入图片URL或上传文件" />
          </Form.Item>

          <Form.Item label="上传图片">
            <Upload action="/api/cms/upload" listType="picture">
              <Button icon={<UploadOutlined />}>点击上传</Button>
            </Upload>
          </Form.Item>

          <Form.Item name="linkUrl" label="跳转链接">
            <Input placeholder="请输入跳转链接（可选）" />
          </Form.Item>

          <Form.Item name="sortOrder" label="排序">
            <Input type="number" placeholder="数字越小越靠前" />
          </Form.Item>

          <Form.Item name="isActive" label="是否启用" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Banners;
