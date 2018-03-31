package com.skillclient.obfuscation;

public class Test {
    public static Object[] objects = new Object[55];

    public static void main(String[] args) {
        objects[5] = (0x8F874E18 ^ 0xF078B1E7);
        System.out.println((int) objects[5]);
    }
}
