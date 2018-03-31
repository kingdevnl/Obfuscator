package com.skillclient.obfuscation;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.TypeInsnNode;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

public class DeobfuscaterDump implements Opcodes {

    static Base64.Encoder enc = Base64.getEncoder();
    static Random random = new Random();

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
                if(o instanceof String) {
                    mv.visitLdcInsn(enc.encodeToString(cipher.doFinal(((String)o).getBytes())));
                    mv.visitMethodInsn(INVOKESTATIC, "skill/if", "decomp", "(Ljava/lang/String;)Ljava/lang/String;", false);
                } else if(o instanceof Integer) {
                    int r = random.nextInt();
                    mv.visitLdcInsn(((int)o) ^ r);
                    mv.visitLdcInsn(r);
                    mv.visitInsn(IXOR);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                    //} else if(o instanceof Long) {
                //    long r = random.nextLong();
                //    mv.visitLdcInsn(((long)o) ^ r);
                //    mv.visitLdcInsn(r);
                //    mv.visitInsn(LXOR);
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
            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "decomp", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
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

        cw.visitEnd();

        return cw.toByteArray();
    }
}
