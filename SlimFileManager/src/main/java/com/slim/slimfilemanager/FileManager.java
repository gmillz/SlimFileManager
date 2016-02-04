package com.slim.slimfilemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.appeaser.sublimenavigationviewlibrary.OnNavigationMenuEventListener;
import com.appeaser.sublimenavigationviewlibrary.SublimeBaseMenuItem;
import com.appeaser.sublimenavigationviewlibrary.SublimeGroup;
import com.appeaser.sublimenavigationviewlibrary.SublimeNavigationView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.slim.slimfilemanager.fragment.BaseBrowserFragment;
import com.slim.slimfilemanager.fragment.BrowserFragment;
import com.slim.slimfilemanager.settings.SettingsActivity;
import com.slim.slimfilemanager.settings.SettingsProvider;
import com.slim.slimfilemanager.utils.FragmentLifecycle;
import com.slim.slimfilemanager.utils.IconCache;
import com.slim.slimfilemanager.utils.PasteTask;
import com.slim.slimfilemanager.utils.Utils;
import com.slim.slimfilemanager.widget.PageIndicator;
import com.slim.slimfilemanager.widget.TabItem;
import com.slim.slimfilemanager.widget.TabPageIndicator;

import butterknife.Bind;
import butterknife.ButterKnife;
import trikita.log.Log;

