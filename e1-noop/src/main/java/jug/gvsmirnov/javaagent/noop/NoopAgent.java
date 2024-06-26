package jug.gvsmirnov.javaagent.noop;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class NoopAgent implements ClassFileTransformer{

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new NoopAgent(), true);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        return classfileBuffer;
    }

}
