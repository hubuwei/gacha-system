import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { MALL_API_BASE } from '../App'

function GameLibrary({ currentUser }) {
  const navigate = useNavigate()
  const [games, setGames] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('all') // all, installed, notInstalled

  useEffect(() => {
    if (currentUser) {
      fetchPurchasedGames()
    }
  }, [currentUser])

  const fetchPurchasedGames = async () => {
    try {
      setLoading(true)
      const response = await fetch(`${MALL_API_BASE}/orders/purchased-games?userId=${currentUser.id}`)
      const result = await response.json()
      
      if (result.code === 200 && result.data) {
        setGames(result.data)
      } else {
        console.warn('获取已购游戏失败:', result.message)
        setGames([])
      }
    } catch (error) {
      console.error('获取已购游戏失败:', error)
      setGames([])
    } finally {
      setLoading(false)
    }
  }

  const handleLaunchGame = (game) => {
    // TODO: 实现游戏启动逻辑
    alert(`正在启动 ${game.title}...`)
  }

  const handleDownloadGame = (game) => {
    // TODO: 实现游戏下载逻辑
    alert(`开始下载 ${game.title}...`)
  }

  // 未登录提示
  if (!currentUser) {
    return (
      <div className="game-library-page">
        <div className="page-header">
          <h1>🎮 我的游戏库</h1>
          <button className="back-btn" onClick={() => navigate(-1)}>
            ← 返回
          </button>
        </div>
        <div className="empty-library">
          <div className="empty-icon">🔒</div>
          <h2>请先登录</h2>
          <p>登录后即可查看您的游戏库</p>
          <button className="browse-btn" onClick={() => navigate('/')}>
            去首页登录
          </button>
        </div>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="game-library-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>加载中...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="game-library-page">
      <div className="page-header">
        <h1>🎮 我的游戏库</h1>
        <button className="back-btn" onClick={() => navigate(-1)}>
          ← 返回
        </button>
      </div>

      {/* 筛选标签 */}
      <div className="library-filters">
        <button 
          className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
          onClick={() => setFilter('all')}
        >
          全部 ({games.length})
        </button>
        <button 
          className={`filter-btn ${filter === 'installed' ? 'active' : ''}`}
          onClick={() => setFilter('installed')}
        >
          已安装
        </button>
        <button 
          className={`filter-btn ${filter === 'notInstalled' ? 'active' : ''}`}
          onClick={() => setFilter('notInstalled')}
        >
          未安装
        </button>
      </div>

      {/* 游戏列表 */}
      {games.length === 0 ? (
        <div className="empty-library">
          <div className="empty-icon">🎮</div>
          <h2>游戏库是空的</h2>
          <p>购买游戏后会显示在这里</p>
          <button className="browse-btn" onClick={() => navigate('/')}>
            去商城逛逛
          </button>
        </div>
      ) : (
        <div className="library-grid">
          {games.map((game) => (
            <div key={game.id} className="library-game-card">
              <div className="game-cover">
                {game.coverImage ? (
                  <img src={game.coverImage} alt={game.title} />
                ) : (
                  <div className="cover-placeholder">🎮</div>
                )}
                
                {game.playTime > 0 && (
                  <div className="play-time-badge">
                    ⏱️ {game.playTime.toFixed(1)}h
                  </div>
                )}
              </div>
              
              <div className="game-info">
                <h3 className="game-title">{game.title}</h3>
                <div className="game-meta">
                  <span className="purchase-date">
                    购买于: {new Date(game.purchaseDate).toLocaleDateString('zh-CN')}
                  </span>
                </div>
                
                <div className="game-actions">
                  {game.installed ? (
                    <button 
                      className="action-btn launch"
                      onClick={() => handleLaunchGame(game)}
                    >
                      ▶️ 启动游戏
                    </button>
                  ) : (
                    <button 
                      className="action-btn download"
                      onClick={() => handleDownloadGame(game)}
                    >
                      ⬇️ 下载
                    </button>
                  )}
                  
                  <button 
                    className="action-btn detail"
                    onClick={() => navigate(`/games/${game.id}`)}
                  >
                    查看详情
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default GameLibrary
