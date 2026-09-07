package net.minecraft.client.model;

/*
 * Adapted to b1.7.3 API
 */

import org.lwjgl.opengl.GL11;

import net.minecraft.game.MathHelper;

public class ModelHauntedCow
extends ModelBase {
    ModelRenderer head;
    ModelRenderer body;
    ModelRenderer leg1;
    ModelRenderer leg2;
    ModelRenderer leg3;
    ModelRenderer leg4;
    ModelRenderer horn1;
    ModelRenderer horn2;
    ModelRenderer udders;
    ModelRenderer bodyInside;

    public ModelHauntedCow() {
        this.head = new ModelRenderer(0, 0).setTextureSize(64, 64);
        this.head.addBox(-4.0f, -4.0f, -6.0f, 8, 8, 6);
        this.head.setRotationPoint(0.0f, 7.0f, -8.0f);
        this.head.setTextureSize(64, 64);
        this.head.mirror = true;
        this.setRotation(this.head, 0.0f, 0.0f, 0.0f);
        this.body = new ModelRenderer(18, 14).setTextureSize(64, 64);
        this.body.addBox(-6.0f, -10.0f, -7.0f, 12, 18, 10);
        this.body.setRotationPoint(0.0f, 5.0f, 2.0f);
        this.body.setTextureSize(64, 64);
        this.body.mirror = true;
        this.setRotation(this.body, 1.570796f, 0.0f, 0.0f);
        this.leg1 = new ModelRenderer(0, 16).setTextureSize(64, 64);
        this.leg1.addBox(-3.0f, 0.0f, -2.0f, 4, 12, 4);
        this.leg1.setRotationPoint(-3.0f, 12.0f, 7.0f);
        this.leg1.setTextureSize(64, 64);
        this.leg1.mirror = true;
        this.setRotation(this.leg1, 0.0f, 0.0f, 0.0f);
        this.leg2 = new ModelRenderer(0, 16).setTextureSize(64, 64);
        this.leg2.addBox(-1.0f, 0.0f, -2.0f, 4, 12, 4);
        this.leg2.setRotationPoint(3.0f, 12.0f, 7.0f);
        this.leg2.setTextureSize(64, 64);
        this.leg2.mirror = true;
        this.setRotation(this.leg2, 0.0f, 0.0f, 0.0f);
        this.leg3 = new ModelRenderer(0, 16).setTextureSize(64, 64);
        this.leg3.addBox(-3.0f, 0.0f, -3.0f, 4, 12, 4);
        this.leg3.setRotationPoint(-3.0f, 12.0f, -5.0f);
        this.leg3.setTextureSize(64, 64);
        this.leg3.mirror = true;
        this.setRotation(this.leg3, 0.0f, 0.0f, 0.0f);
        this.leg4 = new ModelRenderer(0, 16).setTextureSize(64, 64);
        this.leg4.addBox(-1.0f, 0.0f, -3.0f, 4, 12, 4);
        this.leg4.setRotationPoint(3.0f, 12.0f, -5.0f);
        this.leg4.setTextureSize(64, 64);
        this.leg4.mirror = true;
        this.setRotation(this.leg4, 0.0f, 0.0f, 0.0f);
        this.horn1 = new ModelRenderer(31, 0).setTextureSize(64, 64);
        this.horn1.addBox(-3.0f, -10.0f, 4.0f, 1, 3, 1);
        this.horn1.setRotationPoint(0.0f, 3.0f, -7.0f);
        this.horn1.setTextureSize(64, 64);
        this.horn1.mirror = true;
        this.setRotation(this.horn1, 0.0f, 0.0f, 0.0f);
        this.head.addChild(this.horn1);
        this.horn2 = new ModelRenderer(31, 0).setTextureSize(64, 64);
        this.horn2.addBox(2.0f, -10.0f, 4.0f, 1, 3, 1);
        this.horn2.setRotationPoint(0.0f, 3.0f, -7.0f);
        this.horn2.setTextureSize(64, 64);
        this.horn2.mirror = true;
        this.setRotation(this.horn2, 0.0f, 0.0f, 0.0f);
        this.head.addChild(this.horn2);
        this.udders = new ModelRenderer(52, 0).setTextureSize(64, 64);
        this.udders.addBox(-2.0f, -3.0f, 0.0f, 4, 6, 2);
        this.udders.setRotationPoint(0.0f, 14.0f, 6.0f);
        this.udders.setTextureSize(64, 64);
        this.udders.mirror = true;
        this.setRotation(this.udders, 0.0f, 0.0f, 0.0f);
        this.bodyInside = new ModelRenderer(18, 42).setTextureSize(64, 64);
        this.bodyInside.addBox(-4.0f, -4.0f, -0.0f, 8, 8, 14);
        this.bodyInside.setRotationPoint(0.0f, 8.0f, -6.0f);
        this.bodyInside.setTextureSize(64, 64);
        this.setRotation(this.bodyInside, 0.0f, 0.0f, 0.0f);
    }

    @Override
    public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
        super.render(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
        if (this.isChild) {
            GL11.glPushMatrix();
            this.horn1.render(scale);
            this.horn2.render(scale);
            GL11.glPopMatrix();
        } else {
            this.head.render(scale);
            this.body.render(scale);
            this.udders.render(scale);
            this.bodyInside.render(scale);
            this.leg1.render(scale);
            this.leg2.render(scale);
            this.leg3.render(scale);
            this.leg4.render(scale);
        }
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
        this.head.rotateAngleZ += headPitch * 0.001f;
        this.leg1.rotateAngleX = MathHelper.cos((float)(limbSwing * 0.6662f)) * 1.4f * limbSwingAmount;
        this.leg2.rotateAngleX = MathHelper.cos((float)(limbSwing * 0.6662f + (float)Math.PI)) * 1.4f * limbSwingAmount;
        this.leg3.rotateAngleX = MathHelper.cos((float)(limbSwing * 0.6662f + (float)Math.PI)) * 1.4f * limbSwingAmount;
        this.leg4.rotateAngleX = MathHelper.cos((float)(limbSwing * 0.6662f)) * 1.4f * limbSwingAmount;
    }
}
