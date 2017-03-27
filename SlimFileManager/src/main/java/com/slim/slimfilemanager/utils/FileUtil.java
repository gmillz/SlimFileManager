package com.slim.slimfilemanager.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.v4.provider.DocumentFile;
import android.telephony.IccOpenLogicalChannelResponse;
import android.text.TextUtils;

import com.slim.slimfilemanager.settings.SettingsProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import trikita.log.Log;

public class FileUtil {

    public static boolean copyFile(Context context, String f, String fol) {
        File file = new File(f);
        File folder = new File(fol);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            FileInputStream fis = null;
            OutputStream os = null;
            try {
                fis = new FileInputStream(file);
                DocumentFile target = DocumentFile.fromFile(folder);
                os = context.getContentResolver().openOutputStream(target.getUri());
                IOUtils.copy(fis, os);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                    if (os != null) {
                        os.flush();
                        os.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (file.exists()) {
            if (!folder.exists()) {
                if (!mkdir(context, folder)) return false;
            }
            try {
                if (file.isDirectory()) {
                    FileUtils.copyDirectoryToDirectory(file, folder);
                } else if (file.isFile()) {
                    FileUtils.copyFileToDirectory(file, folder);
                }
                return true;
            } catch (IOException e) {
                return SettingsProvider.getBoolean(context, SettingsProvider.KEY_ENABLE_ROOT, false)
                        && RootUtils.isRootAvailable() && RootUtils.copyFile(f, fol);
            }
        } else {
            return false;
        }
    }

    public static boolean moveFile(Context context, String source, String destination) {
        if (TextUtils.isEmpty(source) || TextUtils.isEmpty(destination)) {
            return false;
        }
        File file = new File(source);
        File folder = new File(destination);

        try {
            FileUtils.moveFileToDirectory(file, folder, true);
            return true;
        } catch (IOException e) {
            return SettingsProvider.getBoolean(context, SettingsProvider.KEY_ENABLE_ROOT, false)
                    && RootUtils.isRootAvailable() && RootUtils.moveFile(source, destination);
        }
    }

    public static boolean deleteFile(Context context, String path) {
        try {
            FileUtils.forceDelete(new File(path));
            return true;
        } catch (IOException e) {
            return SettingsProvider.getBoolean(context, SettingsProvider.KEY_ENABLE_ROOT, false)
                    && RootUtils.isRootAvailable() && RootUtils.deleteFile(path);
        }
    }

    public static String[] getFileProperties(Context context, File file) {
        String[] info = null;

        RootUtils.CommandOutput out;
        if (SettingsProvider.getBoolean(context, SettingsProvider.KEY_ENABLE_ROOT, false)
                && RootUtils.isRootAvailable()) {
            out = RootUtils.runCommand("ls -l " + file.getAbsolutePath());
        } else {
            out = runCommand("ls -l " + file.getAbsolutePath());
        }
        if (out == null) return null;
        if (TextUtils.isEmpty(out.error) && out.exitCode == 0) {
            info = getAttrs(out.output);
        }
        return info;
    }

    private static String[] getAttrs(String string) {
        if (string.length() < 44) {
            throw new IllegalArgumentException("Bad ls -l output: " + string);
        }
        final char[] chars = string.toCharArray();

        final String[] results = new String[11];
        int ind = 0;
        final StringBuilder current = new StringBuilder();

        Loop:
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case ' ':
                case '\t':
                    if (current.length() != 0) {
                        results[ind] = current.toString();
                        ind++;
                        current.setLength(0);
                        if (ind == 10) {
                            results[ind] = string.substring(i).trim();
                            break Loop;
                        }
                    }
                    break;

                default:
                    current.append(chars[i]);
                    break;
            }
        }

        return results;
    }

    public static RootUtils.CommandOutput runCommand(String cmd) {
        RootUtils.CommandOutput output = new RootUtils.CommandOutput();
        try {
            Process process = Runtime.getRuntime().exec("sh");
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
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static boolean changeGroupOwner(Context context, File file, String owner, String group) {
        try {
            boolean useRoot = false;
            if (!file.canWrite() && SettingsProvider.getBoolean(context,
                    SettingsProvider.KEY_ENABLE_ROOT, false)
                    && RootUtils.isRootAvailable()) {
                useRoot = true;
                RootUtils.remountSystem("rw");
            }

            if (useRoot) {
                RootUtils.runCommand("chown " + owner + "." + group + " "
                        + file.getAbsolutePath());
            } else {
                runCommand("chown " + owner + "." + group + " "
                        + file.getAbsolutePath());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean applyPermissions(Context context, File file, Permissions permissions) {
        try {
            boolean useSu = false;
            if (!file.canWrite() && SettingsProvider.getBoolean(context,
                    SettingsProvider.KEY_ENABLE_ROOT, false)
                    && RootUtils.isRootAvailable()) {
                useSu = true;
                RootUtils.remountSystem("rw");
            }

            if (useSu) {
                RootUtils.runCommand("chmod " + permissions.getOctalPermissions() + " "
                        + file.getAbsolutePath());
            } else {
                runCommand("chmod " + permissions.getOctalPermissions() + " "
                        + file.getAbsolutePath());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean mkdir(Context context, File dir) {
        if (dir.exists()) {
            return false;
        }
        if (dir.mkdirs()) {
            return true;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DocumentFile document = DocumentFile.fromFile(dir.getParentFile());
                if (document.exists()) {
                    if (document.createDirectory(dir.getAbsolutePath()).exists()) {
                        return true;
                    }
                }
            }
        }

        return (SettingsProvider.getBoolean(context,
                SettingsProvider.KEY_ENABLE_ROOT, false) && RootUtils.isRootAvailable()
                && RootUtils.createFolder(dir));
    }

    public static String getExtension(String fileName) {
        return FilenameUtils.getExtension(fileName);
    }

    public static String removeExtension(String s) {
        return FilenameUtils.removeExtension(new File(s).getName());
    }

    public static void writeFile(Context context, String content, File file, String encoding) {
        if (file.canWrite()) {
            try {
                FileUtils.write(file, content, encoding);
            } catch (IOException e) {
                // ignore
            }
        } else if (SettingsProvider.getBoolean(context, SettingsProvider.KEY_ENABLE_ROOT, false)) {
            RootUtils.writeFile(file, content);
        }
    }

    public static void renameFile(Context context, File oldFile, File newFile) {
        if (oldFile.renameTo(newFile)) {
            return;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DocumentFile document = DocumentFile.fromFile(oldFile);
                if (document.renameTo(newFile.getAbsolutePath())) {
                    return;
                }
            }
        }
        if (SettingsProvider.getBoolean(context, SettingsProvider.KEY_ENABLE_ROOT, false)) {
            RootUtils.renameFile(oldFile, newFile);
        }
    }

    public static long getFileSize(File file) {
        return file.length();
    }

    public static void writeUri(Context context,
                                Uri uri, String newContent, String encoding) throws IOException {
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "w");
        if (pfd != null) {
            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(newContent.getBytes(Charset.forName(encoding)));
            fileOutputStream.close();
            pfd.close();
        }
    }
}
