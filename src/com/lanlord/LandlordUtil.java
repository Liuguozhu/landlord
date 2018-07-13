package com.lag.lanlord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.lag.lanlord.constant.Consts;
import com.lag.lanlord.util.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 3人斗地主出牌验证工具类
 *
 * @author LGZ
 */
public class LandlordUtil extends LandlordBaseUtil {
    private static Logger logger = LoggerFactory.getLogger(LandlordUtil.class);

    /**
     * Don't let anyone instantiate this class.
     */
    private LandlordUtil() {
        throw new Error("Don't instantiate this class!");
    }

    /**
     * 验证此次所出的牌型和上次牌型是否一致，如果一直，验证是否比上个玩家出的牌值大
     *
     * @param playerId              int
     * @param cardsList             List<Integer>
     * @param battle                AbstractBattleDouDiZhu
     * @param cardsReplaceSignboard 用来替代广告牌或者癞子的牌
     * @return boolean
     */
    public static boolean checkPlayCardsType(int playerId, List<Integer> cardsList,
                                             AbstractBattleDouDiZhu battle, int... cardsReplaceSignboard) {
        List<Integer> lastPlayCards = new ArrayList<>();
        battle.lastPlayCards.forEach(lastPlayCards::add);

        Map<Integer, int[]> playerCards = battle.playerCards;

        if (cardsList != null && cardsList.size() > 1) {
            Collections.sort(cardsList);
        }
        if (lastPlayCards.size() > 1) {
            Collections.sort(lastPlayCards);
        }
        // 验证玩家出的牌中是否有重复的牌
        if (ArrayUtil.hasSame(cardsList)) {
            logger.debug("玩家出的牌中有重复元素！" + cardsList);
            return false;
        }
        // 验证玩家是否包含这些手牌
        int[] cards = playerCards.get(playerId);
        Arrays.parallelSort(cards);
        assert cardsList != null;
        for (int card : cardsList) {
            if (!ArrayUtil.contains(cards, card)) {// 玩家手牌中木有这个牌
                return false;
            }
        }

        // XXX 带广告牌的斗地主，看看是不是有广告牌替代的牌
        if (battle.roomType == 404) {
            // 替换本次癞子的牌
            Integer signboard = Consts.SIGNBOARD;
            if (cardsList.contains(signboard)) {
                int index = cardsList.indexOf(signboard);
                if (cardsReplaceSignboard != null && cardsReplaceSignboard.length > 0) {
                    int replaceCard = cardsReplaceSignboard[0];
                    cardsList.set(index, replaceCard);
                }
            }
            // 替换上次癞子的牌
            if (lastPlayCards.contains(signboard)) {
                int index = lastPlayCards.indexOf(signboard);
                if (battle.lastReplaceCards != null && battle.lastReplaceCards.length > 0) {
                    int replaceCard = battle.lastReplaceCards[0];
                    lastPlayCards.set(index, replaceCard);
                }
            }
        }

        if (lastPlayCards.size() == 0) { // 当前没有玩家出牌，当前玩家是这一圈的首次出牌
            int isRocket = checkRocket(cardsList);
            if (isRocket > 0) {
                battle.playCardsType = isRocket;
                battle.addMultiple(2, false);// multiple*=2;
                return true;
            }
            int isBomb = checkBomb(cardsList);
            if (isBomb > 0) {
                battle.playCardsType = isBomb;
                battle.addMultiple(2, false);// multiple*=2;
                return true;
            }
            int isHigh = checkHigh(cardsList);
            if (isHigh > 0) {
                battle.playCardsType = isHigh;
                return true;
            }

            TreeMap<Integer, Integer> elementMap = constructTreeMap();
            TreeSet<Integer> set = parseCards(cardsList, elementMap);

            int isStraight = checkStraight(cardsList, set);
            if (isStraight > 0) {
                battle.playCardsType = isStraight;
                return true;
            }
            int isCheckPair = checkPair(cardsList);
            if (isCheckPair > 0) {
                battle.playCardsType = isCheckPair;
                return true;
            }
            int isSerialPair = checkSerialPair(cardsList, set, elementMap);
            if (isSerialPair > 0) {
                battle.playCardsType = isSerialPair;
                return true;
            }
            int isThreeCards = checkThreeCards(cardsList);
            if (isThreeCards > 0) {
                battle.playCardsType = isThreeCards;
                return true;
            }
            int isSerialThreeCards = checkSerialThreeCards(cardsList, elementMap, set);
            if (isSerialThreeCards > 0) {
                battle.playCardsType = isSerialThreeCards;
                return true;
            }
            int isThreeWithOne = checkThreeWithOne(cardsList, elementMap, set);
            if (isThreeWithOne > 0) {
                battle.playCardsType = isThreeWithOne;
                return true;
            }
            if (battle.can3With2) {
                int isThreeWithTwo = checkThreeWithTwo(cardsList, elementMap, set);
                if (isThreeWithTwo > 0) {
                    battle.playCardsType = isThreeWithTwo;
                    return true;
                }
            }
            int isAirplane = checkAirplane(cardsList, elementMap, set, battle.can3With2);
            if (isAirplane > 0) {
                battle.playCardsType = isAirplane;
                return true;
            }
            int isFourWithTwo = checkFourWithTwo(cardsList, elementMap, set);
            if (isFourWithTwo > 0) {
                battle.playCardsType = isFourWithTwo;
                return true;
            }
            return false;
        }

        if (checkRocket(lastPlayCards) > 0) {// 上个玩家出的王炸。
            return false;
        }
        // 验证王炸
        if (checkRocket(cardsList) > 0) {
            battle.playCardsType = Consts.TYPE_ROCKET;
            battle.addMultiple(2, false);// multiple*=2;
            return true;
        }
        // 验证炸弹
        if (checkBomb(cardsList) > 0) {
            if (compareBomb(cardsList, lastPlayCards)) {
                battle.playCardsType = Consts.TYPE_BOMB;
                battle.addMultiple(2, false);// multiple*=2;
                return true;
            }
            return false;
        }

        if (cardsList.size() == lastPlayCards.size()) {
            // 验证单牌
            if (checkHigh(cardsList) > 0 && checkHigh(lastPlayCards) > 0) {
                if (compareHigh(cardsList, lastPlayCards)) {
                    battle.playCardsType = Consts.TYPE_HIGH;
                    return true;
                }
                return false;
            }

            // 验证对子
            if (checkPair(cardsList) > 0 && checkPair(lastPlayCards) > 0) {
                if (comparePair(cardsList, lastPlayCards)) {
                    battle.playCardsType = Consts.TYPE_PAIR;
                    return true;
                }
                return false;
            }

            // 验证三张
            if (checkThreeCards(cardsList) > 0 && checkThreeCards(lastPlayCards) > 0) {
                if (compareThreeCards(cardsList, lastPlayCards)) {
                    battle.playCardsType = Consts.TYPE_THREE;
                    return true;
                }
                return false;
            }

            int[] array = parseCardsToArray(cardsList);
            int[] lastArray = parseCardsToArray(lastPlayCards);

            TreeMap<Integer, Integer> elementMap = constructTreeMap();
            TreeSet<Integer> recSet = parseCards(cardsList, elementMap);
            TreeMap<Integer, Integer> lastElementMap = constructTreeMap();
            TreeSet<Integer> lastSet = parseCards(lastPlayCards, lastElementMap);

            // 验证顺子
            if (checkStraight(cardsList, recSet) > 0 && checkStraight(lastPlayCards, lastSet) > 0) {
                if (compareStraight(array, lastArray)) {
                    battle.playCardsType = Consts.TYPE_STRAIGHT;
                    return true;
                }
                return false;
            }
            // 验证连对
            if (checkSerialPair(cardsList, recSet, elementMap) > 0
                    && checkSerialPair(lastPlayCards, lastSet, lastElementMap) > 0) {
                if (compareSerialPair(array, lastArray)) {
                    battle.playCardsType = Consts.TYPE_SERIAL_PAIR;
                    return true;
                }
                return false;
            }

            // 三张的顺子
            if (checkSerialThreeCards(cardsList, elementMap, recSet) > 0
                    && checkSerialThreeCards(lastPlayCards, lastElementMap, lastSet) > 0) {
                if (compareSerialThreeCards(array, lastArray)) {
                    battle.playCardsType = Consts.TYPE_SERIAL_THREE;
                    return true;
                }
                return false;
            }

            // 验证三带一
            if (checkThreeWithOne(cardsList, elementMap, recSet) > 0
                    && checkThreeWithOne(lastPlayCards, lastElementMap, lastSet) > 0) {
                if (compareThreeWithOne(elementMap, lastElementMap)) {
                    battle.playCardsType = Consts.TYPE_THREE_WITH_ONE;
                    return true;
                }
                return false;
            }

            // 验证三带二
            if (checkThreeWithTwo(cardsList, elementMap, recSet) > 0
                    && checkThreeWithTwo(lastPlayCards, lastElementMap, lastSet) > 0) {
                if (compareThreeWithTwo(elementMap, lastElementMap)) {
                    battle.playCardsType = Consts.TYPE_THREE_WITH_TWO;
                    return true;
                }
                return false;
            }

            // 验证飞机（两个及以上的连续的三带一或三带2）
            if (checkAirplane(cardsList, elementMap, recSet, battle.can3With2) > 0
                    && checkAirplane(lastPlayCards, lastElementMap, lastSet,
                    battle.can3With2) > 0) {
                if (compareAirPlane(elementMap, lastElementMap, cardsList)) {
                    battle.playCardsType = Consts.TYPE_AIRPLANE;
                    return true;
                }
                return false;
            }

            // 验证四带二
            if (checkFourWithTwo(cardsList, elementMap, recSet) > 0
                    && checkFourWithTwo(lastPlayCards, lastElementMap, lastSet) > 0) {
                if (compareFourWithTwo(elementMap, lastElementMap)) {
                    battle.playCardsType = Consts.TYPE_FOUR_WITH_TWO;
                    return true;
                }
                return false;
            }
        }

        return false;
    }

