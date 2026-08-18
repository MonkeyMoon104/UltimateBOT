package com.monkey.ultimatebot.access;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.monkey.ultimatebot.access.entity.ArrowPickupAccess;
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
