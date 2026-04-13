import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { AUTH_API_BASE, MALL_API_BASE } from '../App'

function WalletRecharge({ currentUser, userBalance, onRechargeSuccess }) {
  const navigate = useNavigate()
  const [amount, setAmount] = useState('')
  const [paymentMethod, setPaymentMethod] = useState('alipay')
  const [processing, setProcessing] = useState(false)

  // 预设充值金额
  const presetAmounts = [10, 30, 50, 100, 200, 500]

  const handleRecharge = async () => {
    const rechargeAmount = parseFloat(amount)
    
    if (!rechargeAmount || rechargeAmount <= 0) {
      alert('请输入有效的充值金额')
      return
    }

    if (rechargeAmount < 1 || rechargeAmount > 10000) {
      alert('充值金额范围为 1-10000 元')
      return
    }

    try {
      setProcessing(true)
      
      // 获取 Token
      const token = localStorage.getItem('token')
      
      // 所有支付方式都调用 mall-service 的钱包充值接口(模拟支付)
      const response = await fetch(`${MALL_API_BASE}/wallet/recharge`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          userId: currentUser.id,
          amount: rechargeAmount,
          paymentMethod: paymentMethod
        })
      })

      const result = await response.json()

      if (result.code === 200) {
        // 根据支付方式显示不同的提示
        let successMessage = ''
        if (paymentMethod === 'balance') {
          successMessage = `已充值 ￥${rechargeAmount.toFixed(2)}，余额已更新`
        } else if (paymentMethod === 'alipay') {
          successMessage = `演示模式 - 已模拟支付宝充值 ￥${rechargeAmount.toFixed(2)}`
        } else if (paymentMethod === 'wechat') {
          successMessage = `演示模式 - 已模拟微信充值 ￥${rechargeAmount.toFixed(2)}`
        }
        
        // 先显示成功通知
        window.dispatchEvent(new CustomEvent('showSuccess', {
          detail: { title: '✅ 充值成功', message: successMessage, duration: 5000 }
        }))
        
        // 刷新余额
        if (onRechargeSuccess) {
          await onRechargeSuccess(rechargeAmount)
        }
        
        // 延迟跳转，让用户看到提示
        setTimeout(() => {
          navigate('/profile')
        }, 1500)
      } else {
        window.dispatchEvent(new CustomEvent('showError', {
          detail: { title: '充值失败', message: result.message, duration: 3000 }
        }))
      }
    } catch (error) {
      console.error('充值错误:', error)
      window.dispatchEvent(new CustomEvent('showError', {
        detail: { title: '充值失败', message: error.message, duration: 3000 }
      }))
    } finally {
      setProcessing(false)
    }
  }

  // 未登录提示
  if (!currentUser) {
    return (
      <div className="wallet-recharge-page">
        <div className="page-header">
          <h1>💳 充值中心</h1>
          <button className="back-btn" onClick={() => navigate(-1)}>
            ← 返回
          </button>
        </div>
        <div className="empty-transactions">
          <div className="empty-icon">🔒</div>
          <h2>请先登录</h2>
          <p>登录后即可进行充值</p>
          <button className="browse-btn" onClick={() => navigate('/')}>
            去首页登录
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="wallet-recharge-page">
      <div className="page-header">
        <h1>💳 充值中心</h1>
        <button className="back-btn" onClick={() => navigate(-1)}>
          ← 返回
        </button>
      </div>

      <div className="recharge-container">
        {/* 当前余额 */}
        <div className="balance-card">
          <div className="balance-label">当前余额</div>
          <div className="balance-amount">¥{userBalance.toFixed(2)}</div>
          <div className="balance-tips">💡 充值后可用于购买游戏</div>
        </div>

        {/* 左侧：充值金额选择和支付方式 */}
        <div className="recharge-left">
          {/* 充值金额选择 */}
          <div className="recharge-section">
            <h2>选择充值金额</h2>
            
            <div className="recharge-amounts">
              {presetAmounts.map((preset, index) => (
                <div
                  key={preset}
                  className={`amount-option ${parseFloat(amount) === preset ? 'selected' : ''} ${index === 2 ? 'popular' : ''}`}
                  onClick={() => setAmount(preset.toString())}
                >
                  <div className="amount-value">¥{preset}</div>
                  {index === 2 && <div className="amount-bonus">送 ¥5</div>}
                  {index === 4 && <div className="amount-bonus">送 ¥20</div>}
                  {index === 5 && <div className="amount-bonus">送 ¥100</div>}
                </div>
              ))}
            </div>

            <div className="custom-amount-section">
              <input
                type="number"
                className="custom-amount-input"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="输入自定义金额 (1-10000)"
                min="1"
                max="10000"
                step="0.01"
              />
            </div>
          </div>

          {/* 支付方式 */}
          <div className="recharge-section">
            <h3 className="payment-methods-title">选择支付方式</h3>
            
            <div className="payment-options">
              <div 
                className={`payment-option ${paymentMethod === 'alipay' ? 'selected' : ''}`}
                onClick={() => setPaymentMethod('alipay')}
              >
                <span className="payment-icon">💙</span>
                <div className="payment-info">
                  <div className="payment-name">支付宝</div>
                  <div className="payment-desc">推荐</div>
                </div>
              </div>

              <div 
                className={`payment-option ${paymentMethod === 'wechat' ? 'selected' : ''}`}
                onClick={() => setPaymentMethod('wechat')}
              >
                <span className="payment-icon">💚</span>
                <div className="payment-info">
                  <div className="payment-name">微信支付</div>
                  <div className="payment-desc">便捷</div>
                </div>
              </div>

              <div 
                className={`payment-option ${paymentMethod === 'balance' ? 'selected' : ''}`}
                onClick={() => setPaymentMethod('balance')}
              >
                <span className="payment-icon">⚡</span>
                <div className="payment-info">
                  <div className="payment-name">测试模式</div>
                  <div className="payment-desc">直接到账</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* 右侧：充值汇总和提交按钮 */}
        <div className="recharge-right">
          {/* 充值汇总 */}
          {amount && parseFloat(amount) > 0 ? (
            <div className="recharge-summary-card">
              <h2>充值汇总</h2>
              <div className="summary-row">
                <span className="summary-label">充值金额</span>
                <span className="summary-value">¥{parseFloat(amount).toFixed(2)}</span>
              </div>
              <div className="summary-row">
                <span className="summary-label">支付方式</span>
                <span className="summary-value">
                  {paymentMethod === 'alipay' && '支付宝'}
                  {paymentMethod === 'wechat' && '微信支付'}
                  {paymentMethod === 'balance' && '测试模式'}
                </span>
              </div>
              <div className="summary-divider"></div>
              <div className="summary-row summary-total">
                <span className="summary-label">实付金额</span>
                <span className="summary-value total-amount">¥{parseFloat(amount).toFixed(2)}</span>
              </div>

              {/* 提交按钮 */}
              <button 
                className="submit-recharge-btn"
                onClick={handleRecharge}
                disabled={processing}
              >
                {processing ? '处理中...' : '立即充值'}
              </button>
            </div>
          ) : (
            <div className="recharge-summary-card empty-summary">
              <div className="empty-icon">💳</div>
              <p>请选择充值金额</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default WalletRecharge
