/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.client.models.connection.FeedthroughModel;
import blusunrize.immersiveengineering.client.models.obj.IESmartObjModel;
import blusunrize.immersiveengineering.common.items.IEBaseItem;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
@OnlyIn(Dist.CLIENT)
public class ImmersiveModelRegistry
{
	public static ImmersiveModelRegistry instance = new ImmersiveModelRegistry();
	private static final ImmutableMap<String, String> flipData = ImmutableMap.of("flip-v", String.valueOf(true));
	private HashMap<ModelResourceLocation, ItemModelReplacement> itemModelReplacements = new HashMap<>();

	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event)
	{
		for(Map.Entry<ModelResourceLocation, ItemModelReplacement> entry : itemModelReplacements.entrySet())
		{
			IBakedModel object = event.getModelRegistry().get(entry.getKey());
			if(object!=null)
			{
				try
				{
					event.getModelRegistry().put(entry.getKey(), entry.getValue().createBakedModel(object));
				} catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		IConveyorBelt belt = ConveyorHandler.getConveyor(new ResourceLocation(ImmersiveEngineering.MODID, "conveyor"), null);
		ModelConveyor modelConveyor = new ModelConveyor(belt);
		ModelResourceLocation mLoc = new ModelResourceLocation(new ResourceLocation("immersiveengineering", "conveyor"), "normal");
		event.getModelRegistry().put(mLoc, modelConveyor);
		mLoc = new ModelResourceLocation(new ResourceLocation("immersiveengineering", "conveyor"), "inventory");
		event.getModelRegistry().put(mLoc, modelConveyor);
		mLoc = new ModelResourceLocation(new ResourceLocation(ImmersiveEngineering.MODID, "connector"), "inventory,type=feedthrough");
		event.getModelRegistry().put(mLoc, new FeedthroughModel());
	}

	public void registerCustomItemModel(ItemStack stack, ItemModelReplacement replacement)
	{
		if(stack.getItem() instanceof IEBaseItem)
		{
			ResourceLocation loc = new ResourceLocation("immersiveengineering", ((IEBaseItem)stack.getItem()).itemName);
			itemModelReplacements.put(new ModelResourceLocation(loc, "inventory"), replacement);
		}
	}


	public abstract static class ItemModelReplacement
	{
		public abstract IBakedModel createBakedModel(IBakedModel existingModel);
	}

	public static class ItemModelReplacement_OBJ extends ItemModelReplacement
	{
		String objPath;
		HashMap<TransformType, Matrix4> transformationMap = new HashMap<TransformType, Matrix4>();
		boolean dynamic;

		public ItemModelReplacement_OBJ(String path, boolean dynamic)
		{
			this.objPath = path;
			this.dynamic = dynamic;
			for(TransformType t : TransformType.values())
				transformationMap.put(t, new Matrix4());
		}

		public ItemModelReplacement_OBJ setTransformations(TransformType type, Matrix4 matrix)
		{
			this.transformationMap.put(type, matrix);
			return this;
		}

		@Override
		public IBakedModel createBakedModel(IBakedModel existingModel)
		{
			try
			{
				Function<ResourceLocation, TextureAtlasSprite> textureGetter =
						location -> Minecraft.getInstance().getTextureMap().getAtlasSprite(location.toString());
				ResourceLocation modelLocation = new ResourceLocation(objPath);
				OBJModel objModel = (OBJModel)OBJLoader.INSTANCE.loadModel(modelLocation);
				objModel = (OBJModel)objModel.process(flipData);
				ImmutableMap.Builder<String, TextureAtlasSprite> builder = ImmutableMap.builder();
				builder.put(ModelLoader.White.LOCATION.toString(), ModelLoader.White.INSTANCE);
				TextureAtlasSprite missing = textureGetter.apply(new ResourceLocation("missingno"));
				for(String s : objModel.getMatLib().getMaterialNames())
					if(objModel.getMatLib().getMaterial(s).getTexture().getTextureLocation().getPath().startsWith("#"))
					{
						IELogger.error("OBJLoader: Unresolved texture '{}' for obj model '{}'",
								objModel.getMatLib().getMaterial(s).getTexture().getTextureLocation().getPath(), modelLocation);
						builder.put(s, missing);
					}
					else
						builder.put(s, textureGetter.apply(objModel.getMatLib().getMaterial(s).getTexture().getTextureLocation()));

				return new IESmartObjModel(existingModel, objModel, new OBJModel.OBJState(Lists.newArrayList(OBJModel.Group.ALL), true),
						DefaultVertexFormats.ITEM, builder.build(), transformationMap, dynamic);
			} catch(Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
	}
}