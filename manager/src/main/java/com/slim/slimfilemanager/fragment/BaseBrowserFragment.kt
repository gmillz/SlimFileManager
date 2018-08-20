package com.slim.slimfilemanager.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.app.Fragment
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.StringRes
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.view.ActionMode
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

import com.balysv.materialripple.MaterialRippleLayout
import com.slim.slimfilemanager.FileManager
import com.slim.slimfilemanager.R
import com.slim.slimfilemanager.ThemeActivity
import com.slim.slimfilemanager.multichoice.MultiChoiceViewHolder
import com.slim.slimfilemanager.multichoice.MultiSelector
import com.slim.slimfilemanager.utils.BackgroundUtils
import com.slim.slimfilemanager.utils.FileUtil
import com.slim.slimfilemanager.utils.FragmentLifecycle
import com.slim.slimfilemanager.utils.MimeUtils
import com.slim.slimfilemanager.utils.PasteTask
import com.slim.slimfilemanager.utils.PasteTask.SelectedFiles
import com.slim.slimfilemanager.utils.PermissionsDialog
import com.slim.slimfilemanager.utils.SortUtils
import com.slim.slimfilemanager.utils.Utils
import com.slim.slimfilemanager.utils.file.BaseFile
import com.slim.slimfilemanager.utils.file.BasicFile
import com.slim.slimfilemanager.widget.DividerItemDecoration

import java.io.File
import java.text.DateFormat
import java.util.ArrayList
import java.util.Locale
import java.util.concurrent.ExecutionException

