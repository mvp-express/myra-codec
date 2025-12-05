# Myra Schema Specification

> Canonical specification for `.myra.yml` and `.myra.lock` files

*Version: 1.0*

---

## Overview

The Myra schema system defines data structures for binary serialization. It consists of two file types:

| File | Purpose | Managed By |
|------|---------|------------|
| `.myra.yml` | Schema definition (messages, types, enums) | Developer |
| `.myra.lock` | Binary identity ledger (IDs, offsets, hashes) | Code generator |

---

## Schema Definition (`.myra.yml`)

### Top-Level Structure

```yaml
# Required: Java package for generated code
namespace: "com.example.trading"

# Optional: Schema version for documentation
version: "1.0.0"

# Message definitions
messages:
  - name: "MessageName"
    fields:
      - name: "fieldName"
        type: "type"

# Enum definitions
enums:
  - name: "EnumName"
    type: "int8"
    values:
      - name: "VALUE_A"
        value: 1
```

### Complete Example

```yaml
namespace: "com.example.trading"
version: "1.0.0"

messages:
  - name: "NewOrderRequest"
    fields:
      - name: "clOrdId"
        type: "string"
      - name: "symbol"
        type: "string"
      - name: "side"
        type: "Side"          # Reference to enum
      - name: "orderQty"
        type: "int64"
      - name: "price"
        type: "int64"
      - name: "comment"
        type: "string"
        optional: true        # Optional field
      - name: "tags"
        type: "string"
        repeated: true        # List of strings

  - name: "ExecutionReport"
    fields:
      - name: "orderId"
        type: "string"
      - name: "clOrdId"
        type: "string"
      - name: "execType"
        type: "ExecType"
      - name: "leavesQty"
        type: "int64"
      - name: "orderDetails"
        type: "NewOrderRequest"  # Nested message

enums:
  - name: "Side"
    type: "int8"
    values:
      - name: "BUY"
        value: 1
      - name: "SELL"
        value: 2

  - name: "ExecType"
    type: "int8"
    values:
      - name: "NEW"
        value: 0
      - name: "PARTIAL_FILL"
        value: 1
      - name: "FILL"
        value: 2
      - name: "CANCELED"
        value: 4
      - name: "REJECTED"
        value: 8
```

---

## Data Types

### Primitive Types

| Type | Description | Wire Size | Java Type |
|------|-------------|-----------|-----------|
| `bool` | Boolean value | 1 byte | `boolean` |
| `int8` | Signed 8-bit integer | 1 byte | `byte` |
| `int16` | Signed 16-bit integer | 2 bytes | `short` |
| `int32` | Signed 32-bit integer | 4 bytes | `int` |
| `int64` | Signed 64-bit integer | 8 bytes | `long` |
| `float32` | 32-bit IEEE 754 float | 4 bytes | `float` |
| `float64` | 64-bit IEEE 754 double | 8 bytes | `double` |

### Variable-Length Types

| Type | Description | Wire Format |
|------|-------------|-------------|
| `string` | UTF-8 encoded text | VarInt length + bytes |
| `bytes` | Raw byte array | VarInt length + bytes |

### Rich Types

| Type | Description | Wire Format |
|------|-------------|-------------|
| `uuid` | UUID (128-bit) | Fixed 16 bytes |
| `timestamp_nanos` | Nanosecond timestamp | int64 |
| `timestamp_millis` | Millisecond timestamp | int64 |
| `decimal` | Fixed-point decimal | int64 (scaled) |

### Complex Types

| Type | Description | Wire Format |
|------|-------------|-------------|
| `MessageName` | Nested message | Inline fields |
| `EnumName` | Enum value | Underlying int type |
| `map<K,V>` | Key-value pairs | VarInt count + pairs |

---

## Field Modifiers

### Optional Fields

```yaml
- name: "comment"
  type: "string"
  optional: true
```

**Wire format**: 1-byte presence flag (0 = absent, 1 = present)

**Generated code**:
```java
public boolean hasComment();
public String getComment();  // Returns null if absent
public void setComment(String value);
public void clearComment();
```

### Repeated Fields (Lists)

```yaml
- name: "tags"
  type: "string"
  repeated: true
```

**Wire format**: VarInt count + elements

**Generated code**:
```java
public int getTagsCount();
public String getTags(int index);
public void addTags(String value);
public void clearTags();
```

### Maps

```yaml
- name: "attributes"
  type: "map<string, string>"
```

**Wire format**: VarInt count + (key, value) pairs

### Oneof (Union Types)

```yaml
oneof:
  - name: "creditCard"
    type: "CreditCardDetails"
  - name: "paypal"
    type: "PayPalDetails"
  - name: "bankTransfer"
    type: "BankTransferDetails"
```

**Wire format**: 1-byte discriminator + selected field data

**Generated code**:
```java
public PaymentCase getPaymentCase();
public CreditCardDetails getCreditCard();
public void setCreditCard(CreditCardDetails value);
// ... similar for other options
```

---

## Repeating Groups (SBE-Style)

