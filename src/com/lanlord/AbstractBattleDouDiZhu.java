package com.lag.lanlord;

import com.lag.lanlord.constant.Consts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractBattleDouDiZhu {
    protected int roomType;//房间类型，401经典斗地主；402四人斗地主，404带广告牌的斗地主
    protected List<Integer> players = new ArrayList<>();

    protected LinkedList<Integer> cardPools = new LinkedList<>();// 所有牌的牌池
    protected Map<Integer, int[]> playerCards = new HashMap<>();// 玩家手牌
    protected List<Integer> callPoints = new ArrayList<>();// 叫地主可以叫的分数
    protected List<Integer> lastPlayCards = new ArrayList<>();// 上个玩家出的牌
    protected Map<Integer, PlayerBO> joinMaps = new HashMap<>();// 玩家信息 （角色、等）
    protected Map<Integer, Integer> playCardsNumMaps = new HashMap<>();// 每局游戏中，每个玩家的出牌次数

    protected int playCardsType = 0;
    protected int[] lastReplaceCards = null;

    protected boolean can3With2 = true;// 可三带二
    protected int maxBombNum = 0;// 封顶炸弹翻倍数量 3炸，4炸，5炸，0不封顶
    protected int bombNum = 0;// 本局出的炸弹数量

    // 四人斗地主选项
    protected int straightType = 5;// 顺子类型，5:5张为顺子，3:3张为顺子；4:4张为顺子。默认5张起

    /**
     * 当前玩法 1叫分抢地主(默认玩法)；2抢地主;
     */
    protected int playType = 1;
    /**
     * 当前倍数 默认倍数为1
     */
    protected int multiple;
    /**
     * 当前叫分/抢地主玩家Id
     */
    protected int callId;


    /**
     * 检测打牌操作是否合法
     *
     * @param ownerId     出牌玩家id
     * @param recCards    出的牌
     * @param replaceCard 出牌中广告牌（癞子）用来抵哪张牌
     * @return boolean
     */
    protected boolean checkStepType(int ownerId, List<Integer> recCards, int... replaceCard) {

        boolean isPlayCardTrue = checkPlayCardsType(ownerId, recCards, replaceCard);
        if (!isPlayCardTrue) {
            //TODO 出牌错误返回一个出牌错误的错误码
        }
        return isPlayCardTrue;
    }

    // 开始牌局
    protected void begin() {
        // 初始化牌池，
        randomShuffle();
        // 发牌 设置初始步骤 发牌
        dealPlayerCard();
        // TODO 洗牌发牌结束，向客户端发送各个玩家手牌
        //TODO 通知上一局获胜者开始叫地主

    }

    protected void randomShuffle() {
        initCardPools();
        Collections.shuffle(cardPools); // 洗牌
    }

    /**
     * 初始发牌 重置上一局残留数据
     */
    protected void dealPlayerCard() {
        playerCards.clear();
        lastPlayCards.clear();
        lastReplaceCards = null;
        playCardsNumMaps.clear();
        multiple = 1;
        callPoints.clear();
        if (playType == 1) {
            callPoints.add(1);
            callPoints.add(2);
            callPoints.add(3);
        }
        if (callId == 0) {
            callId = players.get(0);
        }
        bombNum = 0;

        int playerId = callId;
        do {
            initPlayerCard(playerId);
            playerId = getNextPlayer(playerId);
        } while (playerId != callId);
    }


    /**
     * 验证一组玩家手牌中是否包含双王
     *
     * @param cards 玩家手牌
     * @return true有双王，false没有双王
     */
    private boolean checkDoubleKing(int[] cards) {
        int kingNum = 0;
        for (int c : cards) {
            if (c == Consts.KING_MIN) {
                kingNum++;
            }
            if (c == Consts.KING_MAX) {
                kingNum++;
            }
            if (kingNum >= 2) {
                return true;
            }
        }
        return false;
    }


    /**
     * 增加倍数的方法
     *
     * @param m，需要增加几倍
     * @param isReset，是否重置倍数，如果true，则用m替换之前的multiple，否则用m*multiple
     */
    protected void addMultiple(int m, boolean isReset) {
        if (isReset) {
            multiple = m;
            return;
        }
        if (maxBombNum > 0) {
            if (bombNum > maxBombNum) {
                return;
            }
        }
        multiple *= m;
    }

    /**
     * 抢地主结束，通知角色和底牌，通知地主出牌
     */
    protected void beginPlayCards() {
        callPoints.clear();
        setPlayerRole();
        //TODO 通知所有玩家谁是地主，通知玩家地主获得的三张底牌，通知地主开始出牌
    }

    /**
     * LGZ 设置玩家角色，地主/农民
     */
    protected void setPlayerRole() {
        int diZhuId = callId;
        for (Integer playerId : players) {
            PlayerBO player = new PlayerBO();
            player.setId(playerId);
            player.setScore(0);
            player.setWinner(false);
            if (playerId == diZhuId) {
                player.setRole(Consts.ROLE_LANDLORD);
            } else {
                player.setRole(Consts.ROLE_PEASANT);
            }
            joinMaps.put(playerId, player);
        }
    }

    /**
     * 验证本局是不是春天
     *
     * @return boolean
     */
    protected boolean spring() {
        // 地主id和地主出牌次数
        int landlordId = callId;
        int landlord = playCardsNumMaps.get(landlordId) == null ? 0 : playCardsNumMaps.get(landlordId);

        int peasant = 0;// 农民中有人出牌次数大于0，则大于0，反之等于0
        for (int playerId : players) {
            int playerCardsNum = playCardsNumMaps.get(playerId) == null ? 0 : playCardsNumMaps.get(playerId);
            PlayerBO player = joinMaps.get(playerId);
            if (player == null) {
                continue;
            }
            int role = player.getRole();
            if (role == Consts.ROLE_PEASANT) {
                // 有一个农民春天,地主只出过一次牌
                if (playerCardsNum > 0 && landlord == 1) {
                    return true;
                }
                // 地主和农民都没有春天，至少一个农民出过牌,地主出牌次数大于1
                if (playerCardsNum > 0 && landlord > 1) {
                    return false;
                }
                if (playerCardsNum > 0) {// 农民中有人出过牌
                    peasant++;
                }
            }
        }
        // 地主春天，农民中没有人出过牌，地主出完牌
        if (landlord > 0 && peasant == 0) {
            return true;
        }
        return false;
    }

    /**
     * 获得下一家操作玩家
     *
     * @param playerId int
     * @return int
     */
    protected int getNextPlayer(int playerId) {
        int index = players.indexOf(playerId);
        if (index == players.size() - 1) {
            index = 0;
        } else {
            index += 1;
        }
        return players.get(index);
    }


    /**
     * 初始化牌池 用几副牌，用不用王等
     */
    protected abstract void initCardPools();

    /**
     * 初始化玩家手牌，每个人几张，留几张底牌
     *
     * @param playerId int
     */
    protected abstract void initPlayerCard(int playerId);

    /**
     * 验证此次所出的牌型和上次牌型是否一致，如果一致，验证是否比上个玩家出的牌值大
     *
     * @param playerId              int
     * @param recCards              List<Integer>
     * @param cardsReplaceSignboard int...-用来替代广告牌或者癞子的牌
     */
    protected abstract boolean checkPlayCardsType(int playerId, List<Integer> recCards, int... cardsReplaceSignboard);

}
