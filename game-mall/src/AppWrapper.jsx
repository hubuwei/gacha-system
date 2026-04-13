import { useState, useEffect } from 'react'
import { BrowserRouter as Router, Routes, Route, useParams, useNavigate } from 'react-router-dom'
import App from './App'
import OrderCheckout from './components/OrderCheckout'
import OrderList from './components/OrderList'
import OrderDetail from './components/OrderDetail'
import UserProfile from './components/UserProfile'
import WalletRecharge from './components/WalletRecharge'
import TransactionHistory from './components/TransactionHistory'
import GameLibrary from './components/GameLibrary'
import GameDetail from './components/GameDetail'
import NotificationCenter from './components/NotificationCenter'
import { AUTH_API_BASE, MALL_API_BASE } from './App'

// 游戏详情包装组件
function GameDetailWrapper({ currentUser }) {
  const { gameId } = useParams()
  const navigate = useNavigate()
  
  // 模拟购物车状态（实际应该从全局状态管理）
  const [cartItems, setCartItems] = useState([])
  
  const isInCart = cartItems.some(item => item.gameId === parseInt(gameId))
  
  const handleAddToCart = (game) => {
    // TODO: 实现添加到购物车逻辑
    alert(`已将 ${game.title} 添加到购物车`)
  }
  
  const handleBack = () => {
    navigate(-1)
  }
  
  return (
    <GameDetail 
      gameId={gameId}
      onBack={handleBack}
      onAddToCart={handleAddToCart}
      isInCart={isInCart}
      currentUser={currentUser}
    />
  )
}

function AppWrapper() {
  // 提升状态到 Wrapper 层
  const [currentUser, setCurrentUser] = useState(null)
  const [userBalance, setUserBalance] = useState(0)

  // 页面加载时从 localStorage 恢复用户状态
  useEffect(() => {
    const token = localStorage.getItem('token')
    const user = localStorage.getItem('user')
    const tokenExpiry = localStorage.getItem('tokenExpiry')
    
    if (token && user) {
      // 检查Token是否过期
      if (tokenExpiry && new Date(tokenExpiry) < new Date()) {
        console.log('Token已过期，清除登录状态')
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        localStorage.removeItem('tokenExpiry')
      } else {
        const userData = JSON.parse(user)
        setCurrentUser(userData)
        refreshBalance(userData.id)  // 传入用户ID
      }
    }
  }, [])

  // 刷新用户余额
  const refreshBalance = async (userId) => {
    const uid = userId || currentUser?.id
    if (!uid) {
      console.warn('[AppWrapper] 用户ID为空，无法获取余额')
      return
    }
    try {
      console.log('[AppWrapper] 开始获取余额, userId:', uid)
      // 从 mall-service 获取钱包余额
      const response = await fetch(`${MALL_API_BASE}/wallet/balance?userId=${uid}`)
      console.log('[AppWrapper] 响应状态:', response.status)
      const result = await response.json()
      console.log('[AppWrapper] 响应数据:', result)
      
      if (result.code === 200 && result.data) {
        const balance = result.data.balance || 0
        console.log('[AppWrapper] 设置余额:', balance)
        setUserBalance(balance)
      } else {
        console.warn('[AppWrapper] 获取余额失败:', result.message)
      }
    } catch (error) {
      console.error('[AppWrapper] 获取余额异常:', error)
    }
  }

  // 处理登录成功
  const handleLoginSuccess = (user) => {
    setCurrentUser(user)
    refreshBalance(user.id)  // 传入用户ID
  }

  // 处理登出
  const handleLogoutSuccess = () => {
    setCurrentUser(null)
    setUserBalance(0)
  }

  return (
    <Router>
      <Routes>
        {/* 主页 */}
        <Route 
          path="/" 
          element={
            <App 
              onUserLogin={handleLoginSuccess}
              onUserLogout={handleLogoutSuccess}
            />
          } 
        />
        
        {/* 订单相关 */}
        <Route 
          path="/checkout" 
          element={
            <OrderCheckout 
              currentUser={currentUser}
              userBalance={userBalance}
              onOrderSuccess={refreshBalance}
            />
          } 
        />
        <Route path="/orders" element={<OrderList currentUser={currentUser} />} />
        <Route path="/orders/:orderId" element={<OrderDetail currentUser={currentUser} />} />
        
        {/* 个人中心 */}
        <Route 
          path="/profile" 
          element={
            <UserProfile 
              currentUser={currentUser}
              setCurrentUser={setCurrentUser}
            />
          } 
        />
        
        {/* 钱包相关 */}
        <Route 
          path="/wallet/recharge" 
          element={
            <WalletRecharge 
              currentUser={currentUser}
              userBalance={userBalance}
              onRechargeSuccess={refreshBalance}
            />
          } 
        />
        <Route 
          path="/wallet/transactions" 
          element={<TransactionHistory currentUser={currentUser} />} 
        />
        
        {/* 游戏库 */}
        <Route path="/library" element={<GameLibrary currentUser={currentUser} />} />
        
        {/* 游戏详情 */}
        <Route 
          path="/games/:gameId" 
          element={
            <GameDetailWrapper currentUser={currentUser} />
          } 
        />
        
        {/* 通知中心 */}
        <Route path="/notifications" element={<NotificationCenter currentUser={currentUser} />} />
      </Routes>
    </Router>
  )
}

export default AppWrapper
