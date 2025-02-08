package com.auroali.armourbundles.datafixer;

import com.auroali.armourbundles.datafixer.fixers.ArmourBundleReworkDatafix;
import com.auroali.dfuhooks.v1.api.DFUHooksSchemaHook;
import com.auroali.dfuhooks.v1.api.SchemaRegistry;
import com.mojang.datafixers.DataFixerBuilder;

public class ABDatafixer implements DFUHooksSchemaHook {
    @Override
    public void register(SchemaRegistry registry) {

    }

    @Override
    public void modifySchemas(DataFixerBuilder builder, SchemaGetter schemas) {
        schemas.fromVersion(4081).ifPresent(schema -> {
            builder.addFixer(new ArmourBundleReworkDatafix(schema, false));
        });
    }
}