    /**
     * 炸弹比较： 如果本次是炸弹，之前不是，true。如果上次也是炸弹，验证是否比上次的大。
     *
     * @param cardsList List<Integer>
     * @return boolean
     */
    private static boolean compareBomb(List<Integer> cardsList, List<Integer> lastPlayCards) {
        if (checkBomb(lastPlayCards) > 0) {// 两个都是炸弹
            return compareCards(cardsList, lastPlayCards);
        } else {// 之前不是炸弹
            return true;
        }
    }

    /**
     * 单牌比较
     *
     * @param cardsList List<Integer>
     * @return boolean
     */
    protected static boolean compareHigh(List<Integer> cardsList, List<Integer> lastPlayCards) {
        int recType = getCardType(cardsList.get(0));
        int lastType = getCardType(lastPlayCards.get(0));

        // XXX 带广告牌的斗地主，广告牌替换的王，牌的type会是7,牌值会是225,226
        int kingType2 = getCardType(0XE1);
        if (recType == kingType2) {
            recType = Consts.KING_TYPE;
        }
        if (lastType == kingType2) {
            lastType = Consts.KING_TYPE;
        }

        // 王和高牌的对比
        if (recType == Consts.KING_TYPE && lastType != Consts.KING_TYPE)
            return true;
        if (lastType == Consts.KING_TYPE && recType != Consts.KING_TYPE)
            return false;

        return compareCards(cardsList, lastPlayCards);
    }

