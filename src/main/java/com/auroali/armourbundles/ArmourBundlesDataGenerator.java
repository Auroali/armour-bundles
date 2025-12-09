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
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
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
        protected ABLangGen(FabricDataOutput dataGenerator, CompletableFuture<HolderLookup.Provider> lookup) {
            super(dataGenerator, lookup);
        }

        @Override
        public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
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
        public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {

        }

        @Override
        public void generateItemModels(ItemModelGenerators itemModelGenerator) {
            ItemModel.Unbaked regular = ItemModelUtils.plainModel(itemModelGenerator.createFlatItemModel(ArmourBundles.ARMOUR_BUNDLE, ModelTemplates.FLAT_ITEM));
            Identifier back = itemModelGenerator.generateBundleCoverModel(ArmourBundles.ARMOUR_BUNDLE, ModelTemplates.BUNDLE_OPEN_BACK_INVENTORY, "_open_back");
            Identifier front = itemModelGenerator.generateBundleCoverModel(ArmourBundles.ARMOUR_BUNDLE, ModelTemplates.BUNDLE_OPEN_FRONT_INVENTORY, "_open_front");
            // represents the inventory model
            ItemModel.Unbaked composite = ItemModelUtils.composite(
              ItemModelUtils.plainModel(back),
              new ArmourBundleSelectedItemModel.Unbaked(),
              ItemModelUtils.plainModel(front)
            );

            itemModelGenerator.itemModelOutput.accept(
              ArmourBundles.ARMOUR_BUNDLE,
              ItemModelUtils.select(
                new DisplayContext(),
                regular,
                ItemModelUtils.when(
                  ItemDisplayContext.GUI,
                  ItemModelUtils.conditional(new ArmourBundleHasSelectedItemProperty(), composite, regular)
                )
              )
            );
        }
    }

    public static class ABRecipeProvider extends FabricRecipeProvider {
        public ABRecipeProvider(FabricDataOutput dataGenerator, CompletableFuture<HolderLookup.Provider> lookup) {
            super(dataGenerator, lookup);
        }

        @Override
        public RecipeProvider createRecipeProvider(HolderLookup.Provider reg, RecipeOutput exporter) {
            return new ABRecipeGenerator(reg, exporter);
        }

        @Override
        public String getName() {
            return "Armour Bundles Recipe";
        }
    }

    public static class ABRecipeGenerator extends RecipeProvider {
        HolderGetter<Item> lookup;

        public ABRecipeGenerator(HolderLookup.Provider lookup, RecipeOutput exporter) {
            super(lookup, exporter);
            this.lookup = lookup.lookupOrThrow(Registries.ITEM);
        }

        @Override
        public void buildRecipes() {
            ShapedRecipeBuilder.shaped(lookup, RecipeCategory.COMBAT, ArmourBundles.ARMOUR_BUNDLE)
              .unlockedBy(getHasName(Items.STRING), has(Items.STRING))
              .unlockedBy(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
              .unlockedBy(getHasName(Items.RABBIT_HIDE), has(Items.RABBIT_HIDE))
              .pattern("S")
              .pattern("R")
              .pattern("N")
              .define('R', Items.RABBIT_HIDE)
              .define('N', Items.NETHERITE_INGOT)
              .define('S', Items.STRING)
              .save(output);
        }
    }

    public static class ABTagGenerator extends FabricTagProvider.ItemTagProvider {
        public ABTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider arg) {
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
