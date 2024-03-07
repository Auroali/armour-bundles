package com.auroali.armourbundles;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;

import java.util.function.Consumer;

public class ArmourBundlesDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		fabricDataGenerator.addProvider(new APLangGen(fabricDataGenerator));
		fabricDataGenerator.addProvider(new APModelGen(fabricDataGenerator));
		fabricDataGenerator.addProvider(new APRecipeGenerator(fabricDataGenerator));
	}

	public static class APLangGen extends FabricLanguageProvider {
		protected APLangGen(FabricDataGenerator dataGenerator) {
			super(dataGenerator);
		}

		@Override
		public void generateTranslations(TranslationBuilder translationBuilder) {
			translationBuilder.add(ArmourBundles.ARMOUR_BUNDLE, "Armor Bundle");
			translationBuilder.add("item.armourprofiles.armour_bundle.profile_set", "Set profile %d!");
			translationBuilder.add("item.armourprofiles.armour_bundle.profile_selected", "Selected profile %d!");
			translationBuilder.add("key.armourprofiles.select.1", "Equip Profile 1");
			translationBuilder.add("key.armourprofiles.select.2", "Equip Profile 2");
			translationBuilder.add("key.armourprofiles.select.3", "Equip Profile 3");
			translationBuilder.add("category.armourprofiles.profiles", "Armor Profiles");
		}
	}

	public static class APModelGen extends FabricModelProvider {
		public APModelGen(FabricDataGenerator dataGenerator) {
			super(dataGenerator);
		}

		@Override
		public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {

		}

		@Override
		public void generateItemModels(ItemModelGenerator itemModelGenerator) {
			itemModelGenerator.register(ArmourBundles.ARMOUR_BUNDLE, Models.GENERATED);
		}
	}

	public static class APRecipeGenerator extends FabricRecipeProvider {
		public APRecipeGenerator(FabricDataGenerator dataGenerator) {
			super(dataGenerator);
		}

		@Override
		public void generateRecipes(Consumer<RecipeJsonProvider> exporter) {
			ShapedRecipeJsonBuilder.create(ArmourBundles.ARMOUR_BUNDLE)
					.criterion(hasItem(Items.NETHERITE_INGOT), conditionsFromItem(Items.NETHERITE_INGOT))
					.criterion(hasItem(Items.RABBIT_HIDE), conditionsFromItem(Items.RABBIT_HIDE))
					.pattern(" N ")
					.pattern("R R")
					.pattern(" R ")
					.input('R', Items.RABBIT_HIDE)
					.input('N', Items.NETHERITE_INGOT)
					.offerTo(exporter);
		}
	}
}
