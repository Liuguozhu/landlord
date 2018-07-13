package com.lag.lanlord;

import com.lag.lanlord.card.define.PokerCardUtil;
import com.lag.lanlord.constant.Consts;
import com.lag.lanlord.util.ArrayUtil;

import java.util.*;
import java.util.Map.Entry;


/**
 * 斗地主出牌验证工具类
 *
 * @author LGZ
 */
public class LandlordBaseUtil {

    /**
     * 获取牌值并去重
     *
     * @param cardsList List<Integer>
     * @return TreeSet<Integer>
     */
    public static TreeSet<Integer> parseCards(List<Integer> cardsList, TreeMap<Integer, Integer> elementMap) {
        TreeSet<Integer> set = new TreeSet<>();
        for (int card : cardsList) {
            int value = getCardValue(card);
            set.add(value);
            int num = elementMap.get(value) == null ? 0 : elementMap.get(value);
            elementMap.put(value, ++num);
        }
        return set;
    }

    /**
     * 获取牌值
     *
     * @param cardsList List<Integer>
     * @return int[]
     */
    public static int[] parseCardsToArray(List<Integer> cardsList) {
        int[] array = new int[cardsList.size()];
        int index = 0;
        for (int card : cardsList) {
            int value = getCardValue(card);
            array[index] = value;
            index++;
        }
        return array;
    }

    /**
     * 构造一个按照key由大到小排序的TreeMap
     *
     * @return TreeMap
     */
    public static TreeMap<Integer, Integer> constructTreeMap() {
        return new TreeMap<>((v1, v2) -> {
            return v2.compareTo(v1);
        });
    }

    /**
     * 获取map中，数量为n的最大的那个key
     *
     * @param elementMap TreeMap<Integer>
     * @param n          int
     * @return the max key, int
     */
    public static int getMaxKey(TreeMap<Integer, Integer> elementMap, int n) {
        for (Entry<Integer, Integer> entry : elementMap.entrySet()) {
            if (entry.getValue() == n) {
                return entry.getKey();
            }
        }
        return -1;
    }

    /**
     * 获取map中，数量为v的key的集合
     *
     * @param elementMap TreeMap
     * @param v          int
     * @return TreeSet<Integer>
     */
    public static TreeSet<Integer> getSameNumKey(TreeMap<Integer, Integer> elementMap, int v) {
        TreeSet<Integer> keySet = new TreeSet<>();
        elementMap.forEach((key, val) -> {
            if (val == v) {
                keySet.add(key);
            }
        });
        return keySet;
    }

    /**
     * 验证牌值是否相同
     *
     * @param cardsList List<Integer>
     * @return boolean
     */
    public static boolean isSameElement(List<Integer> cardsList) {
        Set<Integer> cardSet = new TreeSet<>();
        for (int card : cardsList) {
            int value = getCardValue(card);
            cardSet.add(value);
        }
        return 1 == cardSet.size();
    }

    /**
     * 验证数组是不是连续的数字，并且最小值为3，最大值为14
     *
     * @param array Integer[]
     * @return boolean
     */
    public static boolean isSerialArray(Integer[] array) {
        boolean flag = ArrayUtil.isSerial(array); // 顺子
        return flag && array[0] > 2 && array[array.length - 1] < 15;// 顺子且牌值是3-A之间
    }

    /**
     * 获取牌型
     *
     * @param card int
     * @return int
     */
    public static int getCardType(int card) {
        return PokerCardUtil.getType(card);
    }

    /**
     * 获取牌值
     *
     * @param card int
     * @return int
     */
    public static int getCardValue(int card) {
        return PokerCardUtil.getCard(card);
    }

    /**
     * 比较两个map中，value同为v的，最大key的大小
     *
     * @param map1 TreeMap
     * @param map2 TreeMap
     * @param v    int
     * @return boolean
     */
    protected static boolean compareTreeMap(TreeMap<Integer, Integer> map1, TreeMap<Integer, Integer> map2, int v) {
        int m1Max = getMaxKey(map1, v);
        int m2Max = getMaxKey(map2, v);
        return m1Max > m2Max;
    }

    /**
     * 三带
     *
     * @param cardsList  List
     * @param num        三带1还是三带2
     * @param elementMap TreeMap
     * @param set        Set
     * @return boolean
     */
    protected static boolean checkThreeWith(List<Integer> cardsList, int num, TreeMap<Integer, Integer> elementMap,
                                            Set<Integer> set) {
        if (cardsList.size() != num + 3) {
            return false;
        }
        if (set.contains(Consts.KING_VALUE_MIN) && set.contains(Consts.KING_VALUE_MAX)) {// 不能带对王
            return false;
        }
        // 排除4带1的情况
        int k4 = getMaxKey(elementMap, 4);
        if (k4 > -1) {
            return false;
        }
        int k3 = getMaxKey(elementMap, 3);
        if (k3 < 0) {
            return false;
        }

        return set.size() == 2;
    }

