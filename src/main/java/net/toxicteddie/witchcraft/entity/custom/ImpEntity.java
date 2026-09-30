package net.toxicteddie.witchcraft.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

public class ImpEntity extends PathfinderMob {

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState flyingIdleAnimationState = new AnimationState();
    public final AnimationState flyAnimationState = new AnimationState();

    private static final EntityDataAccessor<Boolean> FLYING =
            SynchedEntityData.defineId(
                    ImpEntity.class,
                    EntityDataSerializers.BOOLEAN
            );
    private int flyingTime;
    private int nextFlightAttempt;

    private final MoveControl groundMoveControl;
    private final FlyingMoveControl flyingMoveControl;

    public ImpEntity(
            EntityType<? extends PathfinderMob> entityType,
            Level level
    ) {
        super(entityType, level);

        this.groundMoveControl = this.moveControl;
        this.flyingMoveControl = new FlyingMoveControl(this, 15, true);

        this.nextFlightAttempt = 100 + this.random.nextInt(200);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FLYING_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(
                1,
                new MeleeAttackGoal(this, 1.1D, false)
        );

        this.goalSelector.addGoal(
                5,
                new WaterAvoidingRandomFlyingGoal(this, 0.8D) {
                    @Override
                    public boolean canUse() {
                        return ImpEntity.this.isFlying() && super.canUse();
                    }

                    @Override
                    public boolean canContinueToUse() {
                        return ImpEntity.this.isFlying()
                                && super.canContinueToUse();
                    }
                }
        );

        this.goalSelector.addGoal(
                6,
                new RandomStrollGoal(this, 0.75D) {
                    @Override
                    public boolean canUse() {
                        return !ImpEntity.this.isFlying() && super.canUse();
                    }

                    @Override
                    public boolean canContinueToUse() {
                        return !ImpEntity.this.isFlying()
                                && super.canContinueToUse();
                    }
                }
        );

        this.goalSelector.addGoal(
                7,
                new LookAtPlayerGoal(this, Player.class, 8.0F)
        );

        this.goalSelector.addGoal(
                8,
                new RandomLookAroundGoal(this)
        );

        this.targetSelector.addGoal(
                1,
                new HurtByTargetGoal(this)
        );

        this.targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this,
                        Player.class,
                        true
                )
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            this.updateFlightState();
            this.updateFlyingIdleBob();
        } else {
            this.setupAnimationStates();
        }
    }

    private void updateFlyingIdleBob() {
        if (!this.isFlying()) {
            return;
        }

        boolean moving =
                this.getDeltaMovement().horizontalDistanceSqr() > 0.0005D
                        || this.moveControl.hasWanted();

        if (!moving) {
            double bobSpeed = 0.10D;
            double bobStrength = 0.025D;

            double verticalMotion =
                    Math.sin(this.tickCount * bobSpeed) * bobStrength;

            Vec3 currentMotion = this.getDeltaMovement();

            this.setDeltaMovement(
                    currentMotion.x,
                    verticalMotion,
                    currentMotion.z
            );
        }
    }

    private void updateFlightState() {
        if (this.isFlying()) {
            this.flyingTime--;


            Vec3 motion = this.getDeltaMovement();

            if (!this.moveControl.hasWanted()) {
                this.setDeltaMovement(
                        motion.x,
                        Math.max(motion.y, -0.02D),
                        motion.z
                );
            }

            if (this.flyingTime <= 0 && this.onGround()) {
                this.stopFlying();
            }
        } else {
            this.nextFlightAttempt--;

            if (this.nextFlightAttempt <= 0) {
                this.startFlying();
            }
        }
    }

    private void startFlying() {
        this.setFlying(true);
        this.flyingTime = 100 + this.random.nextInt(160);

        this.moveControl = this.flyingMoveControl;
        this.navigation = this.createFlyingNavigation(this.level());


        this.setDeltaMovement(
                this.getDeltaMovement().add(0.0D, 0.35D, 0.0D)
        );

        this.setNoGravity(true);
    }

    private void stopFlying() {
        this.setFlying(false);

        this.moveControl = this.groundMoveControl;
        this.navigation = this.createGroundNavigation(this.level());

        this.setNoGravity(false);
        this.nextFlightAttempt = 120 + this.random.nextInt(240);
    }

    private PathNavigation createFlyingNavigation(Level level) {
        FlyingPathNavigation navigation =
                new FlyingPathNavigation(this, level);

        navigation.setCanOpenDoors(false);
        navigation.setCanPassDoors(true);
        navigation.setCanFloat(true);

        return navigation;
    }

    private PathNavigation createGroundNavigation(Level level) {
        GroundPathNavigation navigation =
                new GroundPathNavigation(this, level);

        navigation.setCanOpenDoors(false);
        navigation.setCanPassDoors(true);
        navigation.setCanFloat(true);

        return navigation;
    }

    public boolean isFlying() {
        return this.entityData.get(FLYING);
    }

    private void setFlying(boolean flying) {
        this.entityData.set(FLYING, flying);
    }

    private void setupAnimationStates() {
        boolean flying = this.isFlying();

        boolean movingHorizontally =
                this.getDeltaMovement().horizontalDistanceSqr() > 0.0005D;

        if (flying) {
            this.idleAnimationState.stop();

            if (movingHorizontally) {
                this.flyingIdleAnimationState.stop();
                this.flyAnimationState.startIfStopped(this.tickCount);
            } else {
                this.flyAnimationState.stop();
                this.flyingIdleAnimationState.startIfStopped(this.tickCount);
            }
        } else {
            this.flyAnimationState.stop();
            this.flyingIdleAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
    }

    @Override
    protected void updateWalkAnimation(float partialTick) {
        float animationAmount =
                Math.min(partialTick * 6.0F, 1.0F);

        this.walkAnimation.update(animationAmount, 0.2F);
    }

    @Override
    public boolean causeFallDamage(
            float fallDistance,
            float damageMultiplier,
            DamageSource damageSource
    ) {
        return false;
    }

    @Override
    public boolean checkSpawnRules(
            LevelAccessor level,
            MobSpawnType spawnType
    ) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && super.checkSpawnRules(level, spawnType);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }

    @Override
    protected void playStepSound(
            BlockPos pos,
            BlockState state
    ) {
        this.playSound(
                SoundEvents.CHICKEN_STEP,
                0.15F,
                0.7F
        );
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FLYING, false);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