    /**
     * 对子的比较：两个对子中，本次牌值要比之前牌值大
     *
     * @param cardsList     List<Integer>
     * @param lastPlayCards List<Integer>
     * @return boolean
     */
    private static boolean comparePair(List<Integer> cardsList, List<Integer> lastPlayCards) {
        return compareCards(cardsList, lastPlayCards);
    }

    /**
     * 三张比较：两个三张中，本次牌值要比之前牌值大
     *
     * @param cardsList     List<Integer>
     * @param lastPlayCards List<Integer>
     * @return boolean
     */
    private static boolean compareThreeCards(List<Integer> cardsList, List<Integer> lastPlayCards) {
        return compareCards(cardsList, lastPlayCards);
    }

    /**
     * 顺子比较：两个顺子中，本次牌值中最小的要比之前牌值中最小的大
     *
     * @param array     int[]
     * @param lastArray int[]
     * @return boolean
     */
    private static boolean compareStraight(int[] array, int[] lastArray) {
        return compareCardsMore(array, lastArray);
    }

    /**
     * 连对的比较，两个连对中，本次牌值中最小的要比之前牌值中最小的大
     *
     * @param array     int[]
     * @param lastArray int[]
     * @return boolean
     */
    private static boolean compareSerialPair(int[] array, int[] lastArray) {
        return compareCardsMore(array, lastArray);
    }

    /**
     * 三张的顺子比较，两个三张的顺子中，本次牌值中最小的要比之前牌值中最小的大
     *
     * @param array     int[]
     * @param lastArray int[]
     * @return boolean
     */
    private static boolean compareSerialThreeCards(int[] array, int[] lastArray) {
        return compareCardsMore(array, lastArray);
    }

