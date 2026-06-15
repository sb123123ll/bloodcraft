package com.qiamao.blood.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelBloodSeeker extends ModelBase {
    // 定义模型部件
    public ModelRenderer body;
    public ModelRenderer head;
    
    // 6条腿 (两段式)
    public ModelRenderer leg1_base; // 左前根部
    public ModelRenderer leg1_tip;  // 左前末端
    
    public ModelRenderer leg2_base; // 右前根部
    public ModelRenderer leg2_tip;  // 右前末端
    
    public ModelRenderer leg3_base; // 左中根部
    public ModelRenderer leg3_tip;  // 左中末端
    
    public ModelRenderer leg4_base; // 右中根部
    public ModelRenderer leg4_tip;  // 右中末端
    
    public ModelRenderer leg5_base; // 左后根部
    public ModelRenderer leg5_tip;  // 左后末端
    
    public ModelRenderer leg6_base; // 右后根部
    public ModelRenderer leg6_tip;  // 右后末端
    
    // 尾巴
    public ModelRenderer tailBase; // 第一段尾巴
    public ModelRenderer tailTip;  // 第二段尾巴
    public ModelRenderer stinger;  // 尾巴尖刺

    public ModelBloodSeeker() {
        this.textureWidth = 128; // 将宽度扩大到 128，防止大方块 UV 越界被截断
        this.textureHeight = 64;

        // 1. 身体 (长1.3方块约21像素，宽0.5方块约8像素，高约6像素)
        // 彻底解决左侧贴图问题：原版的一个长方体贴图展开需要 (宽*2 + 长*2) = (8*2 + 21*2) = 58 像素的宽度。
        // 原来 textureWidth 只有 64，稍微有点偏差可能就会导致左侧(最后面那块)越界没画出来。
        // 现在我把 textureWidth 改成 128，并且关闭 mirror，让它正常展开。
        this.body = new ModelRenderer(this, 0, 0); 
        this.body.addBox(-4.0F, -3.0F, -10.5F, 8, 6, 21); 
        this.body.setRotationPoint(0.0F, 18.0F, 0.0F); 
        this.body.rotateAngleX = 0.1F; 

        // 2. 头部
        this.head = new ModelRenderer(this, 0, 27);
        // 让头高一点：原先 y 是 -3.0F，现在改为 -5.0F，使得头部整体往上顶。
        this.head.addBox(-3.0F, -5.0F, -6.0F, 6, 6, 6);
        this.head.setRotationPoint(0.0F, 18.0F, -10.5F);

        // 3. 腿部 (两段式：根部向外，末端向下)
        // 修改末端连接方式：直接将根部和末端都绑定在实体主坐标系，不使用 addChild 嵌套。
        // 因为嵌套子节点在进行多次旋转和平移时容易出现局部坐标系错乱导致部件飞到视野外（也就是你看到的“消失”）
        
        // 左前腿
        this.leg1_base = new ModelRenderer(this, 0, 42);
        this.leg1_base.addBox(-8.0F, -1.0F, -1.0F, 8, 2, 2); // 长度翻倍，从 4 改为 8
        this.leg1_base.setRotationPoint(-4.0F, 18.0F, -7.0F);
        
        this.leg1_tip = new ModelRenderer(this, 20, 42); // UV 右移，给 base 留出空间
        this.leg1_tip.addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2); 
        this.leg1_tip.setRotationPoint(-8.0F, 0.0F, 0.0F); // 相对于base的末端
        this.leg1_base.addChild(this.leg1_tip);
        
        // 右前腿
        this.leg2_base = new ModelRenderer(this, 0, 42);
        this.leg2_base.addBox(0.0F, -1.0F, -1.0F, 8, 2, 2); 
        this.leg2_base.setRotationPoint(4.0F, 18.0F, -7.0F);
        
        this.leg2_tip = new ModelRenderer(this, 20, 42);
        this.leg2_tip.addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2); 
        this.leg2_tip.setRotationPoint(8.0F, 0.0F, 0.0F); // 相对于base的末端
        this.leg2_base.addChild(this.leg2_tip);

        // 左中腿
        this.leg3_base = new ModelRenderer(this, 0, 42);
        this.leg3_base.addBox(-8.0F, -1.0F, -1.0F, 8, 2, 2); 
        this.leg3_base.setRotationPoint(-4.0F, 18.0F, 0.0F);
        
        this.leg3_tip = new ModelRenderer(this, 20, 42);
        this.leg3_tip.addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2); 
        this.leg3_tip.setRotationPoint(-8.0F, 0.0F, 0.0F); 
        this.leg3_base.addChild(this.leg3_tip);
        
        // 右中腿
        this.leg4_base = new ModelRenderer(this, 0, 42);
        this.leg4_base.addBox(0.0F, -1.0F, -1.0F, 8, 2, 2); 
        this.leg4_base.setRotationPoint(4.0F, 18.0F, 0.0F);
        
        this.leg4_tip = new ModelRenderer(this, 20, 42);
        this.leg4_tip.addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2); 
        this.leg4_tip.setRotationPoint(8.0F, 0.0F, 0.0F); 
        this.leg4_base.addChild(this.leg4_tip);

        // 左后腿
        this.leg5_base = new ModelRenderer(this, 0, 42);
        this.leg5_base.addBox(-8.0F, -1.0F, -1.0F, 8, 2, 2); 
        this.leg5_base.setRotationPoint(-4.0F, 18.0F, 7.0F);
        
        this.leg5_tip = new ModelRenderer(this, 20, 42);
        this.leg5_tip.addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2); 
        this.leg5_tip.setRotationPoint(-8.0F, 0.0F, 0.0F); 
        this.leg5_base.addChild(this.leg5_tip);
        
        // 右后腿
        this.leg6_base = new ModelRenderer(this, 0, 42);
        this.leg6_base.addBox(0.0F, -1.0F, -1.0F, 8, 2, 2); 
        this.leg6_base.setRotationPoint(4.0F, 18.0F, 7.0F);
        
        this.leg6_tip = new ModelRenderer(this, 20, 42);
        this.leg6_tip.addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2); 
        this.leg6_tip.setRotationPoint(8.0F, 0.0F, 0.0F); 
        this.leg6_base.addChild(this.leg6_tip);

        // 调整腿部的初始旋转：根部微微向下弯，末端往内扣一点，更像真实的节肢动物
        float bendBase = 0.2F;
        float bendTip = -0.2F; // 让尖端往内扣
        
        this.leg1_base.rotateAngleZ = bendBase;  this.leg1_tip.rotateAngleZ = bendTip;
        this.leg2_base.rotateAngleZ = -bendBase; this.leg2_tip.rotateAngleZ = -bendTip;
        this.leg3_base.rotateAngleZ = bendBase;  this.leg3_tip.rotateAngleZ = bendTip;
        this.leg4_base.rotateAngleZ = -bendBase; this.leg4_tip.rotateAngleZ = -bendTip;
        this.leg5_base.rotateAngleZ = bendBase;  this.leg5_tip.rotateAngleZ = bendTip;
        this.leg6_base.rotateAngleZ = -bendBase; this.leg6_tip.rotateAngleZ = -bendTip;

        // 4. 尾巴两段式 + 刺
        this.tailBase = new ModelRenderer(this, 26, 28);
        this.tailBase.addBox(-2.0F, -2.0F, 0.0F, 4, 4, 8);
        this.tailBase.setRotationPoint(0.0F, 17.0F, 10.5F); 
        this.tailBase.rotateAngleX = 1.0F; // 像蝎子一样往前扬起(正角度向上)

        this.tailTip = new ModelRenderer(this, 52, 28);
        this.tailTip.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 8);
        this.tailTip.setRotationPoint(0.0F, 1.0F, 7.5F); 
        this.tailTip.rotateAngleX = 1.0F; // 继续往前弯曲
        this.tailBase.addChild(this.tailTip); 

        this.stinger = new ModelRenderer(this, 76, 28);
        this.stinger.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 6);
        this.stinger.setRotationPoint(0.0F, 0.0F, 8.0F); 
        this.stinger.rotateAngleX = 0.5F; // 刺也朝前
        this.tailTip.addChild(this.stinger); 
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        
        // 渲染所有节点 (因为我们去除了 addChild 嵌套，所以子节点也要手动 render)
        this.body.render(scale);
        this.head.render(scale);
        
        this.leg1_base.render(scale);
        this.leg2_base.render(scale);
        this.leg3_base.render(scale);
        this.leg4_base.render(scale);
        this.leg5_base.render(scale);
        this.leg6_base.render(scale);
        
        this.tailBase.render(scale); 
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        // 1. 头部转动
        this.head.rotateAngleY = netHeadYaw * 0.017453292F;
        this.head.rotateAngleX = headPitch * 0.017453292F;

        // 2. 6腿行走动画 (像节肢动物爬行，使用相差偏移量，并增加 Z 轴抬腿动作)
        // 蜘蛛和蜈蚣等节肢动物爬行时，腿的摆动频率更快，并且是一侧的前/后与中间交替
        float legSwingSpeed = 1.2F; // 基础摆动频率
        // 如果是幼虫，将动画速度(摆动频率)乘以 2 倍
        if (entityIn instanceof com.qiamao.blood.entity.EntityBloodSeekerLarva) {
            legSwingSpeed *= 2.0F;
        }
        float legSwingAmp = 0.8F;   // 稍微降低摆动幅度，让动作更紧凑

        // 计算Y轴的前后摆动 (前后方向)
        // 引入偏移相位 (PI/3)，使得 3 条腿不是同时在一个点上，而是呈现波浪传递
        float ySwing1 = MathHelper.cos(limbSwing * legSwingSpeed) * legSwingAmp * limbSwingAmount;
        float ySwing2 = MathHelper.cos(limbSwing * legSwingSpeed + (float)Math.PI / 3.0F) * legSwingAmp * limbSwingAmount;
        float ySwing3 = MathHelper.cos(limbSwing * legSwingSpeed + (float)Math.PI * 2.0F / 3.0F) * legSwingAmp * limbSwingAmount;

        // 计算Z轴的上下抬腿 (当腿往前迈的时候，需要稍微抬起离开地面)
        // Math.abs(sin) 可以确保腿每次摆动（无论是向前还是向后）都会有一个向上的抬起动作
        float zLiftAmp = 0.5F * limbSwingAmount; 
        float zLift1 = Math.abs(MathHelper.sin(limbSwing * legSwingSpeed)) * zLiftAmp;
        float zLift2 = Math.abs(MathHelper.sin(limbSwing * legSwingSpeed + (float)Math.PI / 3.0F)) * zLiftAmp;
        float zLift3 = Math.abs(MathHelper.sin(limbSwing * legSwingSpeed + (float)Math.PI * 2.0F / 3.0F)) * zLiftAmp;

        // 基础的根部下弯角度
        float bendBase = 0.2F;

        // 前腿 (左1, 右2) - 注意左右腿的Y轴摆动方向相反以保持平衡
        this.leg1_base.rotateAngleY = -ySwing1 - 0.2F; // 左前稍微外八字
        this.leg2_base.rotateAngleY = ySwing1 + 0.2F;  // 右前稍微外八字
        this.leg1_base.rotateAngleZ = bendBase - zLift1; // 左侧Z抬腿(负数向上)
        this.leg2_base.rotateAngleZ = -bendBase + zLift1; // 右侧Z抬腿(正数向上)
        
        // 中腿 (左3, 右4) - 相位错开
        this.leg3_base.rotateAngleY = ySwing2; 
        this.leg4_base.rotateAngleY = -ySwing2; 
        this.leg3_base.rotateAngleZ = bendBase - zLift2;
        this.leg4_base.rotateAngleZ = -bendBase + zLift2;
        
        // 后腿 (左5, 右6) - 相位再次错开
        this.leg5_base.rotateAngleY = -ySwing3 + 0.2F; // 左后稍微外八字
        this.leg6_base.rotateAngleY = ySwing3 - 0.2F;  // 右后稍微外八字
        this.leg5_base.rotateAngleZ = bendBase - zLift3;
        this.leg6_base.rotateAngleZ = -bendBase + zLift3;

        // 3. 尾巴基础动画 (根据是否射击添加甩尾动作)
        // 获取客户端同步的射击计时器
        int shootTimer = 0;
        if (entityIn instanceof com.qiamao.blood.entity.EntityBloodSeeker) {
            shootTimer = ((com.qiamao.blood.entity.EntityBloodSeeker) entityIn).clientShootTimer;
        }

        // 默认的基础尾巴上扬
        float baseTailPitch = 1.0F; 
        float baseTipPitch = 1.0F;
        float baseStingerPitch = 0.5F;

        if (shootTimer > 0) {
            // 我们在服务端设定的总动画时间是 16 ticks，第 8 tick 发射。
            // 将 1-16 映射到 0.0 - 1.0 的进度
            float progress = Math.min(shootTimer / 16.0F, 1.0F); 
            float tailWhip = 0.0F;
            
            if (progress < 0.4F) {
                // 0% - 40% (约 0-6 ticks)：往后蓄力前摇
                // 进度从 0 到 1，tailWhip 从 0 到 -0.6
                float p = progress / 0.4F;
                tailWhip = -p * 0.6F;
            } else if (progress < 0.6F) {
                // 40% - 60% (约 6-10 ticks)：瞬间发力往前猛甩
                // 进度从 0 到 1，tailWhip 从 -0.6 猛增到 0.8
                float p = (progress - 0.4F) / 0.2F;
                tailWhip = -0.6F + p * 1.4F;
            } else {
                // 60% - 100% (约 10-16 ticks)：缓冲恢复原位
                // 进度从 0 到 1，tailWhip 从 0.8 缓慢回到 0
                float p = (progress - 0.6F) / 0.4F;
                tailWhip = 0.8F * (1.0F - p);
            }
            
            // 将力量感甩尾应用到尾巴各节，越往后幅度越大
            this.tailBase.rotateAngleX = baseTailPitch + tailWhip * 0.5F;
            this.tailTip.rotateAngleX = baseTipPitch + tailWhip * 1.0F; 
            this.stinger.rotateAngleX = baseStingerPitch + tailWhip * 1.2F;
            
        } else {
            // 闲置/走路时的尾巴动画
            float tailSway = MathHelper.cos(limbSwing * legSwingSpeed * 0.5F) * 0.2F * limbSwingAmount;
            float idleTail = MathHelper.sin(ageInTicks * 0.1F) * 0.1F; // 呼吸般的轻微起伏

            this.tailBase.rotateAngleX = baseTailPitch + idleTail;
            this.tailBase.rotateAngleY = tailSway; 

            this.tailTip.rotateAngleX = baseTipPitch + idleTail * 1.5F;
            this.tailTip.rotateAngleY = tailSway * 1.5F;

            this.stinger.rotateAngleX = baseStingerPitch;
            this.stinger.rotateAngleY = 0.0F;
        }
    }
}
