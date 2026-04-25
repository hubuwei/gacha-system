import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import SockJS from 'sockjs-client'
import Stomp from '@stomp/stompjs'
import './NewPages.css'

const API_BASE = '/api'

function FriendSystem() {
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState('friends') // friends, applications, blacklist, activities
  
  // 好友列表
  const [friends, setFriends] = useState([])
  const [friendPage, setFriendPage] = useState(1)
  const [friendTotal, setFriendTotal] = useState(0)
  
  // 好友申请
  const [applications, setApplications] = useState([])
  const [appPage, setAppPage] = useState(1)
  const [appTotal, setAppTotal] = useState(0)
  
  // 黑名单
  const [blacklist, setBlacklist] = useState([])
  
  // 动态
  const [activities, setActivities] = useState([])
  const [activityPage, setActivityPage] = useState(1)
  const [activityTotal, setActivityTotal] = useState(0)
  
  // 搜索用户
  const [searchKeyword, setSearchKeyword] = useState('')
  const [searchResults, setSearchResults] = useState([])
  const [showSearchResults, setShowSearchResults] = useState(false)
  
  // 在线状态
  const [onlineStatuses, setOnlineStatuses] = useState({})
  
  // 加载状态
  const [loading, setLoading] = useState(false)
  
  // WebSocket连接
  const [wsConnected, setWsConnected] = useState(false)
  
  useEffect(() => {
    fetchFriends()
    fetchApplications()
    fetchBlacklist()
    fetchActivities()
    connectWebSocket()
    
    return () => {
      if (window.ws) {
        window.ws.disconnect()
      }
    }
  }, [])
  
  // WebSocket连接
  const connectWebSocket = () => {
    try {
      const SockJS = require('sockjs-client')
      const Stomp = require('@stomp/stompjs')
      
      const socket = new SockJS(`${API_BASE}/ws`)
      const stompClient = Stomp.over(socket)
      
      stompClient.connect({}, 
        () => {
          console.log('WebSocket connected')
          setWsConnected(true)
          
          // 订阅通知
          stompClient.subscribe('/user/topic/notifications', (message) => {
            const notification = JSON.parse(message.body)
            console.log('收到通知:', notification)
            
            // 根据通知类型刷新对应数据
            if (notification.type === 'FRIEND_REQUEST') {
              fetchApplications()
            } else if (notification.type === 'GAME_INVITATION') {
              // 显示邀请弹窗
              alert(`收到游戏邀请: ${notification.message}`)
            }
          })
        },
        (error) => {
          console.error('WebSocket connection error:', error)
          setWsConnected(false)
        }
      )
      
      window.ws = stompClient
    } catch (error) {
      console.error('WebSocket setup error:', error)
    }
  }
  
  // 获取好友列表
  const fetchFriends = async (page = 1) => {
    try {
      setLoading(true)
      const response = await fetch(`${API_BASE}/friend/list?page=${page}&size=20`)
      const result = await response.json()
      
      if (result.code === 200) {
        setFriends(result.data.content || [])
        setFriendTotal(result.data.totalElements || 0)
        setFriendPage(page)
        
        // 批量获取在线状态
        const friendUids = (result.data.content || []).map(f => f.friendUid)
        if (friendUids.length > 0) {
          fetchOnlineStatuses(friendUids)
        }
      }
    } catch (error) {
      console.error('获取好友列表失败:', error)
    } finally {
      setLoading(false)
    }
  }
  
  // 批量获取在线状态
  const fetchOnlineStatuses = async (friendUids) => {
    try {
      const statuses = {}
      for (const uid of friendUids) {
        const response = await fetch(`${API_BASE}/friend/status/${uid}`)
        const result = await response.json()
        if (result.code === 200) {
          statuses[uid] = result.data.status || 0
        }
      }
      setOnlineStatuses(statuses)
    } catch (error) {
      console.error('获取在线状态失败:', error)
    }
  }
  
  // 获取好友申请
  const fetchApplications = async (page = 1) => {
    try {
      const response = await fetch(`${API_BASE}/friend/applies/received?page=${page}&size=20`)
      const result = await response.json()
      
      if (result.code === 200) {
        setApplications(result.data.content || [])
        setAppTotal(result.data.totalElements || 0)
        setAppPage(page)
      }
    } catch (error) {
      console.error('获取好友申请失败:', error)
    }
  }
  
  // 获取黑名单
  const fetchBlacklist = async () => {
    try {
      const response = await fetch(`${API_BASE}/friend/blacklist`)
      const result = await response.json()
      
      if (result.code === 200) {
        setBlacklist(result.data || [])
      }
    } catch (error) {
      console.error('获取黑名单失败:', error)
    }
  }
  
  // 获取动态
  const fetchActivities = async (page = 1) => {
    try {
      const response = await fetch(`${API_BASE}/friend/activities?page=${page}&size=20`)
      const result = await response.json()
      
      if (result.code === 200) {
        setActivities(result.data.content || [])
        setActivityTotal(result.data.totalElements || 0)
        setActivityPage(page)
      }
    } catch (error) {
      console.error('获取动态失败:', error)
    }
  }
  
  // 搜索用户
  const handleSearch = async () => {
    if (!searchKeyword.trim()) {
      setShowSearchResults(false)
      return
    }
    
    try {
      // 尝试判断输入是否为数字ID
      const isNumeric = /^\d+$/.test(searchKeyword.trim())
      
      let url
      if (isNumeric) {
        // 按ID搜索
        url = `/api/auth/search?userId=${encodeURIComponent(searchKeyword.trim())}&page=1&size=10`
      } else {
        // 按关键词搜索（用户名或昵称）
        url = `/api/auth/search?keyword=${encodeURIComponent(searchKeyword)}&page=1&size=10`
      }
      
      console.log('搜索URL:', url)
      const response = await fetch(url)
      const result = await response.json()
      
      console.log('搜索结果:', result)
      
      if (result.code === 200) {
        setSearchResults(result.data.list || [])
        setShowSearchResults(true)
      } else {
        console.error('搜索失败:', result.message)
        setSearchResults([])
        setShowSearchResults(true)
      }
    } catch (error) {
      console.error('搜索用户失败:', error)
      setSearchResults([])
      setShowSearchResults(true)
    }
  }
  
  // 发送好友申请
  const sendFriendRequest = async (targetUid, message) => {
    try {
      const response = await fetch(`${API_BASE}/friend/apply`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          receiveUid: targetUid,
          message: message || '我想加你为好友'
        })
      })
      const result = await response.json()
      
      if (result.code === 200) {
        alert('好友申请已发送')
        setShowSearchResults(false)
        setSearchKeyword('')
      } else {
        alert(result.message || '发送失败')
      }
    } catch (error) {
      console.error('发送好友申请失败:', error)
      alert('发送失败')
    }
  }
  
  // 同意好友申请
  const acceptApplication = async (applyId) => {
    try {
      const response = await fetch(`${API_BASE}/friend/apply/${applyId}/accept`, {
        method: 'POST'
      })
      const result = await response.json()
      
      if (result.code === 200) {
        alert('已同意好友申请')
        fetchApplications()
        fetchFriends()
      } else {
        alert(result.message || '操作失败')
      }
    } catch (error) {
      console.error('同意好友申请失败:', error)
      alert('操作失败')
    }
  }
  
  // 拒绝好友申请
  const rejectApplication = async (applyId) => {
    try {
      const response = await fetch(`${API_BASE}/friend/apply/${applyId}/reject`, {
        method: 'POST'
      })
      const result = await response.json()
      
      if (result.code === 200) {
        alert('已拒绝好友申请')
        fetchApplications()
      } else {
        alert(result.message || '操作失败')
      }
    } catch (error) {
      console.error('拒绝好友申请失败:', error)
      alert('操作失败')
    }
  }
  
  // 删除好友
  const removeFriend = async (friendUid) => {
    if (!confirm('确定要删除该好友吗？')) return
    
    try {
      const response = await fetch(`${API_BASE}/friend/${friendUid}`, {
        method: 'DELETE'
      })
      const result = await response.json()
      
      if (result.code === 200) {
        alert('已删除好友')
        fetchFriends()
      } else {
        alert(result.message || '操作失败')
      }
    } catch (error) {
      console.error('删除好友失败:', error)
      alert('操作失败')
    }
  }
  
  // 拉黑用户
  const blockUser = async (blockedUid) => {
    if (!confirm('确定要拉黑该用户吗？拉黑后将自动解除好友关系。')) return
    
    try {
      const response = await fetch(`${API_BASE}/friend/blacklist?blockedUid=${blockedUid}`, {
        method: 'POST'
      })
      const result = await response.json()
      
      if (result.code === 200) {
        alert('已拉黑用户')
        fetchBlacklist()
        fetchFriends()
      } else {
        alert(result.message || '操作失败')
      }
    } catch (error) {
      console.error('拉黑用户失败:', error)
      alert('操作失败')
    }
  }
  
  // 取消拉黑
  const unblockUser = async (blockedUid) => {
    try {
      const response = await fetch(`${API_BASE}/friend/blacklist/${blockedUid}`, {
        method: 'DELETE'
      })
      const result = await response.json()
      
      if (result.code === 200) {
        alert('已取消拉黑')
        fetchBlacklist()
      } else {
        alert(result.message || '操作失败')
      }
    } catch (error) {
      console.error('取消拉黑失败:', error)
      alert('操作失败')
    }
  }
  
  // 获取在线状态文本和颜色
  const getStatusInfo = (status) => {
    switch (status) {
      case 0: return { text: '离线', color: '#999' }
      case 1: return { text: '在线', color: '#52c41a' }
      case 2: return { text: '离开', color: '#faad14' }
      case 3: return { text: '游戏中', color: '#1890ff' }
      default: return { text: '未知', color: '#999' }
    }
  }
  
  return (
    <div className="new-page-container">
      <div className="page-header">
        <button className="back-btn" onClick={() => navigate('/')}>← 返回商城</button>
        <h1>👥 好友系统</h1>
        {wsConnected && <span className="ws-status">● 实时连接</span>}
      </div>
      
      {/* 搜索框 */}
      <div className="friend-search-box">
        <input
          type="text"
          placeholder="搜索用户（ID或昵称）..."
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
        />
        <button onClick={handleSearch}>搜索</button>
        
        {showSearchResults && searchResults.length > 0 && (
          <div className="search-results-dropdown">
            {searchResults.map(user => (
              <div key={user.id} className="search-result-item">
                <div className="user-info">
                  <img src={user.avatarUrl || '/default-avatar.png'} alt="" className="avatar" />
                  <div>
                    <div className="username">{user.nickname || user.username}</div>
                    <div className="user-id">ID: {user.id}</div>
                  </div>
                </div>
                <button 
                  className="add-friend-btn"
                  onClick={() => sendFriendRequest(user.id)}
                >
                  + 添加好友
                </button>
              </div>
            ))}
          </div>
        )}
        
        {showSearchResults && searchResults.length === 0 && (
          <div className="search-results-dropdown">
            <div className="empty-state" style={{ padding: '20px', textAlign: 'center' }}>
              未找到用户
            </div>
          </div>
        )}
      </div>
      
      {/* 标签页 */}
      <div className="friend-tabs">
        <button 
          className={`tab-btn ${activeTab === 'friends' ? 'active' : ''}`}
          onClick={() => setActiveTab('friends')}
        >
          好友列表 ({friendTotal})
        </button>
        <button 
          className={`tab-btn ${activeTab === 'applications' ? 'active' : ''}`}
          onClick={() => setActiveTab('applications')}
        >
          好友申请 ({applications.length})
        </button>
        <button 
          className={`tab-btn ${activeTab === 'activities' ? 'active' : ''}`}
          onClick={() => setActiveTab('activities')}
        >
          好友动态
        </button>
        <button 
          className={`tab-btn ${activeTab === 'blacklist' ? 'active' : ''}`}
          onClick={() => setActiveTab('blacklist')}
        >
          黑名单 ({blacklist.length})
        </button>
      </div>
      
      {/* 内容区域 */}
      <div className="friend-content">
        {loading && <div className="loading">加载中...</div>}
        
        {/* 好友列表 */}
        {activeTab === 'friends' && !loading && (
          <div className="friends-list">
            {friends.length === 0 ? (
              <div className="empty-state">暂无好友，快去添加吧！</div>
            ) : (
              friends.map(friend => {
                const status = onlineStatuses[friend.friendUid] || 0
                const statusInfo = getStatusInfo(status)
                
                return (
                  <div key={friend.friendUid} className="friend-item">
                    <div className="friend-info">
                      <div className="status-indicator" style={{ backgroundColor: statusInfo.color }}></div>
                      <img src={friend.avatar || '/default-avatar.png'} alt="" className="avatar" />
                      <div>
                        <div className="friend-name">{friend.nickname || friend.username}</div>
                        <div className="friend-status" style={{ color: statusInfo.color }}>
                          {statusInfo.text}
                        </div>
                      </div>
                    </div>
                    <div className="friend-actions">
                      <button onClick={() => navigate(`/profile/${friend.friendUid}`)}>查看资料</button>
                      <button onClick={() => blockUser(friend.friendUid)}>拉黑</button>
                      <button className="danger" onClick={() => removeFriend(friend.friendUid)}>删除</button>
                    </div>
                  </div>
                )
              })
            )}
            
            {friendTotal > 20 && (
              <div className="pagination">
                <button disabled={friendPage === 1} onClick={() => fetchFriends(friendPage - 1)}>上一页</button>
                <span>{friendPage} / {Math.ceil(friendTotal / 20)}</span>
                <button disabled={friendPage >= Math.ceil(friendTotal / 20)} onClick={() => fetchFriends(friendPage + 1)}>下一页</button>
              </div>
            )}
          </div>
        )}
        
        {/* 好友申请 */}
        {activeTab === 'applications' && (
          <div className="applications-list">
            {applications.length === 0 ? (
              <div className="empty-state">暂无好友申请</div>
            ) : (
              applications.map(app => (
                <div key={app.id} className="application-item">
                  <div className="app-info">
                    <img src={app.avatar || '/default-avatar.png'} alt="" className="avatar" />
                    <div>
                      <div className="applicant-name">{app.username}</div>
                      <div className="app-message">{app.message || '想加你为好友'}</div>
                      <div className="app-time">{new Date(app.createTime).toLocaleString()}</div>
                    </div>
                  </div>
                  <div className="app-actions">
                    <button className="success" onClick={() => acceptApplication(app.id)}>同意</button>
                    <button className="danger" onClick={() => rejectApplication(app.id)}>拒绝</button>
                  </div>
                </div>
              ))
            )}
          </div>
        )}
        
        {/* 好友动态 */}
        {activeTab === 'activities' && (
          <div className="activities-list">
            {activities.length === 0 ? (
              <div className="empty-state">暂无动态</div>
            ) : (
              activities.map(activity => (
                <div key={activity.id} className="activity-item">
                  <div className="activity-header">
                    <img src={activity.avatar || '/default-avatar.png'} alt="" className="avatar" />
                    <div>
                      <div className="activity-user">{activity.username}</div>
                      <div className="activity-time">{new Date(activity.createdAt).toLocaleString()}</div>
                    </div>
                  </div>
                  <div className="activity-content">
                    {activity.type === 'FRIEND_ADDED' && <div>成为了好友</div>}
                    {activity.content && <div>{activity.content}</div>}
                    {activity.gameTitle && <div className="activity-game">🎮 {activity.gameTitle}</div>}
                  </div>
                </div>
              ))
            )}
          </div>
        )}
        
        {/* 黑名单 */}
        {activeTab === 'blacklist' && (
          <div className="blacklist">
            {blacklist.length === 0 ? (
              <div className="empty-state">黑名单为空</div>
            ) : (
              blacklist.map(item => (
                <div key={item.blockedUid} className="blacklist-item">
                  <div className="blocked-user">
                    <span>用户 {item.blockedUid}</span>
                    <span className="block-time">{new Date(item.createTime).toLocaleDateString()}</span>
                  </div>
                  <button onClick={() => unblockUser(item.blockedUid)}>取消拉黑</button>
                </div>
              ))
            )}
          </div>
        )}
      </div>
    </div>
  )
}

export default FriendSystem
