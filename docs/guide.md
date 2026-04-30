# MyraCodec Usage Guide

MyraCodec is a schema-driven binary serialization library that generates zero-copy flyweight accessors from YAML schema definitions. It is designed for high-frequency trading and other latency-critical applications where every allocation matters.

## Quick Start

### Dependencies

**Gradle (Kotlin DSL):**

```kotlin
plugins {
    id("express.mvp.myra-codegen") version "0.2.1" // Optional: Gradle plugin
}

dependencies {
    implementation("express.mvp.myra:myra-codec-runtime:0.2.1")
    implementation("express.mvp.roray:roray-ffm:0.2.1")
}
```

**Maven:**

```xml
<dependency>
    <groupId>express.mvp.myra</groupId>
    <artifactId>myra-codec-runtime</artifactId>
    <version>0.2.1</version>
</dependency>
<dependency>
    <groupId>express.mvp.roray</groupId>
    <artifactId>roray-ffm-utils</artifactId>
    <version>0.2.1</version>
</dependency>
```

### JVM Arguments

MyraCodec uses Java's Foreign Function & Memory (FFM) API:

```bash
java --enable-preview --enable-native-access=ALL-UNNAMED -jar myapp.jar
```

---

## Schema Definition

Schemas are defined in `.myra.yml` files using YAML syntax.

### Basic Schema Structure

```yaml
namespace: "com.example.trading"
version: "1.0.0"

enums:
  - name: "Side"
    type: "int8"
    values:
      - name: "BUY"
        id: 0
      - name: "SELL"
        id: 1

messages:
  - name: "Order"
    fields:
      - tag: 1
        name: "orderId"
        type: "int64"
      - tag: 2
        name: "symbol"
        type: "string"
        fixed_capacity: 8
      - tag: 3
        name: "price"
        type: "int64"
      - tag: 4
        name: "quantity"
        type: "int32"
      - tag: 5
        name: "side"
        type: "Side"
      - tag: 6
        name: "clientOrderId"
        type: "string"
        optional: true
```

### Supported Types

<div class="table-container">
<table class="table is-fullwidth is-striped is-hoverable">
  <thead>
    <tr>
      <th>Type</th>
      <th>Size</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr><td><code>bool</code></td><td>1 byte</td><td>Boolean value</td></tr>
    <tr><td><code>int8</code></td><td>1 byte</td><td>Signed 8-bit integer</td></tr>
    <tr><td><code>int16</code></td><td>2 bytes</td><td>Signed 16-bit integer (big-endian)</td></tr>
    <tr><td><code>int32</code></td><td>4 bytes</td><td>Signed 32-bit integer (big-endian)</td></tr>
    <tr><td><code>int64</code></td><td>8 bytes</td><td>Signed 64-bit integer (big-endian)</td></tr>
    <tr><td><code>float32</code></td><td>4 bytes</td><td>32-bit IEEE 754 float (big-endian)</td></tr>
    <tr><td><code>float64</code></td><td>8 bytes</td><td>64-bit IEEE 754 double (big-endian)</td></tr>
    <tr><td><code>string</code></td><td>Variable</td><td>UTF-8 encoded string</td></tr>
    <tr><td><code>bytes</code></td><td>Variable</td><td>Raw byte array</td></tr>
    <tr><td><code>&lt;EnumName&gt;</code></td><td>Varies</td><td>Reference to defined enum</td></tr>
    <tr><td><code>&lt;MessageName&gt;</code></td><td>Variable</td><td>Nested message reference</td></tr>
  </tbody>
</table>
</div>

### Field Modifiers

<div class="table-container">
<table class="table is-fullwidth is-striped is-hoverable">
  <thead>
    <tr>
      <th>Modifier</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr><td><code>optional: true</code></td><td>Field may be absent (tracked via presence bits)</td></tr>
    <tr><td><code>repeated: true</code></td><td>Field is an array (not yet fully implemented)</td></tr>
    <tr><td><code>fixed_capacity: N</code></td><td>Fixed-size string/bytes field (inline, no var-length header)</td></tr>
    <tr><td><code>deprecated: true</code></td><td>Mark field as deprecated</td></tr>
  </tbody>
</table>
</div>

### Fixed-Capacity Strings

For predictable memory layout and better cache locality, use `fixed_capacity`:

```yaml
- tag: 2
  name: "symbol"
  type: "string"
  fixed_capacity: 8  # Always 12 bytes: 4-byte length + 8-byte data
```

Benefits:
- Constant-time field access
- No variable-length header lookup
- Better memory alignment

### Nested Messages

Messages can contain other messages:

