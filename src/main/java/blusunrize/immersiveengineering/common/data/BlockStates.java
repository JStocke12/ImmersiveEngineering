/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.cloth.StripCurtainBlock;
import blusunrize.immersiveengineering.common.blocks.generic.IEFenceBlock;
import blusunrize.immersiveengineering.common.blocks.generic.PostBlock;
import blusunrize.immersiveengineering.common.blocks.generic.WallmountBlock;
import blusunrize.immersiveengineering.common.blocks.generic.WallmountBlock.Orientation;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.data.Models.MetalModels;
import blusunrize.immersiveengineering.common.data.blockstate.BlockstateGenerator;
import blusunrize.immersiveengineering.common.data.blockstate.VariantBlockstate.Builder;
import blusunrize.immersiveengineering.common.data.model.ModelFile;
import blusunrize.immersiveengineering.common.data.model.ModelFile.ExistingModelFile;
import blusunrize.immersiveengineering.common.data.model.ModelFile.UncheckedModelFile;
import blusunrize.immersiveengineering.common.data.model.ModelHelper.BasicStairsShape;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.common.data.IEDataGenerator.rl;

public class BlockStates extends BlockstateGenerator
{
	private static final ConfiguredModel EMPTY_MODEL = new ConfiguredModel(new ExistingModelFile(rl("block/ie_empty")));
	private final Models models;

	public BlockStates(DataGenerator gen, Models models)
	{
		super(gen);
		this.models = models;
	}

