/**
 * Myra Codec Runtime - Core classes for high-performance message encoding and decoding.
 *
 * <h2>Thread Safety Summary</h2>
 *
 * <table border="1" cellpadding="4">
 *   <tr>
 *     <th>Class</th>
 *     <th>Thread-Safe?</th>
 *     <th>Notes</th>
 *   </tr>
 *   <tr>
 *     <td>{@link express.mvp.myra.codec.runtime.EncoderConfig}</td>
 *     <td>✅ Yes</td>
 *     <td>Immutable. Share freely.</td>
 *   </tr>
 *   <tr>
 *     <td>{@link express.mvp.myra.codec.runtime.MessageEncoder}</td>
 *     <td>❌ No</td>
 *     <td>Use one per thread (or ThreadLocal).</td>
 *   </tr>
 *   <tr>
 *     <td>{@link express.mvp.myra.codec.runtime.PooledSegment}</td>
 *     <td>❌ No</td>
 *     <td>Single-owner until closed.</td>
 *   </tr>
 *   <tr>
 *     <td>{@link express.mvp.myra.codec.runtime.struct.MessageHeader}</td>
 *     <td>❌ No</td>
 *     <td>Flyweight pattern, reuse via wrap().</td>
 *   </tr>
 *   <tr>
 *     <td>{@link express.mvp.myra.codec.runtime.struct.RepeatingGroupIterator}</td>
 *     <td>❌ No</td>
 *     <td>Flyweight pattern, reuse via wrap().</td>
 *   </tr>
 *   <tr>
 *     <td>{@link express.mvp.myra.codec.runtime.struct.RepeatingGroupBuilder}</td>
 *     <td>❌ No</td>
 *     <td>Single-threaded builder pattern.</td>
 *   </tr>
 *   <tr>
 *     <td>{@link express.mvp.myra.codec.runtime.struct.VariableSizeRepeatingGroupIterator}</td>
 *     <td>❌ No</td>
 *     <td>Flyweight pattern, reuse via wrap().</td>
 *   </tr>
 *   <tr>
 *     <td>{@link express.mvp.myra.codec.runtime.struct.VariableSizeRepeatingGroupBuilder}</td>
 *     <td>❌ No</td>
 *     <td>Single-threaded builder pattern.</td>
 *   </tr>
 * </table>
 *
 * <h2>Recommended Patterns</h2>
 *
 * <h3>High-Throughput Encoding (Multiple Threads)</h3>
 * <pre>{@code
 * // Shared across all threads (thread-safe pool)
 * MemorySegmentPool pool = new LockFreeBufferPool(64 * 1024, 16);
 * EncoderConfig config = EncoderConfig.HIGH_PERFORMANCE;
 *
 * // Per-thread encoder
 * ThreadLocal<MessageEncoder> encoderLocal = ThreadLocal.withInitial(
 *     () -> new MessageEncoder(pool, config));
 *
 * // In each thread:
 * MessageEncoder encoder = encoderLocal.get();
 * MemorySegment segment = encoder.acquire(1024);
 * // ... encode ...
 * }</pre>
 *
 * <h3>Decoding with Flyweight Reuse</h3>
 * <pre>{@code
 * // Per-thread flyweights
 * MessageHeader header = new MessageHeader();
 * MyMessageFlyweight message = new MyMessageFlyweight();
 *
 * // Process multiple messages in a loop
 * while (hasMoreData()) {
 *     header.wrap(segment, offset);
 *     message.wrap(segment, offset + MessageHeader.HEADER_SIZE);
 *     process(message);
 *     offset += header.getFrameLength();
 * }
 * }</pre>
 *
 * <h3>Generated Builders and Flyweights</h3>
 * <p>All generated message flyweights and builders follow the same thread safety rules:
 * <ul>
 *   <li><b>Flyweights:</b> Not thread-safe, reuse via {@code wrap()}, zero allocation</li>
 *   <li><b>Builders:</b> Not thread-safe, use one per encoding operation</li>
 * </ul>
 *
 * @see express.mvp.myra.codec.runtime.MessageEncoder
 * @see express.mvp.myra.codec.runtime.struct.MessageHeader
 */
package express.mvp.myra.codec.runtime;