```yaml
messages:
  - name: "Trade"
    fields:
      - tag: 1
        name: "price"
        type: "int64"
      - tag: 2
        name: "size"
        type: "int32"

  - name: "OrderBookSnapshot"
    fields:
      - tag: 1
        name: "symbol"
        type: "string"
        fixed_capacity: 8
      - tag: 2
        name: "lastTrade"
        type: "Trade"
        optional: true
```

---

## Code Generation

### Using the CLI

```bash
java -jar myra-codec-codegen.jar \
    --schema src/main/resources/schemas/order.myra.yml \
    --output build/generated/myra \
    --lockfile src/main/resources/schemas/order.myra.lock
```

### Generated Artifacts

For each message, the codegen produces:

1. **`{MessageName}Flyweight`** - Zero-copy reader with getters
2. **`{MessageName}Builder`** - Single-pass encoder with setters

For each enum:

1. **`{EnumName}`** - Java enum with `id()` method

### Lock Files

The `.myra.lock` file tracks:
- Stable field IDs for wire compatibility
- Schema version history
- Evolution metadata

**Never delete the lock file** - it ensures backward compatibility.

---

## Using Generated Code

### Reading Data (Flyweight)

Flyweights provide zero-allocation access to binary data:

```java
import com.example.trading.OrderFlyweight;
import express.mvp.roray.utils.memory.Utf8View;
import java.lang.foreign.MemorySegment;

// Create reusable flyweight (allocate once, reuse many times)
OrderFlyweight order = new OrderFlyweight();

// Wrap received binary data
MemorySegment receivedData = ...; // From network, file, etc.
order.wrap(receivedData, 0);

// Zero-allocation field access
long orderId = order.getOrderId();
int quantity = order.getQuantity();
Side side = order.getSide();

// String fields return Utf8View (zero-copy view)
Utf8View symbolView = order.getSymbol();

// Compare without allocating String objects
if (symbolView.equalsString("AAPL")) {
    processAppleOrder(order);
}

// Only allocate String when truly needed
String symbol = symbolView.toString();
```

### Writing Data (Builder)

Builders encode data in a single pass:

```java
import com.example.trading.OrderBuilder;
import express.mvp.myra.codec.runtime.MessageEncoder;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

// Create encoder (manages buffer pool)
MessageEncoder encoder = new MessageEncoder(bufferPool);

// Allocate scratch buffer for string encoding (reuse per thread)
MemorySegment scratch = Arena.ofAuto().allocate(256);

// Build message
OrderBuilder builder = OrderBuilder.allocate(encoder, 1024);
builder
    .setOrderId(12345L)
    .setSymbol("AAPL", scratch)
    .setPrice(15050_00000000L)  // Fixed-point: $150.50
    .setQuantity(100)
    .setSide(Side.BUY);

// Finalize and get pooled segment
PooledSegment pooledMsg = builder.build(
    (short) OrderFlyweight.TEMPLATE_ID,
    (short) 1  // Schema version
);

// Use the segment
MemorySegment encoded = pooledMsg.segment();
transport.send(encoded);

// Return to pool when done
pooledMsg.close();
```

### Nested Messages

```java
// Writing nested messages
OrderBookSnapshotBuilder snapshot = OrderBookSnapshotBuilder.allocate(encoder, 2048);
snapshot
    .setSymbol("AAPL", scratch)
    .setTimestamp(System.nanoTime())
    .setLastTrade(trade -> {
        trade.setPrice(15050_00000000L)
             .setSize(500);
    });

PooledSegment msg = snapshot.build(templateId, schemaVersion);
```

### Optional Fields

```java
// Check presence before reading
OrderFlyweight order = new OrderFlyweight();
order.wrap(data, 0);

if (order.hasClientOrderId()) {
    Utf8View clientId = order.getClientOrderId();
    // Process client order ID
}

// Writing optional fields (simply don't call setter to leave absent)
builder.setOrderId(123L)
       .setSymbol("AAPL", scratch)
       // Not calling setClientOrderId - field will be absent
       .setQuantity(100);
```

---

## Binary Format

### Message Layout

