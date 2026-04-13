import { useState, useRef, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { AUTH_API_BASE } from '../App'

function UserProfile({ currentUser, setCurrentUser }) {
  const navigate = useNavigate()
  const [editing, setEditing] = useState(false)
  const [formData, setFormData] = useState({
    username: '',
    nickname: '',
    email: '',
    phone: '',
    signature: ''
  })
  const [saving, setSaving] = useState(false)
  
  // 头像上传相关状态
  const fileInputRef = useRef(null)
  const [uploading, setUploading] = useState(false)
  const [previewUrl, setPreviewUrl] = useState(null)
  
  // 验证码绑定相关状态
  const [showPhoneModal, setShowPhoneModal] = useState(false)
  const [showEmailModal, setShowEmailModal] = useState(false)
  const [phoneForm, setPhoneForm] = useState({ phone: '', code: '' })
  const [emailForm, setEmailForm] = useState({ email: '', code: '' })
  const [phoneCountdown, setPhoneCountdown] = useState(0)
  const [emailCountdown, setEmailCountdown] = useState(0)
  const [sendingCode, setSendingCode] = useState(false)
  
  // 当 currentUser 变化时，同步更新 formData
  useEffect(() => {
    if (currentUser) {
      setFormData({
        username: currentUser.username || '',
        nickname: currentUser.nickname || '',
        email: currentUser.email || '',
        phone: currentUser.phone || '',
        signature: currentUser.signature || ''
      })
    }
  }, [currentUser])

  const handleSave = async () => {
    try {
      setSaving(true)
      const token = localStorage.getItem('token')
      
      const response = await fetch(`${AUTH_API_BASE}/api/auth/profile`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(formData)
      })

      const result = await response.json()
      
      if (result.code === 200 || result.success) {
        // 更新本地用户信息
        const updatedUser = { ...currentUser, ...formData }
        setCurrentUser(updatedUser)
        localStorage.setItem('user', JSON.stringify(updatedUser))
        
        setEditing(false)
        alert('保存成功')
      } else {
        alert(result.message || '保存失败')
      }
    } catch (error) {
      alert('保存失败：' + error.message)
    } finally {
      setSaving(false)
    }
  }

  const handleChangePassword = () => {
    // TODO: 实现修改密码功能
    alert('修改密码功能开发中...')
  }
  
  // 点击更换头像按钮
  const handleAvatarClick = () => {
    fileInputRef.current?.click()
  }
  
  // 处理文件选择
  const handleFileChange = async (e) => {
    const file = e.target.files?.[0]
    if (!file) return
    
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      alert('请选择图片文件')
      return
    }
    
    // 验证文件大小（限制5MB）
    if (file.size > 5 * 1024 * 1024) {
      alert('图片大小不能超过5MB')
      return
    }
    
    // 创建预览URL
    const preview = URL.createObjectURL(file)
    setPreviewUrl(preview)
    
    // 上传头像
    await uploadAvatar(file)
    
    // 清理预览URL
    setTimeout(() => URL.revokeObjectURL(preview), 1000)
  }
  
  // 上传头像到后端
  const uploadAvatar = async (file) => {
    try {
      setUploading(true)
      const token = localStorage.getItem('token')
      
      const formData = new FormData()
      formData.append('avatar', file)
      
      console.log('开始上传头像...', AUTH_API_BASE)
      
      const response = await fetch(`${AUTH_API_BASE}/api/auth/avatar`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData
      })
      
      const result = await response.json()
      console.log('上传响应:', result)
      
      if (result.code === 200 || result.success) {
        let avatarUrl = result.data?.avatarUrl || result.data?.url
        console.log('原始头像URL:', avatarUrl)
        
        // 如果是相对路径，拼接完整的API基址
        if (avatarUrl && avatarUrl.startsWith('/')) {
          avatarUrl = AUTH_API_BASE + avatarUrl
        }
        
        console.log('完整头像URL:', avatarUrl)
        
        // 更新本地用户信息
        const updatedUser = { 
          ...currentUser, 
          avatarUrl: avatarUrl
        }
        console.log('更新后的用户信息:', updatedUser)
        
        setCurrentUser(updatedUser)
        localStorage.setItem('user', JSON.stringify(updatedUser))
        
        alert('头像上传成功')
      } else {
        alert(result.message || '头像上传失败')
      }
    } catch (error) {
      console.error('上传头像失败:', error)
      alert('上传失败：' + error.message)
    } finally {
      setUploading(false)
      setPreviewUrl(null)
    }
  }
  
  // 发送手机验证码
  const sendPhoneCode = async () => {
    if (!phoneForm.phone) {
      alert('请输入手机号')
      return
    }
    
    if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
      alert('手机号格式不正确')
      return
    }
    
    if (phoneCountdown > 0) {
      alert(`请${phoneCountdown}秒后再试`)
      return
    }
    
    try {
      setSendingCode(true)
      const response = await fetch(`${AUTH_API_BASE}/api/auth/send-sms-code?phone=${phoneForm.phone}`, {
        method: 'POST'
      })
      
      const result = await response.json()
      
      if (result.code === 200 || result.success) {
        alert('验证码已发送，请查收短信')
        // 开始倒计时
        setPhoneCountdown(60)
        const timer = setInterval(() => {
          setPhoneCountdown(prev => {
            if (prev <= 1) {
              clearInterval(timer)
              return 0
            }
            return prev - 1
          })
        }, 1000)
      } else {
        alert(result.message || '发送失败')
      }
    } catch (error) {
      alert('发送失败：' + error.message)
    } finally {
      setSendingCode(false)
    }
  }
  
  // 发送邮箱验证码
  const sendEmailCode = async () => {
    if (!emailForm.email) {
      alert('请输入邮箱')
      return
    }
    
    if (!/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/.test(emailForm.email)) {
      alert('邮箱格式不正确')
      return
    }
    
    if (emailCountdown > 0) {
      alert(`请${emailCountdown}秒后再试`)
      return
    }
    
    try {
      setSendingCode(true)
      const response = await fetch(`${AUTH_API_BASE}/api/auth/send-email-code?email=${emailForm.email}`, {
        method: 'POST'
      })
      
      const result = await response.json()
      
      if (result.code === 200 || result.success) {
        alert('验证码已发送，请查收邮件')
        // 开始倒计时
        setEmailCountdown(60)
        const timer = setInterval(() => {
          setEmailCountdown(prev => {
            if (prev <= 1) {
              clearInterval(timer)
              return 0
            }
            return prev - 1
          })
        }, 1000)
      } else {
        alert(result.message || '发送失败')
      }
    } catch (error) {
      alert('发送失败：' + error.message)
    } finally {
      setSendingCode(false)
    }
  }
  
  // 绑定手机
  const bindPhone = async () => {
    if (!phoneForm.phone || !phoneForm.code) {
      alert('请填写完整信息')
      return
    }
    
    try {
      const token = localStorage.getItem('token')
      const formData = new URLSearchParams()
      formData.append('phone', phoneForm.phone)
      formData.append('code', phoneForm.code)
      
      const response = await fetch(`${AUTH_API_BASE}/api/auth/bind-phone`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: formData
      })
      
      const result = await response.json()
      
      if (result.code === 200 || result.success) {
        alert('手机绑定成功')
        setShowPhoneModal(false)
        // 更新本地用户信息
        const updatedUser = { ...currentUser, phone: phoneForm.phone }
        setCurrentUser(updatedUser)
        localStorage.setItem('user', JSON.stringify(updatedUser))
        setPhoneForm({ phone: '', code: '' })
      } else {
        alert(result.message || '绑定失败')
      }
    } catch (error) {
      alert('绑定失败：' + error.message)
    }
  }
  
  // 绑定邮箱
  const bindEmail = async () => {
    if (!emailForm.email || !emailForm.code) {
      alert('请填写完整信息')
      return
    }
    
    try {
      const token = localStorage.getItem('token')
      const formData = new URLSearchParams()
      formData.append('email', emailForm.email)
      formData.append('code', emailForm.code)
      
      const response = await fetch(`${AUTH_API_BASE}/api/auth/bind-email`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: formData
      })
      
      const result = await response.json()
      
      if (result.code === 200 || result.success) {
        alert('邮箱绑定成功')
        setShowEmailModal(false)
        // 更新本地用户信息
        const updatedUser = { ...currentUser, email: emailForm.email }
        setCurrentUser(updatedUser)
        localStorage.setItem('user', JSON.stringify(updatedUser))
        setEmailForm({ email: '', code: '' })
      } else {
        alert(result.message || '绑定失败')
      }
    } catch (error) {
      alert('绑定失败：' + error.message)
    }
  }

  return (
    <div className="user-profile-page">
      <div className="page-header">
        <h1>👤 个人中心</h1>
        <button className="back-btn" onClick={() => navigate(-1)}>
          ← 返回
        </button>
      </div>

      <div className="profile-content">
        {/* 头像区域 */}
        <div className="profile-avatar-section">
          <div className="avatar-large">
            {uploading ? (
              <div className="avatar-upload-loading">
                <div className="loading-spinner"></div>
                <span>上传中...</span>
              </div>
            ) : previewUrl ? (
              <img src={previewUrl} alt="Preview" />
            ) : currentUser?.avatarUrl ? (
              <img src={currentUser.avatarUrl} alt="Avatar" />
            ) : (
              <span>👤</span>
            )}
          </div>
          <button 
            className="upload-avatar-btn"
            onClick={handleAvatarClick}
            disabled={uploading}
          >
            {uploading ? '⏳ 上传中...' : '📷 更换头像'}
          </button>
          
          {/* 隐藏的文件输入 */}
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            onChange={handleFileChange}
            style={{ display: 'none' }}
          />
        </div>

        {/* 用户信息表单 */}
        <div className="profile-form">
          <div className="form-header">
            <h2>基本信息</h2>
            {!editing ? (
              <button className="edit-btn" onClick={() => setEditing(true)}>
                ✏️ 编辑
              </button>
            ) : (
              <div className="form-actions">
                <button className="cancel-btn" onClick={() => setEditing(false)}>
                  取消
                </button>
                <button 
                  className="save-btn" 
                  onClick={handleSave}
                  disabled={saving}
                >
                  {saving ? '保存中...' : '保存'}
                </button>
              </div>
            )}
          </div>

          <div className="form-grid">
            <div className="form-group">
              <label>用户名</label>
              <input
                type="text"
                value={formData.username}
                disabled
                className="disabled-input"
              />
              <small>用户名不可修改</small>
            </div>

            <div className="form-group">
              <label>昵称</label>
              <input
                type="text"
                value={formData.nickname}
                onChange={(e) => setFormData({...formData, nickname: e.target.value})}
                disabled={!editing}
                placeholder="设置你的昵称"
              />
            </div>

            <div className="form-group">
              <label>邮箱</label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({...formData, email: e.target.value})}
                disabled={!editing}
                placeholder="example@email.com"
              />
            </div>

            <div className="form-group">
              <label>手机号</label>
              <input
                type="tel"
                value={formData.phone}
                onChange={(e) => setFormData({...formData, phone: e.target.value})}
                disabled={!editing}
                placeholder="13800138000"
              />
            </div>

            <div className="form-group full-width">
              <label>个性签名</label>
              <textarea
                value={formData.signature}
                onChange={(e) => setFormData({...formData, signature: e.target.value})}
                disabled={!editing}
                placeholder="写点什么介绍一下自己吧..."
                rows="3"
              />
            </div>
          </div>
        </div>

        {/* 账号安全 */}
        <div className="security-section">
          <h2>🔒 账号安全</h2>
          <div className="security-items">
            <div className="security-item">
              <div className="security-info">
                <span className="security-icon">🔑</span>
                <div>
                  <h4>修改密码</h4>
                  <p>定期修改密码可以保护账号安全</p>
                </div>
              </div>
              <button className="security-btn" onClick={handleChangePassword}>
                修改
              </button>
            </div>

            <div className="security-item">
              <div className="security-info">
                <span className="security-icon">📱</span>
                <div>
                  <h4>手机绑定</h4>
                  <p>{formData.phone ? `已绑定: ${formData.phone}` : '未绑定'}</p>
                </div>
              </div>
              <button className="security-btn" onClick={() => setShowPhoneModal(true)}>
                {formData.phone ? '更换' : '绑定'}
              </button>
            </div>

            <div className="security-item">
              <div className="security-info">
                <span className="security-icon">📧</span>
                <div>
                  <h4>邮箱绑定</h4>
                  <p>{formData.email ? `已绑定: ${formData.email}` : '未绑定'}</p>
                </div>
              </div>
              <button className="security-btn" onClick={() => setShowEmailModal(true)}>
                {formData.email ? '更换' : '绑定'}
              </button>
            </div>
          </div>
        </div>

        {/* 快捷链接 */}
        <div className="quick-links-section">
          <h2>⚡ 快捷访问</h2>
          <div className="quick-links">
            <div className="link-card" onClick={() => navigate('/library')}>
              <span className="link-icon">🎮</span>
              <h3>游戏库</h3>
              <p>查看已购买的游戏</p>
            </div>
            <div className="link-card" onClick={() => navigate('/orders')}>
              <span className="link-icon">📦</span>
              <h3>我的订单</h3>
              <p>查看订单历史记录</p>
            </div>
            <div className="link-card" onClick={() => navigate('/wallet/recharge')}>
              <span className="link-icon">💰</span>
              <h3>充值中心</h3>
              <p>为账户充值余额</p>
            </div>
            <div className="link-card" onClick={() => navigate('/notifications')}>
              <span className="link-icon">🔔</span>
              <h3>通知中心</h3>
              <p>查看系统通知</p>
            </div>
            <div className="link-card" onClick={() => navigate('/wallet/transactions')}>
              <span className="link-icon">📊</span>
              <h3>账单明细</h3>
              <p>查看交易记录</p>
            </div>
          </div>
        </div>
      </div>
      
      {/* 手机绑定弹窗 */}
      {showPhoneModal && (
        <div className="modal-overlay" onClick={() => setShowPhoneModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>📱 绑定手机号</h3>
            <div className="form-group">
              <label>手机号</label>
              <input
                type="tel"
                value={phoneForm.phone}
                onChange={(e) => setPhoneForm({...phoneForm, phone: e.target.value})}
                placeholder="请输入手机号"
              />
            </div>
            <div className="form-group">
              <label>验证码</label>
              <div style={{ display: 'flex', gap: '10px' }}>
                <input
                  type="text"
                  value={phoneForm.code}
                  onChange={(e) => setPhoneForm({...phoneForm, code: e.target.value})}
                  placeholder="请输入6位验证码"
                  maxLength="6"
                  style={{ flex: 1 }}
                />
                <button 
                  className="send-code-btn"
                  onClick={sendPhoneCode}
                  disabled={sendingCode || phoneCountdown > 0}
                  style={{ minWidth: '120px' }}
                >
                  {phoneCountdown > 0 ? `${phoneCountdown}秒后重试` : '获取验证码'}
                </button>
              </div>
            </div>
            <div className="modal-actions">
              <button className="cancel-btn" onClick={() => setShowPhoneModal(false)}>取消</button>
              <button className="confirm-btn" onClick={bindPhone}>确认绑定</button>
            </div>
          </div>
        </div>
      )}
      
      {/* 邮箱绑定弹窗 */}
      {showEmailModal && (
        <div className="modal-overlay" onClick={() => setShowEmailModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>📧 绑定邮箱</h3>
            <div className="form-group">
              <label>邮箱</label>
              <input
                type="email"
                value={emailForm.email}
                onChange={(e) => setEmailForm({...emailForm, email: e.target.value})}
                placeholder="请输入邮箱地址"
              />
            </div>
            <div className="form-group">
              <label>验证码</label>
              <div style={{ display: 'flex', gap: '10px' }}>
                <input
                  type="text"
                  value={emailForm.code}
                  onChange={(e) => setEmailForm({...emailForm, code: e.target.value})}
                  placeholder="请输入6位验证码"
                  maxLength="6"
                  style={{ flex: 1 }}
                />
                <button 
                  className="send-code-btn"
                  onClick={sendEmailCode}
                  disabled={sendingCode || emailCountdown > 0}
                  style={{ minWidth: '120px' }}
                >
                  {emailCountdown > 0 ? `${emailCountdown}秒后重试` : '获取验证码'}
                </button>
              </div>
            </div>
            <div className="modal-actions">
              <button className="cancel-btn" onClick={() => setShowEmailModal(false)}>取消</button>
              <button className="confirm-btn" onClick={bindEmail}>确认绑定</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default UserProfile
