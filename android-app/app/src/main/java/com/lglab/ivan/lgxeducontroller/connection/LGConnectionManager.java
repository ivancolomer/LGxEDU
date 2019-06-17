package com.lglab.ivan.lgxeducontroller.connection;

import android.database.Cursor;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class LGConnectionManager implements Runnable {
    public static final short CONNECTED = 1;
    public static final short NOT_CONNECTED = 2;
    public static final short QUEUE_BUSY = 3;

    private static LGConnectionManager instance = null;
    private static StatusUpdater statusUpdater = null;
    private String user;
    private String password;
    private String hostname;
    private int port;
    private Session session;
    private BlockingQueue<LGCommand> queue;
    private int itemsToDequeue;
    private LGCommand lgCommandToReSend;
    private ILGConnection activity;

    private boolean shouldRestartMapNavigation;

    public LGConnectionManager() {
        user = "lg";
        password = "lg";
        hostname = "10.160.67.56";
        port = 22;

        session = null;
        queue = new LinkedBlockingDeque<>();
        itemsToDequeue = 0;
        lgCommandToReSend = null;
        shouldRestartMapNavigation = false;
        //loadDataFromDB();
    }

    public static LGConnectionManager getInstance() {
        if (instance == null) {
            instance = new LGConnectionManager();
            new Thread(instance).start();
            statusUpdater = new StatusUpdater(instance);
            new Thread(statusUpdater).start();
        }
        return instance;
    }

    public synchronized void tick() {
        ILGConnection activityCopy = activity;
        if (activityCopy != null) {
            if (session == null || !session.isConnected()) {
                activityCopy.setStatus(LGConnectionManager.NOT_CONNECTED);
            } else if (lgCommandToReSend == null && queue.size() == 0) {
                activityCopy.setStatus(LGConnectionManager.CONNECTED);
            } else {
                activityCopy.setStatus(LGConnectionManager.QUEUE_BUSY);
            }
        }
    }

    private void loadDataFromDB() {
        Cursor category_cursor = POIsProvider.getLGConnectionData();
        if (category_cursor.moveToNext()) {
            user = category_cursor.getString(category_cursor.getColumnIndexOrThrow("user"));
            password = category_cursor.getString(category_cursor.getColumnIndexOrThrow("password"));
            hostname = category_cursor.getString(category_cursor.getColumnIndexOrThrow("hostname"));
            port = category_cursor.getInt(category_cursor.getColumnIndexOrThrow("port"));
        }
    }

    private void saveDataToDB() {
        POIsProvider.updateLGConnectionData(user, password, hostname, port);
    }

    private Session getSession() {
        Session oldSession = this.session;
        if (oldSession == null || !oldSession.isConnected()) {
            JSch jsch = new JSch();
            Session session;
            try {
                session = jsch.getSession(user, hostname, port);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            session.setPassword(password);

            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);

            try {
                session.connect(Integer.MAX_VALUE);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            this.session = session;
            return session;
        }


        try {
            oldSession.sendKeepAliveMsg();
            return oldSession;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized boolean sendLGCommand(LGCommand lgCommand, boolean isSearchCommand) {
        lgCommandToReSend = lgCommand;

        Session session = getSession();
        if (session == null || !session.isConnected()) {
            return false;
        }

        ChannelExec channelSsh;
        try {
            channelSsh = (ChannelExec) session.openChannel("exec");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelSsh.setOutputStream(baos);

        channelSsh.setCommand(lgCommand.getCommand());

        try {
            channelSsh.connect();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        channelSsh.disconnect();
        //baos.toString();
        if(isSearchCommand) {
            shouldRestartMapNavigation = true;
        }
        return true;
    }

    public void setData(String user, String password, String hostname, int port) {
        this.user = user;
        this.password = password;
        this.hostname = hostname;
        this.port = port;

        session = null;
        tick();
        addCommandToLG(new LGCommand("echo 'connection';", LGCommand.CRITICAL_MESSAGE));
        //saveDataToDB();
    }

    public void setActivity(ILGConnection activity) {
        this.activity = activity;
    }

    public void addCommandToLG(LGCommand lgCommand) {
        try {
            queue.offer(lgCommand);
        } catch (Exception e) {

        }
    }

    @Override
    public void run() {
        try {
            while (true) {

                LGCommand lgCommand = lgCommandToReSend;
                if (lgCommand == null) {
                    lgCommand = queue.take();

                    if (itemsToDequeue > 0) {
                        itemsToDequeue--;
                        if (lgCommand.getPriorityType() == LGCommand.CRITICAL_MESSAGE) {
                            lgCommandToReSend = lgCommand;
                        }
                        continue;
                    }
                }

                long timeBefore = System.currentTimeMillis();

                if (!sendLGCommand(lgCommand, false)) {
                    //Command not sent
                    itemsToDequeue = queue.size();
                } else if (System.currentTimeMillis() - timeBefore >= 2000) {
                    //Command sent but took more than 2 seconds
                    lgCommandToReSend = null;
                    itemsToDequeue = queue.size();
                } else {
                    //Command sent in less than 2 seconds
                    lgCommandToReSend = null;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public boolean isShouldRestartMapNavigation() {
        return shouldRestartMapNavigation;
    }
}
