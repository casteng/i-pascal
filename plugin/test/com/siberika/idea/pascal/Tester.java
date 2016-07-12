package com.siberika.idea.pascal;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @since 4/14/2016 3:05 AM
 */
@Ignore
public class Tester {
    private static final long MAX_SIZE = 1024 * 1024 * 100;

    @Test
    public void testSI() throws Exception {
        final String fn = ROOT + "fpc/settings.ini";
        FileOutputStream os = null;
        final String section = "stable";
        final String fpcUrl = "http://svn.freepascal.org/svn/fpc/tags/release_3_0_0";
        final String fpcDir = ROOT + "fpc/up";
        final String binutilsdir = ROOT + "fpc/binutils";
        final String fpcbootstrapdir = ROOT + "fpc/bootstrap";
        final String onlyOptions = "";
        try {
            os = new FileOutputStream(new File(fn));
            PrintWriter w = new PrintWriter(os);
            w.print(String.format(TPL, section, fpcDir, fpcUrl, binutilsdir, fpcbootstrapdir, onlyOptions));
            w.flush();
            w.close();
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    private static final String TPL = "[%s]\n" +
            "fpcdir=%s\n" +
            "fpcurl=%s\n" +
            "; Do not create a batch file with shortcut\n" +
            "fpcuplinkname=\"\"\n" +
            "; We can use the binutils/bootstrap dirs that we usually use:\n" +
            "binutilsdir=%s\n" +
            "fpcbootstrapdir=%s\n" +
            "; Just install/update, no questions asked:\n" +
            "noconfirm=true\n" +
            "; In case you want to submit patches, it's nice to be able to update\n" +
            "; without overwriting your fixes:\n" +
            "keeplocalchanges=true\n" +
            "; Specify we only want FPC, not Lazarus\n" +
            "; in case of fpc patches FPCBuildOnly\n" +
            "; Use fpc -Px86_64 for cross compiling to 64 bit.\n" +
            "only=FPC%s\n" +
            "skip=helplazarus,lazarus,lazbuild,useride\n";

    private static final String ROOT =
            //"c:\\dist/fpc/";
            "/home/me/Dropbox/fpc/";
    private static final String PLATFORM =
            //"i386-win32";
            "x86_64-linux";
    private static final String EXE_NAME =
            //"fpcup.exe";
            "fpcup_linux_x64";

    @Test
    public void testDL() throws Exception {
        //URL target = new URL("https://github.com/LongDirtyAnimAlf/Reiniero-fpcup/blob/master/bin/x86_64-linux/fpcup_linux_x64?raw=true");
        URL target = new URL(String.format("https://github.com/LongDirtyAnimAlf/Reiniero-fpcup/blob/master/bin/%s/%s?raw=true", PLATFORM, EXE_NAME));
        ReadableByteChannel channel = Channels.newChannel(target.openStream());
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(new File(ROOT + "fpc/" + EXE_NAME));
            os.getChannel().transferFrom(channel, 0, MAX_SIZE);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    private static final Pattern PATTERN_ERROR = Pattern.compile("(.+(ERROR|Error):.+)|(.*\\*\\*\\*.*Stop.*)");

    @Test
    public void testRun() throws Exception {
        BufferedReader reader = null;
        List<String> errors = new ArrayList<String>();
        try {
            final String fn = ROOT + "fpc/" + EXE_NAME;
            final File file = new File(fn);
            if (!file.setExecutable(true)) {
                System.out.println("Error setting permission");
            }
            Process process = new ProcessBuilder(fn, String.format("--inifile=%ssettings.ini", ROOT + "fpc/"), "--inisection=stable", "--verbose").redirectErrorStream(true).start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (PATTERN_ERROR.matcher(line).matches()) {
                    errors.add(line);
                }
                System.out.println("=== " + line);
            }
            final int code = process.waitFor();
            System.out.println("Exit code: " + code);
            System.out.println("Errors: " + errors.toString());
            Assert.assertTrue((code == 0) && errors.isEmpty());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    @Test
    public void QS() throws Exception {
        List<Integer> arr = Arrays.asList(3, 4, 1);
        System.out.println(arr.toString());
        quickSort(arr, 0, 2);
        System.out.println(arr.toString());
    }

    private void quickSort(List<Integer> data, int low, int high) {
        int i = low;
        int j = high;

        int pivot = data.get(low + (high - low) / 2);

        while (i <= j) {

            while (data.get(i) < pivot) {
                i++;
            }
            while (data.get(j) > pivot) {
                j--;
            }

            if (i <= j) {
                int temp = data.get(i);
                data.set(i, data.get(j));
                data.set(j, temp);
                i++;
                j--;
            }
        }
    }
}
