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
        assertThat(MaterialCatalog.available("THIS_MATERIAL_DOES_NOT_EXIST_XYZ"))
                .isFalse();
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
    void goldenSwordAliasGroupResolvesBidirectionally() {
        Material fromModern = MaterialCatalog.optional("GOLDEN_SWORD", Material.IRON_SWORD);
        Material fromLegacy = MaterialCatalog.optional("GOLD_SWORD", Material.IRON_SWORD);
        assertThat(fromModern).isNotNull().isNotEqualTo(Material.IRON_SWORD);
        assertThat(fromLegacy).isEqualTo(fromModern);
    }

    @Test
    void ofNameBlankUsesFallback() {
        assertThat(MaterialCatalog.ofName("  ", Material.STONE)).isEqualTo(Material.STONE);
        assertThat(MaterialCatalog.ofName(null, Material.STONE)).isEqualTo(Material.STONE);
    }

    @Test
    void grassAliasGroupResolvesBidirectionally() {

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
        Material resolved = MaterialCatalog.optional("SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
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
        assertThat(MaterialCatalog.is(Material.STICK, "THIS_MATERIAL_DOES_NOT_EXIST_XYZ"))
                .isFalse();
    }

    @Test
    void clockAliasGroupResolvesToWatchOrClock() {
        Material resolved = MaterialCatalog.optional("CLOCK", Material.STONE);
        Material clock = Material.matchMaterial("CLOCK");
        Material watch = Material.matchMaterial("WATCH");
        if (clock != null) {
            assertThat(resolved).isEqualTo(clock);
        } else if (watch != null) {
            assertThat(resolved).isEqualTo(watch);
        } else {
            assertThat(resolved).isEqualTo(Material.STONE);
        }
    }

    @Test
    void totemAliasGroupResolvesToTotemOrTotemOfUndying() {
        Material resolved = MaterialCatalog.optional("TOTEM_OF_UNDYING", Material.GOLDEN_APPLE);
        Material modern = Material.matchMaterial("TOTEM_OF_UNDYING");
        Material legacy = Material.matchMaterial("TOTEM");
        if (modern != null) {
            assertThat(resolved).isEqualTo(modern);
        } else if (legacy != null) {
            assertThat(resolved).isEqualTo(legacy);
        } else {
            assertThat(resolved).isEqualTo(Material.GOLDEN_APPLE);
        }
    }

    @Test
    void cobwebAliasGroupResolvesToWebOrCobweb() {
        Material resolved = MaterialCatalog.optional("COBWEB", Material.STRING);
        Material modern = Material.matchMaterial("COBWEB");
        Material legacy = Material.matchMaterial("WEB");
        if (modern != null) {
            assertThat(resolved).isEqualTo(modern);
        } else if (legacy != null) {
            assertThat(resolved).isEqualTo(legacy);
        } else {
            assertThat(resolved).isEqualTo(Material.STRING);
        }
    }

    @Test
    void alwaysPresentOn117PlusAreAvailableOnCompileTarget() {

        assertThat(MaterialCatalog.available("TOTEM_OF_UNDYING") || MaterialCatalog.available("TOTEM"))
                .isTrue();
        assertThat(MaterialCatalog.available("END_CRYSTAL")).isTrue();
    }
}
