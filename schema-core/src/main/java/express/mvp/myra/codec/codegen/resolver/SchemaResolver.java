package express.mvp.myra.codec.codegen.resolver;

import express.mvp.myra.codec.schema.EnumDefinition;
import express.mvp.myra.codec.schema.EnumValueDefinition;
import express.mvp.myra.codec.schema.FieldDefinition;
import express.mvp.myra.codec.schema.MessageDefinition;
import express.mvp.myra.codec.schema.SchemaDefinition;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class SchemaResolver {

    private final SchemaDefinition rawSchema;
    private final LockFile lockFile;
    private final Map<String, Map<String, Integer>> reservedFieldNamesByMessage;
    private final Map<String, LockFile.MessageLock> newMessagesLock = new HashMap<>();
    private final Map<String, LockFile.EnumLock> newEnumsLock = new HashMap<>();
    private final List<ResolvedMessageDefinition> resolvedMessages = new ArrayList<>();
    private final List<ResolvedEnumDefinition> resolvedEnums = new ArrayList<>();
    private final Set<String> processedMessageNames = new HashSet<>();
    private final AtomicInteger nextMessageId;

    private SchemaResolver(SchemaDefinition rawSchema, LockFile existingLockFile) {
        this.rawSchema = rawSchema;
        this.lockFile = (existingLockFile != null) ? existingLockFile : LockFile.empty();
        this.reservedFieldNamesByMessage = extractReservedFieldNames();
        int maxMessageId = findMaxId(this.lockFile.messages.values(), lock -> lock.id);
        this.nextMessageId = new AtomicInteger(maxMessageId + 1);
    }

    public static ResolutionResult resolve(
            SchemaDefinition rawSchema, LockFile existingLockFile, Path schemaPath) {
        SchemaResolver resolver = new SchemaResolver(rawSchema, existingLockFile);
        return resolver.runResolution(schemaPath);
    }

    private ResolutionResult runResolution(Path schemaPath) {
        resolveMessages();
        resolveEnums();
        LockFile updatedLockFile = buildUpdatedLockFile(schemaPath);

        ResolvedSchemaDefinition finalResolvedSchema =
                new ResolvedSchemaDefinition(
                        rawSchema.namespace(),
                        rawSchema.version(),
                        resolvedMessages,
                        resolvedEnums);

        return new ResolutionResult(finalResolvedSchema, updatedLockFile);
    }

    private void resolveMessages() {
        Map<String, LockFile.MessageLock> existingMessageLocks = lockFile.messages;

        for (MessageDefinition rawMessage : rawSchema.messages()) {
            processedMessageNames.add(rawMessage.name());
            LockFile.MessageLock existingMessageLock = existingMessageLocks.get(rawMessage.name());

            int messageId;
            Map<String, Integer> existingFieldLocks;

            if (existingMessageLock != null) {
                messageId = existingMessageLock.id;
                existingFieldLocks = existingMessageLock.fields;
            } else {
                messageId = nextMessageId.getAndIncrement();
                existingFieldLocks = Collections.emptyMap();
            }

            Map<String, Integer> newFieldsLock = new HashMap<>();
            List<ResolvedFieldDefinition> resolvedFields =
                    resolveFieldsForMessage(rawMessage, existingFieldLocks, newFieldsLock);

            resolvedMessages.add(
                    new ResolvedMessageDefinition(rawMessage.name(), messageId, resolvedFields));
            LockFile.MessageLock messageLock = new LockFile.MessageLock();
            messageLock.id = messageId;
            messageLock.fields = newFieldsLock;
            newMessagesLock.put(rawMessage.name(), messageLock);
        }
    }

    private List<ResolvedFieldDefinition> resolveFieldsForMessage(
            MessageDefinition rawMessage,
            Map<String, Integer> existingFieldLocks,
            Map<String, Integer> newFieldsLock) {
        enforceSequentialFieldTags(rawMessage);
        List<ResolvedFieldDefinition> resolvedFields = new ArrayList<>();

        int maxFieldId = findMaxId(existingFieldLocks.values(), Integer::intValue);
        AtomicInteger nextFieldId = new AtomicInteger(maxFieldId + 1);

        for (FieldDefinition rawField : rawMessage.fields()) {
            Integer fieldId = existingFieldLocks.get(rawField.name());

            if (fieldId == null) {
                enforceNoReservedNameReuse(rawMessage.name(), rawField.name());
                fieldId = nextFieldId.getAndIncrement();
            }

            resolvedFields.add(
                    new ResolvedFieldDefinition(
                            rawField.name(),
                            fieldId,
                            rawField.tag(),
                            rawField.type(),
                            rawField.optional(),
                            rawField.repeated(),
                            rawField.deprecated(),
                            rawField.deprecationNote(),
                            rawField.fixedCapacity()));
            newFieldsLock.put(rawField.name(), fieldId);
        }

        return resolvedFields;
    }

    private void enforceSequentialFieldTags(MessageDefinition message) {
        List<FieldDefinition> fields = message.fields();
        if (fields == null || fields.isEmpty()) {
            return;
        }

        Map<Integer, String> seenTags = new HashMap<>();
        for (FieldDefinition field : fields) {
            String previous = seenTags.putIfAbsent(field.tag(), field.name());
            if (previous != null) {
                throw new IllegalStateException(
                        "Message '"
                                + message.name()
                                + "' reuses tag "
                                + field.tag()
                                + " for field '"
                                + field.name()
                                + "', previously used by '"
                                + previous
                                + "'");
            }
        }

        List<FieldDefinition> sortedByTag = new ArrayList<>(fields);
        sortedByTag.sort(Comparator.comparingInt(FieldDefinition::tag));
        int expectedTag = 1;
        for (FieldDefinition field : sortedByTag) {
            if (field.tag() != expectedTag) {
                throw new IllegalStateException(
                        "Message '"
                                + message.name()
                                + "' must assign sequential field tags starting at 1. "
                                + "Expected tag "
                                + expectedTag
                                + " but found "
                                + field.tag()
                                + " on field '"
                                + field.name()
                                + "'");
            }
            expectedTag++;
        }
    }

    private void resolveEnums() {
        List<EnumDefinition> enums = rawSchema.enums();
        if (enums == null || enums.isEmpty()) {
            return;
        }

        for (EnumDefinition rawEnum : enums) {
            resolvedEnums.add(ResolvedEnumDefinition.fromRaw(rawEnum));

            Map<String, Integer> valuesLock =
                    rawEnum.values().stream()
                            .collect(
                                    Collectors.toMap(
                                            EnumValueDefinition::name, EnumValueDefinition::id));
            LockFile.EnumLock enumLock = new LockFile.EnumLock();
            enumLock.values = valuesLock;
            newEnumsLock.put(rawEnum.name(), enumLock);
        }
    }

    private LockFile buildUpdatedLockFile(Path schemaPath) {
        Map<String, Object> reservedIds = new HashMap<>();
        if (lockFile.reservedIds != null) {
            reservedIds.putAll(lockFile.reservedIds);
        }

        List<Integer> reservedMessageIds = extractReservedMessageIds(reservedIds.get("messages"));
        Map<String, Map<String, Integer>> reservedFieldIds =
                new HashMap<>(reservedFieldNamesByMessage);

        for (String oldMessageName : lockFile.messages.keySet()) {
            if (!processedMessageNames.contains(oldMessageName)) {
                int id = lockFile.messages.get(oldMessageName).id;
                if (!reservedMessageIds.contains(id)) {
                    reservedMessageIds.add(id);
                }
            }
        }

        for (Map.Entry<String, LockFile.MessageLock> newEntry : newMessagesLock.entrySet()) {
            String messageName = newEntry.getKey();
            LockFile.MessageLock newMessageLock = newEntry.getValue();
            LockFile.MessageLock oldMessageLock = lockFile.messages.get(messageName);

            if (oldMessageLock != null) {
                Map<String, Integer> reservedForMessage =
                        reservedFieldIds.computeIfAbsent(messageName, k -> new HashMap<>());
                for (String oldFieldName : oldMessageLock.fields.keySet()) {
                    if (!newMessageLock.fields.containsKey(oldFieldName)) {
                        reservedForMessage.putIfAbsent(
                                oldFieldName, oldMessageLock.fields.get(oldFieldName));
                    }
                }
                if (reservedForMessage.isEmpty()) {
                    reservedFieldIds.remove(messageName);
                }
            }
        }

        if (!reservedMessageIds.isEmpty()) {
            reservedIds.put("messages", reservedMessageIds);
        } else {
            reservedIds.remove("messages");
        }
        if (!reservedFieldIds.isEmpty()) {
            reservedIds.put("fields", reservedFieldIds);
        } else {
            reservedIds.remove("fields");
        }

        LockFile result = LockFile.empty();
        result.schemaInfo =
                Map.of(
                        "sourceFile",
                        schemaPath.getFileName().toString(),
                        "version",
                        rawSchema.version());
        result.messages = newMessagesLock;
        result.enums = newEnumsLock;
        result.reservedIds = reservedIds;
        return result;
    }

    private <T> int findMaxId(Collection<T> items, Function<T, Integer> idExtractor) {
        return items.stream().mapToInt(idExtractor::apply).max().orElse(0);
    }

    private Map<String, Map<String, Integer>> extractReservedFieldNames() {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        if (lockFile.reservedIds == null) {
            return result;
        }
        Object fields = lockFile.reservedIds.get("fields");
        if (!(fields instanceof Map<?, ?>)) {
            return result;
        }
        Map<?, ?> messageMap = (Map<?, ?>) fields;
        for (Map.Entry<?, ?> entry : messageMap.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                continue;
            }
            String messageName = (String) entry.getKey();
            Object value = entry.getValue();
            Map<String, Integer> fieldMap = new HashMap<>();
            if (value instanceof Map<?, ?>) {
                Map<?, ?> rawFields = (Map<?, ?>) value;
                for (Map.Entry<?, ?> fieldEntry : rawFields.entrySet()) {
                    if (fieldEntry.getKey() instanceof String
                            && fieldEntry.getValue() instanceof Number) {
                        String fieldName = (String) fieldEntry.getKey();
                        Number id = (Number) fieldEntry.getValue();
                        fieldMap.put(fieldName, id.intValue());
                    }
                }
            }
            if (!fieldMap.isEmpty()) {
                result.put(messageName, fieldMap);
            }
        }
        return result;
    }

    private List<Integer> extractReservedMessageIds(Object existing) {
        if (existing instanceof List<?>) {
            List<?> list = (List<?>) existing;
            List<Integer> copy = new ArrayList<>();
            for (Object value : list) {
                if (value instanceof Number) {
                    Number n = (Number) value;
                    copy.add(n.intValue());
                }
            }
            return copy;
        }
        return new ArrayList<>();
    }

    private void enforceNoReservedNameReuse(String messageName, String fieldName) {
        Map<String, Integer> reservedForMessage = reservedFieldNamesByMessage.get(messageName);
        if (reservedForMessage != null && reservedForMessage.containsKey(fieldName)) {
            throw new IllegalStateException(
                    "Field '"
                            + fieldName
                            + "' in message '"
                            + messageName
                            + "' was previously reserved and cannot be reused");
        }
    }
}
