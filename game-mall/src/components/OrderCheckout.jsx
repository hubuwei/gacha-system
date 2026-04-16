import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { MALL_API_BASE, AUTH_API_BASE } from '../App'

function OrderCheckout({ currentUser, userBalance: propUserBalance, onOrderSuccess }) {
  const navigate = useNavigate()
  const [cartItems, setCartItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [paymentMethod, setPaymentMethod] = useState('balance') // balance, alipay, wechat
  const [processing, setProcessing] = useState(false)
  const [showQRCode, setShowQRCode] = useState(false)
  const [orderCreated, setOrderCreated] = useState(null)
  const [isMockMode, setIsMockMode] = useState(false)
  const [localUserBalance, setLocalUserBalance] = useState(0)
  
  // 使用传入的余额或本地状态
  const userBalance = propUserBalance !== undefined && propUserBalance > 0 ? propUserBalance : localUserBalance
  
  // 获取用户余额
  useEffect(() => {
    if (currentUser) {
      console.log('[OrderCheckout] 开始获取余额, currentUser:', currentUser)
      fetchUserBalance()
    }
  }, [currentUser])
  
  const fetchUserBalance = async () => {
    if (!currentUser) {
      console.warn('[OrderCheckout] currentUser 为空，无法获取余额')
      return
    }
    try {
      console.log('[OrderCheckout] 请求余额接口, userId:', currentUser.id)
      const response = await fetch(`${MALL_API_BASE}/wallet/balance?userId=${currentUser.id}`)
      console.log('[OrderCheckout] 响应状态:', response.status)
      const result = await response.json()
      console.log('[OrderCheckout] 响应数据:', result)
      
      if (result.code === 200 && result.data) {
        const balance = result.data.balance || 0
        console.log('[OrderCheckout] 设置余额:', balance)
        setLocalUserBalance(balance)
      } else {
        console.warn('[OrderCheckout] 获取余额失败:', result.message)
      }
    } catch (error) {
      console.error('[OrderCheckout] 获取余额异常:', error)
    }
  }

  useEffect(() => {
    fetchCartItems()
  }, [currentUser])

  const fetchCartItems = async () => {
    if (!currentUser) return
    
    try {
      setLoading(true)
      const response = await fetch(`${MALL_API_BASE}/cart?userId=${currentUser.id}`)
      const result = await response.json()
      
      if (result.code === 200 && result.data) {
        const checkedItems = result.data.filter(item => item.checked)
        setCartItems(checkedItems)
      }
    } catch (error) {
      console.error('获取购物车失败:', error)
    } finally {
      setLoading(false)
    }
  }

  // 计算总价
  const totalAmount = cartItems.reduce((sum, item) => {
    return sum + (item.game?.currentPrice || 0) * item.quantity
  }, 0)

  const discountAmount = cartItems.reduce((sum, item) => {
    const originalPrice = item.game?.basePrice || 0
    const currentPrice = item.game?.currentPrice || 0
    return sum + (originalPrice - currentPrice) * item.quantity
  }, 0)

  const handleCreateOrder = async () => {
    if (cartItems.length === 0) {
      alert('请选择要结算的商品')
      return
    }

    if (paymentMethod === 'balance' && userBalance < totalAmount) {
      alert('余额不足，请充值或选择其他支付方式')
      return
    }

    try {
      setProcessing(true)
      
      const orderData = {
        userId: currentUser.id,
        paymentMethod: paymentMethod,
        items: cartItems.map(item => ({
          gameId: item.gameId,
          quantity: item.quantity
        }))
      }

      const response = await fetch(`${MALL_API_BASE}/orders/create`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(orderData)
      })

      const result = await response.json()

      if (result.code === 200) {
        setOrderCreated(result.data)
        
        // 检查是否为模拟支付模式
        if (result.data.mockMode) {
          setIsMockMode(true)
        }
        
        // 如果是第三方支付，显示二维码
        // TODO: 微信支付已注释，暂时只支持支付宝
        if (paymentMethod === 'alipay' /* || paymentMethod === 'wechat' */) {
          setShowQRCode(true)
          return
        }
        
        // 余额支付，直接完成
        await clearCheckedCartItems()
        
        if (onOrderSuccess) {
          onOrderSuccess(result.data)
        }
        
        window.dispatchEvent(new CustomEvent('showSuccess', {
          detail: { title: '支付成功', message: `订单号: ${result.data.orderNo}`, duration: 3000 }
        }))
        
        navigate(`/orders/${result.data.orderId}`)
      } else {
        window.dispatchEvent(new CustomEvent('showError', {
          detail: { title: '订单创建失败', message: result.message, duration: 3000 }
        }))
      }
    } catch (error) {
      window.dispatchEvent(new CustomEvent('showError', {
        detail: { title: '订单创建失败', message: error.message, duration: 3000 }
      }))
    } finally {
      setProcessing(false)
    }
  }

  const handlePaymentComplete = async () => {
    // 模拟支付完成
    setShowQRCode(false)
    
    try {
      await clearCheckedCartItems()
      
      // 刷新余额
      await fetchUserBalance()
      
      if (onOrderSuccess) {
        onOrderSuccess(orderCreated)
      }
      
      window.dispatchEvent(new CustomEvent('showSuccess', {
        detail: { title: '支付成功', message: `订单号: ${orderCreated.orderNo}`, duration: 3000 }
      }))
      
      navigate(`/orders/${orderCreated.orderId}`)
    } catch (error) {
      window.dispatchEvent(new CustomEvent('showError', {
        detail: { title: '支付失败', message: error.message, duration: 3000 }
      }))
    }
  }

  const handleCancelQR = () => {
    setShowQRCode(false)
    setOrderCreated(null)
  }

  const clearCheckedCartItems = async () => {
    try {
      for (const item of cartItems) {
        await fetch(`${MALL_API_BASE}/cart/${item.gameId}?userId=${currentUser.id}`, {
          method: 'DELETE'
        })
      }
    } catch (error) {
      console.error('清空购物车失败:', error)
    }
  }

  if (loading) {
    return <div className="checkout-loading">加载中...</div>
  }

  if (cartItems.length === 0) {
    return (
      <div className="checkout-empty">
        <div className="empty-icon">🛒</div>
        <h2>购物车是空的</h2>
        <p>快去挑选心仪的游戏吧！</p>
        <button className="browse-btn" onClick={() => navigate('/')}>
          去逛逛
        </button>
      </div>
    )
  }

  return (
    <div className="order-checkout">
      <div className="checkout-header">
        <h1>💳 订单结算</h1>
        <button className="back-btn" onClick={() => navigate(-1)}>
          ← 返回
        </button>
      </div>

      <div className="checkout-content">
        {/* 商品列表 */}
        <div className="checkout-items">
          <h2>商品清单 ({cartItems.length})</h2>
          <div className="items-list">
            {cartItems.map((item) => (
              <div key={item.cartId} className="checkout-item">
                <div className="item-image">
                  {item.game?.coverImage || '🎮'}
                </div>
                <div className="item-details">
                  <h3 className="item-name">{item.game?.title}</h3>
                  <div className="item-meta">
                    <span className="item-quantity">数量: {item.quantity}</span>
                  </div>
                </div>
                <div className="item-price-section">
                  {item.game?.discountRate > 0 && (
                    <div className="original-price">
                      ¥{(item.game.basePrice * item.quantity).toFixed(2)}
                    </div>
                  )}
                  <div className="current-price">
                    ¥{(item.game?.currentPrice * item.quantity).toFixed(2)}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 订单摘要 */}
        <div className="checkout-summary">
          <h2>订单摘要</h2>
          
          <div className="summary-row">
            <span>商品总额</span>
            <span className="amount">¥{(totalAmount + discountAmount).toFixed(2)}</span>
          </div>
          
          {discountAmount > 0 && (
            <div className="summary-row discount">
              <span>优惠金额</span>
              <span className="amount">-¥{discountAmount.toFixed(2)}</span>
            </div>
          )}
          
          <div className="summary-divider" />
          
          <div className="summary-row total">
            <span>应付总额</span>
            <span className="amount highlight">¥{totalAmount.toFixed(2)}</span>
          </div>

          {/* 支付方式 */}
          <div className="payment-methods">
            <h3>选择支付方式</h3>
            
            <label className={`payment-option ${paymentMethod === 'balance' ? 'selected' : ''}`}>
              <input
                type="radio"
                name="payment"
                value="balance"
                checked={paymentMethod === 'balance'}
                onChange={(e) => setPaymentMethod(e.target.value)}
              />
              <div className="payment-info">
                <span className="payment-icon">💵</span>
                <div className="payment-details">
                  <span className="payment-name">余额支付</span>
                  <span className="payment-balance">当前余额: ¥{userBalance.toFixed(2)}</span>
                </div>
              </div>
            </label>

            <label className={`payment-option ${paymentMethod === 'alipay' ? 'selected' : ''}`}>
              <input
                type="radio"
                name="payment"
                value="alipay"
                checked={paymentMethod === 'alipay'}
                onChange={(e) => setPaymentMethod(e.target.value)}
              />
              <div className="payment-info">
                <span className="payment-icon">💙</span>
                <div className="payment-details">
                  <span className="payment-name">支付宝</span>
                </div>
              </div>
            </label>

            {/* TODO: 微信支付功能已临时注释（生产环境待配置）}
            {/* <label className={`payment-option ${paymentMethod === 'wechat' ? 'selected' : ''}`}>
              <input
                type="radio"
                name="payment"
                value="wechat"
                checked={paymentMethod === 'wechat'}
                onChange={(e) => setPaymentMethod(e.target.value)}
              />
              <div className="payment-info">
                <span className="payment-icon">💚</span>
                <div className="payment-details">
                  <span className="payment-name">微信支付</span>
                </div>
              </div>
            </label> */}
          </div>

          <button 
            className="submit-order-btn"
            onClick={handleCreateOrder}
            disabled={processing}
          >
            {processing ? '处理中...' : `提交订单 (¥${totalAmount.toFixed(2)})`}
          </button>
        </div>
      </div>

      {/* 支付二维码模态框 */}
      {showQRCode && orderCreated && (
        <div className="qr-modal-overlay" onClick={handleCancelQR}>
          <div className="qr-modal-content" onClick={(e) => e.stopPropagation()}>
            <button className="modal-close" onClick={handleCancelQR}>×</button>
            
            {/* 演示模式提示条 */}
            {isMockMode && (
              <div className="mock-mode-banner">
                <span className="mock-icon">🧪</span>
                <span className="mock-text">
                  演示模式 - 扫码后将自动模拟支付成功
                </span>
              </div>
            )}
            
            <div className="qr-modal-header">
              <div className="qr-icon">
                {paymentMethod === 'alipay' ? '💙' : '💚'}
              </div>
              <h2>
                {paymentMethod === 'alipay' ? '支付宝' : '微信支付'}
              </h2>
            </div>
            
            <div className="qr-amount-info">
              <span className="qr-label">订单金额</span>
              <span className="qr-amount">¥{orderCreated.actualAmount || totalAmount}</span>
            </div>
            
            <div className="qr-code-wrapper">
              <div className="qr-code">
                {/* 显示自定义支付二维码图片 */}
                <div className="qr-placeholder">
                  <img 
                    src={paymentMethod === 'alipay' 
                      ? '/alipay-qr.jpg' 
                      : '/wechat-qr.jpg'
                    }
                    alt="支付二维码"
                  />
                </div>
              </div>
              
              <p className="qr-tip">
                {paymentMethod === 'alipay' 
                  ? '打开支付宝，扫码完成支付' 
                  : '打开微信，扫码完成支付'}
              </p>
              
              {/* 演示模式说明 */}
              {isMockMode && (
                <div className="mock-mode-info">
                  <p className="mock-tip-title">ℹ️ 关于演示模式</p>
                  <p className="mock-tip-text">
                    支付功能已对接微信 Native 支付接口（生产环境），当前为演示模式。
                  </p>
                  <p className="mock-tip-text">
                    点击“我已付款”按钮即可模拟支付成功，体验完整购物流程。
                  </p>
                </div>
              )}
            </div>
            
            <div className="qr-actions">
              <button className="qr-cancel-btn" onClick={handleCancelQR}>
                取消支付
              </button>
              <button className="qr-success-btn" onClick={handlePaymentComplete}>
                {isMockMode ? '模拟支付成功' : '我已付款'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default OrderCheckout
