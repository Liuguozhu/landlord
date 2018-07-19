package com.lanlord;

public interface StepTypeDouDiZhu {
    final int SEND_CARD = 1;// 发牌
    final int NOTIFY_ROLE = 6;// 通知玩家角色
    final int NOTIFY_HOLE_CARDS = 7;// 通知玩家底牌
    final int VOTE_DESTROY_ROOM = 8;// 投票解散房间
    final int SETTLEMENT = 9;// 单局结算
    final int RECONNECT = 10;// 断线重连
    final int SPRING = 12;// 春天
    final int PASS = 100;// 过牌
    final int CAN_PLAY_CARDS = 10003;// 可以出牌
    final int PLAY_CARDS = 3;// 出牌
    final int CAN_CALL_POINT = 10004;// 可以叫分
    final int CALL_POINT = 4;// 叫分
    final int CAN_CALL_LANDLORD = 10002;// 可以叫地主
    final int CALL_LANDLORD = 2;// 叫地主
    final int CAN_GRAB_LANDLORD = 10005;// 可以抢地主
    final int GRAB_LANDLORD = 5;// 抢地主
    final int CAN_REDOUBLE = 10011;// 可以加倍
    final int REDOUBLE = 11;// 加倍


}
