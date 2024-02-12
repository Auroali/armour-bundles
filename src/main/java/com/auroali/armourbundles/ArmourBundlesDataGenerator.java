package com.auroali.armourbundles;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;

public class ArmourBundlesDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		fabricDataGenerator.addProvider(new APLangGen(fabricDataGenerator));
		fabricDataGenerator.addProvider(new APModelGen(fabricDataGenerator));
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
}
