import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { MALL_API_BASE } from '../App'

function OrderDetail({ currentUser }) {
  const { orderId } = useParams()
  const navigate = useNavigate()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (currentUser && orderId) {
      fetchOrderDetail()
    }
  }, [currentUser, orderId])

  const fetchOrderDetail = async () => {
    try {
      setLoading(true)
      const response = await fetch(`${MALL_API_BASE}/orders/${orderId}?userId=${currentUser.id}`)
      const result = await response.json()

      if (result.code === 200 && result.data) {
        console.log('订单详情数据:', result.data)
        console.log('订单商品:', result.data.items)
        if (result.data.items && result.data.items.length > 0) {
          console.log('第一个商品字段:', Object.keys(result.data.items[0]))
        }
        setOrder(result.data)
      } else {
        alert(result.message || '获取订单详情失败')
      }
    } catch (error) {
      console.error('获取订单详情失败:', error)
      alert('获取订单详情失败：' + error.message)
    } finally {
      setLoading(false)
    }
  }

  const handleCancelOrder = async () => {
    if (!confirm('确定要取消这个订单吗？')) return

    try {
      console.log('[取消订单] orderId:', orderId, 'userId:', currentUser?.id)
      
      const response = await fetch(`${MALL_API_BASE}/orders/${orderId}/cancel?userId=${currentUser.id}`, {
        method: 'POST'
      })
      
      console.log('[取消订单] 响应状态:', response.status)
      const result = await response.json()
      console.log('[取消订单] 响应数据:', result)
      
      if (result.code === 200) {
        window.dispatchEvent(new CustomEvent('showSuccess', {
          detail: { title: '订单已取消', message: '订单已成功取消', duration: 2000 }
        }))
        fetchOrderDetail()
      } else {
        window.dispatchEvent(new CustomEvent('showError', {
          detail: { title: '取消失败', message: result.message || '取消订单失败', duration: 3000 }
        }))
      }
    } catch (error) {
      console.error('[取消订单] 错误:', error)
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

  const getPaymentMethodText = (method) => {
    const methodMap = {
      'balance': '余额支付',
      'alipay': '支付宝',
      'wechat': '微信支付'
    }
    return methodMap[method] || method
  }

  if (loading) {
    return <div className="order-detail-loading">加载中...</div>
  }

  if (!order) {
    return (
      <div className="order-not-found">
        <h2>订单不存在</h2>
        <button onClick={() => navigate('/orders')}>返回订单列表</button>
      </div>
    )
  }

  return (
    <div className="order-detail-page">
      <div className="page-header">
        <h1>📋 订单详情</h1>
        <button className="back-btn" onClick={() => navigate('/orders')}>
          ← 返回订单列表
        </button>
      </div>

      <div className="order-detail-content">
        {/* 订单状态卡片 */}
        <div className="status-card">
          <div className="status-icon">
            {order.orderStatus === 'completed' ? '✅' : 
             order.orderStatus === 'pending' ? '⏳' :
             order.orderStatus === 'cancelled' ? '❌' : '✓'}
          </div>
          <div className="status-info">
            <h2 className="status-text">{getStatusText(order.orderStatus)}</h2>
            <p className="order-no">订单号: {order.orderNo}</p>
            <p className="order-time">
              下单时间: {new Date(order.createdAt).toLocaleString('zh-CN')}
            </p>
          </div>
        </div>

        {/* 商品信息 */}
        <div className="detail-section">
          <h3>🎮 商品信息</h3>
          <div className="detail-items">
            {order.items?.map((item, idx) => {
              const gameTitle = item.gameTitle || item.title || item.name || item.gameName || '未知游戏'
              const gameCover = item.gameCover || item.cover || item.image || item.gameImage || '🎮'
              return (
                <div key={idx} className="detail-item">
                  <div className="item-image">{gameCover}</div>
                  <div className="item-info">
                    <h4 className="item-name">{gameTitle}</h4>
                    <div className="item-meta">
                      <span>数量: {item.quantity}</span>
                      {item.discountRate > 0 && (
                        <span className="discount-tag">-{item.discountRate}%</span>
                      )}
                    </div>
                  </div>
                  <div className="item-prices">
                    {item.originalPrice !== item.actualPrice && (
                      <div className="original-price">¥{item.originalPrice?.toFixed(2)}</div>
                    )}
                    <div className="actual-price">¥{item.actualPrice?.toFixed(2)}</div>
                  </div>
                </div>
              )
            })}
          </div>
        </div>

        {/* 订单信息 */}
        <div className="detail-section">
          <h3>📝 订单信息</h3>
          <div className="info-grid">
            <div className="info-row">
              <span className="info-label">支付方式:</span>
              <span className="info-value">{getPaymentMethodText(order.paymentMethod)}</span>
            </div>
            
            {order.paymentTime && (
              <div className="info-row">
                <span className="info-label">支付时间:</span>
                <span className="info-value">
                  {new Date(order.paymentTime).toLocaleString('zh-CN')}
                </span>
              </div>
            )}
            
            <div className="info-row">
              <span className="info-label">商品总额:</span>
              <span className="info-value">
                ¥{(order.totalAmount + order.discountAmount).toFixed(2)}
              </span>
            </div>
            
            {order.discountAmount > 0 && (
              <div className="info-row discount">
                <span className="info-label">优惠金额:</span>
                <span className="info-value">-¥{order.discountAmount.toFixed(2)}</span>
              </div>
            )}
            
            <div className="info-row total">
              <span className="info-label">实付金额:</span>
              <span className="info-value highlight">
                ¥{order.actualAmount.toFixed(2)}
              </span>
            </div>
          </div>
        </div>

        {/* 操作按钮 */}
        <div className="detail-actions">
          {order.orderStatus === 'pending' && (
            <>
              <button 
                className="action-btn primary"
                onClick={() => {
                  // 检查是否已过期
                  const createTime = new Date(order.createdAt).getTime()
                  const expireTime = createTime + 15 * 60 * 1000
                  const remaining = expireTime - Date.now()
                  
                  if (remaining <= 0) {
                    window.dispatchEvent(new CustomEvent('showError', {
                      detail: { 
                        title: '订单已过期', 
                        message: '该订单已超过15分钟，已被自动取消', 
                        duration: 3000 
                      }
                    }))
                    // 刷新订单状态
                    fetchOrderDetail()
                    return
                  }
                  
                  // TODO: 实现订单支付功能
                  window.dispatchEvent(new CustomEvent('showInfo', {
                    detail: { 
                      title: '提示', 
                      message: '请前往购物车重新结算或使用余额支付', 
                      duration: 3000 
                    }
                  }))
                }}
              >
                💳 立即支付
              </button>
              <button 
                className="action-btn secondary"
                onClick={handleCancelOrder}
              >
                取消订单
              </button>
            </>
          )}
          
          {order.orderStatus === 'completed' && (
            <button 
              className="action-btn primary"
              onClick={() => navigate('/library')}
            >
              🎮 查看我的游戏库
            </button>
          )}
          
          <button 
            className="action-btn outline"
            onClick={() => window.print()}
          >
            🖨️ 打印订单
          </button>
        </div>
      </div>
    </div>
  )
}

export default OrderDetail
