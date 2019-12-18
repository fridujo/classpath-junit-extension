package com.github.fridujo.junit.extension.classpath;

import java.io.OutputStream;
import java.io.PrintStream;

public class SystemState {
    private final PrintStream out;
    private final PrintStream err;

    private SystemState() {
        this.out = System.out;
        this.err = System.err;
    }

    public static SystemState backup() {
        return new SystemState();
    }
    
    public void restore() {
        System.setOut(out);
        System.setErr(err);
    }
    
    public static void silence() {
        PrintStream noop = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        });
        System.setOut(noop);
        System.setErr(noop);
    }
    
    public static SystemState backupAndSilence() {
        SystemState backup = backup();
        silence();
        return backup;
    }
}
