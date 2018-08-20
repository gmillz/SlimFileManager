package com.slim.slimfilemanager.utils

import android.content.Context

import com.slim.slimfilemanager.utils.file.BaseFile

import org.apache.commons.compress.archivers.ArchiveException
import org.apache.commons.compress.archivers.ArchiveOutputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.utils.IOUtils

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayList
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ArchiveUtils {

    private val BUFFER = 4096

    fun extractZipFiles(zipFile: String, location: String): String? {
        var location = location
        val data = ByteArray(BUFFER)
        val zipstream: ZipInputStream

        if (!location.endsWith(File.separator)) {
            location += File.separator
        }

        location += FileUtil.removeExtension(File(zipFile).name) + File.separator

        if (!File(location).mkdirs()) return null

        try {
            zipstream = ZipInputStream(FileInputStream(zipFile))

            var entry: ZipEntry = zipstream.nextEntry
            while (entry != null) {
                var buildDir = location
                val dirs = entry.name.split("/".toRegex()).dropLastWhile({ it.isEmpty() })
                        .toTypedArray()

                if (dirs.size > 0) {
                    for (i in 0 until dirs.size - 1) {
                        buildDir += dirs[i] + "/"
                        if (!File(buildDir).mkdirs()) return null
                    }
                }

                var read: Int = zipstream.read(data, 0, BUFFER)
                val out = FileOutputStream(
                        location + entry.name)
                while (read != -1) {
                    out.write(data, 0, read)
                    read = zipstream.read(data, 0, BUFFER)
                }

                zipstream.closeEntry()
                out.close()
                entry = zipstream.nextEntry
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return location
    }

    fun createZipFile(zip: String, files: ArrayList<BaseFile>): String {
        var zip = zip
        if (!zip.startsWith(File.separator)) {
            zip = BackgroundUtils.ARCHIVE_LOCATION + File.separator + zip
        }
        if (!zip.endsWith("zip")) {
            zip += ".zip"
        }
        val outFile = File(zip)
        try {
            val dest = FileOutputStream(outFile)
            val out = ZipOutputStream(BufferedOutputStream(
                    dest))

            for (bf in files) {
                bf.getFile { file ->
                    try {
                        if (file.isDirectory) {
                            zipFolder(out, file, file.parent.length)
                        } else {
                            zipFile(out, file)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return BackgroundUtils.ARCHIVE_LOCATION
    }

    @Throws(IOException::class)
    private fun zipFolder(out: ZipOutputStream, folder: File,
                          basePathLength: Int) {
        val fileList = folder.listFiles()

        for (file in fileList) {
            if (file.isDirectory) {
                zipFolder(out, file, basePathLength)
            } else {
                val origin: BufferedInputStream
                val data = ByteArray(BUFFER)
                val unmodifiedFilePath = file.path
                val relativePath = unmodifiedFilePath
                        .substring(basePathLength)

                val fi = FileInputStream(unmodifiedFilePath)
                origin = BufferedInputStream(fi, BUFFER)
                val entry = ZipEntry(relativePath)
                out.putNextEntry(entry)
                var count: Int = origin.read(data, 0, BUFFER)
                while (count != -1) {
                    out.write(data, 0, count)
                    count = origin.read(data, 0, BUFFER)

                }
                origin.close()
            }
        }
    }

    @Throws(IOException::class)
    private fun zipFile(out: ZipOutputStream, file: File) {
        val origin: BufferedInputStream
        val data = ByteArray(BUFFER)
        val str = file.path

        val fi = FileInputStream(str)
        origin = BufferedInputStream(fi, BUFFER)
        val entry = ZipEntry(str.substring(str.lastIndexOf("/") + 1))
        out.putNextEntry(entry)
        var count: Int = origin.read(data, 0, BUFFER)
        while (count != -1) {
            out.write(data, 0, count)
            count = origin.read(data, 0, BUFFER)
        }
        origin.close()
    }

    fun unTar(context: Context, input: String, output: String): String? {
        var output = output
        var inputFile = File(input)

        var deleteAfter = false
        if (FileUtil.getExtension(inputFile.name) == "gz") {
            inputFile = File(unGzip(input, BackgroundUtils.EXTRACTED_LOCATION))
            deleteAfter = true
        }

        if (!output.endsWith(File.separator)) {
            output += File.separator
        }

        output += FileUtil.removeExtension(inputFile.name)

        var outputDir = File(output)

        if (outputDir.exists()) {
            for (i in 1 until Integer.MAX_VALUE) {
                val test = File(outputDir.toString() + "-" + i)
                if (test.exists()) continue
                outputDir = test
                break
            }
        }

        if (!outputDir.mkdirs()) return null

        try {
            val `is` = FileInputStream(inputFile)
            val tis = ArchiveStreamFactory().createArchiveInputStream("tar",
                    `is`) as TarArchiveInputStream

            var entry: TarArchiveEntry = tis.nextTarEntry
            while (entry != null) {
                val outputFile = File(outputDir, entry.name)
                if (entry.isDirectory) {
                    if (!outputFile.exists()) {
                        if (!outputFile.mkdirs()) {
                            throw IllegalStateException(
                                    String.format("Couldn't create directory %s.",
                                            outputFile.absolutePath))
                        }
                    }
                } else {
                    val out = FileOutputStream(outputFile)
                    IOUtils.copy(tis, out)
                    out.close()
                }
                entry = tis.nextEntry as TarArchiveEntry
            }
            tis.close()
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ArchiveException) {
            e.printStackTrace()
        }

        if (deleteAfter) FileUtil.deleteFile(context, input)

        return outputDir.absolutePath
    }

    fun createTar(tar: String, files: ArrayList<BaseFile>): String {
        var tar = tar
        if (!tar.startsWith(File.separator)) {
            tar = BackgroundUtils.ARCHIVE_LOCATION + File.separator + tar
        }
        var tarFile = File(tar)
        if (FileUtil.getExtension(tarFile.name) != "tar") {
            tar += ".tar"
            tarFile = File(tar)
        }

        try {
            val os = FileOutputStream(tarFile)
            val aos = ArchiveStreamFactory()
                    .createArchiveOutputStream(ArchiveStreamFactory.TAR, os)
            for (bf in files) {
                bf.getFile { file ->
                    try {
                        addFilesToCompression(aos, file, ".")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            aos.finish()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ArchiveException) {
            e.printStackTrace()
        }

        return BackgroundUtils.ARCHIVE_LOCATION
    }

    fun createTarGZ(tarFile: String, files: ArrayList<BaseFile>): String {
        var tarFile = tarFile

        if (!tarFile.startsWith(File.separator)) {
            tarFile = BackgroundUtils.ARCHIVE_LOCATION + File.separator + tarFile
        }
        if (!tarFile.endsWith("tar.gz")) {
            tarFile += ".tar.gz"
        }
        val outFile = File(tarFile)
        try {
            val fos = FileOutputStream(outFile)

            val taos = TarArchiveOutputStream(
                    GZIPOutputStream(BufferedOutputStream(fos)))
            taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR)
            taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU)
            for (bf in files) {
                bf.getFile { file ->
                    try {
                        addFilesToCompression(taos, file, ".")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            taos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return BackgroundUtils.ARCHIVE_LOCATION
    }

    //add entries to archive file...
    @Throws(IOException::class)
    private fun addFilesToCompression(
            taos: ArchiveOutputStream, file: File, dir: String) {

        taos.putArchiveEntry(TarArchiveEntry(file, dir + "/" + file.name))

        if (file.isFile) {
            // Add the file to the archive
            val bis = BufferedInputStream(FileInputStream(file))
            IOUtils.copy(bis, taos)
            taos.closeArchiveEntry()
            bis.close()

        } else if (file.isDirectory) {
            // close the archive entry
            taos.closeArchiveEntry()
            // go through all the files in the directory and using recursion, add them to the archive

            for (childFile in file.listFiles()) {
                addFilesToCompression(taos, childFile, file.name)
            }
        }

    }

    fun unGzip(input: String, output: String): String {
        val inputFile = File(input)
        var outputFile = File(output)

        outputFile = File(outputFile, FileUtil.removeExtension(input))

        if (outputFile.exists()) {
            val ext = FileUtil.getExtension(outputFile.name)
            val file = FileUtil.removeExtension(outputFile.absolutePath)
            for (i in 1 until Integer.MAX_VALUE) {
                val test = File("$file-$i.$ext")
                if (test.exists()) continue
                outputFile = test
                break
            }
        }

        try {
            val `in` = GZIPInputStream(FileInputStream(inputFile))
            val out = FileOutputStream(outputFile)
            IOUtils.copy(`in`, out)
            `in`.close()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return outputFile.absolutePath
    }
}
