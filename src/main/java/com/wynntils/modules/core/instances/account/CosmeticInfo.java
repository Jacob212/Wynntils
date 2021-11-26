/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.instances.account;

import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

public class CosmeticInfo extends Texture {

    boolean ears;
    boolean cape;
    boolean elytra;

    String texture;
    BufferedImage image;
    ResourceLocation location;

    public CosmeticInfo(boolean ears, boolean cape, boolean elytra, String texture, ResourceLocation location) {
        this.ears = ears;
        this.cape = cape;
        this.elytra = elytra;
        this.location = location;
        this.texture = texture;
    }

    @Override
    public int getId() {
        if (image == null) { // this is here because uploadTexture requires an opengl container
            byte[] textureBytes = Base64.getDecoder().decode(texture);
            ByteArrayInputStream stream = new ByteArrayInputStream(textureBytes);

            try {
            	//TODO BufferedImage change, doesnt work
//                image = TextureUtil.readResource(stream);
//                TextureUtil.uploadTextureImage(super.getId(), image);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            texture = null; // clean some useless space
        }

        return super.getId();
    }

    @Override
    public void load(IResourceManager resourceManager) {
        // already loaded yey
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public BufferedImage getImage() {
        return image;
    }

    public boolean hasCape() {
        return cape;
    }

    public boolean hasEars() {
        return ears;
    }

    public boolean hasElytra() {
        return elytra;
    }

}
