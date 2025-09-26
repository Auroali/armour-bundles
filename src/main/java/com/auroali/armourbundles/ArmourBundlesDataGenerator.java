package com.auroali.armourbundles;

import com.auroali.armourbundles.client.render.item.model.ArmourBundleSelectedItemModel;
import com.auroali.armourbundles.client.render.item.property.ArmourBundleHasSelectedItemProperty;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.ItemModels;
import net.minecraft.client.data.Models;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.property.select.DisplayContextProperty;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ArmourBundlesDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ABLangGen::new);
        pack.addProvider(ABModelGen::new);
        pack.addProvider(ABRecipeProvider::new);
        pack.addProvider(ABTagGenerator::new);
    }

    public static class ABLangGen extends FabricLanguageProvider {
        protected ABLangGen(FabricDataOutput dataGenerator, CompletableFuture<RegistryWrapper.WrapperLookup> lookup) {
            super(dataGenerator, lookup);
        }

        @Override
        public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
            translationBuilder.add(ArmourBundles.ARMOUR_BUNDLE, "Armor Bundle");
            translationBuilder.add("item.armourprofiles.armour_bundle.profile_set", "Set profile %d!");
            translationBuilder.add("item.armourprofiles.armour_bundle.profile_selected", "Selected profile %d!");
            translationBuilder.add("item.armourprofiles.armour_bundle.current_profile", "Profile %d/%d");
            translationBuilder.add("key.armourprofiles.equip_prev", "Equip Previous");
            translationBuilder.add("key.armourprofiles.equip_next", "Equip Next");
            translationBuilder.add("key.category.armourprofiles.keybinds", "Armor Bundles");
            translationBuilder.add(ArmourBundles.VALID_ARMOUR_BUNDLE_ITEMS, "Armor Bundle Insertables");
            translationBuilder.add("item.armourbundles.armourbundle.empty.description", "Can hold 4 pieces of armor");
            translationBuilder.add("item.armourbundles.armourbundle.bound.description", "Bound Armor");
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
            ItemModel.Unbaked regular = ItemModels.basic(itemModelGenerator.upload(ArmourBundles.ARMOUR_BUNDLE, Models.GENERATED));
            Identifier back = itemModelGenerator.uploadOpenBundleModel(ArmourBundles.ARMOUR_BUNDLE, Models.TEMPLATE_BUNDLE_OPEN_BACK, "_open_back");
            Identifier front = itemModelGenerator.uploadOpenBundleModel(ArmourBundles.ARMOUR_BUNDLE, Models.TEMPLATE_BUNDLE_OPEN_FRONT, "_open_front");
            // represents the inventory model
            ItemModel.Unbaked composite = ItemModels.composite(
              ItemModels.basic(back),
              new ArmourBundleSelectedItemModel.Unbaked(),
              ItemModels.basic(front)
            );

            itemModelGenerator.output.accept(
              ArmourBundles.ARMOUR_BUNDLE,
              ItemModels.select(
                new DisplayContextProperty(),
                regular,
                ItemModels.switchCase(
                  ItemDisplayContext.GUI,
                  ItemModels.condition(new ArmourBundleHasSelectedItemProperty(), composite, regular)
                )
              )
            );
        }
    }

    public static class ABRecipeProvider extends FabricRecipeProvider {
        public ABRecipeProvider(FabricDataOutput dataGenerator, CompletableFuture<RegistryWrapper.WrapperLookup> lookup) {
            super(dataGenerator, lookup);
        }

        @Override
        public RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup reg, RecipeExporter exporter) {
            return new ABRecipeGenerator(reg, exporter);
        }

        @Override
        public String getName() {
            return "Armour Bundles Recipe";
        }
    }

    public static class ABRecipeGenerator extends RecipeGenerator {
        RegistryEntryLookup<Item> lookup;

        public ABRecipeGenerator(RegistryWrapper.WrapperLookup lookup, RecipeExporter exporter) {
            super(lookup, exporter);
            this.lookup = lookup.getOrThrow(RegistryKeys.ITEM);
        }

        @Override
        public void generate() {
            ShapedRecipeJsonBuilder.create(lookup, RecipeCategory.COMBAT, ArmourBundles.ARMOUR_BUNDLE)
              .criterion(hasItem(Items.STRING), conditionsFromItem(Items.STRING))
              .criterion(hasItem(Items.NETHERITE_INGOT), conditionsFromItem(Items.NETHERITE_INGOT))
              .criterion(hasItem(Items.RABBIT_HIDE), conditionsFromItem(Items.RABBIT_HIDE))
              .pattern("S")
              .pattern("R")
              .pattern("N")
              .input('R', Items.RABBIT_HIDE)
              .input('N', Items.NETHERITE_INGOT)
              .input('S', Items.STRING)
              .offerTo(exporter);
        }
    }

    public static class ABTagGenerator extends FabricTagProvider.ItemTagProvider {
        public ABTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup arg) {
            this.valueLookupBuilder(ArmourBundles.VALID_ARMOUR_BUNDLE_ITEMS)
              .add(
                Items.ELYTRA,
                Items.CARVED_PUMPKIN
              )
              .forceAddTag(ItemTags.CHEST_ARMOR)
              .forceAddTag(ItemTags.FOOT_ARMOR)
              .forceAddTag(ItemTags.LEG_ARMOR)
              .forceAddTag(ItemTags.HEAD_ARMOR)
              .forceAddTag(ItemTags.SKULLS);
        }
    }
}
