package com.lag.lanlord.card.define;

/**
 * 牌型定义
 * 从3到2；3最小，2最大
 */
public enum EnumCardTypePoker {
    CLUB(0x23, 0x2F), // 梅花
    SPADE(0x43, 0x4F), // 黑桃
    DIAMOND(0x63, 0x6F), // 方片
    HEART(0x83, 0x8F), // 红桃
    KING(0xA1, 0xA2), // 王
    SIGNBOARD(0x10);// 广告牌

    private final int first;
    private final int last;

    EnumCardTypePoker(int first) {
        this.first = first;
        this.last = first;
    }

    EnumCardTypePoker(int first, int last) {
        this.first = first;
        this.last = last;
    }

    public int getFirst() {
        return first;
    }

    public int getLast() {
        return last;
    }

    public static EnumCardTypePoker getCard(int value) {
        for (EnumCardTypePoker pt : EnumCardTypePoker.values()) {
            if (pt.first <= value && pt.last >= value) {
                return pt;
            }
        }
        return null;
    }

}
