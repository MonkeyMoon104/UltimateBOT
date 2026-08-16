package com.monkey.ultimatebot.compat;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.bukkit.entity.Arrow;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ArrowPickupAccessTest {

    @Test
    void disallowPickupDoesNotThrowOnMockArrow() {
        Arrow arrow = Mockito.mock(Arrow.class);
        assertThatCode(() -> ArrowPickupAccess.disallowPickup(arrow)).doesNotThrowAnyException();
    }
}
