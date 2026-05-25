import { useState, useRef } from "react"

const MALL_API_BASE = "/api"

const GENRES = ["动作冒险", "角色扮演", "射击竞技", "开放世界", "策略模拟", "休闲解谜", "恐怖生存", "体育竞速"]
const STYLES = ["史诗宏大", "悬疑诡谲", "轻松治愈", "热血燃系", "暗黑写实", "赛博科幻", "武侠仙侠", "二次元"]

function AIGameIntro() {
  const [gameName, setGameName] = useState("")
  const [genre, setGenre] = useState("")
  const [style, setStyle] = useState("")
  const [generating, setGenerating] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [copied, setCopied] = useState(false)
  const resultRef = useRef(null)

  const handleGenerate = async () => {
    if (!gameName.trim()) return
    setGenerating(true)
    setError(null)
    setResult(null)
    try {
      const response = await fetch(MALL_API_BASE + "/ai/generate-game-intro", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          gameName: gameName.trim(),
          genre: genre || undefined,
          style: style || undefined,
        }),
      })
      if (!response.ok) throw new Error("请求失败 (" + response.status + ")")
      const data = await response.json()
      if (data.code === 200 && data.data) {
        setResult(data.data)
      } else {
        throw new Error(data.message || "生成失败，请重试")
      }
    } catch (err) {
      setError(err.message || "网络错误，请检查后端服务是否启动")
    } finally {
      setGenerating(false)
    }
  }

  const handleCopy = () => {
    if (!result) return
    const text = [
      "【" + (result.title || gameName) + "】",
      "",
      "📖 简介",
      result.summary || "",
      "",
      "🌍 背景故事",
      result.background || "",
      "",
      "🎮 核心玩法",
      result.gameplay || "",
      "",
      "✨ 特色亮点",
      result.highlights || "",
      "",
      "👍 推荐理由",
      result.recommendation || "",
    ].join("\n")
    navigator.clipboard.writeText(text).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    })
  }

  return (
    <div className="ai-game-intro-page">
      <div className="ai-intro-container">
        <div className="ai-intro-header">
          <div className="ai-intro-icon">🤖</div>
          <h1>AI 游戏介绍生成器</h1>
          <p className="ai-intro-subtitle">输入游戏名称，选择风格偏好，AI 将为你生成引人入胜的游戏介绍文案</p>
        </div>
        <div className="ai-intro-form">
          <div className="form-row">
            <div className="form-field game-name-field">
              <label>🎯 游戏名称</label>
              <input type="text" value={gameName} onChange={(e) => setGameName(e.target.value)} placeholder="输入游戏名称，如：赛博朋克2077..." onKeyDown={(e) => e.key === "Enter" && handleGenerate()} disabled={generating} />
            </div>
          </div>
          <div className="form-row form-row-double">
            <div className="form-field">
              <label>🎭 游戏类型（可选）</label>
              <div className="tag-group">
                {GENRES.map(g => (
                  <span key={g} className={"tag" + (genre === g ? " active" : "") + (generating ? " disabled" : "")} onClick={() => !generating && setGenre(genre === g ? "" : g)}>{g}</span>
                ))}
              </div>
            </div>
          </div>
          <div className="form-row form-row-double">
            <div className="form-field">
              <label>🎨 文案风格（可选）</label>
              <div className="tag-group">
                {STYLES.map(s => (
                  <span key={s} className={"tag" + (style === s ? " active" : "") + (generating ? " disabled" : "")} onClick={() => !generating && setStyle(style === s ? "" : s)}>{s}</span>
                ))}
              </div>
            </div>
          </div>
          <button className="ai-generate-btn" onClick={handleGenerate} disabled={generating || !gameName.trim()}>
            {generating ? (
              <><span className="spinner" /> AI 正在创作中...</>
            ) : (
              <><span>✨</span> 生成游戏介绍</>
            )}
          </button>
        </div>
        {error && (
          <div className="ai-result-card error">
            <div className="ai-result-header"><span className="ai-result-icon">⚠️</span><h3>生成失败</h3></div>
            <p>{error}</p>
          </div>
        )}
        {result && !generating && (
          <div className="ai-result-card" ref={resultRef}>
            <div className="ai-result-header">
              <span className="ai-result-icon">📝</span>
              <h3>{result.title || gameName}</h3>
              <button className="ai-copy-btn" onClick={handleCopy}>{copied ? "✅ 已复制" : "📋 复制全文"}</button>
            </div>
            <div className="ai-result-body">
              <div className="ai-section"><h4>📖 简介</h4><p>{result.summary}</p></div>
              {result.background && (<div className="ai-section"><h4>🌍 背景故事</h4><p>{result.background}</p></div>)}
              {result.gameplay && (<div className="ai-section"><h4>🎮 核心玩法</h4><p>{result.gameplay}</p></div>)}
              {result.highlights && (<div className="ai-section"><h4>✨ 特色亮点</h4><p>{result.highlights}</p></div>)}
              {result.recommendation && (<div className="ai-section"><h4>👍 推荐理由</h4><p>{result.recommendation}</p></div>)}
            </div>
            <div className="ai-result-footer">
              <button className="ai-regenerate-btn" onClick={handleGenerate}>🔄 重新生成</button>
              <span className="ai-disclaimer">* 内容由 AI 生成，仅供参考</span>
            </div>
          </div>
        )}
        {!result && !generating && !error && (
          <div className="ai-intro-placeholder">
            <div className="placeholder-icon">🎮</div>
            <p>输入游戏名称后点击生成，AI 将为你撰写精彩的游戏介绍</p>
            <div className="placeholder-examples">
              <span>试试这些：</span>
              {["赛博朋克2077", "原神", "黑神话：悟空", "艾尔登法环"].map(name => (
                <button key={name} className="example-chip" onClick={() => setGameName(name)}>{name}</button>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default AIGameIntro