For ultra-high performance, lists can use SBE-style repeating groups:

```yaml
groups:
  - name: "orderLegs"
    itemMessage: "OrderLeg"
```

**Wire format**: 
- Block length (2 bytes)
- Num entries (VarInt)
- Fixed-size entries

This allows decoders to skip the entire group without parsing individual elements.

---

## Field Deprecation

```yaml
- name: "oldField"
  type: "int32"
  deprecated: true
  deprecation_note: "Use 'newField' instead."
```

Generated code includes `@Deprecated` annotation with the note.

---

## Lock File (`.myra.lock`)

The lock file is automatically generated and managed by the code generator. It ensures binary compatibility across schema evolution.

### Structure

```yaml
# Auto-generated - do not edit manually
version: 1

messages:
  NewOrderRequest:
    id: 1001
    fields:
      clOrdId:
        id: 1
        deleted: false
      symbol:
        id: 2
        deleted: false
      side:
        id: 3
        deleted: false
      orderQty:
        id: 4
        deleted: false
      price:
        id: 5
        deleted: false
    reservedIds: []

  ExecutionReport:
    id: 1002
    fields:
      orderId:
        id: 1
        deleted: false
      # ...
    reservedIds: [7, 8]  # Previously deleted fields

enums:
  Side:
    id: 2001
    values:
      BUY: 1
      SELL: 2
```

### ID Assignment

- **Messages**: IDs in range 1000-64999
- **Fields**: IDs in range 1-255 per message
- **Enums**: IDs in range 2000-64999

IDs are assigned using FNV-1a hash with deterministic probing:

```
hash = FNV-1a("Message:NewOrderRequest")
id = min + (hash % (max - min + 1))
```

### Schema Evolution Rules

| Change | Allowed | Notes |
|--------|---------|-------|
| Add field | ✅ | Gets new ID, old decoders ignore |
| Add message | ✅ | Gets new ID |
| Add enum value | ✅ | Must not reuse value |
| Remove field | ✅ | ID added to reservedIds |
| Rename field | ✅ | Uses alias in lock file |
| Change field type | ❌ | Breaking change |
| Reorder fields | ✅ | Wire order by ID, not YAML order |

### Deletions

When a field is removed:

```yaml
# .myra.yml - field removed
messages:
  - name: "Order"
    fields:
      - name: "orderId"
        type: "string"
      # legacyField removed

# .myra.lock - ID preserved as reserved
messages:
  Order:
    id: 1001
    fields:
      orderId:
        id: 1
        deleted: false
      legacyField:
        id: 2
        deleted: true  # Marked deleted
    reservedIds: [2]   # Never reused
```

### Renames

```yaml
# Alias preserves ID through rename
aliases:
  messages:
    OldMessageName: NewMessageName
  fields:
    Message.oldField: newField
```

---

## CLI Usage

### Generate Code

```bash
# Generate Java flyweights
myra-cli generate schema.myra.yml -o src/main/java

# Check lock file (CI mode - fails if drift detected)
myra-cli generate schema.myra.yml --check

# Update lock file (development mode)
myra-cli generate schema.myra.yml --write
```

### Validate Schema

```bash
myra-cli validate schema.myra.yml
```

### Show Lock Diff

```bash
myra-cli diff schema.myra.yml
```

---

## Wire Format

### Message Encoding

```
┌─────────────┬─────────────┬─────────────────────────────┐
│ Message ID  │ Total Size  │ Fields (in ID order)        │
│ (VarInt)    │ (VarInt)    │                             │
└─────────────┴─────────────┴─────────────────────────────┘
```

### Field Encoding

| Type | Format |
|------|--------|
| Fixed-size primitives | Raw bytes (little-endian) |
| VarInt types | 7-bit encoding with continuation bit |
| Strings/bytes | VarInt length + data |
| Optional | 1-byte flag + value (if present) |
| Repeated | VarInt count + elements |
| Nested message | Inline fields |

### VarInt Encoding

```
Value         Bytes
0-127         1 byte  (0xxxxxxx)
128-16383     2 bytes (1xxxxxxx 0xxxxxxx)
...
```

---

## Best Practices

### Schema Design

1. **Use meaningful names**: `orderId` not `oid`
2. **Group related fields**: Use nested messages
3. **Version your schemas**: Include `version` field
4. **Document deprecations**: Always provide `deprecation_note`

### Evolution

1. **Never reuse IDs**: Let the lock file manage
2. **Add fields as optional**: Maintains backward compatibility
3. **Commit lock file**: Part of version control
4. **Run CI in check mode**: Detect accidental changes

### Performance

1. **Order fields by access pattern**: Hot fields first
2. **Use fixed-size types**: When range is known
3. **Prefer enums over strings**: For categorical data
4. **Use repeating groups**: For homogeneous lists

---

## Related Specifications

- [MVP.Express RPC Schema (.mvpe.yml)](https://mvp.express/docs/rpc-framework/mvpe-schema-spec/) — Service and RPC definitions that import from `.myra.yml`

