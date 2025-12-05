package express.mvp.myra.codec.runtime;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Tests for {@link EncoderConfig}. */
class EncoderConfigTest {

    @Test
    void default_ShouldHaveChecksumEnabled() {
        assertTrue(EncoderConfig.DEFAULT.isChecksumEnabled());
    }

    @Test
    void highPerformance_ShouldHaveChecksumDisabled() {
        assertFalse(EncoderConfig.HIGH_PERFORMANCE.isChecksumEnabled());
    }

    @Test
    void builder_DefaultShouldHaveChecksumEnabled() {
        EncoderConfig config = EncoderConfig.builder().build();
        assertTrue(config.isChecksumEnabled());
    }

    @Test
    void builder_ShouldAllowDisablingChecksum() {
        EncoderConfig config = EncoderConfig.builder().checksumEnabled(false).build();
        assertFalse(config.isChecksumEnabled());
    }

    @Test
    void builder_ShouldAllowEnablingChecksum() {
        EncoderConfig config = EncoderConfig.builder().checksumEnabled(true).build();
        assertTrue(config.isChecksumEnabled());
    }

    @Test
    void equals_ShouldReturnTrueForSameValues() {
        EncoderConfig config1 = EncoderConfig.builder().checksumEnabled(true).build();
        EncoderConfig config2 = EncoderConfig.builder().checksumEnabled(true).build();
        assertEquals(config1, config2);
    }

    @Test
    void equals_ShouldReturnFalseForDifferentValues() {
        EncoderConfig config1 = EncoderConfig.builder().checksumEnabled(true).build();
        EncoderConfig config2 = EncoderConfig.builder().checksumEnabled(false).build();
        assertNotEquals(config1, config2);
    }

    @Test
    void equals_ShouldReturnTrueForSameInstance() {
        EncoderConfig config = EncoderConfig.DEFAULT;
        assertEquals(config, config);
    }

    @Test
    void equals_ShouldReturnFalseForNull() {
        assertNotEquals(null, EncoderConfig.DEFAULT);
    }

    @Test
    void equals_ShouldReturnFalseForDifferentType() {
        assertNotEquals("not a config", EncoderConfig.DEFAULT);
    }

    @Test
    void hashCode_ShouldBeConsistentWithEquals() {
        EncoderConfig config1 = EncoderConfig.builder().checksumEnabled(true).build();
        EncoderConfig config2 = EncoderConfig.builder().checksumEnabled(true).build();
        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void hashCode_ShouldBeDifferentForDifferentValues() {
        EncoderConfig config1 = EncoderConfig.builder().checksumEnabled(true).build();
        EncoderConfig config2 = EncoderConfig.builder().checksumEnabled(false).build();
        assertNotEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void toString_ShouldContainChecksumEnabledValue() {
        EncoderConfig enabled = EncoderConfig.builder().checksumEnabled(true).build();
        EncoderConfig disabled = EncoderConfig.builder().checksumEnabled(false).build();

        assertTrue(enabled.toString().contains("checksumEnabled=true"));
        assertTrue(disabled.toString().contains("checksumEnabled=false"));
    }

    @Test
    void builder_ShouldSupportChaining() {
        // Verify builder chaining returns the same builder instance
        EncoderConfig.Builder builder = EncoderConfig.builder();
        assertSame(builder, builder.checksumEnabled(true));
    }
}
