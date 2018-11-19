package com.siberika.idea.pascal.module;

import com.intellij.ide.util.importProject.ModuleDescriptor;
import com.intellij.ide.util.importProject.ProjectDescriptor;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.DetectedSourceRoot;
import com.intellij.ide.util.projectWizard.importSources.ProjectFromSourcesBuilder;
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.containers.SmartHashSet;
import com.siberika.idea.pascal.jps.model.JpsPascalModuleType;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PascalProjectStructureDetector extends ProjectStructureDetector {

    private static final Logger LOG = Logger.getInstance(PascalProjectStructureDetector.class);

    @NotNull
    @Override
    public DirectoryProcessingResult detectRoots(@NotNull final File dir, @NotNull File[] children, @NotNull File base, @NotNull List<DetectedProjectRoot> result) {
        for (File child : children) {
            ProjectData projectData = parse(child);
            if (projectData != null) {
                PascalModuleData moduleData = new PascalModuleData();
                moduleData.contentRoot = base;
                moduleData.name = projectData.getName();
                moduleData.mainFile = projectData.getMainFile();
                Set<File> dirs = new HashSet<>();
                addPaths(projectData.getUnits(), projectData.getOtherUnitFilesPath());
                addPaths(projectData.getUnits(), projectData.getIncludeFilesPath());
                for (String unit : projectData.getUnits()) {
                    File file = new File(dir, unit);
                    File parent = file.isDirectory() ? file : file.getParentFile();
                    try {
                        parent = parent.getCanonicalFile();
                    } catch (IOException e) {
                        LOG.error("Error getting canonical file: " + parent.getPath(), e);
                    }
                    if (parent.isDirectory()) {
                        if (dirs.add(parent)) {
                            LOG.info("Found root: " + parent.getPath());
                        }
                    }
                }
                for (File file : dirs) {
                    if (!FileUtil.isAncestor(moduleData.contentRoot, file, false)) {
                        LOG.info(String.format("Source root %s doesn't belong to content root %s. Trying to find common ancestor.", file.getPath(), moduleData.contentRoot.getPath()));
                        moduleData.contentRoot = findCommonAncestor(moduleData.contentRoot, file);
                    }
                    PascalSourceRoot sourceRoot = new PascalSourceRoot(moduleData, file);
                    moduleData.addRoot(sourceRoot);
                    result.add(sourceRoot);
                }
            }
        }
        return DirectoryProcessingResult.PROCESS_CHILDREN;
    }

    @Override
    public void setupProjectStructure(@NotNull Collection<DetectedProjectRoot> roots,
                                      @NotNull ProjectDescriptor projectDescriptor,
                                      @NotNull ProjectFromSourcesBuilder builder) {
        Set<PascalModuleData> moduleDataSet = new SmartHashSet<>();
        PascalSourceRoot someRoot;
        for (DetectedProjectRoot root : roots) {
            if (root instanceof PascalSourceRoot) {
                someRoot = (PascalSourceRoot) root;
                moduleDataSet.add(someRoot.moduleData);
            }
        }
        if (moduleDataSet.isEmpty()) {
            return;
        }

        List<ModuleDescriptor> modules = new SmartList<>();
        for (PascalModuleData moduleData : moduleDataSet) {
            if (!builder.hasRootsFromOtherDetectors(this)) {
                ModuleDescriptor moduleDescriptor = new ModuleDescriptor(moduleData.contentRoot, PascalModuleType.getInstance(), Collections.emptyList());
                if (moduleData.name != null) {
                    moduleDescriptor.setName(moduleData.name);
                }
                modules.add(moduleDescriptor);
                for (DetectedProjectRoot root : roots) {
                    if ((root instanceof PascalSourceRoot) && moduleData.roots.contains(root)) {
                        moduleDescriptor.addSourceRoot(moduleData.contentRoot, (DetectedSourceRoot) root);
                        ((PascalSourceRoot) root).cleanUp();
                    }
                }
                moduleDescriptor.addConfigurationUpdater(new ModuleBuilder.ModuleConfigurationUpdater() {
                    @Override
                    public void update(@NotNull Module module, @NotNull ModifiableRootModel rootModel) {
                        if (PascalModuleType.isPascalModule(module)) {
                            module.setOption(JpsPascalModuleType.USERDATA_KEY_MAIN_FILE.toString(), moduleData.mainFile);
                        }
                    }
                });
            }
        }
        projectDescriptor.setModules(modules);
    }

    static String getMainFile(File base, String name) {
        File file = new File(name);
        if (file.isAbsolute() || (null == base)) {
            return com.siberika.idea.pascal.jps.util.FileUtil.getCanonicalPath(file);
        } else {
            file = new File(base, name);
            return com.siberika.idea.pascal.jps.util.FileUtil.getCanonicalPath(file);
        }
    }

    private ProjectData parse(File child) {
        if (!child.isFile()) {
            return null;
        }
        String name = child.getName().toUpperCase();
        if (name.endsWith(".LPI")) {
            LOG.debug("Parsing Lazarus project file: " + child.getPath());
            return LpiParser.parse(child);
        } else if (name.endsWith(".DPROJ")) {
            LOG.debug("Parsing Delphi project file: " + child.getPath());
            return DProjParser.parse(child);
        } else if (name.endsWith(".DPR")) {
            LOG.debug("Parsing Delphi program file: " + child.getPath());
            return DPRParser.parse(child);
        }
        return null;
    }

    private void addPaths(List<String> units, String paths) {
        if (paths != null) {
            units.addAll(Arrays.asList(paths.split(";")));
        }
    }

    private File findCommonAncestor(File contentRoot, File file) {
        File parent = contentRoot.getParentFile();
        int count = 10;
        while ((count > 0) && (parent != null) && !FileUtil.isAncestor(parent, file, false)) {
            parent = parent.getParentFile();
            count--;
        }
        if ((parent != null) && FileUtil.isAncestor(parent, file, false)) {
            LOG.info("Found common ancestor: " + parent.getPath());
            return parent;
        } else {
            LOG.info("WARN: failed to find common ancestor");
            return contentRoot;
        }
    }

    private static class PascalSourceRoot extends DetectedSourceRoot {
        private PascalModuleData moduleData;

        PascalSourceRoot(PascalModuleData moduleData, File file) {
            super(file, null);
            this.moduleData = moduleData;
        }

        @NotNull
        @Override
        public String getRootTypeName() {
            return "Pascal source";
        }

        void cleanUp() {
            moduleData = null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PascalSourceRoot that = (PascalSourceRoot) o;
            return Objects.equals(getDirectory(), that.getDirectory());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getDirectory());
        }
    }

    private class PascalModuleData {
        private String name;
        private File contentRoot;
        public String mainFile;
        private Set<DetectedSourceRoot> roots = new SmartHashSet<>();

        void addRoot(PascalSourceRoot sourceRoot) {
            roots.add(sourceRoot);
        }
    }
}
