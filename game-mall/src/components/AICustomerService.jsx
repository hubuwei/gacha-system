import { useState, useRef, useEffect } from "react"

const MALL_API_BASE = "/api"

const QUICK_REPLIES = [
  "有什么热门游戏推荐？",
  "怎么购买游戏？",
  "如何退款？",
  "充值多久到账？",
  "游戏支持什么平台？",
]

function AICustomerService() {
  const [isOpen, setIsOpen] = useState(false)
  const [messages, setMessages] = useState([
    { role: "assistant", content: "你好！我是游戏商城 AI 客服 🎮 有什么可以帮你的吗？" },
  ])
  const [input, setInput] = useState("")
  const [loading, setLoading] = useState(false)
  const [unread, setUnread] = useState(0)
  const messagesEndRef = useRef(null)
  const inputRef = useRef(null)

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
  }, [messages])

  useEffect(() => {
    if (isOpen) {
      setUnread(0)
      inputRef.current?.focus()
    }
  }, [isOpen])

  useEffect(() => {
    if (!isOpen && messages.length > 1) {
      setUnread(1)
    }
  }, [messages.length])

  const buildHistory = () => {
    return messages.slice(0, -1).map(m => ({ role: m.role, content: m.content }))
  }

  const handleSend = async () => {
    const text = input.trim()
    if (!text || loading) return

    const userMsg = { role: "user", content: text }
    const newMessages = [...messages, userMsg]
    setMessages(newMessages)
    setInput("")
    setLoading(true)

    try {
      const history = messages.map(m => ({ role: m.role, content: m.content }))
      const response = await fetch(MALL_API_BASE + "/ai/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: text, history }),
      })

      if (!response.ok) throw new Error("请求失败")
      const data = await response.json()
      if (data.code === 200 && data.data) {
        setMessages([...newMessages, { role: "assistant", content: data.data.reply }])
      } else {
        throw new Error(data.message || "请求失败")
      }
    } catch (err) {
      setMessages([...newMessages, { role: "assistant", content: "抱歉，我暂时无法回复 😢 请稍后再试。" }])
    } finally {
      setLoading(false)
    }
  }

  const handleQuickReply = (text) => {
    setInput(text)
    setTimeout(() => handleSendWithText(text), 100)
  }

  const handleSendWithText = async (text) => {
    if (loading) return
    const userMsg = { role: "user", content: text }
    const newMessages = [...messages, userMsg]
    setMessages(newMessages)
    setInput("")
    setLoading(true)
    try {
      const history = messages.map(m => ({ role: m.role, content: m.content }))
      const response = await fetch(MALL_API_BASE + "/ai/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: text, history }),
      })
      if (!response.ok) throw new Error("请求失败")
      const data = await response.json()
      if (data.code === 200 && data.data) {
        setMessages([...newMessages, { role: "assistant", content: data.data.reply }])
      } else {
        throw new Error(data.message || "请求失败")
      }
    } catch (err) {
      setMessages([...newMessages, { role: "assistant", content: "抱歉，我暂时无法回复 😢 请稍后再试。" }])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="ai-cs-wrapper">
      {/* Chat popup */}
      {isOpen && (
        <div className="ai-cs-panel">
          <div className="ai-cs-header">
            <div className="ai-cs-header-left">
              <span className="ai-cs-avatar">🤖</span>
              <div>
                <div className="ai-cs-name">AI 游戏客服</div>
                <div className="ai-cs-status">在线 · 秒回</div>
              </div>
            </div>
            <button className="ai-cs-close" onClick={() => setIsOpen(false)}>✕</button>
          </div>

          <div className="ai-cs-messages">
            {messages.map((msg, i) => (
              <div key={i} className={`ai-cs-msg ${msg.role === "user" ? "user" : "bot"}`}>
                {msg.role === "assistant" && <span className="ai-cs-msg-avatar">🤖</span>}
                <div className="ai-cs-bubble">{msg.content}</div>
              </div>
            ))}
            {loading && (
              <div className="ai-cs-msg bot">
                <span className="ai-cs-msg-avatar">🤖</span>
                <div className="ai-cs-bubble typing">
                  <span className="dot" /><span className="dot" /><span className="dot" />
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Quick replies */}
          {messages.length <= 1 && (
            <div className="ai-cs-quick-replies">
              {QUICK_REPLIES.map((q, i) => (
                <button key={i} className="ai-cs-quick-btn" onClick={() => handleQuickReply(q)}>
                  {q}
                </button>
              ))}
            </div>
          )}

          <div className="ai-cs-input-area">
            <input
              ref={inputRef}
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && handleSend()}
              placeholder="输入你的问题..."
              disabled={loading}
            />
            <button className="ai-cs-send-btn" onClick={handleSend} disabled={loading || !input.trim()}>
              发送
            </button>
          </div>
        </div>
      )}

      {/* Floating button */}
      <button className={`ai-cs-fab ${isOpen ? "active" : ""}`} onClick={() => setIsOpen(!isOpen)}>
        {isOpen ? "✕" : "🤖"}
        {unread > 0 && <span className="ai-cs-badge">{unread}</span>}
      </button>
    </div>
  )
}

export default AICustomerService