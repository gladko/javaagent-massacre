package vk.study.simple;

import javassist.*;

public class Test {
	public static void main(String[] args) throws Exception {
		ClassPool cp = ClassPool.getDefault();

//		CtClass ctClass = cp.get("com.devexperts.io.BufferedInput");
		CtClass ctClass = cp.get("com.devexperts.io.ByteArrayInput");
		for (CtMethod method : ctClass.getMethods()) {
			System.out.println(method.getName() + " " + method.getSignature());
		}


		CtMethod readUtf = ctClass.getMethod("readUTFString", "()Ljava/lang/String;");
		readUtf.insertAfter("System.out.println(\"test\");");

//		CtClass cc = cp.get("Hello");
//		CtMethod m = cc.getDeclaredMethod("say");
//		m.insertBefore("{ System.out.println(\"Hello.say():\"); }");
//		Class c = cc.toClass();
//		Hello h = (Hello)c.newInstance();
//		h.say();
	}
}

