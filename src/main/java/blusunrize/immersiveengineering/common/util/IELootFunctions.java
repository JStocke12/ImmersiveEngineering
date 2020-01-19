/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootFunction;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.minecraft.world.storage.loot.functions.ILootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;

import javax.annotation.Nonnull;

/**
 * @author BluSunrize - 16.08.2018
 */
public class IELootFunctions
{
	public static void preInit()
	{
		LootFunctionManager.registerFunction(new Bluprintz.Serializer());
	}

	public static class Bluprintz extends LootFunction
	{
		protected Bluprintz(ILootCondition[] conditionsIn)
		{
			super(conditionsIn);
		}

		@Override
		public ItemStack doApply(ItemStack stack, LootContext context)
		{
			stack.setDisplayName(new StringTextComponent("Super Special BluPrintz"));
			ItemNBTHelper.setLore(stack, "Congratulations!", "You have found an easter egg!");
			return stack;
		}

		public static class Serializer extends LootFunction.Serializer<Bluprintz>
		{
			protected Serializer()
			{
				super(new ResourceLocation(ImmersiveEngineering.MODID, "secret_bluprintz"), Bluprintz.class);
			}

			@Override
			public void serialize(@Nonnull JsonObject object, @Nonnull Bluprintz functionClazz, @Nonnull JsonSerializationContext serializationContext)
			{
				super.serialize(object, functionClazz, serializationContext);
			}

			@Override
			@Nonnull
			public Bluprintz deserialize(@Nonnull JsonObject object, @Nonnull JsonDeserializationContext deserializationContext, @Nonnull ILootCondition[] conditionsIn)
			{
				return new Bluprintz(conditionsIn);
			}
		}

		public static class Builder extends LootFunction.Builder<Builder>
		{

			@Override
			protected Builder doCast()
			{
				return this;
			}

			@Override
			public ILootFunction build()
			{
				return new Bluprintz(getConditions());
			}
		}

		public static Builder builder()
		{
			return new Builder();
		}
	}
}
