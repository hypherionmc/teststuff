package me.hypherionmc.craterlib.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.hypherionmc.craterlib.systems.fluid.FluidTank;
import me.hypherionmc.craterlib.util.LangUtils;
import me.hypherionmc.craterlib.util.RenderUtils;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.TextComponent;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Modified from https://github.com/SleepyTrousers/EnderIO-Rewrite/blob/dev/1.18.x/enderio-machines/src/main/java/com/enderio/machines/client/FluidStackWidget.java
 */
public class FluidStackWidget extends AbstractWidget {

    private final Screen displayOn;
    private final Supplier<FluidTank> getFluid;

    private final String toolTipTitle;

    public FluidStackWidget(Screen displayOn, Supplier<FluidTank> getFluid, int pX, int pY, int pWidth, int pHeight, String tooltipTitle) {
        super(pX, pY, pWidth, pHeight, TextComponent.EMPTY);
        this.displayOn = displayOn;
        this.getFluid = getFluid;
        this.toolTipTitle = tooltipTitle;
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        FluidTank fluidTank = getFluid.get();
        if (!fluidTank.getResource().isBlank()) {
            FluidVariant fluidStack = fluidTank.getResource();
            TextureAtlasSprite still = FluidVariantRendering.getSprite(fluidStack);
            if (still != null) {
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);

                int color = FluidVariantRendering.getColor(fluidStack);
                RenderSystem.setShaderColor(
                        RenderUtils.ARGB32.red(color) / 255.0F,
                        RenderUtils.ARGB32.green(color) / 255.0F,
                        RenderUtils.ARGB32.blue(color) / 255.0F,
                        RenderUtils.ARGB32.alpha(color) / 255.0F);
                RenderSystem.enableBlend();

                long stored = fluidTank.getAmount();
                float capacity = fluidTank.getCapacity();
                float filledVolume = stored / capacity;
                int renderableHeight = (int) (filledVolume * height);

                int atlasWidth = (int) (still.getWidth() / (still.getU1() - still.getU0()));
                int atlasHeight = (int) (still.getHeight() / (still.getV1() - still.getV0()));

                matrices.pushPose();
                matrices.translate(0, height - 16, 0);
                for (int i = 0; i < Math.ceil(renderableHeight / 16f); i++) {
                    int drawingHeight = Math.min(16, renderableHeight - 16 * i);
                    int notDrawingHeight = 16 - drawingHeight;
                    blit(matrices, x, y + notDrawingHeight, displayOn.getBlitOffset(), still.getU0() * atlasWidth, still.getV0() * atlasHeight + notDrawingHeight, this.width, drawingHeight, atlasWidth, atlasHeight);
                    matrices.translate(0, -16, 0);
                }

                RenderSystem.setShaderColor(1, 1, 1, 1);
                matrices.popPose();
            }
            renderToolTip(matrices, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
        if (this.visible && this.isFocused() && isHoveredOrFocused()) {
            displayOn.renderTooltip(poseStack, Arrays.asList(LangUtils.getTooltipTitle(toolTipTitle), new TextComponent((int) (((float) this.getFluid.get().getAmount() / this.getFluid.get().getCapacity()) * 100) + "%")), Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
}
