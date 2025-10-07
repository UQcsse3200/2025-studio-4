package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.rendering.Renderer;

/**
 * 处理相机缩放和移动的输入组件
 * 支持鼠标中键拖动和鼠标滚轮缩放
 */
public class CameraZoomDragComponent extends InputComponent {
    
    // 缩放相关常量
    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 1.8f;
    private static final float ZOOM_SPEED = 0.1f;
    private static final float DEFAULT_ZOOM = 1.4f; // 默认缩放倍数（1.4倍，显示更多内容）
    
    
    // 鼠标拖拽相关常量
    private static final float DRAG_SENSITIVITY = 0.01f; // 拖拽灵敏度（数值越小越不敏感）
    
    
    // 鼠标中键拖拽相关变量
    private boolean isDragging = false;
    private Vector2 lastMousePosition = new Vector2();
    private Vector2 dragStartPosition = new Vector2();
    
    // 重用的临时变量，避免每帧分配新对象
    private Vector2 tempCurrentPos = new Vector2();
    private Vector2 tempMouseDelta = new Vector2();
    private Vector2 tempWorldDelta = new Vector2();
    
    // 相机引用
    private Camera camera;
    
    public CameraZoomDragComponent() {
        super(1000); // 高优先级，确保在其他输入处理之前处理（数字越大优先级越高）
    }
    
    @Override
    public void create() {
        super.create();
        Renderer renderer = Renderer.getCurrentRenderer();
        if (renderer != null && renderer.camera != null) {
            this.camera = renderer.camera.getCamera();
            setZoom(DEFAULT_ZOOM);
            moveUpOneStep();
        }
    }
    
    @Override
    public void update() {
        if (camera == null) {
            Renderer renderer = Renderer.getCurrentRenderer();
            if (renderer != null && renderer.camera != null) {
                this.camera = renderer.camera.getCamera();
                setZoom(DEFAULT_ZOOM);
            }
        }
        

        handleMouseDrag();
    }
    
    
    /**
     * 处理鼠标中键拖拽
     */
    private void handleMouseDrag() {
        if (camera == null) return;
        
        if (isDragging) {
            // 重用临时对象，避免每帧分配新内存
            tempCurrentPos.set(Gdx.input.getX(), Gdx.input.getY());
            tempMouseDelta.set(tempCurrentPos).sub(lastMousePosition);
            
            // 将屏幕坐标转换为世界坐标的移动
            // 注意：Y轴需要反转，因为屏幕坐标Y轴向下，而世界坐标Y轴向上
            // 同时将X轴也反转，实现拖拽方向反向
            tempWorldDelta.set(-tempMouseDelta.x, tempMouseDelta.y);
            
            // 应用拖拽灵敏度
            tempWorldDelta.scl(DRAG_SENSITIVITY);
            
            // 根据当前缩放级别调整移动速度
            float zoomFactor = 1.0f / ((OrthographicCamera) camera).zoom;
            tempWorldDelta.scl(zoomFactor);
            
            // 移动相机
            camera.position.add(tempWorldDelta.x, tempWorldDelta.y, 0);
            camera.update();
            
            lastMousePosition.set(tempCurrentPos);
        }
    }
    
    /**
     * 处理鼠标滚轮缩放
     */
    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (camera == null) return false;
        
        handleZoomInput(amountY);
        return true;
    }
    
    /**
     * 处理滚轮缩放
     */
    private void handleZoomInput(float wheelAmount) {
        if (camera == null || !(camera instanceof OrthographicCamera)) return;
        
        OrthographicCamera orthoCamera = (OrthographicCamera) camera;
        float currentZoom = orthoCamera.zoom;
        float newZoom = currentZoom;
        
        if (wheelAmount > 0) {
            // 滚轮向上：缩小
            newZoom = Math.min(MAX_ZOOM, currentZoom + ZOOM_SPEED);
        } else if (wheelAmount < 0) {
            // 滚轮向下：放大
            newZoom = Math.max(MIN_ZOOM, currentZoom - ZOOM_SPEED);
        } else {
            return;
        }
        
        if (newZoom != currentZoom) {
            orthoCamera.zoom = newZoom;
            camera.update();
        }
    }
    
    /**
     * 重置相机到默认位置和缩放
     */
    public void resetCamera() {
        if (camera == null) return;
        
        ((OrthographicCamera) camera).zoom = DEFAULT_ZOOM;
        camera.position.set(0, 0, 0);
        camera.update();
    }
    
    /**
     * 设置相机缩放级别
     */
    public void setZoom(float zoom) {
        if (camera == null) return;
        
        float clampedZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
        ((OrthographicCamera) camera).zoom = clampedZoom;
        camera.update();
    }
    
    /**
     * 模拟按W键向上移动相机一格
     * 用于游戏开始时的初始相机位置调整
     */
    public void moveUpOneStep() {
        if (camera == null) return;
        
        float moveDistance = 0.5f; // 固定的移动距离
        
        Renderer renderer = Renderer.getCurrentRenderer();
        if (renderer != null && renderer.camera != null && renderer.camera.getEntity() != null) {
            Vector2 currentPos = renderer.camera.getEntity().getPosition();
            // 重用临时对象，避免分配新内存
            tempCurrentPos.set(currentPos.x, currentPos.y + moveDistance);
            renderer.camera.getEntity().setPosition(tempCurrentPos);
        } else {
            camera.position.add(0, moveDistance, 0);
            camera.update();
        }
    }
    
    /**
     * 处理鼠标按下事件
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.MIDDLE) {
            isDragging = true;
            lastMousePosition.set(screenX, screenY);
            dragStartPosition.set(screenX, screenY);
            return true;
        }
        return false;
    }
    
    /**
     * 处理鼠标释放事件
     */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.MIDDLE) {
            isDragging = false;
            return true;
        }
        return false;
    }
    
}
