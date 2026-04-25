import React, { useState } from 'react';
import { Card, Form, Input, InputNumber, Radio, Button, message, Space } from 'antd';
import { SendOutlined, ReloadOutlined } from '@ant-design/icons';
import request from '../../utils/request';

const BroadcastNotification = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (values) => {
    setLoading(true);
    try {
      const requestData = {
        title: values.title,
        content: values.content,
        gameId: values.gameId || null,
        type: 'promotion'
      };

      // 根据范围选择接口
      const endpoint = values.scope === 'all' 
        ? '/admin/notifications/broadcast-promotion'
        : '/admin/notifications/broadcast-to-email-users';

      const response = await request.post(endpoint, requestData);

      if (response.code === 200) {
        message.success(response.message || '广播发送成功！');
        form.resetFields();
      } else {
        message.error(response.message || '发送失败');
      }
    } catch (error) {
      console.error('发送失败:', error);
      message.error('网络错误：' + (error.message || '请稍后重试'));
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    form.resetFields();
  };

  return (
    <div style={{ padding: '24px' }}>
      <Card title="📢 广播通知" bordered={false}>
        <div style={{ marginBottom: 24, padding: 12, background: '#e7f3ff', borderRadius: 4, borderLeft: '4px solid #2196F3' }}>
          💡 提示：广播消息会通过RabbitMQ异步发送，用户将在铃铛图标中看到通知。
        </div>

        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            scope: 'all'
          }}
        >
          <Form.Item
            label="通知标题"
            name="title"
            rules={[{ required: true, message: '请输入通知标题' }]}
          >
            <Input placeholder="例如：🎉 春季大促开启！" size="large" />
          </Form.Item>

          <Form.Item
            label="通知内容"
            name="content"
            rules={[{ required: true, message: '请输入通知内容' }]}
          >
            <Input.TextArea
              placeholder="例如：全场游戏低至3折，限时7天！快来选购心仪的游戏吧~"
              rows={4}
              size="large"
            />
          </Form.Item>

          <Form.Item
            label="关联游戏ID（可选）"
            name="gameId"
          >
            <InputNumber
              placeholder="输入游戏ID，点击通知可跳转到该游戏"
              min={1}
              style={{ width: '100%' }}
              size="large"
            />
          </Form.Item>

          <Form.Item
            label="广播范围"
            name="scope"
            rules={[{ required: true, message: '请选择广播范围' }]}
          >
            <Radio.Group>
              <Space direction="vertical">
                <Radio value="all">所有用户</Radio>
                <Radio value="email">仅邮箱用户</Radio>
              </Space>
            </Radio.Group>
          </Form.Item>

          <Form.Item>
            <Space size="middle">
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                icon={<SendOutlined />}
                size="large"
              >
                🚀 发送广播
              </Button>
              <Button
                onClick={handleReset}
                icon={<ReloadOutlined />}
                size="large"
              >
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default BroadcastNotification;
