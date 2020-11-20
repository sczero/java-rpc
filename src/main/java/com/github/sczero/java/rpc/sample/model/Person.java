package com.github.sczero.java.rpc.sample.model;

import java.io.Serializable;

public class Person implements Serializable {
    private String name;
    private int ageInt;
    private Integer ageInteger;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAgeInt() {
        return ageInt;
    }

    public void setAgeInt(int ageInt) {
        this.ageInt = ageInt;
    }

    public Integer getAgeInteger() {
        return ageInteger;
    }

    public void setAgeInteger(Integer ageInteger) {
        this.ageInteger = ageInteger;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", ageInt=" + ageInt +
                ", ageInteger=" + ageInteger +
                '}';
    }
}
