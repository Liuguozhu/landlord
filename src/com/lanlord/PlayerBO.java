package com.lag.lanlord;

/**
 *
 * @author LGZ 斗地主player
 *
 */
public class PlayerBO {

    private int id;
    private int role;
    private int score;
    private boolean isWinner = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public void setWinner(boolean isWinner) {
        this.isWinner = isWinner;
    }

    @Override
    public String toString() {
        return "PlayerBO [id=" + id + ", role=" + role + ", score=" + score + ", isWinner=" + isWinner + "]";
    }

}
