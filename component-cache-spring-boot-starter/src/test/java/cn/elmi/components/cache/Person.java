package cn.elmi.components.cache;

import java.io.Serializable;

import lombok.Data;

@Data
public class Person implements Serializable {

    private String name = "xx";
    private int age = 100;

}
