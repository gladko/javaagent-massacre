package jug.gvsmirnov.javaagent.os;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import static jug.gvsmirnov.toolbox.BadThings.wrapCheckedExceptions;

public class MacOsHacks extends Hacks {
    @Override
    public int getPid(Process process) {
        return (int) process.pid();
    }

    @Override
    public long getResidentSetSize(int pid) {
        return wrapCheckedExceptions(() -> {
            final ProcessResult processResult = new ProcessExecutor()
                    .command("ps", "-o", "rss", "-p", Integer.toString(pid))
                    .readOutput(true)
                    .execute();

            return Long.valueOf(processResult.getOutput().getLines().get(1).trim()) * 1024;
        });
    }


}
