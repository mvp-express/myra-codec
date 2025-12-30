package express.mvp.myra.codec.examples;

import express.mvp.myra.codec.examples.generated.orderbook.*;
import express.mvp.myra.codec.runtime.MessageEncoder;
import express.mvp.myra.codec.runtime.struct.MessageHeader;
import express.mvp.roray.ffm.utils.memory.MemorySegmentPool;
import express.mvp.roray.ffm.utils.memory.PooledSegment;
import java.lang.foreign.MemorySegment;

public class ExampleApp {
    public static void main(String[] args) {
        MemorySegmentPool pool = new MemorySegmentPool(4096, 1, 16);
        MessageEncoder encoder = new MessageEncoder(pool);

        MemorySegment scratch = MemorySegment.ofArray(new byte[256]);

        // Reusable writers (no lambdas, no per-call allocation)
        TradeWriterImpl tradeWriter = new TradeWriterImpl();
        LevelArrayWriter bidsWriter = new LevelArrayWriter();
        LevelArrayWriter asksWriter = new LevelArrayWriter();
        MetadataEntryArrayWriter metaWriter = new MetadataEntryArrayWriter();

        // Example 1: full snapshot (all optionals present, 2 bids, 2 asks, 1 metadata)
        tradeWriter.priceNanos = 123_450_000L;
        tradeWriter.size = 100;
        tradeWriter.aggressor = "BUY";
        tradeWriter.aggressorScratch = scratch;

        bidsWriter.count = 2;
        bidsWriter.priceNanos = new long[] {123_440_000L, 123_430_000L};
        bidsWriter.size = new int[] {10, 5};
        bidsWriter.orderCount = new int[] {2, 1};
        bidsWriter.hasMaker = new boolean[] {true, true};
        bidsWriter.maker = new boolean[] {true, false};

        asksWriter.count = 2;
        asksWriter.priceNanos = new long[] {123_460_000L, 123_470_000L};
        asksWriter.size = new int[] {12, 6};
        asksWriter.orderCount = new int[] {1, 1};
        asksWriter.hasMaker = new boolean[] {true, true};
        asksWriter.maker = new boolean[] {false, false};

        metaWriter.count = 1;
        metaWriter.key = new String[] {"source"};
        metaWriter.value = new String[] {"demo"};
        metaWriter.keyScratch = scratch;
        metaWriter.valueScratch = scratch;

        try (PooledSegment pooled =
                OrderBookSnapshotBuilder.allocate(encoder, 2048)
                        .setTimestamp("2024-01-01T12:00:00Z", scratch)
                        .setVenue("XNAS", scratch)
                        .setSymbol("AAPL", scratch)
                        .setInstrumentId(42)
                        .setSequence(1001L)
                        .setIsTrading(true)
                        .setTradingStatus(TradingStatus.OPEN)   // enum overload
                        .setLastTrade(tradeWriter)              // writer overload
                        .setBids(bidsWriter.count, bidsWriter)
                        .setAsks(asksWriter.count, asksWriter)
                        .setMetadata(metaWriter.count, metaWriter)
                        .build((short) OrderBookSnapshotFlyweight.TEMPLATE_ID,
                               OrderBookSnapshotFlyweight.SCHEMA_VERSION)) {

            decodeAndPrint("FULL", pooled.segment());
        }

        // Example 2: minimal snapshot (optionals absent, empty repeats)
        try (PooledSegment pooled =
                OrderBookSnapshotBuilder.allocate(encoder, 2048)
                        .setTimestamp("2024-01-01T12:00:01Z", scratch)
                        .setVenue("XNAS", scratch)
                        .setSymbol("AAPL", scratch)
                        .setInstrumentId(42)
                        .setSequence(1002L)
                        .setIsTrading(false)
                        .setBids(0, bidsWriter)     // zero count
                        .setAsks(0, asksWriter)     // zero count
                        .setMetadata(0, metaWriter)  // zero count
                        .build((short) OrderBookSnapshotFlyweight.TEMPLATE_ID,
                               OrderBookSnapshotFlyweight.SCHEMA_VERSION)) {

            decodeAndPrint("MIN", pooled.segment());
        }
    }

    private static void decodeAndPrint(String label, MemorySegment segment) {
        OrderBookSnapshotFlyweight fw = new OrderBookSnapshotFlyweight();
        fw.wrap(segment, MessageHeader.HEADER_SIZE);
        fw.validate();

        System.out.println("[" + label + "]");
        System.out.println("symbol=" + fw.getSymbol().toString());
        System.out.println("seq=" + fw.getSequence());
        System.out.println("isTrading=" + fw.getIsTrading());

        if (fw.hasTradingStatus()) {
            TradingStatus status =
                    TradingStatus.fromId(Byte.toUnsignedInt(fw.getTradingStatus()));
            System.out.println("tradingStatus=" + status);
        } else {
            System.out.println("tradingStatus=ABSENT");
        }

        if (fw.hasLastTrade()) {
            TradeFlyweight trade = fw.getLastTrade();
            System.out.println("lastTradePrice=" + trade.getPriceNanos());
        } else {
            System.out.println("lastTrade=ABSENT");
        }

        System.out.println("bidsCount=" + fw.getBidsCount());
        for (int i = 0; i < fw.getBidsCount(); i++) {
            LevelFlyweight lvl = fw.getBidsAt(i);
            System.out.println("bid " + i + " " + lvl.getPriceNanos() + " x " + lvl.getSize());
        }

        System.out.println("asksCount=" + fw.getAsksCount());
        for (int i = 0; i < fw.getAsksCount(); i++) {
            LevelFlyweight lvl = fw.getAsksAt(i);
            System.out.println("ask " + i + " " + lvl.getPriceNanos() + " x " + lvl.getSize());
        }

        System.out.println("metaCount=" + fw.getMetadataCount());
        for (int i = 0; i < fw.getMetadataCount(); i++) {
            MetadataEntryFlyweight entry = fw.getMetadataAt(i);
            System.out.println(entry.getKey().toString() + "=" + entry.getValue().toString());
        }
    }

}
