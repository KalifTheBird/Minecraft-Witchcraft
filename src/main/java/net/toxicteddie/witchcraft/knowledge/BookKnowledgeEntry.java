package net.toxicteddie.witchcraft.knowledge;

import net.minecraft.resources.ResourceLocation;

import java.util.List;


public record BookKnowledgeEntry(
        ResourceLocation id,
        String chapter,
        String title,
        String type,
        boolean secret,
        String summary,
        List<String> pages,
        List<String> prerequisites
) {}
