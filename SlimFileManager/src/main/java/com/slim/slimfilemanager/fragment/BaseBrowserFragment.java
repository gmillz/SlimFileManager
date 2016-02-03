package com.slim.slimfilemanager.fragment;

import static butterknife.ButterKnife.findById;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.StringRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.slim.slimfilemanager.BuildConfig;
import com.slim.slimfilemanager.FileManager;
import com.slim.slimfilemanager.R;
import com.slim.slimfilemanager.ThemeActivity;
import com.slim.slimfilemanager.multichoice.MultiChoiceViewHolder;
import com.slim.slimfilemanager.multichoice.MultiSelector;
import com.slim.slimfilemanager.utils.BackgroundUtils;
import com.slim.slimfilemanager.utils.FileUtil;
import com.slim.slimfilemanager.utils.FragmentLifecycle;
import com.slim.slimfilemanager.utils.MimeUtils;
import com.slim.slimfilemanager.utils.PasteTask;
import com.slim.slimfilemanager.utils.PasteTask.SelectedFiles;
import com.slim.slimfilemanager.utils.PermissionsDialog;
import com.slim.slimfilemanager.utils.SortUtils;
import com.slim.slimfilemanager.utils.Utils;
import com.slim.slimfilemanager.utils.file.BaseFile;
import com.slim.slimfilemanager.utils.file.BasicFile;
import com.slim.slimfilemanager.widget.DividerItemDecoration;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class BaseBrowserFragment extends Fragment implements View.OnClickListener,
        FragmentLifecycle, SearchView.OnQueryTextListener {

    protected static final int MENU_COPY = 1001;
    protected static final int MENU_CUT = 1002;
    protected static final int MENU_DELETE = 1003;
    protected static final int MENU_PERMISSIONS = 1004;
    protected static final int MENU_RENAME = 1005;
    protected static final int MENU_SHARE = 1006;
    protected static final int MENU_ARCHIVE = 1007;

    public static final int ACTION_ADD_FOLDER = 10001;
    public static final int ACTION_ADD_FILE = 10002;

    protected List<Integer> ACTIONS = new ArrayList<>();

    protected static final String ARG_PATH = "path";

    protected String mCurrentPath;
    protected String mMimeType;

    protected Context mContext;
    protected FileManager mActivity;

    private MultiSelector mMultiSelector = new MultiSelector();

    protected boolean mSupportsActionMode = true;
    protected boolean mSupportsSearching = true;

    protected ActionMode mActionMode;
    @Bind(R.id.path) TextView mPath;
    private SearchView mSearchView;
    @Bind(R.id.swipe_refresh) SwipeRefreshLayout mRefreshLayout;
    @Bind(R.id.progress) ProgressBar mProgress;
    protected ProgressDialog mProgressDialog;
    @Bind(R.id.list) RecyclerView mRecyclerView;
    protected ViewAdapter mAdapter;
    protected final Executor mExecutor = Executors.newFixedThreadPool(2);
    protected ArrayList<BaseFile> mFiles = new ArrayList<>();

    protected boolean mExitOnBack = false;
    protected boolean mSearching = false;
    protected boolean mPicking = false;

    public abstract String getTabTitle(String path);
    public abstract void onClickFile(String path);

    @CallSuper
    public void filesChanged(String path) {
        mCurrentPath = path;
        mActivity.setTabTitle(this, path);
    }
    public abstract String getDefaultDirectory();
    public abstract BaseFile getFile(int i);
    public abstract void onDeleteFile(String file);
    public abstract void backPressed();
    public abstract String getRootFolder();
    public abstract PasteTask.Callback getPasteCallback();
    public abstract void getIconForFile(ImageView imageView, int position);
    public abstract void addFile(String path);
    public abstract void addNewFile(String name, boolean isFolder);
    public abstract void renameFile(BaseFile file, String name);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ACTIONS.add(MENU_COPY);
        ACTIONS.add(MENU_DELETE);
        ACTIONS.add(MENU_RENAME);
        ACTIONS.add(MENU_SHARE);

        mContext = getActivity();
        mActivity = (FileManager) getActivity();
        setHasOptionsMenu(true);
    }

    @Override
    public void onPauseFragment() {
        mExitOnBack = false;
        if (mSearchView != null && !mSearchView.isIconified()) {
            mSearchView.setIconified(true);
        }
    }

    @Override
    public void onResumeFragment() {
    }

    public void onPreferencesChanged() {
        sortFiles();
        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    public void setPicking() {
        mPicking = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getView() == null) return;
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return event.getAction() == KeyEvent.ACTION_UP
                        && keyCode == KeyEvent.KEYCODE_BACK && onBackPressed();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_browser, container, false);

        ButterKnife.bind(this, rootView);

        String defaultDir = null;
        Bundle extras = getArguments();

        if (extras != null) {
            defaultDir = extras.getString(ARG_PATH);
        }
        if (TextUtils.isEmpty(defaultDir)) {
            defaultDir = getDefaultDirectory();
        }
        mCurrentPath = defaultDir;

        mAdapter = new ViewAdapter();

        mProgress.setIndeterminate(true);
        mRefreshLayout.setColorSchemeColors(ThemeActivity.getAccentColor(getActivity()));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFiles();
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setHasFixedSize(true);
        DividerItemDecoration decor = new DividerItemDecoration(mContext, null);
        decor.setShowFirstDivider(false);
        decor.setShowLastDivider(false);
        mRecyclerView.addItemDecoration(decor);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setMax(100);

        return rootView;
    }

    public void refreshFiles() {
        onClickFile(mCurrentPath);
    }

    public void showProgressDialog(@StringRes final int title, final boolean indeterminate) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mProgressDialog.isShowing()) {
                    mProgressDialog.setTitle(title);
                    mProgressDialog.setMessage("Please wait...");
                    mProgressDialog.setIndeterminate(indeterminate);
                    mProgressDialog.setProgressStyle(indeterminate ?
                            ProgressDialog.STYLE_SPINNER : ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.show();
                }
            }
        });
    }

    public void hideProgressDialog() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.hide();
                }
            }
        });
    }

    protected void showProgress() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mRefreshLayout.setRefreshing(true);
                if (mProgress != null) {
                    mProgress.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    protected void hideProgress() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRefreshLayout != null) {
                    mRefreshLayout.setRefreshing(false);
                }
                if (mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
            }
        });
    }

    public void updateProgress(final int val) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.setProgress(val);
                }
            }
        });
    }


    public boolean onBackPressed() {
        if (mSearchView != null && !mSearchView.isIconified()) {
            mSearchView.setIconified(true);
            return true;
        }
        if (!mCurrentPath.equals(getRootFolder())) {
            backPressed();
            mExitOnBack = false;
        } else if (mCurrentPath.equals(getRootFolder())) {
            if (mExitOnBack) {
                mActivity.finish();
            } else {
                Toast.makeText(mContext, getString(R.string.back_confirm),
                        Toast.LENGTH_SHORT).show();
                mExitOnBack = true;
            }
        } else {
            mExitOnBack = false;
        }
        return true;
    }

    ActionMode.Callback mMultiSelect = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.add(0, MENU_COPY, 0, R.string.copy).setIcon(R.drawable.copy)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(0, MENU_CUT, 0, R.string.move).setIcon(R.drawable.cut)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(0, MENU_DELETE, 0, R.string.move).setIcon(R.drawable.delete)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(0, MENU_PERMISSIONS, 0, R.string.permissions)
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.add(0, MENU_RENAME, 0, R.string.rename)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.add(0, MENU_SHARE, 0, R.string.share).setIcon(R.drawable.share)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(0, MENU_ARCHIVE, 0, R.string.archive)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            mActionMode = mode;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            menu.findItem(MENU_PERMISSIONS).setVisible(
                    mMultiSelector.getSelectedPositions().size() == 1);
            menu.findItem(MENU_RENAME).setVisible(
                    mMultiSelector.getSelectedPositions().size() == 1);

            Log.d("TEST", "menu size=" + menu.size());
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                Log.d("TEST", "itemId=" + item.getItemId());
                item.setVisible(ACTIONS.contains(item.getItemId()));
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            SelectedFiles.clearAll();
            for (int i = 0; i < mFiles.size(); i++) {
                if (mMultiSelector.isSelected(i)) {
                    SelectedFiles.addFile(getFile(i));
                }
            }

            if (mMultiSelector.getSelectedPositions().size() > 0) {
                int id = item.getItemId();
                switch (id) {
                    case MENU_CUT:
                    case MENU_COPY:
                        if (id == MENU_CUT) mActivity.setMove(true);
                        mActivity.showPaste(true);
                        break;
                    case MENU_DELETE:
                        showDialog(MENU_DELETE);
                        mode.finish();
                        break;
                    case MENU_PERMISSIONS:
                        showDialog(MENU_PERMISSIONS);
                        break;
                    case MENU_RENAME:
                        showDialog(MENU_RENAME);
                        break;
                    case MENU_SHARE:
                        handleShareFile();
                        break;
                    case MENU_ARCHIVE:
                        showDialog(MENU_ARCHIVE);
                        break;
                }
            }
            mMultiSelector.clearSelections();
            mMultiSelector.setSelectable(false);
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mMultiSelector.clearSelections();
            mMultiSelector.setSelectable(false);
        }
    };

    public void setSearching(boolean searching) {
        mSearching = searching;
        //mSearchView.setIconified(searching);
    }

    public void setPathText(final String path) {
        if (mPath == null) return;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPath.setText(path);
            }
        });
    }

    @Override
    public void onClick(View v) {
        /*if (v == mPasteButton) {
            new PasteTask(mContext, mMove, mCurrentPath);
            filesChanged(mCurrentPath);
            mMove = false;
            //mActionMenu.setVisibility(View.VISIBLE);
            mPasteButton.setVisibility(View.GONE);
        } else*/ if (((int) v.getTag()) == ACTION_ADD_FOLDER) {
            showDialog(ACTION_ADD_FOLDER);
        }
    }

    public void handleShareFile() {
        ArrayList<Uri> uris = new ArrayList<>();
        for (BaseFile file : SelectedFiles.getFiles()) {
            if (file.exists()) {
                if (!file.isDirectory()) {
                    if (file instanceof BasicFile) {
                        uris.add(Uri.fromFile((File) file.getRealFile()));
                    }
                }
            }
        }
        SelectedFiles.clearAll();

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (uris.size() == 1) {
            intent.setAction(Intent.ACTION_SEND);
            intent.setType(MimeUtils.getMimeType(new File(uris.get(0).getPath()).getName()));
            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
        } else {
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.setType(MimeUtils.ALL_MIME_TYPES);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }
        if (mActionMode != null) mActionMode.finish();
        mActivity.startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        if (mPicking) return;
        menu.findItem(R.id.search).setVisible(mSupportsSearching);
        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        if (mSearchView != null) {
            mSearchView.setIconifiedByDefault(true);
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSearching(true);
                    mActivity.closeDrawers();
                    mFiles.clear();
                    mAdapter.notifyDataSetChanged();
                }
            });
            mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    mProgress.setVisibility(View.VISIBLE);
                    setSearching(false);
                    filesChanged(mCurrentPath);
                    return false;
                }
            });
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                setSearching(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (mSearchView != null && !mSearchView.isIconified()) {
            mFiles.clear();
            mAdapter.notifyDataSetChanged();
            if (!TextUtils.isEmpty(newText)) {
                mProgress.setVisibility(View.VISIBLE);
                searchForFile(mCurrentPath, newText);
                mProgress.setVisibility(View.GONE);
            }
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    public void setMimeType(String type) {
        mMimeType = type;
    }

    protected void filePicked(File file) {
        Uri data = Uri.fromFile(file);

        Intent intent = new Intent();
        intent.setData(data);

        Activity activity = getActivity();
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    protected void onClickArchive(final String file) {

        final File archive = new File(file);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(archive.getName());
        builder.setMessage("What would you like to do with this archive?");
        builder.setPositiveButton("Extract", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (FileUtil.getExtension(archive.getName()).equals("zip")) {
                        onClickFile(new BackgroundUtils(mContext, file,
                                BackgroundUtils.UNZIP_FILE).execute().get());
                    } else if (FileUtil.getExtension(archive.getName()).equals("tar")
                            || FileUtil.getExtension(archive.getName()).equals("gz")) {
                        onClickFile(new BackgroundUtils(mContext, file,
                                BackgroundUtils.UNTAR_FILE).execute().get());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Open", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utils.onClickFile(mContext, file);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void searchForFile(String dir, String query) {
        File root_dir = new File(dir);
        File[] list = root_dir.listFiles();

        if (list != null && root_dir.canRead()) {
            if (list.length == 0) return;

            for (File check : list) {
                String name = check.getName();

                if (check.isFile() && name.toLowerCase().
                        contains(query.toLowerCase())) {
                    addFile(check.getPath());
                } else if(check.isDirectory()) {
                    if (name.toLowerCase().contains(query.toLowerCase())) {
                        addFile(check.getPath());
                    }
                    if (check.canRead() && !dir.equals("/")) {
                        searchForFile(check.getAbsolutePath(), query);
                    }
                }
            }
        }
    }



    public String getCurrentPath() {
        return mCurrentPath;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case MENU_CUT:
            case MENU_COPY:
            case MENU_DELETE:
                showDialog(MENU_DELETE);
                return true;
            case MENU_PERMISSIONS:
                showDialog(MENU_PERMISSIONS);
                return true;
        }
        return false;
    }

    public class ViewAdapter extends RecyclerView.Adapter<BrowserViewHolder> {

        @Override
        public BrowserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.item, parent, false);
            return new BrowserViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final BrowserViewHolder holder, final int position) {
            holder.title.setText(mFiles.get(position).getName());
            getIconForFile(holder.icon, position);

            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                    DateFormat.SHORT, Locale.getDefault());
            BaseFile file = mFiles.get(position);
            if (file.isFile()) {
                holder.info.setText(Utils.displaySize(file.length()));
            } else {
                int num = 0;
                String[] files = file.list();
                if (files != null) {
                    num = files.length;
                }
                holder.info.setText(String.valueOf(num));
            }
            if (mSearching) {
                holder.date.setText(mFiles.get(position).getPath());
            } else {
                holder.date.setText(df.format(file.lastModified()));
            }
        }

        @Override
        public int getItemCount() {
            return mFiles.size();
        }
    }

    public void removeFile(String file) {
        int id = -1;
        for (int i = 0; i < mFiles.size(); i++) {
            if (mFiles.get(i).getPath().equals(file)) {
                id = i;
                break;
            }
        }
        if (id == -1) return;
        mFiles.remove(id);
        mAdapter.notifyItemRemoved(id);
    }

    protected void sortFiles() {
        SortUtils.sort(mContext, mFiles);
    }

    public void showDialog(int id) {
        DialogFragment newFragment =
                MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        BaseBrowserFragment getOwner() {
            return (BaseBrowserFragment) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final int id = getArguments().getInt("id");
            final AlertDialog.Builder builder = new AlertDialog.Builder(getOwner().mContext);
            switch (id) {
                case MENU_DELETE:
                    if (SelectedFiles.getFiles().size() == 1) {
                        BaseFile file = SelectedFiles.getFiles().get(0);
                        if (file != null) {
                            builder.setTitle(file.getName());
                        }
                    } else {
                        builder.setTitle(R.string.delete_dialog_title);
                    }
                    builder.setMessage(R.string.delete_dialog_message);
                    builder.setPositiveButton(R.string.delete,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (BaseFile file : SelectedFiles.getFiles()) {
                                        getOwner().onDeleteFile(file.getRealPath());
                                    }
                                    SelectedFiles.clearAll();
                                    dialog.dismiss();
                                    getOwner().filesChanged(getOwner().getCurrentPath());
                                }
                            });
                    builder.setNegativeButton(R.string.cancel, null);
                    return builder.create();
                case MENU_PERMISSIONS:
                    String path = SelectedFiles.getFiles().get(0).getPath();
                    if (TextUtils.isEmpty(path)) return null;
                    PermissionsDialog dialog = new PermissionsDialog(
                            getOwner().mContext, path);
                    return dialog.getDialog();
                case ACTION_ADD_FILE:
                case ACTION_ADD_FOLDER:
                case MENU_RENAME:
                    View view = View.inflate(getOwner().mContext, R.layout.add_folder, null);
                    final EditText folderName = findById(view, R.id.folder_name);
                    final BaseFile baseFile;
                    if (SelectedFiles.getFiles().size() > 0) {
                        baseFile = SelectedFiles.getFiles().get(0);
                    } else {
                        baseFile = BaseFile.getBlankFile();
                    }
                    if (id == ACTION_ADD_FOLDER) {
                        builder.setTitle(R.string.create_folder);
                        folderName.setHint(R.string.folder_name_hint);
                    } else if (id == ACTION_ADD_FILE) {
                        builder.setTitle(R.string.create_file);
                        folderName.setHint(R.string.file_name_hint);
                    } else {
                        builder.setTitle(baseFile.getName());
                        folderName.setText(baseFile.getName());
                    }
                    builder.setView(view);
                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (v.getId() == R.id.cancel) {
                                dismiss();
                            } else if (v.getId() == R.id.create) {
                                if (id == ACTION_ADD_FILE || id == ACTION_ADD_FOLDER) {
                                    getOwner().addNewFile(folderName.getText().toString(),
                                            id == ACTION_ADD_FOLDER);
                                } else {
                                    getOwner().renameFile(baseFile,
                                            folderName.getText().toString());
                                }
                                dismiss();
                            }
                        }
                    };
                    if (id == MENU_RENAME) {
                        ((Button) findById(view, R.id.create)).setText(R.string.rename);
                    }
                    findById(view, R.id.cancel).setOnClickListener(listener);
                    findById(view, R.id.create).setOnClickListener(listener);
                    return builder.create();
                case MENU_ARCHIVE:
                    View v = View.inflate(getOwner().mContext, R.layout.archive, null);
                    final Spinner archiveType = findById(v, R.id.archive_type);
                    final EditText archiveName = findById(v, R.id.archive_name);
                    if (SelectedFiles.getFiles().size() == 1) {
                        builder.setTitle(SelectedFiles.getFiles().get(0).getName());
                    } else {
                        builder.setTitle("Create Archive.");
                    }
                    builder.setView(v);
                    builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (TextUtils.isEmpty(archiveName.getText())) {
                                Toast.makeText(getOwner().mContext,
                                        "Name cannot be empty.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            String type = String.valueOf(archiveType.getSelectedItem());
                            String name = archiveName.getText().toString();
                            try {
                                switch (type) {
                                    case "zip":
                                        getOwner().filesChanged(new BackgroundUtils(getOwner().mContext,
                                                name, BackgroundUtils.ZIP_FILE).execute().get());
                                        break;
                                    case "tar":
                                        getOwner().filesChanged(new BackgroundUtils(getOwner().mContext,
                                                name, BackgroundUtils.TAR_FILE).execute().get());
                                        break;
                                    case "tar.gz":
                                        getOwner().filesChanged(new BackgroundUtils(getOwner().mContext,
                                                name, BackgroundUtils.TAR_COMPRESS).execute().get());
                                        break;
                                }
                            } catch (Exception e) {
                                // Ignore
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    return builder.create();
            }
            return null;
        }
    }

    public class BrowserViewHolder extends MultiChoiceViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        @Bind(R.id.title) public TextView title;
        @Bind(R.id.date) public TextView date;
        @Bind(R.id.info) public TextView info;
        @Bind(R.id.image) public ImageView icon;

        public BrowserViewHolder(View v) {
            super(v, mMultiSelector);
            ButterKnife.bind(this, v);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
            v.setLongClickable(true);
        }

        public void onClick(View view) {
            if (BuildConfig.DEBUG) Log.d("TEST", "onClick");
            boolean b = mMultiSelector.tapSelection(this);
            if (mMultiSelector.getSelectedPositions().size() == 0) {
                mMultiSelector.setSelectable(false);
                if (mActionMode != null) mActionMode.finish();
            }
            if (!b) {
                Log.d("TEST", "!b");
                onClickFile(mFiles.get(getAdapterPosition()).getRealPath());
            }
            if (mActionMode != null && !b) {
                mActionMode.finish();
            }
        }

        public boolean onLongClick(View view) {
            if (!mSupportsActionMode) return false;
            mActivity.getToolbar().startActionMode(mMultiSelect);
            mMultiSelector.setSelectable(true);
            mMultiSelector.setSelected(this, true);
            return true;
        }
    }

    protected void fileClicked(File file) {
        if (mPicking) {
            filePicked(file);
            return;
        }
        String ext = FileUtil.getExtension(file.getName());
        if (ext.equals("zip") || ext.equals("tar") || ext.equals("gz")) {
            onClickArchive(file.getAbsolutePath());
        } else {
            Utils.onClickFile(mContext, file.getAbsolutePath());
        }
    }

    public void toast(@StringRes int id) {
        toast(mContext.getString(id));
    }

    public void toast(final String message) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
