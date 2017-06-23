package com.zhou;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhOu on 2017/6/22.
 */

public class User {
    public String name;
    public int age;
    public List<Source> sources = new ArrayList<>();

    public User() {
    }

    public User(String name, int age, List<Source> sources) {
        this.name = name;
        this.age = age;
        this.sources.addAll(sources);
    }
}
