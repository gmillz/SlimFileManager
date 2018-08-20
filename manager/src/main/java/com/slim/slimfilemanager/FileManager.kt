package com.slim.slimfilemanager

import android.Manifest
import android.app.Activity
import android.app.Fragment
import android.app.FragmentManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v13.app.FragmentStatePagerAdapter
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.getbase.floatingactionbutton.FloatingActionButton
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.slim.slimfilemanager.fragment.BaseBrowserFragment
import com.slim.slimfilemanager.fragment.BrowserFragment
import com.slim.slimfilemanager.settings.SettingsActivity
import com.slim.slimfilemanager.settings.SettingsProvider
import com.slim.slimfilemanager.utils.Bookmark
import com.slim.slimfilemanager.utils.BookmarkHelper
import com.slim.slimfilemanager.utils.FragmentLifecycle
import com.slim.slimfilemanager.utils.IconCache
import com.slim.slimfilemanager.utils.PasteTask
import com.slim.slimfilemanager.utils.RootUtils
import com.slim.slimfilemanager.utils.Utils
import com.slim.slimfilemanager.widget.PageIndicator
import com.slim.slimfilemanager.widget.TabItem
import com.slim.slimfilemanager.widget.TabPageIndicator
import com.slim.util.SDCardUtils
import kotlinx.android.synthetic.main.file_manager.*

import java.io.File
import java.util.ArrayList

