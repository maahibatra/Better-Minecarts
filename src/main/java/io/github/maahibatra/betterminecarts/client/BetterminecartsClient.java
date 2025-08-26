package io.github.maahibatra.betterminecarts.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class BetterminecartsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
//        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
//            MatrixStack matrices = context.matrixStack();
//            MinecraftClient client = MinecraftClient.getInstance();
//
//            matrices.push();
//
//            VertexConsumerProvider.Immediate vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
//            var builder = vertexConsumers.getBuffer(RenderLayer.getLines());
//
//            Vec3d start = new Vec3d(0, 0, 0);
//            Vec3d end = new Vec3d(1, 1, 1);
//
//            Vec3d camPos = client.gameRenderer.getCamera().getPos();
//            matrices.translate(-camPos.x, -camPos.y, -camPos.z);
//
//            builder.vertex(matrices.peek().getPositionMatrix(), (float) start.x, (float) start.y, (float) start.z).color(200, 200, 200, 255).normal(0, 1, 0);
//            builder.vertex(matrices.peek().getPositionMatrix(), (float) end.x, (float) end.y, (float) end.z).color(200, 200, 200, 255).normal(0, 1, 0);
//
//            matrices.pop();
//            vertexConsumers.draw();
//        });
    }
}
