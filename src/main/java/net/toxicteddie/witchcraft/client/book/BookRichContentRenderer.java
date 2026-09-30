package net.toxicteddie.witchcraft.client.book;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.toxicteddie.witchcraft.entity.ModEntities;
import net.toxicteddie.witchcraft.recipe.ModRecipes;
import net.toxicteddie.witchcraft.recipe.custom.WitchesOvenRecipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class BookRichContentRenderer {
    private static final Map<String, Entity> PREVIEW_ENTITIES = new HashMap<>();
    private BookRichContentRenderer() {}

    public static boolean hasHero(PhysicalBookContent.Page page) {
        return itemFor(page) != null || isCreature(page);
    }

    public static void renderHero(PhysicalBookContent.Page page, PoseStack pose, MultiBufferSource buffer, int light) {
        if (isCreature(page)) { renderCreature(page, pose, buffer, light); return; }
        ItemStack stack = itemFor(page);
        if (stack != null && !stack.isEmpty()) renderItem(stack, 43F, 31F, 12F, pose, buffer, light);
    }

    public static boolean renderRecipe(PhysicalBookContent.Page page, PoseStack pose, MultiBufferSource buffer, int light) {
        if (!"recipe".equalsIgnoreCase(page.type())) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;
        ItemStack wanted = itemFor(page);
        if (wanted == null || wanted.isEmpty()) return false;
        List<WitchesOvenRecipe> recipes = mc.level.getRecipeManager().getAllRecipesFor(ModRecipes.WITCHES_OVEN_TYPE.get());
        WitchesOvenRecipe found = recipes.stream().filter(r -> ItemStack.isSameItem(r.getOutput(), wanted)).findFirst().orElse(null);
        if (found == null) return false;

        Ingredient[] ingredients = {found.getBottle(), found.getReagent()};
        float x = 16F;
        for (Ingredient ingredient : ingredients) {
            ItemStack[] choices = ingredient.getItems();
            if (choices.length > 0) renderItem(choices[0], x, 48F, 8F, pose, buffer, light);
            x += 22F;
        }
        renderArrow(pose, buffer, light);
        renderItem(found.getOutput(), 67F, 48F, 9F, pose, buffer, light);
        return true;
    }

    private static void renderArrow(PoseStack pose, MultiBufferSource buffer, int light) {

        PhysicalBookPageRenderer.drawBookTexture(new ResourceLocation("witchcraft", "textures/gui/book/forward.png"),
                51F, 44F, 8F, 8F, 190, 190, 190, 255, pose, buffer, light);
    }

    private static void renderItem(ItemStack stack, float x, float y, float size, PoseStack pose, MultiBufferSource buffer, int light) {
        pose.pushPose();
        pose.translate(x, y, -0.08F);
        pose.scale(size, -size, size);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GUI, light,
                OverlayTexture.NO_OVERLAY, pose, buffer, Minecraft.getInstance().level, 0);
        pose.popPose();
    }

    private static boolean isCreature(PhysicalBookContent.Page page) {
        return "creature".equalsIgnoreCase(page.type()) || page.id().contains("entities/");
    }

    private static void renderCreature(PhysicalBookContent.Page page, PoseStack pose, MultiBufferSource buffer, int light) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity entity = PREVIEW_ENTITIES.computeIfAbsent(page.id(), id -> {
            if (id.endsWith("/demons")) return ModEntities.DEMON.get().create(mc.level);
            if (id.endsWith("/imps")) return ModEntities.IMP.get().create(mc.level);
            if (id.endsWith("/mandrake")) return ModEntities.MANDRAKE.get().create(mc.level);
            return null;
        });
        if (entity == null) return;
        float spin = (mc.level.getGameTime() + mc.getFrameTime()) * 0.25F + BookCreaturePreviewState.rotation(page.id());
        pose.pushPose();
        pose.translate(43F, 57F, -0.12F);
        float scale = page.id().endsWith("/demons") ? 7.0F : 10.0F;
        pose.scale(scale, -scale, scale);
        pose.mulPose(Axis.YP.rotationDegrees(spin));
        mc.getEntityRenderDispatcher().render(entity, 0, 0, 0, 0, mc.getFrameTime(), pose, buffer, light);
        pose.popPose();
    }

    private static ItemStack itemFor(PhysicalBookContent.Page page) {
        String item = switch (page.id()) {
            case "witchcraft:natural_world/belladonna" -> "witchcraft:belladonna_flower";
            case "witchcraft:natural_world/hemlock" -> "witchcraft:hemlock_flower";
            case "witchcraft:natural_world/mandrake" -> "witchcraft:mandrake_root";
            case "witchcraft:natural_world/rowan" -> "witchcraft:rowan_berries";
            case "witchcraft:natural_world/elder" -> "witchcraft:elder_berries";
            case "witchcraft:brews/dark_unction" -> "witchcraft:dark_unction";
            case "witchcraft:brews/milk_of_the_mother" -> "witchcraft:milk_of_the_mother";
            case "witchcraft:brews/pale_moonlight" -> "witchcraft:pale_moonlight";
            case "witchcraft:items/wands" -> "witchcraft:rowan_wand";
            default -> null;
        };
        if (item == null) return null;
        var value = ForgeRegistries.ITEMS.getValue(new ResourceLocation(item));
        return value == null ? null : new ItemStack(value);
    }
}
