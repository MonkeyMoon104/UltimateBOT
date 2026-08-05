package com.monkey.ultimatebot.extension.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.monkey.ultimatebot.api.extension.combat.ModeKit;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class ModeKitTest {
    @Test
    void returnsAUniqueDefensiveCopyForEveryRead() {
        ItemStack source = mock(ItemStack.class);
        ItemStack builderCopy = mock(ItemStack.class);
        ItemStack storedCopy = mock(ItemStack.class);
        ItemStack firstRead = mock(ItemStack.class);
        ItemStack secondRead = mock(ItemStack.class);
        when(source.clone()).thenReturn(builderCopy);
        when(builderCopy.clone()).thenReturn(storedCopy);
        when(storedCopy.clone()).thenReturn(firstRead, secondRead);

        ModeKit kit = ModeKit.builder().inventoryItem(0, source).build();

        assertThat(Objects.requireNonNull(kit.inventory().get(0), "first read")).isSameAs(firstRead);
        assertThat(Objects.requireNonNull(kit.inventory().get(0), "second read"))
                .isSameAs(secondRead)
                .isNotSameAs(firstRead);
    }
}
