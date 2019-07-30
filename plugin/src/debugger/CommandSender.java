package com.siberika.idea.pascal.debugger;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.diagnostic.Logger;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CommandSender extends Thread {
    private static final Logger LOG = Logger.getInstance(CommandSender.class);
    public static final long TOKEN_UNREACHABLE = 1000000000000000000L;

    private final PascalXDebugProcess process;

    private final BlockingQueue<Command> queue = new ArrayBlockingQueue<>(200);
    private final Map<Long, FinishCallback> callbackMap = new ConcurrentHashMap<>();

    private static final AtomicLong TOKEN_COUNTER = new AtomicLong();

    CommandSender(PascalXDebugProcess pascalXDebugProcess) {
        process = pascalXDebugProcess;
    }

    @Override
    public void run() {
        try {
            while (true) {
                final Command command = queue.take();
                doSend(command);
            }
        } catch (InterruptedException e) {
            LOG.info("Debugger command sender thread has been interrupted");
        }
    }

    private void doSend(Command command) {
        try {
            OutputStream commandStream = process.getProcessHandler().getProcessInput();
            if (commandStream != null) {
                if (command.callback != null) {
                    callbackMap.put(command.token, command.callback);
                }
                commandStream.write(command.toString().getBytes(StandardCharsets.UTF_8));
                commandStream.flush();
                printToConsole(">> " + command, ConsoleViewContentType.LOG_INFO_OUTPUT);
            }
        } catch (IOException e) {
            LOG.warn("ERROR: sending command to GDB", e);
        }
    }

    void send(String command, FinishCallback callback) {
        Long token = callback != null ? nextToken() : null;
        if (!process.getSession().isStopped()) {
            queue.add(new Command(command, token, callback));
        }
    }

    private static long nextToken() {
        return TOKEN_COUNTER.getAndIncrement();
    }

    public FinishCallback findCallback(Long token) {
        return token != null ? callbackMap.get(token) : null;
    }

    private void printToConsole(String text, ConsoleViewContentType contentType) {
        if (process.console != null) {
            process.console.print(text, contentType);
        }
    }

    void syncCalls(int levels, CommandSender.FinishCallback callback) {
        AtomicInteger counter = new AtomicInteger(levels);
        final FinishCallback syncCallback = new FinishCallback() {
            @Override
            public void call(GdbMiLine res) {
                final int current = counter.decrementAndGet();
                if (current > 0) {
                    send(String.format("-gdb-set _sc_=%d", current), this);
                } else {
                    callback.call(res);
                }
            }
        };
        send(String.format("-gdb-set _sc_=%d", levels), syncCallback);
    }

    private static class Command {
        private final String command;
        private final Long token;
        private final FinishCallback callback;

        private Command(String command, Long token, FinishCallback callback) {
            this.command = command;
            this.token = token;
            this.callback = callback;
        }

        @Override
        public String toString() {
            return (token != null ? token.toString() : "") + command + "\n";
        }
    }

    public interface FinishCallback {
        void call(GdbMiLine res);
    }
}
