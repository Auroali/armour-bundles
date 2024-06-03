package com.auroali.armourbundles.items;

import com.auroali.armourbundles.ArmourProfile;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public record Profiles(List<ArmourProfile> profiles) {
    public static Profiles create(int size) {
        return new Profiles(DefaultedList.ofSize(size, new ArmourProfile()));
    }

    public Profiles with(int index, ArmourProfile profile) {
        if(index >= profiles.size())
            throw new IndexOutOfBoundsException();

        DefaultedList<ArmourProfile> newProfiles = DefaultedList.ofSize(profiles.size(), new ArmourProfile());
        for(int i = 0; i < profiles.size(); i++) {
            newProfiles.set(i, profiles.get(i));
        }

        newProfiles.set(index, profile);
        return new Profiles(newProfiles);
    }
}
