/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BottlingMachineMultiblock extends IETemplateMultiblock
{
	//TODO pump seems to be a bit weird
	public BottlingMachineMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/bottling_machine"),
				new BlockPos(1, 1, 0), new BlockPos(1, 1, 1), () -> Multiblocks.bottlingMachine.getDefaultState());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	private static ItemStack renderStack;

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack==null)
			renderStack = new ItemStack(Multiblocks.bottlingMachine);
		GlStateManager.translated(1.5, 1.5, 1.5);
		GlStateManager.rotatef(-45, 0, 1, 0);
		GlStateManager.rotatef(-20, 1, 0, 0);
		GlStateManager.scaled(4, 4, 4);

		GlStateManager.disableCull();
		ClientUtils.mc().getItemRenderer().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}