    /**
     * 单，对子，三张，4张,等牌型对比两个玩家哪个出的牌值比较大
     *
     * @param cardsList     List
     * @param lastPlayCards List
     * @return boolean
     */
    protected static boolean compareCards(List<Integer> cardsList, List<Integer> lastPlayCards) {
        int recValueMin = getCardValue(cardsList.get(0));
        int lastValueMin = getCardValue(lastPlayCards.get(0));
        return recValueMin > lastValueMin;
    }

    /**
     * 顺子，连对，三张的顺子,等牌型对比两个玩家哪个出的比较大
     *
     * @param array     int[]
     * @param lastArray int[]
     * @return boolean
     */
    protected static boolean compareCardsMore(int[] array, int[] lastArray) {
        Arrays.parallelSort(array);
        Arrays.parallelSort(lastArray);
        int recValueMin = getCardValue(array[0]);
        int lastValueMin = getCardValue(lastArray[0]);
        return recValueMin > lastValueMin;
    }

    /**
     * 比较飞机中的map
     *
     * @param recMap    TreeMap
     * @param lastMap   TreeMap
     * @param v         int
     * @param serialNum int
     * @return boolean
     */
    protected static boolean compareAirPlaneTreeMap(TreeMap<Integer, Integer> recMap, TreeMap<Integer, Integer>
            lastMap, int v, int serialNum) {
        int m1Max = getSerialMaxKey(recMap, v, serialNum);
        int m2Max = getSerialMaxKey(lastMap, v, serialNum);
        return m1Max > m2Max;
    }

    /**
     * 三个连续的三张带一个另外的三张算飞机，例如：777888999AAA,这个时候，最大的key是9，不能是A
     *
     * @param elementMap-每个牌值对应数量的map，key是牌值，value是这个牌值的数量
     * @param v-key对应的value，比如3个7，value是3
     * @param needSerialNum，需要key连续的个数。比如上面的牌型，需要连续的个数就是3
     * @return int，飞机中连续的三张中最大的那个，比如上面的牌型，返回9；再比如3334447788，返回4
     */
    private static int getSerialMaxKey(TreeMap<Integer, Integer> elementMap, int v, int needSerialNum) {
        Iterator<Entry<Integer, Integer>> iterator = elementMap.entrySet().iterator();
        int tempKey = 0;
        int serialNum = 1;
        int maxKey = 0;

        while (iterator.hasNext()) {
            Entry<Integer, Integer> entryNext = iterator.next();
            if (entryNext.getValue() == v) {
                int nextKey = entryNext.getKey();
                if (maxKey == 0) {// 拿出第一个key与后面的每一个比较看是不是连续的
                    tempKey = nextKey;
                    maxKey = nextKey;
                    continue;
                }
                if (tempKey - 1 == nextKey) {
                    serialNum++;
                } else {// 不连续，重新计算
                    serialNum = 1;
                    maxKey = nextKey;
                }
                if (serialNum == needSerialNum) {
                    return maxKey;
                }
                tempKey = nextKey;
            }
        }
        return 0;
    }

    /**
     * 验证牌型是不是单张
     *
     * @param cardsList List
     * @return 单张true, 否则 false
     */
    protected static int checkHigh(List<Integer> cardsList) {
        if (cardsList.size() == 1) {
            return Consts.TYPE_HIGH;
        }
        return -1;
    }

    /**
     * 验证牌型是不是对子
     *
     * @param cardsList List
     * @return 是对子true
     */
    protected static int checkPair(List<Integer> cardsList) {
        if (cardsList.size() == 2) {
            if (isSameElement(cardsList)) {
                return Consts.TYPE_PAIR;
            }
        }
        return -1;
    }

    /**
     * 验证三张
     *
     * @param cardsList List
     * @return int
     */
    protected static int checkThreeCards(List<Integer> cardsList) {
        if (cardsList.size() == 3) {
            if (isSameElement(cardsList)) {
                return Consts.TYPE_THREE;
            }
        }
        return -1;
    }

    /**
     * 三张顺子， 至少两组连续的三张， 例333444
     *
     * @param cardsList List
     * @param eleMap    TreeMap
     * @param set       set
     * @return int
     */
    protected static int checkSerialThreeCards(List<Integer> cardsList, TreeMap<Integer, Integer> eleMap,
                                               Set<Integer> set) {
        int cardsSize = cardsList.size();
        if (cardsSize < 6 || cardsSize % 3 != 0) {
            return -1;
        }
        if (set.size() * 3 != cardsSize) {
            return -1;
        }
        // 排除4带2的情况
        int v = getMaxKey(eleMap, 4);
        if (v > 3) {
            return -1;
        }
        Integer[] array = set.toArray(new Integer[set.size()]);
        if (isSerialArray(array)) {
            return Consts.TYPE_SERIAL_THREE;
        }
        return -1;
    }

