import { useState, useEffect } from 'react'
import { MALL_API_BASE } from '../App'

// 简单的内联通知函数（因为侧边栏是子组件）
const showNotification = (type, message) => {
  // 触发自定义事件，让父组件处理
  window.dispatchEvent(new CustomEvent('wishlist-notification', { 
    detail: { type, message } 
  }))
}

function WishlistSidebar({ isOpen, onClose, currentUser }) {
  const [wishlistItems, setWishlistItems] = useState([])
  const [loading, setLoading] = useState(false)

  // 获取愿望单列表
  const fetchWishlist = async () => {
    if (!currentUser) return
    
    try {
      setLoading(true)
      const response = await fetch(`${MALL_API_BASE}/wishlist?userId=${currentUser.id}`)
      const result = await response.json()
      
      if (result.code === 200 && result.data) {
        setWishlistItems(result.data)
      }
    } catch (error) {
      console.error('获取愿望单失败:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (isOpen && currentUser) {
      fetchWishlist()
    }
  }, [isOpen, currentUser])

  // 从愿望单移除
  const handleRemove = async (gameId) => {
    try {
      const response = await fetch(`${MALL_API_BASE}/wishlist/${gameId}?userId=${currentUser.id}`, {
        method: 'DELETE'
      })
      const result = await response.json()
      
      if (result.code === 200) {
        showNotification('success', '已从愿望单移除')
        fetchWishlist()
      } else {
        showNotification('error', result.message || '移除失败')
      }
    } catch (error) {
      showNotification('error', '移除失败：' + error.message)
    }
  }

  // 更新通知设置
  const handleToggleNotify = async (gameId, currentNotify) => {
    try {
      await fetch(`${MALL_API_BASE}/wishlist/${gameId}/notify?userId=${currentUser.id}&notifyDiscount=${!currentNotify}`, {
        method: 'PUT'
      })
      fetchWishlist()
    } catch (error) {
      console.error('更新通知设置失败:', error)
    }
  }

  if (!isOpen) return null

  return (
    <div className="wishlist-sidebar-overlay" onClick={onClose}>
      <div className="wishlist-sidebar" onClick={(e) => e.stopPropagation()}>
        <div className="wishlist-header">
          <h2>❤️ 愿望单 ({wishlistItems.length})</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>

        <div className="wishlist-content">
          {loading ? (
            <div className="loading">加载中...</div>
          ) : wishlistItems.length === 0 ? (
            <div className="empty-wishlist">
              <div className="empty-icon">❤️</div>
              <p>愿望单是空的</p>
              <button className="browse-btn" onClick={onClose}>去逛逛</button>
            </div>
          ) : (
            <>
              <div className="wishlist-items">
                {wishlistItems.map((item) => (
                  <div key={item.wishlistId} className="wishlist-item">
                    <div className="item-image">
                      {item.game?.coverImage ? (
                        <img 
                          src={item.game.coverImage} 
                          alt={item.game.title} 
                          onError={(e) => e.target.src = '🎮'}
                        />
                      ) : (
                        '🎮'
                      )}
                    </div>
                    <div className="item-info">
                      <h4 className="item-name">{item.game?.title}</h4>
                      <div className="item-price">
                        {item.game?.discountRate > 0 ? (
                          <>
                            <span className="original">¥{item.game?.basePrice}</span>
                            <span className="discount-badge">-{item.game?.discountRate}%</span>
                            <span className="current">¥{item.game?.currentPrice}</span>
                          </>
                        ) : (
                          <span className="current">¥{item.game?.currentPrice}</span>
                        )}
                      </div>
                      <div className="item-actions">
                        <label className="notify-toggle">
                          <input
                            type="checkbox"
                            checked={item.notifyDiscount}
                            onChange={() => handleToggleNotify(item.gameId, item.notifyDiscount)}
                          />
                          <span>折扣通知</span>
                        </label>
                        <button 
                          className="remove-btn"
                          onClick={() => handleRemove(item.gameId)}
                        >
                          🗑️
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <div className="wishlist-footer">
                <p className="wishlist-tip">
                  💡 开启"折扣通知"后，游戏打折时会发送邮件提醒
                </p>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

export default WishlistSidebar
