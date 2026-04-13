import { useState, useEffect } from 'react'

// 通知类型
export const NOTIFICATION_TYPES = {
  SUCCESS: 'success',
  ERROR: 'error',
  WARNING: 'warning',
  INFO: 'info'
}

// 通知图标映射
const notificationIcons = {
  success: '✓',
  error: '✕',
  warning: '⚠',
  info: 'ℹ'
}

// 通知颜色映射
const notificationColors = {
  success: {
    bg: 'linear-gradient(135deg, rgba(45, 106, 79, 0.95), rgba(64, 145, 108, 0.95))',
    border: '#40916c',
    glow: 'rgba(64, 145, 108, 0.5)'
  },
  error: {
    bg: 'linear-gradient(135deg, rgba(220, 38, 38, 0.95), rgba(239, 68, 68, 0.95))',
    border: '#ef4444',
    glow: 'rgba(239, 68, 68, 0.5)'
  },
  warning: {
    bg: 'linear-gradient(135deg, rgba(217, 119, 6, 0.95), rgba(245, 158, 11, 0.95))',
    border: '#f59e0b',
    glow: 'rgba(245, 158, 11, 0.5)'
  },
  info: {
    bg: 'linear-gradient(135deg, rgba(37, 99, 235, 0.95), rgba(59, 130, 246, 0.95))',
    border: '#3b82f6',
    glow: 'rgba(59, 130, 246, 0.5)'
  }
}

function Notification({ notification, onClose }) {
  const [visible, setVisible] = useState(false)
  const colors = notificationColors[notification.type]

  useEffect(() => {
    // 入场动画
    setTimeout(() => setVisible(true), 10)
    
    // 自动关闭
    const timer = setTimeout(() => {
      setVisible(false)
      setTimeout(() => onClose(notification.id), 300)
    }, notification.duration || 3000)

    return () => clearTimeout(timer)
  }, [notification, onClose])

  return (
    <div 
      className={`notification ${notification.type} ${visible ? 'show' : ''}`}
      style={{
        background: colors.bg,
        borderColor: colors.border,
        boxShadow: `0 8px 32px ${colors.glow}, inset 0 0 20px rgba(255, 255, 255, 0.1)`
      }}
    >
      <div className="notification-icon">
        {notificationIcons[notification.type]}
      </div>
      <div className="notification-content">
        <div className="notification-title">{notification.title}</div>
        {notification.message && (
          <div className="notification-message">{notification.message}</div>
        )}
      </div>
      <button className="notification-close" onClick={() => {
        setVisible(false)
        setTimeout(() => onClose(notification.id), 300)
      }}>
        ×
      </button>
      <div className="notification-progress">
        <div 
          className="progress-bar" 
          style={{
            animationDuration: `${notification.duration || 3000}ms`
          }}
        />
      </div>
    </div>
  )
}

export function NotificationContainer({ notifications, removeNotification }) {
  if (notifications.length === 0) return null

  return (
    <div className="notification-container">
      {notifications.map(notification => (
        <Notification
          key={notification.id}
          notification={notification}
          onClose={removeNotification}
        />
      ))}
    </div>
  )
}

// 通知管理器 Hook
export function useNotification() {
  const [notifications, setNotifications] = useState([])

  const addNotification = (type, title, message, duration = 3000) => {
    const id = Date.now() + Math.random()
    setNotifications(prev => [...prev, { id, type, title, message, duration }])
  }

  const removeNotification = (id) => {
    setNotifications(prev => prev.filter(n => n.id !== id))
  }

  const showSuccess = (title, message, duration) => 
    addNotification(NOTIFICATION_TYPES.SUCCESS, title, message, duration)
  
  const showError = (title, message, duration) => 
    addNotification(NOTIFICATION_TYPES.ERROR, title, message, duration)
  
  const showWarning = (title, message, duration) => 
    addNotification(NOTIFICATION_TYPES.WARNING, title, message, duration)
  
  const showInfo = (title, message, duration) => 
    addNotification(NOTIFICATION_TYPES.INFO, title, message, duration)

  return {
    notifications,
    removeNotification,
    showSuccess,
    showError,
    showWarning,
    showInfo
  }
}
