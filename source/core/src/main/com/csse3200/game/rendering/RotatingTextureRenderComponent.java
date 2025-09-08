package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/**
 * 支持围绕中心旋转的贴图渲染组件，并允许在运行时切换贴图。
 */
public class RotatingTextureRenderComponent extends RenderComponent {
  /** 当前贴图对象（非 final，允许切换） */
  private Texture texture;
  /** 当前贴图路径（可为空，如果用的是构造传入的 Texture） */
  private String texturePath;

  /** 旋转角（度） */
  private float rotationDeg = 0f;

  /** 用已有 Texture 构造 */
  public RotatingTextureRenderComponent(Texture texture) {
    this.texture = texture;
    this.texturePath = null;
  }

  /** 用路径构造（从资源服务获取） */
  public RotatingTextureRenderComponent(String texturePath) {
    this.texturePath = texturePath;
    this.texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);
  }

  /** 设置旋转角（度） */
  public void setRotation(float deg) { this.rotationDeg = deg; }

  /** 获取旋转角（度） */
  public float getRotation() { return rotationDeg; }

  /** 获取当前贴图（可能为 null，如果资源未加载成功） */
  public Texture getTexture() { return texture; }

  /** 获取当前贴图路径（可能为 null） */
  public String getTexturePath() { return texturePath; }

  /**
   * 运行时切换贴图（通过路径）。若资源未预加载，会同步加载并短暂等待。
   */
  public void setTexture(String newPath) {
    if (newPath == null || newPath.isBlank()) return;
    if (newPath.equals(this.texturePath) && this.texture != null) return;

    var rs = ServiceLocator.getResourceService();
    Texture tex = rs.getAsset(newPath, Texture.class);
    if (tex == null) {
      // 兜底：临时加载并等待（建议仍然在加载阶段预加载，避免当帧卡顿）
      rs.loadTextures(new String[]{newPath});
      while (!rs.loadForMillis(5)) { /* spin briefly */ }
      tex = rs.getAsset(newPath, Texture.class);
      if (tex == null) return; // 加载失败就不切换
    }

    this.texture = tex;
    this.texturePath = newPath;
    // 如需通知渲染系统“贴图已变更”，可在此处发事件或置脏标记
  }

  /**
   * 运行时切换贴图（直接给 Texture）。
   * 注意：调用方需确保该 Texture 的生命周期由 ResourceService 管理或在场景结束时正确释放。
   */
  public void setTexture(Texture newTexture) {
    if (newTexture == null || newTexture == this.texture) return;
    this.texture = newTexture;
    // 不更新 texturePath（它仅在用路径加载时用于记录）
  }

  /** 绘制：围绕中心旋转 */
  @Override
  protected void draw(SpriteBatch batch) {
    if (entity == null || texture == null) return;

    Vector2 pos = entity.getPosition();
    Vector2 size = entity.getScale();
    float w = (size == null ? 1f : size.x);
    float h = (size == null ? 1f : size.y);

    batch.draw(
            texture,
            pos.x, pos.y,
            w / 2f, h / 2f,     // 原点（中心）
            w, h,               // 宽高：使用实体 scale 作为世界尺寸
            1f, 1f,             // 缩放
            rotationDeg,        // 旋转角（度）
            0, 0,
            texture.getWidth(), texture.getHeight(),
            false, false
    );
  }
}


