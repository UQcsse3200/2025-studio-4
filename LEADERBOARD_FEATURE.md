# 本地排行榜功能 - 玩家姓名和分数保存

## 功能概述

本次更新为游戏添加了完整的本地排行榜功能，能够保存玩家在游戏结束时输入的姓名和获得的分数。所有数据都会持久化保存到本地文件中，在游戏重启后仍然可以查看历史记录。

## 主要改进

### 1. 持久化排行榜服务 (`PersistentLeaderboardService`)
- **文件保存**: 排行榜数据自动保存到 `saves/leaderboard.json` 文件
- **自动排序**: 按分数降序排列，分数相同时按时间升序排列
- **排名计算**: 自动计算和更新玩家排名
- **数据恢复**: 游戏启动时自动加载历史排行榜数据

### 2. 改进的游戏结束界面 (`MainGameOver`)
- **玩家姓名输入**: 游戏结束时可以输入玩家姓名
- **分数显示**: 显示本次游戏的最终分数
- **即时保存**: 点击"重新开始"或"主菜单"时自动保存分数和姓名
- **输入验证**: 空姓名时使用默认名称"Player"

### 3. 玩家姓名服务集成
- **姓名管理**: 通过 `PlayerNameService` 统一管理玩家姓名
- **会话保持**: 在游戏会话中保持玩家姓名
- **默认处理**: 智能处理空白或无效姓名

## 技术实现

### 核心组件

1. **PersistentLeaderboardService**
   ```java
   // 添加新的分数记录
   leaderboardService.addEntry("PlayerName", 1500);
   
   // 获取排行榜前10名
   List<LeaderboardEntry> topEntries = leaderboardService.getTopEntries(10);
   
   // 获取当前玩家最佳成绩
   LeaderboardEntry myBest = leaderboardService.getMyBest();
   ```

2. **数据结构**
   ```java
   public class LeaderboardEntry {
       public final int rank;        // 排名 (1, 2, 3, ...)
       public final String playerId; // 玩家ID
       public final String displayName; // 显示姓名
       public final long score;      // 分数
       public final long achievedAtMs; // 达成时间戳
   }
   ```

3. **JSON存储格式**
   ```json
   {
     "entries": [
       {
         "rank": 1,
         "playerId": "player-001",
         "displayName": "Alice",
         "score": 2500,
         "achievedAtMs": 1703123456789
       }
     ],
     "version": 1,
     "lastUpdated": 1703123456789
   }
   ```

### 文件位置
- **排行榜数据**: `saves/leaderboard.json`
- **自动创建**: 如果文件不存在，会在首次保存时自动创建

## 使用流程

### 游戏结束时
1. 游戏结束后显示结束界面
2. 界面显示本次游戏分数
3. 玩家在文本框中输入姓名（可选，默认为"Player"）
4. 点击"重新开始"或"主菜单"按钮
5. 系统自动保存姓名和分数到排行榜
6. 数据持久化到本地JSON文件

### 查看排行榜
1. 通过游戏内排行榜UI查看
2. 按分数从高到低排序显示
3. 显示排名、姓名、分数和达成时间

## 配置和自定义

### 修改保存位置
在 `PersistentLeaderboardService.java` 中修改：
```java
private static final String LEADERBOARD_FILE = "saves/leaderboard.json";
```

### 修改最大姓名长度
在 `MainGameOver.java` 中修改：
```java
nameField.setMaxLength(12); // 当前限制为12个字符
```

### 清空排行榜
```java
PersistentLeaderboardService service = (PersistentLeaderboardService) ServiceLocator.getLeaderboardService();
service.clearLeaderboard(); // 清空所有记录并删除文件
```

## 错误处理

### 文件操作失败
- 保存失败时记录错误日志，但不影响游戏继续
- 加载失败时使用空的排行榜开始

### 数据验证
- 自动过滤无效的姓名输入
- 处理负分数和异常数值
- JSON解析错误时回退到默认状态

## 测试

运行单元测试验证功能：
```bash
./gradlew test --tests "*PersistentLeaderboardServiceTest"
```

测试覆盖：
- ✅ 添加新分数记录
- ✅ 多个记录的排名计算
- ✅ 空姓名的默认处理
- ✅ 获取玩家最佳成绩
- ✅ 无记录时的默认行为

## 兼容性

- **向后兼容**: 现有游戏存档不受影响
- **升级平滑**: 首次运行时自动创建排行榜文件
- **跨平台**: 支持Windows、Mac、Linux

## 未来扩展

可以考虑的功能扩展：
- 在线排行榜同步
- 不同难度等级的分别排行榜
- 成就系统集成
- 排行榜导出功能
- 玩家头像支持

---

**注意**: 排行榜数据保存在本地，卸载游戏或删除存档文件会丢失排行榜记录。建议定期备份 `saves/` 目录。
