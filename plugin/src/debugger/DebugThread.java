package com.siberika.idea.pascal.debugger;

import com.siberika.idea.pascal.debugger.gdb.GdbStackFrame;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiResults;

import java.util.ArrayList;
import java.util.List;

public class DebugThread {
    public enum State {STOPPED, RUNNING;}

    private final Integer id;
    private final String targetId;
    private final String details;
    private final String name;
    private final GdbStackFrame frame;
    private final State state;

    public DebugThread(PascalXDebugProcess process, GdbMiResults thread) {
        this.id = thread.getInteger("id");
        this.targetId = thread.getString("target-id");
        this.details = thread.getString("details");
        this.name = thread.getString("name");
        this.frame = new GdbStackFrame(process, thread.getTuple("frame"), id);
        this.state = State.valueOf(thread.getString("state").toUpperCase());
    }

    public Integer getId() {
        return id;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getDetails() {
        return details;
    }

    public String getName() {
        return name;
    }

    public GdbStackFrame getFrame() {
        return frame;
    }

    public State getState() {
        return state;
    }

    public static List<DebugThread> parseThreads(PascalXDebugProcess process, GdbMiLine res) {
        if ("done".equals(res.getRecClass())) {
            final List<Object> threads = res.getResults().getList("threads");
            List<DebugThread> result = new ArrayList<>(threads.size());
            for (Object t : threads) {
                if (t instanceof GdbMiResults) {
                    result.add(new DebugThread(process, (GdbMiResults) t));
                }
            }
            return result;
        } else {
            return null;
        }
    }

}
