package com.lglab.ivan.lgxeducontroller.connection;

import android.database.Cursor;
import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.lglab.ivan.lgxeducontroller.legacy.data.POIsProvider;

import java.io.IOException;
import java.io.InputStream;
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

    public LGConnectionManager() {
        user = "lg";
        password = "lqgalaxy";
        hostname = "192.168.86.39";
        port = 22;

        session = null;
        queue = new LinkedBlockingDeque<>();
        itemsToDequeue = 0;
        lgCommandToReSend = null;
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

    public void tick() {
        ILGConnection activityCopy = activity;
        if (activityCopy != null) {
            Session oldSession = session;
            if (oldSession == null || !oldSession.isConnected()) {
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
                Log.d("ConnectionManager", e.getMessage());
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

    private boolean sendLGCommand(LGCommand lgCommand) {
        lgCommandToReSend = lgCommand;
        Log.d("ConnectionManager", "sending a lgcommand: " + lgCommand.getCommand());
        Session session = getSession();
        if (session == null || !session.isConnected()) {
            Log.d("ConnectionManager", "session not connected: " + lgCommand.getCommand());
            return false;
        }

        ChannelExec channelSsh;
        StringBuilder outputBuffer = new StringBuilder();
        try {
            channelSsh = (ChannelExec) session.openChannel("exec");
        } catch (Exception e) {
            Log.d("ConnectionManager", "couldn't open channel exec: " + lgCommand.getCommand());
            e.printStackTrace();
            return false;
        }

        InputStream commandOutput;
        try {
            commandOutput = channelSsh.getInputStream();
            channelSsh.setCommand(lgCommand.getCommand());

            try {
                channelSsh.connect();
            } catch (Exception e) {
                Log.d("ConnectionManager", "connect exception: " + e.getMessage());
                return false;
            }

            int readByte = commandOutput.read();

            while(readByte != 0xffffffff)
            {
                outputBuffer.append((char)readByte);
                readByte = commandOutput.read();
            }
        } catch(IOException ioX) {
            Log.d("ConnectionManager", "couldn't get InputStream or read from it: " + ioX.getMessage());
            return false;
        }

        channelSsh.disconnect();

        String response = outputBuffer.toString();
        Log.d("ConnectionManager", "response: " + response);
        lgCommand.doAction(response);
        return true;
    }

    public void setData(String user, String password, String hostname, int port) {
        this.user = user;
        this.password = password;
        this.hostname = hostname;
        this.port = port;

        session = null;
        tick();
        addCommandToLG(new LGCommand("echo 'connection';", LGCommand.CRITICAL_MESSAGE, null));
    }

    public void setActivity(ILGConnection activity) {
        this.activity = activity;
    }

    public void addCommandToLG(LGCommand lgCommand) {
        queue.offer(lgCommand);
    }

    public void removeCommandFromLG(LGCommand lgCommand) {
        queue.remove(lgCommand);
    }

    @Override
    public void run() {
        try {
            do {
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

                if (!sendLGCommand(lgCommand)) {
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
            } while (true);
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
}