	@Override
	protected void registerStates(BiConsumer<Block, IVariantModelGenerator> variantBased, BiConsumer<Block, List<MultiPart>> multipartBased)
	{
		for(EnumMetals m : EnumMetals.values())
		{
			MetalModels metalModels = models.metalModels.get(m);
			if(!m.isVanillaMetal())
			{
				if(m.shouldAddOre())
					createBasicBlock(Metals.ores.get(m), metalModels.ore, variantBased);
				createBasicBlock(Metals.storage.get(m), metalModels.storage, variantBased);
			}
			createBasicBlock(Metals.sheetmetal.get(m), metalModels.sheetmetal, variantBased);
		}
		createFenceBlock(WoodenDecoration.treatedFence, models.treatedFencePost, models.treatedFenceSide, multipartBased);
		createFenceBlock(MetalDecoration.steelFence, models.steelFencePost, models.steelFenceSide, multipartBased);
		createFenceBlock(MetalDecoration.aluFence, models.aluFencePost, models.aluFenceSide, multipartBased);
		for(Entry<Block, ModelFile> entry : models.simpleBlocks.entrySet())
			createBasicBlock(entry.getKey(), entry.getValue(), variantBased);
		for(Entry<Block, Map<SlabType, ModelFile>> entry : models.slabs.entrySet())
			createSlabBlock(entry.getKey(), entry.getValue(), SlabBlock.TYPE, variantBased);
		for(Entry<Block, Map<BasicStairsShape, ModelFile>> entry : models.stairs.entrySet())
			createStairsBlock(entry.getKey(), entry.getValue(), StairsBlock.FACING, StairsBlock.HALF, StairsBlock.SHAPE, variantBased);

		createMultiblock(Multiblocks.excavator, new ExistingModelFile(rl("block/metal_multiblock/excavator.obj")),
				new ExistingModelFile(rl("block/metal_multiblock/excavator_mirrored.obj")),
				variantBased);
		createMultiblock(Multiblocks.crusher, new ExistingModelFile(rl("block/metal_multiblock/crusher_mirrored.obj")),
				new ExistingModelFile(rl("block/metal_multiblock/crusher.obj")),
				variantBased);
		createMultiblock(Multiblocks.metalPress, new ExistingModelFile(rl("block/metal_multiblock/metal_press.obj")), variantBased);
		createMultiblock(Multiblocks.assembler, new ExistingModelFile(rl("block/metal_multiblock/assembler.obj")), variantBased);
		{
			IVariantModelGenerator gen = new Builder(Multiblocks.bucketWheel)
					.setForAllWithState(ImmutableMap.of(), EMPTY_MODEL)
					.build();
			variantBased.accept(Multiblocks.bucketWheel, gen);
		}
		createMultiblock(Multiblocks.arcFurnace, new ExistingModelFile(rl("block/metal_multiblock/arc_furnace.obj")),
				new ExistingModelFile(rl("block/metal_multiblock/arc_furnace_mirrored.obj")), variantBased);

		createMultiblock(Multiblocks.blastFurnaceAdv, new ExistingModelFile(rl("block/blastfurnace_advanced.obj")), variantBased);
		createMultiblock(Multiblocks.cokeOven, models.cokeOvenOff, models.cokeOvenOn, IEProperties.MULTIBLOCKSLAVE,
				IEProperties.FACING_HORIZONTAL, IEProperties.ACTIVE, 180, variantBased);
		createMultiblock(Multiblocks.alloySmelter, models.alloySmelterOff, models.alloySmelterOn, IEProperties.MULTIBLOCKSLAVE,
				IEProperties.FACING_HORIZONTAL, IEProperties.ACTIVE, 180, variantBased);
		createMultiblock(Multiblocks.blastFurnace, models.blastFurnaceOff, models.blastFurnaceOn, IEProperties.MULTIBLOCKSLAVE,
				IEProperties.FACING_HORIZONTAL, IEProperties.ACTIVE, 180, variantBased);
		createMultiblock(Multiblocks.silo, new ExistingModelFile(rl("block/metal_multiblock/silo.obj")), variantBased);
		createMultiblock(Multiblocks.tank, new ExistingModelFile(rl("block/metal_multiblock/tank.obj")), variantBased);
		createMultiblock(Multiblocks.bottlingMachine,
				new ExistingModelFile(rl("block/metal_multiblock/bottling_machine.obj")),
				new ExistingModelFile(rl("block/metal_multiblock/bottling_machine_mirrored.obj")), variantBased);
		createMultiblock(Multiblocks.fermenter,
				new ExistingModelFile(rl("block/metal_multiblock/fermenter.obj")),
				new ExistingModelFile(rl("block/metal_multiblock/fermenter_mirrored.obj")), variantBased);
		createMultiblock(Multiblocks.squeezer,
				new ExistingModelFile(rl("block/metal_multiblock/squeezer.obj")),
				new ExistingModelFile(rl("block/metal_multiblock/squeezer_mirrored.obj")), variantBased);
		createMultiblock(Multiblocks.mixer,
				new ExistingModelFile(rl("block/metal_multiblock/mixer.obj")),
				new ExistingModelFile(rl("block/metal_multiblock/mixer_mirrored.obj")), variantBased);
		createMultiblock(Multiblocks.refinery,
				new ExistingModelFile(rl("block/metal_multiblock/refinery.obj")),
				new ExistingModelFile(rl("block/metal_multiblock/refinery_mirrored.obj")), variantBased);
		createMultiblock(Multiblocks.dieselGenerator,
				new ExistingModelFile(rl("block/metal_multiblock/diesel_generator.obj")),
				new ExistingModelFile(rl("block/metal_multiblock/diesel_generator_mirrored.obj")), variantBased);
		createMultiblock(Multiblocks.lightningrod,
				new ExistingModelFile(rl("block/metal_multiblock/lightningrod.obj")), variantBased);
		createMultiblock(WoodenDevices.workbench, new ExistingModelFile(rl("block/wooden_device/workbench.obj.ie")),
				null, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL, null, 180,
				variantBased);
		createMultiblock(MetalDevices.sampleDrill, new ExistingModelFile(rl("block/metal_device/core_drill.obj")),
				null, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL, null, 180,
				variantBased);
		createBasicBlock(MetalDevices.fluidPipe, new ExistingModelFile(rl("block/metal_device/fluid_pipe.obj.ie")),
				variantBased);
		createConnector(
				MetalDevices.floodlight,
				rl("block/metal_device/floodlight.obj.ie"),
				ImmutableMap.of(),
				variantBased,
				BlockRenderLayer.TRANSLUCENT, BlockRenderLayer.SOLID
		);
		createMultiblock(
				MetalDevices.turretChem,
				new ExistingModelFile(rl("block/metal_device/chem_turret.obj.ie")),
				variantBased
		);
		createMultiblock(
				MetalDevices.turretGun,
				new ExistingModelFile(rl("block/metal_device/gun_turret.obj.ie")),
				variantBased
		);
		createBasicBlock(Misc.fakeLight, EMPTY_MODEL, variantBased);

		createPostBlock(MetalDecoration.aluPost, new ExistingModelFile(rl("block/wooden_device/wooden_post.obj.ie")),
				rl("block/metal_decoration/aluminum_post"),
				variantBased);
		createPostBlock(MetalDecoration.steelPost, new ExistingModelFile(rl("block/wooden_device/wooden_post.obj.ie")),
				rl("block/metal_decoration/steel_post"),
				variantBased);
		createPostBlock(WoodenDecoration.treatedPost, new ExistingModelFile(rl("block/wooden_device/wooden_post.obj.ie")),
				rl("block/wooden_decoration/post"),
				variantBased);
		createMultistateSingleModel(WoodenDevices.windmill, EMPTY_MODEL, variantBased);
		createMultistateSingleModel(WoodenDevices.watermill, EMPTY_MODEL, variantBased);
		createMultistateSingleModel(MetalDecoration.lantern,
				new ConfiguredModel(new ExistingModelFile(rl("block/lantern.obj.ie"))),
				variantBased);

		createDirectionalBlock(MetalDecoration.metalLadder.get(CoverType.NONE), IEProperties.FACING_HORIZONTAL,
				models.metalLadderNone, variantBased);
		createDirectionalBlock(MetalDecoration.metalLadder.get(CoverType.ALU), IEProperties.FACING_HORIZONTAL,
				models.metalLadderAlu, variantBased);
		createDirectionalBlock(MetalDecoration.metalLadder.get(CoverType.STEEL), IEProperties.FACING_HORIZONTAL,
				models.metalLadderSteel, variantBased);

		createWallmount(WoodenDevices.treatedWallmount, rl("block/wooden_device/wallmount"), variantBased);
		createWallmount(MetalDecoration.aluWallmount, rl("block/metal_decoration/aluminum_wallmount"), variantBased);
		createWallmount(MetalDecoration.steelWallmount, rl("block/metal_decoration/steel_wallmount"), variantBased);
		createMultistateSingleModel(
				MetalDecoration.slopeSteel,
				new ConfiguredModel(new ExistingModelFile(rl("block/slope.obj.ie"))),
				variantBased
		);
		createMultistateSingleModel(
				MetalDecoration.slopeAlu,
				new ConfiguredModel(
						new ExistingModelFile(rl("block/slope.obj.ie")),
						0,
						0,
						true,
						ImmutableMap.of(),
						ImmutableMap.of(
								MODID+":block/metal_decoration/steel_scaffolding",
								MODID+":block/metal_decoration/aluminum_scaffolding"
						)
				),
				variantBased
		);

		createConnector(Connectors.getEnergyConnector(WireType.LV_CATEGORY, false), rl("block/connector/connector_lv.obj"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.SOLID);
		createConnector(Connectors.getEnergyConnector(WireType.LV_CATEGORY, true), rl("block/connector/connector_lv.obj"),
				ImmutableMap.of("#immersiveengineering:block/connector/connector_lv", "immersiveengineering:block/connector/relay_lv"),
				variantBased, BlockRenderLayer.SOLID);

		createConnector(Connectors.getEnergyConnector(WireType.MV_CATEGORY, false), rl("block/connector/connector_mv.obj"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.SOLID);
		createConnector(Connectors.getEnergyConnector(WireType.MV_CATEGORY, true), rl("block/connector/connector_mv.obj"),
				ImmutableMap.of("#immersiveengineering:block/connector/connector_mv", "immersiveengineering:block/connector/relay_mv"),
				variantBased, BlockRenderLayer.SOLID);

		createConnector(Connectors.getEnergyConnector(WireType.HV_CATEGORY, false), rl("block/connector/connector_hv.obj"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.SOLID);
		createConnector(Connectors.getEnergyConnector(WireType.HV_CATEGORY, true), rl("block/connector/relay_hv.obj"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.TRANSLUCENT);

		createConnector(Connectors.connectorStructural, rl("block/connector/connector_structural.obj.ie"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.SOLID);
		createConnector(Connectors.connectorRedstone, rl("block/connector/connector_redstone.obj.ie"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.SOLID);
		createConnector(Connectors.connectorProbe, rl("block/connector/connector_probe.obj.ie"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.CUTOUT, BlockRenderLayer.TRANSLUCENT);
		createConnector(Connectors.feedthrough, rl("block/smartmodel/feedthrough"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.SOLID);
		createConnector(MetalDevices.electricLantern, state -> rl("block/metal_device/e_lantern.obj"),
				state -> {
					if(state.get(IEProperties.ACTIVE)==Boolean.FALSE)
						return ImmutableMap.of();
					else
						return ImmutableMap.of(
								"#"+MODID+":block/metal_device/electric_lantern", MODID+":block/metal_device/electric_lantern_on"
						);
				},
				variantBased, ImmutableList.of(IEProperties.ACTIVE));

		createConnector(Connectors.redstoneBreaker, rl("block/connector/redstone_breaker.obj.ie"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.SOLID);
		createConnector(Connectors.breakerswitch, map -> {
			if(map.get(IEProperties.ACTIVE)==Boolean.FALSE)
				return rl("block/connector/breaker_switch_off.obj.ie");
			else
				return rl("block/connector/breaker_switch_on.obj.ie");
		}, ImmutableMap.of(), variantBased, ImmutableList.of(IEProperties.ACTIVE), BlockRenderLayer.SOLID);
		createConnector(Connectors.currentTransformer, map -> {
			if(map.get(IEProperties.MULTIBLOCKSLAVE)==Boolean.TRUE)
				return rl("block/connector/e_meter.obj");
			else
				return EMPTY_MODEL.name.getLocation();
		}, ImmutableMap.of(), variantBased, ImmutableList.of(IEProperties.MULTIBLOCKSLAVE), BlockRenderLayer.SOLID);
		createConnector(MetalDevices.razorWire, rl("block/razor_wire.obj.ie"), ImmutableMap.of(), variantBased);

		createRotatedBlock(StoneDecoration.coresample, map -> new ExistingModelFile(rl("block/coresample.obj")),
				IEProperties.FACING_HORIZONTAL, ImmutableList.of(), variantBased);
		createBasicBlock(StoneDecoration.concreteSheet, models.sheetConcreteBlock, variantBased);
		createBasicBlock(StoneDecoration.concreteQuarter, models.quarterConcreteBlock, variantBased);
		createBasicBlock(StoneDecoration.concreteThreeQuarter, models.threeQuarterConcreteBlock, variantBased);
		createBasicBlock(StoneDecoration.concreteSprayed, new ExistingModelFile(rl("block/sprayed_concrete.obj")),
				variantBased);

		createBasicBlock(WoodenDevices.crate, models.crate, variantBased);
		createBasicBlock(WoodenDevices.reinforcedCrate, models.reinforcedCrate, variantBased);
		createMultistateSingleModel(WoodenDevices.gunpowderBarrel, new ConfiguredModel(models.gunpowderBarrel),
				variantBased);
		createBasicBlock(WoodenDevices.sorter, models.router, variantBased);
		createBasicBlock(WoodenDevices.fluidSorter, models.fluidRouter, variantBased);
		createBasicBlock(WoodenDevices.woodenBarrel,
				new UncheckedModelFile(rl("smartmodel/conf_sides_v_wooden_device/barrel")), variantBased);

		createConnector(Cloth.balloon, map -> rl("block/balloon.obj.ie"), ImmutableMap.of(), variantBased,
				ImmutableList.of(), BlockRenderLayer.SOLID);
		createRotatedBlock(Cloth.curtain,
				state -> new ExistingModelFile(rl(
						state.get(StripCurtainBlock.CEILING_ATTACHED)==Boolean.FALSE?
								"block/stripcurtain":
								"block/stripcurtain_middle"
				)), IEProperties.FACING_HORIZONTAL, ImmutableList.of(StripCurtainBlock.CEILING_ATTACHED),
				variantBased);
		createBasicBlock(Cloth.cushion, models.cushion, variantBased);
		createMultistateSingleModel(Cloth.shaderBanner, EMPTY_MODEL, variantBased);

		createBasicBlock(MetalDevices.barrel,
				new UncheckedModelFile(rl("smartmodel/conf_sides_v_metal_device/barrel")), variantBased);
		for(Entry<Block, String> cap : ImmutableMap.of(
				MetalDevices.capacitorCreative, "creative",
				MetalDevices.capacitorLV, "lv",
				MetalDevices.capacitorMV, "mv",
				MetalDevices.capacitorHV, "hv"
		).entrySet())
			createBasicBlock(cap.getKey(),
					new UncheckedModelFile(rl("smartmodel/conf_sides_hud_metal_device/capacitor_"+cap.getValue())),
					variantBased);
		createMultiblock(MetalDevices.blastFurnacePreheater,
				new ExistingModelFile(rl("block/metal_device/blastfurnace_preheater.obj")),
				variantBased);
		createRotatedBlock(MetalDevices.furnaceHeater, props -> {
			if(props.get(IEProperties.ACTIVE)==Boolean.TRUE)
				return models.furnaceHeaterOn;
			else
				return models.furnaceHeaterOff;
		}, IEProperties.FACING_ALL, ImmutableList.of(IEProperties.ACTIVE), variantBased);
		createRotatedBlock(MetalDevices.dynamo, state -> models.kineticDynamo, IEProperties.FACING_HORIZONTAL,
				ImmutableList.of(), variantBased);
		createBasicBlock(MetalDevices.thermoelectricGen, models.thermoelectricGen, variantBased);
		createRotatedBlock(MetalDevices.chargingStation,
				//TODO glass
				state -> new ExistingModelFile(rl("block/metal_device/charging_station.obj")),
				IEProperties.FACING_HORIZONTAL,
				ImmutableList.of(),
				variantBased
		);
		for(Block b : MetalDevices.CONVEYORS.values())
			createMultistateSingleModel(b, new ConfiguredModel(new UncheckedModelFile(rl("conveyor"))), variantBased);
	}

	private void createRotatedBlock(Block block, Function<Map<IProperty<?>, Object>, ModelFile> model, IProperty<Direction> facing,
									List<IProperty<?>> additionalProps,
									BiConsumer<Block, IVariantModelGenerator> out)
	{
		Builder builder = new Builder(block);
		forEachState(additionalProps, state -> {
			ImmutableMap<String, Object> additional;
			ModelFile modelLoc = model.apply(state);
			if(modelLoc.getLocation().getPath().contains(".obj"))
				additional = ImmutableMap.of("flip-v", true);
			else
				additional = ImmutableMap.of();
			Map<IProperty<?>, Object> baseState = new HashMap<>();
			for(Entry<IProperty<?>, Object> e : state.entrySet())
				baseState.put(e.getKey(), e.getValue());
			for(Direction d : facing.getAllowedValues())
			{
				int x;
				int y;
				switch(d)
				{
					case UP:
						x = 90;
						y = 0;
						break;
					case DOWN:
						x = -90;
						y = 0;
						break;
					default:
						y = getAngle(d, 180);
						x = 0;
				}
				ConfiguredModel configuredModel = new ConfiguredModel(modelLoc, x, y, true,
						additional);

				builder.setForAllWithState(with(baseState, facing, d), configuredModel);
			}
		});
		out.accept(block, builder.build());
	}

	private void createBasicBlock(Block block, ModelFile model, BiConsumer<Block, IVariantModelGenerator> out)
	{
		ConfiguredModel configuredModel = new ConfiguredModel(model);
		createBasicBlock(block, configuredModel, out);
	}

	private void createBasicBlock(Block block, ConfiguredModel model, BiConsumer<Block, IVariantModelGenerator> out)
	{
		IVariantModelGenerator gen = new Builder(block)
				.setModel(block.getDefaultState(), model)
				.build();
		out.accept(block, gen);
	}

	private void createMultistateSingleModel(Block block, ConfiguredModel model, BiConsumer<Block, IVariantModelGenerator> out)
	{
		IVariantModelGenerator gen = new Builder(block)
				.setForAllMatching(state -> true, model)
				.build();
		out.accept(block, gen);
	}

	private void createSlabBlock(Block block, Map<SlabType, ModelFile> baseModels, EnumProperty<SlabType> typeProp, BiConsumer<Block, IVariantModelGenerator> out)
	{
		Builder b = new Builder(block);
		for(SlabType type : SlabType.values())
		{
			Map<IProperty<?>, Object> partialState = ImmutableMap.<IProperty<?>, Object>builder()
					.put(typeProp, type)
					.build();
			b.setForAllWithState(partialState, new ConfiguredModel(baseModels.get(type)));
		}
		out.accept(block, b.build());
	}

	private void createStairsBlock(Block block, Map<BasicStairsShape, ModelFile> baseModels, EnumProperty<Direction> facingProp,
								   EnumProperty<Half> halfProp, EnumProperty<StairsShape> shapeProp, BiConsumer<Block, IVariantModelGenerator> out)
	{
		Builder b = new Builder(block);
		for(Direction dir : Direction.BY_HORIZONTAL_INDEX)
		{
			for(Half half : Half.values())
			{
				for(StairsShape shape : StairsShape.values())
				{
					Map<IProperty<?>, Object> partialState = ImmutableMap.<IProperty<?>, Object>builder()
							.put(facingProp, dir)
							.put(halfProp, half)
							.put(shapeProp, shape)
							.build();
					ModelFile base = baseModels.get(BasicStairsShape.toBasicShape(shape));
					int xRot = 0;
					if(half==Half.TOP)
						xRot = 180;
					int yRot = getAngle(dir, 90);
					if(shape==StairsShape.INNER_LEFT||shape==StairsShape.OUTER_LEFT)
						yRot = (yRot+270)%360;
					b.setForAllWithState(partialState, new ConfiguredModel(base, xRot, yRot, true, ImmutableMap.of()));
				}
			}
		}
		out.accept(block, b.build());
	}

	private void createFenceBlock(IEFenceBlock block, ModelFile post, ModelFile side, BiConsumer<Block, List<MultiPart>> out)
	{
		List<MultiPart> parts = new ArrayList<>();
		ConfiguredModel postModel = new ConfiguredModel(post, 0, 0, false, ImmutableMap.of());
		parts.add(new MultiPart(postModel, false));
		for(Direction dir : Direction.BY_HORIZONTAL_INDEX)
		{
			int angle = getAngle(dir, 180);
			ConfiguredModel sideModel = new ConfiguredModel(side, 0, angle, true, ImmutableMap.of());
			BooleanProperty sideActive = block.getFacingStateMap().get(dir);
			parts.add(new MultiPart(sideModel, false, new PropertyWithValues<>(sideActive, true)));
		}
		out.accept(block, parts);
	}

	private void createMultiblock(Block b, ModelFile masterModel, ModelFile mirroredModel, int rotationOffset,
								  BiConsumer<Block, IVariantModelGenerator> out)
	{
		createMultiblock(b, masterModel, mirroredModel, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL, IEProperties.MIRRORED, rotationOffset, out);
	}

	private void createMultiblock(Block b, ModelFile masterModel, ModelFile mirroredModel,
								  BiConsumer<Block, IVariantModelGenerator> out)
	{
		createMultiblock(b, masterModel, mirroredModel, 180, out);
	}

	private void createMultiblock(Block b, ModelFile masterModel, BiConsumer<Block, IVariantModelGenerator> out)
	{
		createMultiblock(b, masterModel, null, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL, null, 180, out);
	}

	private void createMultiblock(Block b, ModelFile masterModel, @Nullable ModelFile mirroredModel, IProperty<Boolean> isSlave,
								  EnumProperty<Direction> facing, @Nullable IProperty<Boolean> mirroredState, int rotationOffset,
								  BiConsumer<Block, IVariantModelGenerator> out)
	{
		Preconditions.checkArgument((mirroredModel==null)==(mirroredState==null));
		Builder builder = new Builder(b);
		builder.setForAllWithState(ImmutableMap.of(isSlave, true), EMPTY_MODEL);
		boolean[] possibleMirrorStates;
		if(mirroredState!=null)
			possibleMirrorStates = new boolean[]{false, true};
		else
			possibleMirrorStates = new boolean[1];
		for(boolean mirrored : possibleMirrorStates)
			for(Direction dir : Direction.BY_HORIZONTAL_INDEX)
			{
				int angle = getAngle(dir, rotationOffset);
				ModelFile model = mirrored?mirroredModel: masterModel;
				ImmutableMap.Builder<IProperty<?>, Object> partialState = ImmutableMap.builder();
				partialState.put(isSlave, Boolean.FALSE)
						.put(facing, dir);
				if(mirroredState!=null)
					partialState.put(mirroredState, mirrored);
				builder.setForAllWithState(partialState.build(),
						new ConfiguredModel(model, 0, angle, true, ImmutableMap.of("flip-v", true)));
			}
		out.accept(b, builder.build());
	}

	private void createPostBlock(Block b, ModelFile masterModel, ResourceLocation texture, BiConsumer<Block, IVariantModelGenerator> out)
	{
		Builder builder = new Builder(b);
		for(int i : PostBlock.POST_SLAVE.getAllowedValues())
			if(i!=0)
				builder.setForAllWithState(ImmutableMap.of(PostBlock.POST_SLAVE, i), EMPTY_MODEL);
		builder.setForAllWithState(ImmutableMap.of(PostBlock.POST_SLAVE, 0),
				new ConfiguredModel(masterModel, 0, 0, true, ImmutableMap.of("flip-v", true),
						ImmutableMap.of("#immersiveengineering:block/wooden_decoration/post", texture.toString())));
		out.accept(b, builder.build());
	}

	private int getAngle(Direction dir, int offset)
	{
		return (int)((dir.getHorizontalAngle()+offset)%360);
	}

	private void createDirectionalBlock(Block b, IProperty<Direction> prop, ModelFile model, BiConsumer<Block, IVariantModelGenerator> out)
	{
		Builder builder = new Builder(b);
		for(Direction d : Direction.BY_HORIZONTAL_INDEX)
			builder.setForAllWithState(ImmutableMap.of(prop, d), new ConfiguredModel(model, 0, getAngle(d, 180),
					true, ImmutableMap.of()));
		out.accept(b, builder.build());
	}

	private void createWallmount(Block b, ResourceLocation texture, BiConsumer<Block, IVariantModelGenerator> out)
	{
		Builder builder = new Builder(b);
		for(Direction d : Direction.BY_HORIZONTAL_INDEX)
		{
			int rotation = getAngle(d, 0);
			for(WallmountBlock.Orientation or : Orientation.values())
			{
				ResourceLocation model = rl("block/wooden_device/wallmount"+or.modelSuffix()+".obj");
				builder.setForAllWithState(
						ImmutableMap.of(IEProperties.FACING_HORIZONTAL, d, WallmountBlock.ORIENTATION, or),
						new ConfiguredModel(new ExistingModelFile(model), 0, rotation, true,
								ImmutableMap.of("flip-v", true),
								ImmutableMap.of("#immersiveengineering:block/wooden_device/wallmount", texture.toString())));
			}
		}
		out.accept(b, builder.build());
	}

	private void forEachState(List<IProperty<?>> props, Consumer<Map<IProperty<?>, Object>> out)
	{
		if(props.size() > 0)
		{
			List<IProperty<?>> remaining = props.subList(1, props.size());
			IProperty<?> main = props.get(0);
			for(Object value : main.getAllowedValues())
				forEachState(remaining, map -> {
					map.put(main, value);
					out.accept(map);
				});
		}
		else
			out.accept(new HashMap<>());
	}

	private void createConnector(Block b, Function<Map<IProperty<?>, Object>, ResourceLocation> model,
								 Function<Map<IProperty<?>, Object>, ImmutableMap<String, String>> textures,
								 BiConsumer<Block, IVariantModelGenerator> out,
								 List<IProperty<?>> additional, BlockRenderLayer... layers)
	{
		final ModelFile connFile = new UncheckedModelFile(rl("connector"));
		StringBuilder layerString = new StringBuilder("[");
		for(int i = 0; i < layers.length; i++)
		{
			BlockRenderLayer l = layers[i];
			layerString.append(l.name());
			if(i+1 < layers.length)
				layerString.append(", ");
		}
		layerString.append("]");
		final IProperty<Direction> facingProp;
		final int xForHorizontal;
		if(b.getDefaultState().has(IEProperties.FACING_ALL))
		{
			facingProp = IEProperties.FACING_ALL;
			xForHorizontal = 90;
		}
		else if(b.getDefaultState().has(IEProperties.FACING_TOP_DOWN))
		{
			facingProp = IEProperties.FACING_TOP_DOWN;
			xForHorizontal = 90;
		}
		else if(b.getDefaultState().has(IEProperties.FACING_HORIZONTAL))
		{
			facingProp = IEProperties.FACING_HORIZONTAL;
			xForHorizontal = 0;
		}
		else
		{
			facingProp = null;
			xForHorizontal = 0;
		}
		Preconditions.checkState(facingProp==null||b.getDefaultState().has(facingProp),
				b+" does not have "+facingProp);
		Builder builder = new Builder(b);
		forEachState(additional, map -> {
			final ImmutableMap<String, Object> customData = ImmutableMap.of("flip-v", true,
					"base", model.apply(map).toString(),
					"layers", layerString.toString());
			if(facingProp!=null)
			{
				if(facingProp.getAllowedValues().contains(Direction.DOWN))
				{
					builder.setForAllWithState(with(map, facingProp, Direction.DOWN),
							new ConfiguredModel(connFile, xForHorizontal-90, 0, true, customData,
									textures.apply(map)));
					builder.setForAllWithState(with(map, facingProp, Direction.UP),
							new ConfiguredModel(connFile, xForHorizontal+90, 0, true, customData,
									textures.apply(map)));
				}
				for(Direction d : Direction.BY_HORIZONTAL_INDEX)
				{
					int rotation = getAngle(d, 0);
					builder.setForAllWithState(
							with(map, facingProp, d),
							new ConfiguredModel(connFile, xForHorizontal, rotation, true, customData, textures.apply(map)));
				}
			}
			else
				builder.setForAllWithState(map,
						new ConfiguredModel(connFile, 0, 0, true, customData, textures.apply(map)));
		});
		out.accept(b, builder.build());
	}


	private void createConnector(Block b, Function<Map<IProperty<?>, Object>, ResourceLocation> model,
								 ImmutableMap<String, String> textures, BiConsumer<Block, IVariantModelGenerator> out,
								 List<IProperty<?>> additional, BlockRenderLayer... layers)
	{
		createConnector(b, model, state -> textures, out, additional, layers);
	}

	private <K, V> Map<K, V> with(Map<K, V> old, K newKey, V newVal)
	{
		Map<K, V> ret = new HashMap<>(old);
		ret.put(newKey, newVal);
		return ret;
	}

	private void createConnector(Block b, ResourceLocation model, ImmutableMap<String, String> textures,
								 BiConsumer<Block, IVariantModelGenerator> out, BlockRenderLayer... layers)
	{
		createConnector(b, map -> model, textures, out, ImmutableList.of(), layers);
	}
}