public class FileManager extends ThemeActivity implements View.OnClickListener,
        OnNavigationMenuEventListener {

    private static final int DRAWER_ROOT      = R.id.nav_root;
    private static final int DRAWER_SDCARD    = R.id.nav_sdcard;
    private static final int DRAWER_DOWNLOADS = R.id.nav_downloads;
    private static final int DRAWER_DCIM      = R.id.nav_dcim;

    private SublimeBaseMenuItem mDropboxItem;
    private SublimeBaseMenuItem mDriveItem;
    private SublimeBaseMenuItem mAddCloudOption;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private BaseBrowserFragment mFragment;

    @Bind(R.id.base) View mView;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.pager) ViewPager mViewPager;
    @Bind(R.id.indicator) PageIndicator mPageIndicator;
    @Bind(R.id.tab_indicator) TabPageIndicator mTabs;
    @Bind(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @Bind(R.id.nav_view) SublimeNavigationView mNavView;
    @Bind(R.id.paste) FloatingActionButton mPasteButton;
    @Bind(R.id.float_button) FloatingActionsMenu mActionMenu;
    private ActionBarDrawerToggle mDrawerToggle;

    int mCurrentPosition;
    boolean mMove;
    boolean mPicking;

    private List<ActivityCallback> mCallbacks = new ArrayList<>();

    public interface ActivityCallback {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsProvider.get(this)
                .registerOnSharedPreferenceChangeListener(mPreferenceListener);

        Intent intent = getIntent();

        setContentView(R.layout.file_manager);
        ButterKnife.bind(this);
        setupToolbar();

        checkPermissions();

        if (intent.getAction().equals(Intent.ACTION_GET_CONTENT)) {
            hideViews();
            showFragment(intent.getType());
            mPicking = true;
        } else {
            setupNavigationDrawer();
            setupTabs();
            setupActionButtons();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(R.string.file_manager);
        }

        if (savedInstanceState == null) {
            if (mDrawerLayout != null && mDrawerToggle != null) {
                mDrawerLayout.openDrawer(GravityCompat.START);
                mDrawerToggle.syncState();
            }
        }
    }

    public void addActivityCallback(ActivityCallback callback) {
        mCallbacks.add(callback);
    }

    @SuppressWarnings("unused")
    public void removeActivityCallback(ActivityCallback callback) {
        if (mCallbacks.contains(callback)) {
            mCallbacks.remove(callback);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (ActivityCallback callback : mCallbacks) {
            callback.onActivityResult(requestCode, resultCode, data);
        }
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public void checkPermissions() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        boolean granted = true;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                granted = false;
            }
        }
        if (granted) {
            return;
        }

        ActivityCompat.requestPermissions(this, permissions, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        if (requestCode == 1) {
            boolean denied = false;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    denied = true;
                }
            }
            if (denied) {
                Snackbar.make(mView, "Unable to continue, Permissions Denied",
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onNavigationMenuEvent(Event event, SublimeBaseMenuItem item) {
        if (event.equals(Event.CLICKED)) {
            int id = item.getItemId();
            String path = null;
            switch (id) {
                case DRAWER_ROOT:
                    path = "/";
                    break;
                case DRAWER_SDCARD:
                    path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    break;
                case DRAWER_DOWNLOADS:
                    path = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                    break;
                case DRAWER_DCIM:
                    path = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DCIM).getAbsolutePath();
                    break;
            }

            if (!TextUtils.isEmpty(path)) {
                if (mSectionsPagerAdapter.getCount() == 0) {
                    mSectionsPagerAdapter.addTab(path);
                } else {
                    mFragment.filesChanged(path);
                }
                return true;
            }
            if (item.equals(mDropboxItem)) {
                if (mSectionsPagerAdapter.containsTabId(TabItem.TAB_DROPBOX)) {
                    mSectionsPagerAdapter.moveToTabId(TabItem.TAB_DROPBOX);
                } else {
                    mSectionsPagerAdapter.addTabId(TabItem.TAB_DROPBOX);
                }
            } else if (item.equals(mDriveItem)) {
                if (mSectionsPagerAdapter.containsTabId(TabItem.TAB_DRIVE)) {
                    mSectionsPagerAdapter.moveToTabId(TabItem.TAB_DRIVE);
                } else {
                    mSectionsPagerAdapter.addTabId(TabItem.TAB_DRIVE);
                }
            }
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mSectionsPagerAdapter != null && mViewPager != null) {
            setCurrentlyDisplayedFragment((BaseBrowserFragment) mSectionsPagerAdapter.getItem(
                    mViewPager.getCurrentItem()));
        }
    }

    private void showFragment(String type) {
        BaseBrowserFragment fragment = new BrowserFragment();
        getFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
        setCurrentlyDisplayedFragment(fragment);
        fragment.setPicking();
        fragment.setMimeType(type);
    }

    private void hideViews() {
        mNavView.setVisibility(View.GONE);
        mDrawerLayout.setVisibility(View.GONE);
        mViewPager.setVisibility(View.GONE);
        mPageIndicator.setVisibility(View.GONE);
        mTabs.setVisibility(View.GONE);
        mActionMenu.setVisibility(View.GONE);
        mPasteButton.setVisibility(View.GONE);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(R.string.file_manager);
    }

    private void setupNavigationDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close);

        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (Float.toString(slideOffset).contains("0.1")) {
                    /*mDrawerAdapter.notifyDataSetChanged();
                    mDrawerAdapter.notifyDataSetInvalidated();*/
                    mDrawerLayout.invalidate();
                }
                mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                mDrawerToggle.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                mDrawerToggle.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                mDrawerToggle.onDrawerStateChanged(newState);
            }
        });
        mDrawerToggle.syncState();
        /*mDrawerAdapter = new DrawerAdapter(this);
        mDrawer.setAdapter(mDrawerAdapter);
        mDrawerAdapter.addItem(getString(R.string.root_title), "/");
        mDrawerAdapter.addItem(getString(R.string.sdcard_title),
                Environment.getExternalStorageDirectory().getPath());
        mDrawerAdapter.addItem(getString(R.string.downloads_title),
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS).getPath());
        mDrawerAdapter.addItem(getString(R.string.dcim_title),
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM).getPath());
        getExternalSDCard();
        mDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mSectionsPagerAdapter.getCount() == 0) {
                    mSectionsPagerAdapter.addTab(mDrawerAdapter.getPath(position));
                } else {
                    mFragment.filesChanged(mDrawerAdapter.getPath(position));
                }
                mDrawerLayout.closeDrawers();
            }
        });*/

        addNavDrawerItems();
        //mNavView.setNavigationItemSelectedListener(this);

        mNavView.setNavigationMenuEventListener(this);
    }

    private void addNavDrawerItems() {
        SublimeGroup group = mNavView.getMenu().addGroup(true, true, true, true,
                SublimeGroup.CheckableBehavior.SINGLE);
        mNavView.getMenu().addGroupHeaderItem(group.getGroupId(), "Cloud Storage", "", false);
        mDropboxItem = mNavView.getMenu().addTextItem(group.getGroupId(), "Dropbox", "", true);
        mDriveItem = mNavView.getMenu().addTextItem(group.getGroupId(), "Drive", "", true);
        mAddCloudOption = mNavView.getMenu().addTextItem(
                group.getGroupId(), "Add Cloud Storage", "", true);
        mAddCloudOption.setIcon(R.drawable.add);

        mNavView.getMenu().finalizeUpdates();
    }

    private void setupTabs() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position,
                                       float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                FragmentLifecycle fragmentToShow = (FragmentLifecycle)
                        mSectionsPagerAdapter.getItem(position);
                fragmentToShow.onResumeFragment();

                if (mActionMenu != null) {
                    mActionMenu.collapse();
                }

                FragmentLifecycle fragmentToHide = (FragmentLifecycle)
                        mSectionsPagerAdapter.getItem(mCurrentPosition);
                if (fragmentToHide != null) fragmentToHide.onPauseFragment();

                mCurrentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        setupPageIndicators();

        mViewPager.setCurrentItem(SettingsProvider.getInt(this, "current_tab", 0));
    }

    private void setupPageIndicators() {
        mPageIndicator.setViewPager(mViewPager);
        mTabs.setViewPager(mViewPager);

        if (SettingsProvider.getBoolean(this, SettingsProvider.SMALL_INDICATOR, false)) {
            mTabs.setVisibility(View.GONE);
            mPageIndicator.setVisibility(View.VISIBLE);
        } else {
            mPageIndicator.setVisibility(View.GONE);
            mTabs.setVisibility(View.VISIBLE);
        }
    }

    public void setTabTitle(final BaseBrowserFragment fragment, final String path) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String title = fragment.getTabTitle(path);
                if (fragment.getUserVisibleHint()) {
                    mTabs.setTabTitle(title, mCurrentPosition);
                } else {
                    for (TabItem item : mSectionsPagerAdapter.getItems()) {
                        if (item.fragment == fragment) {
                            mTabs.setTabTitle(
                                    title, mSectionsPagerAdapter.getItems().indexOf(item));
                            break;
                        }
                    }
                }
            }
        });
    }

    private void setupActionButtons() {
        buildActionButtons();

        mActionMenu.setColorNormal(getAccentColor(this));
        mActionMenu.setColorPressed(Utils.darkenColor(getAccentColor(this)));

        mPasteButton.setColorNormal(getAccentColor(this));
        mPasteButton.setColorPressed(Utils.darkenColor(getAccentColor(this)));
        mPasteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PasteTask(FileManager.this, mMove, mFragment.getCurrentPath(),
                        mFragment.getPasteCallback());
                for (TabItem item : mSectionsPagerAdapter.getItems()) {
                    item.fragment.filesChanged(item.fragment.getCurrentPath());
                }
                //mFragment.filesChanged(mFragment.getCurrentPath());
                setMove(false);
                showPaste(false);
            }
        });
        mPasteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PasteTask.SelectedFiles.clearAll();
                setMove(false);
                showPaste(false);
                return true;
            }
        });
    }

    private void buildActionButtons() {
        mActionMenu.addButton(getButton(R.drawable.add_folder,
                R.string.create_folder, BaseBrowserFragment.ACTION_ADD_FOLDER));
        mActionMenu.addButton(getButton(R.drawable.add_file,
                R.string.create_file, BaseBrowserFragment.ACTION_ADD_FILE));
    }

    private FloatingActionButton getButton(int icon, int title, int tag) {
        FloatingActionButton button = new FloatingActionButton(this);
        button.setColorNormalResId(R.color.accent);
        button.setColorPressedResId(R.color.accent_dark);
        button.setIcon(icon);
        button.setTitle(getString(title));
        button.setTag(tag);
        button.setOnClickListener(this);
        return button;
    }

    public void onClick(View v) {
        if (v.getTag().equals(BaseBrowserFragment.ACTION_ADD_FILE)) {
            mActionMenu.collapse();
            mFragment.showDialog(BaseBrowserFragment.ACTION_ADD_FILE);
        } else if (v.getTag().equals(BaseBrowserFragment.ACTION_ADD_FOLDER)) {
            mActionMenu.collapse();
            mFragment.showDialog(BaseBrowserFragment.ACTION_ADD_FOLDER);
        }
    }

    public void setMove(boolean move) {
        mMove = move;
    }

    public void showPaste(boolean show) {
        if (show) {
            mPasteButton.setVisibility(View.VISIBLE);
            mActionMenu.setVisibility(View.GONE);
        } else {
            mActionMenu.setVisibility(View.VISIBLE);
            mPasteButton.setVisibility(View.GONE);
        }
    }

    @SuppressWarnings("unused")
    // TODO: update this for new nav drawer
    public void getExternalSDCard() {
        String secondaryStorage = System.getenv("SECONDARY_STORAGE");
        Log.d(secondaryStorage);
        Set<String> sec = new HashSet<>();
        if (!TextUtils.isEmpty(secondaryStorage)) {
            String[] secs = secondaryStorage.split(File.pathSeparator);
            Collections.addAll(sec, secs);
            if (!sec.isEmpty()) {
                for (String stor : sec) {
                    if (stor.toLowerCase().contains("usb")) {
                        File f = new File(stor);
                        if (f.exists() && f.isDirectory()) {
                            //mDrawerAdapter.addItem("USB OTG", stor);
                        }
                    } else if (stor.toLowerCase().contains("sdcard1")) {
                        File f = new File(stor);
                        if (f.exists() && f.isDirectory()) {
                            //mDrawerAdapter.addItem("External SD", stor);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (mPicking) return true;
        getMenuInflater().inflate(R.menu.menu_file_manager, menu);
        return true;
    }

    public void setCurrentlyDisplayedFragment(final BaseBrowserFragment fragment) {
        mFragment = fragment;
    }

    public void closeDrawers() {
        mDrawerLayout.closeDrawers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (mPicking && id == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        }

        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.add_tab:
                mSectionsPagerAdapter.addTab(Environment.getExternalStorageDirectory().getPath());
                return true;
            case R.id.close_tab:
                mSectionsPagerAdapter.removeCurrentTab();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        ArrayList<TabItem> mItems = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            addDefault();
            mItems = SettingsProvider.getTabList(FileManager.this, "tabs", mItems);
        }

        public void addTab(String path) {
            mItems.add(new TabItem(path, TabItem.TAB_BROWSER));
            notifyDataSetChanged();
            FileManager.this.mTabs.notifyDataSetChanged();
            mViewPager.setCurrentItem(getCount());
            //TODO setTabTitle(mItems.get(mItems.size() - 1).fragment);
            if (mItems.size() == 1) {
                setCurrentlyDisplayedFragment(mItems.get(0).fragment);
            }
        }

        public boolean containsTabId(int id) {
            for (TabItem tab : mItems) {
                if (tab.id == id) {
                    return true;
                }
            }
            return false;
        }

        public void moveToTabId(int id) {
            for (TabItem tab : mItems) {
                if (tab.id == id) {
                    mViewPager.setCurrentItem(mItems.indexOf(tab), true);
                }
            }
        }

        public void addTabId(int id) {
            TabItem tab = new TabItem("/", id);
            mItems.add(tab);
            notifyDataSetChanged();
            mViewPager.setCurrentItem(mItems.indexOf(tab), true);
            FileManager.this.mTabs.notifyDataSetChanged();
        }

        public void removeCurrentTab() {
            int id = mCurrentPosition;
            mViewPager.setCurrentItem(id - 1, true);
            mItems.get(id).fragment.onDestroyView();
            mItems.remove(mItems.get(id));
            notifyDataSetChanged();
            FileManager.this.mTabs.notifyDataSetChanged();
        }

        public void addDefault() {
            mItems.add(new TabItem(Environment.getExternalStorageDirectory().getAbsolutePath(),
                    TabItem.TAB_BROWSER));
            mItems.add(new TabItem("/", TabItem.TAB_BROWSER));
        }

        @Override
        public Fragment getItem(int position) {
            if (mItems.size() == 0) return null;
            return mItems.get(position).fragment;
        }

        public ArrayList<TabItem> getItems() {
            return mItems;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (mItems.get(position).id == TabItem.TAB_DROPBOX) {
                return "Dropbox";
            } else if (mItems.get(position).id == TabItem.TAB_DRIVE) {
                return "Google Drive";
            }
            if (!TextUtils.isEmpty(mItems.get(position).fragment.getCurrentPath())) {
                File file = new File(mItems.get(position).fragment.getCurrentPath());
                if (file.exists())
                    return file.getName();
            }
            return "";
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (object instanceof BaseBrowserFragment) {
                setCurrentlyDisplayedFragment((BaseBrowserFragment) object);
            }
            super.setPrimaryItem(container, position, object);
        }
    }

    @Override
    public void onTrimMemory(int level) {
        if (level >= Activity.TRIM_MEMORY_MODERATE) {
            IconCache.clearCache();
            com.slim.slimfilemanager.services.drive.IconCache.clearCache();
            com.slim.slimfilemanager.services.dropbox.IconCache.clearCache();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SettingsProvider.get(this)
                .unregisterOnSharedPreferenceChangeListener(mPreferenceListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSectionsPagerAdapter == null) return;
        ArrayList<String> arrayList = new ArrayList<>();
        for (TabItem item : mSectionsPagerAdapter.getItems()) {
            String path = item.fragment.getCurrentPath();
            if (!TextUtils.isEmpty(path)) {
                arrayList.add(item.fragment.getCurrentPath());
            }
        }
        if (!arrayList.isEmpty()) {
            SettingsProvider.putTabList(this, "tabs", mSectionsPagerAdapter.getItems());
        }
        SettingsProvider.putInt(this, "current_tab", mViewPager.getCurrentItem());
    }

    SharedPreferences.OnSharedPreferenceChangeListener mPreferenceListener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            for (TabItem tabItem : mSectionsPagerAdapter.getItems()) {
                tabItem.fragment.onPreferencesChanged();
            }
            if (key.equals(SettingsProvider.SMALL_INDICATOR)) {
                setupPageIndicators();
            }
        }
    };
}