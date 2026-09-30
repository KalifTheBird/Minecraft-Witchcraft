package net.toxicteddie.witchcraft.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.toxicteddie.witchcraft.entity.path.CircleAroundPlayerGoal;

public class MandrakeEntity extends PathfinderMob {

    public MandrakeEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
    }

    public final AnimationState idleAnimationState = new AnimationState();

    @Override
    public void tick() {
        super.tick();

        if(this.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        this.idleAnimationState.startIfStopped(this.tickCount);
    }

    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float f;
        if(this.getPose() == Pose.STANDING) {
            f = Math.min(pPartialTick * 6F, 1f);
        } else {
            f = 0f;
        }

        this.walkAnimation.update(f, 0.2f);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.55D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CircleAroundPlayerGoal(this, 1.0D, 5.0D, 0.1D));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        this.level().players().stream()
                .filter(player -> player.distanceToSqr(this) < 25)
                .forEach(player -> {
                    if (!player.hasEffect(MobEffects.CONFUSION)) {
                        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
                    }
                });

        if (this.tickCount % 100 == 0) {
            this.playScreamSound();
        }
    }
    private void playScreamSound() {
        this.playSound(SoundEvents.GHAST_SCREAM, 1.0F, 1.0F);
    }
     @Override
     protected void playStepSound(BlockPos pos, BlockState blockIn) {
         this.playSound(SoundEvents.GHAST_SCREAM, 1.0F, 1.0F);
     }

    @Override
    public boolean checkSpawnRules(LevelAccessor world, MobSpawnType spawnReason) {

        return world.getDifficulty() != Difficulty.PEACEFUL && super.checkSpawnRules(world, spawnReason);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GHAST_WARN;
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.GHAST_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GHAST_DEATH;
    }
}
