package vk.study.person;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class PersonAgent {

	public static void premain(String agentArgs, Instrumentation inst){
		System.out.println("PersonAgent starts.");
		inst.addTransformer(new PersonTransformer());
	}


	public static class PersonTransformer implements ClassFileTransformer {
		public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
								ProtectionDomain protectionDomain, byte[] classfileBuffer) {
			if (className == null)
				return null;

			String targetClassName = className.replace('/', '.');
			if (targetClassName.equals("vk.study.person.Person")){
				try {
					ClassPool classPool = ClassPool.getDefault();
					// wondering if when we need it
					classPool.appendClassPath(new LoaderClassPath(loader));

					CtClass clazz = classPool.makeClass(new ByteArrayInputStream(classfileBuffer), false);

					CtField param = new CtField(classPool.get("java.lang.String"), "sex", clazz);
					param.setModifiers(Modifier.PRIVATE);
					clazz.addField(param, CtField.Initializer.constant("male"));

					clazz.addMethod(CtNewMethod.setter("setSex", param));
					clazz.addMethod(CtNewMethod.getter("getSex", param));

					CtMethod method = clazz.getDeclaredMethod("toString");
					method.setBody("return \"User{\" +\n" +
							"                \"name='\" + name + '\\',' +\n" +
							"                \"sex='\" + sex + '\\'' +\n" +
							"                '}';");
					return clazz.toBytecode();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}
}
