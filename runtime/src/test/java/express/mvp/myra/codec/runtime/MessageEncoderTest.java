package express.mvp.myra.codec.runtime;

import static org.junit.jupiter.api.Assertions.*;

import express.mvp.myra.codec.runtime.struct.MessageHeader;
import express.mvp.roray.ffm.utils.memory.MemorySegmentPool;
import express.mvp.roray.ffm.utils.memory.SegmentBinaryWriter;
import express.mvp.roray.ffm.utils.memory.SegmentUtils;
import java.lang.foreign.MemorySegment;
import org.junit.jupiter.api.Test;

class MessageEncoderTest {

    @Test
    void finalizeMessage_ShouldWriteHeaderAndChecksum() {
        MemorySegmentPool pool = new MemorySegmentPool(256, 1, 2);
        MessageEncoder encoder = new MessageEncoder(pool);
        MemorySegment segment = encoder.acquire(256);

        SegmentBinaryWriter writer = encoder.getWriter(segment);
        writer.position(MessageHeader.HEADER_SIZE);
        writer.writeIntBE(42);

        long frameLength = encoder.finalizeMessage(segment, (short) 9, (short) 3);

        MessageHeader header = new MessageHeader();
        header.wrap(segment, 0);
        assertEquals(MessageHeader.HEADER_SIZE + 4, frameLength);
        assertEquals(frameLength, header.getFrameLength());
        assertEquals(9, header.getTemplateId());
        assertEquals(3, header.getSchemaVersion());
        assertEquals(0, header.getFlags());
        assertNotEquals(0, header.getChecksum());
    }

    @Test
    void getWriter_ShouldResetPositionOnWrap() {
        MemorySegmentPool pool = new MemorySegmentPool(128, 1, 2);
        MessageEncoder encoder = new MessageEncoder(pool);
        MemorySegment segment = encoder.acquire(64);

        SegmentBinaryWriter writer = encoder.getWriter(segment);
        writer.position(24);
        SegmentBinaryWriter reused = encoder.getWriter(segment);

        assertSame(writer, reused);
        assertEquals(0, reused.position());
    }

    // --- New tests for EncoderConfig and checksum configuration ---

    @Test
    void constructor_WithDefaultConfig_ShouldEnableChecksum() {
        MemorySegmentPool pool = new MemorySegmentPool(256, 1, 2);
        MessageEncoder encoder = new MessageEncoder(pool);

        assertTrue(encoder.config().isChecksumEnabled());
    }

    @Test
    void constructor_WithExplicitConfig_ShouldUseProvidedConfig() {
        MemorySegmentPool pool = new MemorySegmentPool(256, 1, 2);
        EncoderConfig config = EncoderConfig.builder().checksumEnabled(false).build();
        MessageEncoder encoder = new MessageEncoder(pool, config);

        assertFalse(encoder.config().isChecksumEnabled());
    }

    @Test
    void finalizeMessage_WithChecksumDisabled_ShouldSetChecksumToZero() {
        MemorySegmentPool pool = new MemorySegmentPool(256, 1, 2);
        EncoderConfig config = EncoderConfig.builder().checksumEnabled(false).build();
        MessageEncoder encoder = new MessageEncoder(pool, config);
        MemorySegment segment = encoder.acquire(256);

        SegmentBinaryWriter writer = encoder.getWriter(segment);
        writer.position(MessageHeader.HEADER_SIZE);
        writer.writeIntBE(42);
        writer.writeLongBE(123456789L);

        encoder.finalizeMessage(segment, (short) 9, (short) 3);

        MessageHeader header = new MessageHeader();
        header.wrap(segment, 0);
        assertEquals(0, header.getChecksum(), "Checksum should be 0 when disabled");
    }

    @Test
    void finalizeMessage_WithChecksumEnabled_ShouldComputeValidChecksum() {
        MemorySegmentPool pool = new MemorySegmentPool(256, 1, 2);
        EncoderConfig config = EncoderConfig.builder().checksumEnabled(true).build();
        MessageEncoder encoder = new MessageEncoder(pool, config);
        MemorySegment segment = encoder.acquire(256);

        SegmentBinaryWriter writer = encoder.getWriter(segment);
        writer.position(MessageHeader.HEADER_SIZE);
        writer.writeIntBE(42);
        writer.writeLongBE(123456789L);

        long frameLength = encoder.finalizeMessage(segment, (short) 9, (short) 3);

        MessageHeader header = new MessageHeader();
        header.wrap(segment, 0);

        // Verify checksum is non-zero and matches computed value
        assertNotEquals(0, header.getChecksum());
        MemorySegment payload =
                segment.asSlice(MessageHeader.HEADER_SIZE, frameLength - MessageHeader.HEADER_SIZE);
        int expectedChecksum = SegmentUtils.calculateCrc32(payload);
        assertEquals(expectedChecksum, header.getChecksum());
    }

    @Test
    void finalizeMessage_WithHighPerformanceConfig_ShouldDisableChecksum() {
        MemorySegmentPool pool = new MemorySegmentPool(256, 1, 2);
        MessageEncoder encoder = new MessageEncoder(pool, EncoderConfig.HIGH_PERFORMANCE);
        MemorySegment segment = encoder.acquire(256);

        SegmentBinaryWriter writer = encoder.getWriter(segment);
        writer.position(MessageHeader.HEADER_SIZE);
        writer.writeIntBE(42);

        encoder.finalizeMessage(segment, (short) 9, (short) 3);

        MessageHeader header = new MessageHeader();
        header.wrap(segment, 0);
        assertEquals(0, header.getChecksum());
    }

    @Test
    void config_ShouldReturnSameInstanceProvided() {
        MemorySegmentPool pool = new MemorySegmentPool(256, 1, 2);
        EncoderConfig config = EncoderConfig.builder().checksumEnabled(false).build();
        MessageEncoder encoder = new MessageEncoder(pool, config);

        assertSame(config, encoder.config());
    }
}
