package com.monkey.ultimatebot.access;

import static org.assertj.core.api.Assertions.assertThat;

import com.monkey.ultimatebot.access.entity.VelocityAccess;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

class VelocityAccessTest {

    @Test
    void finiteVectorsAreUnchanged() {
        Vector velocity = new Vector(0.32D, -0.08D, 0.1D);
        assertThat(VelocityAccess.finite(velocity)).isSameAs(velocity);
    }

    @Test
    void nonFiniteComponentsBecomeZero() {
        Vector sanitized = VelocityAccess.finite(new Vector(Double.NaN, 0.4D, Double.POSITIVE_INFINITY));
        assertThat(sanitized.getX()).isZero();
        assertThat(sanitized.getY()).isEqualTo(0.4D);
        assertThat(sanitized.getZ()).isZero();
    }

    @Test
    void zeroNormalizeDoesNotStayNaN() {
        Vector zero = new Vector();
        zero.normalize();
        Vector sanitized = VelocityAccess.finite(zero);
        assertThat(Double.isFinite(sanitized.getX())).isTrue();
        assertThat(Double.isFinite(sanitized.getY())).isTrue();
        assertThat(Double.isFinite(sanitized.getZ())).isTrue();
    }
}
