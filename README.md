# UQ 2025 Studio 4

## Description

This project is developed as part of **CSSE3200 Software Engineering Studio** at the [University of Queensland](https://uq.edu.au/).  
It provides a fun, team-based environment in which students collaboratively design, implement, and test a working game over the semester.  

The engine is built on the [libGDX](https://libgdx.com/) framework and written in Java.  
It is open-sourced under the [MIT License](https://opensource.org/licenses/MIT) (please acknowledge the source if reused).

---
## 🎯 Feature Team Contribution - Hero System
Our feature team is responsible for implementing the **Hero System**, including:

- **Rotating Hero Rendering**  
  - Hero sprite rotates to face the mouse cursor using a new `RotatingTextureRenderComponent`.
- **Hero Turret Attack Component**  
  - Hero can shoot bullets towards the mouse with configurable attributes:
    - `cooldown` (attack rate)  
    - `bulletSpeed`  
    - `bulletLife`  
    - `damage`
- **Integration with Game Engine**  
  - Implemented in `HeroFactory` to spawn a hero with physics, collision, rendering, and attack logic.  
  - Fully integrated with the engine's **Entity Component System (ECS)** and `ProjectileFactory`.

## 📂 File Structure and Responsibilities

After integration with the **Enemy system**, our Hero feature is organised as follows:
```
source/core/assets/images/
├─ hero/ # Hero sprites
│ └─ Heroshoot.png # Default hero sprite (rotatable)
│ └─ Bullet.png # Default hero sprite (rotatable)
└─ base_enemy.png # Enemy sprite (used in EnemyFactory)
### 🔎 Asset Purposes
- **hero/** - folder containing all hero sprites (used in `HeroFactory` & `HeroTurretAttackComponent`).
- **base_enemy.png** - default enemy sprite, used in `EnemyFactory`.

source/core/src/main/com/csse3200/game/
├─ components/
│ └─ hero/
│ └─ HeroTurretAttackComponent.java # Handles aiming, shooting, cooldown logic
│
├─ entities/
│ ├─ factories/
│ │ ├─ HeroFactory.java # Builds hero entity (physics, rendering, attack, combat stats)
│ │ └─ EnemyFactory.java # Builds enemy entities with AI/behaviour
│ └─ Enemies/
│ └─ Enemy.java # Base enemy entity definition
│
├─ rendering/
│ ├─ TextureRenderComponent.java # Static texture rendering (used by tests)
│ └─ RotatingTextureRenderComponent.java # Rotating texture rendering (used by Hero & bullets)
│
├─ physics/
│ ├─ components/ColliderComponent.java # Collision handling
│ ├─ components/HitboxComponent.java # Hitbox layer setup
│ └─ PhysicsLayer.java # Defines collision layers (PLAYER, ENEMY, PROJECTILE, etc.)
│
└─ entities/configs/
├─ HeroConfig.java # Config values for hero (health, attack, cooldown, textures)
└─ EnemyConfig.java # Config values for enemie

### 🔎 File Purposes
- **HeroTurretAttackComponent.java** - Controls hero's turret behaviour:
  - Rotates hero sprite towards mouse.
  - Fires bullets using `ProjectileFactory`.
- **HeroFactory.java** - Central place to spawn hero entity with all required components.
- **RotatingTextureRenderComponent.java** - Provides rotation support for rendering hero/enemy sprites.
- **EnemyFactory.java & Enemy.java** - Build and define enemy entities, integrated alongside hero for combat interactions.
- **HeroConfig.java / EnemyConfig.java** - Store configurable attributes (HP, attack, textures, cooldowns).
- **Physics components** - Ensure hero/enemy interact correctly (collisions, layers).

## Documentation and Reports

- [JavaDoc](https://uqcsse3200.github.io/2025-studio-4/)
- [SonarCloud](https://sonarcloud.io/project/overview?id=UQcsse3200_2025-studio-4)
