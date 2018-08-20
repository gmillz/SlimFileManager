package com.slim.slimfilemanager.utils

import android.webkit.MimeTypeMap

import java.util.HashMap
import java.util.Locale
import java.util.regex.Pattern

object MimeUtils {

    val ALL_MIME_TYPES = "*/*"
    val MIME_CODE =
            arrayOf("cs", "php", "js", "java", "py", "rb", "aspx", "cshtml", "vbhtml", "go", "c",
                    "h", "cc", "cpp", "hh", "hpp", "pl", "pm", "t", "pod", "m", "f", "for", "f90",
                    "f95", "asp", "json", "wiki", "lua", "r")
    val MIME_SQL = arrayOf("sql", "mdf", "ndf", "ldf")
    val MIME_MARKDOWN = arrayOf("md", "mdown", "markdown")
    private val MIME_TYPES = HashMap<String, String>()

    init {
        /*
     * ================= MIME TYPES ====================
	 */
        MIME_TYPES["asm"] = "text/x-asm"
        MIME_TYPES["def"] = "text/plain"
        MIME_TYPES["in"] = "text/plain"
        MIME_TYPES["rc"] = "text/plain"
        MIME_TYPES["list"] = "text/plain"
        MIME_TYPES["log"] = "text/plain"
        MIME_TYPES["pl"] = "text/plain"
        MIME_TYPES["prop"] = "text/plain"
        MIME_TYPES["properties"] = "text/plain"
        MIME_TYPES["rc"] = "text/plain"
        MIME_TYPES["sh"] = "text/plain"

        MIME_TYPES["epub"] = "application/epub+zip"
        MIME_TYPES["ibooks"] = "application/x-ibooks+zip"

        MIME_TYPES["ifb"] = "text/calendar"
        MIME_TYPES["eml"] = "message/rfc822"
        MIME_TYPES["msg"] = "application/vnd.ms-outlook"

        MIME_TYPES["ace"] = "application/x-ace-compressed"
        MIME_TYPES["bz"] = "application/x-bzip"
        MIME_TYPES["bz2"] = "application/x-bzip2"
        MIME_TYPES["cab"] = "application/vnd.ms-cab-compressed"
        MIME_TYPES["gz"] = "application/x-gzip"
        MIME_TYPES["lrf"] = "application/octet-stream"
        MIME_TYPES["jar"] = "application/java-archive"
        MIME_TYPES["xz"] = "application/x-xz"
        MIME_TYPES["Z"] = "application/x-compress"

        MIME_TYPES["bat"] = "application/x-msdownload"
        MIME_TYPES["ksh"] = "text/plain"

        MIME_TYPES["db"] = "application/octet-stream"
        MIME_TYPES["db3"] = "application/octet-stream"

        MIME_TYPES["otf"] = "x-font-otf"
        MIME_TYPES["ttf"] = "x-font-ttf"
        MIME_TYPES["psf"] = "x-font-linux-psf"

        MIME_TYPES["cgm"] = "image/cgm"
        MIME_TYPES["btif"] = "image/prs.btif"
        MIME_TYPES["dwg"] = "image/vnd.dwg"
        MIME_TYPES["dxf"] = "image/vnd.dxf"
        MIME_TYPES["fbs"] = "image/vnd.fastbidsheet"
        MIME_TYPES["fpx"] = "image/vnd.fpx"
        MIME_TYPES["fst"] = "image/vnd.fst"
        MIME_TYPES["mdi"] = "image/vnd.ms-mdi"
        MIME_TYPES["npx"] = "image/vnd.net-fpx"
        MIME_TYPES["xif"] = "image/vnd.xiff"
        MIME_TYPES["pct"] = "image/x-pict"
        MIME_TYPES["pic"] = "image/x-pict"

        MIME_TYPES["adp"] = "audio/adpcm"
        MIME_TYPES["au"] = "audio/basic"
        MIME_TYPES["snd"] = "audio/basic"
        MIME_TYPES["m2a"] = "audio/mpeg"
        MIME_TYPES["m3a"] = "audio/mpeg"
        MIME_TYPES["oga"] = "audio/ogg"
        MIME_TYPES["spx"] = "audio/ogg"
        MIME_TYPES["aac"] = "audio/x-aac"
        MIME_TYPES["mka"] = "audio/x-matroska"

        MIME_TYPES["jpgv"] = "video/jpeg"
        MIME_TYPES["jpgm"] = "video/jpm"
        MIME_TYPES["jpm"] = "video/jpm"
        MIME_TYPES["mj2"] = "video/mj2"
        MIME_TYPES["mjp2"] = "video/mj2"
        MIME_TYPES["mpa"] = "video/mpeg"
        MIME_TYPES["ogv"] = "video/ogg"
        MIME_TYPES["flv"] = "video/x-flv"
        MIME_TYPES["mkv"] = "video/x-matroska"
    }

    fun getMimeType(name: String): String? {
        val extension = FileUtil.getExtension(name)
        var type: String? = null

        if (extension != null && !extension.isEmpty()) {
            val extensionLowerCase = extension.toLowerCase(Locale
                    .getDefault())
            val mime = MimeTypeMap.getSingleton()
            type = mime.getMimeTypeFromExtension(extensionLowerCase)
            if (type == null) {
                type = MIME_TYPES[extensionLowerCase]
            }
        }
        return type
    }

    private fun mimeTypeMatch(mime: String, input: String): Boolean {
        return Pattern.matches(mime.replace("*", ".*"), input)
    }

    fun isPicture(name: String): Boolean {
        val mime = getMimeType(name)
        return mime != null && mimeTypeMatch("image/*", mime)
    }

    fun isVideo(name: String): Boolean {
        val mime = getMimeType(name)
        return mime != null && mimeTypeMatch("video/*", mime)
    }

    fun isApp(name: String): Boolean {
        return name.endsWith(".apk")
    }

    fun isTextFile(name: String): Boolean {
        val mime = getMimeType(name)
        return mime != null && mimeTypeMatch("text/*", mime)
    }
}
