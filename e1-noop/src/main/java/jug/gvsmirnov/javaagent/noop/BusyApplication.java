package jug.gvsmirnov.javaagent.noop;

import jug.gvsmirnov.toolbox.BadThings;
import jug.gvsmirnov.toolbox.BottomlessClassLoader;

import java.util.ArrayList;
import java.util.Collection;

public class BusyApplication {

    public static volatile Collection<Object> classInstanceSink = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        System.out.println("BusyApplication is starting");

        BadThings.expandHeap();
        long nextPrintTime = System.currentTimeMillis();

        while (true) {
            try {
                final Class<?> clazz = BottomlessClassLoader.loadHugeClass();
                classInstanceSink.add(clazz.newInstance());

                long currentTime = System.currentTimeMillis();
                if (currentTime >= nextPrintTime) {
                    nextPrintTime = currentTime + 5_000;

                    // TODO: calling clazz.getSimpleName results in an IncompatibeClassChangeError
                    System.out.println("Loaded " + classInstanceSink.size() + " classes, latest:" + clazz.getName());
                    Thread.sleep(1);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

}
