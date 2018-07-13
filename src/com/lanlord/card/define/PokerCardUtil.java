package com.lag.lanlord.card.define;

/**
 * 牌型解析
 */
public class PokerCardUtil {
    private static final int TYPE_OFFSET = 5;
    private static final int CARD_MASK = 0X1F;
    private static final int TYPE_MASK = 7 << TYPE_OFFSET;

    public static int getType(int card) {
        return (card & TYPE_MASK) >> TYPE_OFFSET;
    }

    public static int getCard(int card) {
        return card & CARD_MASK;
    }

    public static int restoreCard(int type, int value) {
        return type << TYPE_OFFSET | value;
    }
}
