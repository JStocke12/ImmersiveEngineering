/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.OreOutput;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@SuppressWarnings("deprecation")
public class ModelCoresample extends BakedIEModel
{
	private static final Cache<String, ModelCoresample> modelCache = CacheBuilder.newBuilder()
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build();
	private MineralMix mineral;
	private List<BakedQuad> bakedQuads;

	public ModelCoresample(MineralMix mineral)
	{
		this.mineral = mineral;
	}

	public static void clearCache()
	{
		modelCache.invalidateAll();
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState coreState, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		MineralMix mineral;
		if(extraData.hasProperty(Model.MINERAL))
			mineral = extraData.getData(Model.MINERAL);
		else
			mineral = this.mineral;
		if(bakedQuads==null||this.mineral==null)
		{
			bakedQuads = new ArrayList<>();
			try
			{
				float width = .25f;
				float depth = .25f;
				float wOff = (1-width)/2;
				float dOff = (1-depth)/2;
				int pixelLength = 0;

				Map<TextureAtlasSprite, Integer> textureOre = new HashMap<>();
				if(mineral!=null)
				{
					for(OreOutput o : mineral.outputs)
						if(!o.stack.isEmpty())
						{
							int weight = Math.max(2, (int)Math.round(16*o.recalculatedChance));
							Block b = Block.getBlockFromItem(o.stack.getItem());
							BlockState state = b!=Blocks.AIR?b.getDefaultState(): Blocks.STONE.getDefaultState();
							IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(state);
							if(model!=null&&model.getParticleTexture()!=null)
								textureOre.put(model.getParticleTexture(), weight);
							pixelLength += weight;
						}
				}
				else
					pixelLength = 16;
				TextureAtlasSprite textureStone = ClientUtils.getSprite(new ResourceLocation("block/stone"));

				Vec2f[] stoneUVs = {
						new Vec2f(textureStone.getInterpolatedU(16*wOff), textureStone.getInterpolatedV(16*dOff)),
						new Vec2f(textureStone.getInterpolatedU(16*wOff), textureStone.getInterpolatedV(16*(dOff+depth))),
						new Vec2f(textureStone.getInterpolatedU(16*(wOff+width)), textureStone.getInterpolatedV(16*(dOff+depth))),
						new Vec2f(textureStone.getInterpolatedU(16*(wOff+width)), textureStone.getInterpolatedV(16*dOff))};

				putVertexData(new Vec3d(0, -1, 0), new Vec3d[]{new Vec3d(wOff, 0, dOff),
						new Vec3d(wOff+width, 0, dOff), new Vec3d(wOff+width, 0, dOff+depth),
						new Vec3d(wOff, 0, dOff+depth)}, stoneUVs, textureStone, bakedQuads);
				putVertexData(new Vec3d(0, 1, 0), new Vec3d[]{new Vec3d(wOff, 1, dOff),
						new Vec3d(wOff, 1, dOff+depth), new Vec3d(wOff+width, 1, dOff+depth),
						new Vec3d(wOff+width, 1, dOff)}, stoneUVs, textureStone, bakedQuads);
				if(textureOre.isEmpty())
				{
					Vec2f[][] uvs = new Vec2f[4][];
					for(int j = 0; j < 4; j++)
						uvs[j] = new Vec2f[]{
								new Vec2f(textureStone.getInterpolatedU(j*4), textureStone.getInterpolatedV(0)),
								new Vec2f(textureStone.getInterpolatedU(j*4), textureStone.getInterpolatedV(16)),
								new Vec2f(textureStone.getInterpolatedU((j+1)*4), textureStone.getInterpolatedV(16)),
								new Vec2f(textureStone.getInterpolatedU((j+1)*4), textureStone.getInterpolatedV(0))};

					putVertexData(new Vec3d(0, 0, -1), new Vec3d[]{
							new Vec3d(wOff, 0, dOff),
							new Vec3d(wOff, 1, dOff),
							new Vec3d(wOff+width, 1, dOff),
							new Vec3d(wOff+width, 0, dOff)
					}, uvs[0], textureStone, bakedQuads);
					putVertexData(new Vec3d(0, 0, 1), new Vec3d[]{
							new Vec3d(wOff+width, 0, dOff+depth),
							new Vec3d(wOff+width, 1, dOff+depth),
							new Vec3d(wOff, 1, dOff+depth),
							new Vec3d(wOff, 0, dOff+depth)
					}, uvs[2], textureStone, bakedQuads);
					putVertexData(new Vec3d(-1, 0, 0), new Vec3d[]{
									new Vec3d(wOff, 0, dOff+depth),
									new Vec3d(wOff, 1, dOff+depth),
									new Vec3d(wOff, 1, dOff),
									new Vec3d(wOff, 0, dOff)
							},
							uvs[3], textureStone, bakedQuads);
					putVertexData(new Vec3d(1, 0, 0), new Vec3d[]{
							new Vec3d(wOff+width, 0, dOff),
							new Vec3d(wOff+width, 1, dOff),
							new Vec3d(wOff+width, 1, dOff+depth),
							new Vec3d(wOff+width, 0, dOff+depth)
					}, uvs[1], textureStone, bakedQuads);
				}
				else
				{
					float h = 0;
					for(TextureAtlasSprite sprite : textureOre.keySet())
					{
						int weight = textureOre.get(sprite);
						int v = weight > 8?16-weight: 8;
						Vec2f[][] uvs = new Vec2f[4][];
						for(int j = 0; j < 4; j++)
							uvs[j] = new Vec2f[]{
									new Vec2f(sprite.getInterpolatedU(j*4), sprite.getInterpolatedV(v)),
									new Vec2f(sprite.getInterpolatedU(j*4), sprite.getInterpolatedV(v+weight)),
									new Vec2f(sprite.getInterpolatedU((j+1)*4), sprite.getInterpolatedV(v+weight)),
									new Vec2f(sprite.getInterpolatedU((j+1)*4), sprite.getInterpolatedV(v))};

						float h1 = weight/(float)pixelLength;
						putVertexData(new Vec3d(0, 0, -1), new Vec3d[]{
								new Vec3d(wOff, h, dOff),
								new Vec3d(wOff, h+h1, dOff),
								new Vec3d(wOff+width, h+h1, dOff),
								new Vec3d(wOff+width, h, dOff)
						}, uvs[0], sprite, bakedQuads);
						putVertexData(new Vec3d(0, 0, 1), new Vec3d[]{
								new Vec3d(wOff+width, h, dOff+depth),
								new Vec3d(wOff+width, h+h1, dOff+depth),
								new Vec3d(wOff, h+h1, dOff+depth),
								new Vec3d(wOff, h, dOff+depth)
						}, uvs[2], sprite, bakedQuads);
						putVertexData(new Vec3d(-1, 0, 0), new Vec3d[]{
								new Vec3d(wOff, h, dOff+depth),
								new Vec3d(wOff, h+h1, dOff+depth),
								new Vec3d(wOff, h+h1, dOff),
								new Vec3d(wOff, h, dOff)
						}, uvs[3], sprite, bakedQuads);
						putVertexData(new Vec3d(1, 0, 0), new Vec3d[]{
								new Vec3d(wOff+width, h, dOff),
								new Vec3d(wOff+width, h+h1, dOff),
								new Vec3d(wOff+width, h+h1, dOff+depth),
								new Vec3d(wOff+width, h, dOff+depth)
						}, uvs[1], sprite, bakedQuads);
						h += h1;
					}
				}
			} catch(Exception e)
			{
				e.printStackTrace();
			}
			if(bakedQuads.isEmpty())
				throw new RuntimeException("Empty quad list!");
			return bakedQuads;
		}
		return bakedQuads;
	}