abstract class BaseBrowserFragment : Fragment(), View.OnClickListener, FragmentLifecycle,
                                     SearchView.OnQueryTextListener {
    protected var ACTIONS: MutableList<Int> = ArrayList()
    lateinit var currentPath: String
        protected set
    protected lateinit var mMimeType: String
    protected lateinit var mContext: Context
    protected var mActivity: FileManager? = null
    protected var mSupportsActionMode = true
    protected var mSupportsSearching = true

    protected var mActionMode: ActionMode? = null
    protected lateinit var mProgressDialog: ProgressDialog
    protected var mAdapter: ViewAdapter? = null
    protected var mFiles = ArrayList<BaseFile>()
    protected var mExitOnBack = false
    protected var mSearching = false
    protected var mPicking = false
    internal var mPath: TextView? = null
    internal lateinit var mRefreshLayout: SwipeRefreshLayout
    internal lateinit var mProgress: ProgressBar
    internal lateinit var mRecyclerView: RecyclerView
    private val mMultiSelector = MultiSelector()
    internal var mMultiSelect: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.add(0, MENU_COPY, 0, R.string.copy).setIcon(R.drawable.copy)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            menu.add(0, MENU_CUT, 0, R.string.move).setIcon(R.drawable.cut)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            menu.add(0, MENU_DELETE, 0, R.string.move).setIcon(R.drawable.delete)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            menu.add(0, MENU_PERMISSIONS, 0, R.string.permissions)
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
            menu.add(0, MENU_RENAME, 0, R.string.rename)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            menu.add(0, MENU_SHARE, 0, R.string.share).setIcon(R.drawable.share)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            menu.add(0, MENU_ARCHIVE, 0, R.string.archive)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            mActionMode = mode
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.findItem(MENU_PERMISSIONS).isVisible = mMultiSelector.selectedPositions.size == 1
            menu.findItem(MENU_RENAME).isVisible = mMultiSelector.selectedPositions.size == 1

            for (i in 0 until menu.size()) {
                val item = menu.getItem(i)
                item.isVisible = ACTIONS.contains(item.itemId)
            }
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {

            SelectedFiles.clearAll()
            for (i in mFiles.indices) {
                if (mMultiSelector.isSelected(i)) {
                    SelectedFiles.addFile(getFile(i))
                }
            }

            if (mMultiSelector.selectedPositions.size > 0) {
                val id = item.itemId
                when (id) {
                    MENU_CUT, MENU_COPY -> {
                        if (id == MENU_CUT) mActivity!!.setMove(true)
                        mActivity!!.showPaste(true)
                    }
                    MENU_DELETE -> {
                        showDialog(MENU_DELETE)
                        mode.finish()
                    }
                    MENU_PERMISSIONS -> showDialog(MENU_PERMISSIONS)
                    MENU_RENAME -> showDialog(MENU_RENAME)
                    MENU_SHARE -> handleShareFile()
                    MENU_ARCHIVE -> showDialog(MENU_ARCHIVE)
                }
            }
            mMultiSelector.clearSelections()
            mMultiSelector.setSelectable(false)
            mode.finish()
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mMultiSelector.clearSelections()
            mMultiSelector.setSelectable(false)
        }
    }
    private var mSearchView: SearchView? = null

    abstract val defaultDirectory: String

    abstract val rootFolder: String

    abstract val pasteCallback: PasteTask.Callback

    abstract fun getTabTitle(path: String): String

    abstract fun onClickFile(path: String)

    @CallSuper
    open fun filesChanged(path: String) {
        currentPath = path
        if (mActivity != null) mActivity!!.setTabTitle(this, path)
    }

    abstract fun getFile(i: Int): BaseFile

    abstract fun onDeleteFile(file: String)

    abstract fun backPressed()

    abstract fun getIconForFile(imageView: ImageView, position: Int)

    abstract fun addFile(path: String)

    abstract fun addNewFile(name: String, isFolder: Boolean)

    abstract fun renameFile(file: BaseFile, name: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ACTIONS.add(MENU_COPY)
        ACTIONS.add(MENU_DELETE)
        ACTIONS.add(MENU_RENAME)
        ACTIONS.add(MENU_SHARE)

        mContext = activity
        mActivity = activity as FileManager
        setHasOptionsMenu(true)
    }

    override fun onPauseFragment() {
        mExitOnBack = false
        if (mSearchView != null && !mSearchView!!.isIconified) {
            mSearchView!!.isIconified = true
        }
    }

    override fun onResumeFragment() {}

    fun onPreferencesChanged() {
        sortFiles()
        if (mAdapter != null) mAdapter!!.notifyDataSetChanged()
    }

    fun setPicking() {
        mPicking = true
    }

    override fun onResume() {
        super.onResume()

        if (view == null) return
        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
        view!!.setOnKeyListener { v, keyCode, event ->
            (event.action == KeyEvent.ACTION_UP
                    && keyCode == KeyEvent.KEYCODE_BACK && onBackPressed())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_browser, container, false)

        mPath = rootView.findViewById<View>(R.id.path) as TextView
        mRefreshLayout = rootView.findViewById<View>(R.id.swipe_refresh) as SwipeRefreshLayout
        mProgress = rootView.findViewById<View>(R.id.progress) as ProgressBar
        mRecyclerView = rootView.findViewById<View>(R.id.list) as RecyclerView

        var defaultDir = ""
        val extras = arguments

        if (extras != null) {
            defaultDir = extras.getString(ARG_PATH) ?: ""
        }
        if (TextUtils.isEmpty(defaultDir)) {
            defaultDir = defaultDirectory
        }
        currentPath = defaultDir

        mAdapter = ViewAdapter()

        mProgress.isIndeterminate = true
        mRefreshLayout.setColorSchemeColors(ThemeActivity.getAccentColor(mContext))
        mRefreshLayout.setOnRefreshListener { refreshFiles() }

        val layoutManager = LinearLayoutManager(mContext)
        mRecyclerView.setHasFixedSize(true)
        /*val decor = DividerItemDecoration(mContext,)
        decor.setShowFirstDivider(false)
        decor.setShowLastDivider(false)
        mRecyclerView.addItemDecoration(decor)*/
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.adapter = mAdapter

        mProgressDialog = ProgressDialog(mActivity)
        mProgressDialog.max = 100

        return rootView
    }

    fun refreshFiles() {
        onClickFile(currentPath)
    }

    fun showProgressDialog(@StringRes title: Int, indeterminate: Boolean) {
        mActivity!!.runOnUiThread {
            if (!mProgressDialog.isShowing) {
                mProgressDialog.setTitle(title)
                mProgressDialog.setMessage("Please wait...")
                mProgressDialog.isIndeterminate = indeterminate
                mProgressDialog.setProgressStyle(if (indeterminate)
                    ProgressDialog.STYLE_SPINNER
                else
                    ProgressDialog.STYLE_HORIZONTAL)
                mProgressDialog.show()
            }
        }
    }

    fun hideProgressDialog() {
        mActivity!!.runOnUiThread {
            if (mProgressDialog.isShowing) {
                mProgressDialog.hide()
            }
        }
    }

    protected fun showProgress() {
        mActivity!!.runOnUiThread {
            if (!mRefreshLayout.isRefreshing) {
                mProgress.visibility = View.VISIBLE
            }
        }
    }

    protected fun hideProgress() {
        mActivity!!.runOnUiThread {
            if (mRefreshLayout.isRefreshing) {
                mRefreshLayout.isRefreshing = false
            } else {
                mProgress.visibility = View.GONE
            }
        }
    }

    fun updateProgress(`val`: Int) {
        mActivity!!.runOnUiThread {
            if (mProgressDialog.isShowing) {
                mProgressDialog.progress = `val`
            }
        }
    }

    fun onBackPressed(): Boolean {
        if (mSearchView != null && !mSearchView!!.isIconified) {
            mSearchView!!.isIconified = true
            return true
        }
        if (currentPath != rootFolder) {
            backPressed()
            mExitOnBack = false
        } else if (currentPath == rootFolder) {
            if (mExitOnBack) {
                mActivity!!.finish()
            } else {
                Toast.makeText(mContext, getString(R.string.back_confirm),
                        Toast.LENGTH_SHORT).show()
                mExitOnBack = true
            }
        } else {
            mExitOnBack = false
        }
        return true
    }

    fun setSearching(searching: Boolean) {
        mSearching = searching
        //mSearchView.setIconified(searching);
    }

    fun setPathText(path: String) {
        if (mPath == null) return
        mActivity!!.runOnUiThread { mPath!!.text = path }
    }

    override fun onClick(v: View) {
        /*if (v == mPasteButton) {
            new PasteTask(mContext, mMove, mCurrentPath);
            filesChanged(mCurrentPath);
            mMove = false;
            //mActionMenu.setVisibility(View.VISIBLE);
            mPasteButton.setVisibility(View.GONE);
        } else*/
        if (v.tag as Int == ACTION_ADD_FOLDER) {
            showDialog(ACTION_ADD_FOLDER)
        }
    }

    fun handleShareFile() {
        val uris = ArrayList<Uri>()
        for (file in SelectedFiles.files) {
            if (file.exists()) {
                if (!file.isDirectory) {
                    if (file is BasicFile) {
                        uris.add(Uri.fromFile(file.realFile as File))
                    }
                }
            }
        }
        SelectedFiles.clearAll()

        val intent = Intent()
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (uris.size == 1) {
            intent.action = Intent.ACTION_SEND
            intent.type = MimeUtils.getMimeType(File(uris[0].path!!).name)
            intent.putExtra(Intent.EXTRA_STREAM, uris[0])
        } else {
            intent.action = Intent.ACTION_SEND_MULTIPLE
            intent.type = MimeUtils.ALL_MIME_TYPES
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        }
        if (mActionMode != null) mActionMode!!.finish()
        mActivity!!.startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (mPicking) return
        menu.findItem(R.id.search).isVisible = mSupportsSearching
        mSearchView = menu.findItem(R.id.search).actionView as SearchView
        if (mSearchView != null) {
            mSearchView!!.setIconifiedByDefault(true)
            mSearchView!!.setOnQueryTextListener(this)
            mSearchView!!.setOnSearchClickListener {
                setSearching(true)
                mActivity!!.closeDrawers()
                mFiles.clear()
                mAdapter!!.notifyDataSetChanged()
            }
            mSearchView!!.setOnCloseListener {
                mProgress.visibility = View.VISIBLE
                setSearching(false)
                filesChanged(currentPath)
                false
            }
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                setSearching(true)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextChange(newText: String): Boolean {
        if (mSearchView != null && !mSearchView!!.isIconified) {
            mFiles.clear()
            mAdapter!!.notifyDataSetChanged()
            if (!TextUtils.isEmpty(newText)) {
                mProgress.visibility = View.VISIBLE
                searchForFile(currentPath, newText)
                mProgress.visibility = View.GONE
            }
        }
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    fun setMimeType(type: String) {
        mMimeType = type
    }

    protected fun filePicked(file: File) {
        val data = Uri.fromFile(file)

        val intent = Intent()
        intent.data = data

        val activity = activity
        activity.setResult(Activity.RESULT_OK, intent)
        activity.finish()
    }

    protected fun onClickArchive(file: String) {

        val archive = File(file)

        val builder = AlertDialog.Builder(mContext)
        builder.setTitle(archive.name)
        builder.setMessage("What would you like to do with this archive?")
        builder.setPositiveButton("Extract") { dialog, which ->
            try {
                if (FileUtil.getExtension(archive.name) == "zip") {
                    onClickFile(BackgroundUtils(mContext, file,
                            BackgroundUtils.UNZIP_FILE).execute().get())
                } else if (FileUtil.getExtension(archive.name) == "tar" || FileUtil.getExtension(
                                archive.name) == "gz") {
                    onClickFile(BackgroundUtils(mContext, file,
                            BackgroundUtils.UNTAR_FILE).execute().get())
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }

            dialog.dismiss()
        }
        builder.setNegativeButton("Open") { dialog, which ->
            Utils.onClickFile(mContext, file)
            dialog.dismiss()
        }
        builder.show()
    }

    fun searchForFile(dir: String, query: String) {
        val root_dir = File(dir)
        val list = root_dir.listFiles()

        if (list != null && root_dir.canRead()) {
            if (list.size == 0) return

            for (check in list) {
                val name = check.name

                if (check.isFile && name.toLowerCase().contains(query.toLowerCase())) {
                    addFile(check.path)
                } else if (check.isDirectory) {
                    if (name.toLowerCase().contains(query.toLowerCase())) {
                        addFile(check.path)
                    }
                    if (check.canRead() && dir != "/") {
                        searchForFile(check.absolutePath, query)
                    }
                }
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            MENU_CUT, MENU_COPY, MENU_DELETE -> {
                showDialog(MENU_DELETE)
                return true
            }
            MENU_PERMISSIONS -> {
                showDialog(MENU_PERMISSIONS)
                return true
            }
        }
        return false
    }

    fun removeFile(file: String) {
        var id = -1
        for (i in mFiles.indices) {
            if (mFiles[i].getPath() == file) {
                id = i
                break
            }
        }
        if (id == -1) return
        mFiles.removeAt(id)
        mAdapter!!.notifyItemRemoved(id)
    }

    protected fun sortFiles() {
        SortUtils.sort(mContext, mFiles)
    }

    fun showDialog(id: Int) {
        val newFragment = MyAlertDialogFragment.newInstance(id)
        newFragment.setTargetFragment(this, 0)
        newFragment.show(mActivity!!.fragmentManager, "dialog $id")
    }

    protected fun fileClicked(file: File) {
        if (mPicking) {
            filePicked(file)
            return
        }
        val ext = FileUtil.getExtension(file.name)
        if (ext == "zip" || ext == "tar" || ext == "gz") {
            onClickArchive(file.absolutePath)
        } else {
            Utils.onClickFile(mContext, file.absolutePath)
        }
    }

    fun toast(@StringRes id: Int) {
        toast(mContext.getString(id))
    }

    fun toast(message: String) {
        mActivity!!.runOnUiThread { Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show() }
    }

    class MyAlertDialogFragment : DialogFragment() {

        internal val owner: BaseBrowserFragment
            get() = targetFragment as BaseBrowserFragment

        override fun onCreateDialog(savedInstanceState: Bundle): Dialog? {
            val id = arguments.getInt("id")
            val builder = AlertDialog.Builder(owner.mContext)
            when (id) {
                MENU_DELETE -> {
                    if (SelectedFiles.files.size == 1) {
                        val file = SelectedFiles.files[0]
                        if (file != null) {
                            builder.setTitle(file.name)
                        }
                    } else {
                        builder.setTitle(R.string.delete_dialog_title)
                    }
                    builder.setMessage(R.string.delete_dialog_message)
                    builder.setPositiveButton(R.string.delete
                                             ) { dialog, which ->
                        for (file in SelectedFiles.files) {
                            owner.onDeleteFile(file.realPath)
                        }
                        SelectedFiles.clearAll()
                        dialog.dismiss()
                    }
                    builder.setNegativeButton(R.string.cancel, null)
                    return builder.create()
                }
                MENU_PERMISSIONS -> {
                    val path = SelectedFiles.files[0].getPath()
                    if (TextUtils.isEmpty(path)) return null
                    val dialog = PermissionsDialog(
                            owner.mContext, path)
                    return dialog.dialog
                }
                ACTION_ADD_FILE, ACTION_ADD_FOLDER, MENU_RENAME -> {
                    val view = View.inflate(owner.mContext, R.layout.add_folder, null)
                    val folderName = view.findViewById<View>(R.id.folder_name) as EditText
                    val baseFile: BaseFile
                    if (SelectedFiles.files.size > 0) {
                        baseFile = SelectedFiles.files[0]
                    } else {
                        baseFile = BaseFile.blankFile
                    }
                    if (id == ACTION_ADD_FOLDER) {
                        builder.setTitle(R.string.create_folder)
                        folderName.setHint(R.string.folder_name_hint)
                    } else if (id == ACTION_ADD_FILE) {
                        builder.setTitle(R.string.create_file)
                        folderName.setHint(R.string.file_name_hint)
                    } else {
                        builder.setTitle(baseFile.name)
                        folderName.setText(baseFile.name)
                    }
                    builder.setView(view)
                    val listener = View.OnClickListener { v ->
                        if (v.id == R.id.cancel) {
                            dismiss()
                        } else if (v.id == R.id.create) {
                            if (id == ACTION_ADD_FILE || id == ACTION_ADD_FOLDER) {
                                owner.addNewFile(folderName.text.toString(),
                                        id == ACTION_ADD_FOLDER)
                            } else {
                                owner.renameFile(baseFile,
                                        folderName.text.toString())
                            }
                            dismiss()
                        }
                    }
                    if (id == MENU_RENAME) {
                        (view.findViewById<View>(R.id.create) as Button).setText(R.string.rename)
                    }
                    view.findViewById<View>(R.id.cancel).setOnClickListener(listener)
                    view.findViewById<View>(R.id.create).setOnClickListener(listener)
                    return builder.create()
                }
                MENU_ARCHIVE -> {
                    val v = View.inflate(owner.mContext, R.layout.archive, null)
                    val archiveType = v.findViewById<View>(R.id.archive_type) as Spinner
                    val archiveName = v.findViewById<View>(R.id.archive_name) as EditText
                    if (SelectedFiles.files.size == 1) {
                        builder.setTitle(SelectedFiles.files[0].name)
                    } else {
                        builder.setTitle("Create Archive.")
                    }
                    builder.setView(v)
                    builder.setPositiveButton("Create",
                            DialogInterface.OnClickListener { dialog, which ->
                                if (TextUtils.isEmpty(archiveName.text)) {
                                    Toast.makeText(owner.mContext,
                                            "Name cannot be empty.", Toast.LENGTH_SHORT).show()
                                    return@OnClickListener
                                }
                                val type = archiveType.selectedItem.toString()
                                val name = archiveName.text.toString()
                                try {
                                    when (type) {
                                        "zip" -> owner.filesChanged(BackgroundUtils(owner.mContext,
                                                name, BackgroundUtils.ZIP_FILE).execute().get())
                                        "tar" -> owner.filesChanged(BackgroundUtils(owner.mContext,
                                                name, BackgroundUtils.TAR_FILE).execute().get())
                                        "tar.gz" -> owner.filesChanged(
                                                BackgroundUtils(owner.mContext,
                                                        name,
                                                        BackgroundUtils.TAR_COMPRESS).execute().get())
                                    }
                                } catch (e: Exception) {
                                    // Ignore
                                }
                            })
                    builder.setNegativeButton("Cancel", null)
                    return builder.create()
                }
            }
            return null
        }

        companion object {

            fun newInstance(id: Int): MyAlertDialogFragment {
                val frag = MyAlertDialogFragment()
                val args = Bundle()
                args.putInt("id", id)
                frag.arguments = args
                return frag
            }
        }
    }

    protected inner class ViewAdapter : RecyclerView.Adapter<BrowserViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowserViewHolder {
            val inflater = mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val v = inflater.inflate(R.layout.item, parent, false)
            return BrowserViewHolder(v)
        }

        override fun onBindViewHolder(holder: BrowserViewHolder, position: Int) {
            holder.title.text = mFiles[position].name
            getIconForFile(holder.icon, position)

            val df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                    DateFormat.SHORT, Locale.getDefault())
            val file = mFiles[position]
            if (file.isFile) {
                holder.info.text = Utils.displaySize(file.length())
            } else {
                var num = 0
                val files = file.list()
                if (files != null) {
                    num = files.size
                }
                holder.info.text = num.toString()
            }
            if (mSearching) {
                holder.date.text = mFiles[position].getPath()
            } else {
                holder.date.text = df.format(file.lastModified())
            }
        }

        override fun getItemCount(): Int {
            return mFiles.size
        }
    }

    inner class BrowserViewHolder internal constructor(v: View) :
            MultiChoiceViewHolder(v, mMultiSelector), View.OnClickListener,
            View.OnLongClickListener {

        var title: TextView
        val date: TextView
        val info: TextView
        var icon: ImageView
        internal var rippleLayout: MaterialRippleLayout

        init {

            title = v.findViewById<View>(R.id.title) as TextView
            date = v.findViewById<View>(R.id.date) as TextView
            info = v.findViewById<View>(R.id.info) as TextView
            icon = v.findViewById<View>(R.id.image) as ImageView
            rippleLayout = v.findViewById<View>(R.id.ripple_layout) as MaterialRippleLayout

            v.setOnClickListener(this)
            v.setOnLongClickListener(this)
            v.isLongClickable = true

            rippleLayout.setRippleColor(ThemeActivity.getAccentColor(mContext))
        }

        override fun onClick(view: View) {
            val b = mMultiSelector.tapSelection(this)
            if (mMultiSelector.selectedPositions.size == 0) {
                mMultiSelector.setSelectable(false)
                if (mActionMode != null) mActionMode!!.finish()
            }
            if (!b) {
                onClickFile(mFiles[adapterPosition].realPath)
            }
            if (mActionMode != null && !b) {
                mActionMode!!.finish()
            }
        }

        override fun onLongClick(view: View): Boolean {
            if (!mSupportsActionMode) return false
            //mActivity.getToolbar().startActionMode(mMultiSelect);
            mActivity!!.startSupportActionMode(mMultiSelect)
            mMultiSelector.setSelectable(true)
            mMultiSelector.setSelected(this, true)
            return true
        }
    }

    companion object {

        val ACTION_ADD_FOLDER = 10001
        val ACTION_ADD_FILE = 10002
        val MENU_COPY = 1001
        val MENU_CUT = 1002
        val MENU_DELETE = 1003
        val MENU_PERMISSIONS = 1004
        val MENU_RENAME = 1005
        val MENU_SHARE = 1006
        val MENU_ARCHIVE = 1007
        val ARG_PATH = "path"
    }
}
