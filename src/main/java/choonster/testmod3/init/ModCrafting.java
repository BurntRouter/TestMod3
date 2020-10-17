package choonster.testmod3.init;

import choonster.testmod3.TestMod3;
import choonster.testmod3.crafting.ingredient.ConditionalIngredientSerializer;
import choonster.testmod3.crafting.ingredient.FilledUniversalBucketIngredientSerializer;
import choonster.testmod3.crafting.ingredient.IngredientNever;
import choonster.testmod3.crafting.ingredient.MobSpawnerIngredientSerializer;
import choonster.testmod3.crafting.recipe.ShapedArmourUpgradeRecipe;
import choonster.testmod3.crafting.recipe.ShapelessCuttingRecipe;
import choonster.testmod3.util.LogUtil;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.potion.Potions;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.NBTIngredient;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Predicate;

import static choonster.testmod3.util.InjectionUtil.Null;

/**
 * Manages this mod's recipes and ingredients and removes recipes.
 */
public class ModCrafting {

	@Mod.EventBusSubscriber(modid = TestMod3.MODID, bus = Bus.MOD)
	public static class Brewing {
		private static final Logger LOGGER = LogManager.getLogger();

		private static final Method ADD_MIX = ObfuscationReflectionHelper.findMethod(PotionBrewing.class, "func_193357_a" /* addMix */, Potion.class, Item.class, Potion.class);

		/**
		 * Add this mod's brewing recipes.
		 *
		 * @param event The common setup event
		 */
		@SubscribeEvent
		public static void registerBrewingRecipes(final FMLCommonSetupEvent event) {
			addStandardConversionRecipes(ModPotions.TEST, ModPotions.LONG_TEST, ModPotions.STRONG_TEST, ModItems.ARROW);
		}

		/**
		 * Add the standard conversion recipes for the specified {@link Potion}s:
		 * <ul>
		 * <li>Awkward + Ingredient = Standard</li>
		 * <li>Standard + Redstone = Long</li>
		 * <li>Standard + Glowstone = Strong</li>
		 * </ul>
		 *
		 * @param standardPotionType The standard PotionType
		 * @param longPotionType     The long PotionType
		 * @param strongPotionType   The strong PotionType
		 * @param ingredient         The ingredient
		 */
		private static void addStandardConversionRecipes(final Potion standardPotionType, final Potion longPotionType, final Potion strongPotionType, final Item ingredient) {
			try {
				ADD_MIX.invoke(null, Potions.AWKWARD, ingredient, standardPotionType);
				ADD_MIX.invoke(null, standardPotionType, Items.REDSTONE, longPotionType);
				ADD_MIX.invoke(null, standardPotionType, Items.GLOWSTONE_DUST, strongPotionType);
			} catch (final IllegalAccessException | InvocationTargetException e) {
				LogUtil.error(LOGGER, e, "Failed to add potion recipes for potion type {}/ingredient item {}", standardPotionType.getRegistryName(), ingredient.getRegistryName());
			}
		}
	}

	@SuppressWarnings("unused")
	public static class Ingredients {
		public static final IIngredientSerializer<Ingredient> CONDITIONAL = CraftingHelper.register(new ResourceLocation(TestMod3.MODID, "conditional"), new ConditionalIngredientSerializer());
		public static final IIngredientSerializer<NBTIngredient> FILLED_UNIVERSAL_BUCKET = CraftingHelper.register(new ResourceLocation(TestMod3.MODID, "filled_universal_bucket"), new FilledUniversalBucketIngredientSerializer());
		public static final IIngredientSerializer<NBTIngredient> MOB_SPAWNER = CraftingHelper.register(new ResourceLocation(TestMod3.MODID, "mob_spawner"), new MobSpawnerIngredientSerializer());
		public static final IIngredientSerializer<IngredientNever> NEVER = CraftingHelper.register(new ResourceLocation(TestMod3.MODID, "never"), new IngredientNever.Serializer());

		public static void register() {
			// No-op method to ensure that this class is loaded and its static initialisers are run
		}
	}

	@SuppressWarnings("unused")
	@ObjectHolder(TestMod3.MODID)
	@Mod.EventBusSubscriber(modid = TestMod3.MODID, bus = Bus.MOD)
	public static class Recipes {
		public static final IRecipeSerializer<ShapedArmourUpgradeRecipe> ARMOUR_UPGRADE_SHAPED = Null();
		public static final IRecipeSerializer<ShapelessCuttingRecipe> CUTTING_SHAPELESS = Null();

