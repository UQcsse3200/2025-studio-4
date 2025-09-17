# 玩家姓名输入功能

## 功能概述

在游戏开始时，现在会显示一个对话框让玩家输入自己的姓名。这个姓名会在游戏过程中显示，让游戏体验更加个性化。

## 功能特点

### 用户界面
- **姓名输入对话框**: 当玩家点击"New Game"按钮时，会弹出一个美观的对话框
- **输入验证**: 姓名长度限制为1-20个字符
- **键盘支持**: 支持Enter键确认，ESC键取消
- **错误提示**: 输入无效时会显示相应的错误信息

### 技术实现
- **PlayerNameInputDialog**: 自定义对话框组件，处理用户输入
- **PlayerNameService**: 管理玩家姓名的服务类
- **ServiceLocator集成**: 通过服务定位器模式全局访问玩家姓名

## 使用流程

1. 启动游戏，进入主菜单
2. 点击"New Game"按钮
3. 在弹出的对话框中输入玩家姓名（1-20个字符）
4. 点击"Start Game"按钮或按Enter键确认
5. 游戏开始，玩家姓名显示在游戏界面左上角

## 代码结构

### 新增文件
- `PlayerNameInputDialog.java` - 姓名输入对话框
- `PlayerNameService.java` - 玩家姓名管理服务

### 修改文件
- `MainMenuActions.java` - 集成姓名输入流程
- `ServiceLocator.java` - 添加PlayerNameService支持
- `GdxGame.java` - 初始化PlayerNameService
- `PlayerStatsDisplay.java` - 显示玩家姓名

## API使用

### 获取玩家姓名
```java
// 通过ServiceLocator获取玩家姓名
String playerName = ServiceLocator.getPlayerNameService().getPlayerName();
```

### 设置玩家姓名
```java
// 设置新的玩家姓名
ServiceLocator.getPlayerNameService().setPlayerName("新姓名");
```

### 检查是否使用自定义姓名
```java
// 检查是否设置了自定义姓名（非默认的"Player"）
boolean hasCustomName = ServiceLocator.getPlayerNameService().hasCustomName();
```

## 样式和设计

对话框使用了游戏现有的UI风格：
- 使用`SimpleUI`样式系统
- 圆角边框和现代化外观
- 与游戏整体UI保持一致的颜色主题
- 响应式布局，适应不同屏幕尺寸

## 未来扩展

这个功能为以下特性奠定了基础：
- 排行榜系统中显示玩家姓名
- 保存文件中包含玩家姓名
- 多人游戏中的玩家识别
- 成就系统中的个性化信息

## 测试

功能已通过以下测试：
- ✅ 编译测试通过
- ✅ 输入验证正常工作
- ✅ 键盘快捷键响应正确
- ✅ 姓名在游戏中正确显示
- ✅ 服务正确注册和访问
