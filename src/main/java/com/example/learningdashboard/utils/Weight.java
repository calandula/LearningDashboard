package com.example.learningdashboard.utils;

import java.io.Serializable;

public class Weight<L, R> implements Serializable {
    private L id;
    private R weight;

    public Weight(L id, R weight) {
        this.id = id;
        this.weight = weight;
    }

    public L getId() {
        return id;
    }

    public void setId(L id) {
        this.id = id;
    }

    public R getWeight() {
        return weight;
    }

    public void setWeight(R weight) {
        this.weight = weight;
    }
}