<div class="io-diagram">
  <svg viewBox="0 0 900 560" role="img" aria-label="Message layout diagram" style="width: 100%; height: auto;">
    <rect x="10" y="10" width="880" height="540" rx="14" ry="14" fill="none" stroke="#e5e7eb" stroke-width="2" />

    <text x="450" y="45" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="16">Message Header</text>
    <rect x="60" y="60" width="780" height="70" fill="none" stroke="#e5e7eb" stroke-width="2" />
    <line x1="255" y1="60" x2="255" y2="130" stroke="#e5e7eb" stroke-width="2" />
    <line x1="450" y1="60" x2="450" y2="130" stroke="#e5e7eb" stroke-width="2" />
    <line x1="645" y1="60" x2="645" y2="130" stroke="#e5e7eb" stroke-width="2" />
    <text x="157" y="90" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="13">Frame Length</text>
    <text x="157" y="110" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="12">(4 bytes)</text>
    <text x="352" y="90" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="13">Template ID</text>
    <text x="352" y="110" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="12">(2 bytes)</text>
    <text x="547" y="90" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="13">Schema Ver</text>
    <text x="547" y="110" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="12">(2 bytes)</text>
    <text x="742" y="90" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="13">Reserved</text>
    <text x="742" y="110" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="12">(8 bytes)</text>

    <line x1="10" y1="150" x2="890" y2="150" stroke="#e5e7eb" stroke-width="2" />
    <text x="450" y="185" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="15">Presence Bits</text>
    <text x="450" y="205" text-anchor="middle" fill="#cbd5e1" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="12">(N bytes, where N = ceil(optional_fields / 8))</text>
    <line x1="10" y1="225" x2="890" y2="225" stroke="#e5e7eb" stroke-width="2" />

    <text x="450" y="255" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="15">Fixed Fields Block</text>
    <rect x="60" y="270" width="780" height="70" fill="none" stroke="#e5e7eb" stroke-width="2" />
    <line x1="255" y1="270" x2="255" y2="340" stroke="#e5e7eb" stroke-width="2" />
    <line x1="450" y1="270" x2="450" y2="340" stroke="#e5e7eb" stroke-width="2" />
    <line x1="645" y1="270" x2="645" y2="340" stroke="#e5e7eb" stroke-width="2" />
    <text x="157" y="310" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="13">Field 1</text>
    <text x="352" y="310" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="13">Field 2</text>
    <text x="547" y="310" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="13">Field 3</text>
    <text x="742" y="310" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="13">…</text>

    <line x1="10" y1="360" x2="890" y2="360" stroke="#e5e7eb" stroke-width="2" />
    <text x="450" y="390" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="15">Variable Fields Headers</text>
    <rect x="60" y="405" width="520" height="55" fill="none" stroke="#e5e7eb" stroke-width="2" />
    <line x1="320" y1="405" x2="320" y2="460" stroke="#e5e7eb" stroke-width="2" />
    <text x="190" y="435" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="12">Offset (4 bytes)</text>
    <text x="450" y="435" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="12">Length (4 bytes)</text>
    <text x="675" y="438" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="12">× N fields</text>

    <line x1="10" y1="475" x2="890" y2="475" stroke="#e5e7eb" stroke-width="2" />
    <text x="450" y="500" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="15">Variable Fields Data</text>
    <rect x="60" y="515" width="780" height="30" fill="none" stroke="#e5e7eb" stroke-width="2" />
    <text x="450" y="535" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="12">Field N data ... Field N+1 data ... Field N+2 data ...</text>
  </svg>
</div>

### Endianness

All multi-byte integers use **big-endian** (network byte order) for:
- Efficient network transmission
- Predictable cross-platform behavior
- Easier debugging with hex dumps

### Fixed-Capacity String Layout

<div class="io-diagram">
  <svg viewBox="0 0 700 160" role="img" aria-label="Fixed-capacity string layout diagram" style="width: 100%; height: auto;">
    <rect x="10" y="10" width="680" height="140" rx="14" ry="14" fill="none" stroke="#e5e7eb" stroke-width="2" />
    <rect x="60" y="45" width="580" height="70" fill="none" stroke="#e5e7eb" stroke-width="2" />
    <line x1="220" y1="45" x2="220" y2="115" stroke="#e5e7eb" stroke-width="2" />
    <text x="140" y="75" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="13">Actual Length</text>
    <text x="140" y="95" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="12">(4 bytes)</text>
    <text x="140" y="115" text-anchor="middle" fill="#cbd5e1" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="11">big-endian</text>
    <text x="430" y="80" text-anchor="middle" fill="#e5e7eb" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="13">UTF-8 Data</text>
    <text x="430" y="100" text-anchor="middle" fill="#cbd5e1" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="12">(fixed_capacity bytes)</text>
    <text x="430" y="120" text-anchor="middle" fill="#cbd5e1" font-family="Noto Sans Mono, DejaVu Sans Mono, Liberation Mono, monospace" font-size="11">(padded with zeros)</text>
  </svg>
</div>

---

## Best Practices

### 1. Reuse Flyweights

```java
// GOOD: Create once, wrap many times
OrderFlyweight order = new OrderFlyweight();
while (hasMoreMessages()) {
    order.wrap(nextMessage, 0);
    process(order);
}

// BAD: Creates garbage
while (hasMoreMessages()) {
    OrderFlyweight order = new OrderFlyweight();  // Allocation!
    order.wrap(nextMessage, 0);
    process(order);
}
```

