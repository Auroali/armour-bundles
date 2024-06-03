package com.auroali.armourbundles.items.tooltipdata;

import com.auroali.armourbundles.items.ArmourBundleInventory;
import net.minecraft.client.item.TooltipData;

public record ArmourBundleTooltipData(ArmourBundleInventory inventory) implements TooltipData {
}
