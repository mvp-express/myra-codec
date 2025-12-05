package express.mvp.myra.codec.codegen;

import com.palantir.javapoet.*;
import express.mvp.myra.codec.codegen.resolver.ResolvedEnumDefinition;
import express.mvp.myra.codec.codegen.resolver.ResolvedFieldDefinition;
import express.mvp.myra.codec.codegen.resolver.ResolvedMessageDefinition;
import express.mvp.myra.codec.codegen.resolver.ResolvedSchemaDefinition;
import express.mvp.myra.codec.runtime.struct.RepeatingGroupBuilder;
import express.mvp.myra.codec.runtime.struct.RepeatingGroupIterator;
import express.mvp.myra.codec.runtime.struct.VariableSizeRepeatingGroupBuilder;
import express.mvp.myra.codec.runtime.struct.VariableSizeRepeatingGroupIterator;
import express.mvp.myra.codec.schema.EnumValueDefinition;
import express.mvp.myra.codec.schema.SchemaVersion;
import express.mvp.roray.utils.memory.*;
import express.mvp.roray.utils.memory.BitSetView;
import express.mvp.roray.utils.memory.Layouts;
import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

/** Generates Java source files from a resolved schema definition. */
public final class StubGenerator {

    private final ResolvedSchemaDefinition schema;
    private final String flyweightSuffix = "Flyweight";

    public StubGenerator(ResolvedSchemaDefinition schema) {
        this.schema = Objects.requireNonNull(schema);
    }

    /**
     * Generates all necessary source files for the given schema.
     *
     * @return A list of JavaFile objects ready to be written to disk.
     */
    public List<JavaFile> generate() {
        List<JavaFile> generatedFiles = new ArrayList<>();

        // Generate a flyweight class for each message in the schema.
        for (ResolvedMessageDefinition message : schema.messages()) {
            if (message.fields() != null && !message.fields().isEmpty()) {
                generatedFiles.add(generateMessageFlyweight(message));
                // Also emit a minimal builder skeleton to support single-pass encoding.
                generatedFiles.add(generateMessageBuilder(message));
            }
        }

        // Generate a Java enum for each enum definition in the schema.
        for (ResolvedEnumDefinition enumDef : schema.enums()) {
            generatedFiles.add(generateEnum(enumDef));
        }

        return generatedFiles;
    }

    /**
     * Writes all generated Java files to the specified output directory.
     *
     * @param outputDir The root directory for the generated source code.
     */
    public void writeFiles(Path outputDir) throws IOException {
        for (JavaFile javaFile : generate()) {
            javaFile.writeTo(outputDir);
        }
    }