		@SubscribeEvent
		public static void register(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
			event.getRegistry().registerAll(
					new ShapedArmourUpgradeRecipe.Serializer().setRegistryName(new ResourceLocation(TestMod3.MODID, "armour_upgrade_shaped")),
					new ShapelessCuttingRecipe.Serializer().setRegistryName(new ResourceLocation(TestMod3.MODID, "cutting_shapeless"))
			);
		}
	}

	@Mod.EventBusSubscriber(modid = TestMod3.MODID)
	public static class RecipeRemover {
		private static final Logger LOGGER = LogManager.getLogger();

		private static final Field RECIPES = ObfuscationReflectionHelper.findField(RecipeManager.class, "field_199522_d" /* recipes */);

		/**
		 * Removes recipes from the server's recipe manager when it starts up.
		 *
		 * @param event The server starting event
		 */
		@SubscribeEvent
		public static void removeRecipes(final FMLServerStartedEvent event) {
			final RecipeManager recipeManager = event.getServer().getRecipeManager();
			removeRecipes(recipeManager, FireworkRocketRecipe.class);
			removeRecipes(recipeManager, FireworkStarRecipe.class);
			removeRecipes(recipeManager, FireworkStarFadeRecipe.class);
			removeRecipes(recipeManager, ModTags.Items.VANILLA_DYES);
			removeRecipes(recipeManager, ModTags.Items.VANILLA_TERRACOTTA);
		}

		/**
		 * Removes all crafting recipes with an output item contained in the specified tag.
		 *
		 * @param recipeManager The recipe manager
		 * @param tag           The tag
		 */
		private static void removeRecipes(final RecipeManager recipeManager, final ITag.INamedTag<Item> tag) {
			final int recipesRemoved = removeRecipes(recipeManager, recipe -> {
				final ItemStack recipeOutput = recipe.getRecipeOutput();
				return !recipeOutput.isEmpty() && recipeOutput.getItem().isIn(tag);
			});

			LOGGER.info("Removed {} recipe(s) for tag {}", recipesRemoved, tag.getName());
		}

		/**
		 * Remove all crafting recipes that are instances of the specified class.
		 * <p>
		 * Test for this thread:
		 * https://www.minecraftforge.net/forum/topic/33420-removing-vanilla-recipes/
		 *
		 * @param recipeManager The recipe manager
		 * @param recipeClass   The recipe class
		 */
		private static void removeRecipes(final RecipeManager recipeManager, final Class<? extends IRecipe> recipeClass) {
			final int recipesRemoved = removeRecipes(recipeManager, recipeClass::isInstance);

			LOGGER.info("Removed {} recipe(s) for class {}", recipesRemoved, recipeClass);
		}

		/**
		 * Remove all crafting recipes that match the specified predicate.
		 *
		 * @param recipeManager The recipe manager
		 * @param predicate     The predicate
		 * @return The number of recipes removed
		 */
		private static int removeRecipes(final RecipeManager recipeManager, final Predicate<IRecipe> predicate) {
			final Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> existingRecipes;
			try {
				@SuppressWarnings("unchecked")
				final Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipesMap = (Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>>) RECIPES.get(recipeManager);
				existingRecipes = recipesMap;
			} catch (final IllegalAccessException e) {
				throw new RuntimeException("Couldn't get recipes map while removing recipes", e);
			}

			final Object2IntMap<IRecipeType<?>> removedCounts = new Object2IntOpenHashMap<>();
			final ImmutableMap.Builder<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> newRecipes = ImmutableMap.builder();

			// For each recipe type, create a new map that doesn't contain the recipes to be removed
			existingRecipes.forEach((recipeType, existingRecipesForType) -> {
				//noinspection UnstableApiUsage
				final ImmutableMap<ResourceLocation, IRecipe<?>> newRecipesForType = existingRecipesForType.entrySet()
						.stream()
						.filter(entry -> !predicate.test(entry.getValue()))
						.collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

				removedCounts.put(recipeType, existingRecipesForType.size() - newRecipesForType.size());
				newRecipes.put(recipeType, newRecipesForType);
			});

			final int removedCount = removedCounts.values().stream().reduce(0, Integer::sum);

			try {
				RECIPES.set(recipeManager, newRecipes.build());
			} catch (final IllegalAccessException e) {
				throw new RuntimeException("Couldn't replace recipes map while removing recipes", e);
			}

			return removedCount;
		}
	}
}
