# MyraCodec Usage Guide

MyraCodec is a schema-driven binary serialization library that generates zero-copy flyweight accessors from YAML schema definitions. It is designed for high-frequency trading and other latency-critical applications where every allocation matters.

## Quick Start

### Dependencies

**Gradle (Kotlin DSL):**

```kotlin
plugins {
    id("express.mvp.myra-codegen") version "0.1.0-SNAPSHOT" // Optional: Gradle plugin
}

dependencies {
    implementation("express.mvp.myra:myra-codec-runtime:0.1.0-SNAPSHOT")
    implementation("express.mvp.roray:roray-ffm-utils:0.1.0-SNAPSHOT")
}
```

**Maven:**

```xml
<dependency>
    <groupId>express.mvp.myra</groupId>
    <artifactId>myra-codec-runtime</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>express.mvp.roray</groupId>
    <artifactId>roray-ffm-utils</artifactId>
    <version>0.1.0-SNAPSHOT</version>
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

| Type | Size | Description |
|------|------|-------------|
| `bool` | 1 byte | Boolean value |
| `int8` | 1 byte | Signed 8-bit integer |
| `int16` | 2 bytes | Signed 16-bit integer (big-endian) |
| `int32` | 4 bytes | Signed 32-bit integer (big-endian) |
| `int64` | 8 bytes | Signed 64-bit integer (big-endian) |
| `float32` | 4 bytes | 32-bit IEEE 754 float (big-endian) |
| `float64` | 8 bytes | 64-bit IEEE 754 double (big-endian) |
| `string` | Variable | UTF-8 encoded string |
| `bytes` | Variable | Raw byte array |
| `<EnumName>` | Varies | Reference to defined enum |
| `<MessageName>` | Variable | Nested message reference |

### Field Modifiers

| Modifier | Description |
|----------|-------------|
| `optional: true` | Field may be absent (tracked via presence bits) |
| `repeated: true` | Field is an array (not yet fully implemented) |
| `fixed_capacity: N` | Fixed-size string/bytes field (inline, no var-length header) |
| `deprecated: true` | Mark field as deprecated |

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
pooledMsg.release();
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

```
┌─────────────────────────────────────────────────────────────────┐
│                         Message Header                          │
│  ┌──────────────┬──────────────┬──────────────┬───────────────┐ │
│  │ Frame Length │ Template ID  │ Schema Ver   │  Reserved     │ │
│  │   (4 bytes)  │  (2 bytes)   │  (2 bytes)   │  (8 bytes)    │ │
│  └──────────────┴──────────────┴──────────────┴───────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                       Presence Bits                              │
│         (N bytes, where N = ceil(optional_fields / 8))          │
├─────────────────────────────────────────────────────────────────┤
│                       Fixed Fields Block                         │
│  ┌──────────────┬──────────────┬──────────────┬───────────────┐ │
│  │   Field 1    │   Field 2    │   Field 3    │     ...       │ │
│  └──────────────┴──────────────┴──────────────┴───────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                   Variable Fields Headers                        │
│  ┌──────────────────────┬──────────────────────┐                │
│  │  Offset (4 bytes)    │  Length (4 bytes)    │  × N fields   │
│  └──────────────────────┴──────────────────────┘                │
├─────────────────────────────────────────────────────────────────┤
│                   Variable Fields Data                           │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Field N data ... Field N+1 data ... Field N+2 data ...  │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### Endianness

All multi-byte integers use **big-endian** (network byte order) for:
- Efficient network transmission
- Predictable cross-platform behavior
- Easier debugging with hex dumps

### Fixed-Capacity String Layout

```
┌────────────────┬────────────────────────────────┐
│ Actual Length  │         UTF-8 Data             │
│   (4 bytes)    │    (fixed_capacity bytes)      │
│   big-endian   │   (padded with zeros)          │
└────────────────┴────────────────────────────────┘
```

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
    msg.release();  // Return to pool
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
    b.build(templateId, version).release();
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
        batch.forEach(PooledSegment::release);
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

| Issue | Cause | Solution |
|-------|-------|----------|
| `IllegalStateException: Flyweight is not wrapped` | Accessing fields before `wrap()` | Call `flyweight.wrap(segment, offset)` first |
| `IndexOutOfBoundsException` in getter | Segment too small | Check `segment.byteSize() >= BLOCK_LENGTH` |
| `IllegalStateException: Field already written` | Double-setting field in builder | Each field can only be set once |
| `IllegalStateException: Missing required field` | Not setting required field | Set all non-optional fields before `build()` |
| Garbled strings | Wrong encoding | Ensure UTF-8 encoding; check `fixed_capacity` matches data |

---

## API Reference

For complete API documentation, generate Javadoc:

```bash
./gradlew :runtime:javadoc
./gradlew :codegen:javadoc
```

Open `build/docs/javadoc/index.html` in your browser.
