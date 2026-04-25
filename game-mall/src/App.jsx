import { useState, useEffect, useCallback } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import './App.css'
import './components/NewPages.css'
import bgImage from './assets/Jason_and_Lucia_02_landscape.jpg'
import GameDetail from './components/GameDetail'
import CartSidebar from './components/CartSidebar'
import WishlistSidebar from './components/WishlistSidebar'
import { NotificationContainer, useNotification } from './components/Notification'
import ConfirmDialog from './components/ConfirmDialog'

// API 基础地址 - 使用相对路径，通过 Nginx 反向代理
const getApiBase = () => {
  // 生产环境使用相对路径，通过 Nginx 代理
  return ''
}

export const AUTH_API_BASE = getApiBase()
export const MALL_API_BASE = '/api'

function App({ onUserLogin, onUserLogout }) {
  const navigate = useNavigate()
  const location = useLocation() // 获取当前路由信息
  const [activeCategory, setActiveCategory] = useState('全部')
  const [searchQuery, setSearchQuery] = useState('')
  const [showAdvancedFilter, setShowAdvancedFilter] = useState(false)
  const [priceRange, setPriceRange] = useState([0, 500])
  const [minRating, setMinRating] = useState(0)
  const [sortBy, setSortBy] = useState('default')
  
  // 使用自定义通知系统
  const { 
    notifications: toastNotifications, 
    removeNotification, 
    showSuccess, 
    showError, 
    showWarning, 
    showInfo 
  } = useNotification()
  
  // 用户状态
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [currentUser, setCurrentUser] = useState(null)
  const [userBalance, setUserBalance] = useState(0)
  const [showLoginModal, setShowLoginModal] = useState(false)
  const [loginForm, setLoginForm] = useState({ username: '', password: '', rememberMe: false })
  const [emailLoginForm, setEmailLoginForm] = useState({ email: '', code: '' })
  const [loginMode, setLoginMode] = useState('password') // 'password' or 'email'
  const [sendingCode, setSendingCode] = useState(false)
  const [countdown, setCountdown] = useState(0)
  const [registerForm, setRegisterForm] = useState({ 
    username: '', 
    password: '', 
    confirmPassword: '',
    email: '',
    code: ''
  })
  const [isRegisterMode, setIsRegisterMode] = useState(false)
  const [sendingRegisterCode, setSendingRegisterCode] = useState(false)
  const [registerCountdown, setRegisterCountdown] = useState(0)
  
  // 购物车状态
  const [cart, setCart] = useState([])
  const [showCart, setShowCart] = useState(false)
  const [cartGameIds, setCartGameIds] = useState(new Set()) // 用于快速检查游戏是否在购物车中
  
  // 已购游戏列表
  const [purchasedGameIds, setPurchasedGameIds] = useState(new Set())
  
  // 愿望单状态
  const [showWishlist, setShowWishlist] = useState(false)
  
  // 游戏数据
  const [games, setGames] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchResults, setSearchResults] = useState(null) // ES搜索结果
  
  // 游戏详情视图状态
  const [selectedGameId, setSelectedGameId] = useState(null)
  
  // 标签数据
  const [tags, setTags] = useState([])
  const [selectedTag, setSelectedTag] = useState(null)
  
  // 下拉菜单状态
  const [showDropdown, setShowDropdown] = useState(false)
  const [dropdownTimer, setDropdownTimer] = useState(null)
  
  // 通知铃铛状态
  const [notifications, setNotifications] = useState([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [showNotificationPanel, setShowNotificationPanel] = useState(false)
  const [notificationTimer, setNotificationTimer] = useState(null)
  
  // 搜索建议状态
  const [searchSuggestions, setSearchSuggestions] = useState([])
  const [showSuggestions, setShowSuggestions] = useState(false)
  const [searchDebounceTimer, setSearchDebounceTimer] = useState(null)
  
  // 返回顶部按钮状态
  const [showBackToTop, setShowBackToTop] = useState(false)
  
  // 用于强制刷新数据的计数器
  const [refreshKey, setRefreshKey] = useState(0)
  
  // GTA6倒计时弹窗状态
  const [showGTA6Modal, setShowGTA6Modal] = useState(false)
  const [gta6Countdown, setGta6Countdown] = useState({ months: 0, days: 0, hours: 0, minutes: 0 })

  const categories = ['全部', '动作', '冒险', '赛车', '射击', 'RPG', '模拟', '策略']

  // Banner 轮播图状态
  const [currentBanner, setCurrentBanner] = useState(0)
  const bannerGames = games.filter(g => g.featured || g.discount > 30).slice(0, 5)

  // 页面加载或路由变化时检查登录状态并获取数据
  useEffect(() => {
    console.log('[App] useEffect 触发, pathname:', location.pathname)
    
    // 首次加载显示GTA6倒计时弹窗
    if (!sessionStorage.getItem('gta6ModalShown')) {
      setShowGTA6Modal(true)
      sessionStorage.setItem('gta6ModalShown', 'true')
    }
    
    let refreshInterval = null
    
    const token = localStorage.getItem('token')
    const user = localStorage.getItem('user')
    const tokenExpiry = localStorage.getItem('tokenExpiry')
    
    // 检查是否有记住的账号密码
    const rememberedUsername = localStorage.getItem('rememberedUsername')
    const rememberedPassword = localStorage.getItem('rememberedPassword')
    if (rememberedUsername && rememberedPassword) {
      setLoginForm(prev => ({
        ...prev,
        username: rememberedUsername,
        password: rememberedPassword,
        rememberMe: true
      }))
    }
    
    if (token && user) {
      // 检查Token是否过期
      if (tokenExpiry && new Date(tokenExpiry) < new Date()) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        localStorage.removeItem('tokenExpiry')
      } else {
        const userData = JSON.parse(user)
        setCurrentUser(userData)
        setIsLoggedIn(true)
        
        // 获取完整的用户信息（包括头像）
        fetchFullUserInfo(userData.id)
        
        fetchUserBalance(userData.id)  // 传入用户ID
        fetchCartCount()
        fetchPurchasedGames(userData.id)  // 获取已购游戏列表
        fetchNotifications()  // 获取通知
        
        // 设置定时刷新Token（每5分钟检查一次）
        refreshInterval = setInterval(() => {
          refreshToken()
        }, 5 * 60 * 1000)
        
        // 设置定时刷新通知（每30秒静默更新）
        window.notificationRefreshInterval = setInterval(() => {
          fetchNotifications()
        }, 30 * 1000)
      }
    }
    
    // 无论是否登录，都要获取游戏列表和标签
    console.log('[App] 开始加载游戏数据')
    fetchGames()
    fetchTags()
    
    const handleNotification = (event) => {
      const { type, message } = event.detail
      switch(type) {
        case 'success':
          showSuccess('操作成功', message)
          break
        case 'error':
          showError('操作失败', message)
          break
        case 'warning':
          showWarning('提示', message)
          break
        default:
          showInfo('通知', message)
      }
    }
    
    window.addEventListener('cart-notification', handleNotification)
    window.addEventListener('wishlist-notification', handleNotification)
    
    // GTA6倒计时定时器（每秒更新）
    const gta6CountdownInterval = setInterval(() => {
      updateGTA6Countdown()
    }, 1000)
    
    // 初始化倒计时
    updateGTA6Countdown()
    
    // 清理函数：统一在最后返回
    return () => {
      console.log('[App] useEffect 清理')
      if (refreshInterval) {
        clearInterval(refreshInterval)
      }
      // 清理通知刷新定时器
      if (window.notificationRefreshInterval) {
        clearInterval(window.notificationRefreshInterval)
      }
      // 清理GTA6倒计时定时器
      clearInterval(gta6CountdownInterval)
      window.removeEventListener('cart-notification', handleNotification)
      window.removeEventListener('wishlist-notification', handleNotification)
    }
  }, [location.pathname])
  
  // GTA6发售日期（假设为2026年10月26日）
  const GTA6_RELEASE_DATE = new Date('2026-10-26T00:00:00')
  
  // 更新GTA6倒计时
  const updateGTA6Countdown = () => {
    const now = new Date()
    const diff = GTA6_RELEASE_DATE - now
    
    if (diff <= 0) {
      setGta6Countdown({ months: 0, days: 0, hours: 0, minutes: 0 })
      return
    }
    
    const months = Math.floor(diff / (1000 * 60 * 60 * 24 * 30))
    const days = Math.floor((diff % (1000 * 60 * 60 * 24 * 30)) / (1000 * 60 * 60 * 24))
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    
    setGta6Countdown({ months, days, hours, minutes })
  }

  // Banner 自动轮播
  useEffect(() => {
    if (bannerGames.length > 1) {
      const interval = setInterval(() => {
        setCurrentBanner((prev) => (prev + 1) % bannerGames.length)
      }, 5000) // 每5秒自动切换
      return () => clearInterval(interval)
    }
  }, [bannerGames.length])

  // Token刷新机制
  const refreshToken = async () => {
    const token = localStorage.getItem('token')
    if (!token) return
    
    try {
      const response = await fetch(`${AUTH_API_BASE}/api/auth/refresh`, {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      })
      
      if (response.ok) {
        const result = await response.json()
        if (result.code === 200 || result.success) {
          const newToken = result.data.token
          const newExpiry = result.data.expiry || new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
          
          localStorage.setItem('token', newToken)
          localStorage.setItem('tokenExpiry', newExpiry)
          console.log('Token刷新成功')
        }
      } else if (response.status === 401) {
        // Token已过期，强制登出
        console.log('Token已过期，自动登出')
        handleLogout()
        showWarning('登录已过期', '请重新登录')
      }
    } catch (error) {
      console.error('Token刷新失败:', error)
    }
  }

  // 从后端获取游戏数据
  const fetchGames = useCallback(async () => {
    try {
      console.log('[fetchGames] 开始请求 API')
      setLoading(true)
      const response = await fetch(`${MALL_API_BASE}/games/all-with-tags`)
      console.log('[fetchGames] API 响应状态:', response.status)
      
      const result = await response.json()
      console.log('[fetchGames] API 返回 code:', result.code, 'data 数量:', result.data?.length)
      
      if (result.code === 200 && result.data) {
        const gamesData = result.data.map(game => ({
          id: game.id,
          name: game.title,
          price: parseFloat(game.currentPrice),
          originalPrice: parseFloat(game.basePrice),
          category: game.category || '全部',
          image: game.coverImage || '',
          rating: game.rating || 0,
          tags: game.tags || [],
          tagIds: game.tagIds || [],
          discount: game.discountRate || 0,
          featured: game.isFeatured || false,
          description: game.shortDescription,
          fullDescription: game.fullDescription,
          bannerImage: game.bannerImage,
          screenshots: game.screenshots,
          trailerUrl: game.trailerUrl,
          developer: game.developer,
          publisher: game.publisher,
          releaseDate: game.releaseDate,
          totalSales: game.totalSales,
          totalReviews: game.totalReviews
        }))
        console.log('[fetchGames] 设置 games 状态，数量:', gamesData.length)
        setGames(gamesData)
      } else {
        console.error('[fetchGames] 获取游戏列表失败:', result.message)
      }
    } catch (error) {
      console.error('[fetchGames] 获取游戏数据失败:', error)
    } finally {
      setLoading(false)
      console.log('[fetchGames] loading 设为 false, 当前 games 数量:', games.length)
    }
  }, [])
  
  // 获取标签列表
  const fetchTags = useCallback(async () => {
    try {
      const response = await fetch(`${MALL_API_BASE}/games/tags`)
      const result = await response.json()
      if (result.code === 200 && result.data) {
        setTags(result.data)
      }
    } catch (error) {
      console.error('获取标签失败:', error)
    }
  }, [])
  
  // 获取购物车数量
  const fetchCartCount = async () => {
    if (!currentUser) return
    try {
      const response = await fetch(`${MALL_API_BASE}/cart/count?userId=${currentUser.id}`)
      const result = await response.json()
      if (result.code === 200 && result.data) {
        // 更新购物车按钮显示的数量
        const countBtn = document.querySelector('.cart-btn')
        if (countBtn) {
          countBtn.textContent = `🛒 购物车 (${result.data.count})`
        }
      }
      // 同时获取购物车完整列表以更新 cartGameIds
      await fetchCartList()
    } catch (error) {
      console.error('获取购物车数量失败:', error)
    }
  }
  
  // 获取购物车完整列表
  const fetchCartList = async () => {
    if (!currentUser) return
    try {
      const response = await fetch(`${MALL_API_BASE}/cart?userId=${currentUser.id}`)
      const result = await response.json()
      if (result.code === 200 && result.data) {
        const gameIds = new Set(result.data.map(item => item.gameId))
        setCartGameIds(gameIds)
      }
    } catch (error) {
      console.error('获取购物车列表失败:', error)
    }
  }
  
  // 获取用户通知（静默刷新，不显示错误）
  const fetchNotifications = async () => {
    try {
      const token = localStorage.getItem('token')
      if (!token || !currentUser) return
      
      const response = await fetch(`${MALL_API_BASE}/notifications?userId=${currentUser.id}&unreadOnly=false`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      
      // 如果404或其他错误，静默失败，不影响用户体验
      if (!response.ok) {
        console.warn('[fetchNotifications] API请求失败:', response.status)
        return
      }
      
      const result = await response.json()
      
      if (result.code === 200 && result.data) {
        // 后端返回格式: { list: [...], unreadCount: X, total: N }
        const notificationList = result.data.list || []
        setNotifications(notificationList)
        // 计算未读数量
        const unread = result.data.unreadCount || notificationList.filter(n => !n.isRead).length
        setUnreadCount(unread)
      }
    } catch (error) {
      // 静默失败，不显示错误提示
      console.warn('[fetchNotifications] 获取通知失败:', error.message)
    }
  }
  
  // 标记通知为已读
  const markNotificationAsRead = async (notificationId) => {
    try {
      const token = localStorage.getItem('token')
      if (!token) return
      
      await fetch(`${MALL_API_BASE}/notifications/${notificationId}/read`, {
        method: 'PUT',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      })
      
      // 重新获取通知列表以更新未读数
      await fetchNotifications()
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  
  // 清除所有未读
  const clearAllUnread = async () => {
    try {
      const token = localStorage.getItem('token')
      if (!token || !currentUser) return
      
      await fetch(`${MALL_API_BASE}/notifications/mark-all-read?userId=${currentUser.id}`, {
        method: 'PUT',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      })
      
      // 重新获取通知列表以更新未读数
      await fetchNotifications()
      
      showSuccess('操作成功', '已清除所有未读消息')
    } catch (error) {
      console.error('清除未读失败:', error)
      showError('操作失败', '清除未读消息失败')
    }
  }

  // 获取用户余额
  const fetchUserBalance = async (userId) => {
    const uid = userId || currentUser?.id
    if (!uid) {
      console.warn('[fetchUserBalance] 用户ID为空')
      return
    }
    try {
      console.log('[fetchUserBalance] 开始获取余额, userId:', uid)
      // 从 mall-service 获取钱包余额
      const response = await fetch(`${MALL_API_BASE}/wallet/balance?userId=${uid}`)
      const result = await response.json()
      console.log('[fetchUserBalance] 响应:', result)
      if (result.code === 200 && result.data) {
        const newBalance = result.data.balance || 0
        console.log('[fetchUserBalance] 设置余额:', newBalance)
        setUserBalance(newBalance)
      } else {
        console.warn('[fetchUserBalance] 获取失败:', result.message)
      }
    } catch (error) {
      console.error('[fetchUserBalance] 获取余额失败:', error)
    }
  }

  // 处理登录
  const handleLogin = async (e) => {
    e.preventDefault()
    try {
      const response = await fetch(`${AUTH_API_BASE}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(loginForm)
      })
      const result = await response.json()
      if (result.code === 200 || result.success) {
        const data = result.data
        // 后端返回的是 LoginResponse: { userId, username, token, points, balance, currentServer }
        const user = {
          id: data.userId,
          username: data.username,
          points: data.points,
          balance: data.balance
        }
        localStorage.setItem('token', data.token)
        localStorage.setItem('user', JSON.stringify(user))
        
        // 如果勾选了记住我，保存账号密码
        if (loginForm.rememberMe) {
          localStorage.setItem('rememberedUsername', loginForm.username)
          localStorage.setItem('rememberedPassword', loginForm.password)
        } else {
          // 否则清除记住的信息
          localStorage.removeItem('rememberedUsername')
          localStorage.removeItem('rememberedPassword')
        }
        
        // 设置Token过期时间（24小时后）
        const expiryTime = new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
        localStorage.setItem('tokenExpiry', expiryTime)
        setCurrentUser(user)
        setIsLoggedIn(true)
        setShowLoginModal(false)
        showSuccess('登录成功', `欢迎回来，${user.username}！`)
        
        // 登录后获取完整的用户信息（包括头像）
        await fetchFullUserInfo(data.userId)
        
        // 登录后获取用户特定的数据
        fetchUserBalance(user.id)  // 传入用户ID
        fetchCartCount()
        
        // 调用父组件回调
        if (onUserLogin) {
          onUserLogin(user)
        }
      } else {
        showError('登录失败', result.message || '未知错误')
      }
    } catch (error) {
      showError('网络错误', '登录失败：' + error.message)
    }
  }
  
  // 发送邮箱验证码
  const sendEmailCode = async () => {
    if (!emailLoginForm.email) {
      showWarning('提示', '请输入邮箱地址')
      return
    }
    
    // 验证邮箱格式
    const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/
    if (!emailRegex.test(emailLoginForm.email)) {
      showWarning('提示', '邮箱格式不正确')
      return
    }
    
    try {
      setSendingCode(true)
      const response = await fetch(`${AUTH_API_BASE}/api/auth/send-email-code?email=${emailLoginForm.email}`, {
        method: 'POST'
      })
      const result = await response.json()
      
      if (result.code === 200) {
        showSuccess('发送成功', '验证码已发送到您的邮箱')
        // 开始倒计时
        setCountdown(60)
        const timer = setInterval(() => {
          setCountdown(prev => {
            if (prev <= 1) {
              clearInterval(timer)
              return 0
            }
            return prev - 1
          })
        }, 1000)
      } else {
        showError('发送失败', result.message || '验证码发送失败')
      }
    } catch (error) {
      showError('网络错误', '发送失败：' + error.message)
    } finally {
      setSendingCode(false)
    }
  }
  
  // 邮箱验证码登录
  const handleEmailLogin = async (e) => {
    e.preventDefault()
    
    if (!emailLoginForm.email || !emailLoginForm.code) {
      showWarning('提示', '请填写完整信息')
      return
    }
    
    try {
      const formData = new FormData()
      formData.append('email', emailLoginForm.email)
      formData.append('code', emailLoginForm.code)
      
      const response = await fetch(`${AUTH_API_BASE}/api/auth/login-email`, {
        method: 'POST',
        body: formData
      })
      const result = await response.json()
      
      if (result.code === 200 || result.success) {
        const data = result.data
        const user = {
          id: data.userId,
          username: data.username,
          points: data.points,
          balance: data.balance
        }
        localStorage.setItem('token', data.token)
        localStorage.setItem('user', JSON.stringify(user))
        
        const expiryTime = new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
        localStorage.setItem('tokenExpiry', expiryTime)
        setCurrentUser(user)
        setIsLoggedIn(true)
        setShowLoginModal(false)
        showSuccess('登录成功', `欢迎回来，${user.username}！`)
        
        await fetchFullUserInfo(data.userId)
        fetchUserBalance(user.id)
        fetchCartCount()
        
        if (onUserLogin) {
          onUserLogin(user)
        }
      } else {
        showError('登录失败', result.message || '验证码错误或已过期')
      }
    } catch (error) {
      showError('网络错误', '登录失败：' + error.message)
    }
  }

  // 处理注册
  const handleRegister = async (e) => {
    e.preventDefault()
    
    // 验证邮箱格式
    const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/
    if (!emailRegex.test(registerForm.email)) {
      showWarning('提示', '邮箱格式不正确')
      return
    }
    
    // 验证验证码
    if (!registerForm.code || registerForm.code.length !== 6) {
      showWarning('提示', '请输入6位验证码')
      return
    }
    
    if (registerForm.password !== registerForm.confirmPassword) {
      showWarning('密码不匹配', '两次输入的密码不一致')
      return
    }
    
    try {
      const response = await fetch(`${AUTH_API_BASE}/api/auth/register-with-email`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: registerForm.username,
          password: registerForm.password,
          email: registerForm.email,
          code: registerForm.code
        })
      })
      const result = await response.json()
      if (result.code === 200 || result.success) {
        showSuccess('注册成功', '请使用新账号登录')
        setIsRegisterMode(false)
        setRegisterForm({ username: '', password: '', confirmPassword: '', email: '', code: '' })
      } else {
        showError('注册失败', result.message || '未知错误')
      }
    } catch (error) {
      showError('网络错误', '注册失败：' + error.message)
    }
  }
  
  // 发送注册邮箱验证码
  const sendRegisterEmailCode = async () => {
    if (!registerForm.email) {
      showWarning('提示', '请输入邮箱地址')
      return
    }
    
    // 验证邮箱格式
    const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/
    if (!emailRegex.test(registerForm.email)) {
      showWarning('提示', '邮箱格式不正确')
      return
    }
    
    try {
      setSendingRegisterCode(true)
      const response = await fetch(`${AUTH_API_BASE}/api/auth/send-email-code?email=${registerForm.email}&purpose=register`, {
        method: 'POST'
      })
      const result = await response.json()
      
      if (result.code === 200) {
        showSuccess('发送成功', '验证码已发送到您的邮箱')
        // 开始倒计时
        setRegisterCountdown(60)
        const timer = setInterval(() => {
          setRegisterCountdown(prev => {
            if (prev <= 1) {
              clearInterval(timer)
              return 0
            }
            return prev - 1
          })
        }, 1000)
      } else {
        showError('发送失败', result.message || '验证码发送失败')
      }
    } catch (error) {
      showError('网络错误', '发送失败：' + error.message)
    } finally {
      setSendingRegisterCode(false)
    }
  }

  // 处理登出
  const handleLogout = async () => {
    try {
      const token = localStorage.getItem('token')
      await fetch(`${AUTH_API_BASE}/api/auth/logout`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      })
    } catch (error) {
      console.error('登出失败:', error)
    }
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('tokenExpiry')
    setCurrentUser(null)
    setIsLoggedIn(false)
    setUserBalance(0)
    setShowCart(false)
    setCart([])
    
    // 调用父组件回调
    if (onUserLogout) {
      onUserLogout()
    }
  }

  // 添加到购物车
  const addToCart = async (game) => {
    if (!isLoggedIn) {
      showWarning('需要登录', '请先登录后再添加商品到购物车')
      setShowLoginModal(true)
      return
    }
    
    // 如果已经在购物车中，不允许重复添加
    if (cartGameIds.has(game.id)) {
      showInfo('提示', '该游戏已在购物车中')
      return
    }
    
    // 如果已经购买过该游戏，不允许再次购买
    if (purchasedGameIds.has(game.id)) {
      showInfo('提示', '您已拥有该游戏，无需重复购买')
      return
    }
    
    try {
      const response = await fetch(`${MALL_API_BASE}/cart?userId=${currentUser.id}&gameId=${game.id}`, {
        method: 'POST'
      })
      const result = await response.json()
      
      if (result.code === 200) {
        // 立即更新本地状态
        setCartGameIds(prev => new Set([...prev, game.id]))
        showSuccess('添加成功', `${game.name} 已加入购物车`)
        fetchCartCount() // 更新购物车数量
      } else {
        showError('添加失败', result.message || '未知错误')
      }
    } catch (error) {
      showError('网络错误', '添加失败：' + error.message)
    }
  }
  
  // 获取已购游戏列表
  const fetchPurchasedGames = async (userId) => {
    if (!userId) return
    
    try {
      const response = await fetch(`${MALL_API_BASE}/orders/purchased-games?userId=${userId}`)
      const result = await response.json()
      
      if (result.code === 200 && result.data) {
        const purchasedIds = new Set(result.data.map(g => g.id))
        setPurchasedGameIds(purchasedIds)
      }
    } catch (error) {
      console.error('获取已购游戏列表失败:', error)
    }
  }
  
  // 获取完整的用户信息（包括头像）
  const fetchFullUserInfo = async (userId) => {
    if (!userId) return
    
    try {
      const token = localStorage.getItem('token')
      const response = await fetch(`${AUTH_API_BASE}/api/auth/info`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      const result = await response.json()
      
      if (result.code === 200 && result.data) {
        const fullUserInfo = {
          id: result.data.id,
          username: result.data.username,
          nickname: result.data.nickname,
          email: result.data.email,
          phone: result.data.phone,
          signature: result.data.signature,
          avatarUrl: result.data.avatarUrl,
          points: result.data.points,
          balance: result.data.balance
        }
        
        console.log('完整用户信息:', fullUserInfo)
        
        // 更新本地状态和localStorage
        setCurrentUser(fullUserInfo)
        localStorage.setItem('user', JSON.stringify(fullUserInfo))
      }
    } catch (error) {
      console.error('获取完整用户信息失败:', error)
    }
  }
  
  // 查看游戏详情
  const viewGameDetail = (gameId) => {
    setSelectedGameId(gameId)
    window.scrollTo(0, 0)
  }
  
  // 返回游戏列表
  const backToGameList = () => {
    setSelectedGameId(null)
    // 不重新加载数据，因为 games 状态还在
  }

  // 从购物车移除
  const removeFromCart = (gameId) => {
    setCart(cart.filter(item => item.id !== gameId))
  }

  // 计算购物车总价
  const cartTotal = cart.reduce((sum, game) => sum + game.price, 0)
  
  // 获取搜索建议（防抖）
  const fetchSearchSuggestions = async (keyword) => {
    if (!keyword || keyword.trim().length === 0) {
      setSearchSuggestions([])
      setShowSuggestions(false)
      return
    }
    
    try {
      const response = await fetch(`${MALL_API_BASE}/search/autocomplete?prefix=${encodeURIComponent(keyword)}&size=5`)
      const result = await response.json()
      
      if (result.code === 200 && result.data) {
        setSearchSuggestions(result.data)
        setShowSuggestions(true)
      } else {
        setSearchSuggestions([])
        setShowSuggestions(false)
      }
    } catch (error) {
      console.error('获取搜索建议失败:', error)
      setSearchSuggestions([])
      setShowSuggestions(false)
    }
  }
  
  // 执行ES搜索
  const executeSearch = async (keyword) => {
    if (!keyword || keyword.trim().length === 0) {
      setSearchResults(null)
      return
    }
    
    try {
      // 不设置全局loading，避免页面闪烁
      const params = new URLSearchParams({
        keyword: keyword,
        page: 0,
        size: 100,
        sortBy: 'relevance',
        order: 'desc'
      })
      
      const response = await fetch(`${MALL_API_BASE}/search/games?${params}`)
      const result = await response.json()
      
      if (result.code === 200 && result.data) {
        setSearchResults(result.data)
        // 搜索完成后，自动滚动到游戏列表区域
        setTimeout(() => {
          const mainContent = document.querySelector('.main-content')
          if (mainContent) {
            mainContent.scrollIntoView({ behavior: 'smooth', block: 'start' })
          }
        }, 50) // 减少延迟，更快响应
      } else {
        setSearchResults(null)
      }
    } catch (error) {
      console.error('ES搜索失败:', error)
      setSearchResults(null)
    }
  }
  
  // 处理搜索输入变化（带防抖）
  const handleSearchChange = (e) => {
    const value = e.target.value
    setSearchQuery(value)
    
    // 清除之前的定时器
    if (searchDebounceTimer) {
      clearTimeout(searchDebounceTimer)
    }
    
    // 如果清空搜索，立即清除结果
    if (!value || value.trim().length === 0) {
      setSearchResults(null)
      setSearchSuggestions([])
      setShowSuggestions(false)
      return
    }
    
    // 设置新的防抖定时器（500ms，减少请求频率）
    const timer = setTimeout(() => {
      fetchSearchSuggestions(value)
      executeSearch(value)
    }, 500)
    
    setSearchDebounceTimer(timer)
  }
  
  // 选择搜索建议
  const handleSelectSuggestion = (suggestion) => {
    setSearchQuery(suggestion.title)
    setShowSuggestions(false)
    setSearchSuggestions([])
    // 执行搜索
    executeSearch(suggestion.title)
  }
  
  // 点击外部关闭搜索建议
  useEffect(() => {
    const handleClickOutside = (event) => {
      const searchBox = document.querySelector('.search-box')
      if (searchBox && !searchBox.contains(event.target)) {
        setShowSuggestions(false)
      }
    }
    
    document.addEventListener('mousedown', handleClickOutside)
    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
    }
  }, [])
  
  // 当分类或标签变化时，清除ES搜索结果
  useEffect(() => {
    if (activeCategory !== '全部' || selectedTag || minRating > 0 || priceRange[0] > 0 || priceRange[1] < 500) {
      setSearchResults(null)
    }
  }, [activeCategory, selectedTag, minRating, priceRange])
  
  // 监听滚动，显示/隐藏返回顶部按钮
  useEffect(() => {
    const handleScroll = () => {
      // 当滚动超过300px时显示按钮
      if (window.scrollY > 300) {
        setShowBackToTop(true)
      } else {
        setShowBackToTop(false)
      }
    }
    
    window.addEventListener('scroll', handleScroll, { passive: true })
    return () => {
      window.removeEventListener('scroll', handleScroll)
    }
  }, [])
  
  // 返回顶部函数
  const scrollToTop = () => {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
  }

  // 处理购买
  const handlePurchase = async () => {
    if (!isLoggedIn) {
      showWarning('需要登录', '请先登录后再结算')
      setShowLoginModal(true)
      return
    }
    if (userBalance < cartTotal) {
      showError('余额不足', '请充值后再试')
      return
    }
    try {
      showSuccess('购买成功', `消费 ￥${cartTotal}，祝您游戏愉快！`)
      setUserBalance(userBalance - cartTotal)
      setCart([])
      setShowCart(false)
    } catch (error) {
      showError('购买失败', error.message)
    }
  }

  // 计算显示的游戏列表
  const getDisplayGames = () => {
    // 如果有ES搜索结果，使用ES结果
    if (searchResults && searchResults.results) {
      return searchResults.results.map(game => ({
        id: game.id,
        name: game.title,
        price: parseFloat(game.currentPrice),
        originalPrice: parseFloat(game.basePrice),
        discount: game.discountRate || 0,
        rating: game.rating || 0,
        image: game.coverImage || '🎮',
        category: game.categories && game.categories.length > 0 ? game.categories[0] : '游戏',
        tags: game.tags || [],
        tagIds: [],
        featured: game.isFeatured || false,
        description: game.shortDescription || '',
        bannerImage: game.coverImage,
        releaseDate: game.releaseDate,
        totalSales: game.totalSales,
        totalReviews: game.ratingCount || 0
      }))
    }
    
    // 否则使用本地过滤
    return games.filter(game => {
      const matchesCategory = activeCategory === '全部' || game.category === activeCategory
      const matchesSearch = searchQuery === '' || game.name.toLowerCase().includes(searchQuery.toLowerCase())
      // 标签筛选：检查游戏是否包含选中的标签
      const matchesTag = !selectedTag || (game.tagIds && game.tagIds.includes(selectedTag))
      // 价格筛选
      const matchesPrice = game.price >= priceRange[0] && game.price <= priceRange[1]
      // 评分筛选
      const matchesRating = game.rating >= minRating
      return matchesCategory && matchesSearch && matchesTag && matchesPrice && matchesRating
    }).sort((a, b) => {
      switch(sortBy) {
        case 'price_asc':
          return a.price - b.price
        case 'price_desc':
          return b.price - a.price
        case 'rating':
          return b.rating - a.rating
        case 'name':
          return a.name.localeCompare(b.name, 'zh-CN')
        case 'discount':
          return b.discount - a.discount
        default:
          return 0
      }
    })
  }
  
  const filteredGames = getDisplayGames()
  const featuredGames = games.filter(g => g.featured)

  return (
    <div className="app">
      {/* 通知容器 */}
      <NotificationContainer 
        notifications={toastNotifications} 
        removeNotification={removeNotification} 
      />
      
      {/* 如果选择了游戏，显示详情页 */}
      {selectedGameId ? (
        <GameDetail 
          gameId={selectedGameId}
          onBack={backToGameList}
          onAddToCart={addToCart}
          isInCart={(gameId) => cart.some(item => item.id === gameId)}
          currentUser={currentUser}
        />
      ) : (
        <>
      {/* 顶部导航栏 */}
      <nav className="navbar">
        <div className="navbar-container">
          <div className="navbar-logo">
            <span className="logo-icon">🎮</span>
            <h1>VICE CITY STORE</h1>
          </div>
          
          <div className="navbar-categories">
            <div className="search-box" style={{ position: 'relative' }}>
              <span className="search-icon">🔍</span>
              <input 
                type="text" 
                placeholder="搜索游戏..."
                value={searchQuery}
                onChange={handleSearchChange}
                onFocus={() => searchQuery && searchSuggestions.length > 0 && setShowSuggestions(true)}
                className="search-input"
              />
              
              {/* 搜索建议下拉框 - 使用portal确保不被遮挡 */}
              {showSuggestions && searchSuggestions.length > 0 && (
                <div className="search-suggestions" style={{ 
                  position: 'absolute',
                  top: '100%',
                  left: 0,
                  right: 0,
                  zIndex: 9999,
                  marginTop: '8px'
                }}>
                  {searchSuggestions.map((suggestion) => (
                    <div 
                      key={suggestion.id} 
                      className="suggestion-item"
                      onClick={() => handleSelectSuggestion(suggestion)}
                    >
                      <div className="suggestion-cover">
                        {suggestion.coverImage ? (
                          <img src={suggestion.coverImage} alt={suggestion.title} />
                        ) : (
                          <div className="cover-placeholder">🎮</div>
                        )}
                      </div>
                      <div className="suggestion-info">
                        <div className="suggestion-title">{suggestion.title}</div>
                        <div className="suggestion-meta">
                          <span className="suggestion-category">{suggestion.category}</span>
                          {suggestion.tags && suggestion.tags.length > 0 && (
                            <span className="suggestion-tags">
                              {suggestion.tags.join(' · ')}
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="suggestion-price">
                        {suggestion.discountRate > 0 ? (
                          <>
                            <span className="original-price">¥{parseFloat(suggestion.basePrice).toFixed(2)}</span>
                            <span className="current-price">¥{parseFloat(suggestion.currentPrice).toFixed(2)}</span>
                          </>
                        ) : (
                          <span className="current-price">¥{parseFloat(suggestion.currentPrice).toFixed(2)}</span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div className="navbar-user">
            {isLoggedIn ? (
              <>
                <button className="nav-link-btn" onClick={() => navigate('/friends')}>
                  👥 好友
                </button>
                <button className="nav-link-btn" onClick={() => navigate('/library')}>
                  🎮 游戏库
                </button>
                <button className="nav-link-btn" onClick={() => navigate('/orders')}>
                  📦 订单
                </button>
                {/* 通知铃铛 */}
                <div 
                  className="notification-bell-wrapper"
                  onMouseEnter={() => {
                    if (notificationTimer) {
                      clearTimeout(notificationTimer)
                      setNotificationTimer(null)
                    }
                    setShowNotificationPanel(true)
                    // 打开时获取最新通知
                    fetchNotifications()
                  }}
                  onMouseLeave={() => {
                    const timer = setTimeout(() => {
                      setShowNotificationPanel(false)
                    }, 300)
                    setNotificationTimer(timer)
                  }}
                >
                  <button className="notification-bell-btn">
                    🔔
                    {unreadCount > 0 && (
                      <span className="notification-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
                    )}
                  </button>
                  
                  {/* 通知下拉面板 */}
                  <div className={`notification-panel ${showNotificationPanel ? 'show' : ''}`}>
                    <div className="notification-header">
                      <h3>📬 消息中心</h3>
                      {unreadCount > 0 && (
                        <button className="clear-unread-btn" onClick={clearAllUnread}>
                          ✓ 清除未读
                        </button>
                      )}
                    </div>
                    
                    <div className="notification-list">
                      {!Array.isArray(notifications) || notifications.length === 0 ? (
                        <div className="empty-notifications">
                          <div className="empty-icon">📭</div>
                          <p>暂无消息</p>
                        </div>
                      ) : (
                        notifications.slice(0, 10).map((notification) => (
                          <div 
                            key={notification.id} 
                            className={`notification-item ${!notification.isRead ? 'unread' : ''}`}
                            onClick={() => markNotificationAsRead(notification.id)}
                          >
                            <div className="notification-icon">
                              {notification.type === 'promotion' ? '🎉' :
                               notification.type === 'system' ? '⚙️' :
                               notification.type === 'order' ? '📦' : '🔔'}
                            </div>
                            <div className="notification-content">
                              <div className="notification-title">{notification.title}</div>
                              <div className="notification-message">{notification.message}</div>
                              <div className="notification-time">
                                {new Date(notification.createdAt).toLocaleString('zh-CN', {
                                  month: '2-digit',
                                  day: '2-digit',
                                  hour: '2-digit',
                                  minute: '2-digit'
                                })}
                              </div>
                            </div>
                            {!notification.isRead && (
                              <div className="unread-dot"></div>
                            )}
                          </div>
                        ))
                      )}
                    </div>
                    
                    {Array.isArray(notifications) && notifications.length > 0 && (
                      <div className="notification-footer">
                        <button onClick={() => navigate('/notifications')}>
                          查看全部消息 →
                        </button>
                      </div>
                    )}
                  </div>
                </div>
                
                <button className="wishlist-btn" onClick={() => setShowWishlist(!showWishlist)}>
                  ❤️ 愿望单
                </button>
                <button className="cart-btn" onClick={() => setShowCart(!showCart)}>
                  🛒 购物车
                </button>
                <div className="user-info">
                  <span className="wallet">💵 ¥{userBalance.toFixed(2)}</span>
                  <button className="recharge-btn" onClick={() => navigate('/wallet/recharge')}>
                    💳 充值
                  </button>
                  <div 
                    className="dropdown" 
                    onMouseEnter={() => {
                      if (dropdownTimer) {
                        clearTimeout(dropdownTimer)
                        setDropdownTimer(null)
                      }
                      setShowDropdown(true)
                    }}
                    onMouseLeave={() => {
                      const timer = setTimeout(() => {
                        setShowDropdown(false)
                      }, 300) // 300ms 延迟隐藏
                      setDropdownTimer(timer)
                    }}
                  >
                    <button className="user-avatar">
                      {currentUser?.avatarUrl ? (
                        <img src={currentUser.avatarUrl} alt="Avatar" style={{ width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover' }} />
                      ) : (
                        '👤'
                      )}
                    </button>
                    <div className={`dropdown-menu ${showDropdown ? 'show' : ''}`}>
                      <button onClick={() => navigate('/profile')}>个人中心</button>
                      <button onClick={() => navigate('/notifications')}>通知中心</button>
                      <button onClick={() => navigate('/wallet/transactions')}>账单明细</button>
                      <button onClick={handleLogout}>退出登录</button>
                    </div>
                  </div>
                </div>
              </>
            ) : (
              <button className="login-btn" onClick={() => setShowLoginModal(true)}>
                👤 登录/注册
              </button>
            )}
          </div>
        </div>
      </nav>

      {/* Banner 轮播图 - 搜索时隐藏 */}
      {!searchResults && bannerGames.length > 0 && (
        <section className="banner-carousel">
          <div className="banner-container">
            {bannerGames.map((game, index) => (
              <div 
                key={game.id} 
                className={`banner-slide ${index === currentBanner ? 'active' : ''}`}
                onClick={() => viewGameDetail(game.id)}
              >
                <div className="banner-bg" style={{ backgroundImage: `url(${game.bannerImage || game.image})` }}></div>
                <div className="banner-content">
                  <div className="banner-tags">
                    {game.discount > 0 && <span className="banner-discount-badge">-{game.discount}%</span>}
                    {game.tags.slice(0, 3).map((tag, i) => (
                      <span key={i} className="banner-tag">{tag}</span>
                    ))}
                  </div>
                  <h2 className="banner-title">{game.name}</h2>
                  <p className="banner-description">{game.description || '立即体验这款精彩游戏'}</p>
                  <div className="banner-price">
                    {game.price === 0 ? (
                      <span className="banner-current">免费开玩</span>
                    ) : game.discount > 0 ? (
                      <>
                        <span className="banner-original">¥{game.originalPrice}</span>
                        <span className="banner-current">¥{game.price}</span>
                      </>
                    ) : (
                      <span className="banner-current">¥{game.price}</span>
                    )}
                  </div>
                  <button 
                    className="banner-buy-btn"
                    onClick={(e) => {
                      e.stopPropagation()
                      addToCart(game)
                    }}
                  >
                    {cartGameIds.has(game.id) ? '✓ 已在购物车' : '🛒 立即购买'}
                  </button>
                </div>
              </div>
            ))}
          </div>
          
          {/* 轮播指示器 */}
          <div className="banner-indicators">
            {bannerGames.map((_, index) => (
              <button
                key={index}
                className={`indicator ${index === currentBanner ? 'active' : ''}`}
                onClick={() => setCurrentBanner(index)}
              />
            ))}
          </div>
          
          {/* 轮播控制按钮 */}
          <button 
            className="banner-arrow banner-prev"
            onClick={() => setCurrentBanner((prev) => (prev - 1 + bannerGames.length) % bannerGames.length)}
          >
            ‹
          </button>
          <button 
            className="banner-arrow banner-next"
            onClick={() => setCurrentBanner((prev) => (prev + 1) % bannerGames.length)}
          >
            ›
          </button>
        </section>
      )}

      {/* 精选推荐（小卡片）- 搜索时隐藏 */}
      {!searchResults && featuredGames.length > 0 && (
        <section className="featured-section">
          <div className="featured-header">
            <h2>🔥 精选推荐</h2>
            <p className="section-description">编辑精选的优质游戏，值得入手</p>
          </div>
          <div className="featured-grid">
            {featuredGames.slice(0, 4).map((game) => (
              <div key={game.id} className="featured-card" onClick={() => viewGameDetail(game.id)}>
                <div className="featured-image">
                  {game.image && game.image.startsWith('/') ? (
                    <img 
                      src={game.image} 
                      alt={game.name}
                      style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                      onError={(e) => {
                        e.target.style.display = 'none'
                        e.target.parentElement.textContent = '🎮'
                      }}
                    />
                  ) : (
                    game.image || '🎮'
                  )}
                </div>
                <div className="featured-info">
                  <h3>{game.name}</h3>
                  <div className="featured-tags">
                    {game.tags.map((tag, i) => (
                      <span key={i} className="tag">{tag}</span>
                    ))}
                  </div>
                  <div className="featured-price">
                    {game.discount > 0 ? (
                      <>
                        <span className="original-price">¥{game.originalPrice}</span>
                        <span className="discount-badge">-{game.discount}%</span>
                        <span className="current-price">¥{game.price}</span>
                      </>
                    ) : (
                      <span className="current-price">¥{game.price}</span>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* 标签筛选 */}
      {tags.length > 0 && (
        <div className="tag-filter-bar">
          <button
            className={`tag-btn ${!selectedTag ? 'active' : ''}`}
            onClick={() => setSelectedTag(null)}
          >
            全部标签
          </button>
          {tags.slice(0, 10).map((tag) => (
            <button
              key={tag.id}
              className={`tag-btn ${selectedTag === tag.id ? 'active' : ''}`}
              onClick={() => setSelectedTag(tag.id)}
              style={{ borderColor: selectedTag === tag.id ? tag.color : undefined }}
            >
              {tag.name}
            </button>
          ))}
          
          {/* 高级筛选按钮 */}
          <button 
            className={`advanced-filter-toggle ${showAdvancedFilter ? 'active' : ''}`}
            onClick={() => setShowAdvancedFilter(!showAdvancedFilter)}
          >
            ⚙️ 高级筛选
          </button>
        </div>
      )}

      {/* 高级筛选面板 */}
      {showAdvancedFilter && (
        <div className="advanced-filter-panel">
          <div className="filter-group">
            <label className="filter-label">💰 价格范围</label>
            <div className="price-range-inputs">
              <input 
                type="number" 
                value={priceRange[0]} 
                onChange={(e) => setPriceRange([Number(e.target.value), priceRange[1]])}
                placeholder="最低价"
                min="0"
              />
              <span>—</span>
              <input 
                type="number" 
                value={priceRange[1]} 
                onChange={(e) => setPriceRange([priceRange[0], Number(e.target.value)])}
                placeholder="最高价"
                min="0"
              />
            </div>
          </div>
          
          <div className="filter-group">
            <label className="filter-label">⭐ 最低评分</label>
            <div className="rating-selector">
              {[0, 5, 6, 7, 8, 9].map((rating) => (
                <button
                  key={rating}
                  className={`rating-btn ${minRating === rating ? 'active' : ''}`}
                  onClick={() => setMinRating(rating)}
                >
                  {rating === 0 ? '全部' : `${rating}+`}
                </button>
              ))}
            </div>
          </div>
          
          <div className="filter-group">
            <label className="filter-label">📊 排序方式</label>
            <select value={sortBy} onChange={(e) => setSortBy(e.target.value)} className="sort-select">
              <option value="default">默认排序</option>
              <option value="price_asc">价格从低到高</option>
              <option value="price_desc">价格从高到低</option>
              <option value="rating">评分最高</option>
              <option value="name">名称A-Z</option>
              <option value="discount">折扣力度</option>
            </select>
          </div>
          
          <button className="reset-filter-btn" onClick={() => {
            setPriceRange([0, 500])
            setMinRating(0)
            setSortBy('default')
          }}>
            🔄 重置筛选
          </button>
        </div>
      )}

      {/* 主内容区 */}
      <main className="main-content">
        <div className="content-wrapper">
          {/* 无搜索结果 */}
          {!loading && searchResults && searchResults.results && searchResults.results.length === 0 && (
            <div className="no-results">
              <div className="no-results-icon">🔍</div>
              <h3>未找到相关游戏</h3>
              <p>尝试使用其他关键词或清除筛选条件</p>
              <button onClick={() => {
                setSearchQuery('')
                setSearchResults(null)
              }} className="clear-search-btn">
                清除搜索
              </button>
            </div>
          )}
          
          {/* 有搜索结果或正常浏览 */}
          {(filteredGames.length > 0 || (!searchResults && !loading)) && (
            <>
              <div className="section-header">
                <h2>
                  {searchResults 
                    ? `ES搜索结果："${searchQuery}"` 
                    : (activeCategory === '全部' 
                      ? (searchQuery ? `搜索结果："${searchQuery}"` : '全部游戏') 
                      : `${activeCategory}游戏`)}
                </h2>
                <span className="result-count">
                  {filteredGames.length} 款游戏
                  {searchResults && `（共 ${searchResults.total} 条结果）`}
                </span>
                <p className="section-description">
                  {searchResults 
                    ? `基于 Elasticsearch 智能搜索，支持中文分词、拼音搜索`
                    : (selectedTag 
                      ? `筛选标签：${tags.find(t => t.id === selectedTag)?.name || ''}` 
                      : '浏览所有可用游戏')}
                </p>
              </div>
              
              <div className="games-grid">
                {filteredGames.map((game) => (
                  <div key={game.id} className="game-card" onClick={() => viewGameDetail(game.id)}>
                    <div className="game-image">
                      {game.image && game.image.startsWith('/') ? (
                        <img 
                          src={game.image} 
                          alt={game.name}
                          style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                          onError={(e) => {
                            e.target.style.display = 'none'
                            e.target.parentElement.textContent = '🎮'
                          }}
                        />
                      ) : (
                        game.image || '🎮'
                      )}
                    </div>
                    <div className="game-info">
                      <h3 className="game-name">{game.name}</h3>
                      <div className="game-rating">⭐ {game.rating}</div>
                      <div className="game-tags">
                        {game.tags.map((tag, i) => (
                          <span key={i} className="tag-small">{tag}</span>
                        ))}
                      </div>
                      <div className="game-footer">
                        <div className="game-price">
                          {game.discount > 0 ? (
                            <>
                              <span className="original">¥{game.originalPrice}</span>
                              <span className="discount">-{game.discount}%</span>
                              <span className="current">¥{game.price}</span>
                            </>
                          ) : (
                            <span className="current">¥{game.price}</span>
                          )}
                        </div>
                        <button 
                          className={`buy-btn ${cartGameIds.has(game.id) ? 'in-cart' : ''} ${purchasedGameIds.has(game.id) ? 'purchased' : ''}`}
                          onClick={(e) => {
                            e.stopPropagation()
                            addToCart(game)
                          }}
                          disabled={cartGameIds.has(game.id) || purchasedGameIds.has(game.id)}
                        >
                          {purchasedGameIds.has(game.id) ? '✓ 已在游戏库' : (cartGameIds.has(game.id) ? '✓ 已在购物车' : '加入购物车')}
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>
      </main>

      {/* 页脚 */}
      <footer className="footer">
        <div className="footer-content">
          <div className="footer-section">
            <h4>关于我们</h4>
            <p>VICE CITY STORE 是最受欢迎的游戏数字发行平台</p>
          </div>
          <div className="footer-section">
            <h4>客户服务</h4>
            <ul>
              <li>帮助中心</li>
              <li>退款政策</li>
              <li>联系我们</li>
            </ul>
          </div>
          <div className="footer-section">
            <h4>关注我们</h4>
            <div className="social-links">
              <span>📘</span>
              <span>🐦</span>
              <span>📸</span>
            </div>
          </div>
        </div>
        <div className="footer-bottom">
          <p>© 2026 VICE CITY STORE. All rights reserved.</p>
        </div>
      </footer>
      
      {/* 返回顶部按钮 */}
      {showBackToTop && (
        <button 
          className="back-to-top"
          onClick={scrollToTop}
          title="返回顶部"
        >
          🚀
        </button>
      )}
        </>
      )}

      {/* GTA6倒计时弹窗 */}
      {showGTA6Modal && (
        <div className="gta6-modal-overlay" onClick={() => setShowGTA6Modal(false)}>
          <div className="gta6-modal-content" onClick={(e) => e.stopPropagation()}>
            <button 
              className="gta6-modal-close" 
              onClick={() => setShowGTA6Modal(false)}
            >
              ×
            </button>
            <div className="gta6-modal-body">
              <h2 className="gta6-modal-title">距离GTA6发售还有</h2>
              <div className="gta6-countdown">
                <div className="countdown-item">
                  <span className="countdown-number">{gta6Countdown.months}</span>
                  <span className="countdown-label">月</span>
                </div>
                <div className="countdown-separator">:</div>
                <div className="countdown-item">
                  <span className="countdown-number">{gta6Countdown.days}</span>
                  <span className="countdown-label">天</span>
                </div>
                <div className="countdown-separator">:</div>
                <div className="countdown-item">
                  <span className="countdown-number">{gta6Countdown.hours}</span>
                  <span className="countdown-label">时</span>
                </div>
                <div className="countdown-separator">:</div>
                <div className="countdown-item">
                  <span className="countdown-number">{gta6Countdown.minutes}</span>
                  <span className="countdown-label">分</span>
                </div>
              </div>
              <button 
                className="gta6-modal-confirm" 
                onClick={() => setShowGTA6Modal(false)}
              >
                我知道了
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 登录/注册模态框 */}
      {showLoginModal && (
        <div className="modal-overlay" onClick={() => {
          setShowLoginModal(false)
          // 关闭模态框时清空所有验证码
          setEmailLoginForm(prev => ({...prev, code: ''}))
          setRegisterForm(prev => ({...prev, code: ''}))
          setCountdown(0)
          setRegisterCountdown(0)
        }}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <button className="modal-close" onClick={() => {
              setShowLoginModal(false)
              // 清空所有验证码
              setEmailLoginForm(prev => ({...prev, code: ''}))
              setRegisterForm(prev => ({...prev, code: ''}))
              setCountdown(0)
              setRegisterCountdown(0)
            }}>×</button>
            <h2>{isRegisterMode ? '用户注册' : '用户登录'}</h2>
            
            {isRegisterMode ? (
              <form onSubmit={handleRegister}>
                <div className="form-group">
                  <label>用户名 *</label>
                  <input
                    type="text"
                    value={registerForm.username}
                    onChange={(e) => setRegisterForm({...registerForm, username: e.target.value})}
                    placeholder="3-20 位字母、数字或下划线"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>邮箱 *</label>
                  <div className="code-input-group">
                    <input
                      type="email"
                      value={registerForm.email}
                      onChange={(e) => setRegisterForm({...registerForm, email: e.target.value})}
                      placeholder="请输入邮箱地址"
                      required
                    />
                    <button 
                      type="button" 
                      className="send-code-btn"
                      onClick={sendRegisterEmailCode}
                      disabled={sendingRegisterCode || registerCountdown > 0}
                    >
                      {registerCountdown > 0 ? `${registerCountdown}s` : (sendingRegisterCode ? '发送中...' : '获取验证码')}
                    </button>
                  </div>
                </div>
                <div className="form-group">
                  <label>验证码 *</label>
                  <input
                    type="text"
                    value={registerForm.code}
                    onChange={(e) => setRegisterForm({...registerForm, code: e.target.value})}
                    placeholder="请输入6位验证码"
                    maxLength="6"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>密码 *</label>
                  <input
                    type="password"
                    value={registerForm.password}
                    onChange={(e) => setRegisterForm({...registerForm, password: e.target.value})}
                    placeholder="6-50 位密码"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>确认密码 *</label>
                  <input
                    type="password"
                    value={registerForm.confirmPassword}
                    onChange={(e) => setRegisterForm({...registerForm, confirmPassword: e.target.value})}
                    placeholder="请再次输入密码"
                    required
                  />
                </div>
                <button type="submit" className="submit-btn">注册</button>
                <p className="modal-footer">
                  已有账号？<button type="button" onClick={() => {
                    setIsRegisterMode(false)
                    // 清空注册表单的验证码
                    setRegisterForm(prev => ({...prev, code: ''}))
                    setRegisterCountdown(0)
                  }}>去登录</button>
                </p>
              </form>
            ) : (
              <>
                {/* 登录方式切换 */}
                <div className="login-mode-tabs">
                  <button 
                    className={`tab-btn ${loginMode === 'password' ? 'active' : ''}`}
                    onClick={() => setLoginMode('password')}
                  >
                    密码登录
                  </button>
                  <button 
                    className={`tab-btn ${loginMode === 'email' ? 'active' : ''}`}
                    onClick={() => setLoginMode('email')}
                  >
                    邮箱验证码
                  </button>
                </div>
                
                {loginMode === 'password' ? (
                  <form onSubmit={handleLogin}>
                    <div className="form-group">
                      <label>用户名</label>
                      <input
                        type="text"
                        value={loginForm.username}
                        onChange={(e) => setLoginForm({...loginForm, username: e.target.value})}
                        required
                      />
                    </div>
                    <div className="form-group">
                      <label>密码</label>
                      <input
                        type="password"
                        value={loginForm.password}
                        onChange={(e) => setLoginForm({...loginForm, password: e.target.value})}
                        required
                      />
                    </div>
                    <div className="form-group remember-me">
                      <input
                        type="checkbox"
                        id="rememberMe"
                        checked={loginForm.rememberMe}
                        onChange={(e) => setLoginForm({...loginForm, rememberMe: e.target.checked})}
                      />
                      <label htmlFor="rememberMe">记住我</label>
                    </div>
                    <button type="submit" className="submit-btn">登录</button>
                    <p className="modal-footer">
                      没有账号？<button type="button" onClick={() => {
                        setIsRegisterMode(true)
                        // 清空登录表单的验证码
                        setEmailLoginForm(prev => ({...prev, code: ''}))
                        setCountdown(0)
                      }}>去注册</button>
                    </p>
                  </form>
                ) : (
                  <form onSubmit={handleEmailLogin}>
                    <div className="form-group">
                      <label>邮箱</label>
                      <input
                        type="email"
                        value={emailLoginForm.email}
                        onChange={(e) => setEmailLoginForm({...emailLoginForm, email: e.target.value})}
                        placeholder="请输入邮箱地址"
                        required
                      />
                    </div>
                    <div className="form-group">
                      <label>验证码</label>
                      <div className="code-input-group">
                        <input
                          type="text"
                          value={emailLoginForm.code}
                          onChange={(e) => setEmailLoginForm({...emailLoginForm, code: e.target.value})}
                          placeholder="请输入6位验证码"
                          maxLength="6"
                          required
                        />
                        <button 
                          type="button" 
                          className="send-code-btn"
                          onClick={sendEmailCode}
                          disabled={sendingCode || countdown > 0}
                        >
                          {countdown > 0 ? `${countdown}s` : (sendingCode ? '发送中...' : '获取验证码')}
                        </button>
                      </div>
                    </div>
                    <button type="submit" className="submit-btn">登录</button>
                    <p className="modal-footer">
                      没有账号？<button type="button" onClick={() => {
                        setIsRegisterMode(true)
                        // 清空邮箱登录表单的验证码
                        setEmailLoginForm(prev => ({...prev, code: ''}))
                        setCountdown(0)
                      }}>去注册</button>
                    </p>
                  </form>
                )}
              </>
            )}
          </div>
        </div>
      )}

      {/* 购物车侧边栏 */}
      <CartSidebar 
        isOpen={showCart} 
        onClose={() => setShowCart(false)}
        currentUser={currentUser}
      />
      
      {/* 愿望单侧边栏 */}
      <WishlistSidebar 
        isOpen={showWishlist} 
        onClose={() => setShowWishlist(false)}
        currentUser={currentUser}
      />
    </div>
  )
}

export default App
