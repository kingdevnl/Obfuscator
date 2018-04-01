package com.skillclient.obfuscation;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Textifier;

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

                optimizeCheck(classNode);
                replaceConstants(classNode);
                optimizeCheck(classNode);
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
                    else if (insnNode.getOpcode() == Opcodes.FCONST_0)
                        n = new LdcInsnNode(0.0f);
                    else if (insnNode.getOpcode() == Opcodes.FCONST_1)
                        n = new LdcInsnNode(1.0f);
                    else if (insnNode.getOpcode() == Opcodes.FCONST_2)
                        n = new LdcInsnNode(2.0f);
                    if(n != null) {
                        method.instructions.set(insnNode, n);
                        insnNode = n;
                    }
                }

                if (insnNode.getOpcode() == Opcodes.LDC) {
                    LdcInsnNode ldc = (LdcInsnNode) insnNode;
                    if (ldc.cst instanceof String || ldc.cst instanceof Integer || ldc.cst instanceof Float) {
                        int i = getNext(ldc.cst);
                        InsnList list = new InsnList();
                        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "skill/if", "assert", "[Ljava/lang/Object;"));
                        list.add(new IntInsnNode(Opcodes.SIPUSH, (short) (i + 2)));
                        list.add(new InsnNode(Opcodes.AALOAD));
                        list.add(new TypeInsnNode(Opcodes.CHECKCAST, ldc.cst.getClass().getName().replace('.', '/')));
                        if(ldc.cst instanceof Integer)
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
                        else if (ldc.cst instanceof Float)
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false));
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
     * There's no optimization, as Proguard should do this. Still added a Check in case something goes wrong.
     * @param classNode
     */
    private static void optimizeCheck(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            for(AbstractInsnNode node:method.instructions.toArray()) {
                int i = node.getOpcode();
                if (node instanceof LineNumberNode)
                    System.out.println("LineNumberNode " + ((LineNumberNode) node).line + " at " + classNode.name + "." + method.name + method.desc);
                else if (i == Opcodes.NOP)
                    System.out.println("NOP at " + classNode.name + "." + method.name + method.desc);
                if ((i >= 26 && i <= 45) || (i >= 59 && i <= 78))
                    System.out.println("*LOAD_N / *STORE_N : " + i + " at " + classNode.name + "." + method.name + method.desc);
            }
            AbstractInsnNode[] aina = method.instructions.toArray();
            for(int i = aina.length-1; i > 0; i--) {
                AbstractInsnNode a = aina[i-1];
                AbstractInsnNode b = aina[i];
                if(a.getOpcode() == b.getOpcode()) {
                    if(a instanceof VarInsnNode && ((VarInsnNode) a).var == ((VarInsnNode) b).var)
                        System.out.println(Textifier.OPCODES[a.getOpcode()] + " " + ((VarInsnNode) a).var + " " + ((VarInsnNode) b).var + " at " + classNode.name + "." +  method.name + method.desc);
                    else if(a instanceof FieldInsnNode && ((FieldInsnNode) a).owner.equals(((FieldInsnNode) b).owner) && ((FieldInsnNode) a).name.equals(((FieldInsnNode) b).name) && ((FieldInsnNode) a).desc.equals(((FieldInsnNode) b).desc))
                        System.out.println(Textifier.OPCODES[a.getOpcode()] + " " + ((FieldInsnNode) a).owner + " " + ((FieldInsnNode) a).name + ((FieldInsnNode) a).desc + " at " + classNode.name + "." +  method.name + method.desc);
                    else if(a.getOpcode() == Opcodes.POP)
                        System.out.println("POP POP != POP2 at " + classNode.name + "." +  method.name + method.desc);
                }
            }
            for(int i = aina.length-1; i > 1; i--) {
                if(aina[i].getOpcode() == Opcodes.DUP && aina[i-1].getOpcode() == Opcodes.DUP && aina[i-2].getOpcode() == Opcodes.DUP)
                    System.out.println("DUP DUP DUP != DUP DUP2 at " + classNode.name + "." +  method.name + method.desc);
            }
        }
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
