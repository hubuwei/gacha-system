import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { AUTH_API_BASE, MALL_API_BASE } from '../App'

function TransactionHistory({ currentUser }) {
  const navigate = useNavigate()
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('all') // all, recharge, purchase, refund

  useEffect(() => {
    if (currentUser) {
      fetchTransactions()
    }
  }, [currentUser, filter])

  const fetchTransactions = async () => {
    try {
      setLoading(true)
      const token = localStorage.getItem('token')
      const response = await fetch(
        `${MALL_API_BASE}/wallet/transactions?userId=${currentUser.id}&type=${filter}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      )
      const result = await response.json()
      
      if (result.code === 200 || result.success) {
        setTransactions(result.data || [])
      } else {
        setTransactions([])
      }
    } catch (error) {
      console.error('获取交易记录失败:', error)
      setTransactions([])
    } finally {
      setLoading(false)
    }
  }

  const getTypeText = (type) => {
    const typeMap = {
      'recharge': '充值',
      'purchase': '消费',
      'refund': '退款',
      'withdraw': '提现'
    }
    return typeMap[type] || type
  }

  const getTypeIcon = (type) => {
    const iconMap = {
      'recharge': '💰',
      'purchase': '🛒',
      'refund': '↩️',
      'withdraw': '💸'
    }
    return iconMap[type] || '💵'
  }

  const formatDate = (dateStr) => {
    return new Date(dateStr).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  // 未登录提示
  if (!currentUser) {
    return (
      <div className="transaction-history-page">
        <div className="page-header">
          <h1>📊 账单明细</h1>
          <button className="back-btn" onClick={() => navigate(-1)}>
            ← 返回
          </button>
        </div>
        <div className="empty-transactions">
          <div className="empty-icon">🔒</div>
          <h2>请先登录</h2>
          <p>登录后即可查看您的账单明细</p>
          <button className="browse-btn" onClick={() => navigate('/')}>
            去首页登录
          </button>
        </div>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="transaction-history-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>加载中...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="transaction-history-page">
      <div className="page-header">
        <h1>📊 账单明细</h1>
        <button className="back-btn" onClick={() => navigate(-1)}>
          ← 返回
        </button>
      </div>

      {/* 筛选标签 */}
      <div className="transaction-filters">
        <button 
          className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
          onClick={() => setFilter('all')}
        >
          全部
        </button>
        <button 
          className={`filter-btn ${filter === 'recharge' ? 'active' : ''}`}
          onClick={() => setFilter('recharge')}
        >
          充值
        </button>
        <button 
          className={`filter-btn ${filter === 'purchase' ? 'active' : ''}`}
          onClick={() => setFilter('purchase')}
        >
          消费
        </button>
        <button 
          className={`filter-btn ${filter === 'refund' ? 'active' : ''}`}
          onClick={() => setFilter('refund')}
        >
          退款
        </button>
      </div>

      {/* 交易列表 */}
      {transactions.length === 0 ? (
        <div className="empty-transactions">
          <div className="empty-icon">📊</div>
          <h2>暂无交易记录</h2>
          <p>充值或消费后会显示在这里</p>
          <button className="browse-btn" onClick={() => navigate('/wallet/recharge')}>
            去充值
          </button>
        </div>
      ) : (
        <div className="transactions-list">
          {transactions.map((transaction) => (
            <div key={transaction.id} className="transaction-item">
              <div className="transaction-icon">
                {getTypeIcon(transaction.transactionType)}
              </div>
              
              <div className="transaction-info">
                <h4 className="transaction-title">
                  {getTypeText(transaction.transactionType)}
                </h4>
                <p className="transaction-desc">{transaction.description || '-'}</p>
                {transaction.relatedOrderNo && (
                  <span className="transaction-order-no">
                    单号: {transaction.relatedOrderNo}
                  </span>
                )}
                <span className="transaction-time">
                  {formatDate(transaction.createdAt)}
                </span>
              </div>
              
              <div className="transaction-amount-section">
                <div className={`transaction-amount ${
                  transaction.transactionType === 'recharge' || 
                  transaction.transactionType === 'refund' 
                    ? 'income' 
                    : 'expense'
                }`}>
                  {transaction.transactionType === 'recharge' || 
                   transaction.transactionType === 'refund' ? '+' : '-'}
                  ¥{Math.abs(transaction.amount).toFixed(2)}
                </div>
                <div className="transaction-balance">
                  余额: ¥{transaction.balanceAfter.toFixed(2)}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default TransactionHistory
