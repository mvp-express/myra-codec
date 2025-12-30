package express.mvp.myra.codec.examples.generated.orderbook;

import java.lang.Override;
import java.lang.String;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Auto-generated writer implementation for OrderBookSnapshot.
 *
 * Fill fields directly, then pass this instance to a builder.
 */
public final class OrderBookSnapshotWriterImpl implements OrderBookSnapshotWriter {
    public String timestamp;

    public MemorySegment timestampScratch;

    public String venue;

    public MemorySegment venueScratch;

    public String symbol;

    public MemorySegment symbolScratch;

    public int instrumentId;

    public long sequence;

    public boolean isTrading;

    public TradingStatus tradingStatus;

    public TradeWriter lastTradeWriter;

    public int bidsCount = 0;

    public LevelWriter bidsWriter;

    public int asksCount = 0;

    public LevelWriter asksWriter;

    public int metadataCount = 0;

    public MetadataEntryWriter metadataWriter;

    @Override
    public void writeTo(OrderBookSnapshotBuilder builder, int index) {
        Objects.requireNonNull(builder, "builder");
        builder.setTimestamp(this.timestamp, this.timestampScratch);
        builder.setVenue(this.venue, this.venueScratch);
        builder.setSymbol(this.symbol, this.symbolScratch);
        builder.setInstrumentId(this.instrumentId);
        builder.setSequence(this.sequence);
        builder.setIsTrading(this.isTrading);
        if (this.tradingStatus != null) {
            builder.setTradingStatus(this.tradingStatus);
        }
        if (this.lastTradeWriter != null) {
            builder.setLastTrade(this.lastTradeWriter);
        }
        if (this.bidsWriter != null) {
            builder.setBids(this.bidsCount, this.bidsWriter);
        }
        if (this.asksWriter != null) {
            builder.setAsks(this.asksCount, this.asksWriter);
        }
        if (this.metadataWriter != null) {
            builder.setMetadata(this.metadataCount, this.metadataWriter);
        }
    }
}
