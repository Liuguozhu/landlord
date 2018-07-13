package com.lag.lanlord;

import com.lag.lanlord.card.define.EnumCardTypePoker;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * 战斗中各种操作
 *
 * @author Administrator
 */
public class BattleDouDiZhu extends AbstractBattleDouDiZhu {

    private static final int CARD_SIZE = 20;
    private static final int CARD_INIT_SIZE = 17;
    private static final int ONE_CARD_SIZE = 1;

    /**
     * 创建房间时调用
     *
     * @param attributes 一些房间属性
     * @param roomType   房间类型
     */
    public BattleDouDiZhu(List<Integer> attributes, int roomType) {
        this.roomType = roomType;
        int a2 = 1;
        if (attributes != null) {
            int size = attributes.size();
            if (size > 0) {
                playType = attributes.get(0);
            }
            if (size > 2) {
                a2 = attributes.get(2);
            }
            if (size > 4) {
                maxBombNum = attributes.get(4);
            }

        }
        can3With2 = a2 == 1;
    }

    protected void initCardPools() {
        cardPools.clear();
        putCardByType(EnumCardTypePoker.CLUB, cardPools);
        putCardByType(EnumCardTypePoker.DIAMOND, cardPools);
        putCardByType(EnumCardTypePoker.HEART, cardPools);
        putCardByType(EnumCardTypePoker.SPADE, cardPools);
        putCardByType(EnumCardTypePoker.KING, cardPools);
    }

    /**
     * 给玩家发牌
     *
     * @param playerId int
     */
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
    protected boolean checkPlayCardsType(int playerId, List<Integer> recCards,
                                         int... cardsReplaceSignBoard) {
        return LandlordUtil.checkPlayCardsType(playerId, recCards, this);
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

}
