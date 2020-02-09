package com.black.autobundlesample.model;

import java.io.Serializable;

/**
 * Created by wei.
 * Date: 2020-02-04 10:21
 * Description:
 */
public class Student implements Serializable {

    private String name;
    private String address;

    public Student() {
    }

    public Student(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