class FileManager : ThemeActivity(), View.OnClickListener,
                    NavigationView.OnNavigationItemSelectedListener {
    internal lateinit var mView: View
    internal lateinit var mToolbar: Toolbar
    internal var mDrawerLayout: DrawerLayout? = null
    internal lateinit var mNavView: NavigationView
    internal lateinit var mPasteButton: FloatingActionButton
    internal var mActionMenu: FloatingActionsMenu? = null
    internal var mCurrentPosition: Int = 0
    internal var mMove: Boolean = false
    internal var mPicking: Boolean = false
    private val mBookmarks = ArrayList<Bookmark>()
    private var mBookmarkHelper: BookmarkHelper? = null
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    internal var mPreferenceListener: SharedPreferences.OnSharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                for (tabItem in mSectionsPagerAdapter!!.items) {
                    tabItem.fragment!!.onPreferencesChanged()
                }
                if (key == SettingsProvider.SMALL_INDICATOR) {
                    setupPageIndicators()
                }
            }
    private var mFragment: BaseBrowserFragment? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private val mCallbacks = ArrayList<ActivityCallback>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBookmarkHelper = BookmarkHelper(this)

        SettingsProvider.get(this)
                .registerOnSharedPreferenceChangeListener(mPreferenceListener)

        val intent = intent

        checkPermissions()

        if (intent.action == Intent.ACTION_GET_CONTENT) {
            setContentView(R.layout.file_picker)
            mView = findViewById(R.id.base)
            showFragment(intent.type)
            mPicking = true
        } else {
            setContentView(R.layout.file_manager)
            mView = findViewById(R.id.base)
            setupNavigationDrawer()
            setupTabs()
            setupActionButtons()
        }
        setupToolbar()

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.setTitle(R.string.file_manager)
        }

        if (savedInstanceState == null) {
            if (mDrawerLayout != null && mDrawerToggle != null) {
                mDrawerLayout!!.openDrawer(GravityCompat.START)
                mDrawerToggle!!.syncState()
            }
        }
    }

    fun addActivityCallback(callback: ActivityCallback) {
        mCallbacks.add(callback)
    }

    fun removeActivityCallback(callback: ActivityCallback) {
        if (mCallbacks.contains(callback)) {
            mCallbacks.remove(callback)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (callback in mCallbacks) {
            callback.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun checkPermissions() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        var granted = true
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(this,
                            perm) != PackageManager.PERMISSION_GRANTED) {
                granted = false
            }
        }
        if (granted) {
            return
        }

        ActivityCompat.requestPermissions(this, permissions, 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == 1) {
            var denied = false
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    denied = true
                }
            }
            if (denied) {
                Snackbar.make(mView, "Unable to continue, Permissions Denied",
                        Snackbar.LENGTH_SHORT).show()
            } else {
                for (tab in mSectionsPagerAdapter!!.items) {
                    tab.fragment!!.filesChanged(tab.path)
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        var fragmentId = TabItem.TAB_BROWSER
        var path: String? = null
        if (id == ROOT_ID) {
            path = "/"

        } else if (id == SDCARD_ID) {
            path = Environment.getExternalStorageDirectory().absolutePath
        } else if (id == EXTERNALSD_ID) {
            path = SDCardUtils.instance()!!.directory
        } else {
            for (bookmark in mBookmarks) {
                if (bookmark.menuId == id) {
                    path = bookmark.path
                    fragmentId = bookmark.fragmentId
                    break
                }
            }
        }

        if (!TextUtils.isEmpty(path)) {
            if (mSectionsPagerAdapter!!.containsTabId(fragmentId)) {
                mSectionsPagerAdapter!!.moveToTabId(fragmentId, path)
            } else {
                mSectionsPagerAdapter!!.addTabId(fragmentId, path!!)
            }
        }
        item.isEnabled = true
        mDrawerLayout!!.closeDrawers()
        return true
    }

    public override fun onStart() {
        super.onStart()

        if (mSectionsPagerAdapter != null && pager != null) {
            setCurrentlyDisplayedFragment(mSectionsPagerAdapter!!.getItem(
                    pager.currentItem) as BaseBrowserFragment)
        }
    }

    private fun showFragment(type: String) {
        val fragment = BrowserFragment()
        findViewById<View>(R.id.content).visibility = View.VISIBLE
        fragmentManager.beginTransaction().add(R.id.content, fragment).commit()
        setCurrentlyDisplayedFragment(fragment)
        fragment.setPicking()
        fragment.setMimeType(type)
    }

    private fun setupToolbar() {
        mToolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(mToolbar)
        mToolbar.setTitle(R.string.file_manager)
    }

    private fun setupNavigationDrawer() {
        mDrawerLayout = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        mNavView = findViewById<View>(R.id.nav_view) as NavigationView

        mDrawerToggle = ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close)

        mDrawerLayout!!.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (java.lang.Float.toString(slideOffset).contains("0.1")) {
                    /*mDrawerAdapter.notifyDataSetChanged();
                    mDrawerAdapter.notifyDataSetInvalidated();*/
                    mDrawerLayout!!.invalidate()
                }
                mDrawerToggle!!.onDrawerSlide(drawerView, slideOffset)
            }

            override fun onDrawerOpened(drawerView: View) {
                mDrawerToggle!!.onDrawerOpened(drawerView)
            }

            override fun onDrawerClosed(drawerView: View) {
                mDrawerToggle!!.onDrawerClosed(drawerView)
            }

            override fun onDrawerStateChanged(newState: Int) {
                mDrawerToggle!!.onDrawerStateChanged(newState)
            }
        })
        mDrawerToggle!!.syncState()

        recreateDrawerItems()

        mNavView.setNavigationItemSelectedListener(this)

        val header = mNavView.getHeaderView(0)
        header?.findViewById<View>(R.id.header_layout)?.setBackgroundColor(mPrimaryColor)
    }

    private fun recreateDrawerItems() {
        mNavView.menu.clear()

        addBaseItems()

        val bookmarks = mBookmarkHelper!!.bookmarks

        val bookmarkGroup = mNavView.menu.addSubMenu("Bookmarks")

        if (bookmarks.size == 0) {
            addDefaultBookmarks()
        }
        for (bookmark in bookmarks) {
            mBookmarks.add(bookmark)
            val view = ImageView(this)
            view.setImageResource(R.drawable.close)
            bookmarkGroup.add(0, bookmark.menuId, 0, bookmark.name).actionView = view
        }

        //SubMenu cloud = mNavView.getMenu().addSubMenu("Cloud Storage");
        //cloud.add(0, DROPBOX_ID, 0, "Dropbox");
        //cloud.add(0, DRIVE_ID, 0, "Drive");
    }

    private fun addDefaultBookmarks() {
        var bookmark = Bookmark()
        bookmark.name = getString(R.string.downloads_title)
        bookmark.path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).absolutePath
        bookmark.fragmentId = TabItem.TAB_BROWSER
        bookmark.menuId = View.generateViewId()
        mBookmarkHelper!!.addBookmark(bookmark)
        bookmark = Bookmark()
        bookmark.name = getString(R.string.dcim_title)
        bookmark.path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).absolutePath
        bookmark.fragmentId = TabItem.TAB_BROWSER
        bookmark.menuId = View.generateViewId()
        mBookmarkHelper!!.addBookmark(bookmark)
        bookmark = Bookmark()
        bookmark.name = "Documents"
        bookmark.path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS).absolutePath
        bookmark.fragmentId = TabItem.TAB_BROWSER
        bookmark.menuId = View.generateViewId()
        mBookmarkHelper!!.addBookmark(bookmark)
    }

    private fun addBaseItems() {
        mNavView.menu.add(0, ROOT_ID, 0, R.string.root_title)
                .isVisible = SettingsProvider.getBoolean(this,
                SettingsProvider.KEY_ENABLE_ROOT, false) && RootUtils.isRootAvailable
        mNavView.menu.add(0, SDCARD_ID, 0, R.string.sdcard_title)

        getExternalSDCard()
    }

    private fun addBookmark() {
        val file = File(mFragment!!.currentPath)
        val bookmark = Bookmark()
        bookmark.name = file.name
        bookmark.path = file.absolutePath
        bookmark.fragmentId = mSectionsPagerAdapter!!.currentTabId
        bookmark.menuId = View.generateViewId()
        mBookmarkHelper!!.addBookmark(bookmark)
        recreateDrawerItems()
    }

    private fun setupTabs() {

        mSectionsPagerAdapter = SectionsPagerAdapter(fragmentManager)
        pager.adapter = mSectionsPagerAdapter

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrolled(position: Int,
                                        positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                val fragmentToShow = mSectionsPagerAdapter!!.getItem(position) as FragmentLifecycle?
                fragmentToShow!!.onResumeFragment()

                if (mActionMenu != null) {
                    mActionMenu!!.collapse()
                }

                val fragmentToHide =
                        mSectionsPagerAdapter!!.getItem(mCurrentPosition) as FragmentLifecycle?
                fragmentToHide?.onPauseFragment()

                mCurrentPosition = position

                saveTabs()
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        setupPageIndicators()

        pager.currentItem = SettingsProvider.getInt(this, "current_tab", 0)
    }

    private fun setupPageIndicators() {
        indicator.setViewPager(pager)
        tab_indicator.setViewPager(pager)

        indicator.setSelectedColor(mPrimaryColorDark)
        indicator.setUnselectedColor(mPrimaryColor)
        tab_indicator.setSelectedColor(mPrimaryColorDark)
        tab_indicator.setUnselectedColor(mPrimaryColor)

        if (SettingsProvider.getBoolean(this, SettingsProvider.SMALL_INDICATOR, false)) {
            tab_indicator.visibility = View.GONE
            indicator.visibility = View.VISIBLE
        } else {
            indicator.visibility = View.GONE
            tab_indicator.visibility = View.VISIBLE
        }
    }

    fun setTabTitle(fragment: BaseBrowserFragment, path: String) {
        if (mPicking) return
        runOnUiThread {
            val title = fragment.getTabTitle(path)
            for (item in mSectionsPagerAdapter!!.items) {
                if (item.fragment === fragment) {
                    tab_indicator.setTabTitle(
                            title, mSectionsPagerAdapter!!.items.indexOf(item))
                    break
                }
            }
        }
    }

    private fun setupActionButtons() {
        mActionMenu = findViewById<View>(R.id.float_button) as FloatingActionsMenu
        mPasteButton = findViewById<View>(R.id.paste) as FloatingActionButton

        buildActionButtons()

        mActionMenu!!.setColorNormal(ThemeActivity.getAccentColor(this))
        mActionMenu!!.setColorPressed(Utils.darkenColor(ThemeActivity.getAccentColor(this)))

        mPasteButton.colorNormal = ThemeActivity.getAccentColor(this)
        mPasteButton.colorPressed = Utils.darkenColor(ThemeActivity.getAccentColor(this))
        mPasteButton.setOnClickListener {
            PasteTask(this@FileManager, mMove, mFragment!!.currentPath,
                    mFragment!!.pasteCallback)
            for (item in mSectionsPagerAdapter!!.items) {
                item.fragment!!.filesChanged(item.fragment!!.currentPath)
            }
            //mFragment.filesChanged(mFragment.getCurrentPath());
            setMove(false)
            showPaste(false)
        }
        mPasteButton.setOnLongClickListener {
            PasteTask.SelectedFiles.clearAll()
            setMove(false)
            showPaste(false)
            true
        }
    }

    private fun buildActionButtons() {
        mActionMenu!!.addButton(getButton(R.drawable.add_folder,
                R.string.create_folder, BaseBrowserFragment.ACTION_ADD_FOLDER))
        mActionMenu!!.addButton(getButton(R.drawable.add_file,
                R.string.create_file, BaseBrowserFragment.ACTION_ADD_FILE))
    }

    private fun getButton(icon: Int, title: Int, tag: Int): FloatingActionButton {
        val button = FloatingActionButton(this)
        button.colorNormal = ThemeActivity.getAccentColor(this)
        button.colorPressed = Utils.darkenColor(ThemeActivity.getAccentColor(this))
        button.setIcon(icon)
        button.title = getString(title)
        button.tag = tag
        button.setOnClickListener(this)
        return button
    }

    override fun onClick(v: View) {
        if (v.tag == BaseBrowserFragment.ACTION_ADD_FILE) {
            mActionMenu!!.collapse()
            val fragment = mSectionsPagerAdapter!!.getItem(mCurrentPosition) as BaseBrowserFragment?
            fragment!!.showDialog(BaseBrowserFragment.ACTION_ADD_FILE)
        } else if (v.tag == BaseBrowserFragment.ACTION_ADD_FOLDER) {
            mActionMenu!!.collapse()
            mFragment!!.showDialog(BaseBrowserFragment.ACTION_ADD_FOLDER)
        }
    }

    fun setMove(move: Boolean) {
        mMove = move
    }

    fun showPaste(show: Boolean) {
        if (show) {
            mPasteButton.visibility = View.VISIBLE
            mActionMenu!!.visibility = View.GONE
        } else {
            mActionMenu!!.visibility = View.VISIBLE
            mPasteButton.visibility = View.GONE
        }
    }

    fun getExternalSDCard() {
        SDCardUtils.initialize(this)
        val extSD = SDCardUtils.instance()!!.directory
        if (!TextUtils.isEmpty(extSD)) {
            mNavView.menu.add(0, EXTERNALSD_ID, 0, "External SD")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        if (mPicking) return true
        menuInflater.inflate(R.menu.menu_file_manager, menu)
        if (mSectionsPagerAdapter!!.count == 0) {
            for (i in 0 until menu.size()) {
                val item = menu.getItem(i)
                if (item.itemId == R.id.close_tab) {
                    item.isVisible = false
                }
            }
        }
        return true
    }

    fun setCurrentlyDisplayedFragment(fragment: BaseBrowserFragment) {
        mFragment = fragment
    }

    fun closeDrawers() {
        mDrawerLayout!!.closeDrawers()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (mPicking && id == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return true
        }

        if (mDrawerToggle != null && mDrawerToggle!!.onOptionsItemSelected(item)) {
            return true
        }

        when (id) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            R.id.add_tab -> {
                mSectionsPagerAdapter!!.addTab(Environment.getExternalStorageDirectory().path)
                return true
            }
            R.id.close_tab -> {
                mSectionsPagerAdapter!!.removeCurrentTab()
                invalidateOptionsMenu()
                return true
            }
            R.id.add_bookmark -> {
                addBookmark()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onTrimMemory(level: Int) {
        if (level >= Activity.TRIM_MEMORY_MODERATE) {
            IconCache.clearCache()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        SettingsProvider.get(this)
                .unregisterOnSharedPreferenceChangeListener(mPreferenceListener)
    }

    private fun saveTabs() {
        if (mSectionsPagerAdapter == null) return
        val arrayList = ArrayList<String>()
        for (item in mSectionsPagerAdapter!!.items) {
            val path = item.fragment!!.currentPath
            if (!TextUtils.isEmpty(path)) {
                arrayList.add(item.fragment!!.currentPath)
            }
        }
        if (!arrayList.isEmpty()) {
            SettingsProvider.putTabList(this, "tabs", mSectionsPagerAdapter!!.items)
        }
        SettingsProvider.putInt(this, "current_tab", pager.currentItem)
    }

    interface ActivityCallback {
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    }

    private inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) :
            FragmentStatePagerAdapter(fm) {

        internal var items = ArrayList<TabItem>()

        internal val currentTabId: Int
            get() = items[pager.currentItem].id

        init {
            addDefault()
            items = SettingsProvider.getTabList(this@FileManager, "tabs", items)
        }

        internal fun addTab(path: String) {
            val tab = TabItem(path, TabItem.TAB_BROWSER)
            items.add(tab)
            notifyDataSetChanged()
            this@FileManager.tab_indicator.notifyDataSetChanged()
            pager.currentItem = count
            if (items.size == 1) {
                setCurrentlyDisplayedFragment(items[0].fragment!!)
            }
        }

        internal fun containsTabId(id: Int): Boolean {
            for (tab in items) {
                if (tab.id == id) {
                    return true
                }
            }
            return false
        }

        @JvmOverloads internal fun moveToTabId(id: Int, path: String? = null) {
            for (tab in items) {
                if (tab.id == id) {
                    pager.setCurrentItem(items.indexOf(tab), true)
                    if (path != null) {
                        tab.fragment!!.filesChanged(path)
                    }
                    return
                }
            }
        }

        @JvmOverloads internal fun addTabId(id: Int, path: String = "/") {
            val tab = TabItem(path, id)
            items.add(tab)
            notifyDataSetChanged()
            pager.setCurrentItem(items.indexOf(tab), true)
            this@FileManager.tab_indicator.notifyDataSetChanged()
        }

        internal fun removeCurrentTab() {
            val id = mCurrentPosition
            pager.setCurrentItem(id - 1, true)
            items[id].fragment!!.onDestroyView()
            items.remove(items[id])
            notifyDataSetChanged()
            this@FileManager.tab_indicator.notifyDataSetChanged()
        }

        internal fun addDefault() {
            items.add(TabItem(Environment.getExternalStorageDirectory().absolutePath,
                    TabItem.TAB_BROWSER))
            if (RootUtils.isRootAvailable && SettingsProvider.getBoolean(this@FileManager,
                            SettingsProvider.KEY_ENABLE_ROOT, false)) {
                items.add(TabItem("/", TabItem.TAB_BROWSER))
            } else {
                items.add(TabItem(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS).path, TabItem.TAB_BROWSER))
            }
        }

        override fun getItem(position: Int): Fragment? {
            return if (items.size == 0) null else items[position].fragment
        }

        override fun getCount(): Int {
            return items.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return if (!TextUtils.isEmpty(items[position].path)) {
                items[position].fragment!!.getTabTitle(
                        items[position].path)
            } else ""
        }

        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            if (`object` is BaseBrowserFragment) {
                setCurrentlyDisplayedFragment(`object`)
            }
            super.setPrimaryItem(container!!, position, `object`)
        }
    }

    companion object {

        private val EXTERNALSD_ID = 0x003
        private val USB_OTG_ID = 0x004
        private val ROOT_ID = 0x005
        private val SDCARD_ID = 0x006
    }
}