package com.plusls.MasaGadget.mixin.tweakeroo.inventoryPreviewSyncData;

import com.plusls.MasaGadget.ModInfo;
import com.plusls.MasaGadget.config.Configs;
import com.plusls.MasaGadget.util.PcaSyncProtocol;
import com.plusls.MasaGadget.util.TraceUtil;
import fi.dy.masa.tweakeroo.renderer.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.hendrixshen.magiclib.dependency.annotation.Dependencies;
import top.hendrixshen.magiclib.dependency.annotation.Dependency;

import java.util.Objects;


@Dependencies(and = @Dependency(ModInfo.TWEAKEROO_MOD_ID))
@Mixin(value = RenderUtils.class, remap = false)
public abstract class MixinRenderUtils {

    @ModifyVariable(method = "renderInventoryOverlay",
            at = @At(value = "HEAD"), argsOnly = true, ordinal = 0)
    private static Minecraft preOnRenderInventoryOverlay(Minecraft mc) {
        if (!Configs.inventoryPreviewSyncData || !PcaSyncProtocol.enable || mc.hasSingleplayerServer()) {
            return mc;
        }
        Level world = Objects.requireNonNull(mc.level);
        HitResult hitResult = TraceUtil.getTraceResult();
        if (hitResult == null) {
            return mc;
        }
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
            BlockEntity blockEntity = world.getChunkAt(pos).getBlockEntity(pos);
            if (blockEntity instanceof AbstractFurnaceBlockEntity ||
                    blockEntity instanceof DispenserBlockEntity ||
                    blockEntity instanceof HopperBlockEntity ||
                    blockEntity instanceof ShulkerBoxBlockEntity ||
                    blockEntity instanceof BarrelBlockEntity ||
                    blockEntity instanceof BrewingStandBlockEntity ||
                    blockEntity instanceof ChestBlockEntity ||
                    (blockEntity instanceof ComparatorBlockEntity && Configs.inventoryPreviewSupportComparator) ||
                    //#if MC > 11404
                    (blockEntity instanceof BeehiveBlockEntity && Configs.pcaSyncProtocolSyncBeehive)
                //#else
                //$$ true
                //#endif
            ) {
                PcaSyncProtocol.syncBlockEntity(pos);
            }
        } else if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hitResult).getEntity();
            if (entity instanceof Container ||
                    entity instanceof AbstractVillager ||
                    entity instanceof AbstractHorse ||
                    entity instanceof Player) {
                PcaSyncProtocol.syncEntity(entity.getId());
            }
        }
        return mc;
    }
}
