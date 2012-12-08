package com.siberika.idea.pascal;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 12/5/12
 */
public class PascalFileTypeLoader extends FileTypeFactory {
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(PascalFileType.PASCAL_FILE_TYPE, PascalFileType.EXTENSION_FILE_NAME_MATCHERS);
    }
}
