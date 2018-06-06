package com.jht.homegym.dao;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class FreeTraining {
    @Id
    private long id;
    private long userId;
    private String curTime;
    private String totalTime;
    private int pullRopeNum;
    private int skipRopeNum;
    private int dumbbellNum;
    private int totalNum;

    public FreeTraining() {
    }

    public FreeTraining(long id, long userId, String curTime, String totalTime, int pullRopeNum, int skipRopeNum, int dumbbellNum, int totalNum) {
        this.id = id;
        this.userId = userId;
        this.curTime = curTime;
        this.totalTime = totalTime;
        this.pullRopeNum = pullRopeNum;
        this.skipRopeNum = skipRopeNum;
        this.dumbbellNum = dumbbellNum;
        this.totalNum = totalNum;
    }

    @Override
    public String toString() {
        return "FreeTraining{" +
                "id=" + id +
                ", userId=" + userId +
                ", curTime='" + curTime + '\'' +
                ", totalTime='" + totalTime + '\'' +
                ", pullRopeNum=" + pullRopeNum +
                ", skipRopeNum=" + skipRopeNum +
                ", dumbbellNum=" + dumbbellNum +
                ", totalNum=" + totalNum +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getCurTime() {
        return curTime;
    }

    public void setCurTime(String curTime) {
        this.curTime = curTime;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public int getPullRopeNum() {
        return pullRopeNum;
    }

    public void setPullRopeNum(int pullRopeNum) {
        this.pullRopeNum = pullRopeNum;
    }

    public int getSkipRopeNum() {
        return skipRopeNum;
    }

    public void setSkipRopeNum(int skipRopeNum) {
        this.skipRopeNum = skipRopeNum;
    }

    public int getDumbbellNum() {
        return dumbbellNum;
    }

    public void setDumbbellNum(int dumbbellNum) {
        this.dumbbellNum = dumbbellNum;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }
}
