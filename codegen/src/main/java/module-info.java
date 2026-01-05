/** Code generation CLI and helpers for Myra Codec schemas. */
module express.mvp.myra.codec.codegen {
    requires com.palantir.javapoet;
    requires express.mvp.myra.codec.schema;
    requires express.mvp.roray.ffm;
    requires info.picocli;
    requires java.compiler;

    exports express.mvp.myra.codec.codegen;
}
