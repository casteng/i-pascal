package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StubInputStream;
import com.intellij.util.io.StringRef;

import java.io.IOException;

public class StubUtil {
    static <T extends Enum<T>> T readEnum(StubInputStream dataStream, Class<T> clazz) throws IOException {
        String name = readName(dataStream);
        return name != null ? T.valueOf(clazz, name) : null;
    }

    static String readName(StubInputStream dataStream) throws IOException {
        StringRef ref = dataStream.readName();
        return ref != null ? ref.getString() : null;
    }
}