### 2. Use Utf8View for Comparisons

```java
// GOOD: Zero-allocation comparison
if (order.getSymbol().equalsString("AAPL")) { ... }

// BAD: Allocates String
if (order.getSymbol().toString().equals("AAPL")) { ... }
```

### 3. Thread-Local Scratch Buffers

```java
private static final ThreadLocal<MemorySegment> SCRATCH = ThreadLocal.withInitial(
    () -> Arena.ofAuto().allocate(1024)
);

public void encode(OrderBuilder builder, String symbol) {
    builder.setSymbol(symbol, SCRATCH.get());
}
```

### 4. Pool Encoded Messages

```java
PooledSegment msg = builder.build(templateId, version);
try {
    transport.send(msg.segment()).join();
} finally {
    msg.close();  // Return to pool
}
```

### 5. Fixed-Capacity for Hot Fields

For fields accessed frequently in hot paths, prefer `fixed_capacity`:

```yaml
# Hot path optimization
- tag: 1
  name: "symbol"
  type: "string"
  fixed_capacity: 8  # Constant-time access

# Cold path is fine with variable length
- tag: 10
  name: "comment"
  type: "string"  # Variable length OK
```

---

## Schema Evolution

### Adding Fields

Safe additions:
- New optional fields at end of message
- New enum values

```yaml
# Version 1
messages:
  - name: "Order"
    fields:
      - tag: 1
        name: "orderId"
        type: "int64"

# Version 2 (backward compatible)
messages:
  - name: "Order"
    fields:
      - tag: 1
        name: "orderId"
        type: "int64"
      - tag: 2           # New field
        name: "timestamp"
        type: "int64"
        optional: true   # Must be optional for compatibility
```

### Deprecating Fields

```yaml
- tag: 5
  name: "oldField"
  type: "string"
  deprecated: true
  deprecationNote: "Use newField instead, will be removed in v3.0"
```

### Breaking Changes (Major Version)

These require a new schema version:
- Removing fields
- Changing field types
- Changing field tags
- Making optional fields required

---

## Performance Tips

### 1. Pre-allocate Builders

```java
// Pre-warm the pool
for (int i = 0; i < POOL_SIZE; i++) {
    OrderBuilder b = OrderBuilder.allocate(encoder, 1024);
    b.build(templateId, version).close();
}
```

### 2. Batch Operations

```java
// Process messages in batches
List<PooledSegment> batch = new ArrayList<>(BATCH_SIZE);
for (Order order : orders) {
    batch.add(encodeOrder(order));
    if (batch.size() >= BATCH_SIZE) {
        transport.sendBatch(batch);
        batch.forEach(PooledSegment::close);
        batch.clear();
    }
}
```

### 3. Avoid Optional Field Overhead

If a field is almost always present, don't make it optional:

```yaml
# Always present - no presence bit overhead
- tag: 1
  name: "orderId"
  type: "int64"

# Rarely absent - presence bit overhead on every message
- tag: 2
  name: "timestamp"
  type: "int64"
  optional: true  # Only if truly optional
```

---

## Troubleshooting

<div class="table-container">
<table class="table is-fullwidth is-striped is-hoverable">
  <thead>
    <tr>
      <th>Issue</th>
      <th>Cause</th>
      <th>Solution</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>IllegalStateException: Flyweight is not wrapped</code></td>
      <td>Accessing fields before <code>wrap()</code></td>
      <td>Call <code>flyweight.wrap(segment, offset)</code> first</td>
    </tr>
    <tr>
      <td><code>IndexOutOfBoundsException</code> in getter</td>
      <td>Segment too small</td>
      <td>Check <code>segment.byteSize() &gt;= BLOCK_LENGTH</code></td>
    </tr>
    <tr>
      <td><code>IllegalStateException: Field already written</code></td>
      <td>Double-setting field in builder</td>
      <td>Each field can only be set once</td>
    </tr>
    <tr>
      <td><code>IllegalStateException: Missing required field</code></td>
      <td>Not setting required field</td>
      <td>Set all non-optional fields before <code>build()</code></td>
    </tr>
    <tr>
      <td>Garbled strings</td>
      <td>Wrong encoding</td>
      <td>Ensure UTF-8 encoding; check <code>fixed_capacity</code> matches data</td>
    </tr>
  </tbody>
</table>
</div>

---

## API Reference

For complete API documentation, generate Javadoc:

```bash
./gradlew :runtime:javadoc
./gradlew :codegen:javadoc
```

Open `build/docs/javadoc/index.html` in your browser.
