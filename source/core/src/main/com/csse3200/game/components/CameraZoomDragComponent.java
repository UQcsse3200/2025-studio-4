package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.rendering.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理相机缩放和移动的输入组件
 * 支持鼠标滚轮缩放和WASD键盘移动
 */
public class CameraZoomDragComponent extends InputComponent {
    private static final Logger logger = LoggerFactory.getLogger(CameraZoomDragComponent.class);
    
    // 缩放相关常量
    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 1.8f;
    private static final float ZOOM_SPEED = 0.1f;
    private static final float DEFAULT_ZOOM = 1.4f; // 默认缩放倍数（0.7倍，显示更多内容）
    
    // 键盘移动相关常量
    private static final float MOVE_SPEED = 5.0f; // 相机移动速度
    
    // 键盘状态跟踪
    private boolean wPressed = false;
    private boolean aPressed = false;
    private boolean sPressed = false;
    private boolean dPressed = false;
    
    // 相机引用
    private Camera camera;
    
    public CameraZoomDragComponent() {
        super(10); // 高优先级，确保在其他输入处理之前处理
    }
    
    @Override
    public void create() {
        super.create();
        // 获取相机引用
        Renderer renderer = Renderer.getCurrentRenderer();
        if (renderer != null && renderer.camera != null) {
            this.camera = renderer.camera.getCamera();
            // 设置默认缩放
            setZoom(DEFAULT_ZOOM);
            // 游戏开始时相机向上移动一格
            moveUpOneStep();
            logger.debug("相机初始化完成，设置默认缩放为: {}，并向上移动一格", DEFAULT_ZOOM);
        }
    }
    
    @Override
    public void update() {
        if (camera == null) {
            // 尝试重新获取相机引用
            Renderer renderer = Renderer.getCurrentRenderer();
            if (renderer != null && renderer.camera != null) {
                this.camera = renderer.camera.getCamera();
                // 在重新获取相机后设置默认缩放
                setZoom(DEFAULT_ZOOM);
                logger.debug("重新获取相机并设置默认缩放为: {}", DEFAULT_ZOOM);
            }
        }
        
        // 处理WASD键盘移动
        handleKeyboardMovement();
    }
    
    /**
     * 处理WASD键盘移动
     */
    private void handleKeyboardMovement() {
        if (camera == null) return;
        
        float deltaTime = Gdx.graphics.getDeltaTime();
        float moveDistance = MOVE_SPEED * deltaTime;
        
        Vector2 movement = new Vector2();
        
        // 计算移动方向
        if (wPressed) movement.y += moveDistance;
        if (sPressed) movement.y -= moveDistance;
        if (aPressed) movement.x -= moveDistance;
        if (dPressed) movement.x += moveDistance;
        
        // 应用移动
        if (movement.len2() > 0) {
            camera.position.add(movement.x, movement.y, 0);
            camera.update();
        }
    }
    
    /**
     * 处理鼠标滚轮缩放
     */
    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (camera == null) return false;
        
        // 获取鼠标在世界坐标中的位置
        Vector3 mouseWorldPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseWorldPos);
        
        // 计算缩放前的世界坐标
        Vector2 worldPosBeforeZoom = new Vector2(mouseWorldPos.x, mouseWorldPos.y);
        
        // 计算新的缩放级别
        float currentZoom = ((OrthographicCamera) camera).zoom;
        float zoomDelta = amountY * ZOOM_SPEED;
        float newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, currentZoom + zoomDelta));
        
        // 应用缩放
        ((OrthographicCamera) camera).zoom = newZoom;
        camera.update();
        
        // 计算缩放后的世界坐标
        Vector3 mouseWorldPosAfter = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseWorldPosAfter);
        Vector2 worldPosAfterZoom = new Vector2(mouseWorldPosAfter.x, mouseWorldPosAfter.y);
        
        // 调整相机位置以保持鼠标指向的世界位置不变
        Vector2 offset = worldPosBeforeZoom.sub(worldPosAfterZoom);
        camera.position.add(offset.x, offset.y, 0);
        camera.update();
        
        logger.debug("Camera zoom: {} -> {}, position: ({}, {})", 
                    currentZoom, newZoom, camera.position.x, camera.position.y);
        
        return true;
    }
    
  
    
    /**
     * 重置相机到默认位置和缩放
     */
    public void resetCamera() {
        if (camera == null) return;
        
        ((OrthographicCamera) camera).zoom = DEFAULT_ZOOM;
        camera.position.set(0, 0, 0);
        camera.update();
        
        logger.debug("重置相机到默认位置和缩放: {}", DEFAULT_ZOOM);
    }
    
    /**
     * 设置相机缩放级别
     */
    public void setZoom(float zoom) {
        if (camera == null) return;
        
        float clampedZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
        ((OrthographicCamera) camera).zoom = clampedZoom;
        camera.update();
        
        logger.debug("设置相机缩放到: {}", clampedZoom);
    }
    
    /**
     * 获取当前缩放级别
     */
    public float getZoom() {
        if (camera == null) return DEFAULT_ZOOM;
        return ((OrthographicCamera) camera).zoom;
    }
    
    /**
     * 获取相机位置
     */
    public Vector2 getCameraPosition() {
        if (camera == null) return new Vector2(0, 0);
        return new Vector2(camera.position.x, camera.position.y);
    }
    
    /**
     * 设置相机位置
     */
    public void setCameraPosition(Vector2 position) {
        if (camera == null) return;
        
        camera.position.set(position.x, position.y, 0);
        camera.update();
        
        logger.debug("设置相机位置到: ({}, {})", position.x, position.y);
    }
    
    /**
     * 模拟按W键向上移动相机一格
     * 用于游戏开始时的初始相机位置调整
     */
    public void moveUpOneStep() {
        if (camera == null) return;
        
        // 使用一个合适的移动距离，模拟按一次W键的效果
        // 基于MOVE_SPEED和典型的deltaTime值来计算
        float moveDistance = MOVE_SPEED * 0.1f; // 相当于按W键0.1秒的移动距离
        
        // 获取相机实体并移动它，而不是直接移动相机
        Renderer renderer = Renderer.getCurrentRenderer();
        if (renderer != null && renderer.camera != null && renderer.camera.getEntity() != null) {
            Vector2 currentPos = renderer.camera.getEntity().getPosition();
            Vector2 newPos = new Vector2(currentPos.x, currentPos.y + moveDistance);
            renderer.camera.getEntity().setPosition(newPos);
            
            logger.debug("相机实体向上移动一格，移动距离: {}，新位置: ({}, {})", moveDistance, newPos.x, newPos.y);
        } else {
            // 如果无法获取相机实体，直接移动相机作为备选方案
            camera.position.add(0, moveDistance, 0);
            camera.update();
            logger.debug("相机直接向上移动一格，移动距离: {}，新位置: ({}, {})", moveDistance, camera.position.x, camera.position.y);
        }
    }
    
    /**
     * 处理键盘按下事件
     */
    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.W:
                wPressed = true;
                return true;
            case Input.Keys.A:
                aPressed = true;
                return true;
            case Input.Keys.S:
                sPressed = true;
                return true;
            case Input.Keys.D:
                dPressed = true;
                return true;
            default:
                return false;
        }
    }
    
    /**
     * 处理键盘释放事件
     */
    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.W:
                wPressed = false;
                return true;
            case Input.Keys.A:
                aPressed = false;
                return true;
            case Input.Keys.S:
                sPressed = false;
                return true;
            case Input.Keys.D:
                dPressed = false;
                return true;
            default:
                return false;
        }
    }
}
