package com.example.learningdashboard.utils;

import java.io.Serializable;

public class Membership<L, R> implements Serializable {
    private L id;
    private R username;

    public Membership(L id, R username) {
        this.id = id;
        this.username = username;
    }

    public L getId() {
        return id;
    }

    public void setId(L id) {
        this.id = id;
    }

    public R getUsername() {
        return username;
    }

    public void setUsername(R username) {
        this.username = username;
    }
}
