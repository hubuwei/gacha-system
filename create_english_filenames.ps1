# ============================================
# 为GamePapers中的中文文件名游戏创建英文文件名副本
# ============================================

$gamePapersPath = "E:\CFDemo\gacha-system\GamePapers"

# 定义中文到英文的映射关系
$fileNameMap = @{
    "原神.jpg" = "original-god.jpg"
    "王者荣耀.jpg" = "honor-of-kings.jpg"
    "和平精英.jpg" = "pubg.jpg"
    "绝地求生.jpg" = "pubg.jpg"
    "英雄联盟.jpg" = "league-of-legends.jpg"
    "黑神话悟空.jpg" = "black-myth-wukong.jpg"
    "倩女幽魂.jpg" = "chinese-ghost-story.jpg"
    "崩坏星穹铁道.jpg" = "honkai-star-rail.jpg"
    "冒险岛.jpg" = "maplestory.jpg"
    "我的世界.jpg" = "minecraft.jpg"
    "堡垒之夜.jpg" = "fortnite.jpg"
    "DOTA2.jpg" = "dota2.jpg"
    "守望先锋.jpg" = "overwatch.jpg"
    "Apex英雄.jpg" = "apex-legends.jpg"
    "赛博朋克2077.jpg" = "cyberpunk-2077.jpg"
    "明日方舟.jpg" = "arknights.jpg"
    "阴阳师.jpg" = "onmyoji.jpg"
    "最终幻想.jpg" = "final-fantasy.jpg"
    "勇者斗恶龙.jpg" = "dragon-quest.jpg"
    "魔兽世界.jpg" = "world-of-warcraft.jpg"
    "GTA.jpg" = "gta.jpg"
    "只狼.jpg" = "sekiro.jpg"
    "艾尔登法环.jpg" = "elden-ring.jpg"
    "塞尔达传说.jpg" = "zelda.jpg"
    "马里奥.jpg" = "mario.jpg"
    "鬼泣.jpg" = "devil-may-cry.jpg"
    "生化危机.jpg" = "resident-evil.jpg"
    "怪物猎人.jpg" = "monster-hunter.jpg"
    "巫师3.jpg" = "witcher3.jpg"
    "刺客信条.jpg" = "assassins-creed.jpg"
    "炉石传说.jpg" = "hearthstone.jpg"
    "穿越火线.jpg" = "crossfire.jpg"
    "DNF.jpg" = "dnf.jpg"
    "CSGO.jpg" = "csgo.jpg"
    "QQ飞车.jpg" = "qq-speed.jpg"
    "剑网3.jpg" = "jx3.jpg"
    "天涯明月刀.jpg" = "moonlight-blade.jpg"
    "天龙八部.jpg" = "tlbb.jpg"
    "大话西游.jpg" = "westward-journey.jpg"
    "梦幻西游.jpg" = "fantasy-westward.jpg"
    "诛仙.jpg" = "zhuxian.jpg"
    "征途.jpg" = "zhengtu.jpg"
    "奇迹MU.jpg" = "miracle-mu.jpg"
    "热血传奇.jpg" = "legend-of-mir.jpg"
    "跑跑卡丁车.jpg" = "kart-rider.jpg"
    "逆水寒.jpg" = "justice-online.jpg"
    "问道.jpg" = "wendao.jpg"
    "口袋妖怪.jpg" = "pokemon.jpg"
    "永劫无间.jpg" = "naraka.jpg"
    "Valorant.jpg" = "valorant.jpg"
}

Write-Host "开始处理GamePapers文件夹..." -ForegroundColor Cyan

foreach ($chineseName in $fileNameMap.Keys) {
    $englishName = $fileNameMap[$chineseName]
    $chinesePath = Join-Path $gamePapersPath $chineseName
    $englishPath = Join-Path $gamePapersPath $englishName
    
    # 检查中文文件是否存在
    if (Test-Path $chinesePath) {
        # 检查英文文件是否已存在
        if (-not (Test-Path $englishPath)) {
            # 复制文件（保留原中文文件）
            Copy-Item $chinesePath $englishPath
            Write-Host "✓ 已创建: $englishName" -ForegroundColor Green
        } else {
            Write-Host "- 已存在: $englishName" -ForegroundColor Yellow
        }
    } else {
        Write-Host "✗ 不存在: $chineseName" -ForegroundColor Red
    }
}

Write-Host "`n处理完成！" -ForegroundColor Cyan
Write-Host "请检查以上输出，确保所有需要的文件都已创建。" -ForegroundColor White
