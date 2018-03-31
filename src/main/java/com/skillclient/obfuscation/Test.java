package com.skillclient.obfuscation;

import java.util.Random;

public class Test {

    public static Random r = new Random();

    public static void main(String[] args) {
        int a = r.nextInt(256) + 256;
        int b = r.nextInt(Integer.MAX_VALUE/2) + Integer.MAX_VALUE/4;
        int c = r.nextInt(Integer.MAX_VALUE/2) + Integer.MAX_VALUE/4;
        System.out.println(a + " " + b + " " + c + " " + a(a, b, c, 1337));
    }

    public static int a(final int a, int b, final long c, int i) {
        final long n = b * (a * a) - a >>> 2;
        final long n2 = (n * n) % c;
        final long n3 = (n2 * n2) % c;
        return (int)((n3 * n3) % c) ^ i;
    }
}
