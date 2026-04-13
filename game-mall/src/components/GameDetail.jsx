import { useState, useEffect } from 'react'

// API 基础地址 - 使用相对路径，通过 Nginx 反向代理
const MALL_API_BASE = '/api'

// 简单的内联通知函数
const showNotification = (type, message) => {
  window.dispatchEvent(new CustomEvent('cart-notification', { 
    detail: { type, message } 
  }))
}

function GameDetail({ gameId, onBack, onAddToCart, isInCart, currentUser }) {
  const [game, setGame] = useState(null)
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState('description') // description, requirements, reviews
  const [currentScreenshot, setCurrentScreenshot] = useState(0)
  const [inWishlist, setInWishlist] = useState(false)
  const [checkingWishlist, setCheckingWishlist] = useState(false)
  const [purchasedGames, setPurchasedGames] = useState([])
  const [checkingPurchased, setCheckingPurchased] = useState(false)
  
  // 评论相关状态
  const [reviews, setReviews] = useState([])
  const [reviewStats, setReviewStats] = useState({ averageRating: '0.00', totalReviews: 0 })
  const [showReviewForm, setShowReviewForm] = useState(false)
  const [reviewForm, setReviewForm] = useState({
    rating: 5,
    title: '',
    content: '',
    pros: '',
    cons: ''
  })

  useEffect(() => {
    fetchGameDetail()
    if (currentUser) {
      checkWishlistStatus()
      checkPurchasedStatus()
    }
    fetchReviews()
  }, [gameId, currentUser])

  // 检查是否在愿望单中
  const checkWishlistStatus = async () => {
    if (!currentUser || !gameId) return
    
    try {
      setCheckingWishlist(true)
      const response = await fetch(`${MALL_API_BASE}/wishlist?userId=${currentUser.id}`)
      const result = await response.json()
      
      if (result.code === 200 && result.data) {
        const isInList = result.data.some(item => item.gameId === gameId)
        setInWishlist(isInList)
      }
    } catch (error) {
      console.error('检查愿望单状态失败:', error)
    } finally {
      setCheckingWishlist(false)
    }
  }

  // 检查是否已购买该游戏
  const checkPurchasedStatus = async () => {
    if (!currentUser || !gameId) return
    
    try {
      setCheckingPurchased(true)
      const response = await fetch(`${MALL_API_BASE}/orders/purchased-games?userId=${currentUser.id}`)
      const result = await response.json()
      
      if (result.code === 200 && result.data) {
        setPurchasedGames(result.data.map(g => g.id))
      }
    } catch (error) {
      console.error('检查购买状态失败:', error)
    } finally {
      setCheckingPurchased(false)
    }
  }

  // 判断当前游戏是否已购买
  const isPurchased = purchasedGames.includes(gameId)

  // 添加到愿望单
  const handleAddToWishlist = async () => {
    if (!currentUser) {
      showNotification('warning', '请先登录')
      return
    }
    
    try {
      const response = await fetch(
        `${MALL_API_BASE}/wishlist?userId=${currentUser.id}&gameId=${gameId}&notifyDiscount=true`,
        { method: 'POST' }
      )
      const result = await response.json()
      
      if (result.code === 200) {
        setInWishlist(true)
        showNotification('success', '已添加到愿望单！游戏打折时会发送邮件提醒。')
      } else {
        showNotification('error', result.message || '添加失败')
      }
    } catch (error) {
      showNotification('error', '添加失败：' + error.message)
    }
  }

  // 从愿望单移除
  const handleRemoveFromWishlist = async () => {
    if (!currentUser) return
    
    try {
      const response = await fetch(
        `${MALL_API_BASE}/wishlist/${gameId}?userId=${currentUser.id}`,
        { method: 'DELETE' }
      )
      const result = await response.json()
      
      if (result.code === 200) {
        setInWishlist(false)
        showNotification('success', '已从愿望单移除')
      } else {
        showNotification('error', result.message || '移除失败')
      }
    } catch (error) {
      showNotification('error', '移除失败：' + error.message)
    }
  }

  // 获取评论列表
  const fetchReviews = async () => {
    try {
      const response = await fetch(`${MALL_API_BASE}/games/${gameId}/reviews?page=0&size=10`)
      const result = await response.json()
      
      if (result.code === 200 && result.data) {
        setReviews(result.data.reviews || [])
        setReviewStats(result.data.stats || { averageRating: '0.00', totalReviews: 0 })
      }
    } catch (error) {
      console.error('获取评论失败:', error)
    }
  }

  // 提交评论
  const handleSubmitReview = async (e) => {
    e.preventDefault()
    
    if (!currentUser) {
      showNotification('warning', '请先登录')
      return
    }
    
    if (!reviewForm.title.trim() || !reviewForm.content.trim()) {
      showNotification('warning', '请填写评论标题和内容')
      return
    }
    
    try {
      const response = await fetch(`${MALL_API_BASE}/games/${gameId}/reviews`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          userId: currentUser.id,
          username: currentUser.username,
          userAvatar: currentUser.avatarUrl || '',
          rating: reviewForm.rating,
          title: reviewForm.title,
          content: reviewForm.content,
          pros: reviewForm.pros,
          cons: reviewForm.cons,
          status: 1 // 直接显示，不审核
        })
      })
      
      const result = await response.json()
      
      if (result.code === 200) {
        showNotification('success', '评论发表成功！')
        setShowReviewForm(false)
        setReviewForm({ rating: 5, title: '', content: '', pros: '', cons: '' })
        fetchReviews() // 重新加载评论
        fetchGameDetail() // 更新游戏评分
      } else {
        showNotification('error', result.message || '评论发表失败')
      }
    } catch (error) {
      showNotification('error', '评论发表失败：' + error.message)
    }
  }

  const fetchGameDetail = async () => {
    try {
      setLoading(true)
      const response = await fetch(`${MALL_API_BASE}/games/${gameId}`)
      const result = await response.json()
      
      if (result.code === 200 && result.data) {
        setGame(result.data)
      } else {
        console.error('获取游戏详情失败:', result.message)
      }
    } catch (error) {
      console.error('获取游戏详情失败:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="game-detail-loading">
        <div className="loading-spinner">加载中...</div>
      </div>
    )
  }

  if (!game) {
    return (
      <div className="game-detail-error">
        <p>游戏不存在</p>
        <button onClick={onBack}>返回</button>
      </div>
    )
  }

  // 解析截图 JSON
  let screenshots = []
  try {
    screenshots = game.screenshots ? JSON.parse(game.screenshots) : []
  } catch (e) {
    screenshots = []
  }

  // 计算折扣
  const discount = game.discountRate || 0
  const hasDiscount = discount > 0

  return (
    <div className="game-detail-container">
      {/* 返回按钮 */}
      <button className="back-btn" onClick={onBack}>
        ← 返回游戏列表
      </button>

      {/* 游戏横幅 */}
      <div className="game-banner" style={{
        backgroundImage: game.bannerImage ? `url(${game.bannerImage})` : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      }}>
        <div className="game-banner-overlay">
          <h1 className="game-title">{game.title}</h1>
          <div className="game-meta">
            <span className="developer">开发商: {game.developer || '未知'}</span>
            <span className="publisher">发行商: {game.publisher || '未知'}</span>
            {game.releaseDate && (
              <span className="release-date">发布日期: {game.releaseDate}</span>
            )}
          </div>
        </div>
      </div>

      {/* 主要内容区 */}
      <div className="game-detail-content">
        {/* 左侧信息 */}
        <div className="game-detail-left">
          {/* 价格和购买 */}
          <div className="game-purchase-card">
            <div className="game-price-section">
              {hasDiscount ? (
                <>
                  <div className="discount-info">
                    <span className="discount-badge-large">-{discount}%</span>
                    <span className="original-price-large">¥{parseFloat(game.basePrice).toFixed(2)}</span>
                  </div>
                  <div className="current-price-large">¥{parseFloat(game.currentPrice).toFixed(2)}</div>
                </>
              ) : (
                <div className="current-price-large">¥{parseFloat(game.currentPrice).toFixed(2)}</div>
              )}
            </div>
            
            <button 
              className={`add-to-cart-btn ${isInCart ? 'in-cart' : ''} ${isPurchased ? 'purchased' : ''}`}
              onClick={() => onAddToCart(game)}
              disabled={isInCart || isPurchased}
            >
              {isPurchased ? '✓ 已在游戏库中' : (isInCart ? '✓ 已在购物车中' : '🛒 加入购物车')}
            </button>

            <button 
              className={`add-to-wishlist-btn ${inWishlist ? 'in-wishlist' : ''}`}
              onClick={inWishlist ? handleRemoveFromWishlist : handleAddToWishlist}
              disabled={checkingWishlist}
            >
              {checkingWishlist ? '检查中...' : (inWishlist ? '❤️ 已在愿望单' : '♡ 加入愿望单')}
            </button>

            <div className="game-stats">
              <div className="stat-item">
                <span className="stat-label">评分</span>
                <span className="stat-value rating">⭐ {game.rating || '暂无'}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">评价数</span>
                <span className="stat-value">{game.totalReviews || 0}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">销量</span>
                <span className="stat-value">{game.totalSales || 0}</span>
              </div>
            </div>
          </div>

          {/* 分类和标签 */}
          <div className="game-categories-tags">
            {game.categories && game.categories.length > 0 && (
              <div className="categories-section">
                <h3>分类</h3>
                <div className="category-list">
                  {game.categories.map((cat, idx) => (
                    <span key={idx} className="category-tag">{cat}</span>
                  ))}
                </div>
              </div>
            )}
            
            {game.tags && game.tags.length > 0 && (
              <div className="tags-section">
                <h3>标签</h3>
                <div className="tag-list">
                  {game.tags.map((tag, idx) => (
                    <span 
                      key={idx} 
                      className="game-tag"
                      style={{ backgroundColor: tag.color || '#667eea' }}
                    >
                      {tag.name}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* 截图展示 */}
          {screenshots.length > 0 && (
            <div className="screenshots-section">
              <h3>游戏截图</h3>
              <div className="screenshot-viewer">
                <div className="screenshot-main">
                  <img src={screenshots[currentScreenshot]} alt={`Screenshot ${currentScreenshot + 1}`} />
                </div>
                <div className="screenshot-thumbnails">
                  {screenshots.map((shot, idx) => (
                    <img
                      key={idx}
                      src={shot}
                      alt={`Thumbnail ${idx + 1}`}
                      className={`thumbnail ${idx === currentScreenshot ? 'active' : ''}`}
                      onClick={() => setCurrentScreenshot(idx)}
                    />
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* 预告片 */}
          {game.trailerUrl && (
            <div className="trailer-section">
              <h3>游戏预告</h3>
              <div className="trailer-container">
                <iframe
                  src={game.trailerUrl}
                  title="Game Trailer"
                  frameBorder="0"
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                  allowFullScreen
                ></iframe>
              </div>
            </div>
          )}
        </div>

        {/* 右侧内容 */}
        <div className="game-detail-right">
          {/* 选项卡 */}
          <div className="detail-tabs">
            <button 
              className={`tab-btn ${activeTab === 'description' ? 'active' : ''}`}
              onClick={() => setActiveTab('description')}
            >
              游戏介绍
            </button>
            <button 
              className={`tab-btn ${activeTab === 'requirements' ? 'active' : ''}`}
              onClick={() => setActiveTab('requirements')}
            >
              配置要求
            </button>
            <button 
              className={`tab-btn ${activeTab === 'reviews' ? 'active' : ''}`}
              onClick={() => setActiveTab('reviews')}
            >
              用户评价 ({game.totalReviews || 0})
            </button>
          </div>

          {/* 游戏介绍 */}
          {activeTab === 'description' && (
            <div className="tab-content description-content">
              <h3>游戏简介</h3>
              <p className="short-desc">{game.shortDescription}</p>
              <div className="full-desc" dangerouslySetInnerHTML={{ __html: game.fullDescription || '' }} />
            </div>
          )}

          {/* 配置要求 */}
          {activeTab === 'requirements' && game.systemRequirements && (
            <div className="tab-content requirements-content">
              <div className="requirements-grid">
                <div className="requirements-column minimum">
                  <h4>最低配置</h4>
                  <div className="req-item">
                    <span className="req-label">操作系统:</span>
                    <span className="req-value">{game.systemRequirements.osMin || '未指定'}</span>
                  </div>
                  <div className="req-item">
                    <span className="req-label">处理器:</span>
                    <span className="req-value">{game.systemRequirements.cpuMin || '未指定'}</span>
                  </div>
                  <div className="req-item">
                    <span className="req-label">内存:</span>
                    <span className="req-value">{game.systemRequirements.ramMin || '未指定'}</span>
                  </div>
                  <div className="req-item">
                    <span className="req-label">显卡:</span>
                    <span className="req-value">{game.systemRequirements.gpuMin || '未指定'}</span>
                  </div>
                  <div className="req-item">
                    <span className="req-label">存储空间:</span>
                    <span className="req-value">{game.systemRequirements.storageMin || '未指定'}</span>
                  </div>
                </div>

                <div className="requirements-column recommended">
                  <h4>推荐配置</h4>
                  <div className="req-item">
                    <span className="req-label">操作系统:</span>
                    <span className="req-value">{game.systemRequirements.osRecommended || '未指定'}</span>
                  </div>
                  <div className="req-item">
                    <span className="req-label">处理器:</span>
                    <span className="req-value">{game.systemRequirements.cpuRecommended || '未指定'}</span>
                  </div>
                  <div className="req-item">
                    <span className="req-label">内存:</span>
                    <span className="req-value">{game.systemRequirements.ramRecommended || '未指定'}</span>
                  </div>
                  <div className="req-item">
                    <span className="req-label">显卡:</span>
                    <span className="req-value">{game.systemRequirements.gpuRecommended || '未指定'}</span>
                  </div>
                  <div className="req-item">
                    <span className="req-label">存储空间:</span>
                    <span className="req-value">{game.systemRequirements.storageRecommended || '未指定'}</span>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* 用户评价 */}
          {activeTab === 'reviews' && (
            <div className="tab-content reviews-content">
              <div className="reviews-summary">
                <div className="average-rating">
                  <div className="rating-big">{reviewStats.averageRating}</div>
                  <div className="rating-stars">⭐⭐⭐⭐⭐</div>
                  <div className="rating-count">{reviewStats.totalReviews} 条评价</div>
                </div>
              </div>
              
              {/* 发表评论按钮 */}
              {currentUser ? (
                <button 
                  className="write-review-btn"
                  onClick={() => setShowReviewForm(!showReviewForm)}
                >
                  ✍️ {showReviewForm ? '取消评论' : '发表评价'}
                </button>
              ) : (
                <p className="login-to-review">登录后即可发表评论</p>
              )}
              
              {/* 评论表单 */}
              {showReviewForm && currentUser && (
                <form className="review-form" onSubmit={handleSubmitReview}>
                  <div className="form-group">
                    <label>评分：</label>
                    <div className="rating-input">
                      {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((num) => (
                        <button
                          key={num}
                          type="button"
                          className={`rating-btn ${reviewForm.rating >= num ? 'active' : ''}`}
                          onClick={() => setReviewForm({...reviewForm, rating: num})}
                        >
                          {num}
                        </button>
                      ))}
                    </div>
                  </div>
                  
                  <div className="form-group">
                    <label>标题：</label>
                    <input
                      type="text"
                      value={reviewForm.title}
                      onChange={(e) => setReviewForm({...reviewForm, title: e.target.value})}
                      placeholder="给您的评论起个标题"
                      required
                    />
                  </div>
                  
                  <div className="form-group">
                    <label>评论内容：</label>
                    <textarea
                      value={reviewForm.content}
                      onChange={(e) => setReviewForm({...reviewForm, content: e.target.value})}
                      placeholder="分享您游戏体验..."
                      rows="5"
                      required
                    />
                  </div>
                  
                  <div className="form-row">
                    <div className="form-group">
                      <label>优点：</label>
                      <textarea
                        value={reviewForm.pros}
                        onChange={(e) => setReviewForm({...reviewForm, pros: e.target.value})}
                        placeholder="这款游戏有哪些优点？"
                        rows="3"
                      />
                    </div>
                    
                    <div className="form-group">
                      <label>缺点：</label>
                      <textarea
                        value={reviewForm.cons}
                        onChange={(e) => setReviewForm({...reviewForm, cons: e.target.value})}
                        placeholder="这款游戏有哪些不足？"
                        rows="3"
                      />
                    </div>
                  </div>
                  
                  <div className="form-actions">
                    <button type="submit" className="submit-review-btn">提交评论</button>
                  </div>
                </form>
              )}
              
              {/* 评论列表 */}
              <div className="reviews-list">
                {reviews.length === 0 ? (
                  <p className="no-reviews">暂无评价，快来发表第一条评论吧！</p>
                ) : (
                  reviews.map((review) => (
                    <div key={review.id} className="review-item">
                      <div className="review-header">
                        <div className="reviewer-info">
                          <span className="reviewer-avatar">👤</span>
                          <span className="reviewer-name">{review.username}</span>
                        </div>
                        <div className="review-meta">
                          <span className="review-rating">⭐ {review.rating}/10</span>
                          <span className="review-date">{new Date(review.createdAt).toLocaleDateString('zh-CN')}</span>
                        </div>
                      </div>
                      
                      {review.title && <h4 className="review-title">{review.title}</h4>}
                      <p className="review-content">{review.content}</p>
                      
                      {(review.pros || review.cons) && (
                        <div className="review-pros-cons">
                          {review.pros && (
                            <div className="pros">
                              <strong>👍 优点：</strong>
                              <p>{review.pros}</p>
                            </div>
                          )}
                          {review.cons && (
                            <div className="cons">
                              <strong>👎 缺点：</strong>
                              <p>{review.cons}</p>
                            </div>
                          )}
                        </div>
                      )}
                      
                      <div className="review-footer">
                        <button className="helpful-btn">
                          👍 有用 ({review.helpfulCount || 0})
                        </button>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default GameDetail
