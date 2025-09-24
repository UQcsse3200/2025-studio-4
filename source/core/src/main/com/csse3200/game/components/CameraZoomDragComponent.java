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
 * 支持鼠标滚轮缩放和WASD键盘移动
 */
public class CameraZoomDragComponent extends InputComponent {
    
    // 缩放相关常量
    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 1.8f;
    private static final float ZOOM_SPEED = 0.1f;
    private static final float DEFAULT_ZOOM = 1.4f; // 默认缩放倍数（1.4倍，显示更多内容）
    
    // 键盘移动相关常量
    private static final float MOVE_SPEED = 5.0f; // 相机移动速度
    
    // 鼠标拖拽相关常量
   // private static final float DRAG_SENSITIVITY = 0.01f; // 拖拽灵敏度（数值越小越不敏感）
    
    // 键盘状态跟踪
    private boolean wPressed = false;
    private boolean aPressed = false;
    private boolean sPressed = false;
    private boolean dPressed = false;
    
    // 鼠标右键拖拽相关变量
//     private boolean isDragging = false;
//     private Vector2 lastMousePosition = new Vector2();
//     private Vector2 dragStartPosition = new Vector2();
    
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
        

        handleKeyboardMovement();
        //handleMouseDrag();
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

     * 处理鼠标右键拖拽
     */
//     private void handleMouseDrag() {
//         if (camera == null) return;

//      * 处理鼠标滚轮缩放
//      * 暂时注释掉滚轮缩放功能
//      */
//     @Override
//     public boolean scrolled(float amountX, float amountY) {
//         // 暂时注释掉滚轮缩放功能
//         return false;
        
        /*
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
        
        if (isDragging) {
            Vector2 currentMousePosition = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            Vector2 mouseDelta = new Vector2(currentMousePosition).sub(lastMousePosition);
            
            // 将屏幕坐标转换为世界坐标的移动
            // 注意：Y轴需要反转，因为屏幕坐标Y轴向下，而世界坐标Y轴向上
            // 同时将X轴也反转，实现拖拽方向反向
            Vector2 worldDelta = new Vector2(-mouseDelta.x, mouseDelta.y);
            
            // 应用拖拽灵敏度
            worldDelta.scl(DRAG_SENSITIVITY);
            
            // 根据当前缩放级别调整移动速度
            float zoomFactor = 1.0f / ((OrthographicCamera) camera).zoom;
            worldDelta.scl(zoomFactor);
            
            // 移动相机
            camera.position.add(worldDelta.x, worldDelta.y, 0);
            camera.update();
            
            lastMousePosition.set(currentMousePosition);
        }
    }
    
    /**
     * 处理鼠标滚轮缩放
     */
   // @Override
   // public boolean scrolled(float amountX, float amountY) {
   //     if (camera == null) return false;
   //     
   //     handleZoomInput(amountY);
   //     return true;
   // }
    
    /**
     * 处理滚轮缩放
     */
//     private void handleZoomInput(float wheelAmount) {
//         if (camera == null || !(camera instanceof OrthographicCamera)) return;
        
//         OrthographicCamera orthoCamera = (OrthographicCamera) camera;
//         float currentZoom = orthoCamera.zoom;
//         float newZoom = currentZoom;
        
//         if (wheelAmount > 0) {
//             // 滚轮向上：放大
//             newZoom = Math.min(MAX_ZOOM, currentZoom + ZOOM_SPEED);
//         } else if (wheelAmount < 0) {
//             // 滚轮向下：缩小
//             newZoom = Math.max(MIN_ZOOM, currentZoom - ZOOM_SPEED);
//         } else {
//             return;
//         }
        
//         if (newZoom != currentZoom) {
//             orthoCamera.zoom = newZoom;
//             camera.update();
//         }
//     }
    
//         return true;
//         */
//     }
    
  
    
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
    }
    
    /**
     * 模拟按W键向上移动相机一格
     * 用于游戏开始时的初始相机位置调整
     */
    public void moveUpOneStep() {
        if (camera == null) return;
        
        float moveDistance = MOVE_SPEED * 0.1f;
        
        Renderer renderer = Renderer.getCurrentRenderer();
        if (renderer != null && renderer.camera != null && renderer.camera.getEntity() != null) {
            Vector2 currentPos = renderer.camera.getEntity().getPosition();
            Vector2 newPos = new Vector2(currentPos.x, currentPos.y + moveDistance);
            renderer.camera.getEntity().setPosition(newPos);
        } else {
            camera.position.add(0, moveDistance, 0);
            camera.update();
        }
    }
    
//     /**
//      * 处理鼠标按下事件
//      */
//     @Override
//     public boolean touchDown(int screenX, int screenY, int pointer, int button) {
//         if (button == Input.Buttons.RIGHT) {
//             isDragging = true;
//             lastMousePosition.set(screenX, screenY);
//             dragStartPosition.set(screenX, screenY);
//             return true;
//         }
//         return false;
//     }
    
//     /**
//      * 处理鼠标释放事件
//      */
//     @Override
//     public boolean touchUp(int screenX, int screenY, int pointer, int button) {
//         if (button == Input.Buttons.RIGHT) {
//             isDragging = false;
//             return true;
//         }
//         return false;
//     }
    
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