	protected final void putVertexData(Vec3d normal, Vec3d[] vertices, Vec2f[] uvs, TextureAtlasSprite sprite, List<BakedQuad> out)
	{
		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM);
		builder.setQuadOrientation(Direction.getFacingFromVector(normal.x, normal.y, normal.z));
		builder.setTexture(sprite);
		for(int i = 0; i < vertices.length; i++)
		{
			builder.put(0, (float)vertices[i].x, (float)vertices[i].y, (float)vertices[i].z, 1);//Pos
			float d = LightUtil.diffuseLight((float)normal.x, (float)normal.y, (float)normal.z);
			builder.put(1, d, d, d, 1);//Colour
			builder.put(2, uvs[i].x, uvs[i].y, 0, 1);//UV
			builder.put(3, (float)normal.x, (float)normal.y, (float)normal.z, 0);//Normal
			builder.put(4);//padding
		}
		out.add(builder.build());
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return null;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return overrideList;
	}


	ItemOverrideList overrideList = new ItemOverrideList()
	{

		@Nullable
		@Override
		public IBakedModel getModelWithOverrides(IBakedModel originalModel, ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn)
		{
			if(ItemNBTHelper.hasKey(stack, "mineral"))
			{
				String name = ItemNBTHelper.getString(stack, "mineral");
				if(!name.isEmpty())
				{
					try
					{
						return modelCache.get(name, () -> {
							for(MineralMix mix : ExcavatorHandler.mineralList.keySet())
								if(name.equals(mix.name))
									return new ModelCoresample(mix);
							throw new RuntimeException("Invalid mineral mix: "+name);
						});
					} catch(ExecutionException e)
					{
						throw new RuntimeException(e);
					}
				}
			}
			return originalModel;
		}
	};

	static HashMap<TransformType, Matrix4> transformationMap = new HashMap<>();

	static
	{
		transformationMap.put(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().translate(0, .28, 0).rotate(Math.toRadians(180), 1, 0, 0).rotate(Math.toRadians(-90), 0, 1, 0));
		transformationMap.put(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().translate(0, .28, 0).rotate(Math.toRadians(180), 1, 0, 0).rotate(Math.toRadians(-90), 0, 1, 0));
		transformationMap.put(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(0, .0625, -.125).scale(.625, .625, .625).rotate(Math.toRadians(30), 1, 0, 0).rotate(Math.toRadians(130), 0, 1, 0));
		transformationMap.put(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(0, .0625, -.125).scale(.625, .625, .625).rotate(Math.toRadians(30), 1, 0, 0).rotate(Math.toRadians(130), 0, 1, 0));
		transformationMap.put(TransformType.GUI, new Matrix4().scale(1.25, 1.25, 1.25).rotate(Math.toRadians(180), 1, 0, 0).rotate(Math.toRadians(20), 0, 1, 0).rotate(Math.toRadians(-30), 0, 0, 1));
		transformationMap.put(TransformType.FIXED, new Matrix4().scale(1.5, 1.5, 1.5).rotate(Math.toRadians(180), 1, 0, 0));
		transformationMap.put(TransformType.GROUND, new Matrix4().scale(1.5, 1.5, 1.5).rotate(Math.toRadians(180), 1, 0, 0));
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
	{
		Matrix4f id = new Matrix4f();
		id.setIdentity();
		return Pair.of(this, id);
	}

	public static class RawCoresampleModel implements IModelGeometry<RawCoresampleModel>
	{
		@Override
		public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite, VertexFormat format, ItemOverrideList overrides)
		{
			return new ModelCoresample(null);
		}

		@Override
		public Collection<ResourceLocation> getTextureDependencies(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<String> missingTextureErrors)
		{
			return ImmutableList.of();
		}
	}

	public static class CoresampleLoader implements IModelLoader<RawCoresampleModel>
	{
		public static final ResourceLocation LOCATION = new ResourceLocation(ImmersiveEngineering.MODID, "models/coresample");

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager)
		{
		}

		@Override
		public RawCoresampleModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents)
		{
			return new RawCoresampleModel();
		}
	}
}