    /**
     * 三带一
     *
     * @param cardsList  List
     * @param elementMap TreeMap
     * @param set        Set
     * @return int
     */
    protected static int checkThreeWithOne(List<Integer> cardsList, TreeMap<Integer, Integer> elementMap,
                                           Set<Integer> set) {
        if (checkThreeWith(cardsList, 1, elementMap, set)) {
            return Consts.TYPE_THREE_WITH_ONE;
        }
        return -1;
    }

    /**
     * 三带二
     *
     * @param cardsList  List
     * @param elementMap TreeMap
     * @param set        Set
     * @return int
     */
    protected static int checkThreeWithTwo(List<Integer> cardsList, TreeMap<Integer, Integer> elementMap,
                                           Set<Integer> set) {
        if (checkThreeWith(cardsList, 2, elementMap, set)) {
            return Consts.TYPE_THREE_WITH_TWO;
        }
        return -1;
    }

    /**
     * 验证飞机（两个及以上的连续的三带一或三带2）
     * 只有勾选三带对，飞机才可以带对，
     *
     * @param cardsList  List
     * @param elementMap TreeMap
     * @param set        Set
     * @param can3With2  boolean
     * @return int
     */
    protected static int checkAirplane(List<Integer> cardsList, TreeMap<Integer, Integer> elementMap,
                                       Set<Integer> set, boolean can3With2) {
        if (cardsList.size() < 8) {// 飞机至少由两个三带1组成
            return -1;
        }
        if (can3With2) {// 可以三带对
            if (cardsList.size() % 4 != 0 && cardsList.size() % 5 != 0) {// 长度既不是4的倍数（多个三带1），也不是5的倍数（多个三带2）
                return -1;
            }
        } else {// 不可以三带对，只能带单张
            if (cardsList.size() % 4 != 0) {// 长度既不是4的倍数（多个三带1）
                return -1;
            }
        }

        if (set.contains(1) && set.contains(2)) {// 不能带对王
            return -1;
        }
        TreeSet<Integer> keySet = getSameNumKey(elementMap, 3);// 有几个三带，将数量为3的牌值放入此集合中

        // 去重后,有n个三带，则牌值总数量小于等于n*2,
        if (set.size() > keySet.size() * 2) {
            return -1;
        }
        // 必须是连续的三带
        Integer[] array = keySet.toArray(new Integer[keySet.size()]);
        if (isSerialArray(array)) {
            // 有n个三带，则牌的总数量应该是这个n*4或n*5
            if (keySet.size() * 4 != cardsList.size() && keySet.size() * 5 != cardsList.size()) {
                return -1;
            }
            return Consts.TYPE_AIRPLANE;
        } else {
            if (cardsList.size() % 4 == 0) {// 三个连续的三张带一个另外的三张算飞机，例如：777888999333
                Iterator<Integer> iterator = keySet.iterator();
                int a;
                int serialNum = 1;
                if (iterator.hasNext()) {
                    a = iterator.next();
                    while (iterator.hasNext()) {
                        int aNext = iterator.next();
                        if (a + 1 == aNext) {
                            serialNum++;
                        } else {
                            serialNum = 1;
                        }
                        if (serialNum == cardsList.size() / 4) {
                            return Consts.TYPE_AIRPLANE;
                        }
                        a = aNext;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 四带二
     *
     * @param cardsList  List
     * @param elementMap TreeMap
     * @param set        Set
     * @return int
     */
    protected static int checkFourWithTwo(List<Integer> cardsList, TreeMap<Integer, Integer> elementMap,
                                          Set<Integer> set) {
        if (cardsList.size() != 6 && cardsList.size() != 8) {
            return -1;
        }
        if (set.contains(1) && set.contains(2)) {// 不能带对王
            return -1;
        }
        int k = getMaxKey(elementMap, 4);// 有没有四张的
        if (k < 0) {
            return -1;
        }
        if (cardsList.size() == 8) {// 排除 AAAABBBC的情况
            for (Entry<Integer, Integer> entry : elementMap.entrySet()) {
                if (entry.getValue() == 3) {
                    return -1;
                }
            }
        }
        if (set.size() == 3 || set.size() == 2) {
            return Consts.TYPE_FOUR_WITH_TWO;
        }
        return -1;
    }

}