    /**
     * Generates the Java source file for a single message flyweight. This method uses a two-pass
     * approach to handle both fixed-size and variable-length fields, creating a highly optimized
     * and zero-copy data accessor.
     */
    private JavaFile generateMessageFlyweight(ResolvedMessageDefinition message) {
        String flyweightName = message.name() + flyweightSuffix;
        ClassName flyweightClassName = ClassName.get(schema.namespace(), flyweightName);

        // --- 1. Two-Pass Field Layout and FieldSpec Creation ---
        List<ResolvedFieldDefinition> fixedFields =
                message.fields().stream().filter(this::isFixedSize).collect(Collectors.toList());
        List<ResolvedFieldDefinition> varFields =
                message.fields().stream().filter(f -> !isFixedSize(f)).collect(Collectors.toList());
        Map<ResolvedFieldDefinition, Integer> optionalBits = optionalBitIndexes(message.fields());
        int presenceBytes = optionalBits.isEmpty() ? 0 : (optionalBits.size() + 7) / 8;

        List<FieldSpec> constantFields = new ArrayList<>();
        List<FieldSpec> viewFields = new ArrayList<>();
        int currentOffset = presenceBytes;

        if (presenceBytes > 0) {
            constantFields.add(
                    FieldSpec.builder(
                                    int.class,
                                    "PRESENCE_BYTES",
                                    Modifier.PUBLIC,
                                    Modifier.STATIC,
                                    Modifier.FINAL)
                            .initializer("$L", presenceBytes)
                            .build());
            viewFields.add(
                    FieldSpec.builder(
                                    BitSetView.class,
                                    "presenceBits",
                                    Modifier.PRIVATE,
                                    Modifier.FINAL)
                            .initializer("new $T()", BitSetView.class)
                            .build());
        }

        optionalBits.forEach(
                (field, index) ->
                        constantFields.add(
                                FieldSpec.builder(
                                                int.class,
                                                constantName(field.name(), "OPT_BIT"),
                                                Modifier.PUBLIC,
                                                Modifier.STATIC,
                                                Modifier.FINAL)
                                        .initializer("$L", index)
                                        .build()));

        // Pass 1: Lay out all fixed-size fields first.
        for (ResolvedFieldDefinition field : fixedFields) {
            constantFields.add(createOffsetConstant(field.name(), currentOffset));
            if (isFixedInlineUtf8(field)) {
                viewFields.add(
                        FieldSpec.builder(
                                        Utf8View.class,
                                        field.name() + "View",
                                        Modifier.PRIVATE,
                                        Modifier.FINAL)
                                .initializer("new $T()", Utf8View.class)
                                .build());
            }
            currentOffset += getFixedSize(field);
        }

        // Pass 2: For each variable-length field, add a fixed-size header (offset + length)
        // to the main flyweight block. Also create a reusable view field for zero-GC access.
        int varHeaderSize = 8; // 4 bytes for data offset, 4 for data length
        for (ResolvedFieldDefinition field : varFields) {
            constantFields.add(createOffsetConstant(field.name(), currentOffset));
            currentOffset += varHeaderSize;

            if (field.repeated()) {
                // Repeating groups use specialized iterators
                if (isRepeatedPrimitiveOrEnum(field)) {
                    // Fixed-size elements use RepeatingGroupIterator
                    int elementSize = getRepeatedElementSize(field);
                    viewFields.add(
                            FieldSpec.builder(
                                            RepeatingGroupIterator.class,
                                            field.name() + "Iterator",
                                            Modifier.PRIVATE,
                                            Modifier.FINAL)
                                    .initializer("new $T($L)", RepeatingGroupIterator.class, elementSize)
                                    .build());
                } else {
                    // Variable-size elements (strings, messages, bytes) use VariableSizeRepeatingGroupIterator
                    viewFields.add(
                            FieldSpec.builder(
                                            VariableSizeRepeatingGroupIterator.class,
                                            field.name() + "Iterator",
                                            Modifier.PRIVATE,
                                            Modifier.FINAL)
                                    .initializer("new $T()", VariableSizeRepeatingGroupIterator.class)
                                    .build());
                    // Also add a flyweight view for nested messages
                    if (isMessageType(field)) {
                        ClassName childFlyweight =
                                ClassName.get(schema.namespace(), field.type() + flyweightSuffix);
                        viewFields.add(
                                FieldSpec.builder(
                                                childFlyweight,
                                                field.name() + "View",
                                                Modifier.PRIVATE,
                                                Modifier.FINAL)
                                        .initializer("new $T()", childFlyweight)
                                        .build());
                    }
                }
            } else if (isMessageType(field)) {
                ClassName childFlyweight =
                        ClassName.get(schema.namespace(), field.type() + flyweightSuffix);
                viewFields.add(
                        FieldSpec.builder(
                                        childFlyweight,
                                        field.name() + "View",
                                        Modifier.PRIVATE,
                                        Modifier.FINAL)
                                .initializer("new $T()", childFlyweight)
                                .build());
            } else {
                viewFields.add(
                        FieldSpec.builder(
                                        Utf8View.class,
                                        field.name() + "View",
                                        Modifier.PRIVATE,
                                        Modifier.FINAL)
                                .initializer("new $T()", Utf8View.class)
                                .build());
            }
        }

        // Add final constants for Template ID, Schema Version, and the fixed block length.
        constantFields.add(
                FieldSpec.builder(
                                int.class,
                                "TEMPLATE_ID",
                                Modifier.PUBLIC,
                                Modifier.STATIC,
                                Modifier.FINAL)
                        .initializer("$L", message.id())
                        .build());

        // Parse schema version and add wire-format constant
        SchemaVersion schemaVersion = SchemaVersion.parse(schema.version());
        constantFields.add(
                FieldSpec.builder(
                                short.class,
                                "SCHEMA_VERSION",
                                Modifier.PUBLIC,
                                Modifier.STATIC,
                                Modifier.FINAL)
                        .addJavadoc("Schema version in wire format: " + schemaVersion.toShortString() + "\n")
                        .initializer("(short) $L", schemaVersion.toWireFormat())
                        .build());

        constantFields.add(
                FieldSpec.builder(
                                int.class,
                                "BLOCK_LENGTH",
                                Modifier.PUBLIC,
                                Modifier.STATIC,
                                Modifier.FINAL)
                        .initializer("$L", currentOffset)
                        .build());

        // --- 2. Generate Accessor and Core Flyweight Methods ---
        List<MethodSpec> methods = new ArrayList<>();
        methods.add(createWrapMethod(presenceBytes > 0));
        methods.add(createSegmentMethod());
        methods.add(createByteSizeMethod());
        methods.add(createIsWrappedMethod());
        methods.add(createValidateMethod());

        // Generate getters and setters for FIXED-SIZE fields.
        for (ResolvedFieldDefinition field : fixedFields) {
            String offsetConstantName = field.name().toUpperCase() + "_OFFSET";
            if (isFixedInlineUtf8(field)) {
                methods.add(createInlineUtf8Getter(field, offsetConstantName));
            } else {
                TypeName fieldType = getJavaTypeName(field.type());
                methods.add(
                        createGetter(
                                field.name(),
                                fieldType,
                                getLayoutConstantName(field.type()),
                                offsetConstantName));
                methods.add(
                        createSetter(
                                field.name(),
                                fieldType,
                                getLayoutConstantName(field.type()),
                                offsetConstantName));
            }
        }

        // Generate GETTERS ONLY for VARIABLE-LENGTH fields.
        for (ResolvedFieldDefinition field : varFields) {
            String offsetConstantName = field.name().toUpperCase() + "_OFFSET";
            if (field.repeated()) {
                // Generate repeating group accessors
                methods.addAll(createRepeatingGroupGetters(field, offsetConstantName));
            } else if (isMessageType(field)) {
                ClassName childFlyweight =
                        ClassName.get(schema.namespace(), field.type() + flyweightSuffix);
                methods.add(createMessageFieldGetter(field, childFlyweight, offsetConstantName));
            } else {
                methods.add(
                        createVarFieldGetter(
                                field, ClassName.get(Utf8View.class), offsetConstantName));
            }
        }

        if (!optionalBits.isEmpty()) {
            optionalBits.forEach(
                    (field, bit) -> methods.add(createPresenceChecker(field.name(), bit)));
        }

        // --- 3. Generate the writeTo() serialization method ---
        // The flyweight wraps already-serialized data in BinaryWriter format
        // We need to write out the fields in the same format: varint length + bytes
        MethodSpec.Builder writeToMethodBuilder =
                MethodSpec.methodBuilder("writeTo")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(BinaryWriter.class, "writer");

        // For each field, write using the appropriate writer method
        for (ResolvedFieldDefinition field : message.fields()) {
            String fieldName = field.name();
            String getterCall = "this.get" + capitalize(fieldName) + "()";
            String viewVar = fieldName + "ViewTmp";
            String bytesVar = fieldName + "BytesTmp";

            if (isMessageType(field)) {
                String offsetConst = field.name().toUpperCase() + "_OFFSET";
                writeToMethodBuilder
                        .addStatement(
                                "final int relativeOffset = this.segment.get($T.INT_BE, this.offset"
                                        + " + $L)",
                                Layouts.class,
                                offsetConst)
                        .addStatement(
                                "final int nestedLength = this.segment.get($T.INT_BE, this.offset +"
                                        + " $L + 4)",
                                Layouts.class,
                                offsetConst)
                        .addStatement("writer.writeVarInt(nestedLength)")
                        .addStatement(
                                "writer.writeSegmentRaw(this.segment, this.offset + relativeOffset,"
                                        + " nestedLength)");
                continue;
            }

            String writerMethod = getWriterMethodSuffix(field.type());

            if ("String".equals(writerMethod)) {
                writeToMethodBuilder.addStatement(
                        "$T $L = $L", Utf8View.class, viewVar, getterCall);
                writeToMethodBuilder.addStatement(
                        "byte[] $L = new byte[(int)$L.byteSize()]", bytesVar, viewVar);
                writeToMethodBuilder.addStatement(
                        "$T.copy($L.segment(), $L.offset(), $T.ofArray($L), 0, $L.byteSize())",
                        MemorySegment.class,
                        viewVar,
                        viewVar,
                        MemorySegment.class,
                        bytesVar,
                        viewVar);
                writeToMethodBuilder.addStatement("writer.writeBytes($L)", bytesVar);
            } else if ("Bytes".equals(writerMethod)) {
                writeToMethodBuilder.addStatement(
                        "$T $L = $L", Utf8View.class, viewVar, getterCall);
                writeToMethodBuilder.addStatement(
                        "byte[] $L = new byte[(int)$L.byteSize()]", bytesVar, viewVar);
                writeToMethodBuilder.addStatement(
                        "$T.copy($L.segment(), $L.offset(), $T.ofArray($L), 0, $L.byteSize())",
                        MemorySegment.class,
                        viewVar,
                        viewVar,
                        MemorySegment.class,
                        bytesVar,
                        viewVar);
                writeToMethodBuilder.addStatement("writer.writeBytes($L)", bytesVar);
            } else {
                // For primitive and enum types, write directly
                writeToMethodBuilder.addStatement("writer.write$L($L)", writerMethod, getterCall);
            }
        }

        methods.add(writeToMethodBuilder.build());

        // --- 4. Assemble the Final Class ---
        TypeSpec flyweightClass =
                TypeSpec.classBuilder(flyweightClassName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addSuperinterface(FlyweightAccessor.class)
                        .addJavadoc(
                                """
                                Auto-generated, zero-copy flyweight for the $L message.

                                Provides direct access to binary data without deserialization overhead.
                                Thread-safe for read operations when properly synchronized.

                                @see FlyweightAccessor
                                """,
                                message.name())
                        .addFields(constantFields)
                        .addField(
                                FieldSpec.builder(MemorySegment.class, "segment", Modifier.PRIVATE)
                                        .build())
                        .addField(FieldSpec.builder(long.class, "offset", Modifier.PRIVATE).build())
                        .addFields(viewFields)
                        .addMethods(methods)
                        .build();

        return JavaFile.builder(schema.namespace(), flyweightClass).indent("    ").build();
    }

    /** Generates a write-once builder for a message that enforces single-pass encoding. */
    private JavaFile generateMessageBuilder(ResolvedMessageDefinition message) {
        String builderName = message.name() + "Builder";
        ClassName builderClassName = ClassName.get(schema.namespace(), builderName);
        ClassName flyweightClassName =
                ClassName.get(schema.namespace(), message.name() + flyweightSuffix);
        ClassName encoderClass = ClassName.get("express.mvp.myra.codec.runtime", "MessageEncoder");
        ClassName pooledSegmentClass =
                ClassName.get("express.mvp.myra.codec.runtime", "PooledSegment");
        ClassName messageHeaderClass =
                ClassName.get("express.mvp.myra.codec.runtime.struct", "MessageHeader");
        ClassName varFieldWriterClass =
                ClassName.get("express.mvp.roray.utils.memory", "VarFieldWriter");
        ClassName layoutsClass = ClassName.get("express.mvp.roray.utils.memory", "Layouts");
        ClassName objectsClass = ClassName.get("java.util", "Objects");
        ClassName nestedHandleClass =
                ClassName.get(
                        "express.mvp.roray.utils.memory", "VarFieldWriter", "NestedFieldHandle");

        List<ResolvedFieldDefinition> fields = message.fields();
        int totalFields = fields.size();
        int varFieldCount = (int) fields.stream().filter(f -> !isFixedSize(f)).count();
        Map<ResolvedFieldDefinition, Integer> optionalBits = optionalBitIndexes(fields);
        int presenceBytes = optionalBits.isEmpty() ? 0 : (optionalBits.size() + 7) / 8;

        TypeSpec.Builder builder =
                TypeSpec.classBuilder(builderName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addJavadoc(
                                "Single-pass, write-once builder for {@code $L}.\n",
                                message.name());

        builder.addField(
                FieldSpec.builder(
                                int.class,
                                "TOTAL_FIELDS",
                                Modifier.PRIVATE,
                                Modifier.STATIC,
                                Modifier.FINAL)
                        .initializer("$L", totalFields)
                        .build());
        builder.addField(
                FieldSpec.builder(
                                int.class,
                                "VAR_FIELD_COUNT",
                                Modifier.PRIVATE,
                                Modifier.STATIC,
                                Modifier.FINAL)
                        .initializer("$L", varFieldCount)
                        .build());
        builder.addField(
                FieldSpec.builder(
                                int.class,
                                "PRESENCE_BYTES",
                                Modifier.PRIVATE,
                                Modifier.STATIC,
                                Modifier.FINAL)
                        .initializer("$L", presenceBytes)
                        .build());

        // Field name metadata for error reporting
        CodeBlock.Builder namesInit = CodeBlock.builder().add("{\n");
        for (int i = 0; i < fields.size(); i++) {
            namesInit.add("    $S", fields.get(i).name());
            if (i < fields.size() - 1) {
                namesInit.add(",\n");
            }
        }
        namesInit.add("\n}");
        builder.addField(
                FieldSpec.builder(
                                ArrayTypeName.of(String.class),
                                "FIELD_NAMES",
                                Modifier.PRIVATE,
                                Modifier.STATIC,
                                Modifier.FINAL)
                        .initializer(namesInit.build())
                        .build());

        optionalBits.forEach(
                (field, bit) ->
                        builder.addField(
                                FieldSpec.builder(
                                                int.class,
                                                constantName(field.name(), "OPT_BIT"),
                                                Modifier.PRIVATE,
                                                Modifier.STATIC,
                                                Modifier.FINAL)
                                        .initializer("$L", bit)
                                        .build()));

        List<Integer> requiredIndexes = new ArrayList<>();
        Map<ResolvedFieldDefinition, Integer> fieldIndexMap = new LinkedHashMap<>();
        Map<ResolvedFieldDefinition, Integer> varSlotMap = new HashMap<>();
        int fieldIdx = 0;
        int varSlot = 0;
        for (ResolvedFieldDefinition field : fields) {
            fieldIndexMap.put(field, fieldIdx);
            builder.addField(
                    FieldSpec.builder(
                                    int.class,
                                    constantName(field.name(), "INDEX"),
                                    Modifier.PRIVATE,
                                    Modifier.STATIC,
                                    Modifier.FINAL)
                            .initializer("$L", fieldIdx)
                            .build());
            if (!field.optional()) {
                requiredIndexes.add(fieldIdx);
            }
            if (!isFixedSize(field)) {
                builder.addField(
                        FieldSpec.builder(
                                        int.class,
                                        constantName(field.name(), "VAR_SLOT"),
                                        Modifier.PRIVATE,
                                        Modifier.STATIC,
                                        Modifier.FINAL)
                                .initializer("$L", varSlot)
                                .build());
                varSlotMap.put(field, varSlot);
                varSlot++;
            }
            if (isFixedInlineUtf8(field)) {
                builder.addField(
                        FieldSpec.builder(
                                        int.class,
                                        constantName(field.name(), "FIXED_CAPACITY"),
                                        Modifier.PRIVATE,
                                        Modifier.STATIC,
                                        Modifier.FINAL)
                                .initializer("$L", field.fixedCapacity())
                                .build());
            }
            fieldIdx++;
        }

        CodeBlock requiredInitializer;
        if (requiredIndexes.isEmpty()) {
            requiredInitializer = CodeBlock.of("new int[0]");
        } else {
            CodeBlock.Builder requiredBuilder = CodeBlock.builder().add("new int[] { ");
            for (int i = 0; i < requiredIndexes.size(); i++) {
                requiredBuilder.add("$L", requiredIndexes.get(i));
                if (i < requiredIndexes.size() - 1) {
                    requiredBuilder.add(", ");
                }
            }
            requiredBuilder.add(" }");
            requiredInitializer = requiredBuilder.build();
        }
        builder.addField(
                FieldSpec.builder(
                                ArrayTypeName.of(int.class),
                                "REQUIRED_FIELD_INDEXES",
                                Modifier.PRIVATE,
                                Modifier.STATIC,
                                Modifier.FINAL)
                        .initializer(requiredInitializer)
                        .build());

        // Instance fields
        builder.addField(
                FieldSpec.builder(encoderClass, "encoder", Modifier.PRIVATE, Modifier.FINAL)
                        .build());
        builder.addField(
                FieldSpec.builder(MemorySegment.class, "segment", Modifier.PRIVATE, Modifier.FINAL)
                        .build());
        builder.addField(
                FieldSpec.builder(long.class, "payloadBase", Modifier.PRIVATE, Modifier.FINAL)
                        .build());
        builder.addField(
                FieldSpec.builder(boolean.class, "inline", Modifier.PRIVATE, Modifier.FINAL)
                        .build());
        builder.addField(
                FieldSpec.builder(
                                ArrayTypeName.of(boolean.class),
                                "written",
                                Modifier.PRIVATE,
                                Modifier.FINAL)
                        .build());
        builder.addField(
                FieldSpec.builder(
                                varFieldWriterClass, "varWriter", Modifier.PRIVATE, Modifier.FINAL)
                        .build());
        builder.addField(
                FieldSpec.builder(
                                BitSetView.class, "presenceBits", Modifier.PRIVATE, Modifier.FINAL)
                        .build());
        builder.addField(FieldSpec.builder(boolean.class, "built", Modifier.PRIVATE).build());
        builder.addField(FieldSpec.builder(long.class, "frameLength", Modifier.PRIVATE).build());

        // Static allocator
        builder.addMethod(
                MethodSpec.methodBuilder("allocate")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(builderClassName)
                        .addParameter(encoderClass, "encoder")
                        .addParameter(int.class, "capacity")
                        .addStatement("$T.requireNonNull(encoder, \"encoder\")", objectsClass)
                        .addStatement("$T segment = encoder.acquire(capacity)", MemorySegment.class)
                        .addStatement("return new $T(encoder, segment, false)", builderClassName)
                        .build());

        builder.addMethod(
                MethodSpec.methodBuilder("inline")
                        .addModifiers(Modifier.STATIC)
                        .returns(builderClassName)
                        .addParameter(MemorySegment.class, "target")
                        .addStatement("$T.requireNonNull(target, \"target\")", objectsClass)
                        .addStatement("return new $T(null, target, true)", builderClassName)
                        .build());

        // Private constructor
        MethodSpec.Builder ctor =
                MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .addParameter(encoderClass, "encoder")
                        .addParameter(MemorySegment.class, "segment")
                        .addParameter(boolean.class, "inlineMode")
                        .addStatement("this.inline = inlineMode")
                        .addStatement(
                                "this.encoder = inline ? encoder : $T.requireNonNull(encoder,"
                                        + " \"encoder\")",
                                objectsClass)
                        .addStatement(
                                "this.segment = $T.requireNonNull(segment, \"segment\")",
                                objectsClass)
                        .addStatement(
                                "this.payloadBase = inline ? 0L : $T.HEADER_SIZE",
                                messageHeaderClass)
                        .addStatement("this.written = new boolean[TOTAL_FIELDS]");
        if (varFieldCount > 0) {
            ctor.addStatement(
                            "$T body = segment.asSlice(this.payloadBase, segment.byteSize() -"
                                    + " this.payloadBase)",
                            MemorySegment.class)
                    .addStatement(
                            "this.varWriter = new $T(body, $T.BLOCK_LENGTH - (VAR_FIELD_COUNT * 8),"
                                    + " VAR_FIELD_COUNT)",
                            varFieldWriterClass,
                            flyweightClassName)
                    .addStatement(
                            "for (int i = 0; i < VAR_FIELD_COUNT; i++) {$>\n"
                                    + "this.varWriter.reserveVarField();\n"
                                    + "$<}");
        } else {
            ctor.addStatement("this.varWriter = null");
        }
        if (presenceBytes > 0) {
            ctor.addStatement("this.presenceBits = new $T()", BitSetView.class)
                    .addStatement(
                            "this.presenceBits.wrap(segment, this.payloadBase, PRESENCE_BYTES)")
                    .addStatement("this.presenceBits.clearAll()");
        } else {
            ctor.addStatement("this.presenceBits = null");
        }
        builder.addMethod(ctor.build());

        // Helper methods
        builder.addMethod(
                MethodSpec.methodBuilder("ensureWritable")
                        .addModifiers(Modifier.PRIVATE)
                        .addParameter(int.class, "fieldIndex")
                        .addParameter(String.class, "fieldName")
                        .beginControlFlow("if (built)")
                        .addStatement(
                                "throw new IllegalStateException(\"Builder already finalized\")")
                        .endControlFlow()
                        .beginControlFlow("if (written[fieldIndex])")
                        .addStatement(
                                "throw new IllegalStateException(\"Field '"
                                        + "\" + fieldName + \"' already written\")")
                        .endControlFlow()
                        .build());

        builder.addMethod(
                MethodSpec.methodBuilder("markWritten")
                        .addModifiers(Modifier.PRIVATE)
                        .addParameter(int.class, "fieldIndex")
                        .addStatement("written[fieldIndex] = true")
                        .build());

        builder.addMethod(
                MethodSpec.methodBuilder("verifyRequiredFields")
                        .addModifiers(Modifier.PRIVATE)
                        .beginControlFlow("for (int idx : REQUIRED_FIELD_INDEXES)")
                        .beginControlFlow("if (!written[idx])")
                        .addStatement(
                                "throw new IllegalStateException(\"Missing required field: \" +"
                                        + " FIELD_NAMES[idx])")
                        .endControlFlow()
                        .endControlFlow()
                        .build());

        builder.addMethod(
                MethodSpec.methodBuilder("bodySize")
                        .addModifiers(Modifier.PRIVATE)
                        .returns(long.class)
                        .beginControlFlow("if (varWriter == null)")
                        .addStatement("return $T.BLOCK_LENGTH", flyweightClassName)
                        .endControlFlow()
                        .addStatement("return varWriter.bytesWritten()")
                        .build());

        builder.addMethod(
                MethodSpec.methodBuilder("frameLength")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(long.class)
                        .beginControlFlow("if (!built)")
                        .addStatement(
                                "throw new IllegalStateException(\"Call build() before querying"
                                        + " frameLength\")")
                        .endControlFlow()
                        .addStatement("return this.frameLength")
                        .build());

        // build() method
        builder.addMethod(
                MethodSpec.methodBuilder("build")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(pooledSegmentClass)
                        .addParameter(short.class, "templateId")
                        .addParameter(short.class, "schemaVersion")
                        .beginControlFlow("if (built)")
                        .addStatement(
                                "throw new IllegalStateException(\"Builder already finalized\")")
                        .endControlFlow()
                        .beginControlFlow("if (inline)")
                        .addStatement(
                                "throw new IllegalStateException(\"Inline builders cannot call"
                                        + " build()\")")
                        .endControlFlow()
                        .addStatement("verifyRequiredFields()")
                        .addStatement("long payloadSize = bodySize()")
                        .addStatement(
                                "long targetLength = $T.HEADER_SIZE + payloadSize",
                                messageHeaderClass)
                        .addStatement("encoder.getWriter(segment).position(targetLength)")
                        .addStatement(
                                "this.frameLength = encoder.finalizeMessage(segment, templateId,"
                                        + " schemaVersion)")
                        .addStatement("this.built = true")
                        .addStatement("return new $T(segment, encoder.pool())", pooledSegmentClass)
                        .build());

        builder.addMethod(
                MethodSpec.methodBuilder("finishInline")
                        .addModifiers(Modifier.FINAL)
                        .returns(long.class)
                        .beginControlFlow("if (!inline)")
                        .addStatement(
                                "throw new IllegalStateException(\"finishInline() is only valid for"
                                        + " inline builders\")")
                        .endControlFlow()
                        .beginControlFlow("if (built)")
                        .addStatement(
                                "throw new IllegalStateException(\"Builder already finalized\")")
                        .endControlFlow()
                        .addStatement("verifyRequiredFields()")
                        .addStatement("long payloadSize = bodySize()")
                        .addStatement("this.built = true")
                        .addStatement("return payloadSize")
                        .build());

        // Generate setters per field
        for (ResolvedFieldDefinition field : fields) {
            builder.addMethod(
                    createBuilderSetter(
                            builderClassName,
                            field,
                            fieldIndexMap.get(field),
                            varSlotMap.get(field),
                            optionalBits.get(field),
                            layoutsClass,
                            flyweightClassName,
                            objectsClass,
                            varFieldWriterClass,
                            nestedHandleClass));
        }

        return JavaFile.builder(schema.namespace(), builder.build()).indent("    ").build();
    }

    /** Generates a Java enum class from a schema definition. */
    private JavaFile generateEnum(ResolvedEnumDefinition enumDef) {
        ClassName enumClass = ClassName.get(schema.namespace(), enumDef.name());
        TypeSpec.Builder enumBuilder =
                TypeSpec.enumBuilder(enumClass)
                        .addModifiers(Modifier.PUBLIC)
                        .addJavadoc(
                                """
                                Auto-generated enum for $L.

                                Provides type-safe enumeration with stable integer IDs for wire format.
                                Uses O(1) array lookup via {@link #fromId(int)} for high-performance decoding.
                                """,
                                enumDef.name());

        // Add enum fields and constructor
        enumBuilder.addField(int.class, "id", Modifier.PRIVATE, Modifier.FINAL);
        enumBuilder.addMethod(
                MethodSpec.constructorBuilder()
                        .addParameter(int.class, "id")
                        .addStatement("this.id = id")
                        .build());
        enumBuilder.addMethod(
                MethodSpec.methodBuilder("id")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(int.class)
                        .addJavadoc("Returns the wire-format integer ID for this enum value.\n"
                                + "@return the numeric ID")
                        .addStatement("return this.id")
                        .build());

        // Add enum constants
        int minId = Integer.MAX_VALUE;
        int maxId = Integer.MIN_VALUE;
        for (EnumValueDefinition value : enumDef.values()) {
            enumBuilder.addEnumConstant(
                    value.name(), TypeSpec.anonymousClassBuilder("$L", value.id()).build());
            minId = Math.min(minId, value.id());
            maxId = Math.max(maxId, value.id());
        }

        // Add O(1) lookup array for non-negative, reasonably-sized ID ranges
        // We use an array indexed by ID for constant-time lookup
        if (minId >= 0 && maxId < 1024) { // Reasonable limit to avoid huge arrays
            int arraySize = maxId + 1;

            // Add static lookup array field
            ArrayTypeName arrayType = ArrayTypeName.of(enumClass);
            enumBuilder.addField(
                    FieldSpec.builder(arrayType, "VALUES_BY_ID", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                            .build());

            // Add static initializer to populate the array
            CodeBlock.Builder staticBlock = CodeBlock.builder();
            staticBlock.addStatement("VALUES_BY_ID = new $T[$L]", enumClass, arraySize);
            staticBlock.beginControlFlow("for ($T e : values())", enumClass);
            staticBlock.addStatement("VALUES_BY_ID[e.id] = e");
            staticBlock.endControlFlow();
            enumBuilder.addStaticBlock(staticBlock.build());

            // Add O(1) fromId method
            enumBuilder.addMethod(
                    MethodSpec.methodBuilder("fromId")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .addParameter(int.class, "id")
                            .returns(enumClass)
                            .addJavadoc("""
                                    Returns the enum constant for the given wire-format ID.
                                    Uses O(1) array lookup for high-performance decoding.

                                    @param id the wire-format integer ID
                                    @return the corresponding enum constant
                                    @throws IllegalArgumentException if id is out of range or unknown
                                    """)
                            .beginControlFlow("if (id < 0 || id >= VALUES_BY_ID.length)")
                            .addStatement("throw new IllegalArgumentException(\"Unknown enum id: \" + id)")
                            .endControlFlow()
                            .addStatement("$T result = VALUES_BY_ID[id]", enumClass)
                            .beginControlFlow("if (result == null)")
                            .addStatement("throw new IllegalArgumentException(\"Unknown enum id: \" + id)")
                            .endControlFlow()
                            .addStatement("return result")
                            .build());

            // Add safe fromIdOrNull method
            enumBuilder.addMethod(
                    MethodSpec.methodBuilder("fromIdOrNull")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .addParameter(int.class, "id")
                            .returns(enumClass)
                            .addAnnotation(
                                    AnnotationSpec.builder(SuppressWarnings.class)
                                            .addMember("value", "$S", "unused")
                                            .build())
                            .addJavadoc("""
                                    Returns the enum constant for the given wire-format ID, or null if unknown.
                                    Uses O(1) array lookup for high-performance decoding.

                                    @param id the wire-format integer ID
                                    @return the corresponding enum constant, or null if id is out of range or unknown
                                    """)
                            .beginControlFlow("if (id < 0 || id >= VALUES_BY_ID.length)")
                            .addStatement("return null")
                            .endControlFlow()
                            .addStatement("return VALUES_BY_ID[id]")
                            .build());

        } else {
            // For large or negative ID ranges, fall back to linear search
            enumBuilder.addMethod(
                    MethodSpec.methodBuilder("fromId")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .addParameter(int.class, "id")
                            .returns(enumClass)
                            .addJavadoc("""
                                    Returns the enum constant for the given wire-format ID.

                                    @param id the wire-format integer ID
                                    @return the corresponding enum constant
                                    @throws IllegalArgumentException if id is unknown
                                    """)
                            .beginControlFlow("for ($T e : values())", enumClass)
                            .beginControlFlow("if (e.id == id)")
                            .addStatement("return e")
                            .endControlFlow()
                            .endControlFlow()
                            .addStatement("throw new IllegalArgumentException(\"Unknown enum id: \" + id)")
                            .build());

            enumBuilder.addMethod(
                    MethodSpec.methodBuilder("fromIdOrNull")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .addParameter(int.class, "id")
                            .returns(enumClass)
                            .addAnnotation(
                                    AnnotationSpec.builder(SuppressWarnings.class)
                                            .addMember("value", "$S", "unused")
                                            .build())
                            .addJavadoc("""
                                    Returns the enum constant for the given wire-format ID, or null if unknown.

                                    @param id the wire-format integer ID
                                    @return the corresponding enum constant, or null if id is unknown
                                    """)
                            .beginControlFlow("for ($T e : values())", enumClass)
                            .beginControlFlow("if (e.id == id)")
                            .addStatement("return e")
                            .endControlFlow()
                            .endControlFlow()
                            .addStatement("return null")
                            .build());
        }

        return JavaFile.builder(schema.namespace(), enumBuilder.build()).indent("    ").build();
    }

    /** Creates the standard wrap() method for a flyweight. */
    private MethodSpec createWrapMethod(boolean hasPresenceBits) {
        MethodSpec.Builder builder =
                MethodSpec.methodBuilder("wrap")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(MemorySegment.class, "segment")
                        .addParameter(long.class, "offset")
                        .addStatement("this.segment = segment")
                        .addStatement("this.offset = offset");
        if (hasPresenceBits) {
            builder.addStatement("this.presenceBits.wrap(segment, offset, PRESENCE_BYTES)");
        }
        return builder.build();
    }

    /** Creates the standard segment() accessor for a flyweight. */
    private MethodSpec createSegmentMethod() {
        return MethodSpec.methodBuilder("segment")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(MemorySegment.class)
                .addStatement("return this.segment")
                .build();
    }

    /** Creates the standard byteSize() method, returning the fixed block length. */
    private MethodSpec createByteSizeMethod() {
        return MethodSpec.methodBuilder("byteSize")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addStatement("return BLOCK_LENGTH")
                .build();
    }

    private MethodSpec createIsWrappedMethod() {
        return MethodSpec.methodBuilder("isWrapped")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(boolean.class)
                .addStatement("return this.segment != null")
                .build();
    }

    private MethodSpec createValidateMethod() {
        return MethodSpec.methodBuilder("validate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .beginControlFlow("if (this.segment == null)")
                .addStatement("throw new IllegalStateException(\"Flyweight is not wrapped\")")
                .endControlFlow()
                .addStatement("final long remaining = segment.byteSize() - this.offset")
                .beginControlFlow("if (remaining < BLOCK_LENGTH)")
                .addStatement(
                        "throw new IllegalStateException(\"Insufficient bytes for flyweight:"
                                + " required \" + BLOCK_LENGTH)")
                .endControlFlow()
                .build();
    }

    /** Creates a private static final int constant for a field's offset. */
    private FieldSpec createOffsetConstant(String fieldName, int offset) {
        return FieldSpec.builder(
                        int.class,
                        fieldName.toUpperCase() + "_OFFSET",
                        Modifier.PUBLIC,
                        Modifier.STATIC,
                        Modifier.FINAL)
                .initializer("$L", offset)
                .build();
    }

    /** Creates a standard getter for a fixed-size primitive field. */
    private MethodSpec createGetter(String name, TypeName type, String layout, String offsetConst) {
        return MethodSpec.methodBuilder("get" + capitalize(name))
                .addModifiers(Modifier.PUBLIC)
                .returns(type)
                .addStatement(
                        "return segment.get($T.$L, this.offset + $L)",
                        Layouts.class,
                        layout,
                        offsetConst)
                .build();
    }

    /** Creates a standard setter for a fixed-size primitive field. */
    private MethodSpec createSetter(String name, TypeName type, String layout, String offsetConst) {
        return MethodSpec.methodBuilder("set" + capitalize(name))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(type, "value")
                .addStatement(
                        "segment.set($T.$L, this.offset + $L, value)",
                        Layouts.class,
                        layout,
                        offsetConst)
                .build();
    }

    /**
     * Creates a zero-GC getter for a variable-length field (like a string). It reads the offset and
     * length from the main flyweight's block and wraps a reusable view object around the actual
     * data.
     */
    private MethodSpec createVarFieldGetter(
            ResolvedFieldDefinition field, TypeName viewClass, String offsetConst) {
        // Assumes the var field header is [offset (int), length (int)]
        String viewFieldName = field.name() + "View";
        return MethodSpec.methodBuilder("get" + capitalize(field.name()))
                .addModifiers(Modifier.PUBLIC)
                .returns(viewClass)
                .addStatement(
                        "final int relativeOffset = segment.get($T.INT_BE, this.offset + $L)",
                        Layouts.class,
                        offsetConst)
                .addStatement(
                        "final int dataLength = segment.get($T.INT_BE, this.offset + $L + 4)",
                        Layouts.class,
                        offsetConst)
                .addStatement(
                        "this.$L.wrap(this.segment, this.offset + relativeOffset, dataLength)",
                        viewFieldName)
                .addStatement("return this.$L", viewFieldName)
                .build();
    }

    /**
     * Creates accessor methods for a repeating group field.
     * Generates:
     * - getXXXCount(): returns the number of elements
     * - getXXXAt(int index): returns element at index (for primitives)
     * - getXXXIterator(): returns the iterator for more complex access
     */
    private List<MethodSpec> createRepeatingGroupGetters(
            ResolvedFieldDefinition field, String offsetConst) {
        List<MethodSpec> methods = new ArrayList<>();
        String iteratorFieldName = field.name() + "Iterator";
        String capitalizedName = capitalize(field.name());

        // Count getter - wraps the iterator and returns count
        MethodSpec countGetter = MethodSpec.methodBuilder("get" + capitalizedName + "Count")
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addStatement(
                        "final long dataOffset = this.offset + segment.get($T.INT_BE, this.offset + $L)",
                        Layouts.class, offsetConst)
                .addStatement("this.$L.wrap(this.segment, dataOffset)", iteratorFieldName)
                .addStatement("return this.$L.count()", iteratorFieldName)
                .build();
        methods.add(countGetter);

        if (isRepeatedPrimitiveOrEnum(field)) {
            // For primitives: generate indexed element accessor
            TypeName elementType = getRepeatedElementType(field);
            String getterMethod = getIteratorGetterMethod(field.type());

            MethodSpec elementGetter = MethodSpec.methodBuilder("get" + capitalizedName + "At")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(int.class, "index")
                    .returns(elementType)
                    .addJavadoc("Returns the element at the given index.\n"
                            + "@param index the element index (0-based)\n"
                            + "@return the element value\n"
                            + "@throws IndexOutOfBoundsException if index is out of range")
                    .addStatement(
                            "final long dataOffset = this.offset + segment.get($T.INT_BE, this.offset + $L)",
                            Layouts.class, offsetConst)
                    .addStatement("this.$L.wrap(this.segment, dataOffset)", iteratorFieldName)
                    .addStatement("return this.$L.$L(index)", iteratorFieldName, getterMethod)
                    .build();
            methods.add(elementGetter);

            // For enums, also generate a method that returns the enum type
            if (isEnum(field.type())) {
                ClassName enumClass = ClassName.get(schema.namespace(), field.type());
                MethodSpec enumGetter = MethodSpec.methodBuilder("get" + capitalizedName + "EnumAt")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(int.class, "index")
                        .returns(enumClass)
                        .addJavadoc("Returns the enum element at the given index.\n"
                                + "@param index the element index (0-based)\n"
                                + "@return the enum value")
                        .addStatement(
                                "final long dataOffset = this.offset + segment.get($T.INT_BE, this.offset + $L)",
                                Layouts.class, offsetConst)
                        .addStatement("this.$L.wrap(this.segment, dataOffset)", iteratorFieldName)
                        .addStatement("int rawValue = (int) this.$L.$L(index)", iteratorFieldName, getterMethod)
                        .beginControlFlow("for ($T e : $T.values())", enumClass, enumClass)
                        .beginControlFlow("if (e.id() == rawValue)")
                        .addStatement("return e")
                        .endControlFlow()
                        .endControlFlow()
                        .addStatement("throw new IllegalArgumentException(\"Unknown enum value: \" + rawValue)")
                        .build();
                methods.add(enumGetter);
            }
        } else if (isMessageType(field)) {
            // For nested messages: generate method that wraps flyweight at index
            ClassName childFlyweight = ClassName.get(schema.namespace(), field.type() + flyweightSuffix);
            String viewFieldName = field.name() + "View";

            MethodSpec elementGetter = MethodSpec.methodBuilder("get" + capitalizedName + "At")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(int.class, "index")
                    .returns(childFlyweight)
                    .addJavadoc("Returns the nested message at the given index, wrapped in a reusable flyweight.\n"
                            + "@param index the element index (0-based)\n"
                            + "@return the flyweight wrapper (reused instance)\n"
                            + "@throws IndexOutOfBoundsException if index is out of range")
                    .addStatement(
                            "final long dataOffset = this.offset + segment.get($T.INT_BE, this.offset + $L)",
                            Layouts.class, offsetConst)
                    .addStatement("this.$L.wrap(this.segment, dataOffset)", iteratorFieldName)
                    .addStatement("return this.$L.wrapElementAt(index, this.$L)",
                            iteratorFieldName, viewFieldName)
                    .build();
            methods.add(elementGetter);
        } else if (isStringType(field)) {
            // For strings: generate method that fills a Utf8View
            MethodSpec stringGetter = MethodSpec.methodBuilder("get" + capitalizedName + "At")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(int.class, "index")
                    .addParameter(Utf8View.class, "view")
                    .addJavadoc("Reads the string at the given index into the provided Utf8View.\n"
                            + "@param index the element index (0-based)\n"
                            + "@param view the view to wrap around the string data")
                    .addStatement(
                            "final long dataOffset = this.offset + segment.get($T.INT_BE, this.offset + $L)",
                            Layouts.class, offsetConst)
                    .addStatement("this.$L.wrap(this.segment, dataOffset)", iteratorFieldName)
                    .addStatement("this.$L.getStringAt(index, view)", iteratorFieldName)
                    .build();
            methods.add(stringGetter);
        } else if (isBytesType(field)) {
            // For bytes: generate method that returns a segment slice
            MethodSpec bytesGetter = MethodSpec.methodBuilder("get" + capitalizedName + "At")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(int.class, "index")
                    .returns(MemorySegment.class)
                    .addJavadoc("Returns a slice of the bytes at the given index.\n"
                            + "@param index the element index (0-based)\n"
                            + "@return a MemorySegment slice containing the bytes")
                    .addStatement(
                            "final long dataOffset = this.offset + segment.get($T.INT_BE, this.offset + $L)",
                            Layouts.class, offsetConst)
                    .addStatement("this.$L.wrap(this.segment, dataOffset)", iteratorFieldName)
                    .addStatement("return this.$L.getBytesAt(index)", iteratorFieldName)
                    .build();
            methods.add(bytesGetter);
        }

        return methods;
    }

    private MethodSpec createMessageFieldGetter(
            ResolvedFieldDefinition field, ClassName childFlyweight, String offsetConst) {
        String viewFieldName = field.name() + "View";
        MethodSpec.Builder builder =
                MethodSpec.methodBuilder("get" + capitalize(field.name()))
                        .addModifiers(Modifier.PUBLIC)
                        .returns(childFlyweight);
        if (field.optional()) {
            builder.beginControlFlow("if (!has$L())", capitalize(field.name()))
                    .addStatement(
                            "throw new IllegalStateException(\"Field '$L' is not present\")",
                            field.name())
                    .endControlFlow();
        }
        builder.addStatement(
                        "final int relativeOffset = segment.get($T.INT_BE, this.offset + $L)",
                        Layouts.class,
                        offsetConst)
                .addStatement(
                        "this.$L.wrap(this.segment, this.offset + relativeOffset)", viewFieldName)
                .addStatement("return this.$L", viewFieldName);
        return builder.build();
    }

    private MethodSpec createPresenceChecker(String fieldName, int bitIndex) {
        return MethodSpec.methodBuilder("has" + capitalize(fieldName))
                .addModifiers(Modifier.PUBLIC)
                .returns(boolean.class)
                .addStatement("return this.presenceBits.get($L)", bitIndex)
                .build();
    }

    private MethodSpec createInlineUtf8Getter(ResolvedFieldDefinition field, String offsetConst) {
        String viewFieldName = field.name() + "View";
        return MethodSpec.methodBuilder("get" + capitalize(field.name()))
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(Utf8View.class))
                .addStatement("final long base = this.offset + $L", offsetConst)
                .addStatement("final int dataLength = segment.get($T.INT_BE, base)", Layouts.class)
                .addStatement("this.$L.wrap(this.segment, base + 4, dataLength)", viewFieldName)
                .addStatement("return this.$L", viewFieldName)
                .build();
    }

    private MethodSpec createBuilderSetter(
            ClassName builderClassName,
            ResolvedFieldDefinition field,
            int fieldIndex,
            Integer varSlot,
            Integer optionalBitIndex,
            ClassName layoutsClass,
            ClassName flyweightClass,
            ClassName objectsClass,
            ClassName varFieldWriterClass,
            ClassName nestedHandleClass) {
        MethodSpec.Builder method =
                MethodSpec.methodBuilder("set" + capitalize(field.name()))
                        .addModifiers(Modifier.PUBLIC)
                        .returns(builderClassName);

        if (field.repeated()) {
            // Generate setter for repeating groups
            return createRepeatingGroupSetter(
                    builderClassName, field, fieldIndex, varSlot, optionalBitIndex,
                    layoutsClass, flyweightClass, objectsClass, varFieldWriterClass);
        }

        String indexConst = constantName(field.name(), "INDEX");
        String offsetConst = constantName(field.name(), "OFFSET");
        String optionalConst =
                optionalBitIndex == null ? null : constantName(field.name(), "OPT_BIT");

        if (!isFixedSize(field)) {
            if (isStringType(field)) {
                method.addParameter(String.class, "value");
                method.addParameter(MemorySegment.class, "scratchBuffer");
                method.addStatement("$T.requireNonNull(value, \"value\")", objectsClass)
                        .addStatement(
                                "$T.requireNonNull(scratchBuffer, \"scratchBuffer\")", objectsClass)
                        .addStatement("ensureWritable($L, $S)", indexConst, field.name())
                        .beginControlFlow("if (varWriter == null)")
                        .addStatement(
                                "throw new IllegalStateException(\"Message has no variable"
                                        + " fields\")")
                        .endControlFlow()
                        .addStatement(
                                "varWriter.writeVarField($L, value, scratchBuffer)",
                                constantName(field.name(), "VAR_SLOT"))
                        .addStatement("markWritten($L)", indexConst);
                if (optionalConst != null) {
                    method.addStatement("presenceBits.set($L)", optionalConst);
                }
                method.addStatement("return this");
                return method.build();
            } else if (isBytesType(field)) {
                method.addParameter(MemorySegment.class, "source");
                method.addStatement("$T.requireNonNull(source, \"source\")", objectsClass)
                        .addStatement("ensureWritable($L, $S)", indexConst, field.name())
                        .beginControlFlow("if (varWriter == null)")
                        .addStatement(
                                "throw new IllegalStateException(\"Message has no variable"
                                        + " fields\")")
                        .endControlFlow()
                        .addStatement(
                                "varWriter.writeVarField($L, source)",
                                constantName(field.name(), "VAR_SLOT"))
                        .addStatement("markWritten($L)", indexConst);
                if (optionalConst != null) {
                    method.addStatement("presenceBits.set($L)", optionalConst);
                }
                method.addStatement("return this");
                return method.build();
            } else if (isMessageType(field)) {
                ClassName childBuilder =
                        ClassName.get(schema.namespace(), field.type() + "Builder");
                ParameterizedTypeName consumerType =
                        ParameterizedTypeName.get(ClassName.get(Consumer.class), childBuilder);
                method.addParameter(consumerType, "encoder");
                method.addStatement("$T.requireNonNull(encoder, \"encoder\")", objectsClass)
                        .addStatement("ensureWritable($L, $S)", indexConst, field.name())
                        .beginControlFlow("if (varWriter == null)")
                        .addStatement(
                                "throw new IllegalStateException(\"Message has no variable"
                                        + " fields\")")
                        .endControlFlow()
                        .addStatement(
                                "$T handle = varWriter.beginNestedField($L)",
                                nestedHandleClass,
                                constantName(field.name(), "VAR_SLOT"))
                        .addStatement("long absoluteOffset = payloadBase + handle.relativeOffset()")
                        .addStatement(
                                "$T nestedSlice = segment.asSlice(absoluteOffset,"
                                        + " segment.byteSize() - absoluteOffset)",
                                MemorySegment.class)
                        .addStatement(
                                "$T nestedBuilder = $T.inline(nestedSlice)",
                                childBuilder,
                                childBuilder)
                        .addStatement("encoder.accept(nestedBuilder)")
                        .addStatement("long nestedSize = nestedBuilder.finishInline()")
                        .addStatement("handle.finish(nestedSize)")
                        .addStatement("markWritten($L)", indexConst);
                if (optionalConst != null) {
                    method.addStatement("presenceBits.set($L)", optionalConst);
                }
                method.addStatement("return this");
                return method.build();
            }
        }

        if (isFixedInlineUtf8(field)) {
            method.addParameter(String.class, "value");
            method.addParameter(MemorySegment.class, "scratchBuffer");
            method.addStatement("$T.requireNonNull(value, \"value\")", objectsClass)
                    .addStatement(
                            "$T.requireNonNull(scratchBuffer, \"scratchBuffer\")", objectsClass)
                    .addStatement("ensureWritable($L, $S)", indexConst, field.name())
                    .addStatement("int encodedLength = $T.utf8Length(value)", varFieldWriterClass)
                    .beginControlFlow(
                            "if (encodedLength > $L)", constantName(field.name(), "FIXED_CAPACITY"))
                    .addStatement(
                            "throw new IllegalArgumentException(\"Field '\" + $S + \"' exceeds"
                                    + " fixed_capacity of \" + $L)",
                            field.name(),
                            field.fixedCapacity())
                    .endControlFlow()
                    .addStatement(
                            "$T.encodeUtf8(value, scratchBuffer, encodedLength)",
                            varFieldWriterClass)
                    .addStatement("long base = payloadBase + $T.$L", flyweightClass, offsetConst)
                    .addStatement("segment.set($T.INT_BE, base, encodedLength)", layoutsClass)
                    .addStatement(
                            "$T.copy(scratchBuffer, 0, segment, base + 4, encodedLength)",
                            MemorySegment.class)
                    .beginControlFlow(
                            "if (encodedLength < $L)", constantName(field.name(), "FIXED_CAPACITY"))
                    .addStatement(
                            "segment.asSlice(base + 4 + encodedLength, $L -"
                                    + " encodedLength).fill((byte) 0)",
                            constantName(field.name(), "FIXED_CAPACITY"))
                    .endControlFlow()
                    .addStatement("markWritten($L)", indexConst);
            if (optionalConst != null) {
                method.addStatement("presenceBits.set($L)", optionalConst);
            }
            method.addStatement("return this");
            return method.build();
        }

        if (isStringType(field)) {
            method.addParameter(String.class, "value");
            method.addStatement("$T.requireNonNull(value, \"value\")", objectsClass)
                    .addStatement("ensureWritable($L, $S)", indexConst, field.name())
                    .addStatement(
                            "throw new UnsupportedOperationException(\"Variable strings require"
                                    + " scratch buffer setter\")");
            return method.build();
        }

        TypeName javaType = getJavaTypeName(field.type());
        method.addParameter(javaType, "value")
                .addStatement("ensureWritable($L, $S)", indexConst, field.name())
                .addStatement(
                        "segment.set($T.$L, payloadBase + $T.$L, value)",
                        layoutsClass,
                        getLayoutConstantName(field.type()),
                        flyweightClass,
                        offsetConst)
                .addStatement("markWritten($L)", indexConst);
        if (optionalConst != null) {
            method.addStatement("presenceBits.set($L)", optionalConst);
        }
        method.addStatement("return this");
        return method.build();
    }

    /**
     * Creates a builder setter method for a repeating group field.
     * For primitive types: accepts an array parameter.
     * For complex types: accepts a count and a Consumer for populating elements.
     */
    private MethodSpec createRepeatingGroupSetter(
            ClassName builderClassName,
            ResolvedFieldDefinition field,
            int fieldIndex,
            Integer varSlot,
            Integer optionalBitIndex,
            ClassName layoutsClass,
            ClassName flyweightClass,
            ClassName objectsClass,
            ClassName varFieldWriterClass) {

        MethodSpec.Builder method =
                MethodSpec.methodBuilder("set" + capitalize(field.name()))
                        .addModifiers(Modifier.PUBLIC)
                        .returns(builderClassName);

        String indexConst = constantName(field.name(), "INDEX");
        String optionalConst =
                optionalBitIndex == null ? null : constantName(field.name(), "OPT_BIT");

        if (isRepeatedPrimitiveOrEnum(field)) {
            // For primitives: accept array and use RepeatingGroupBuilder
            TypeName elementType = getRepeatedElementType(field);
            ArrayTypeName arrayType = ArrayTypeName.of(elementType);
            String builderAddMethod = getRepeatingGroupBuilderAddMethod(field.type());

            method.addParameter(arrayType, "values")
                    .addJavadoc("Sets the repeated $L field with the given values.\n"
                            + "@param values the array of values to write\n"
                            + "@return this builder for chaining", field.name())
                    .addStatement("$T.requireNonNull(values, \"values\")", objectsClass)
                    .addStatement("ensureWritable($L, $S)", indexConst, field.name())
                    .beginControlFlow("if (varWriter == null)")
                    .addStatement("throw new IllegalStateException(\"Message has no variable fields\")")
                    .endControlFlow()
                    .addStatement("$T handle = varWriter.beginNestedField($L)",
                            ClassName.get("express.mvp.myra.codec.runtime", "VarFieldWriter", "NestedHandle"),
                            constantName(field.name(), "VAR_SLOT"))
                    .addStatement("long absoluteOffset = payloadBase + handle.relativeOffset()")
                    .addStatement("$T groupBuilder = new $T($L)",
                            RepeatingGroupBuilder.class, RepeatingGroupBuilder.class,
                            getRepeatedElementSize(field))
                    .addStatement("groupBuilder.wrap(segment, absoluteOffset)")
                    .beginControlFlow("for ($T value : values)", elementType)
                    .addStatement("groupBuilder.$L(value)", builderAddMethod)
                    .endControlFlow()
                    .addStatement("int bytesWritten = groupBuilder.finish()")
                    .addStatement("handle.finish(bytesWritten)")
                    .addStatement("markWritten($L)", indexConst);

            if (optionalConst != null) {
                method.addStatement("presenceBits.set($L)", optionalConst);
            }
            method.addStatement("return this");
            return method.build();

        } else if (isMessageType(field)) {
            // For nested messages: accept count and consumer
            ClassName childBuilder = ClassName.get(schema.namespace(), field.type() + "Builder");
            ParameterizedTypeName consumerType =
                    ParameterizedTypeName.get(ClassName.get(Consumer.class), childBuilder);

            method.addParameter(int.class, "count")
                    .addParameter(consumerType, "elementWriter")
                    .addJavadoc("Sets the repeated $L field with the given count.\n"
                            + "The consumer is called for each element to populate it.\n"
                            + "@param count the number of elements\n"
                            + "@param elementWriter the consumer to populate each element\n"
                            + "@return this builder for chaining", field.name())
                    .addStatement("$T.requireNonNull(elementWriter, \"elementWriter\")", objectsClass)
                    .addStatement("ensureWritable($L, $S)", indexConst, field.name())
                    .beginControlFlow("if (varWriter == null)")
                    .addStatement("throw new IllegalStateException(\"Message has no variable fields\")")
                    .endControlFlow()
                    .addStatement("$T handle = varWriter.beginNestedField($L)",
                            ClassName.get("express.mvp.myra.codec.runtime", "VarFieldWriter", "NestedHandle"),
                            constantName(field.name(), "VAR_SLOT"))
                    .addStatement("long absoluteOffset = payloadBase + handle.relativeOffset()")
                    .addStatement("$T groupBuilder = new $T()",
                            VariableSizeRepeatingGroupBuilder.class, VariableSizeRepeatingGroupBuilder.class)
                    .addStatement("groupBuilder.beginWithCount(segment, absoluteOffset, count)")
                    .beginControlFlow("for (int i = 0; i < count; i++)")
                    .addStatement("long elementStart = groupBuilder.beginElement()")
                    .addStatement("$T elementSlice = segment.asSlice(elementStart, segment.byteSize() - elementStart)",
                            MemorySegment.class)
                    .addStatement("$T nestedBuilder = $T.inline(elementSlice)", childBuilder, childBuilder)
                    .addStatement("elementWriter.accept(nestedBuilder)")
                    .addStatement("long nestedSize = nestedBuilder.finishInline()")
                    .addStatement("groupBuilder.endElement((int) nestedSize)")
                    .endControlFlow()
                    .addStatement("int bytesWritten = groupBuilder.finish()")
                    .addStatement("handle.finish(bytesWritten)")
                    .addStatement("markWritten($L)", indexConst);

            if (optionalConst != null) {
                method.addStatement("presenceBits.set($L)", optionalConst);
            }
            method.addStatement("return this");
            return method.build();

        } else if (isStringType(field)) {
            // For strings: accept String array
            method.addParameter(String[].class, "values")
                    .addParameter(MemorySegment.class, "scratchBuffer")
                    .addJavadoc("Sets the repeated $L field with the given string values.\n"
                            + "@param values the array of string values\n"
                            + "@param scratchBuffer scratch buffer for encoding\n"
                            + "@return this builder for chaining", field.name())
                    .addStatement("$T.requireNonNull(values, \"values\")", objectsClass)
                    .addStatement("$T.requireNonNull(scratchBuffer, \"scratchBuffer\")", objectsClass)
                    .addStatement("ensureWritable($L, $S)", indexConst, field.name())
                    .beginControlFlow("if (varWriter == null)")
                    .addStatement("throw new IllegalStateException(\"Message has no variable fields\")")
                    .endControlFlow()
                    .addStatement("$T handle = varWriter.beginNestedField($L)",
                            ClassName.get("express.mvp.myra.codec.runtime", "VarFieldWriter", "NestedHandle"),
                            constantName(field.name(), "VAR_SLOT"))
                    .addStatement("long absoluteOffset = payloadBase + handle.relativeOffset()")
                    .addStatement("$T groupBuilder = new $T()",
                            VariableSizeRepeatingGroupBuilder.class, VariableSizeRepeatingGroupBuilder.class)
                    .addStatement("groupBuilder.beginWithCount(segment, absoluteOffset, values.length)")
                    .beginControlFlow("for ($T value : values)", String.class)
                    .addStatement("groupBuilder.addString(value, scratchBuffer)")
                    .endControlFlow()
                    .addStatement("int bytesWritten = groupBuilder.finish()")
                    .addStatement("handle.finish(bytesWritten)")
                    .addStatement("markWritten($L)", indexConst);

            if (optionalConst != null) {
                method.addStatement("presenceBits.set($L)", optionalConst);
            }
            method.addStatement("return this");
            return method.build();

        } else {
            // For bytes: accept byte[][] array
            method.addParameter(byte[][].class, "values")
                    .addJavadoc("Sets the repeated $L field with the given byte arrays.\n"
                            + "@param values the array of byte arrays\n"
                            + "@return this builder for chaining", field.name())
                    .addStatement("$T.requireNonNull(values, \"values\")", objectsClass)
                    .addStatement("ensureWritable($L, $S)", indexConst, field.name())
                    .beginControlFlow("if (varWriter == null)")
                    .addStatement("throw new IllegalStateException(\"Message has no variable fields\")")
                    .endControlFlow()
                    .addStatement("$T handle = varWriter.beginNestedField($L)",
                            ClassName.get("express.mvp.myra.codec.runtime", "VarFieldWriter", "NestedHandle"),
                            constantName(field.name(), "VAR_SLOT"))
                    .addStatement("long absoluteOffset = payloadBase + handle.relativeOffset()")
                    .addStatement("$T groupBuilder = new $T()",
                            VariableSizeRepeatingGroupBuilder.class, VariableSizeRepeatingGroupBuilder.class)
                    .addStatement("groupBuilder.beginWithCount(segment, absoluteOffset, values.length)")
                    .beginControlFlow("for (byte[] value : values)")
                    .addStatement("groupBuilder.addBytes(value)")
                    .endControlFlow()
                    .addStatement("int bytesWritten = groupBuilder.finish()")
                    .addStatement("handle.finish(bytesWritten)")
                    .addStatement("markWritten($L)", indexConst);

            if (optionalConst != null) {
                method.addStatement("presenceBits.set($L)", optionalConst);
            }
            method.addStatement("return this");
            return method.build();
        }
    }

    /**
     * Gets the RepeatingGroupBuilder add method name for a primitive type.
     */
    private String getRepeatingGroupBuilderAddMethod(String schemaType) {
        String underlyingType = getUnderlyingType(schemaType);
        return switch (underlyingType) {
            case "bool" -> "addBoolean";
            case "int8" -> "addByte";
            case "int16" -> "addShort";
            case "int32" -> "addInt";
            case "int64" -> "addLong";
            case "float32" -> "addFloat";
            case "float64" -> "addDouble";
            default -> throw new IllegalArgumentException(
                    "Cannot get builder method for: " + schemaType);
        };
    }

    /**
     * Finds the underlying primitive type for a given schema type. If the type is an enum, it looks
     * up its definition. Otherwise, it returns the type name itself.
     */
    private boolean isEnum(String schemaType) {
        return schema.enums().stream().anyMatch(e -> e.name().equals(schemaType));
    }

    private String getUnderlyingType(String schemaType) {
        // Find a matching enum definition in the schema.
        return schema.enums().stream()
                .filter(e -> e.name().equals(schemaType))
                .findFirst()
                // If it's an enum, return its underlying type (e.g., "int8").
                .map(ResolvedEnumDefinition::type)
                // Otherwise, it's a primitive, so return the original name.
                .orElse(schemaType);
    }

    private boolean isFixedSize(ResolvedFieldDefinition field) {
        if (isMessageType(field) || field.repeated()) {
            return false;
        }
        String underlyingType = getUnderlyingType(field.type());
        if (underlyingType.equals("string") || underlyingType.equals("bytes")) {
            return field.fixedCapacity() != null;
        }
        return true;
    }

    private TypeName getJavaTypeName(String schemaType) {
        String underlyingType = getUnderlyingType(schemaType);
        return switch (underlyingType) {
            case "int8" -> TypeName.BYTE;
            case "int16" -> TypeName.SHORT;
            case "int32" -> TypeName.INT;
            case "int64" -> TypeName.LONG;
            case "float32" -> TypeName.FLOAT;
            case "float64" -> TypeName.DOUBLE;
            case "bool" -> TypeName.BOOLEAN;
            // For a custom enum type, the Java type is the enum class itself.
            default -> ClassName.get(schema.namespace(), schemaType);
        };
    }

    private String getLayoutConstantName(String schemaType) {
        String underlyingType = getUnderlyingType(schemaType);
        return switch (underlyingType) {
            case "bool" -> "BOOLEAN";
            case "int8" -> "BYTE";
            case "int16" -> "SHORT_BE";
            case "int32" -> "INT_BE";
            case "int64" -> "LONG_BE";
            case "float32" -> "FLOAT_BE";
            case "float64" -> "DOUBLE_BE";
            default ->
                    throw new IllegalArgumentException(
                            "Unsupported type for layout: " + schemaType);
        };
    }

    private int getFixedSize(ResolvedFieldDefinition field) {
        String underlyingType = getUnderlyingType(field.type());
        return switch (underlyingType) {
            case "bool", "int8" -> 1;
            case "int16" -> 2;
            case "int32", "float32" -> 4;
            case "int64", "float64" -> 8;
            case "string" -> {
                if (field.fixedCapacity() == null) {
                    throw new IllegalArgumentException(
                            "string field " + field.name() + " is missing fixedCapacity");
                }
                yield 4 + field.fixedCapacity();
            }
            default ->
                    throw new IllegalArgumentException(
                            "Cannot get fixed size for type: " + field.type());
        };
    }

    private Map<ResolvedFieldDefinition, Integer> optionalBitIndexes(
            List<ResolvedFieldDefinition> fields) {
        Map<ResolvedFieldDefinition, Integer> indexes = new LinkedHashMap<>();
        int next = 0;
        for (ResolvedFieldDefinition field : fields) {
            if (field.optional()) {
                indexes.put(field, next++);
            }
        }
        return indexes;
    }

    private String constantName(String fieldName, String suffix) {
        return fieldName.toUpperCase() + "_" + suffix;
    }

    private boolean isStringType(ResolvedFieldDefinition field) {
        return "string".equals(getUnderlyingType(field.type()));
    }

    private boolean isBytesType(ResolvedFieldDefinition field) {
        return "bytes".equals(getUnderlyingType(field.type()));
    }

    private boolean isMessageType(ResolvedFieldDefinition field) {
        return schema.messages().stream().anyMatch(m -> m.name().equals(field.type()));
    }

    private boolean isFixedInlineUtf8(ResolvedFieldDefinition field) {
        return isStringType(field) && field.fixedCapacity() != null;
    }

    /**
     * Determines if a repeated field contains fixed-size elements (primitives or enums).
     * Such fields use inline encoding: [count][element0][element1]...
     */
    private boolean isRepeatedPrimitiveOrEnum(ResolvedFieldDefinition field) {
        if (!field.repeated()) return false;
        String underlyingType = getUnderlyingType(field.type());
        return switch (underlyingType) {
            case "bool", "int8", "int16", "int32", "int64", "float32", "float64" -> true;
            default -> isEnum(field.type());
        };
    }

    /**
     * Gets the element size in bytes for a repeated primitive or enum field.
     */
    private int getRepeatedElementSize(ResolvedFieldDefinition field) {
        String underlyingType = getUnderlyingType(field.type());
        return switch (underlyingType) {
            case "bool", "int8" -> 1;
            case "int16" -> 2;
            case "int32", "float32" -> 4;
            case "int64", "float64" -> 8;
            default -> throw new IllegalArgumentException(
                    "Cannot get element size for type: " + field.type());
        };
    }

    /**
     * Gets the Java type for accessing elements of a repeated primitive field.
     */
    private TypeName getRepeatedElementType(ResolvedFieldDefinition field) {
        String underlyingType = getUnderlyingType(field.type());
        return switch (underlyingType) {
            case "bool" -> TypeName.BOOLEAN;
            case "int8" -> TypeName.BYTE;
            case "int16" -> TypeName.SHORT;
            case "int32" -> TypeName.INT;
            case "int64" -> TypeName.LONG;
            case "float32" -> TypeName.FLOAT;
            case "float64" -> TypeName.DOUBLE;
            default -> {
                if (isEnum(field.type())) {
                    yield ClassName.get(schema.namespace(), field.type());
                }
                throw new IllegalArgumentException(
                        "Cannot get element type for: " + field.type());
            }
        };
    }

    /**
     * Gets the iterator getter method name for a repeated primitive type.
     */
    private String getIteratorGetterMethod(String schemaType) {
        String underlyingType = getUnderlyingType(schemaType);
        return switch (underlyingType) {
            case "bool" -> "getBooleanAt";
            case "int8" -> "getByteAt";
            case "int16" -> "getShortAt";
            case "int32" -> "getIntAt";
            case "int64" -> "getLongAt";
            case "float32" -> "getFloatAt";
            case "float64" -> "getDoubleAt";
            default -> throw new IllegalArgumentException(
                    "Cannot get iterator method for: " + schemaType);
        };
    }

    private String getWriterMethodSuffix(String schemaType) {
        String underlyingType = getUnderlyingType(schemaType);
        return switch (underlyingType) {
            case "bool" -> "Boolean";
            case "int8" -> "Byte";
            case "int16" -> "ShortBE";
            case "int32" -> "IntBE";
            case "int64" -> "LongBE";
            case "string" -> "String";
            case "bytes" -> "Bytes";
            default ->
                    throw new IllegalArgumentException(
                            "Unsupported type for writer: " + schemaType);
        };
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
