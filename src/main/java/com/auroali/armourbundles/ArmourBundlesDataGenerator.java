package com.auroali.armourbundles;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ArmourBundlesDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(ABLangGen::new);
		pack.addProvider(ABModelGen::new);
		pack.addProvider(ABRecipeGenerator::new);
		pack.addProvider(ABTagGenerator::new);
	}

	public static class ABLangGen extends FabricLanguageProvider {
		protected ABLangGen(FabricDataOutput dataGenerator, CompletableFuture< RegistryWrapper.WrapperLookup > lookup) {
			super(dataGenerator, lookup);
		}

		@Override
		public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
			translationBuilder.add(ArmourBundles.ARMOUR_BUNDLE, "Armor Bundle");
			translationBuilder.add("item.armourprofiles.armour_bundle.profile_set", "Set profile %d!");
			translationBuilder.add("item.armourprofiles.armour_bundle.profile_selected", "Selected profile %d!");
			translationBuilder.add("key.armourprofiles.select.1", "Equip Profile 1");
			translationBuilder.add("key.armourprofiles.select.2", "Equip Profile 2");
			translationBuilder.add("key.armourprofiles.select.3", "Equip Profile 3");
			translationBuilder.add("category.armourprofiles.profiles", "Armor Profiles");
		}
	}

	public static class ABModelGen extends FabricModelProvider {
		public ABModelGen(FabricDataOutput dataGenerator) {
			super(dataGenerator);
		}

		@Override
		public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {

		}

		@Override
		public void generateItemModels(ItemModelGenerator itemModelGenerator) {
			itemModelGenerator.register(ArmourBundles.ARMOUR_BUNDLE, "_filled", Models.GENERATED);
		}
	}

	public static class ABRecipeGenerator extends FabricRecipeProvider {
		public ABRecipeGenerator(FabricDataOutput dataGenerator, CompletableFuture<RegistryWrapper.WrapperLookup> lookup) {
			super(dataGenerator, lookup);
		}

		@Override
		public void generate(RecipeExporter exporter) {
			ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, ArmourBundles.ARMOUR_BUNDLE)
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

	public static class ABTagGenerator extends FabricTagProvider<Item> {
		public ABTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(output, RegistryKeys.ITEM, registriesFuture);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup arg) {
			getOrCreateTagBuilder(ArmourBundles.VALID_ARMOUR_BUNDLE_ITEMS)
					.add(Items.ELYTRA)
					.add(Items.CARVED_PUMPKIN);
		}
	}
}
