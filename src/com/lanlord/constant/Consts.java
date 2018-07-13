package com.lag.lanlord.constant;


import com.lag.lanlord.LandlordBaseUtil;
import com.lag.lanlord.card.define.EnumCardTypePoker;

public interface Consts {
     int ROLE_PEASANT = 1; // 农民
     int ROLE_LANDLORD = 2; // 地主

     int TYPE_HIGH = 1; // 单牌
     int TYPE_PAIR = 2; // 一对
     int TYPE_THREE = 3; // 三张
     int TYPE_STRAIGHT = 4; // 顺子 例34567
     int TYPE_SERIAL_PAIR = 5; // 连对 例778899
     int TYPE_THREE_WITH_ONE = 6; // 三带1 例3335
     int TYPE_THREE_WITH_TWO = 7; // 三带2 例88866
     int TYPE_SERIAL_THREE = 8; // 三张的顺子 例777888
     int TYPE_AIRPLANE = 9; // 飞机 多个连续的三带1或连续的三带2 例777888999456
     int TYPE_FOUR_WITH_TWO = 10; // 四带二或四带两对
     int TYPE_BOMB = 11; // 炸弹
     int TYPE_ROCKET = 12; // 火箭(王炸)

    // 部分牌值
     int KING_MIN = EnumCardTypePoker.KING.getFirst();// 小王
     int KING_MAX = EnumCardTypePoker.KING.getLast();// 大王
     int SIGNBOARD = EnumCardTypePoker.SIGNBOARD.getFirst();// 广告牌
     int KING_TYPE = LandlordBaseUtil.getCardType(KING_MIN); // 大小王的type
     int KING_VALUE_MIN = LandlordBaseUtil.getCardValue(KING_MIN);// 小王解析后的value
     int KING_VALUE_MAX = LandlordBaseUtil.getCardValue(KING_MAX);// 大王解析后的value


}
