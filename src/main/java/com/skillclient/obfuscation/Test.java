package com.skillclient.obfuscation;

import java.util.Random;

public class Test {

    public static Random random = new Random();

    public static void main(String[] args) {
        short a = (short) (random.nextInt(256) + 256);
        int b = random.nextInt(Integer.MAX_VALUE / 2) + Integer.MAX_VALUE / 4;
        long c = random.nextInt(Integer.MAX_VALUE / 2) + Integer.MAX_VALUE / 4;
        System.out.println(a + ", " + b + ", " + c + ", " + calc(a, b, c, 13.37f));
        System.out.println(a(383, 1022006764, 873849579, 1501040079));
    }

    public static float a(final int a, final int b, final long c, final int d) {
        final long n = b * (a * a) - a >>> 2;
        final long n2 = n * n % c;
        final long n3 = n2 * n2 % c;
        return Float.intBitsToFloat((int) (n3 * n3 % c) ^ d);
    }

    public static int calc(int a, int b, long c, float d) {
        final long n = b * (a * a) - a >>> 2;
        final long n2 = n * n % c;
        final long n3 = n2 * n2 % c;
        return (int) (n3 * n3 % c) ^ Float.floatToIntBits(d);
    }
}
