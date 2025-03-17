package de.czymm.serversigns.utils;

import org.bukkit.Material;

import java.util.EnumSet;

public class MaterialUtils {
    private MaterialUtils() {}

    public static EnumSet<Material> getMaterials(String... materials) {
        EnumSet<Material> enumSet = EnumSet.noneOf(Material.class);

        for(int i = 0; i < materials.length; i++) {
            Material mat = Material.getMaterial(materials[i]);
            if (mat != null)
                enumSet.add(mat);
        }

        return enumSet;
    }
}
