package com.slim.slimfilemanager.sftp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.slim.slimfilemanager.R;

import java.io.File;

public class SftpAccountActivity extends Activity
        implements View.OnClickListener {

    public final static String SETTINGS_ACTION = "com.slim.slimfilemanager.sftp.SETTINGS";
    public final static String ACCOUNT_ID_EXTRA = "com.slim.slimfilemanager.sftp.ACCOUNT_ID";

    private String mInitialPath;
    private String mAccountDisplay;

    AccountProvider mProvider;

    private Button testConnection;
    private TextView host;
    private TextView port;
    private TextView user;
    private TextView pass;
    private CheckBox useSshKey;
    private TextView sshKeyPath;
    private TextView sshKeyPass;
    private TextView initialPath;
    private View sshKeyFrame;

    private boolean mTestedConnection;
    private Thread mThread;
    /* private ProgressDialogFragment mProgress; */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sftp);

        mProvider = new AccountProvider(this);

        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);

        testConnection = (Button) findViewById(R.id.testConnection);
        host = (TextView) findViewById(R.id.host);
        port = (TextView) findViewById(R.id.port);
        user = (TextView) findViewById(R.id.user);
        pass = (TextView) findViewById(R.id.pass);
        useSshKey = (CheckBox) findViewById(R.id.checkUseKey);
        sshKeyPath = (TextView) findViewById(R.id.sshKeyPath);
        sshKeyPass = (TextView) findViewById(R.id.sshKeyPassphrase);
        initialPath = (TextView) findViewById(R.id.initialPath);
        sshKeyFrame = findViewById(R.id.sshKeyFrame);

        final String lastSshKey = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("last_ssh_key", null);
        useSshKey.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTestedConnection = false;
                sshKeyFrame.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                pass.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                findViewById(R.id.passwordLabel).setVisibility(isChecked ? View.GONE : View.VISIBLE);
                invalidateTestConnectionEnabled();
            }
        });
        if (lastSshKey != null) {
            useSshKey.setChecked(true);
            sshKeyPath.setText("");
            sshKeyPath.append(lastSshKey);
        }

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                mTestedConnection = false;
                invalidateTestConnectionEnabled();
                invalidateOptionsMenu();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        host.addTextChangedListener(watcher);
        port.addTextChangedListener(watcher);
        user.addTextChangedListener(watcher);
        pass.addTextChangedListener(watcher);
        sshKeyPath.addTextChangedListener(watcher);
        sshKeyPass.addTextChangedListener(watcher);
        initialPath.addTextChangedListener(watcher);
        testConnection.setOnClickListener(this);

        findViewById(R.id.browseSshKey).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File fi = null;
                final String currentPath = sshKeyPath.getText().toString().trim();
                if (!currentPath.isEmpty())
                    fi = new File(currentPath).getParentFile();
                if (fi == null)
                    fi = Environment.getExternalStorageDirectory();
                //TODO
                //FileChooserDialog.show(AuthenticationActivity.this, fi);
            }
        });

        if (isSettings() && savedInstanceState == null) {
            final String accountId = getAccountId();
            SftpAccount account = mProvider.getAccount(Integer.parseInt(accountId));
            if (account == null) {
                finish();
                return;
            }
            host.setText(account.host);
            port.setText(account.port + "");
            user.setText(account.username);
            if (account.sshKeyPath != null && !account.sshKeyPath.trim().isEmpty()) {
                sshKeyPath.setText(account.sshKeyPath);
                sshKeyPass.setText(account.sshKeyPassword);
                pass.setText("");
                useSshKey.setChecked(true);
            } else {
                sshKeyPath.setText("");
                sshKeyPass.setText("");
                pass.setText(account.password);
                useSshKey.setChecked(false);
            }
            initialPath.setText(account.initialPath);
        }
    }

    private boolean isSettings() {
        return getIntent().getAction() != null && getIntent().getAction().equals(SETTINGS_ACTION);
    }

    protected final String getAccountId() {
        return getIntent().getStringExtra(ACCOUNT_ID_EXTRA);
    }

    private void invalidateTestConnectionEnabled() {
        if (host.getText().toString().trim().length() > 0 &&
                port.getText().toString().trim().length() > 0 &&
                user.getText().toString().trim().length() > 0) {
            testConnection.setEnabled(true);
        } else {
            testConnection.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sftp, menu);
        menu.findItem(R.id.done).setVisible(testConnection.isEnabled());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //cancel();
            return true;
        } else if (item.getItemId() == R.id.done) {
            done();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleViews(final boolean enabled, @StringRes final int testConnectionText) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testConnection.setEnabled(enabled);
                host.setEnabled(enabled);
                port.setEnabled(enabled);
                user.setEnabled(enabled);
                pass.setEnabled(enabled);
                useSshKey.setEnabled(enabled);
                sshKeyPath.setEnabled(enabled);
                sshKeyPass.setEnabled(enabled);
                initialPath.setEnabled(enabled);
                testConnection.setText(testConnectionText);
                //TODO
                /*if (closeProgress && mProgress != null) {
                    mProgress.dismiss();
                    mProgress = null;
                }*/
            }
        });
    }

    private void done() {
        if (!mTestedConnection) {
            testConnection(true);
            return;
        }

        final SftpAccount account = new SftpAccount();
        account.host = host.getText().toString();
        account.port = Integer.parseInt(port.getText().toString().trim());
        account.username = user.getText().toString();
        if (useSshKey.isChecked()) {
            account.sshKeyPath = sshKeyPath.getText().toString();
            account.sshKeyPassword = sshKeyPass.getText().toString();
            account.password = null;
        } else {
            account.sshKeyPath = null;
            account.sshKeyPassword = null;
            account.password = pass.getText().toString();
        }
        account.initialPath = initialPath.getText().toString();

        setInitialPath(account.initialPath);
        setAccountDisplay(account.username + "@" + account.host);

         if (isSettings()) {
            account.id = Integer.parseInt(getAccountId());
            mProvider.updateAccount(account);
            finish();
        } else {
            mProvider.addAccount(account);
            finish();
        }
    }

    public final void setAccountDisplay(String display) {
        mAccountDisplay = display;
    }

    public final void setInitialPath(String initialPath) {
        mInitialPath = initialPath;
    }

    private void showError(final Throwable e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(SftpAccountActivity.this)
                        .setTitle(R.string.error)
                        .setMessage(e.getLocalizedMessage())
                        .setPositiveButton(android.R.string.ok, null)
                        .setCancelable(false)
                        .show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mThread != null && !mThread.isInterrupted())
            mThread.interrupt();
        // TODO
        /*if (mProgress != null) {
            mProgress.dismiss();
            mProgress = null;
        }*/
    }

    // Test Connection
    @Override
    public void onClick(View v) {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(testConnection.getWindowToken(), 0);
        testConnection(false);
    }

    private void testConnection(final boolean doneAfter) {
        toggleViews(false, R.string.testing_connection);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (useSshKey.isChecked())
            prefs.edit().putString("last_ssh_key", sshKeyPath.getText().toString().trim()).apply();
        else
            prefs.edit().remove("last_ssh_key").apply();

        // TODO
        //mProgress = ProgressDialogFragment.create(R.string.testing_connection).show(this);
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");

                JSch ssh = new JSch();
                Session session = null;
                ChannelSftp channel = null;

                if (useSshKey.isChecked()) {
                    try {
                        if (!sshKeyPass.getText().toString().trim().isEmpty()) {
                            ssh.addIdentity(sshKeyPath.getText().toString(),
                                    sshKeyPass.getText().toString());
                        } else {
                            ssh.addIdentity(sshKeyPath.getText().toString());
                        }
                        config.put("PreferredAuthentications", "publickey");
                    } catch (Throwable e) {
                        e.printStackTrace();
                        showError(e);
                        toggleViews(true, R.string.error_retry);
                        return;
                    }
                } else {
                    config.put("PreferredAuthentications", "password");
                }

                try {
                    session = ssh.getSession(user.getText().toString(),
                            host.getText().toString(),
                            Integer.parseInt(port.getText().toString()));
                    session.setConfig(config);
                    if (!useSshKey.isChecked())
                        session.setPassword(pass.getText().toString());
                    session.connect();
                    channel = (ChannelSftp) session.openChannel("sftp");
                    channel.connect();

                    if (!initialPath.getText().toString().trim().isEmpty())
                        channel.cd(initialPath.getText().toString());
                    toggleViews(true, R.string.successful);
                    mTestedConnection = true;
                    if (doneAfter) done();
                } catch (Throwable e) {
                    e.printStackTrace();
                    showError(e);
                    toggleViews(true, R.string.error_retry);
                    mTestedConnection = false;
                    if (doneAfter) showError(e);
                } finally {
                    if (session != null)
                        session.disconnect();
                    if (channel != null)
                        channel.disconnect();
                }
            }
        });
        mThread.start();
    }

    /*@Override
    public void onChoice(File file) {
        sshKeyPath.setText("");
        sshKeyPath.append(file.getAbsolutePath());
    }*/
}
