package com.skillclient.obfuscation;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Main {
    static ZipFile in;
    static ZipOutputStream out;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("usage: in.jar out.jar libs/");
            System.exit(0);
        }
        ClassPathHacker.addFile(new File(args[0]));
        ClassPathHacker.addFile(new File(args[2]));
        in = new ZipFile(new File(args[0]), ZipFile.OPEN_READ);
        File f = new File(args[1]);
        f.delete();
        out = new ZipOutputStream(new FileOutputStream(f));

        Enumeration<? extends ZipEntry> entries = in.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String filename = entry.getName();
            byte[] data = getBytesFromInputStream(in.getInputStream(entry));

            if (filename.endsWith(".class")) {
                ClassNode classNode = new ClassNode();
                ClassReader classReader = new ClassReader(data);
                classReader.accept(classNode, 0);

                replaceConstants(classNode);
                addClientSide(classNode);

                try {
                    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                    classNode.accept(classWriter);
                    data = classWriter.toByteArray();
                } catch (Exception e) {
                    System.out.println(e.toString() + " " + filename);
                }
            }

            if (!filename.endsWith(".json")) {
                out.putNextEntry(new ZipEntry(filename));
                out.write(data);
                out.closeEntry();
            }
        }

        try {
            out.putNextEntry(new ZipEntry("skill/if.class"));
            out.write(DeobfuscaterDump.dump());
            out.closeEntry();
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.close();
        System.out.println("array size: " + objects.size());
    }

    static List<Object> objects = new ArrayList<>();

    private static void replaceConstants(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode insnNode : method.instructions.toArray()) {
                {
                    LdcInsnNode n = null;
                    if(insnNode.getOpcode() == Opcodes.ICONST_M1)
                        n = new LdcInsnNode(-1);
                    else if(insnNode.getOpcode() == Opcodes.ICONST_0)
                        n = new LdcInsnNode(0);
                    else if(insnNode.getOpcode() == Opcodes.ICONST_1)
                        n = new LdcInsnNode(1);
                    else if(insnNode.getOpcode() == Opcodes.ICONST_2)
                        n = new LdcInsnNode(2);
                    else if(insnNode.getOpcode() == Opcodes.ICONST_3)
                        n = new LdcInsnNode(3);
                    else if(insnNode.getOpcode() == Opcodes.ICONST_4)
                        n = new LdcInsnNode(4);
                    else if(insnNode.getOpcode() == Opcodes.ICONST_5)
                        n = new LdcInsnNode(5);
                    else if(insnNode.getOpcode() == Opcodes.SIPUSH || insnNode.getOpcode() == Opcodes.BIPUSH)
                        n = new LdcInsnNode(((IntInsnNode)insnNode).operand);
                    if(n != null) {
                        method.instructions.set(insnNode, n);
                        insnNode = n;
                    }
                }

                if (insnNode.getOpcode() == Opcodes.LDC) {
                    LdcInsnNode ldc = (LdcInsnNode) insnNode;
                    if (ldc.cst instanceof String || ldc.cst instanceof Integer) {
                        int i = getNext(ldc.cst);
                        InsnList list = new InsnList();
                        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "skill/if", "assert", "[Ljava/lang/Object;"));
                        list.add(new IntInsnNode(Opcodes.SIPUSH, (short) (i + 2)));
                        list.add(new InsnNode(Opcodes.AALOAD));
                        list.add(new TypeInsnNode(Opcodes.CHECKCAST, ldc.cst.getClass().getName().replace('.', '/')));
                        if(ldc.cst instanceof Integer)
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
                        method.instructions.insertBefore(insnNode, list);
                        method.instructions.remove(insnNode);
                    }
                }
            }
        }
    }

    public static int getNext(Object o) {
        int i = objects.indexOf(o);
        if (i == -1) {
            objects.add(o);
            i = objects.indexOf(o);
        }
        return i;
    }

    /**
     * add the Annotation @SideOnly(Side.CLIENT) to all classes
     * @param classNode
     */
    private static void addClientSide(ClassNode classNode) {
        if (classNode.visibleAnnotations == null) {
            classNode.visibleAnnotations = new ArrayList<>();
            AnnotationNode node = new AnnotationNode("Lnet/minecraftforge/fml/relauncher/SideOnly;");
            node.values = new ArrayList<>();
            node.values.add("value");
            node.values.add(new String[] {"Lnet/minecraftforge/fml/relauncher/Side;", "CLIENT"});
            classNode.visibleAnnotations.add(node);
        } else {
            boolean hasSideOnly = false;
            for (AnnotationNode node : classNode.visibleAnnotations) {
                if(node.desc.equals("Lnet/minecraftforge/fml/relauncher/SideOnly;"))
                    hasSideOnly = true;
            }
            if(!hasSideOnly) {
                AnnotationNode node = new AnnotationNode("Lnet/minecraftforge/fml/relauncher/SideOnly;");
                node.values = new ArrayList<>();
                node.values.add("value");
                node.values.add(new String[] {"Lnet/minecraftforge/fml/relauncher/Side;", "CLIENT"});
                classNode.visibleAnnotations.add(node);
            }
        }
    }

    public static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer))
            os.write(buffer, 0, len);

        return os.toByteArray();
    }
}
