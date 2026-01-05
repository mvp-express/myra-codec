package express.mvp.myra.codec.examples.generated.portfolio;

import java.lang.Override;
import java.lang.String;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Auto-generated writer implementation for Portfolio.
 *
 * Fill fields directly, then pass this instance to a builder.
 */
public final class PortfolioWriterImpl implements PortfolioWriter {
    public String accountId;

    public MemorySegment accountIdScratch;

    public int legsCount = 0;

    public LegWriter legsWriter;

    public String[] tagsValues;

    public MemorySegment tagsScratch;

    public byte[][] attachmentsValues;

    public String comment;

    public MemorySegment commentScratch;

    @Override
    public void writeTo(PortfolioBuilder builder, int index) {
        Objects.requireNonNull(builder, "builder");
        builder.setAccountId(this.accountId, this.accountIdScratch);
        if (this.legsWriter != null) {
            builder.setLegs(this.legsCount, this.legsWriter);
        }
        if (this.tagsValues != null) {
            builder.setTags(this.tagsValues, this.tagsScratch);
        }
        if (this.attachmentsValues != null) {
            builder.setAttachments(this.attachmentsValues);
        }
        if (this.comment != null) {
            builder.setComment(this.comment, this.commentScratch);
        }
    }
}
