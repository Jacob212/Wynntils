/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.map.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.wynntils.McIf;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.core.framework.rendering.colors.CustomColor;
import com.wynntils.core.framework.rendering.textures.Texture;
import com.wynntils.core.utils.objects.Location;
import com.wynntils.core.utils.objects.Pair;
import com.wynntils.modules.map.instances.LootRunPath;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import com.wynntils.transition.GlStateManager;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

public class PointRenderer {

    public static void drawTexturedLines(Texture texture, Long2ObjectMap<List<List<LootRunPath.LootRunPathLocation>>> points, Long2ObjectMap<List<List<Vector3d>>> directions, CustomColor color, float width) {
        List<ChunkPos> chunks = new ArrayList<>();
        int renderDistance = McIf.mc().options.renderDistance;
        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int z = -renderDistance; z <= renderDistance; z++) {
                int playerChunkX = McIf.player().xChunk;
                int playerChunkZ = McIf.player().zChunk;
                ChunkPos chunk = new ChunkPos(x + playerChunkX, z + playerChunkZ);
                chunks.add(chunk);
            }
        }

        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        texture.bind();

        for (ChunkPos chunk : chunks) {
            if (!McIf.world().hasChunk(chunk.x, chunk.z)) {
                continue;
            }
            List<List<LootRunPath.LootRunPathLocation>> pointsInChunk = points.get(ChunkPos.asLong(chunk.x, chunk.z));
            List<List<Vector3d>> directionsInChunk = directions.get(ChunkPos.asLong(chunk.x, chunk.z));
            if (pointsInChunk != null) {
                for (int i = 0; i < pointsInChunk.size(); ++i) {
                    List<LootRunPath.LootRunPathLocation> pointsInRoute = pointsInChunk.get(i);
                    List<Vector3d> directionsInRoute = directionsInChunk.get(i);
                    boolean disable = false;
                    List<Pair<LootRunPath.LootRunPathLocation, Vector3d>> toRender = new ArrayList<>();
                    for (int k = 0; k < pointsInRoute.size(); ++k) {
                        Point3d start = new Point3d(pointsInRoute.get(k).getLocation());
                        World world = McIf.world();
                        BlockPos minPos = new BlockPos(start.x - 0.3D, start.y - 1D, start.z - 0.3D);
                        BlockPos maxPos = new BlockPos(start.x + 0.3D, start.y - 1D, start.z + 0.3D);
                        Iterable<BlockPos> blocks = BlockPos.getAllInBox(minPos, maxPos);
                        boolean barrier = false;
                        boolean validBlock = false;
                        for (BlockPos blockInArea : blocks) {
                            BlockState blockStateInArea = world.getBlockState(blockInArea);
                            if (blockStateInArea.getBlock() == Blocks.BARRIER) {
                                barrier = true;
                            } else if (blockStateInArea.getCollisionShape(world, blockInArea) != null) {
                                validBlock = true;
                            }

                        }

                        if (validBlock) {
                            disable = false;
                            for (Pair<LootRunPath.LootRunPathLocation, Vector3d> render : toRender) {
                                Point3d startRender = new Point3d(render.a.getLocation());
                                Vector3d direction = new Vector3d(render.b);
                                Point3d end = new Point3d(render.a.getLocation());
                                CustomColor locationColor = color == CommonColors.RAINBOW ? render.a.getColor() : color;
                                locationColor.applyColor();

                                startRender.y -= .24;

                                direction.normalize();
                                end.add(direction);
                                end.y -= .24;

                                drawTexturedLine(startRender, end, width);
                            }
                            toRender.clear();
                        } else if (barrier) {
                            disable = true;
                            toRender.clear();
                            continue;
                        } else if (disable) {
                            continue;
                        } else {
                            toRender.add(new Pair<>(pointsInRoute.get(k), directionsInRoute.get(k)));
                            continue;
                        }

                        Vector3d direction = new Vector3d(directionsInRoute.get(k));
                        Point3d end = new Point3d(pointsInRoute.get(k).getLocation());
                        CustomColor locationColor = color == CommonColors.RAINBOW ? pointsInRoute.get(k).getColor() : color;
                        locationColor.applyColor();

                        start.y -= .24;

                        direction.normalize();
                        end.add(direction);
                        end.y -= .24;

                        BlockPos startBlockPos = new BlockPos(start.x, start.y, start.z);
                        net.minecraft.util.math.vector.Vector3d startVec = new net.minecraft.util.math.vector.Vector3d(start.x, start.y, start.z);

                        BlockPos endBlockPos = new BlockPos(end.x, end.y, end.z);
                        net.minecraft.util.math.vector.Vector3d endVec = new net.minecraft.util.math.vector.Vector3d(end.x, end.y, end.z);

                        AxisAlignedBB startCollisionBox = world.getBlockState(startBlockPos).getCollisionShape(world, startBlockPos);
                        if (startCollisionBox != Block.NULL_AABB) {
                            AxisAlignedBB offsetStartCollisionBox = startCollisionBox.offset(startBlockPos);
                            if (offsetStartCollisionBox.contains(startVec)) {
                                if (startCollisionBox.maxY >= 1) {
                                    if (world.getBlockState(startBlockPos.up()).getCollisionBoundingBox(world, startBlockPos) == Block.NULL_AABB) {
                                        start.y = Math.ceil(start.y) + 0.01;
                                    }
                                } else {
                                    start.y = offsetStartCollisionBox.maxY + 0.01;
                                }
                            }
                        }

                        AxisAlignedBB endCollisionBox = world.getBlockState(endBlockPos).getCollisionBoundingBox(world, endBlockPos);
                        if (endCollisionBox != Block.NULL_AABB) {
                            AxisAlignedBB offsetEndCollisionBox = endCollisionBox.offset(endBlockPos);
                            if (offsetEndCollisionBox.contains(endVec)) {
                                if (endCollisionBox.maxY >= 1) {
                                    if (world.getBlockState(endBlockPos.up()).getCollisionBoundingBox(world, endBlockPos) == Block.NULL_AABB) {
                                        end.y = Math.ceil(end.y) + 0.01;
                                    }
                                } else {
                                    end.y = offsetEndCollisionBox.maxY + 0.01;
                                }
                            }
                        }

                        drawTexturedLine(start, end, width);
                    }

                    for (Pair<LootRunPath.LootRunPathLocation, Vector3d> render : toRender) {
                        Point3d start = new Point3d(render.a.getLocation());
                        Vector3d direction = new Vector3d(render.b);
                        Point3d end = new Point3d(render.a.getLocation());

                        CustomColor locationColor = color == CommonColors.RAINBOW ? render.a.getColor() : color;
                        locationColor.applyColor();

                        start.y -= .24;

                        direction.normalize();
                        end.add(direction);
                        end.y -= .24;

                        drawTexturedLine(start, end, width);
                    }
                }
            }
        }

        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    public static void drawTexturedLine(Texture texture, Point3d start, Point3d end, CommonColors color, float width) {
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        color.applyColor();

        texture.bind();

        drawTexturedLine(start, end, width);

        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private static void drawTexturedLine(Point3d start, Point3d end, float width) {
        EntityRendererManager renderManager = McIf.mc().getEntityRenderDispatcher();

        Vector3d direction = new Vector3d(start);
        direction.sub(end);
        direction.normalize();

        Vector3d rotationAxis = new Vector3d();
        rotationAxis.cross(new Vector3d(direction.x, 0, direction.z), new Vector3d(0, 1, 0));

        Matrix3d transformation = new Matrix3d();
        transformation.set(new AxisAngle4d(rotationAxis, -Math.PI / 2));

        Vector3d normal = new Vector3d(direction);
        transformation.transform(normal);
        normal.cross(new Vector3d(direction.x, 0, direction.z), normal);
        normal.normalize();

        net.minecraft.util.math.vector.Vector3d scaled = new net.minecraft.util.math.vector.Vector3d(normal.x, normal.y, normal.z).scale(width);

        // we need 4 points for rendering
        net.minecraft.util.math.vector.Vector3d startVec = new net.minecraft.util.math.vector.Vector3d(start.x, start.y, start.z);
        net.minecraft.util.math.vector.Vector3d endVec = new net.minecraft.util.math.vector.Vector3d(end.x, end.y, end.z);
        net.minecraft.util.math.vector.Vector3d p1 = startVec.add(scaled);
        net.minecraft.util.math.vector.Vector3d p2 = startVec.subtract(scaled);
        net.minecraft.util.math.vector.Vector3d p3 = endVec.add(scaled);
        net.minecraft.util.math.vector.Vector3d p4 = endVec.subtract(scaled);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuilder();

        { buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);

            buffer.vertex(p1.x - renderManager.viewerPosX, p1.y - renderManager.viewerPosY, p1.z - renderManager.viewerPosZ)
                    .uv(0f, 0f).endVertex();
            buffer.vertex(p3.x - renderManager.viewerPosX, p3.y - renderManager.viewerPosY, p3.z - renderManager.viewerPosZ)
                    .uv(1f, 0f).endVertex();
            buffer.vertex(p4.x - renderManager.viewerPosX, p4.y - renderManager.viewerPosY, p4.z - renderManager.viewerPosZ)
                    .uv(1f, 1f).endVertex();
            buffer.vertex(p2.x - renderManager.viewerPosX, p2.y - renderManager.viewerPosY, p2.z - renderManager.viewerPosZ)
                    .uv(0f, 1f).endVertex();

        } tess.end();
    }

    public static void drawLines(Long2ObjectMap<List<List<LootRunPath.LootRunPathLocation>>> locations, CustomColor color) {
        if (locations.isEmpty()) return;

        List<ChunkPos> chunks = new ArrayList<>();
        int renderDistance = McIf.mc().options.renderDistance;
        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int z = -renderDistance; z <= renderDistance; z++) {
                int playerChunkX = McIf.player().xChunk;
                int playerChunkZ = McIf.player().zChunk;
                ChunkPos chunk = new ChunkPos(x + playerChunkX, z + playerChunkZ);
                chunks.add(chunk);
            }
        }

        EntityRendererManager renderManager = McIf.mc().getEntityRenderDispatcher();

        GlStateManager.glLineWidth(3f);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuilder();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ);

        {
            for (ChunkPos chunkPos : chunks) {
                if (!McIf.world().hasChunk(chunkPos.x, chunkPos.z)) {
                    continue;
                }
                List<List<LootRunPath.LootRunPathLocation>> locationsInChunk = locations.get(ChunkPos.asLong(chunkPos.x, chunkPos.z));
                if (locationsInChunk != null) {
                    for (List<LootRunPath.LootRunPathLocation> locationsInRoute : locationsInChunk) {
                        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                        boolean disabled = false;
                        List<LootRunPath.LootRunPathLocation> toRender = new ArrayList<>();
                        boolean disable = false;
                        BlockPos lastBlockPos = null;
                        for (LootRunPath.LootRunPathLocation loc : locationsInRoute) {
                            boolean pauseDraw = false;
                            BlockPos blockPos = loc.getLocation().toBlockPos();

                            World world = McIf.world();

                            if (!blockPos.equals(lastBlockPos)) {
                                BlockPos minPos = new BlockPos(loc.getLocation().x - 0.3D, loc.getLocation().y - 1D, loc.getLocation().z - 0.3D);
                                BlockPos maxPos = new BlockPos(loc.getLocation().x + 0.3D, loc.getLocation().y - 1D, loc.getLocation().z + 0.3D);
                                Iterable<BlockPos> blocks = BlockPos.getAllInBox(minPos, maxPos);
                                boolean barrier = false;
                                boolean validBlock = false;
                                for (BlockPos blockInArea : blocks) {
                                    BlockState blockStateInArea = world.getBlockState(blockInArea);
                                    if (blockStateInArea.getBlock() == Blocks.BARRIER) {
                                        barrier = true;
                                    } else if (blockStateInArea.getCollisionBoundingBox(world, blockInArea) != null) {
                                        validBlock = true;
                                    }

                                }

                                if (validBlock) {
                                    disable = false;
                                    if (disabled) {
                                        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                                        disabled = false;
                                    }
                                    for (LootRunPath.LootRunPathLocation location : toRender) {
                                        Location rawLocation = location.getLocation();
                                        CustomColor locationColor = color == CommonColors.RAINBOW ? location.getColor() : color;
                                        buffer.vertex(rawLocation.x, rawLocation.y, rawLocation.z).color(locationColor.r, locationColor.g, locationColor.b, 1f).endVertex();
                                    }
                                    toRender.clear();
                                } else if (barrier) {
                                    disable = true;
                                    pauseDraw = true;
                                    toRender.clear();
                                } else if (disable) {
                                    pauseDraw = true;
                                } else {
                                    toRender.add(loc);
                                    continue;
                                }
                            } else if (disable) {
                                pauseDraw = true;
                            } else if (!toRender.isEmpty()) {
                                toRender.add(loc);
                            }

                            lastBlockPos = blockPos;

                            if (!pauseDraw) {
                                if (disabled) {
                                    buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                                    disabled = false;
                                }
                                Location rawLocation = loc.getLocation();
                                CustomColor locationColor = color == CommonColors.RAINBOW ? loc.getColor() : color;
                                buffer.vertex(rawLocation.x, rawLocation.y, rawLocation.z).color(locationColor.r, locationColor.g, locationColor.b, 1f).endVertex();
                            } else if (!disabled) {
                                tess.end();
                                disabled = true;
                            }
                        }
                        if (!disabled) {
                            for (LootRunPath.LootRunPathLocation location : toRender) {
                                Location rawLocation = location.getLocation();
                                CustomColor locationColor = color == CommonColors.RAINBOW ? location.getColor() : color;
                                buffer.vertex(rawLocation.x, rawLocation.y, rawLocation.z).color(locationColor.r, locationColor.g, locationColor.b, 1f).endVertex();
                            }
                            tess.end();
                        }
                    }
                }
            }
        }

        GlStateManager.popMatrix();

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    public static void drawCube(BlockPos point, CustomColor color) {
        if (!McIf.world().isBlockLoaded(point, false)) return;

        EntityRendererManager renderManager = McIf.mc().getEntityRenderDispatcher();

        Location c = new Location(
            point.getX() - renderManager.viewerPosX,
            point.getY() - renderManager.viewerPosY,
            point.getZ() - renderManager.viewerPosZ
        );

        GlStateManager.pushMatrix();

        GlStateManager.glLineWidth(3f);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        WorldRenderer.drawBoundingBox(c.x, c.y, c.z, c.x + 1, c.y + 1, c.z + 1, color.r, color.g, color.b, color.a);

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);

        GlStateManager.popMatrix();
    }

}
