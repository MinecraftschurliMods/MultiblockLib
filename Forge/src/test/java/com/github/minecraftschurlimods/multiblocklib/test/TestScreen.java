package com.github.minecraftschurlimods.multiblocklib.test;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.client.ClientMBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.client.MultiblockVisualisationRenderer;
import com.github.minecraftschurlimods.multiblocklib.api.client.MultiblockWidget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber
public class TestScreen extends Screen {
    @SubscribeEvent
    public static void keyPress(InputEvent.KeyInputEvent event) {
        if (event.getKey() == GLFW.GLFW_KEY_M && event.getAction() == GLFW.GLFW_PRESS) {
            MultiblockVisualisationRenderer renderer = MultiblockVisualisationRenderer.INSTANCE;
            if (renderer.isEnabled()) {
                if (renderer.getAnchorPos() == null) {
                    renderer.setAnchorPos(renderer.getCurrentPos());
                } else {
                    Minecraft mc = Minecraft.getInstance();
                    renderer.getMultiblock().place(mc.getSingleplayerServer().getLevel(mc.level.dimension()), renderer.getAnchorPos(), renderer.getRotation(), renderer.getMirror());
                    renderer.setAnchorPos(null);
                    renderer.setDisabled();
                }
            } else {
                renderer.setMultiblock(new ResourceLocation(MBAPI.MODID, "fluid_test"));
                renderer.setEnabled();
            }
        }
        if (event.getKey() == GLFW.GLFW_KEY_N && event.getAction() == GLFW.GLFW_PRESS) {
            Minecraft.getInstance().setScreen(new TestScreen());
        }
    }

    private MultiblockWidget multiblock;

    protected TestScreen() {
        super(Component.literal("Test"));
        multiblock = addRenderableWidget(ClientMBAPI.INSTANCE.newMultiblockWidget(0, 0, 100, 100));
        multiblock.setMultiblock(new ResourceLocation(MBAPI.MODID, "fluid_test"));
    }

    @Override
    protected void init() {
        super.init();
        multiblock = addRenderableWidget(ClientMBAPI.INSTANCE.newMultiblockWidget(width / 2 - 50, height / 2 - 50, 100, 100));
        multiblock.setMultiblock(new ResourceLocation(MBAPI.MODID, "fluid_test"));
    }

    @Override
    public void render(final PoseStack stack, final int mouseX, final int mouseY, final float partialTicks) {
        final Quaternion rot = Vector3f.XP.rotationDegrees(-30);
        rot.mul(Vector3f.YP.rotationDegrees(45));
        multiblock.setRotation(rot);
        setBlitOffset(0);
        fill(stack, width/2-50, height/2-50, width/2+50, height/2+50, 0xFFAAAA99);
        setBlitOffset(4);
        super.render(stack, mouseX, mouseY, partialTicks);
    }
}
