package express.mvp.myra.codec.bench.shared.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class OrderBookSnapshot {
    private String timestamp;
    private String venue;
    private String symbol;
    private int instrumentId;
    private long sequence;
    private boolean isTrading;
    private String tradingStatus;
    private Trade lastTrade;
    private List<Level> bids;
    private List<Level> asks;
    private List<MetadataEntry> metadata;

    public OrderBookSnapshot() {
        this.bids = new ArrayList<>();
        this.asks = new ArrayList<>();
        this.metadata = new ArrayList<>();
    }

    public OrderBookSnapshot(String timestamp,
                             String venue,
                             String symbol,
                             int instrumentId,
                             long sequence,
                             boolean isTrading,
                             String tradingStatus,
                             Trade lastTrade,
                             List<Level> bids,
                             List<Level> asks,
                             List<MetadataEntry> metadata) {
        this.timestamp = timestamp;
        this.venue = venue;
        this.symbol = symbol;
        this.instrumentId = instrumentId;
        this.sequence = sequence;
        this.isTrading = isTrading;
        this.tradingStatus = tradingStatus;
        this.lastTrade = lastTrade;
        this.bids = bids == null ? new ArrayList<>() : new ArrayList<>(bids);
        this.asks = asks == null ? new ArrayList<>() : new ArrayList<>(asks);
        this.metadata = metadata == null ? new ArrayList<>() : new ArrayList<>(metadata);
    }

    public String timestamp() {
        return timestamp;
    }

    public String venue() {
        return venue;
    }

    public String symbol() {
        return symbol;
    }

    public int instrumentId() {
        return instrumentId;
    }

    public long sequence() {
        return sequence;
    }

    public boolean isTrading() {
        return isTrading;
    }

    public String tradingStatus() {
        return tradingStatus;
    }

    public Trade lastTrade() {
        return lastTrade;
    }

    public List<Level> bids() {
        return bids;
    }

    public List<Level> asks() {
        return asks;
    }

    public List<MetadataEntry> metadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "OrderBookSnapshot{" +
                "timestamp='" + timestamp + '\'' +
                ", venue='" + venue + '\'' +
                ", symbol='" + symbol + '\'' +
                ", instrumentId=" + instrumentId +
                ", sequence=" + sequence +
                ", isTrading=" + isTrading +
                ", tradingStatus='" + tradingStatus + '\'' +
                ", lastTrade=" + lastTrade +
                ", bids=" + bids +
                ", asks=" + asks +
                ", metadata=" + metadata +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, venue, symbol, instrumentId, sequence, isTrading, tradingStatus, lastTrade, bids, asks, metadata);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OrderBookSnapshot other)) {
            return false;
        }
        return instrumentId == other.instrumentId
                && sequence == other.sequence
                && isTrading == other.isTrading
                && Objects.equals(timestamp, other.timestamp)
                && Objects.equals(venue, other.venue)
                && Objects.equals(symbol, other.symbol)
                && Objects.equals(tradingStatus, other.tradingStatus)
                && Objects.equals(lastTrade, other.lastTrade)
                && Objects.equals(bids, other.bids)
                && Objects.equals(asks, other.asks)
                && Objects.equals(metadata, other.metadata);
    }
}
