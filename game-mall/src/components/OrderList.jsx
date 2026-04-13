import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { MALL_API_BASE } from '../App'

function OrderList({ currentUser }) {
  const navigate = useNavigate()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('all') // all, pending, paid, completed, cancelled
  const [countdowns, setCountdowns] = useState({}) // 存储每个订单的倒计时

  useEffect(() => {
    if (currentUser) {
      fetchOrders()
    }
  }, [currentUser, filter])
  
  // 倒计时定时器 - 仅用于显示，不再过滤订单
  useEffect(() => {
    const timer = setInterval(() => {
      updateCountdowns()
    }, 1000)
    
    return () => clearInterval(timer)
  }, [orders])
  
  // 更新倒计时（仅用于UI显示）
  const updateCountdowns = () => {
    const newCountdowns = {}
    
    orders.forEach(order => {
      if (order.orderStatus === 'pending') {
        const createTime = new Date(order.createdAt).getTime()
        const expireTime = createTime + 15 * 60 * 1000 // 15分钟后过期
        const remaining = expireTime - Date.now()
        
        if (remaining > 0) {
          const minutes = Math.floor(remaining / 60000)
          const seconds = Math.floor((remaining % 60000) / 1000)
          newCountdowns[order.id] = `${minutes}:${seconds.toString().padStart(2, '0')}`
        }
      }
    })
    
    setCountdowns(newCountdowns)
  }

  const fetchOrders = async () => {
    if (!currentUser) return

    try {
      setLoading(true)
      const response = await fetch(`${MALL_API_BASE}/orders?userId=${currentUser.id}&status=${filter}`)
      const result = await response.json()
      
      if (result.code === 200 && result.data) {
        setOrders(result.data)
      } else {
        console.warn('获取订单失败:', result.message)
        setOrders([])
      }
    } catch (error) {
      console.error('获取订单列表失败:', error)
      setOrders([])
    } finally {
      setLoading(false)
    }
  }

  const handleCancelOrder = async (orderId) => {
    if (!window.confirm('确定要取消这个订单吗？')) return

    try {
      const response = await fetch(`${MALL_API_BASE}/orders/${orderId}/cancel?userId=${currentUser.id}`, {
        method: 'POST'
      })
      const result = await response.json()
      
      if (result.code === 200) {
        // 使用自定义通知
        window.dispatchEvent(new CustomEvent('showSuccess', {
          detail: { title: '订单已取消', message: '订单已成功取消', duration: 2000 }
        }))
        fetchOrders()
      } else {
        window.dispatchEvent(new CustomEvent('showError', {
          detail: { title: '取消失败', message: result.message || '取消订单失败', duration: 3000 }
        }))
      }
    } catch (error) {
      window.dispatchEvent(new CustomEvent('showError', {
        detail: { title: '取消失败', message: error.message, duration: 3000 }
      }))
    }
  }

  const getStatusText = (status) => {
    const statusMap = {
      'pending': '待支付',
      'paid': '已支付',
      'completed': '已完成',
      'cancelled': '已取消',
      'refunded': '已退款'
    }
    return statusMap[status] || status
  }

  const getStatusClass = (status) => {
    const classMap = {
      'pending': 'status-pending',
      'paid': 'status-paid',
      'completed': 'status-completed',
      'cancelled': 'status-cancelled',
      'refunded': 'status-refunded'
    }
    return classMap[status] || ''
  }

  // 未登录提示
  if (!currentUser) {
    return (
      <div className="order-list-page">
        <div className="page-header">
          <h1>📦 我的订单</h1>
          <button className="back-btn" onClick={() => navigate(-1)}>
            ← 返回
          </button>
        </div>
        <div className="empty-orders">
          <div className="empty-icon">🔒</div>
          <h2>请先登录</h2>
          <p>登录后即可查看您的订单记录</p>
          <button className="browse-btn" onClick={() => navigate('/')}>
            去首页登录
          </button>
        </div>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="order-list-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>加载中...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="order-list-page">
      <div className="page-header">
        <h1>📦 我的订单</h1>
        <button className="back-btn" onClick={() => navigate('/')}>
          ← 返回首页
        </button>
      </div>

      {/* 筛选标签 */}
      <div className="order-filters">
        <button 
          className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
          onClick={() => setFilter('all')}
        >
          全部
        </button>
        <button 
          className={`filter-btn ${filter === 'pending' ? 'active' : ''}`}
          onClick={() => setFilter('pending')}
        >
          待支付
        </button>
        <button 
          className={`filter-btn ${filter === 'paid' ? 'active' : ''}`}
          onClick={() => setFilter('paid')}
        >
          已支付
        </button>
        <button 
          className={`filter-btn ${filter === 'completed' ? 'active' : ''}`}
          onClick={() => setFilter('completed')}
        >
          已完成
        </button>
        <button 
          className={`filter-btn ${filter === 'cancelled' ? 'active' : ''}`}
          onClick={() => setFilter('cancelled')}
        >
          已取消
        </button>
      </div>

      {/* 订单列表 */}
      {orders.length === 0 ? (
        <div className="empty-orders">
          <div className="empty-icon">📦</div>
          <h2>暂无订单</h2>
          <p>快去选购心仪的游戏吧！</p>
          <button className="browse-btn" onClick={() => navigate('/')}>
            去逛逛
          </button>
        </div>
      ) : (
        <div className="orders-container">
          {orders.map((order) => (
            <div key={order.id} className="order-card">
              <div className="order-header">
                <div className="order-info">
                  <span className="order-no">订单号: {order.orderNo}</span>
                  <span className="order-date">
                    {new Date(order.createdAt).toLocaleString('zh-CN')}
                  </span>
                  {order.orderStatus === 'pending' && countdowns[order.id] && (
                    <span className="order-countdown">
                      ⏰ 剩余支付时间: <strong>{countdowns[order.id]}</strong>
                    </span>
                  )}
                </div>
                <span className={`order-status ${getStatusClass(order.orderStatus)}`}>
                  {getStatusText(order.orderStatus)}
                </span>
              </div>

              <div className="order-items">
                {order.items?.map((item, idx) => {
                  const gameTitle = item.gameTitle || item.title || item.name || item.gameName || '未知游戏'
                  const gameCover = item.gameCover || item.cover || item.image || item.gameImage || '🎮'
                  return (
                    <div key={idx} className="order-item">
                      <div className="item-image">{gameCover}</div>
                      <div className="item-info">
                        <h4 className="item-name">{gameTitle}</h4>
                        <span className="item-quantity">x{item.quantity}</span>
                      </div>
                      <div className="item-price">¥{item.actualPrice?.toFixed(2)}</div>
                    </div>
                  )
                })}
              </div>

              <div className="order-footer">
                <div className="order-total">
                  <span className="total-label">实付金额:</span>
                  <span className="total-amount">¥{order.actualAmount.toFixed(2)}</span>
                  {order.discountAmount > 0 && (
                    <span className="discount-info">
                      (优惠 ¥{order.discountAmount.toFixed(2)})
                    </span>
                  )}
                </div>
                
                <div className="order-actions">
                  <button 
                    className="action-btn detail"
                    onClick={() => navigate(`/orders/${order.id}`)}
                  >
                    查看详情
                  </button>
                  
                  {order.orderStatus === 'pending' && (
                    <>
                      <button 
                        className="action-btn pay"
                        onClick={() => navigate(`/orders/${order.id}/pay`)}
                      >
                        立即支付
                      </button>
                      <button 
                        className="action-btn cancel"
                        onClick={() => handleCancelOrder(order.id)}
                      >
                        取消订单
                      </button>
                    </>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default OrderList
