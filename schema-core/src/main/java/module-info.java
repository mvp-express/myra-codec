/** Schema model, parsing, and resolution utilities for Myra Codec. */
module express.mvp.myra.codec.schema {
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires static com.github.spotbugs.annotations;

    exports express.mvp.myra.codec.schema;
    exports express.mvp.myra.codec.schema.resolver;
}
