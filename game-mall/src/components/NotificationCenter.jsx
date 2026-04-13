import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { MALL_API_BASE } from '../App'
import './NotificationCenter.css'

function NotificationCenter({ currentUser }) {
  const navigate = useNavigate()
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('all') // all, unread, read
  const [expandedId, setExpandedId] = useState(null) // 展开的通知ID

  // 获取通知列表
  const fetchNotifications = async () => {
    if (!currentUser) return
    
    try {
      setLoading(true)
      const token = localStorage.getItem('token')
      const unreadOnly = filter === 'unread'
      
      const response = await fetch(
        `${MALL_API_BASE}/notifications?userId=${currentUser.id}&unreadOnly=${unreadOnly}`,
        {
          headers: { 
            'Authorization': `Bearer ${token}`
          }
        }
      )
      const result = await response.json()
      
      if (result.code === 200 && result.data) {
        console.log('[NotificationCenter] 收到的通知数据:', result.data.list)
        setNotifications(result.data.list || [])
      }
    } catch (error) {
      console.error('获取通知失败:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchNotifications()
  }, [currentUser, filter])

  // 标记为已读
  const markAsRead = async (notificationId) => {
    try {
      const token = localStorage.getItem('token')
      await fetch(`${MALL_API_BASE}/notifications/${notificationId}/read`, {
        method: 'PUT',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      })
      
      // 更新本地状态
      setNotifications(prev => 
        prev.map(n => n.id === notificationId ? { ...n, isRead: true } : n)
      )
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  // 标记所有为已读
  const markAllAsRead = async () => {
    try {
      const token = localStorage.getItem('token')
      await fetch(`${MALL_API_BASE}/notifications/mark-all-read?userId=${currentUser.id}`, {
        method: 'PUT',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      })
      
      // 更新本地状态
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })))
    } catch (error) {
      console.error('全部标记已读失败:', error)
    }
  }

  // 删除通知
  const deleteNotification = async (notificationId) => {
    if (!confirm('确定要删除这条通知吗？')) return
    
    try {
      const token = localStorage.getItem('token')
      await fetch(`${MALL_API_BASE}/notifications/${notificationId}`, {
        method: 'DELETE',
        headers: { 
          'Authorization': `Bearer ${token}`
        }
      })
      
      // 更新本地状态
      setNotifications(prev => prev.filter(n => n.id !== notificationId))
    } catch (error) {
      console.error('删除通知失败:', error)
    }
  }

  // 切换展开/收起
  const toggleExpand = (notificationId, e) => {
    e.stopPropagation()
    e.preventDefault() // 防止触发父元素的点击事件
    setExpandedId(expandedId === notificationId ? null : notificationId)
  }

  // 跳转到相关页面
  const handleNotificationClick = (notification) => {
    // 标记为已读
    if (!notification.isRead) {
      markAsRead(notification.id)
    }
    
    // 根据类型跳转
    if (notification.relatedGameId) {
      navigate(`/games/${notification.relatedGameId}`)
    } else if (notification.relatedOrderId) {
      navigate(`/orders/${notification.relatedOrderId}`)
    }
  }

  // 格式化时间
  const formatTime = (dateString) => {
    const date = new Date(dateString)
    const now = new Date()
    const diff = now - date
    
    // 小于1分钟
    if (diff < 60000) {
      return '刚刚'
    }
    // 小于1小时
    if (diff < 3600000) {
      return `${Math.floor(diff / 60000)}分钟前`
    }
    // 小于24小时
    if (diff < 86400000) {
      return `${Math.floor(diff / 3600000)}小时前`
    }
    // 小于7天
    if (diff < 604800000) {
      return `${Math.floor(diff / 86400000)}天前`
    }
    // 否则显示完整日期
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  // 获取通知图标
  const getNotificationIcon = (type) => {
    switch (type) {
      case 'promotion': return '🎉'
      case 'system': return '⚙️'
      case 'order': return '📦'
      default: return '🔔'
    }
  }

  if (!currentUser) {
    return (
      <div className="notification-center">
        <div className="empty-state">
          <h2>请先登录</h2>
          <button onClick={() => navigate('/')}>返回首页</button>
        </div>
      </div>
    )
  }

  return (
    <div className="notification-center">
      <div className="page-header">
        <h1>📬 消息中心</h1>
        <button className="back-btn" onClick={() => navigate(-1)}>
          ← 返回
        </button>
      </div>

      <div className="nc-toolbar">
        <div className="filter-tabs">
          <button 
            className={`tab ${filter === 'all' ? 'active' : ''}`}
            onClick={() => setFilter('all')}
          >
            全部
          </button>
          <button 
            className={`tab ${filter === 'unread' ? 'active' : ''}`}
            onClick={() => setFilter('unread')}
          >
            未读
          </button>
          <button 
            className={`tab ${filter === 'read' ? 'active' : ''}`}
            onClick={() => setFilter('read')}
          >
            已读
          </button>
        </div>
        
        {notifications.some(n => !n.isRead) && (
          <button className="mark-all-read-btn" onClick={markAllAsRead}>
            ✓ 全部标记为已读
          </button>
        )}
      </div>

      <div className="nc-content">
        {loading ? (
          <div className="loading">加载中...</div>
        ) : notifications.length === 0 ? (
          <div className="empty-notifications">
            <div className="empty-icon">📭</div>
            <p>暂无消息</p>
          </div>
        ) : (
          <div className="notification-list">
            {notifications.map((notification) => {
              const isExpanded = expandedId === notification.id
              // 尝试多个可能的字段名
              const messageText = notification.message || notification.content || notification.body || ''
              const isLongMessage = messageText && messageText.length > 100
              
              console.log('[NotificationCenter] 渲染通知:', {
                id: notification.id,
                title: notification.title,
                message: messageText,
                messageLength: messageText.length,
                isLongMessage,
                hasExpandBtn: isLongMessage,
                allKeys: Object.keys(notification)
              })
              
              return (
                <div 
                  key={notification.id} 
                  className={`notification-item ${!notification.isRead ? 'unread' : ''} ${isExpanded ? 'expanded' : ''}`}
                  onClick={() => handleNotificationClick(notification)}
                >
                  <div className="notification-icon">
                    {getNotificationIcon(notification.type)}
                  </div>
                  <div className="notification-body">
                    <div className="notification-header">
                      <div className="notification-title">{notification.title}</div>
                      <div className="notification-time">
                        {formatTime(notification.createdAt)}
                      </div>
                    </div>
                    
                    {/* 消息内容 */}
                    <div className={`notification-message ${isExpanded ? 'full' : 'preview'}`}>
                      {messageText || '暂无内容'}
                    </div>
                    
                    {/* 展开/收起按钮 - 只有长消息才显示 */}
                    {isLongMessage && (
                      <button 
                        className="expand-btn"
                        onClick={(e) => toggleExpand(notification.id, e)}
                      >
                        {isExpanded ? '▲ 收起' : '▼ 展开'}
                      </button>
                    )}
                  </div>
                  <div className="notification-actions">
                    {!notification.isRead && <div className="unread-dot"></div>}
                    <button 
                      className="delete-btn"
                      onClick={(e) => {
                        e.stopPropagation()
                        deleteNotification(notification.id)
                      }}
                      title="删除"
                    >
                      🗑️
                    </button>
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}

export default NotificationCenter
