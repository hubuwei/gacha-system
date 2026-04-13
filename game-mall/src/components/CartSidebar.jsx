import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { MALL_API_BASE } from '../App'

// 简单的内联通知函数（因为侧边栏是子组件）
const showNotification = (type, message) => {
  // 触发自定义事件，让父组件处理
  window.dispatchEvent(new CustomEvent('cart-notification', { 
    detail: { type, message } 
  }))
}

function CartSidebar({ isOpen, onClose, currentUser }) {
  const navigate = useNavigate()
  const [cartItems, setCartItems] = useState([])
  const [loading, setLoading] = useState(false)

  // 获取购物车列表
  const fetchCart = async () => {
    if (!currentUser) return
    
    try {
      setLoading(true)
      const response = await fetch(`${MALL_API_BASE}/cart?userId=${currentUser.id}`)
      const result = await response.json()
      
      if (result.code === 200 && result.data) {
        setCartItems(result.data)
      }
    } catch (error) {
      console.error('获取购物车失败:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (isOpen && currentUser) {
      fetchCart()
    }
  }, [isOpen, currentUser])

  // 从购物车移除
  const handleRemove = async (gameId) => {
    try {
      const response = await fetch(`${MALL_API_BASE}/cart/${gameId}?userId=${currentUser.id}`, {
        method: 'DELETE'
      })
      const result = await response.json()
      
      if (result.code === 200) {
        showNotification('success', '已从购物车移除')
        fetchCart() // 重新加载购物车
      } else {
        showNotification('error', result.message || '移除失败')
      }
    } catch (error) {
      showNotification('error', '移除失败：' + error.message)
    }
  }

  // 更新选中状态
  const handleCheck = async (gameId, checked) => {
    try {
      await fetch(`${MALL_API_BASE}/cart/${gameId}/check?userId=${currentUser.id}&checked=${checked}`, {
        method: 'PUT'
      })
      fetchCart()
    } catch (error) {
      console.error('更新选中状态失败:', error)
    }
  }

  // 计算选中商品总价
  const checkedItems = cartItems.filter(item => item.checked)
  const totalAmount = checkedItems.reduce((sum, item) => {
    return sum + (item.game?.currentPrice || 0) * item.quantity
  }, 0)

  if (!isOpen) return null

  return (
    <div className="cart-sidebar-overlay" onClick={onClose} style={{position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.7)', zIndex: 1000, display: 'flex', justifyContent: 'flex-end'}}>
      <div className="cart-sidebar" onClick={(e) => e.stopPropagation()} style={{width: '450px', height: '100vh', background: 'linear-gradient(135deg, rgba(20, 20, 40, 0.98), rgba(10, 10, 30, 0.98))', backdropFilter: 'blur(20px)', borderLeft: '1px solid var(--glass-border)', display: 'flex', flexDirection: 'column', boxShadow: '-10px 0 30px rgba(0,0,0,0.5)'}}>
        <div className="cart-header" style={{padding: '25px', borderBottom: '1px solid var(--glass-border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
          <h2 style={{color: 'var(--text-light)', fontSize: '24px', fontWeight: '800', margin: 0}}>🛒 购物车 ({cartItems.length})</h2>
          <button className="close-btn" onClick={onClose} style={{background: 'transparent', border: 'none', color: 'var(--text-gray)', fontSize: '32px', cursor: 'pointer', width: '40px', height: '40px', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '8px', transition: 'all 0.3s'}} onMouseOver={(e) => {e.target.style.color = 'var(--neon-pink)'; e.target.style.background = 'rgba(255,0,255,0.1)';}} onMouseOut={(e) => {e.target.style.color = 'var(--text-gray)'; e.target.style.background = 'transparent';}}>×</button>
        </div>

        <div className="cart-content" style={{flex: 1, overflowY: 'auto', padding: '20px'}}>
          {loading ? (
            <div className="loading" style={{textAlign: 'center', padding: '60px 20px', color: 'var(--text-gray)', fontSize: '16px'}}>加载中...</div>
          ) : cartItems.length === 0 ? (
            <div className="empty-cart" style={{textAlign: 'center', padding: '80px 20px', color: 'var(--text-gray)'}}>
              <div className="empty-icon" style={{fontSize: '80px', marginBottom: '20px'}}>🛒</div>
              <p style={{fontSize: '18px', marginBottom: '30px'}}>购物车是空的</p>
              <button className="browse-btn" onClick={onClose} style={{padding: '12px 30px', background: 'linear-gradient(135deg, var(--neon-pink), var(--neon-purple))', border: 'none', borderRadius: '25px', color: 'white', fontSize: '16px', fontWeight: '600', cursor: 'pointer', transition: 'all 0.3s'}} onMouseOver={(e) => {e.target.style.transform = 'translateY(-2px)'; e.target.style.boxShadow = '0 10px 30px rgba(255,0,255,0.4)';}} onMouseOut={(e) => {e.target.style.transform = 'translateY(0)'; e.target.style.boxShadow = 'none';}}>去逛逛</button>
            </div>
          ) : (
            <>
              <div className="cart-items" style={{display: 'flex', flexDirection: 'column', gap: '15px'}}>
                {cartItems.map((item) => (
                  <div key={item.cartId} className="cart-item" style={{background: 'rgba(255, 255, 255, 0.05)', backdropFilter: 'blur(10px)', border: '1px solid var(--glass-border)', borderRadius: '12px', padding: '15px', display: 'flex', alignItems: 'center', gap: '15px', transition: 'all 0.3s'}} onMouseOver={(e) => {e.currentTarget.style.background = 'rgba(255, 255, 255, 0.08)'; e.currentTarget.style.borderColor = 'var(--neon-blue)';}} onMouseOut={(e) => {e.currentTarget.style.background = 'rgba(255, 255, 255, 0.05)'; e.currentTarget.style.borderColor = 'var(--glass-border)';}}>
                    <input
                      type="checkbox"
                      checked={item.checked}
                      onChange={(e) => handleCheck(item.gameId, e.target.checked)}
                      className="item-checkbox"
                      style={{width: '20px', height: '20px', cursor: 'pointer', accentColor: '#ff00ff', flexShrink: 0}}
                    />
                    <div className="item-image" style={{width: '100px', height: '60px', borderRadius: '8px', overflow: 'hidden', flexShrink: 0, background: 'rgba(255,255,255,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center'}}>
                      {item.game?.coverImage ? (
                        <img src={item.game.coverImage} alt={item.game.title} style={{width: '100%', height: '100%', objectFit: 'cover'}} />
                      ) : (
                        <span style={{fontSize: '30px'}}>🎮</span>
                      )}
                    </div>
                    <div className="item-info" style={{flex: 1, minWidth: 0}}>
                      <h4 className="item-name" style={{color: 'var(--text-light)', fontSize: '15px', marginBottom: '8px', fontWeight: '600', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap'}}>{item.game?.title}</h4>
                      <div className="item-price">
                        {item.game?.discountRate > 0 ? (
                          <>
                            <span className="original" style={{color: 'var(--text-gray)', textDecoration: 'line-through', fontSize: '13px', marginRight: '8px'}}>¥{item.game?.basePrice}</span>
                            <span className="current" style={{color: 'var(--price-color)', fontSize: '18px', fontWeight: '700'}}>¥{item.game?.currentPrice}</span>
                          </>
                        ) : (
                          <span className="current" style={{color: 'var(--price-color)', fontSize: '18px', fontWeight: '700'}}>¥{item.game?.currentPrice}</span>
                        )}
                      </div>
                    </div>
                    <button 
                      className="remove-btn"
                      onClick={() => handleRemove(item.gameId)}
                      style={{background: 'rgba(255, 71, 87, 0.1)', border: '1px solid rgba(255, 71, 87, 0.3)', color: 'var(--discount-color)', fontSize: '18px', cursor: 'pointer', width: '36px', height: '36px', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '8px', transition: 'all 0.3s', flexShrink: 0}}
                      onMouseOver={(e) => {e.target.style.background = 'rgba(255, 71, 87, 0.3)'; e.target.style.transform = 'scale(1.1)';}}
                      onMouseOut={(e) => {e.target.style.background = 'rgba(255, 71, 87, 0.1)'; e.target.style.transform = 'scale(1)';}}
                    >
                      🗑️
                    </button>
                  </div>
                ))}
              </div>

              <div className="cart-footer" style={{marginTop: '20px', paddingTop: '20px', borderTop: '1px solid var(--glass-border)'}}>
                <div className="cart-summary" style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', padding: '15px', background: 'rgba(255, 255, 255, 0.05)', borderRadius: '12px'}}>
                  <span className="summary-label" style={{color: 'var(--text-gray)', fontSize: '14px'}}>已选 {checkedItems.length} 件</span>
                  <span className="summary-total" style={{color: 'var(--text-light)', fontSize: '16px', fontWeight: '600'}}>
                    合计: <span className="amount" style={{color: 'var(--price-color)', fontSize: '28px', fontWeight: '800'}}>¥{totalAmount.toFixed(2)}</span>
                  </span>
                </div>
                <button 
                  className="checkout-btn"
                  disabled={checkedItems.length === 0}
                  onClick={() => {
                    onClose()
                    navigate('/checkout')
                  }}
                  style={{width: '100%', padding: '16px', background: checkedItems.length > 0 ? 'linear-gradient(135deg, var(--neon-pink), var(--sunset-orange))' : 'rgba(255, 255, 255, 0.1)', border: 'none', borderRadius: '12px', color: 'white', fontWeight: '700', fontSize: '16px', cursor: checkedItems.length > 0 ? 'pointer' : 'not-allowed', transition: 'all 0.3s', textTransform: 'uppercase', letterSpacing: '1px'}}
                  onMouseOver={(e) => {if (checkedItems.length > 0) {e.target.style.transform = 'translateY(-2px)'; e.target.style.boxShadow = '0 10px 30px rgba(255,0,255,0.5)';}}}
                  onMouseOut={(e) => {e.target.style.transform = 'translateY(0)'; e.target.style.boxShadow = 'none';}}
                >
                  结算 ({checkedItems.length})
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

export default CartSidebar
