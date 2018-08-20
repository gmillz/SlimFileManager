package com.slim.slimfilemanager.utils

import android.content.Context

import java.io.File

class Permissions {

    lateinit var owner: String
    lateinit var group: String

    var userRead: Boolean = false
    var userWrite: Boolean = false
    var userExecute: Boolean = false

    var groupRead: Boolean = false
    var groupWrite: Boolean = false
    var groupExecute: Boolean = false

    var otherRead: Boolean = false
    var otherWrite: Boolean = false
    var otherExecute: Boolean = false

    val octalPermissions: String
        get() {
            var user = 0
            var group = 0
            var other = 0

            if (userRead) {
                user += 4
            }
            if (userWrite) {
                user += 2
            }
            if (userExecute) {
                user += 1
            }

            if (groupRead) {
                group += 4
            }
            if (groupWrite) {
                group += 2
            }
            if (groupExecute) {
                group += 1
            }

            if (otherRead) {
                other += 4
            }
            if (otherWrite) {
                other += 2
            }
            if (otherExecute) {
                other += 1
            }

            return user.toString() + group + other
        }

    val string: String
        get() {
            var string = ""
            string += if (userRead) 'r' else '-'
            string += if (userWrite) 'w' else '-'
            string += if (userExecute) 'x' else '-'
            string += if (groupRead) 'r' else '-'
            string += if (groupWrite) 'w' else '-'
            string += if (groupExecute) 'x' else '-'
            string += if (otherRead) 'r' else '-'
            string += if (otherWrite) 'w' else '-'
            string += if (otherExecute) 'x' else '-'
            return string
        }

    fun loadFromFile(context: Context, file: File) {
        val fileInfo = FileUtil.getFileProperties(context, file)
        var line: String? = null

        if (fileInfo != null) {
            owner = fileInfo[1]
            group = fileInfo[2]
            line = fileInfo[0]
        }

        if (line == null || line.length != 10) {
            return
        }

        userRead = line[1] == 'r'
        userWrite = line[2] == 'w'
        userExecute = line[3] == 'x'

        groupRead = line[4] == 'r'
        groupWrite = line[5] == 'w'
        groupExecute = line[6] == 'x'

        otherRead = line[7] == 'r'
        otherWrite = line[8] == 'w'
        otherExecute = line[9] == 'x'
    }

    override fun equals(o: Any?): Boolean {
        if (o !is Permissions) {
            return false
        }
        val p = o as Permissions?
        return string == p!!.string && owner == p.owner && group == p.group
    }
}
