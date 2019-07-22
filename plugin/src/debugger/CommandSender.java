package com.siberika.idea.pascal.debugger;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class CommandSender extends Thread {
    private static final Logger LOG = Logger.getInstance(CommandSender.class);

    private final PascalXDebugProcess process;

    private final BlockingQueue<Command> queue = new ArrayBlockingQueue<>(200);
    private final Map<Long, FinishCallback> callbackMap = new ConcurrentHashMap<>();

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

    void send(String command, Long token, FinishCallback callback) {
        assert ((callback != null) && (token != null)) || (null == callback) : "Token should be specified if callback is specified";
        if (!process.getSession().isStopped()) {
            queue.add(new Command(command, token, callback));
        }
    }

    public FinishCallback findCallback(Long token) {
        return token != null ? callbackMap.get(token) : null;
    }

    private void printToConsole(String text, ConsoleViewContentType contentType) {
        if (process.console != null) {
            process.console.print(text, contentType);
        }
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
        void call(boolean success);
    }
}
