package mchorse.metamorph.client;

import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.EntityMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import mchorse.metamorph.client.gui.elements.GuiOverlay;
import mchorse.metamorph.client.gui.elements.GuiSurvivalMorphs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Rendering handler
 *
 * This handler is rendering handler which is responsible for few things:
 * 
 * - Overlays (survival morph menu, morph acquiring)
 * - Player model
 */
@SideOnly(Side.CLIENT)
public class RenderingHandler
{
    private GuiSurvivalMorphs overlay;
    private GuiOverlay morphOverlay;
    private RenderManager manager;

    public RenderingHandler(GuiSurvivalMorphs overlay, GuiOverlay morphOverlay)
    {
        this.overlay = overlay;
        this.morphOverlay = morphOverlay;
        this.manager = Minecraft.getMinecraft().getRenderManager();
    }

    /**
     * Draw HUD additions 
     */
    @SubscribeEvent
    public void onHUDRender(RenderGameOverlayEvent.Post event)
    {
        ScaledResolution resolution = event.getResolution();

        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL)
        {
            if (this.overlay.inGUI == false)
            {
                this.overlay.render(resolution.getScaledWidth(), resolution.getScaledHeight());
            }

            this.morphOverlay.render(resolution.getScaledWidth(), resolution.getScaledHeight());
        }
    }

    /**
     * Render player hook
     * 
     * This method is responsible for rendering player, in case if he's morphed, 
     * into morphed entity. This method is also responsible for down scaling
     * oversized entities in inventory GUIs.
     * 
     * I wish devs at Mojang scissored the inventory area where those the 
     * player model is rendered. 
     */
    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Pre event)
    {
        EntityPlayer player = event.getEntityPlayer();
        IMorphing capability = Morphing.get(player);

        /* No morph, no problem */
        if (capability == null || !capability.isMorphed())
        {
            return;
        }

        AbstractMorph morph = capability.getCurrentMorph();

        event.setCanceled(true);

        /* Render the morph itself */
        morph.render(player, event.getX(), event.getY(), event.getZ(), player.rotationYaw, event.getPartialRenderTick());
    }

    /**
     * On name render, simply render the name of the user, instead of the name of 
     * the entity.  
     */
    @SubscribeEvent
    public void onNameRender(RenderLivingEvent.Specials.Pre<EntityLivingBase> event)
    {
        EntityLivingBase render = EntityMorph.renderEntity;

        if (render == null)
        {
            return;
        }

        event.setCanceled(true);

        EntityLivingBase entity = event.getEntity();
        boolean canRenderName = Minecraft.isGuiEnabled() && render != this.manager.renderViewEntity && !entity.isBeingRidden();

        if (!canRenderName)
        {
            return;
        }

        double dist = entity.getDistanceSqToEntity(this.manager.renderViewEntity);
        float factor = entity.isSneaking() ? 32.0F : 64.0F;

        if (dist < (double) (factor * factor))
        {
            GlStateManager.alphaFunc(516, 0.1F);
            this.renderEntityName(entity, render.getDisplayName().getFormattedText(), event.getX(), event.getY(), event.getZ());
        }
    }

    /**
     * Renders an entity's name above its head (copied and modified from 
     * {@link RenderLivingBase})
     */
    protected void renderEntityName(EntityLivingBase entity, String name, double x, double y, double z)
    {
        if (name.isEmpty())
        {
            return;
        }

        int maxDistance = 64;
        double dist = entity.getDistanceSqToEntity(this.manager.renderViewEntity);

        if (dist <= (double) (maxDistance * maxDistance))
        {
            this.renderNameplate(entity, name, x, y, z, maxDistance);
        }
    }

    /**
     * Copied from {@link Render} class. In 1.10.2 it is much better extracted 
     * method in {@link EntityRenderer}. *facepalm* 
     */
    protected void renderNameplate(EntityLivingBase entityIn, String str, double x, double y, double z, int maxDistance)
    {
        boolean flag = entityIn.isSneaking();
        GlStateManager.pushMatrix();
        float f = flag ? 0.25F : 0.0F;
        GlStateManager.translate((float) x, (float) y + entityIn.height + 0.5F - f, (float) z);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-this.manager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) (this.manager.options.thirdPersonView == 2 ? -1 : 1) * this.manager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        if (!flag)
        {
            GlStateManager.disableDepth();
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        int i = str.equals("deadmau5") ? -10 : 0;
        FontRenderer fontrenderer = this.manager.getFontRenderer();
        int j = fontrenderer.getStringWidth(str) / 2;
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos((double) (-j - 1), (double) (-1 + i), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        vertexbuffer.pos((double) (-j - 1), (double) (8 + i), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        vertexbuffer.pos((double) (j + 1), (double) (8 + i), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        vertexbuffer.pos((double) (j + 1), (double) (-1 + i), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();

        if (!flag)
        {
            fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, 553648127);
            GlStateManager.enableDepth();
        }

        GlStateManager.depthMask(true);
        fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, flag ? 553648127 : -1);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}