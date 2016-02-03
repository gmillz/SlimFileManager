package com.slim.slimfilemanager.utils;

import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class RootUtils {

    protected final static Pattern sEscape = Pattern.compile("([\"\'`\\\\])");

    public static class CommandOutput {
        String output;
        String error;
        int exitCode;
    }

    public static ArrayList<String> listFiles(String path, boolean showHidden) {
        if (!isRootAvailable()) return null;
        ArrayList<String> mDirContent = new ArrayList<>();
        CommandOutput output = runCommand("ls -a " + "\"" + path + "\"\n");
        if (output == null) {
            return mDirContent;
        }
        String[] split = output.output.split("\n");
        for (String line : split) {
            if (!showHidden) {
                if (line.charAt(0) != '.')
                    mDirContent.add(path + "/" + line);
            } else {
                mDirContent.add(path + "/" + line);
            }
        }
        return mDirContent;
    }

    public static CommandOutput runCommand(String cmd) {
        if (!isRootAvailable()) return null;
        CommandOutput output = new CommandOutput();
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(
                    process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();

            output.exitCode = process.waitFor();
            output.output = IOUtils.toString(process.getInputStream());
            output.error = IOUtils.toString(process.getErrorStream());

            if (output.exitCode != 0 || (!TextUtils.isEmpty(output.error))) {
                Log.e("Root Error, cmd: " + cmd, "error: " + output.error);
            }
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static boolean copyFile(String old, String newDir) {
        if (!isRootAvailable()) return false;
        try {
            boolean remounted = false;
            if (!new File(newDir).canWrite()) {
                remounted = true;
                remountSystem("rw");
            }
            runCommand("cp -fr " + old + " " + newDir);
            if (remounted) {
                remountSystem("ro");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean moveFile(String old, String newDir) {
        if (!isRootAvailable()) return false;
        try {
            remountSystem("rw");
            runCommand("mv -f " + old + " " + newDir);
            remountSystem("ro");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return new File(newDir).exists();
    }

    public static boolean remountSystem(String mountType) {
        if (!isRootAvailable()) return false;
        CommandOutput output = runCommand("mount -o remount," + mountType + " /system \n");
        return output != null && output.exitCode == 0 && TextUtils.isEmpty(output.error);
    }

    public static boolean deleteFile(String path) {
        if (!isRootAvailable()) return false;
        try {
            remountSystem("rw");

            if (new File(path).isDirectory()) {
                runCommand("rm -rf '" + path + "'\n");
            } else {
                runCommand("rm -rf '" + path + "'\n");
            }

            remountSystem("ro");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return !new File(path).exists();
    }

    public static boolean createFile(File file) {
        if (!isRootAvailable()) return false;
        remountSystem("rw");
        runCommand("touch " + file.getAbsolutePath());
        remountSystem("ro");
        return true;
    }

    public static boolean createFolder(File folder) {
        if (!isRootAvailable()) return false;
        remountSystem("rw");
        runCommand("mkdir " + folder.getPath());
        remountSystem("ro");
        return true;
    }

    public static boolean isRootAvailable() {
        CommandOutput output = runCommand("id");
        if (output != null && TextUtils.isEmpty(output.error) && output.exitCode == 0) {
            return output.output.contains("uid=0");
        } else {
            output = runCommand("echo _TEST_");
            return output != null && output.output.contains("_TEST_");
        }
    }

    public static void writeFile(File file, String content) {
        if (!isRootAvailable()) return;
        String redirect = ">";
        String[] input = content.trim().split("\n");
        remountSystem("rw");
        for (String line : input) {
            String l = sEscape.matcher(line).replaceAll("\\\\$1");
            runCommand("echo '" + l + "' " + redirect + " '" + file.getAbsolutePath() + "' ");
            redirect = ">>";
        }
    }

    public static String readFile(File file) {
        CommandOutput out = runCommand("cat " + file.getAbsolutePath());
        return out != null ? out.output : null;
    }

    public static void renameFile(File oldFile, File newFile) {
        remountSystem("rw");
        runCommand("mv " + oldFile.getAbsolutePath() + " " + newFile.getAbsolutePath() + "\n");
        remountSystem("ro");
    }
}
