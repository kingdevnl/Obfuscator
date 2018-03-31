package com.skillclient.obfuscation;

import java.util.Random;

public class Test {

    public static Random r = new Random();

    public static void main(String[] args) {
        int a = r.nextInt(256) + 256;
        int b = r.nextInt(Integer.MAX_VALUE/2) + Integer.MAX_VALUE/4;
        int c = r.nextInt(Integer.MAX_VALUE/2) + Integer.MAX_VALUE/4;
        System.out.println(a + " " + b + " " + c + " " + a(a, b, c));
    }

    public static int a(int a, int b, int c) {
        b *= a*a;
        b -= a;
        b >>>= 2;
        b = (int) ((long)b*b) % c;
        b = (int) ((long)b*b) % c;
        b = (int) ((long)b*b) % c;
        return b;
    }
}
