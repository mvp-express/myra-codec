package express.mvp.myra.codec.runtime.struct;

import static org.junit.jupiter.api.Assertions.*;

import express.mvp.roray.ffm.utils.memory.SegmentBinaryWriter;
import java.lang.foreign.MemorySegment;
import org.junit.jupiter.api.Test;

/** Tests for {@link MessageHeader} flyweight behavior. */
class MessageHeaderTest {

    @Test
    void wrapAndAccessors_ShouldRoundTripValues() {
        MessageHeader header = new MessageHeader();
        assertFalse(header.isWrapped());

        MemorySegment segment = MemorySegment.ofArray(new byte[MessageHeader.HEADER_SIZE]);
        header.wrap(segment, 0);

        header.setFrameLength(128);
        header.setTemplateId((short) 42);
        header.setSchemaVersion((short) 7);
        header.setFlags((byte) 0x5A);
        header.setChecksum(0xCAFE_BABE);

        assertTrue(header.isWrapped());
        assertEquals(128, header.getFrameLength());
        assertEquals(42, header.getTemplateId());
        assertEquals(7, header.getSchemaVersion());
        assertEquals((byte) 0x5A, header.getFlags());
        assertEquals(0xCAFE_BABE, header.getChecksum());
    }

    @Test
    void writeTo_ShouldCopyHeaderFields() {
        MemorySegment source = MemorySegment.ofArray(new byte[MessageHeader.HEADER_SIZE]);
        MessageHeader header = new MessageHeader();
        header.wrap(source, 0);
        header.setFrameLength(256);
        header.setTemplateId((short) 17);
        header.setSchemaVersion((short) 2);
        header.setFlags((byte) 0x0F);
        header.setChecksum(0xDEAD_BEEF);

        MemorySegment target = MemorySegment.ofArray(new byte[MessageHeader.HEADER_SIZE]);
        SegmentBinaryWriter writer = new SegmentBinaryWriter();
        writer.wrap(target);
        header.writeTo(writer);

        MessageHeader copied = new MessageHeader();
        copied.wrap(target, 0);
        assertEquals(header.getFrameLength(), copied.getFrameLength());
        assertEquals(header.getTemplateId(), copied.getTemplateId());
        assertEquals(header.getSchemaVersion(), copied.getSchemaVersion());
        assertEquals(header.getFlags(), copied.getFlags());
        assertEquals(header.getChecksum(), copied.getChecksum());
    }
}
