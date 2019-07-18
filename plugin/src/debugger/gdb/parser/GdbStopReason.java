package com.siberika.idea.pascal.debugger.gdb.parser;

/**
 * Author: George Bakhtadze
 * Date: 01/04/2017
 */
public enum GdbStopReason {
    // A breakpoint was reached.
    BREAKPOINT_HIT("breakpoint-hit"),
    // A watchpoint was triggered.
    WATCHPOINT_TRIGGER("watchpoint-trigger"),
    // A read watchpoint was triggered.
    READ_WATCHPOINT_TRIGGER("read-watchpoint-trigger"),
    // An access watchpoint was triggered.
    ACCESS_WATCHPOINT_TRIGGER("access-watchpoint-trigger"),
    // An -exec-finish or similar CLI command was accomplished.
    FUNCTION_FINISHED("function-finished"),
    // An -exec-until or similar CLI command was accomplished.
    LOCATION_REACHED("location-reached"),
    // A watchpoint has gone out of scope.
    WATCHPOINT_SCOPE("watchpoint-scope"),
    // An -exec-next, -exec-next-instruction, -exec-step, -exec-step-instruction or similar CLI command was accomplished.
    END_STEPPING_RANGE("end-stepping-range"),
    // The inferior exited because of a signal.
    EXITED_SIGNALLED("exited-signalled"),
    // The inferior exited.
    EXITED("exited"),
    // The inferior exited normally.
    EXITED_NORMALLY("exited-normally"),
    // A signal was received by the inferior.
    SIGNAL_RECEIVED("signal-received"),
    // The inferior has stopped due to a library being loaded or unloaded. This can happen when stop-on-solib-events (see Files) is set or when a catch load or catch unload catchpoint is in use (see Set Catchpoints).
    SOLIB_EVENT("solib-event"),
    // The inferior has forked. This is reported when catch fork (see Set Catchpoints) has been used.
    FORK("fork"),
    // The inferior has vforked. This is reported in when catch vfork (see Set Catchpoints) has been used.
    VFORK("vfork"),
    // The inferior entered a system call. This is reported when catch syscall (see Set Catchpoints) has been used.
    SYSCALL_ENTRY("syscall-entry"),
    // The inferior returned from a system call. This is reported when catch syscall (see Set Catchpoints) has been used.
    SYSCALL_RETURN("syscall-return"),
    // The inferior called exec. This is reported when catch exec (see Set Catchpoints) has been used.
    EXEC("exec"),
    // Exception occured
    EXCEPTION("exception-received");

    private final String uid;

    GdbStopReason(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public static GdbStopReason fromUid(String uid) {
        for (GdbStopReason reason : values()) {
            if (reason.uid.equals(uid)) {
                return reason;
            }
        }
        return null;
    }
}
