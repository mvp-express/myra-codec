package express.mvp.myra.codec.examples;

import express.mvp.myra.codec.examples.generated.portfolio.LegArrayWriter;
import express.mvp.myra.codec.examples.generated.portfolio.LegFlyweight;
import express.mvp.myra.codec.examples.generated.portfolio.PortfolioBuilder;
import express.mvp.myra.codec.examples.generated.portfolio.PortfolioFlyweight;
import express.mvp.myra.codec.examples.generated.portfolio.Side;
import express.mvp.myra.codec.runtime.MessageEncoder;
import express.mvp.myra.codec.runtime.struct.MessageHeader;
import express.mvp.roray.ffm.utils.memory.MemorySegmentPool;
import express.mvp.roray.ffm.utils.memory.PooledSegment;
import express.mvp.roray.ffm.utils.memory.Utf8View;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;

/** Demonstrates nested and repeated field encoding for a portfolio schema. */
public final class PortfolioExampleApp {

    private PortfolioExampleApp() {}

    /** Runs a small portfolio encode/decode demo. */
    public static void main(String[] args) {
        MemorySegmentPool pool = new MemorySegmentPool(4096, 1, 8);
        MessageEncoder encoder = new MessageEncoder(pool);
        MemorySegment scratch = MemorySegment.ofArray(new byte[256]);

        LegArrayWriter legWriter = new LegArrayWriter();
        legWriter.count = 2;
        legWriter.symbol = new String[] {"AAPL", "MSFT"};
        legWriter.symbolScratch = scratch;
        legWriter.side = new Side[] {Side.BUY, Side.SELL};
        legWriter.quantity = new int[] {10, 20};

        byte[][] attachments = new byte[][] {new byte[] {1, 2}, new byte[] {3, 4, 5}};

        try (PooledSegment pooled =
                PortfolioBuilder.allocate(encoder, 2048)
                        .setAccountId("acct-77", scratch)
                        .setLegs(legWriter.count, legWriter)
                        .setTags(new String[] {"core", "long-term"}, scratch)
                        .setAttachments(attachments)
                        .setComment("review", scratch)
                        .build(
                                (short) PortfolioFlyweight.TEMPLATE_ID,
                                PortfolioFlyweight.SCHEMA_VERSION)) {
            decodeAndPrint(pooled.segment());
        }
    }

    private static void decodeAndPrint(MemorySegment segment) {
        PortfolioFlyweight fw = new PortfolioFlyweight();
        fw.wrap(segment, MessageHeader.HEADER_SIZE);
        fw.validate();

        System.out.println("accountId=" + fw.getAccountId().toString());

        System.out.println("legsCount=" + fw.getLegsCount());
        for (int i = 0; i < fw.getLegsCount(); i++) {
            LegFlyweight leg = fw.getLegsAt(i);
            String symbol = leg.getSymbol().toString();
            Side side = Side.fromId(Byte.toUnsignedInt(leg.getSide()));
            System.out.println(
                    "leg " + i + " " + symbol + " " + side + " qty=" + leg.getQuantity());
        }

        System.out.println("tagsCount=" + fw.getTagsCount());
        Utf8View tagView = new Utf8View();
        for (int i = 0; i < fw.getTagsCount(); i++) {
            fw.getTagsAt(i, tagView);
            System.out.println("tag " + i + " " + tagView.toString());
        }

        System.out.println("attachmentsCount=" + fw.getAttachmentsCount());
        for (int i = 0; i < fw.getAttachmentsCount(); i++) {
            MemorySegment attachment = fw.getAttachmentsAt(i);
            byte[] bytes = new byte[(int) attachment.byteSize()];
            MemorySegment.copy(
                    attachment, 0, MemorySegment.ofArray(bytes), 0, attachment.byteSize());
            System.out.println("attachment " + i + " " + Arrays.toString(bytes));
        }

        if (fw.hasComment()) {
            System.out.println("comment=" + fw.getComment().toString());
        } else {
            System.out.println("comment=ABSENT");
        }
    }
}
