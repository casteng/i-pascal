package com.siberika.idea.pascal;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface PascalIcons {
    String PATH = "/icons/";

    Icon GENERAL = IconLoader.findIcon(PATH + "pascal_16x16.png");
    Icon MODULE  = GENERAL;
    Icon UNIT = IconLoader.findIcon(PATH + "unit.png");
    Icon PROGRAM = IconLoader.findIcon(PATH + "nprogram.png");
    Icon INCLUDE = IconLoader.findIcon(PATH + "ninclude.png");
    Icon COMPILED = IconLoader.findIcon(PATH + "compiled.png");

    Icon FILE_PROGRAM = IconLoader.findIcon(PATH + "program.png");
    Icon FILE_LIBRARY = IconLoader.findIcon(PATH + "library.png");
    Icon FILE_INCLUDE = IconLoader.findIcon(PATH + "include.png");

    Icon TYPE = IconLoader.findIcon(PATH + "ntype.png");
    Icon VARIABLE = IconLoader.findIcon(PATH + "nvar.png");
    Icon CONSTANT = IconLoader.findIcon(PATH + "nconst.png");
    Icon PROPERTY = IconLoader.findIcon(PATH + "nproperty.png");
    Icon ROUTINE = IconLoader.findIcon(PATH + "nroutine.png");
    Icon INTERFACE = IconLoader.findIcon(PATH + "ninterface.png");
    Icon CLASS = IconLoader.findIcon(PATH + "nclass.png");
    Icon OBJECT = IconLoader.findIcon(PATH + "nobject.png");
    Icon RECORD = IconLoader.findIcon(PATH + "nrecord.png");
    Icon HELPER = IconLoader.findIcon(PATH + "nhelper.png");

    final class Idea {
        public static final Icon RUN = loadIcon("/general/run.png", "/actions/runAll.svg");
        public static final Icon USED_BY = loadIcon("/general/inheritedMethod.svg", "/general/inheritedMethod.png");

        private static Icon loadIcon(String...paths) {
            for (String path : paths) {
                try {
                    Icon icon = IconLoader.findIcon(path);
                    if ((icon != null) && (icon.getIconWidth() > 1)) {
                        return icon;
                    }
                } catch (Throwable t) {
//                    System.out.println(String.format("Error for icon %s: %s", path, t.getMessage()));
                }
            }
            return null;
        }

    }
}
