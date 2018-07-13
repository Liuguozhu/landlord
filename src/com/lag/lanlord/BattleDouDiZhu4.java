package com.lag.lanlord;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.lag.lanlord.card.define.EnumCardTypePoker;

/**
 * 四人斗地主
 * 去掉两个黑三的一副牌
 *
 * @author LGZ
 */
public class BattleDouDiZhu4 extends AbstractBattleDouDiZhu {

    private static final int CARD_SIZE = 16;
    private static final int CARD_INIT_SIZE = 12;
    private static final int ONE_CARD_SIZE = 1;

    /**
     * 创建房间时调用
     *
     * @param attributes 一些房间属性
     */
    public BattleDouDiZhu4(List<Integer> attributes) {
        roomType = 402;
        if (attributes != null) {
            if (attributes.size() > 0) {
                playType = attributes.get(0);
            }
            if (attributes.size() > 2) {
                straightType = attributes.get(2);
            }
            if (attributes.size() > 3) {
                maxBombNum = attributes.get(3);
            }
        }
    }

    @Override
    protected void initCardPools() {
        cardPools.clear();
        putCardByType(EnumCardTypePoker.CLUB, cardPools, 1);// 去掉梅花3
        putCardByType(EnumCardTypePoker.DIAMOND, cardPools);
        putCardByType(EnumCardTypePoker.HEART, cardPools);
        putCardByType(EnumCardTypePoker.SPADE, cardPools, 1);// 去掉黑桃3
        putCardByType(EnumCardTypePoker.KING, cardPools);

    }

    /**
     * 生成牌色对应牌值
     *
     * @param cardType  EnumCardTypePoker
     * @param cardPools LinkedList<Integer>
     */
    protected void putCardByType(EnumCardTypePoker cardType, LinkedList<Integer> cardPools) {
        putCardByType(cardType, cardPools, 0);
    }

    /**
     * @param cardType  EnumCardTypePoker
     * @param cardPools LinkedList<Integer>
     * @param offset    偏移量，比如不要3-10，offset=8.就会从牌型定义的第8个值开始
     */
    protected void putCardByType(EnumCardTypePoker cardType, LinkedList<Integer> cardPools, int offset) {
        for (int i = cardType.getFirst() + offset; i <= cardType.getLast(); i++) {
            for (int j = 0; j < ONE_CARD_SIZE; j++) {
                cardPools.add(i);
            }
        }
    }

    @Override
    protected void initPlayerCard(int playerId) {
        int[] cards = playerCards.get(playerId);
        if (cards == null) {
            cards = new int[CARD_SIZE];
            playerCards.put(playerId, cards);
        } else {
            Arrays.parallelSetAll(cards, index -> 0);
        }

        for (int i = 0; i < CARD_INIT_SIZE; i++) {
            int card = cardPools.removeFirst();
            cards[i] = card;
        }
    }

    @Override
    protected boolean checkPlayCardsType(int playerId, List<Integer> cardsList, int... cardsReplaceSignBoard) {
        return LandlordUtil4.checkPlayCardsType(playerId, cardsList, this);
    }

}
