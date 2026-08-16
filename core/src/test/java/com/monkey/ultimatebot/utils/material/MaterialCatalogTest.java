package com.monkey.ultimatebot.utils.material;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class MaterialCatalogTest {

    @Test
    void optionalReturnsMatchWhenPresent() {
        assertThat(MaterialCatalog.optional("DIAMOND_SWORD", Material.STICK)).isEqualTo(Material.DIAMOND_SWORD);
    }

    @Test
    void optionalFallsBackWhenMissing() {
        assertThat(MaterialCatalog.optional("THIS_MATERIAL_DOES_NOT_EXIST_XYZ", Material.STICK))
                .isEqualTo(Material.STICK);
    }

    @Test
    void availableIsFalseForUnknown() {
        assertThat(MaterialCatalog.available("THIS_MATERIAL_DOES_NOT_EXIST_XYZ")).isFalse();
        assertThat(MaterialCatalog.available("DIAMOND_SWORD")).isTrue();
    }

    @Test
    void requireThrowsWhenUnresolved() {
        assertThatThrownBy(() -> MaterialCatalog.require("THIS_MATERIAL_DOES_NOT_EXIST_XYZ"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown material");
    }

    @Test
    void requireUsesTableFallbackForMaceWhenNeeded() {
        Material resolved = MaterialCatalog.require("MACE");
        Material mace = Material.matchMaterial("MACE");
        if (mace != null) {
            assertThat(resolved).isEqualTo(mace);
        } else {
            assertThat(resolved).isEqualTo(Material.DIAMOND_SWORD);
        }
    }

    @Test
    void requireUsesTableFallbackForCrossbowWhenNeeded() {
        Material resolved = MaterialCatalog.require("CROSSBOW");
        Material crossbow = Material.matchMaterial("CROSSBOW");
        if (crossbow != null) {
            assertThat(resolved).isEqualTo(crossbow);
        } else {
            assertThat(resolved).isEqualTo(Material.BOW);
        }
    }

    @Test
    void ofNameBlankUsesFallback() {
        assertThat(MaterialCatalog.ofName("  ", Material.STONE)).isEqualTo(Material.STONE);
        assertThat(MaterialCatalog.ofName(null, Material.STONE)).isEqualTo(Material.STONE);
    }

    @Test
    void grassAliasGroupResolvesBidirectionally() {
        // On 1.20.3+ GRASS was renamed to SHORT_GRASS; either request must resolve.
        Material fromLegacy = MaterialCatalog.optional("GRASS", Material.STONE);
        Material fromModern = MaterialCatalog.optional("SHORT_GRASS", Material.STONE);
        assertThat(fromLegacy).isNotNull().isNotEqualTo(Material.STONE);
        assertThat(fromModern).isEqualTo(fromLegacy);
    }

    @Test
    void piglinHeadOptionalUsesZombieFallbackWhenAbsent() {
        Material resolved = MaterialCatalog.optional("PIGLIN_HEAD", Material.ZOMBIE_HEAD);
        Material piglin = Material.matchMaterial("PIGLIN_HEAD");
        if (piglin != null) {
            assertThat(resolved).isEqualTo(piglin);
        } else {
            assertThat(resolved).isEqualTo(Material.ZOMBIE_HEAD);
        }
    }

    @Test
    void trimTemplateSuffixFallsBackToGunpowderWhenAbsent() {
        Material resolved =
                MaterialCatalog.optional("SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
        Material trim = Material.matchMaterial("SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE");
        if (trim != null) {
            assertThat(resolved).isEqualTo(trim);
        } else {
            assertThat(resolved).isEqualTo(Material.GUNPOWDER);
        }
    }

    @Test
    void isComparesByNameWithoutRequiringEnumField() {
        assertThat(MaterialCatalog.is(Material.DIAMOND_SWORD, "DIAMOND_SWORD")).isTrue();
        assertThat(MaterialCatalog.is(Material.STICK, "DIAMOND_SWORD")).isFalse();
        assertThat(MaterialCatalog.is(Material.STICK, "THIS_MATERIAL_DOES_NOT_EXIST_XYZ")).isFalse();
    }

    @Test
    void alwaysPresentOn117PlusAreAvailableOnCompileTarget() {
        // Sanity on this compile target; older runtimes resolve via optional()/is() instead of enum fields.
        assertThat(MaterialCatalog.available("TOTEM_OF_UNDYING")).isTrue();
        assertThat(MaterialCatalog.available("END_CRYSTAL")).isTrue();
    }
}
