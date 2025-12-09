package express.mvp.myra.codec.runtime;

import express.mvp.myra.codec.runtime.struct.MessageHeader;
import express.mvp.roray.utils.memory.MemorySegmentPool;
import express.mvp.roray.utils.memory.SegmentBinaryWriter;
import express.mvp.roray.utils.memory.SegmentUtils;
import java.lang.foreign.MemorySegment;

/**
 * Small helper for performing forward-only, single-pass message encoding with the standard Myra
 * message header. The generator emits "builder" classes using this helper so callers never have to
 * finalize header bookkeeping manually.
 *
 * <h2>Thread Safety</h2>
 *
 * <p><b>This class is NOT thread-safe.</b> Each thread should use its own {@code MessageEncoder}
 * instance. The encoder maintains mutable state (writer position, header wrapper) that cannot be
 * safely shared across threads.
 *
 * <p>However, the underlying {@link MemorySegmentPool} may be shared across threads if it is
 * thread-safe (such as {@code LockFreeBufferPool}). In typical high-throughput scenarios:
 *
 * <ul>
 *   <li>Create one {@code MessageEncoder} per thread (or use thread-local)
 *   <li>Share a single {@code MemorySegmentPool} across all encoders
 * </ul>
 *
 * <p><b>Example pattern:</b>
 *
 * <pre>{@code
 * // Shared pool (thread-safe)
 * MemorySegmentPool pool = new LockFreeBufferPool(64 * 1024, 16);
 *
 * // Per-thread encoder
 * ThreadLocal<MessageEncoder> encoderLocal = ThreadLocal.withInitial(
 *     () -> new MessageEncoder(pool));
 *
 * // Usage in any thread
 * MessageEncoder encoder = encoderLocal.get();
 * MemorySegment segment = encoder.acquire(1024);
 * // ... encode message ...
 * }</pre>
 */
public final class MessageEncoder {

    private final MemorySegmentPool pool;
    private final SegmentBinaryWriter writer;
    private final MessageHeader header;
    private final EncoderConfig config;

    /**
     * Creates a MessageEncoder with default configuration (checksum enabled).
     *
     * @param pool The memory segment pool for allocations.
     */
    public MessageEncoder(MemorySegmentPool pool) {
        this(pool, EncoderConfig.DEFAULT);
    }

    /**
     * Creates a MessageEncoder with the specified configuration.
     *
     * @param pool The memory segment pool for allocations.
     * @param config The encoder configuration controlling checksum behavior.
     */
    public MessageEncoder(MemorySegmentPool pool, EncoderConfig config) {
        this.pool = pool;
        this.config = config;
        this.writer = new SegmentBinaryWriter();
        this.header = new MessageHeader();
    }

    /**
     * Acquire a fresh MemorySegment from the configured pool. Callers must treat the returned
     * segment as write-once. When finished, call {@link #finalizeMessage(MemorySegment, short,
     * short)} to finish the header and compute the checksum.
     */
    public MemorySegment acquire(int minCapacityBytes) {
        return pool.acquire(minCapacityBytes);
    }

    /**
     * Provides the writer bound to the given target segment. This lets generated code append
     * content at the correct offsets.
     */
    public SegmentBinaryWriter getWriter(MemorySegment target) {
        return writer.wrap(target);
    }

    /**
     * Finalize the encoded message: write header length, template id, schema version, flags and
     * checksum (if enabled). This is a convenience helper used by generated builders to keep the
     * encoding flow minimal.
     *
     * @param target The MemorySegment containing the encoded payload.
     * @param templateId The message template id.
     * @param schemaVersion The schema version to place into the header.
     * @return The final frame length in bytes (including header).
     */
    public long finalizeMessage(MemorySegment target, short templateId, short schemaVersion) {
        header.wrap(target, 0);
        header.setTemplateId(templateId);
        header.setSchemaVersion(schemaVersion);
        header.setFlags((byte) 0);

        // Frame length was advanced via writer; read back and write.
        long frameLength = writer.position();
        header.setFrameLength((int) frameLength);

        // CRC covers the payload area after header (if checksum is enabled)
        if (config.isChecksumEnabled()) {
            MemorySegment payload =
                    target.asSlice(
                            MessageHeader.HEADER_SIZE, frameLength - MessageHeader.HEADER_SIZE);
            header.setChecksum(SegmentUtils.calculateCrc32(payload));
        } else {
            header.setChecksum(0);
        }

        return frameLength;
    }

    /**
     * Returns the encoder configuration.
     *
     * @return The encoder configuration.
     */
    public EncoderConfig config() {
        return this.config;
    }

    /**
     * Access the underlying pool used for allocations. Generated builders can use this to create a
     * `PooledSegment` to return ownership of the allocated segment back to callers.
     */
    public MemorySegmentPool pool() {
        return this.pool;
    }
}
