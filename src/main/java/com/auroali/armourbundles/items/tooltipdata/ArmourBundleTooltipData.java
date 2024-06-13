package com.auroali.armourbundles.items.tooltipdata;

import com.auroali.armourbundles.items.ArmourBundleInventory;
import net.minecraft.item.tooltip.TooltipData;

public record ArmourBundleTooltipData(ArmourBundleInventory inventory) implements TooltipData {
}
