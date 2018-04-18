package com.skillclient.obfuscation;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Textifier;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Main {
    static ZipFile in;
    static ZipOutputStream out;
    static Random RANDOM = new Random();

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
                annotations(classNode);
                optimizeCheck(classNode);
                flow(classNode);

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

    private static void flow(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode insnNode : method.instructions.toArray()) {
                // NOP
                if (RANDOM.nextInt(144) == 0)
                    method.instructions.insertBefore(insnNode, new InsnNode(Opcodes.NOP));
                // final int n = a.hashCode() & (int)if.assert[747];
                if (insnNode.getOpcode() == Opcodes.ARETURN) {
                    LabelNode label = new LabelNode();
                    InsnList list = new InsnList();
                    list.add(new InsnNode(Opcodes.DUP));
                    list.add(new JumpInsnNode(Opcodes.IFNULL, label));
                    list.add(new InsnNode(Opcodes.DUP));
                    list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false));
                    int f = RANDOM.nextInt(objects.size());
                    while (!(objects.get(f) instanceof Integer))
                        f = RANDOM.nextInt(objects.size());
                    list.add(new FieldInsnNode(Opcodes.GETSTATIC, "skill/if", "assert", "[Ljava/lang/Object;"));
                    list.add(new IntInsnNode(Opcodes.SIPUSH, f + 2));
                    list.add(new InsnNode(Opcodes.AALOAD));
                    list.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Integer"));
                    list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
                    list.add(new InsnNode(Opcodes.IAND));
                    list.add(new InsnNode(Opcodes.POP));
                    list.add(label);
                    method.instructions.insertBefore(insnNode, list);
                }
                if (insnNode instanceof LabelNode) {
                    InsnList list = new InsnList();

                    if (RANDOM.nextBoolean()) {
                        // if.assert[959] == null
                        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "skill/if", "assert", "[Ljava/lang/Object;"));
                        list.add(new IntInsnNode(Opcodes.SIPUSH, RANDOM.nextInt(objects.size())));
                        list.add(new InsnNode(Opcodes.AALOAD));
                        list.add(new JumpInsnNode(Opcodes.IFNULL, (LabelNode) insnNode));
                    } else {
                        // (int)if.assert[41] > 31136
                        int f = RANDOM.nextInt(objects.size());
                        while (!(objects.get(f) instanceof Integer))
                            f = RANDOM.nextInt(objects.size());
                        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "skill/if", "assert", "[Ljava/lang/Object;"));
                        list.add(new IntInsnNode(Opcodes.SIPUSH, f + 2));
                        list.add(new InsnNode(Opcodes.AALOAD));
                        list.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Integer"));
                        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
                        int s = RANDOM.nextInt(Short.MAX_VALUE);
                        list.add(new IntInsnNode(Opcodes.SIPUSH, s));
                        list.add(new JumpInsnNode((int) objects.get(f) > s ? Opcodes.IF_ICMPLT : Opcodes.IF_ICMPGT, (LabelNode) insnNode));
                    }

                    // while () {} OR  if (!) {}
                    if (RANDOM.nextBoolean())
                        method.instructions.insert(insnNode, list);
                    else
                        method.instructions.insertBefore(insnNode, list);
                }
            }
        }
    }

    static List<Object> objects = new ArrayList<>();

    private static void replaceConstants(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode insnNode : method.instructions.toArray()) {
                {
                    LdcInsnNode n = null;
                    if (insnNode.getOpcode() == Opcodes.ICONST_M1)
                        n = new LdcInsnNode(-1);
                    else if (insnNode.getOpcode() == Opcodes.ICONST_0)
                        n = new LdcInsnNode(0);
                    else if (insnNode.getOpcode() == Opcodes.ICONST_1)
                        n = new LdcInsnNode(1);
                    else if (insnNode.getOpcode() == Opcodes.ICONST_2)
                        n = new LdcInsnNode(2);
                    else if (insnNode.getOpcode() == Opcodes.ICONST_3)
                        n = new LdcInsnNode(3);
                    else if (insnNode.getOpcode() == Opcodes.ICONST_4)
                        n = new LdcInsnNode(4);
                    else if (insnNode.getOpcode() == Opcodes.ICONST_5)
                        n = new LdcInsnNode(5);
                    else if (insnNode.getOpcode() == Opcodes.SIPUSH || insnNode.getOpcode() == Opcodes.BIPUSH)
                        n = new LdcInsnNode(((IntInsnNode) insnNode).operand);
                    else if (insnNode.getOpcode() == Opcodes.FCONST_0)
                        n = new LdcInsnNode(0.0f);
                    else if (insnNode.getOpcode() == Opcodes.FCONST_1)
                        n = new LdcInsnNode(1.0f);
                    else if (insnNode.getOpcode() == Opcodes.FCONST_2)
                        n = new LdcInsnNode(2.0f);
                    else if (insnNode.getOpcode() == Opcodes.DCONST_0)
                        n = new LdcInsnNode(0D);
                    else if (insnNode.getOpcode() == Opcodes.DCONST_1)
                        n = new LdcInsnNode(1D);
                    else if (insnNode.getOpcode() == Opcodes.LCONST_0)
                        n = new LdcInsnNode(0L);
                    else if (insnNode.getOpcode() == Opcodes.LCONST_1)
                        n = new LdcInsnNode(1L);
                    if (n != null) {
                        method.instructions.set(insnNode, n);
                        insnNode = n;
                    }
                }

                if (insnNode.getOpcode() == Opcodes.LDC) {
                    LdcInsnNode ldc = (LdcInsnNode) insnNode;
                    if (ldc.cst instanceof String || ldc.cst instanceof Integer || ldc.cst instanceof Float || (ldc.cst instanceof Long && ((Long) ldc.cst).intValue() == (Long) ldc.cst) || ldc.cst instanceof Double || ldc.cst instanceof Type) {
                        int i = getNext(ldc.cst);
                        InsnList list = new InsnList();
                        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "skill/if", "assert", "[Ljava/lang/Object;"));
                        list.add(new IntInsnNode(Opcodes.SIPUSH, (short) (i + 2)));
                        list.add(new InsnNode(Opcodes.AALOAD));
                        if (ldc.cst instanceof Long)
                            list.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Integer"));
                        else if (ldc.cst instanceof Type)
                            list.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Class"));
                        else
                            list.add(new TypeInsnNode(Opcodes.CHECKCAST, ldc.cst.getClass().getName().replace('.', '/')));

                        if (ldc.cst instanceof Integer)
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
                        else if (ldc.cst instanceof Long)
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "longValue", "()J", false));
                        else if (ldc.cst instanceof Float)
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false));
                        else if (ldc.cst instanceof Double)
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false));
                        method.instructions.insertBefore(insnNode, list);
                        method.instructions.remove(insnNode);
                    } else {
                        System.out.println(classNode.name + " " + method.name + method.desc + " " + ldc.cst.getClass() + " " + ldc.cst);
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
     * There's no optimization, as Proguard should do this. Still added a Check in case something goes wrong.
     *
     * @param classNode
     */
    private static void optimizeCheck(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode node : method.instructions.toArray()) {
                int i = node.getOpcode();
                if (node instanceof LineNumberNode)
                    System.out.println("LineNumberNode " + ((LineNumberNode) node).line + " at " + classNode.name + "." + method.name + method.desc);
                else if (i == Opcodes.NOP)
                    System.out.println("NOP at " + classNode.name + "." + method.name + method.desc);
                if ((i >= 26 && i <= 45) || (i >= 59 && i <= 78))
                    System.out.println("*LOAD_N / *STORE_N : " + i + " at " + classNode.name + "." + method.name + method.desc);
            }
            AbstractInsnNode[] aina = method.instructions.toArray();
            for (int i = aina.length - 1; i > 0; i--) {
                AbstractInsnNode a = aina[i - 1];
                AbstractInsnNode b = aina[i];
                if (a.getOpcode() == b.getOpcode()) {
                    if (a instanceof VarInsnNode && ((VarInsnNode) a).var == ((VarInsnNode) b).var)
                        System.out.println(Textifier.OPCODES[a.getOpcode()] + " " + ((VarInsnNode) a).var + " " + ((VarInsnNode) b).var + " at " + classNode.name + "." + method.name + method.desc);
                    else if (a instanceof FieldInsnNode && ((FieldInsnNode) a).owner.equals(((FieldInsnNode) b).owner) && ((FieldInsnNode) a).name.equals(((FieldInsnNode) b).name) && ((FieldInsnNode) a).desc.equals(((FieldInsnNode) b).desc))
                        System.out.println(Textifier.OPCODES[a.getOpcode()] + " " + ((FieldInsnNode) a).owner + " " + ((FieldInsnNode) a).name + ((FieldInsnNode) a).desc + " at " + classNode.name + "." + method.name + method.desc);
                    else if (a.getOpcode() == Opcodes.POP)
                        System.out.println("POP POP != POP2 at " + classNode.name + "." + method.name + method.desc);
                }
            }
            for (int i = aina.length - 1; i > 1; i--) {
                if (aina[i].getOpcode() == Opcodes.DUP && aina[i - 1].getOpcode() == Opcodes.DUP && aina[i - 2].getOpcode() == Opcodes.DUP)
                    System.out.println("DUP DUP DUP != DUP DUP2 at " + classNode.name + "." + method.name + method.desc);
            }
        }
    }

    /**
     * add the Annotation @SideOnly(Side.CLIENT) to all classes
     *
     * @param classNode
     */
    private static void annotations(ClassNode classNode) {
        if (classNode.visibleAnnotations == null) {
            classNode.visibleAnnotations = new ArrayList<>();
            AnnotationNode node = new AnnotationNode("Lnet/minecraftforge/fml/relauncher/SideOnly;");
            node.values = new ArrayList<>();
            node.values.add("value");
            node.values.add(new String[]{"Lnet/minecraftforge/fml/relauncher/Side;", "CLIENT"});
            classNode.visibleAnnotations.add(node);
        } else {
            boolean hasSideOnly = false;
            for (AnnotationNode node : classNode.visibleAnnotations) {
                if (node.desc.equals("Lnet/minecraftforge/fml/relauncher/SideOnly;"))
                    hasSideOnly = true;
            }
            if (!hasSideOnly) {
                AnnotationNode node = new AnnotationNode("Lnet/minecraftforge/fml/relauncher/SideOnly;");
                node.values = new ArrayList<>();
                node.values.add("value");
                node.values.add(new String[]{"Lnet/minecraftforge/fml/relauncher/Side;", "CLIENT"});
                classNode.visibleAnnotations.add(node);
            }
        }
        check(classNode, classNode.visibleAnnotations);
        check(classNode, classNode.invisibleAnnotations);
        check(classNode, classNode.visibleTypeAnnotations);
        check(classNode, classNode.invisibleTypeAnnotations);

        for (MethodNode method : classNode.methods) {
            check(classNode, method.visibleAnnotations);
            check(classNode, method.invisibleAnnotations);
            check(classNode, method.visibleTypeAnnotations);
            check(classNode, method.invisibleTypeAnnotations);
            check(classNode, method.visibleLocalVariableAnnotations);
            check(classNode, method.invisibleLocalVariableAnnotations);
            if (method.visibleParameterAnnotations != null)
                for (List<AnnotationNode> la : method.visibleParameterAnnotations)
                    check(classNode, la);
            if (method.invisibleParameterAnnotations != null)
                for (List<AnnotationNode> la : method.invisibleParameterAnnotations)
                    check(classNode, la);
            for (AbstractInsnNode ins : method.instructions.toArray()) {
                check(classNode, ins.visibleTypeAnnotations);
                check(classNode, ins.invisibleTypeAnnotations);
            }
        }
        for (FieldNode field : classNode.fields) {
            check(classNode, field.visibleAnnotations);
            check(classNode, field.invisibleAnnotations);
            check(classNode, field.visibleTypeAnnotations);
            check(classNode, field.invisibleTypeAnnotations);
        }
    }

    static void check(ClassNode classNode, List<? extends AnnotationNode> list) {
        if (list != null) {
            List<AnnotationNode> remove = new ArrayList<>();
            for (AnnotationNode annotation : list) {
                if (annotation.desc.equals("Lnet/minecraftforge/fml/relauncher/SideOnly;") || annotation.desc.startsWith("Lnet/minecraftforge/fml/relauncher/IFMLLoadingPlugin") || annotation.desc.startsWith("Lskill/") || annotation.desc.equals("Lnet/minecraftforge/fml/common/eventhandler/SubscribeEvent;") || annotation.desc.equals("Lcom/google/common/eventbus/Subscribe;") || annotation.desc.equals("Ljava/lang/annotation/Retention;") || annotation.desc.equals("Ljava/lang/annotation/Target;") || annotation.desc.equals("Ljava/lang/annotation/Documented;")) {
                } else if (annotation.desc.equals("Ljava/lang/FunctionalInterface;") || annotation.desc.equals("Ljavax/annotation/Nullable;")) {
                    System.out.println("removed: " + annotation.desc + " from " + classNode.name);
                    remove.add(annotation);
                } else {
                    System.out.println("found: " + annotation.desc + " from " + classNode.name);
                }
            }
            list.removeAll(remove);
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
