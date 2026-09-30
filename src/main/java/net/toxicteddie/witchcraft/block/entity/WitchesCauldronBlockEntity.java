package net.toxicteddie.witchcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import net.toxicteddie.witchcraft.block.custom.WitchesCauldronBlock;
import net.toxicteddie.witchcraft.item.ModItems;
import net.toxicteddie.witchcraft.item.custom.WandWoodType;
import net.toxicteddie.witchcraft.magic.MagicDeedManager;
import net.toxicteddie.witchcraft.magic.MagicDeedResult;
import net.toxicteddie.witchcraft.path.PlayerPathManager;
import net.toxicteddie.witchcraft.recipe.ModRecipes;
import net.toxicteddie.witchcraft.recipe.custom.CauldronRecipe;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WitchesCauldronBlockEntity extends BlockEntity {
    private static final int HEAT_TICKS_REQUIRED = 100;
    private static final int ALTAR_RANGE = 16;
    private static final int BREWER_RANGE = 10;
    private static final int MAX_INGREDIENTS = 12;
    private static final int BREWING_SHOW_TICKS = 120;
    private static final float[] WATER_HEIGHTS = {0.0F, 0.25F, 0.4375F, 0.625F};
    private static final double BUBBLE_MIN = 5.0D / 16.0D, BUBBLE_MAX = 11.0D / 16.0D;

    private int heatTicks;
    private boolean boiling;
    private int animationTicks;
    private int brewingShowTicks;
    private ItemStack pendingOutput = ItemStack.EMPTY;
    private UUID brewerUuid;
    private final List<ItemStack> mixture = new ArrayList<>();

    public WitchesCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WITCHES_CAULDRON_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WitchesCauldronBlockEntity be) {
        if (level.isClientSide) {
            if (be.animationTicks > 0) be.animationTicks--;
            if (be.brewingShowTicks > 0) be.brewingShowTicks--;
            return;
        }
        if (!state.hasProperty(WitchesCauldronBlock.LEVEL)) return;


        if (be.brewingShowTicks > 0 && level instanceof ServerLevel server) {
            be.tickBrewingShow(server);
            return;
        }
        int waterLevel = state.getValue(WitchesCauldronBlock.LEVEL);
        if (waterLevel <= 0) { be.heatTicks=0; be.setBoiling(false); if(!be.mixture.isEmpty()) be.clearMixture(); return; }
        if (isHeatSource(level.getBlockState(pos.below()))) be.heatTicks=Math.min(HEAT_TICKS_REQUIRED,be.heatTicks+1);
        else be.heatTicks=Math.max(0,be.heatTicks-2);
        be.setBoiling(be.heatTicks>=HEAT_TICKS_REQUIRED);
        if (be.boiling && level instanceof ServerLevel server) {
            spawnBoilingParticles(server,pos,waterLevel,be.getMixtureColor());
            be.absorbThrownItems(server);
        }
        be.setChanged();
    }

    private void absorbThrownItems(ServerLevel level) {
        AABB box = new AABB(worldPosition.getX()+0.2,worldPosition.getY()+0.2,worldPosition.getZ()+0.2, worldPosition.getX()+0.8,worldPosition.getY()+0.9,worldPosition.getZ()+0.8);
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, box, e -> e.isAlive() && e.getItem().getCount()>0)) {


            if (entity.getItem().is(ModItems.SALT.get())) {
                if (!mixture.isEmpty()) {
                    entity.getItem().shrink(1);
                    if (entity.getItem().isEmpty()) entity.discard();
                    cleanseMixture(level);
                } else {
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.12, 0));
                }
                break;
            }

            ServerPlayer brewer = resolveBrewer(level, entity.getOwner());
            if (brewer == null || !PlayerPathManager.isWitch(brewer)) {
                if (brewer != null) brewer.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.witchcraft.cauldron.witches_only"), true);
                entity.setDeltaMovement(entity.getDeltaMovement().add(0,0.18,0));
                continue;
            }
            ItemStack one=entity.getItem().copy(); one.setCount(1);
            if (acceptIngredient(level,brewer,one)) {
                entity.getItem().shrink(1); if(entity.getItem().isEmpty()) entity.discard();
            }
            break;
        }
    }

    @Nullable private ServerPlayer resolveBrewer(ServerLevel level, @Nullable Entity owner) {
        if (owner instanceof ServerPlayer player
                && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5)
                <= BREWER_RANGE * BREWER_RANGE) {
            return player;
        }
        return level.getNearestPlayer(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,BREWER_RANGE,false) instanceof ServerPlayer p ? p : null;
    }

    private boolean acceptIngredient(ServerLevel level, ServerPlayer brewer, ItemStack stack) {
        this.brewerUuid = brewer.getUUID();
        List<CauldronRecipe> recipes = level.getRecipeManager().getAllRecipesFor(ModRecipes.CAULDRON_TYPE.get());
        List<ItemStack> candidate = new ArrayList<>(mixture);
        candidate.add(stack);


        if (stack.is(ModItems.WOOD_ASH.get())) {
            mixture.add(stack.copy());
            animationTicks = 30;
            sendUpdate();
            playIngredientMagic(level, stack);
            finish(level, brewer, recipes);
            return true;
        }


        if (candidate.size() >= MAX_INGREDIENTS) {
            return false;
        }

        mixture.add(stack.copy());
        animationTicks = 30;
        sendUpdate();
        playIngredientMagic(level, stack);
        return true;
    }

    private void finish(ServerLevel level, ServerPlayer brewer, List<CauldronRecipe> recipes) {
        if (tryFinishWand(level, brewer)) return;

        Optional<CauldronRecipe> match=recipes.stream().filter(r->r.matchesPrefix(mixture) && r.size()==mixture.size()).findFirst();
        if(match.isEmpty()) { triggerMishap(level,brewer); clearMixture(); return; }
        CauldronRecipe recipe=match.get();


        int totalMagicCost = Math.max(0, recipe.getMagicCost())
                + Math.max(0, recipe.getAltarCost());
        MagicDeedResult payment = MagicDeedManager.perform(
                brewer,
                worldPosition,
                totalMagicCost,
                ALTAR_RANGE
        );

        if (!payment.successful()) {


            brewer.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.witchcraft.cauldron.not_enough_magic"
                    ),
                    true
            );
            triggerMishap(level, brewer);
            clearMixture();
            return;
        }
        pendingOutput = recipe.getOutput();
        brewingShowTicks = BREWING_SHOW_TICKS;
        animationTicks = BREWING_SHOW_TICKS;
        setChanged();
        sendUpdate();
        beginBrewingShow(level);
    }


    private boolean tryFinishWand(ServerLevel level, ServerPlayer brewer) {
        if (mixture.size() != 4) return false;

        ItemStack core = mixture.get(0);
        WandWoodType woodType = WandWoodType.fromCore(core);
        if (woodType == null) return false;

        if (!mixture.get(1).is(ModItems.GLIMMERSTONE_SHARD.get())
                || !mixture.get(2).is(ModItems.GLOOMSTONE_DUST.get())
                || !mixture.get(3).is(ModItems.WOOD_ASH.get())) {
            return false;
        }

        MagicDeedResult payment = MagicDeedManager.perform(
                brewer,
                worldPosition,
                200,
                ALTAR_RANGE
        );
        if (!payment.successful()) {
            brewer.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.witchcraft.cauldron.not_enough_magic"),
                    true
            );
            triggerMishap(level, brewer);
            clearMixture();
            return true;
        }

        ItemStack wand = new ItemStack(woodType.wandItem());

        pendingOutput = wand;
        brewingShowTicks = BREWING_SHOW_TICKS;
        animationTicks = BREWING_SHOW_TICKS;
        setChanged();
        sendUpdate();
        beginBrewingShow(level);
        return true;
    }


    private void beginBrewingShow(ServerLevel level) {
        double x = worldPosition.getX() + .5;
        double y = worldPosition.getY() + .76;
        double z = worldPosition.getZ() + .5;


        level.sendParticles(ParticleTypes.BUBBLE, x, y - .03, z,
                7, .17, .015, .17, .025);
        level.sendParticles(ParticleTypes.END_ROD, x, y + .03, z,
                2, .10, .04, .10, .006);
        level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW,
                SoundSource.BLOCKS, .28f, .58f);
        level.playSound(null, worldPosition, SoundEvents.AMETHYST_CLUSTER_HIT,
                SoundSource.BLOCKS, .18f, .70f);
    }

    private void tickBrewingShow(ServerLevel level) {
        brewingShowTicks--;
        animationTicks = brewingShowTicks;

        double x = worldPosition.getX() + .5;
        double y = worldPosition.getY() + .77;
        double z = worldPosition.getZ() + .5;


        if (brewingShowTicks == 94) {
            level.sendParticles(ParticleTypes.BUBBLE, x, y - .02, z,
                    5, .18, .01, .18, .020);
            level.playSound(null, worldPosition, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE,
                    SoundSource.BLOCKS, .18f, .76f);
        } else if (brewingShowTicks == 62) {
            level.sendParticles(ParticleTypes.END_ROD, x, y + .12, z,
                    2, .10, .08, .10, .006);
            level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.BLOCKS, .16f, .68f);
        } else if (brewingShowTicks == 30) {
            level.sendParticles(ParticleTypes.BUBBLE, x, y - .015, z,
                    8, .14, .012, .14, .028);
            level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW,
                    SoundSource.BLOCKS, .24f, .74f);
        } else if (brewingShowTicks == 14) {

            spawnColoredWhorl(level, x, y + .01, z, getMixtureColor(),
                    8, .19, .010);
            level.playSound(null, worldPosition, SoundEvents.SNOW_BREAK,
                    SoundSource.BLOCKS, .18f, .88f);
        }

        if (brewingShowTicks <= 0) {
            completeBrew(level);
        } else {
            setChanged();
        }
    }

    private void completeBrew(ServerLevel level) {
        if (!pendingOutput.isEmpty()) {
            double centerX = worldPosition.getX() + .5;
            double centerZ = worldPosition.getZ() + .5;
            double pushX = .16;
            double pushZ = 0.0;


            ServerPlayer nearestPlayer = (ServerPlayer) level.getNearestPlayer(
                    centerX, worldPosition.getY() + 1.0, centerZ, 8.0, false);
            if (nearestPlayer != null) {
                double dx = nearestPlayer.getX() - centerX;
                double dz = nearestPlayer.getZ() - centerZ;
                double length = Math.sqrt(dx * dx + dz * dz);
                if (length > 0.001) {
                    pushX = dx / length * .16;
                    pushZ = dz / length * .16;
                }
            }

            ItemEntity result = new ItemEntity(level,
                    centerX,
                    worldPosition.getY() + 1.22,
                    centerZ,
                    pendingOutput.copy());
            result.setDeltaMovement(pushX, .20, pushZ);
            result.setPickUpDelay(10);
            level.addFreshEntity(result);
        }

        int water = getBlockState().getValue(WitchesCauldronBlock.LEVEL);
        level.setBlock(worldPosition,
                getBlockState().setValue(WitchesCauldronBlock.LEVEL, Math.max(0, water - 1)), 3);

        successEffects(level);
        clearMixture();
    }

    private void playIngredientMagic(ServerLevel level, ItemStack stack) {
        int color = colorFor(stack);
        double x = worldPosition.getX() + .5;
        double y = worldPosition.getY() + .70;
        double z = worldPosition.getZ() + .5;


        spawnColoredWhorl(level, x, y + .10, z, color, 10, .24, -.020);
        level.sendParticles(ParticleTypes.BUBBLE,
                x, y - .025, z, 4, .12, .012, .12, .020);
        level.sendParticles(ParticleTypes.END_ROD,
                x, y + .03, z, 2, .09, .035, .09, .010);

        level.playSound(null, worldPosition, SoundEvents.BOTTLE_EMPTY,
                SoundSource.BLOCKS, .32f, .68f + level.random.nextFloat() * .08f);
        level.playSound(null, worldPosition, SoundEvents.AMETHYST_CLUSTER_HIT,
                SoundSource.BLOCKS, .26f, .72f + level.random.nextFloat() * .10f);
        level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW,
                SoundSource.BLOCKS, .10f, .68f);
    }

    private void cleanseMixture(ServerLevel level) {
        clearMixture();
        double x = worldPosition.getX() + .5;
        double y = worldPosition.getY() + .72;
        double z = worldPosition.getZ() + .5;


        for (int i = 0; i < 10; i++) {
            double angle = (Math.PI * 2.0 * i) / 10.0;
            double radius = .27;
            double px = x + Math.cos(angle) * radius;
            double pz = z + Math.sin(angle) * radius;
            level.sendParticles(ParticleTypes.POOF, px, y + .02, pz,
                    1, .015, .015, .015, .003);
        }
        level.sendParticles(ParticleTypes.END_ROD, x, y + .08, z,
                3, .08, .09, .08, .009);
        level.playSound(null, worldPosition, SoundEvents.SNOW_BREAK,
                SoundSource.BLOCKS, .40f, .92f);
        level.playSound(null, worldPosition, SoundEvents.CANDLE_EXTINGUISH,
                SoundSource.BLOCKS, .20f, .78f);
        level.playSound(null, worldPosition, SoundEvents.AMETHYST_CLUSTER_HIT,
                SoundSource.BLOCKS, .18f, 1.12f);
    }

    private void successEffects(ServerLevel level) {
        double x = worldPosition.getX() + .5;
        double y = worldPosition.getY() + .78;
        double z = worldPosition.getZ() + .5;
        int color = getMixtureColor();


        spawnColoredWhorl(level, x, y + .02, z, color, 16, .30, .018);
        level.sendParticles(ParticleTypes.POOF, x, y + .03, z,
                6, .15, .055, .15, .010);
        level.sendParticles(ParticleTypes.END_ROD, x, y + .12, z,
                5, .13, .18, .13, .018);
        level.sendParticles(ParticleTypes.ASH, x, y + .10, z,
                3, .13, .10, .13, .006);

        level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW,
                SoundSource.BLOCKS, .34f, .68f);
        level.playSound(null, worldPosition, SoundEvents.AMETHYST_CLUSTER_HIT,
                SoundSource.BLOCKS, .30f, .62f);
        level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS, .20f, 1.02f);
        level.playSound(null, worldPosition, SoundEvents.SNOW_BREAK,
                SoundSource.BLOCKS, .25f, .92f);
    }

    private static void spawnColoredWhorl(ServerLevel level, double x, double y, double z,
                                          int color, int points, double radius, double verticalSpeed) {
        float r = ((color >> 16) & 255) / 255f;
        float g = ((color >> 8) & 255) / 255f;
        float b = (color & 255) / 255f;
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(r, g, b), .68f);

        double phase = level.random.nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < points; i++) {
            double progress = points <= 1 ? 0.0 : (double) i / (points - 1);
            double angle = phase + progress * Math.PI * 2.5;
            double currentRadius = radius * (1.0 - progress * .55);
            double px = x + Math.cos(angle) * currentRadius;
            double pz = z + Math.sin(angle) * currentRadius;
            double py = y + progress * .18;

            double vx = -Math.cos(angle) * .004;
            double vz = -Math.sin(angle) * .004;
            level.sendParticles(dust, px, py, pz, 1, vx, verticalSpeed, vz, 0.0);
        }
    }

    private void triggerMishap(ServerLevel level, ServerPlayer brewer) {
        int roll=level.random.nextInt(5);
        switch(roll){
            case 0 -> brewer.addEffect(new MobEffectInstance(MobEffects.CONFUSION,200,0));
            case 1 -> brewer.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,80,0));
            case 2 -> brewer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,160,1));
            case 3 -> { brewer.setSecondsOnFire(3); level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,brewer.getX(),brewer.getY()+1,brewer.getZ(),25,.3,.5,.3,.05); }
            default -> level.explode(null,worldPosition.getX()+.5,worldPosition.getY()+.6,worldPosition.getZ()+.5,1.25f,Level.ExplosionInteraction.NONE);
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                worldPosition.getX()+.5, worldPosition.getY()+.7, worldPosition.getZ()+.5,
                12, .24, .18, .24, .025);
        level.sendParticles(ParticleTypes.ASH,
                worldPosition.getX()+.5, worldPosition.getY()+.82, worldPosition.getZ()+.5,
                7, .20, .14, .20, .012);
        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS, .50f, .62f);
        level.playSound(null, worldPosition, SoundEvents.DEEPSLATE_BREAK,
                SoundSource.BLOCKS, .32f, .72f);
    }

    private static boolean isHeatSource(BlockState state) {
        if(state.getFluidState().is(FluidTags.LAVA)||state.is(Blocks.MAGMA_BLOCK)||state.is(Blocks.FIRE)||state.is(Blocks.SOUL_FIRE)) return true;
        if(state.getBlock() instanceof CampfireBlock && state.hasProperty(BlockStateProperties.LIT)) return state.getValue(BlockStateProperties.LIT);
        return (state.is(Blocks.FURNACE)||state.is(Blocks.BLAST_FURNACE)||state.is(Blocks.SMOKER))&&state.hasProperty(BlockStateProperties.LIT)&&state.getValue(BlockStateProperties.LIT);
    }
    private static void spawnBoilingParticles(ServerLevel level,BlockPos pos,int waterLevel,int color){
        RandomSource random=level.random; double y=pos.getY()+WATER_HEIGHTS[waterLevel]+.015;
        for(int i=0;i<2+random.nextInt(2);i++){double x=pos.getX()+BUBBLE_MIN+random.nextDouble()*(BUBBLE_MAX-BUBBLE_MIN),z=pos.getZ()+BUBBLE_MIN+random.nextDouble()*(BUBBLE_MAX-BUBBLE_MIN); level.sendParticles(ParticleTypes.BUBBLE,x,y,z,1,.015,.005,.015,.035); if(random.nextFloat()<.35f)level.sendParticles(ParticleTypes.SPLASH,x,y+.015,z,1,.015,.005,.015,.012);}
        if(random.nextFloat()<.06f)level.playSound(null,pos,SoundEvents.BUBBLE_COLUMN_BUBBLE_POP,SoundSource.BLOCKS,.22f,.9f+random.nextFloat()*.25f);
    }
    private static int colorFor(ItemStack stack){ ResourceLocation id=ForgeRegistries.ITEMS.getKey(stack.getItem()); int h=id==null?0x3F76E4:id.toString().hashCode(); return 0x303030 | (h & 0xCFCFCF); }
    public int getMixtureColor(){ if(mixture.isEmpty())return 0x3F76E4; long r=0,g=0,b=0; for(ItemStack s:mixture){int c=colorFor(s);r+=(c>>16)&255;g+=(c>>8)&255;b+=c&255;}int n=mixture.size();return ((int)(r/n)<<16)|((int)(g/n)<<8)|(int)(b/n); }
    public List<ItemStack> getMixture(){return List.copyOf(mixture);}
    public int getAnimationTicks(){return animationTicks;}
    public int getBrewingShowTicks(){return brewingShowTicks;}
    public int getBrewingShowDuration(){return BREWING_SHOW_TICKS;}
    public ItemStack getPendingOutput(){return pendingOutput.copy();}
    public boolean isBrewingShowActive(){return brewingShowTicks > 0;}
    private void clearMixture(){mixture.clear();brewerUuid=null;animationTicks=0;brewingShowTicks=0;pendingOutput=ItemStack.EMPTY;setChanged();sendUpdate();}
    private void setBoiling(boolean v){if(boiling!=v){boiling=v;setChanged();sendUpdate();}} public boolean isBoiling(){return boiling;}
    private void sendUpdate(){if(level!=null){BlockState s=getBlockState();level.sendBlockUpdated(worldPosition,s,s,3);}}

    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.putInt("HeatTicks",heatTicks);tag.putBoolean("Boiling",boiling);tag.putInt("AnimationTicks",animationTicks);tag.putInt("BrewingShowTicks",brewingShowTicks);if(!pendingOutput.isEmpty()){CompoundTag outputTag=new CompoundTag();pendingOutput.save(outputTag);tag.put("PendingOutput",outputTag);}if(brewerUuid!=null)tag.putUUID("Brewer",brewerUuid);ListTag list=new ListTag();for(ItemStack s:mixture){CompoundTag t=new CompoundTag();s.save(t);list.add(t);}tag.put("Mixture",list);}
    @Override public void load(CompoundTag tag){super.load(tag);heatTicks=tag.getInt("HeatTicks");boiling=tag.getBoolean("Boiling");animationTicks=tag.getInt("AnimationTicks");brewingShowTicks=tag.getInt("BrewingShowTicks");pendingOutput=tag.contains("PendingOutput",10)?ItemStack.of(tag.getCompound("PendingOutput")):ItemStack.EMPTY;brewerUuid=tag.hasUUID("Brewer")?tag.getUUID("Brewer"):null;mixture.clear();ListTag list=tag.getList("Mixture",10);for(int i=0;i<list.size();i++)mixture.add(ItemStack.of(list.getCompound(i)));}
    @Override public CompoundTag getUpdateTag(){return saveWithoutMetadata();}
    @Override public void handleUpdateTag(CompoundTag tag){load(tag);}
    @Override public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public void onDataPacket(Connection net,ClientboundBlockEntityDataPacket pkt){CompoundTag tag=pkt.getTag();if(tag!=null)load(tag);}
}
