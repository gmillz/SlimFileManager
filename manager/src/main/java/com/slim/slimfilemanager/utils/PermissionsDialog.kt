package com.slim.slimfilemanager.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.support.annotation.IdRes
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText

import com.slim.slimfilemanager.R

import java.io.File

class PermissionsDialog(private val mContext: Context, file: String) :
        CompoundButton.OnCheckedChangeListener {

    private var mBuilder: AlertDialog.Builder? = null

    private val mOriginalPermissions: Permissions
    private val mPermissions: Permissions
    private var mOwner: EditText? = null
    private var mGroup: EditText? = null
    private var uRead: CheckBox? = null
    private var uWrite: CheckBox? = null
    private var uExecute: CheckBox? = null
    private var gRead: CheckBox? = null
    private var gWrite: CheckBox? = null
    private var gExecute: CheckBox? = null
    private var oRead: CheckBox? = null
    private var oWrite: CheckBox? = null
    private var oExecute: CheckBox? = null
    private var mView: View? = null
    private val mFile: File

    val dialog: Dialog
        get() = mBuilder!!.create()

    init {
        mFile = File(file)

        mOriginalPermissions = Permissions()
        mOriginalPermissions.loadFromFile(mContext, mFile)

        mPermissions = mOriginalPermissions

        init()
    }

    override fun onCheckedChanged(view: CompoundButton, isChecked: Boolean) {
        if (view === uRead) {
            mPermissions.userRead = isChecked
        } else if (view === uWrite) {
            mPermissions.userWrite = isChecked
        } else if (view === uExecute) {
            mPermissions.userExecute = isChecked
        } else if (view === gRead) {
            mPermissions.groupRead = isChecked
        } else if (view === gWrite) {
            mPermissions.groupWrite = isChecked
        } else if (view === gExecute) {
            mPermissions.groupExecute = isChecked
        } else if (view === oRead) {
            mPermissions.otherRead = isChecked
        } else if (view === oWrite) {
            mPermissions.otherWrite = isChecked
        } else if (view === oExecute) {
            mPermissions.otherExecute = isChecked
        }
    }

    private fun init() {
        initViews()
        initBuilder()
        initValues()
    }

    private fun initValues() {
        mOwner!!.setText(mOriginalPermissions.owner)
        mGroup!!.setText(mOriginalPermissions.group)

        uRead!!.isChecked = mOriginalPermissions.userRead
        uWrite!!.isChecked = mOriginalPermissions.userWrite
        uExecute!!.isChecked = mOriginalPermissions.userExecute
        gRead!!.isChecked = mOriginalPermissions.groupRead
        gWrite!!.isChecked = mOriginalPermissions.groupWrite
        gExecute!!.isChecked = mOriginalPermissions.groupExecute
        oRead!!.isChecked = mOriginalPermissions.otherRead
        oWrite!!.isChecked = mOriginalPermissions.otherWrite
        oExecute!!.isChecked = mOriginalPermissions.otherExecute
    }

    private fun initViews() {
        mView = View.inflate(mContext, R.layout.permissions, null)
        mOwner = mView!!.findViewById<View>(R.id.owner) as EditText
        mGroup = mView!!.findViewById<View>(R.id.group) as EditText

        uRead = mView!!.findViewById<View>(R.id.uread) as CheckBox
        uWrite = mView!!.findViewById<View>(R.id.uwrite) as CheckBox
        uExecute = mView!!.findViewById<View>(R.id.uexecute) as CheckBox
        gRead = mView!!.findViewById<View>(R.id.gread) as CheckBox
        gWrite = mView!!.findViewById<View>(R.id.gwrite) as CheckBox
        gExecute = mView!!.findViewById<View>(R.id.gexecute) as CheckBox
        oRead = mView!!.findViewById<View>(R.id.oread) as CheckBox
        oWrite = mView!!.findViewById<View>(R.id.owrite) as CheckBox
        oExecute = mView!!.findViewById<View>(R.id.oexecute) as CheckBox

        val ids = intArrayOf(R.id.uread, R.id.uwrite, R.id.uexecute, R.id.gread, R.id.gwrite,
                R.id.gexecute, R.id.oread, R.id.owrite, R.id.oexecute)
        for (id in ids) {
            (mView!!.findViewById<View>(id) as CheckBox).setOnCheckedChangeListener(this)
        }
    }

    private fun initBuilder() {
        mBuilder = AlertDialog.Builder(mContext)
        mBuilder!!.setTitle(mFile.name)
        mBuilder!!.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
        mBuilder!!.setPositiveButton(R.string.apply) { dialog, which ->
            if (mPermissions == mOriginalPermissions)
                FileUtil.applyPermissions(mContext, mFile, mPermissions)
            FileUtil.changeGroupOwner(mContext, mFile, mPermissions.owner, mPermissions.group)
            dialog.dismiss()
        }
        mBuilder!!.setView(mView)
    }
}
