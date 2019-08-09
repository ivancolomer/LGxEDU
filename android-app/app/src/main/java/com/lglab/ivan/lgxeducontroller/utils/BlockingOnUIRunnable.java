package com.lglab.ivan.lgxeducontroller.utils;


import android.app.Activity;

public class BlockingOnUIRunnable
{
    // Activity
    private Activity activity;

    // Event Listener
    private BlockingOnUIRunnableListener listener;

    // UI runnable
    private Runnable uiRunnable;


    /**
     * Class initialization
     * @param activity Activity
     * @param listener Event listener
     */
    public BlockingOnUIRunnable( Activity activity, BlockingOnUIRunnableListener listener )
    {
        this.activity = activity;
        this.listener = listener;

        uiRunnable = new Runnable()
        {
            public void run()
            {
                // Execute custom code
                if ( BlockingOnUIRunnable.this.listener != null ) BlockingOnUIRunnable.this.listener.onRunOnUIThread();

                synchronized ( this )
                {
                    this.notify();
                }
            }
        };
    }


    /**
     * Start runnable on UI thread and wait until finished
     */
    public void startOnUiAndWait()
    {
        synchronized ( uiRunnable )
        {
            // Execute code on UI thread
            activity.runOnUiThread( uiRunnable );

            // Wait until runnable finished
            try
            {
                uiRunnable.wait();
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
        }
    }

}