    /**
     * 三带一比较:两个三带1中，本次的三张 要比之前的三张大
     *
     * @param recMap  TreeMap<Integer, Integer>
     * @param lastMap TreeMap<Integer, Integer>
     * @return boolean
     */
    private static boolean compareThreeWithOne(TreeMap<Integer, Integer> recMap,  TreeMap<Integer, Integer> lastMap) {
        return compareTreeMap(recMap, lastMap, 3);
    }

    /**
     * 三带二的比较：两个三带2中，本次的三张 要比之前的三张大
     *
     * @param recMap  TreeMap<Integer, Integer>
     * @param lastMap TreeMap<Integer, Integer>
     * @return boolean
     */
    private static boolean compareThreeWithTwo(TreeMap<Integer, Integer> recMap, TreeMap<Integer, Integer> lastMap) {
        return compareTreeMap(recMap, lastMap, 3);
    }

    /**
     * 飞机比较 ：两个飞机中，本次最大的那个三张 要比之前最大的那个三张大
     *
     * @param recMap   TreeMap<Integer, Integer>
     * @param lastMap  TreeMap<Integer, Integer>
     * @param recCards List<Integer>
     * @return boolean
     */
    private static boolean compareAirPlane(TreeMap<Integer, Integer> recMap, TreeMap<Integer, Integer> lastMap,
                                           List<Integer> recCards) {
        int needSerialNum = 0;
        if (recCards.size() % 4 == 0) {
            needSerialNum = recCards.size() / 4;
        }
        if (recCards.size() % 5 == 0) {
            needSerialNum = recCards.size() / 5;
        }
        return compareAirPlaneTreeMap(recMap, lastMap, 3, needSerialNum);
    }

    /**
     * 四带二比较，两个4带2中，本次的那个四张要比之前的那个四张大
     *
     * @param recMap  TreeMap<Integer, Integer>
     * @param lastMap TreeMap<Integer, Integer>
     * @return boolean
     */
    private static boolean compareFourWithTwo(TreeMap<Integer, Integer> recMap, TreeMap<Integer, Integer> lastMap) {
        return compareTreeMap(recMap, lastMap, 4);
    }

    /**
     * 验证牌型是不是王炸
     *
     * @param cardsList List<Integer>
     * @return 是炸弹，返回true
     */
    private static int checkRocket(List<Integer> cardsList) {
        if (cardsList.size() == 2) {// 王炸
            // XXX 带广告牌的斗地主，广告牌替换的王，牌的type会是7,牌值会是225,226
            int kingType2 = getCardType(0XE1);
            int type1 = getCardType(cardsList.get(0));
            int type2 = getCardType(cardsList.get(1));
            if (type1 == kingType2) {
                type1 = Consts.KING_TYPE;
            }
            if (type2 == kingType2) {
                type2 = Consts.KING_TYPE;
            }
            if (type1 == Consts.KING_TYPE && type2 == Consts.KING_TYPE) {
                return Consts.TYPE_ROCKET;
            }
        }
        return -1;
    }

    /**
     * 验证牌型是不是炸弹
     *
     * @param cardsList List<Integer>
     * @return 是炸弹，返回true
     */
    private static int checkBomb(List<Integer> cardsList) {
        if (cardsList.size() == 4) {// 普通炸弹，4张一样
            if (isSameElement(cardsList)) {
                return Consts.TYPE_BOMB;
            }
        }
        return -1;
    }

    /**
     * 验证顺子
     *
     * @param cardsList List<Integer>
     * @return 顺子返回true
     */
    private static int checkStraight(List<Integer> cardsList, Set<Integer> set) {
        if (cardsList.size() < 5) {
            return -1;
        }
        if (set.size() != cardsList.size()) {
            return -1;
        }
        Integer[] array = set.toArray(new Integer[set.size()]);
        if (isSerialArray(array)) {
            return Consts.TYPE_STRAIGHT;
        }
        return -1;
    }

    /**
     * 验证连对
     *
     * @param cardsList List<Integer>
     * @return int
     */
    private static int checkSerialPair(List<Integer> cardsList, Set<Integer> set,
                                       TreeMap<Integer, Integer> elementMap) {
        int cardsSize = cardsList.size();
        if (cardsSize < 6 || cardsSize % 2 != 0) {
            return -1;
        }
        if (set.size() * 2 != cardsSize) {
            return -1;
        }
        for (int num : elementMap.values()) {
            if (num != 2) {
                return -1;
            }
        }
        Integer[] array = set.toArray(new Integer[set.size()]);
        if (isSerialArray(array)) {
            return Consts.TYPE_SERIAL_PAIR;
        }
        return -1;
    }

}
