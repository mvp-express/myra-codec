package express.mvp.myra.codec.schema.resolver;

/**
 * Result of schema resolution.
 *
 * @param resolvedSchema the resolved schema with stable ids
 * @param updatedLockFile the lock file capturing the resolved ids
 */
public record ResolutionResult(ResolvedSchemaDefinition resolvedSchema, LockFile updatedLockFile) {}
