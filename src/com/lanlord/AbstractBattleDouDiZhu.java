package com.lanlord;

import com.lanlord.constant.Consts;

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
    protected int lastPlayerId = 0;//上个出牌玩家的id
    protected List<Integer> lastPlayCards = new ArrayList<>();// 上个玩家出的牌
    protected Map<Integer, PlayerBO> joinMaps = new HashMap<>();// 玩家信息 （角色、等）
    protected Map<Integer, Integer> playCardsNumMaps = new HashMap<>();// 每局游戏中，每个玩家的出牌次数
    protected Map<Integer, Boolean> callMaps = new HashMap<>();// 玩家叫地主情况
    protected Map<Integer, Boolean> redoubleMap = new HashMap<>();// 玩家加倍情况
    /**
     * 玩家当前权限，比如，某个玩家可以叫地主或某个玩家可以出牌，LinkedList里没有的，就是非法操作
     * LinkedList<权限对象> id + StepTypeDouDiZhu
     */
    protected LinkedList battleStepsCan = new LinkedList<>();

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
     * 上一局获胜者id，下一局叫地主玩家id
     */
    protected int lastWinerId;
    /**
     * 本局第一个点击了叫地主的玩家
     */
    protected int firstCallId;
    /**
     * 值为3(或玩家人数)的时候表示没人抢地主
     **/
    protected int pointoTime = 0;
    /**
     * 所有玩家总共可以过牌次数
     */
    protected int canPassTime;


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
        //重置上一局残留数据
        resetData();
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
     * 初始发牌
     */
    protected void dealPlayerCard() {
        int playerId = callId;
        do {
            initPlayerCard(playerId);
            playerId = getNextPlayer(playerId);
        } while (playerId != callId);
    }

    /**
     * 重置牌局信息，清理上一局残留数据
     */
    protected void resetData() {
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
        if (lastWinerId == 0) {
            lastWinerId = callId;
        }
        bombNum = 0;
        battleStepsCan.clear();
        redoubleMap.clear();
    }


    /**
     * 验证一组玩家手牌中是否包含双王，双王必须叫地主用
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
     * 抢地主的操作，设置下一个抢地主的用户，设置当前倍数
     *
     * @param playerId 用户id
     * @param stepType 用户操作类型（叫地主/抢地主/叫分）
     * @param point    叫地主叫的几分（0,1,2,3）。0代表不叫地主或不抢地主；
     */
    protected void grabLandlord(Integer playerId, int stepType, int point) {
        //TODO 验证玩家操作是否合法，是否有叫地主或者抢地主的权限

        //TODO ,清除当前玩家的权限（叫地主权限，等通知下个玩家叫地主的时候，设置下个玩家的权限是叫地主，以备操作合法性的验证使用）
        battleStepsCan.clear();

        if (callMaps.size() == players.size()) {
            callMaps.put(0, false);
        }

        if (point > 0) {
            if (stepType == StepTypeDouDiZhu.CALL_LANDLORD || (stepType == StepTypeDouDiZhu.CALL_POINT && firstCallId == 0)) {
                firstCallId = playerId;
            }
            callMaps.put(playerId, true);
            callId = playerId;
        } else {//不叫，不抢地主
            callMaps.put(playerId, false);
            pointoTime++;
        }

        if (stepType == StepTypeDouDiZhu.CALL_POINT) {// 叫分
            if (point > 0 && point < 4) {
                addMultiple(point, true);
                if (point == 3) {// 地主确定，抢地主结束
                    callPoints.clear();
                    //TODO 通知所有玩家当前玩家叫的分数
                    beginPlayCards();
                    return;
                }
                int index = callPoints.indexOf(point);
                callPoints = callPoints.subList(index + 1, callPoints.size());
            }
        }

        if (stepType == StepTypeDouDiZhu.GRAB_LANDLORD) {// 抢地主
            if (point > 0 && point > 0) {
                addMultiple(2, false);
            }
        }

        //TODO 通知所有玩家当前玩家叫的分数

        boolean isOnlyFirstCall = true;
        if (callMaps.size() == players.size()) {
            for (Map.Entry<Integer, Boolean> player : callMaps.entrySet()) {
                boolean isCall = player.getValue();
                if (player.getKey().intValue() != firstCallId && isCall) {
                    isOnlyFirstCall = false;
                    break;
                }
            }
        }
        // 抢地主结束
        if ((callMaps.size() == players.size() && (isOnlyFirstCall || pointoTime == players.size()))
                || callMaps.size() > players.size()) {
            if (pointoTime == players.size()) {// 没人叫地主，重新开始
                lastWinerId = getNextPlayer(lastWinerId);
                callId = lastWinerId;
                callMaps.clear();
                begin();
                return;
            }
            callPoints.clear();
            beginPlayCards();
            return;
        }

        // 通知下一个玩家抢地主
        int nextPlayerId;
        if (callMaps.size() < players.size()) {
            nextPlayerId = getNextPlayer(playerId);
        } else {
            nextPlayerId = firstCallId;
        }
        // 如果是叫地主，修改为通知可抢地主
        if (stepType == StepTypeDouDiZhu.CALL_LANDLORD && point > 0) {
            stepType = StepTypeDouDiZhu.GRAB_LANDLORD;
        }

        /*
        TODO 发送个客户端，通知所有玩家下一个玩家(nextPlayerId)可以抢地主,并给这个玩家设置权限
         battleStepsCan.add(new <权限对象> (用户id , StepTypeDouDiZhu.CAN_CALL_LANDLORD))
         */

    }

    /**
     * 抢地主结束，通知角色和底牌，通知地主出牌
     */
    protected void beginPlayCards() {
        callPoints.clear();
        setPlayerRole();
        //TODO 通知所有玩家谁是地主，通知玩家地主获得的三张底牌，通知地主开始出牌，并设置地主的权限为可出牌 battleStepsCan.add（new <权限对象>）
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
     * 出牌
     *
     * @param playerId   玩家id
     * @param playCards  玩家出的牌
     * @param attributes 广告牌（癞子）代替的哪张牌或哪几张牌
     */
    private void playCards(int playerId, List<Integer> playCards, List<Integer> attributes) {
        battleStepsCan.clear();
        int num = playCardsNumMaps.get(playerId) == null ? 0 : playCardsNumMaps.get(playerId);
        playCardsNumMaps.put(playerId, num + 1);
        // 移除手牌
        int[] holdCards = playerCards.get(playerId);
        for (int c : playCards) {
            for (int i = 0; i < holdCards.length; i++) {
                if (holdCards[i] == c) {
                    holdCards[i] = 0;
                    break;
                }
            }
        }
        lastPlayCards.clear();
        playCards.forEach(c -> lastPlayCards.add(c));
        lastPlayerId = playerId;
        int[] replaceCards = new int[attributes.size()];
        if (attributes.size() > 0) {
            for (int i = 0; i < replaceCards.length; i++) {
                replaceCards[i] = attributes.get(i);
            }
        }
        lastReplaceCards = replaceCards;
        // 当前玩家所有手牌都出完了，通知玩家手牌出完，出结算单
        if (checkFinish(holdCards)) {
            lastWinerId = playerId;

            //TODO 通知客户端当前玩家的操作，出的什么牌
            if (spring()) {// 春天，倍数翻倍。
                addMultiple(2, false);
                //TODO 通知春天，进行结算
                return;
            }

            //TODO 进行结算
            return;
        }

        canPassTime = players.size() - 1; // 当前玩家出牌。其余玩家都可以过牌。
        notifyCards(playerId, playCards, true, replaceCards);
    }

    /**
     * 过牌
     *
     * @param playerId
     */
    private void pass(int playerId) {
        battleStepsCan.clear();
        canPassTime -= 1;
        if (canPassTime <= 0) {// 其余玩家都过牌了，则上次出牌清零，第一个出牌玩家可以继续随意出牌。
            lastPlayCards.clear();
        }
        notifyCards(playerId, new ArrayList<>(), true);
    }

    /**
     * 通知所有玩家当前玩家出的牌 通知下一个玩家开始出牌
     *
     * @param playerId本次出牌玩家id
     * @param playCards为empty的时候，则表示当前玩家是过牌。
     * @param cardsReplaceSignBoard,广告牌（癞子）代替的是什么牌
     */
    private void notifyCards(int playerId, List<Integer> playCards, boolean isNext,
                             int... cardsReplaceSignBoard) {
        // TODO 通知所有玩家，当前玩家出的牌是什么

        // 通知下一个玩家可以出牌
        int nextPlayerId = playerId;
        if (isNext) {
            nextPlayerId = getNextPlayer(playerId);
        }
        //TODO 向客户端发送下一个玩家可以出牌的消息，并设置nextPlayerId的权限为可出牌
    }

    /**
     * 验证 int 类型的数组是否所有元素都为0
     *
     * @param _arr
     * @return boolean
     */
    protected boolean checkFinish(int[] _arr) {
        for (int i = 0; i < _arr.length; i++) {
            if (0 != _arr[i]) {
                return false;
            }
        }
        return true;
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
     * 结算
     *
     * @param isNormalFinish 是正常打完还是中途解散
     * @param isDestroy      是否结束战斗
     */
    public synchronized void calculateResult(boolean isNormalFinish, boolean isDestroy) {
        // TODO 增加战斗局数
        //TODO 设置结算信息 Sttlement

        int scorePeasantBase = 0;
        int winRole = 0;
        if (isNormalFinish) {// 是正常打完牌结束
            setSettlementStep();

            int winerId = lastWinerId;
            winRole = joinMaps.get(winerId).getRole();

            if (winRole == Consts.ROLE_LANDLORD) {// 地主获胜
                scorePeasantBase = -1 * multiple;
            } else {// 农民获胜
                scorePeasantBase = multiple;
            }
        } else {// 是投票解散
            setVoteDestoryRoomStep();
        }
        int scoreLandlord = 0;
        for (int playerId : players) {// 设置农民的结算信息，并据此计算地主的分数，

            int scorePeasant = 0;
            if (isNormalFinish) {
                PlayerBO player = joinMaps.get(playerId);
                int role = player.getRole();
                if (role == Consts.ROLE_LANDLORD) {
                    continue;
                }
                scorePeasant = getPeasantScore(playerId, scorePeasantBase);
                scoreLandlord -= scorePeasant;
            }
            setSettlementScore(playerId, winRole, scorePeasant);
        }

        if (isNormalFinish) {
            // TODO 设置地主的分数，为农民玩家的分数之和的负数
            setSettlementScore(callId, winRole, scoreLandlord);
        }

    }

    /**
     * 计算农民的分数
     *
     * @param playerId
     * @param scorePeasantBase
     * @return
     */
    private int getPeasantScore(Integer playerId, int scorePeasantBase) {
        int extraMultiple = 1;// 额外倍数
        Boolean isLandlordRedouble = redoubleMap.get(callId);
        // 地主加倍，所有玩家的分数都*2
        if (isLandlordRedouble != null && isLandlordRedouble.booleanValue()) {
            // 左位移1 等于 *2的1次方，位移2 等于 * 2的2次方
            extraMultiple = extraMultiple << 1;
        }
        Boolean isRedouble = redoubleMap.get(playerId);// 看看该玩家有没有选择加倍
        // 该农民玩家也加倍了，分数再*2
        if (isRedouble != null && isRedouble.booleanValue()) {
            extraMultiple = extraMultiple << 1;
        }
        return scorePeasantBase * extraMultiple;
    }

    /**
     * 单局结算的Step
     *
     * @param Sttlement
     */
    private void setSettlementStep() {
        //TODO Sttlement设置当前 StepTypeDouDiZhu为SETTLEMENT ,以及其他信息
    }

    /**
     * 投票解散的step
     *
     * @param Sttlement
     */
    private void setVoteDestoryRoomStep() {
        //TODO Sttlement设置当前 StepTypeDouDiZhu 为 VOTE_DESTROY_ROOM，以及其他信息
    }

    /**
     * 设置玩家分数，更新积分，设置单局结算数据
     *
     * @param playerId    玩家id
     * @param winRole     获胜角色是地主还是农民
     * @param balance     玩家单局信息，本局剩余手牌本局分数，获胜还是失败，本局倍数，本局底牌
     * @param census      玩家总积分，获胜次数，地主次数等总战绩信息
     * @param score       本局分数
     * @param dataBuilder Sttlement结算数据对象
     */
    private void setSettlementScore(int playerId, int winRole, int score) {
        //TODO   自行实现，单局战绩和总战绩
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
