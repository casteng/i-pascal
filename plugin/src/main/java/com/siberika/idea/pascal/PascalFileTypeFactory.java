package com.siberika.idea.pascal;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * User: George Bakhtadze
 * Date: 09.12.2012
 */
public class PascalFileTypeFactory extends FileTypeFactory {
  @Override
  public void createFileTypes(@NotNull FileTypeConsumer fileTypeConsumer) {
      fileTypeConsumer.consume(PascalFileType.INSTANCE, "pas;pp;lpr;dpr;inc");
      fileTypeConsumer.consume(PPUFileType.INSTANCE, "ppu");
      fileTypeConsumer.consume(DCUFileType.INSTANCE, "dcu");

      fileTypeConsumer.consume(XmlFileType.INSTANCE, "lpi;dproj");
  }
}
