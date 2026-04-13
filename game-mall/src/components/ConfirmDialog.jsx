import { useState, useEffect } from 'react'

function ConfirmDialog({ isOpen, title, message, onConfirm, onCancel, confirmText = '确认', cancelText = '取消', type = 'warning' }) {
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    if (isOpen) {
      setTimeout(() => setVisible(true), 10)
    } else {
      setVisible(false)
    }
  }, [isOpen])

  if (!isOpen) return null

  const handleConfirm = () => {
    setVisible(false)
    setTimeout(() => onConfirm(), 300)
  }

  const handleCancel = () => {
    setVisible(false)
    setTimeout(() => onCancel(), 300)
  }

  const getTypeStyles = () => {
    switch (type) {
      case 'success':
        return {
          icon: '✓',
          color: '#40916c',
          gradient: 'linear-gradient(135deg, rgba(45, 106, 79, 0.3), rgba(64, 145, 108, 0.2))'
        }
      case 'error':
        return {
          icon: '✕',
          color: '#ef4444',
          gradient: 'linear-gradient(135deg, rgba(220, 38, 38, 0.3), rgba(239, 68, 68, 0.2))'
        }
      case 'warning':
        return {
          icon: '⚠',
          color: '#f59e0b',
          gradient: 'linear-gradient(135deg, rgba(217, 119, 6, 0.3), rgba(245, 158, 11, 0.2))'
        }
      default:
        return {
          icon: 'ℹ',
          color: '#3b82f6',
          gradient: 'linear-gradient(135deg, rgba(37, 99, 235, 0.3), rgba(59, 130, 246, 0.2))'
        }
    }
  }

  const styles = getTypeStyles()

  return (
    <div className={`confirm-dialog-overlay ${visible ? 'show' : ''}`} onClick={handleCancel}>
      <div className="confirm-dialog" onClick={(e) => e.stopPropagation()}>
        <div 
          className="confirm-dialog-icon"
          style={{
            background: styles.gradient,
            borderColor: styles.color,
            boxShadow: `0 0 30px ${styles.color}40`
          }}
        >
          {styles.icon}
        </div>
        
        <h3 className="confirm-dialog-title">{title}</h3>
        <p className="confirm-dialog-message">{message}</p>
        
        <div className="confirm-dialog-actions">
          <button className="confirm-btn cancel" onClick={handleCancel}>
            {cancelText}
          </button>
          <button 
            className="confirm-btn confirm" 
            onClick={handleConfirm}
            style={{
              background: `linear-gradient(135deg, ${styles.color}, ${styles.color}dd)`,
              boxShadow: `0 4px 15px ${styles.color}60`
            }}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  )
}

export default ConfirmDialog
