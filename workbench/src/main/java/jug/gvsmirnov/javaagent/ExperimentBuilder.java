package jug.gvsmirnov.javaagent;


import jug.gvsmirnov.javaagent.measurement.ResidentSetSize;
import jug.gvsmirnov.javaagent.measurement.SampleNativeMemoryTracking;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExperimentBuilder {

    private final String name;
    private final File outputRoot;

    private String agentJarPath;
    private String applicationJarPath;

    private boolean trackResidentSetSize = false;
    private boolean trackNativeMemory    = false;
    private boolean traceClassLoading    = false;

    private boolean logStdOut = false;
    private boolean logStdErr = false;

    private int iterations = 15;

    private Collection<String> extraArgs = new LinkedHashSet<>();

    public ExperimentBuilder(String name) {
        Objects.requireNonNull(name, "Experiment name must not be null");

        this.name = name;
        // TODO: rootDir = System.getProperty("root.dir", "build/experiment");
        this.outputRoot = new File(new File("build", name), now());
    }

    public ExperimentBuilder applicationJar(String path) {
        this.applicationJarPath = path;
        return this;
    }

    public ExperimentBuilder withAgent(String jarPath) {
        this.agentJarPath = jarPath;
        return this;
    }

    public ExperimentBuilder withoutAgent() {
        this.agentJarPath = null;
        return this;
    }

    public ExperimentBuilder trackResidentSetSize(boolean trackResidentSetSize) {
        this.trackResidentSetSize = trackResidentSetSize;
        return this;
    }

    public ExperimentBuilder trackNativeMemory(boolean trackNativeMemory) {
        this.trackNativeMemory = trackNativeMemory;
        return this;
    }

    public ExperimentBuilder logStdOut() {
        this.logStdOut = true;
        return this;
    }

    public ExperimentBuilder logStdErr() {
        this.logStdErr = true;
        return this;
    }

    public ExperimentBuilder traceClassLoading(boolean traceClassLoading) {
        this.traceClassLoading = traceClassLoading;
        return this;
    }

    public ExperimentBuilder iterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

    public ExperimentBuilder withJvmArgument(String arg) {
        this.extraArgs.add(arg);
        return this;
    }

    public Experiment build() {
        validate();
        return new Experiment(outputRoot, buildCommand(), buildMeasurements(), logStdOut, logStdErr, iterations);
    }

    private List<String> buildCommand() {
        // LHS to remove duplicates but preserve order
        final Set<String> command = new LinkedHashSet<>();

//        command.add("/Users/koz963/java/zulu17.36.17-ca-jdk17.0.4.1/zulu-17.jdk/Contents/Home/bin/java");
        command.add("java");
        command.add("-jar");

        // TODO: parametrize
        command.add("-Xms64m");
        command.add("-Xmx64m");

        if (agentJarPath != null) {
            command.add("-javaagent:" + agentJarPath);
        }

        if (trackNativeMemory) {
            command.add("-XX:NativeMemoryTracking=summary");
        }

        if (traceClassLoading) {
            // TODO: VM log file
            command.add("-XX:+UnlockDiagnosticVMOptions");
            command.add("-XX:+TraceClassLoading");
        }

        command.addAll(extraArgs);

        command.add(applicationJarPath);

        return new ArrayList<>(command);
    }

    private Collection<MeasurementFactory> buildMeasurements() {
        final List<MeasurementFactory> result = new ArrayList<>();

        if (trackResidentSetSize) {
            result.add(ResidentSetSize::new);
        }

        if (trackNativeMemory) {
            result.add(javaPid -> new SampleNativeMemoryTracking(outputRoot, javaPid));
        }

        return result;
    }

    private void validate() {
        Objects.requireNonNull(applicationJarPath, "Must specify a jar file");

        if (!new File(applicationJarPath).exists()) {
            throw new IllegalArgumentException("Application jar file does not exist at " + applicationJarPath);
        }

        if (agentJarPath != null && !new File(agentJarPath).exists()) {
            throw new IllegalArgumentException("Agent jar file does not exist at " + applicationJarPath);
        }
    }

    private static String now() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(LocalDateTime.now());
    }
}
