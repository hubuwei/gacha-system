import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Tag, Space, Modal, message, Input } from 'antd';
import { CheckOutlined, CloseOutlined, EyeOutlined } from '@ant-design/icons';
import request from '../utils/request';

const { TextArea } = Input;

const Reviews = () => {
  const [loading, setLoading] = useState(false);
  const [reviews, setReviews] = useState([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [selectedReview, setSelectedReview] = useState(null);
  const [modalVisible, setModalVisible] = useState(false);

  // 获取评论列表
  const fetchReviews = async (status = null) => {
    try {
      setLoading(true);
      const params = { page, size: pageSize };
      if (status !== null) {
        params.status = status;
      }
      
      // request baseURL已包含 /api/cms，所以只需 /reviews
      const response = await request.get('/reviews', { params });
      if (response.code === 200) {
        setReviews(response.data.list || []);
        setTotal(response.data.total || 0);
      }
    } catch (error) {
      message.error('获取评论列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReviews();
  }, [page, pageSize]);

  // 审核通过
  const handleApprove = async (id) => {
    try {
      const response = await request.put(`/reviews/${id}/approve`);
      if (response.code === 200) {
        message.success('审核通过');
        fetchReviews();
      }
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 审核拒绝
  const handleReject = async (id) => {
    Modal.confirm({
      title: '拒绝评论',
      content: '请输入拒绝原因',
      input: <TextArea placeholder="请输入拒绝原因" />,
      onOk: async (value) => {
        try {
          const response = await request.put(`/reviews/${id}/reject`, {
            reason: value,
          });
          if (response.code === 200) {
            message.success('已拒绝');
            fetchReviews();
          }
        } catch (error) {
          message.error('操作失败');
        }
      },
    });
  };

  // 查看详情
  const showDetail = (review) => {
    setSelectedReview(review);
    setModalVisible(true);
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '用户',
      dataIndex: 'username',
      key: 'username',
      width: 120,
    },
    {
      title: '游戏',
      dataIndex: 'gameTitle',
      key: 'gameTitle',
      width: 150,
    },
    {
      title: '评分',
      dataIndex: 'rating',
      key: 'rating',
      width: 100,
      render: (rating) => (
        <Tag color={rating >= 4 ? 'green' : rating >= 3 ? 'orange' : 'red'}>
          {rating} 星
        </Tag>
      ),
    },
    {
      title: '评论内容',
      dataIndex: 'content',
      key: 'content',
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status) => {
        const statusMap = {
          pending: { color: 'orange', text: '待审核' },
          approved: { color: 'green', text: '已通过' },
          rejected: { color: 'red', text: '已拒绝' },
        };
        const config = statusMap[status] || { color: 'default', text: status };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => showDetail(record)}
          >
            查看
          </Button>
          {record.status === 'pending' && (
            <>
              <Button
                type="link"
                icon={<CheckOutlined />}
                style={{ color: '#52c41a' }}
                onClick={() => handleApprove(record.id)}
              >
                通过
              </Button>
              <Button
                type="link"
                icon={<CloseOutlined />}
                danger
                onClick={() => handleReject(record.id)}
              >
                拒绝
              </Button>
            </>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <h2>评论内容审核</h2>
      <Card>
        <Table
          dataSource={reviews}
          columns={columns}
          rowKey="id"
          loading={loading}
          pagination={{
            current: page,
            pageSize,
            total,
            onChange: (p, ps) => {
              setPage(p);
              setPageSize(ps);
            },
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条评论`,
          }}
        />
      </Card>

      {/* 详情弹窗 */}
      <Modal
        title="评论详情"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={600}
      >
        {selectedReview && (
          <div>
            <p><strong>用户：</strong>{selectedReview.username}</p>
            <p><strong>游戏：</strong>{selectedReview.gameTitle}</p>
            <p><strong>评分：</strong>{selectedReview.rating} 星</p>
            <p><strong>状态：</strong>
              <Tag color={
                selectedReview.status === 'pending' ? 'orange' :
                selectedReview.status === 'approved' ? 'green' : 'red'
              }>
                {selectedReview.status === 'pending' ? '待审核' :
                 selectedReview.status === 'approved' ? '已通过' : '已拒绝'}
              </Tag>
            </p>
            <p><strong>评论内容：</strong></p>
            <div style={{ 
              padding: 12, 
              background: '#f5f5f5', 
              borderRadius: 4,
              whiteSpace: 'pre-wrap'
            }}>
              {selectedReview.content}
            </div>
            <p style={{ marginTop: 16 }}>
              <strong>创建时间：</strong>{selectedReview.createTime}
            </p>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default Reviews;
