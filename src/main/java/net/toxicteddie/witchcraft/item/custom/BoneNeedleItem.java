package net.toxicteddie.witchcraft.item.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.toxicteddie.witchcraft.item.ModItems;
import net.toxicteddie.witchcraft.util.ModTags;

public class BoneNeedleItem extends Item {
    private static final double BEHIND_DOT_THRESHOLD = -0.5D;
    private static final int SELF_COLLECTION_DURATION = 24;
    private static final float SELF_COLLECTION_DAMAGE = 1.0F;
    private static final int SELF_PRICK_VISUAL_TICK = 7;

    public BoneNeedleItem(Properties properties) {
        super(properties);
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack needle = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND
                || !player.getOffhandItem().is(Items.GLASS_BOTTLE)) {
            return InteractionResultHolder.pass(needle);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(needle);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return SELF_COLLECTION_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {


        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        if (!(user instanceof Player player)
                || player.getUsedItemHand() != InteractionHand.MAIN_HAND
                || !player.getOffhandItem().is(Items.GLASS_BOTTLE)) {
            return;
        }

        int elapsedTicks = getUseDuration(stack) - remainingUseDuration;


        if (elapsedTicks == SELF_PRICK_VISUAL_TICK) {
            player.swing(InteractionHand.OFF_HAND, true);

            if (!level.isClientSide) {
                level.playSound(
                        null,
                        player.blockPosition(),
                        SoundEvents.TRIPWIRE_CLICK_ON,
                        SoundSource.PLAYERS,
                        0.55F,
                        1.55F
                );

                if (level instanceof ServerLevel serverLevel) {
                    Vec3 look = player.getLookAngle();
                    double particleX = player.getX() + look.x * 0.32D;
                    double particleY = player.getY() + player.getEyeHeight() - 0.42D;
                    double particleZ = player.getZ() + look.z * 0.32D;

                    serverLevel.sendParticles(
                            ParticleTypes.DAMAGE_INDICATOR,
                            particleX,
                            particleY,
                            particleZ,
                            2,
                            0.035D,
                            0.025D,
                            0.035D,
                            0.01D
                    );
                }
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack needle, Level level, LivingEntity user) {
        if (!(user instanceof Player player)
                || !player.getOffhandItem().is(Items.GLASS_BOTTLE)) {
            return needle;
        }

        if (!level.isClientSide) {
            ItemStack bloodBottle = createBloodBottle(player, player);

            if (!player.getAbilities().instabuild) {
                player.getOffhandItem().shrink(1);
            }

            if (!player.getInventory().add(bloodBottle)) {
                player.drop(bloodBottle, false);
            }

            player.hurt(level.damageSources().generic(), SELF_COLLECTION_DAMAGE);

            needle.hurtAndBreak(1, player, brokenPlayer ->
                    brokenPlayer.broadcastBreakEvent(EquipmentSlot.MAINHAND));

            level.playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.BOTTLE_FILL,
                    SoundSource.PLAYERS,
                    0.8F,
                    0.9F + level.random.nextFloat() * 0.15F
            );
        }

        return needle;
    }


    @Override
    public InteractionResult interactLivingEntity(ItemStack needle, Player player, LivingEntity target,
                                                   InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (!player.isShiftKeyDown() || !isBehindTarget(player, target)) {
            return InteractionResult.PASS;
        }

        CollectionResult result = collectBlood(needle, player, target, hand);
        return result == CollectionResult.NOT_HANDLED
                ? InteractionResult.PASS
                : InteractionResult.sidedSuccess(player.level().isClientSide);
    }


    @Override
    public boolean hurtEnemy(ItemStack needle, LivingEntity target, LivingEntity attacker) {
        if (!(attacker instanceof Player player)) {
            return super.hurtEnemy(needle, target, attacker);
        }

        collectBlood(needle, player, target, InteractionHand.MAIN_HAND);
        return true;
    }

    private static CollectionResult collectBlood(ItemStack needle, Player player, LivingEntity target,
                                                  InteractionHand hand) {
        if (target.getType().is(ModTags.EntityTypes.DEMONS)) {
            if (!player.level().isClientSide) {
                player.displayClientMessage(Component.translatable("message.witchcraft.blood.demon"), true);
            }
            return CollectionResult.BLOCKED;
        }

        int bottleSlot = findGlassBottleInHotbar(player);
        if (bottleSlot < 0) {
            if (!player.level().isClientSide) {
                player.displayClientMessage(Component.translatable("message.witchcraft.blood.no_bottle"), true);
            }
            return CollectionResult.BLOCKED;
        }

        if (player.level().isClientSide) {
            return CollectionResult.COLLECTED;
        }

        ItemStack bloodBottle = createBloodBottle(player, target);

        if (!player.getAbilities().instabuild) {
            player.getInventory().getItem(bottleSlot).shrink(1);
        }

        if (!player.getInventory().add(bloodBottle)) {
            player.drop(bloodBottle, false);
        }

        needle.hurtAndBreak(1, player, brokenPlayer -> {
            if (hand == InteractionHand.MAIN_HAND) {
                brokenPlayer.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            } else {
                brokenPlayer.broadcastBreakEvent(EquipmentSlot.OFFHAND);
            }
        });

        player.level().playSound(
                null,
                target.blockPosition(),
                SoundEvents.BOTTLE_FILL,
                SoundSource.PLAYERS,
                0.8F,
                0.9F + player.level().random.nextFloat() * 0.15F
        );
        player.swing(hand, true);

        return CollectionResult.COLLECTED;
    }

    private static boolean isBehindTarget(Player player, LivingEntity target) {
        Vec3 targetFacing = target.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        Vec3 targetToPlayer = player.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);

        if (targetFacing.lengthSqr() < 1.0E-6D || targetToPlayer.lengthSqr() < 1.0E-6D) {
            return false;
        }

        double dot = targetFacing.normalize().dot(targetToPlayer.normalize());
        return dot <= BEHIND_DOT_THRESHOLD;
    }

    private static int findGlassBottleInHotbar(Player player) {
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).is(Items.GLASS_BOTTLE)) {
                return slot;
            }
        }
        return -1;
    }

    private static ItemStack createBloodBottle(Player collector, LivingEntity target) {
        ItemStack result = new ItemStack(ModItems.BOTTLE_OF_BLOOD.get());
        CompoundTag tag = result.getOrCreateTag();

        String sourceName;
        if (target instanceof Player targetPlayer) {
            sourceName = targetPlayer.getGameProfile().getName();
        } else if (target.hasCustomName() && target.getCustomName() != null) {
            sourceName = target.getCustomName().getString();
        } else {
            sourceName = target.getName().getString();
        }

        tag.putString(BloodBottleItem.TAG_NAME, sourceName);
        tag.putString(BloodBottleItem.TAG_ENTITY_TYPE,
                BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString());
        tag.putString(BloodBottleItem.TAG_ENTITY_UUID, target.getUUID().toString());
        tag.putBoolean(BloodBottleItem.TAG_IS_PLAYER, target instanceof Player);
        tag.putFloat(BloodBottleItem.TAG_HEALTH, target.getHealth());
        tag.putFloat(BloodBottleItem.TAG_MAX_HEALTH, target.getMaxHealth());
        tag.putString(BloodBottleItem.TAG_COLLECTOR, collector.getName().getString());
        tag.putLong(BloodBottleItem.TAG_COLLECTED_AT, collector.level().getGameTime());

        return result;
    }

    private enum CollectionResult {
        COLLECTED,
        BLOCKED,
        NOT_HANDLED
    }
}
