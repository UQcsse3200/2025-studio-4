# UQ 2025 Studio 4

## 📖 Description
This project is developed as part of **CSSE3200 Software Engineering Studio** at the [University of Queensland](https://uq.edu.au/).  
It provides a fun, team-based environment in which students collaboratively design, implement, and test a working game over the semester.  

The engine is built on the [libGDX](https://libgdx.com/) framework and written in Java.  
It is open-sourced under the [MIT License](https://opensource.org/licenses/MIT).

---

## 🎯 Feature Team Contribution – Hero System
Our feature team is responsible for implementing the **Hero System**, including:

- **Rotating Hero Rendering**  
  Hero sprite rotates to face the mouse cursor using a new `RotatingTextureRenderComponent`.

- **Hero Turret Attack Component**  
  Hero can shoot bullets towards the mouse with configurable attributes:
  - `cooldown` (attack rate)  
  - `bulletSpeed`  
  - `bulletLife`  
  - `damage`

- **Integration with Game Engine**  
  Implemented in `HeroFactory` to spawn a hero with physics, collision, rendering, and attack logic.  
  Fully integrated with the engine's **Entity Component System (ECS)** and `ProjectileFactory`.

---

## 📂 File Structure and Responsibilities

### 🎨 Assets
```
source/core/assets/images/
├─ hero/
│ ├─ Heroshoot.png # Default hero sprite (rotatable)
│ └─ Bullet.png # Bullet sprite
└─ base_enemy.png # Enemy sprite (used in EnemyFactory)
```

**Asset Purposes**
- `hero/` – folder containing hero sprites (used in `HeroFactory` & `HeroTurretAttackComponent`).
- `base_enemy.png` – default enemy sprite, used in `EnemyFactory`.

---

### 🧩 Code
```
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
└─ EnemyConfig.java # Config values for enemies
```

**File Purposes**
- **HeroTurretAttackComponent.java** – Controls hero’s turret behaviour (aim, rotation, shooting).  
- **HeroFactory.java** – Central place to spawn hero entity with all required components.  
- **RotatingTextureRenderComponent.java** – Provides rotation support for rendering hero/enemy sprites.  
- **EnemyFactory.java & Enemy.java** – Build and define enemy entities, integrated alongside hero for combat interactions.  
- **HeroConfig.java / EnemyConfig.java** – Store configurable attributes (HP, attack, textures, cooldowns).  
- **Physics components** – Ensure hero/enemy interact correctly (collisions, layers).  
---

## 🔫 Projectiles / Bullets

### 📂 Code
```
source/core/src/main/com/csse3200/game/
├─ entities/factories/ProjectileFactory.java # Creates bullet entities with texture, speed, lifetime
├─ components/projectile/
│  ├─ DestroyOnHitComponent.java  # Destroys bullet on impact and applies damage
│  └─ ProjectileComponent.java    # Handles bullet movement and behaviour
```


### 🔎 Responsibilities
- **ProjectileFactory** – Creates a standard bullet entity with rendering, physics, lifetime, combat stats, and collision logic.  
- **HeroTurretAttackComponent** – Spawns bullets in the direction of the mouse, passing speed, lifetime, and damage values.  
- **ProjectileComponent** – Controls bullet motion and removes it after its lifetime expires.  
- **TouchAttackComponent** – Applies the bullet’s damage (from `CombatStatsComponent`) to enemy entities on collision.  
- **DestroyOnHitComponent** – Ensures the bullet is safely destroyed when it collides with a target.  
- **PhysicsLayer.PROJECTILE** – Restricts bullet collisions to enemies (e.g., not the player).  

---

### 🔁 Lifecycle (actual)
1. **Spawn** → `HeroTurretAttackComponent` calls `ProjectileFactory.createBullet(...)`.  
   The bullet entity is assembled with:  
   - `TextureRenderComponent` (visuals)  
   - `PhysicsComponent` (movement & collisions)  
   - `ProjectileComponent(vx, vy, life)` (velocity & lifetime)  
   - `CombatStatsComponent(damage)` (stores bullet damage)  
   - `TouchAttackComponent` (applies damage to enemies)  
   - `DestroyOnHitComponent` (destroys bullet on collision)

2. **Init** → In `ProjectileComponent.create()`, the lifetime timer starts and the physics body is given linear velocity.  

3. **Fly** → Bullet travels through the world under physics simulation.  

4. **End** → The bullet is removed under either condition:  
   - **Hit**: `TouchAttackComponent` applies damage to the enemy; `DestroyOnHitComponent` schedules bullet destruction.  
   - **Timeout**: `ProjectileComponent.update()` timer expires → physics disabled → bullet scheduled for removal.  

5. **Cleanup** → Removal is deferred with `Gdx.app.postRunnable`, calling `entity.dispose()` and `EntityService.unregister()` on the next frame to avoid concurrent modification errors.

---

## Documentation and Reports

- [JavaDoc](https://uqcsse3200.github.io/2025-studio-4/)
- [SonarCloud](https://sonarcloud.io/project/overview?id=UQcsse3200_2025-studio-4)
