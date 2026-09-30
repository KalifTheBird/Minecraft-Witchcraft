package net.toxicteddie.witchcraft.particle.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;


public final class RitualFlameParticle extends TextureSheetParticle {
    private static final int FRAME_COUNT = 10;
    private static final float FRAME_SPEED = 0.25F;
    private static final float FULL_WIDTH = 0.75F;
    private static final float FULL_HEIGHT = 1.50F;

    private final SpriteSet sprites;
    private final float visualWidth;
    private final float visualHeight;
    private final int frameOffset;

    private RitualFlameParticle(
            ClientLevel level,
            double x, double y, double z,
            float progress,
            int frameOffset,
            SpriteSet sprites
    ) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
        this.lifetime = 3;

        float scale = Mth.clamp(progress, 0.0F, 1.0F);
        this.visualWidth = FULL_WIDTH * scale;
        this.visualHeight = FULL_HEIGHT * scale;
        this.frameOffset = Math.floorMod(frameOffset, FRAME_COUNT);
        updateSprite();
    }

    private void updateSprite() {
        int frame = Math.floorMod(
                Mth.floor(level.getGameTime() * FRAME_SPEED) + frameOffset,
                FRAME_COUNT
        );
        setSprite(sprites.get(frame, FRAME_COUNT - 1));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        updateSprite();
        if (++this.age >= this.lifetime) remove();
    }

    @Override
    public ParticleRenderType getRenderType() {

        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        if (visualWidth <= 0.001F || visualHeight <= 0.001F) return;

        Vec3 cameraPos = camera.getPosition();
        float px = (float) (Mth.lerp(partialTick, xo, x) - cameraPos.x());
        float py = (float) (Mth.lerp(partialTick, yo, y) - cameraPos.y());
        float pz = (float) (Mth.lerp(partialTick, zo, z) - cameraPos.z());

        Quaternionf rotation = new Quaternionf(camera.rotation());
        float halfWidth = visualWidth * 0.5F;
        Vector3f[] corners = {
                new Vector3f(-halfWidth, 0.0F, 0.0F),
                new Vector3f(-halfWidth, visualHeight, 0.0F),
                new Vector3f( halfWidth, visualHeight, 0.0F),
                new Vector3f( halfWidth, 0.0F, 0.0F)
        };
        for (Vector3f corner : corners) {
            corner.rotate(rotation);
            corner.add(px, py, pz);
        }

        float u0 = getU0();
        float u1 = getU1();
        float v0 = getV0();
        float v1 = getV1();
        int light = getLightColor(partialTick);

        vertex(consumer, corners[0], u1, v1, light);
        vertex(consumer, corners[1], u1, v0, light);
        vertex(consumer, corners[2], u0, v0, light);
        vertex(consumer, corners[3], u0, v1, light);
    }

    private static void vertex(VertexConsumer consumer, Vector3f point, float u, float v, int light) {
        consumer.vertex(point.x(), point.y(), point.z())
                .uv(u, v)
                .color(255, 255, 255, 255)
                .uv2(light)
                .endVertex();
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed
        ) {
            return new RitualFlameParticle(
                    level, x, y, z,
                    (float) xSpeed,
                    Mth.floor(ySpeed),
                    sprites
            );
        }
    }
}
