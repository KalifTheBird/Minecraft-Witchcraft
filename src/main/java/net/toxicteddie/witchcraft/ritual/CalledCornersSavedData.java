package net.toxicteddie.witchcraft.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public final class CalledCornersSavedData extends SavedData {
    public static final String DATA_NAME = "witchcraft_called_corners";
    public static final int DEFAULT_RADIUS = 32;
    public static final long DEFAULT_DURATION = 24_000L;

    private final List<Area> areas = new ArrayList<>();

    public static CalledCornersSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                CalledCornersSavedData::load,
                CalledCornersSavedData::new,
                DATA_NAME
        );
    }

    public static CalledCornersSavedData load(CompoundTag tag) {
        CalledCornersSavedData data = new CalledCornersSavedData();
        ListTag list = tag.getList("Areas", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            data.areas.add(new Area(
                    BlockPos.of(entry.getLong("Center")),
                    entry.getInt("Radius"),
                    entry.getLong("Expires")
            ));
        }
        return data;
    }

    public void call(ServerLevel level, BlockPos center) {
        cleanup(level.getGameTime());
        uncallNear(center, 4);
        areas.add(new Area(center.immutable(), DEFAULT_RADIUS,
                level.getGameTime() + DEFAULT_DURATION));
        setDirty();
    }

    public boolean uncallNear(BlockPos center, int tolerance) {
        long max = (long) tolerance * tolerance;
        boolean removed = areas.removeIf(area -> area.center().distSqr(center) <= max);
        if (removed) setDirty();
        return removed;
    }

    public boolean hasCenterNear(ServerLevel level, BlockPos center, int tolerance) {
        cleanup(level.getGameTime());
        long max = (long) tolerance * tolerance;
        return areas.stream().anyMatch(area -> area.center().distSqr(center) <= max);
    }

    public boolean isInside(ServerLevel level, BlockPos pos) {
        cleanup(level.getGameTime());
        for (Area area : areas) {
            if (area.center().distSqr(pos) <= (long) area.radius() * area.radius()) return true;
        }
        return false;
    }

    private void cleanup(long gameTime) {
        Iterator<Area> iterator = areas.iterator();
        boolean changed = false;
        while (iterator.hasNext()) {
            if (iterator.next().expiresAt() <= gameTime) {
                iterator.remove();
                changed = true;
            }
        }
        if (changed) setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Area area : areas) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Center", area.center().asLong());
            entry.putInt("Radius", area.radius());
            entry.putLong("Expires", area.expiresAt());
            list.add(entry);
        }
        tag.put("Areas", list);
        return tag;
    }

    private record Area(BlockPos center, int radius, long expiresAt) {}
}
