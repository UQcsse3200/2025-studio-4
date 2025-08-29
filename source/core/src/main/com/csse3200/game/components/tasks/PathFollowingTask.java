package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 路径跟随任务 - 使敌人沿着预定义的路径移动
 * 这是一个高优先级任务，用于替代普通的AI行为
 */
public class PathFollowingTask extends DefaultTask implements PriorityTask {
    private static final Logger logger = LoggerFactory.getLogger(PathFollowingTask.class);

    private final List<Vector2> pathPoints;
    private final float speed;
    private final int priority;
    private int currentTargetIndex = 0;
    private float reachThreshold = 0.5f; // 到达目标点的距离阈值
    private boolean pathCompleted = false;
    private MovementTask movementTask;

    /**
     * 创建路径跟随任务
     * @param pathPoints 路径点列表（世界坐标）
     * @param speed 移动速度
     * @param priority 任务优先级（应该高于其他AI任务）
     */
    public PathFollowingTask(List<Vector2> pathPoints, float speed, int priority) {
        this.pathPoints = pathPoints;
        this.speed = speed;
        this.priority = priority;
    }

    @Override
    public void start() {
        super.start();
        currentTargetIndex = 0;
        pathCompleted = false;

        if (pathPoints != null && !pathPoints.isEmpty()) {
            // 创建初始移动任务
            Vector2 firstTarget = pathPoints.get(0);
            movementTask = new MovementTask(firstTarget, reachThreshold);
            movementTask.create(owner);
            movementTask.start();

            logger.debug("开始路径跟随任务，目标点：{}", firstTarget);
            System.out.println("👻 开始路径跟随任务，第一个目标点：" + firstTarget);
        } else {
            pathCompleted = true;
            logger.warn("路径点为空，无法开始路径跟随");
        }
    }

    @Override
    public void update() {
        if (pathPoints == null || pathPoints.isEmpty() || pathCompleted) {
            status = Status.FINISHED;
            return;
        }

        if (currentTargetIndex >= pathPoints.size()) {
            pathCompleted = true;
            status = Status.FINISHED;
            System.out.println("👻 敌人完成了所有路径点的移动");
            return;
        }

        // 更新当前移动任务
        if (movementTask != null) {
            movementTask.update();

            // 如果到达当前目标点，移动到下一个目标
            if (movementTask.getStatus() == Status.FINISHED) {
                currentTargetIndex++;
                System.out.println("🎯 敌人到达路径点 " + currentTargetIndex + "/" + pathPoints.size());

                if (currentTargetIndex < pathPoints.size()) {
                    // 设置下一个目标点
                    Vector2 nextTarget = pathPoints.get(currentTargetIndex);
                    movementTask = new MovementTask(nextTarget, reachThreshold);
                    movementTask.create(owner);
                    movementTask.start();

                    logger.debug("移动到下一个目标点：{}", nextTarget);
                    System.out.println("➡️ 下一个目标点：" + nextTarget);
                } else {
                    pathCompleted = true;
                }
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (movementTask != null) {
            movementTask.stop();
        }
        logger.debug("停止路径跟随任务");
    }

    @Override
    public int getPriority() {
        // 如果路径已完成，返回负优先级，让其他任务接管
        if (pathCompleted) {
            return -1;
        }
        return priority;
    }

    /**
     * 重置路径跟随状态
     */
    public void resetPath() {
        currentTargetIndex = 0;
        pathCompleted = false;
        if (movementTask != null) {
            movementTask.stop();
        }

        if (pathPoints != null && !pathPoints.isEmpty()) {
            Vector2 firstTarget = pathPoints.get(0);
            movementTask = new MovementTask(firstTarget, reachThreshold);
            if (owner != null) {
                movementTask.create(owner);
                movementTask.start();
            }
        }
    }

    /**
     * 检查路径是否完成
     */
    public boolean isPathCompleted() {
        return pathCompleted;
    }

    /**
     * 获取当前目标点索引
     */
    public int getCurrentTargetIndex() {
        return currentTargetIndex;
    }

    /**
     * 设置到达目标点的距离阈值
     */
    public void setReachThreshold(float threshold) {
        this.reachThreshold = threshold;
    }
}
