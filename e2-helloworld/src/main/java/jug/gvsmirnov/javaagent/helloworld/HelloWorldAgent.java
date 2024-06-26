package jug.gvsmirnov.javaagent.helloworld;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ASM9;

public class HelloWorldAgent implements ClassFileTransformer {
    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new HelloWorldAgent());
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer)
    {
        try {
            if (className == null || !className.equals("jug/gvsmirnov/javaagent/helloworld/HelloWorldApplication")) {
                return null;
            }

            System.out.println("\n\nTransforming class " + className);

            return transformBytes(classfileBuffer);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private byte[] transformBytes(byte[] originalClassBytes) {
        final ClassReader cr  = new ClassReader(originalClassBytes);
        final ClassWriter cw  = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        ClassVisitor transformer = createTransformer(cw);

        cr.accept(transformer, 0);
        return cw.toByteArray();
    }

    public ClassVisitor createTransformer(ClassWriter cw) {
        return new ClassVisitor(ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(
                    int access, String methodName, String desc, String signature, String[] exceptions)
            {
                if (!methodName.equals("main")) {
                    return super.visitMethod(access, methodName, desc, signature, exceptions);
                }

                System.out.println("Replacing method " + methodName);
                return generateMainMethod(cw);
            }
        };
    }


    private static MethodVisitor generateMainMethod(ClassWriter cw) {
        GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC + ACC_STATIC, Method.getMethod("void main (String[])"),
                null, null, cw);
        mg.getStatic(Type.getType(System.class), "out", Type.getType(PrintStream.class));
        mg.push("Hello, transformed world!");
        mg.invokeVirtual(Type.getType(PrintStream.class),
                Method.getMethod("void println (String)"));
        mg.returnValue();
        mg.endMethod();

        return mg;
    }
}
