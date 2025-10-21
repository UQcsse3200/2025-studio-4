package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

/**
 * 在实体销毁（dispose）前播放一次音效。
 * 用于召唤物死亡/回收的爆炸音效等。
 */
public class SfxOnDeathComponent extends Component {
    private String sfxKey = "sounds/explosion_2s.ogg"; // 你也可以用已有的 "sounds/Explosion_sfx.ogg"
    private float volume = 2.0f;
    private boolean played = false;

    public SfxOnDeathComponent() {}
    public SfxOnDeathComponent(String sfxKey, float volume) {
        if (sfxKey != null && !sfxKey.isBlank()) this.sfxKey = sfxKey;
        this.volume = Math.max(0f, Math.min(1f, volume));
    }

    public SfxOnDeathComponent setSfxKey(String key) {
        if (key != null && !key.isBlank()) this.sfxKey = key;
        return this;
    }

    public SfxOnDeathComponent setVolume(float vol) {
        this.volume = Math.max(0f, Math.min(1f, vol));
        return this;
    }

    private void playOnce() {
        if (played || sfxKey == null || sfxKey.isBlank()) return;
        played = true;

        float vol = Math.max(0f, Math.min(1f, volume));
        try {
            var rs = ServiceLocator.getResourceService();
            Sound s = null;
            if (rs != null) {
                try { s = rs.getAsset(sfxKey, Sound.class); } catch (Throwable ignored) {}
            }
            if (s != null) {
                s.play(vol);
                return;
            }
            // 回退：直接从文件创建（确保打包在 assets）
            if (Gdx.files.internal(sfxKey).exists() && Gdx.audio != null) {
                Sound s2 = Gdx.audio.newSound(Gdx.files.internal(sfxKey));
                s2.play(vol);
            } else {
                Gdx.app.error("SfxOnDeath", "Sound not found or audio unavailable: " + sfxKey);
            }
        } catch (Throwable t) {
            Gdx.app.error("SfxOnDeath", "Failed to play sfx: " + sfxKey, t);
        }
    }

    @Override
    public void dispose() {
        // 记录下位置仅用于调试（可选）
        Vector2 pos = (entity != null) ? entity.getPosition() : null;
        playOnce();
        super.dispose();
    }
}
