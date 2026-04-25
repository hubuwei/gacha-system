import React, { useState } from 'react';
import { Card, Form, InputNumber, Button, message, Space } from 'antd';
import { SendOutlined, ReloadOutlined } from '@ant-design/icons';
import request from '../../utils/request';

const DiscountNotification = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  // 自动计算现价
  const handleOldPriceChange = (value) => {
    const discountRate = form.getFieldValue('discountRate');
    if (value && discountRate !== undefined && discountRate !== null) {
      const newPrice = (value * (100 - discountRate) / 100).toFixed(2);
      form.setFieldValue('newPrice', parseFloat(newPrice));
    }
  };

  // 自动计算折扣率
  const handleNewPriceChange = (value) => {
    const oldPrice = form.getFieldValue('oldPrice');
    if (oldPrice && value && oldPrice > 0) {
      const discountRate = Math.round((1 - value / oldPrice) * 100);
      form.setFieldValue('discountRate', discountRate);
    }
  };

  // 根据折扣率计算现价
  const handleDiscountRateChange = (value) => {
    const oldPrice = form.getFieldValue('oldPrice');
    if (oldPrice && value !== undefined && value !== null) {
      const newPrice = (oldPrice * (100 - value) / 100).toFixed(2);
      form.setFieldValue('newPrice', parseFloat(newPrice));
    }
  };

  const handleSubmit = async (values) => {
    setLoading(true);
    try {
      const requestData = {
        gameId: values.gameId,
        oldPrice: values.oldPrice,
        newPrice: values.newPrice,
        discountRate: values.discountRate
      };

      // TODO: 后端需要提供这个接口
      const response = await request.post('/admin/notifications/send-discount-notification', requestData);

      if (response.code === 200) {
        message.success(response.message || '折扣通知发送成功！');
        form.resetFields();
      } else {
        message.error(response.message || '发送失败');
      }
    } catch (error) {
      console.error('发送失败:', error);
      message.error('网络错误：' + (error.message || '后端接口尚未实现，请联系开发人员'));
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    form.resetFields();
  };

  return (
    <div style={{ padding: '24px' }}>
      <Card title="🎮 愿望单折扣通知" bordered={false}>
        <div style={{ marginBottom: 24, padding: 12, background: '#e7f3ff', borderRadius: 4, borderLeft: '4px solid #2196F3' }}>
          💡 提示：系统会自动检测游戏折扣，并向愿望单中该游戏的用户发送精美的HTML格式邮件通知。
        </div>

        <div style={{ marginBottom: 24, padding: 12, background: '#fff3cd', borderRadius: 4, borderLeft: '4px solid #ffc107', color: '#856404' }}>
          ⚠️ 注意：此功能需要后端支持，目前通过修改游戏价格自动触发。如需手动测试，请联系开发人员。
        </div>

        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Form.Item
            label="游戏ID"
            name="gameId"
            rules={[{ required: true, message: '请输入游戏ID' }]}
          >
            <InputNumber
              placeholder="例如：1（黑神话:悟空）"
              min={1}
              style={{ width: '100%' }}
              size="large"
            />
          </Form.Item>

          <Form.Item
            label="原价（元）"
            name="oldPrice"
            rules={[{ required: true, message: '请输入原价' }]}
          >
            <InputNumber
              placeholder="例如：298.00"
              step={0.01}
              min={0}
              style={{ width: '100%' }}
              size="large"
              onChange={handleOldPriceChange}
            />
          </Form.Item>

          <Form.Item
            label="现价（元）"
            name="newPrice"
            rules={[{ required: true, message: '请输入现价' }]}
          >
            <InputNumber
              placeholder="例如：198.00"
              step={0.01}
              min={0}
              style={{ width: '100%' }}
              size="large"
              onChange={handleNewPriceChange}
            />
          </Form.Item>

          <Form.Item
            label="折扣率（%）"
            name="discountRate"
            rules={[{ required: true, message: '请输入折扣率' }]}
          >
            <InputNumber
              placeholder="例如：33（表示33%折扣）"
              min={0}
              max={100}
              style={{ width: '100%' }}
              size="large"
              onChange={handleDiscountRateChange}
            />
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
                📧 发送折扣通知
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

        <div style={{ marginTop: 30, paddingTop: 20, borderTop: '2px solid #e0e0e0' }}>
          <h3 style={{ marginBottom: 15, color: '#333' }}>📋 使用说明</h3>
          <ol style={{ color: '#666', lineHeight: 1.8, paddingLeft: 20 }}>
            <li>填写游戏ID、原价、现价和折扣率</li>
            <li>点击"发送折扣通知"按钮</li>
            <li>系统会查找所有将该游戏加入愿望单的用户</li>
            <li>向这些用户发送精美的Steam风格HTML邮件</li>
            <li>邮件中包含游戏信息、价格对比和购买链接</li>
          </ol>
        </div>
      </Card>
    </div>
  );
};

export default DiscountNotification;
