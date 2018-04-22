package com.skillclient.obfuscation;

import org.objectweb.asm.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

public class DeobfuscaterDump implements Opcodes {
    static final Base64.Encoder enc = Base64.getEncoder();

    public static byte[] dump() throws Exception {
        ClassWriter cw = new ClassWriter(0); // ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES
        MethodVisitor mv;

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, "skill/if", null, "java/lang/Object", null);

        cw.visitInnerClass("java/util/Base64$Decoder", "java/util/Base64", "Decoder", ACC_PUBLIC + ACC_STATIC);

        {
            cw.visitField(ACC_PUBLIC + ACC_STATIC, "assert", "[Ljava/lang/Object;", null, null).visitEnd();
            cw.visitField(ACC_PUBLIC + ACC_STATIC, "decoder", "Ljava/util/Base64$Decoder;", null, null).visitEnd();
            cw.visitField(ACC_PUBLIC + ACC_STATIC, "cipher", "Ljavax/crypto/Cipher;", null, null).visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "Lskill/if;", null, l0, l1, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            Label label_start = new Label();
            Label label_end = new Label();
            mv.visitLabel(label_start);

            mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();

            // assert = new String[Main.objects.size() + 2];
            mv.visitIntInsn(SIPUSH, Main.objects.size() + 2);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitInsn(DUP);
            mv.visitFieldInsn(PUTSTATIC, "skill/if", "assert", "[Ljava/lang/Object;");

            // decoder = Base64.getDecoder();
            mv.visitMethodInsn(INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;", false);
            mv.visitFieldInsn(PUTSTATIC, "skill/if", "decoder", "Ljava/util/Base64$Decoder;");

            // assert[0] = "AES/ECB/PKCS5Padding";
            mv.visitInsn(DUP);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitTypeInsn(NEW, "java/lang/String");
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETSTATIC, "skill/if", "decoder", "Ljava/util/Base64$Decoder;");
            mv.visitLdcInsn(enc.encodeToString("AES/ECB/PKCS5Padding".getBytes()));
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "(Ljava/lang/String;)[B", false);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V", false);
            mv.visitInsn(AASTORE);
            // assert[1] = "AES";
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitTypeInsn(NEW, "java/lang/String");
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETSTATIC, "skill/if", "decoder", "Ljava/util/Base64$Decoder;");
            mv.visitLdcInsn(enc.encodeToString("AES".getBytes()));
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "(Ljava/lang/String;)[B", false);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V", false);
            mv.visitInsn(AASTORE);

            // cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            {
                //mv.visitLdcInsn("AES/ECB/PKCS5Padding");
                mv.visitFieldInsn(GETSTATIC, "skill/if", "assert", "[Ljava/lang/Object;");
                mv.visitInsn(ICONST_0);
                mv.visitInsn(AALOAD);
                mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            }
            mv.visitMethodInsn(INVOKESTATIC, "javax/crypto/Cipher", "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Cipher;", false);
            mv.visitInsn(DUP);
            mv.visitFieldInsn(PUTSTATIC, "skill/if", "cipher", "Ljavax/crypto/Cipher;");

            // cipher.init(Cipher.DECRYPT_MODE, <SecretKeySpec>)
            mv.visitInsn(ICONST_2);

            // new SecretKeySpec(key[16], "AES")
            mv.visitTypeInsn(NEW, "javax/crypto/spec/SecretKeySpec");
            mv.visitInsn(DUP);
            byte[] key = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(key);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));

            mv.visitIntInsn(BIPUSH, 16);
            mv.visitIntInsn(NEWARRAY, T_BYTE);
            for (int i = 0; i < 16; i++) {
                mv.visitInsn(DUP);
                mv.visitIntInsn(BIPUSH, i);
                mv.visitIntInsn(BIPUSH, key[i]);
                mv.visitInsn(BASTORE);
            }

            {
                //mv.visitLdcInsn("AES");
                mv.visitFieldInsn(GETSTATIC, "skill/if", "assert", "[Ljava/lang/Object;");
                mv.visitInsn(ICONST_1);
                mv.visitInsn(AALOAD);
                mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            }

            mv.visitMethodInsn(INVOKESPECIAL, "javax/crypto/spec/SecretKeySpec", "<init>", "([BLjava/lang/String;)V", false);

            mv.visitMethodInsn(INVOKEVIRTUAL, "javax/crypto/Cipher", "init", "(ILjava/security/Key;)V", false);

            mv.visitFieldInsn(GETSTATIC, "skill/if", "assert", "[Ljava/lang/Object;");
            for (int i = 0; i < Main.objects.size(); i++) {
                mv.visitInsn(DUP);
                mv.visitIntInsn(SIPUSH, (short) (i + 2));
                Object o = Main.objects.get(i);
                if (o instanceof String) {
                    mv.visitLdcInsn(enc.encodeToString(cipher.doFinal(((String) o).getBytes())));
                    mv.visitMethodInsn(INVOKESTATIC, "skill/if", "a", "(Ljava/lang/String;)Ljava/lang/String;", false);
                } else if (o instanceof Integer || o instanceof Long) {
                    short a = (short) (random.nextInt(256) + 256);
                    int b = random.nextInt(Integer.MAX_VALUE / 2) + Integer.MAX_VALUE / 4;
                    long c = random.nextInt(Integer.MAX_VALUE / 2) + Integer.MAX_VALUE / 4;

                    mv.visitIntInsn(SIPUSH, a);
                    mv.visitLdcInsn(b);
                    mv.visitLdcInsn(c);
                    mv.visitLdcInsn(calcI(a, b, c, o instanceof Integer ? (int) o : Long.valueOf((long) o).intValue()));
                    mv.visitMethodInsn(INVOKESTATIC, "skill/if", "a", "(IIJI)I", false);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                } else if (o instanceof Float) {
                    short a = (short) (random.nextInt(256) + 256);
                    int b = random.nextInt(Integer.MAX_VALUE / 2) + Integer.MAX_VALUE / 4;
                    long c = random.nextInt(Integer.MAX_VALUE / 2) + Integer.MAX_VALUE / 4;
                    mv.visitIntInsn(SIPUSH, a);
                    mv.visitLdcInsn(b);
                    mv.visitLdcInsn(c);
                    mv.visitLdcInsn(calcF(a, b, c, (float) o));
                    mv.visitMethodInsn(INVOKESTATIC, "skill/if", "a", "(IIJI)F", false);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
                } else if (o instanceof Double) {
                    mv.visitLdcInsn(new String(ByteBuffer.allocate(Double.BYTES).putDouble((Double) o).array()));
                    mv.visitMethodInsn(INVOKESTATIC, "skill/if", "a", "(Ljava/lang/String;)D", false);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
                } else if (o instanceof Type) {
                    mv.visitLdcInsn(o);
                } else {
                    mv.visitLdcInsn(o);
                    System.out.println(o.getClass() + " " + o);
                }
                mv.visitInsn(AASTORE);
            }

            mv.visitLabel(label_end);
            mv.visitInsn(RETURN);
            mv.visitMaxs(9, 0);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "a", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();

            mv.visitLabel(l0);
            mv.visitTypeInsn(NEW, "java/lang/String");
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETSTATIC, "skill/if", "cipher", "Ljavax/crypto/Cipher;");
            mv.visitFieldInsn(GETSTATIC, "skill/if", "decoder", "Ljava/util/Base64$Decoder;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "(Ljava/lang/String;)[B", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "javax/crypto/Cipher", "doFinal", "([B)[B", false);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V", false);
            mv.visitInsn(ARETURN);
            mv.visitLabel(l1);
            mv.visitLocalVariable("s", "Ljava/lang/String;", null, l0, l1, 0);
            mv.visitMaxs(5, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "a", "(IIJI)I", null, null);
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitVarInsn(ILOAD, 0);
            mv.visitInsn(DUP);
            mv.visitInsn(IMUL);
            mv.visitInsn(IMUL);
            mv.visitVarInsn(ILOAD, 0);
            mv.visitInsn(ISUB);
            mv.visitInsn(ICONST_2);
            mv.visitInsn(IUSHR);
            mv.visitInsn(I2L);
            mv.visitInsn(DUP2);
            mv.visitInsn(LMUL);
            mv.visitVarInsn(LLOAD, 2);
            mv.visitInsn(LREM);
            mv.visitInsn(DUP2);
            mv.visitInsn(LMUL);
            mv.visitVarInsn(LLOAD, 2);
            mv.visitInsn(LREM);
            mv.visitInsn(DUP2);
            mv.visitInsn(LMUL);
            mv.visitVarInsn(LLOAD, 2);
            mv.visitInsn(LREM);
            mv.visitInsn(L2I);
            mv.visitVarInsn(ILOAD, 4);
            mv.visitInsn(IXOR);
            mv.visitInsn(IRETURN);
            mv.visitLabel(l1);
            mv.visitLocalVariable("a", "I", null, l0, l1, 0);
            mv.visitLocalVariable("b", "I", null, l0, l1, 1);
            mv.visitLocalVariable("c", "J", null, l0, l1, 2);
            mv.visitLocalVariable("d", "I", null, l0, l1, 4);
            mv.visitMaxs(4, 5);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "a", "(IIJI)F", null, null);
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ILOAD, 0);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitVarInsn(LLOAD, 2);
            mv.visitVarInsn(ILOAD, 4);
            mv.visitMethodInsn(INVOKESTATIC, "skill/if", "a", "(IIJI)I", false);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "intBitsToFloat", "(I)F", false);
            mv.visitInsn(FRETURN);
            mv.visitLabel(l1);
            mv.visitLocalVariable("a", "I", null, l0, l1, 0);
            mv.visitLocalVariable("b", "I", null, l0, l1, 1);
            mv.visitLocalVariable("c", "J", null, l0, l1, 2);
            mv.visitLocalVariable("d", "I", null, l0, l1, 4);
            mv.visitMaxs(5, 5);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "a", "(Ljava/lang/String;)D", null, null);
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B", false);
            mv.visitMethodInsn(INVOKESTATIC, "java/nio/ByteBuffer", "wrap", "([B)Ljava/nio/ByteBuffer;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/nio/ByteBuffer", "getDouble", "()D", false);
            mv.visitInsn(DRETURN);
            mv.visitLabel(l1);
            mv.visitLocalVariable("a", "Ljava/lang/String;", null, l0, l1, 0);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }

        cw.visitEnd();

        return cw.toByteArray();
    }

    public static int calcI(final int a, int b, final long c, int i) {
        final long n = b * (a * a) - a >>> 2;
        final long n2 = (n * n) % c;
        final long n3 = (n2 * n2) % c;
        return (int) ((n3 * n3) % c) ^ i;
    }

    public static int calcF(int a, int b, long c, float d) {
        final long n = b * (a * a) - a >>> 2;
        final long n2 = n * n % c;
        final long n3 = n2 * n2 % c;
        return (int) (n3 * n3 % c) ^ Float.floatToIntBits(d);
    }
}
