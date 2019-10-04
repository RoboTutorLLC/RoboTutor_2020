package cmu.xprize.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.comp_logging.CErrorManager;

/**
 * CMessageQueue
 *
 * <p>Why does every single CComponent have a different Queue? That's dumb.
 * Let's just put them all in the same class.</p>
 * Created by kevindeland on 8/27/19.
 */

public class CMessageQueueFactory {

    private IMessageQueueRunner runner;
    private String TAG;

    public CMessageQueueFactory(IMessageQueueRunner runner, String TAG) {
        this.runner = runner;
        this.TAG = TAG;
    }


    //************************************************************************
    //************************************************************************
    // Component Message Queue  -- Start

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private HashMap queueMap    = new HashMap();
    private HashMap                 nameMap    = new HashMap();
    private boolean                 _qDisabled  = false;

    public class Queue implements Runnable {

        protected String _command;
        String _name; // used to find a command and cancel it.
        Object _targetObject;
        String _targetString;

        public Queue(String command) {
            _command = command;
        }

        // needed for cancelling
        public Queue (String name, String command) {
            this._name = name;
            this._command = command;

            if (name != null) {
                nameMap.put(name, this);
            }
        }

        public Queue(String command, Object target) {
            _command = command;
            _targetObject = target;
        }

        public Queue(String name, String command, String target) {
            _name = name;
            _command = command;
            _targetString = target;
        }

        public String getCommand() {
            return _command;
        }


        @Override
        public void run() {

            try {

                if (_name != null) {
                    nameMap.remove(_name);
                }

                queueMap.remove(this);
                Log.d("QUEUE_FACTORY",
                        String.format(
                                "CMessageQueue.run _name=%s;_command=%s;_targetObject=%s;_targetString=%s",
                                _name, _command, _targetObject, _targetString));

                // ugh this is somewhat different for each file (see QueueConstructorVars.txt)
                // could refactor
                if(_targetObject != null) {
                    Log.wtf("QUEUE_FACTORY", "runCommand(object)");
                    runner.runCommand(_command, _targetObject);
                } else if(_targetString != null) {
                    runner.runCommand(_command, _targetString);
                    Log.wtf("QUEUE_FACTORY", "runCommand(string)");
                } else {
                    runner.runCommand(_command);
                    Log.wtf("QUEUE_FACTORY", String.format("runCommand(%s)", _command));
                }

            }
            catch(Exception e) {
                CErrorManager.logEvent(TAG, "Run Error: cmd:" + _command + " tar: " + _targetObject + "  >", e, false);
            }
        }
    }


    /**
     *  Disable the input queues permenantly in prep for destruction
     *  walks the queue chain to diaable scene queue
     *
     */
    public void terminateQueue() {

        // disable the input queue permenantly in prep for destruction
        //
        _qDisabled = true;
        flushQueue();
    }


    /**
     * Remove any pending scenegraph commands.
     *
     */
    private void flushQueue() {

        Iterator<?> tObjects = queueMap.entrySet().iterator();

        while(tObjects.hasNext() ) {
            Map.Entry entry = (Map.Entry) tObjects.next();

            Log.d(TAG, "Post Cancelled on Flush: " + ((Queue)entry.getValue()).getCommand());

            mainHandler.removeCallbacks((Queue)(entry.getValue()));
        }
    }

    /**
     * Remove named posts
     *
     */
    public void cancelPost(String name) {

        Log.d(TAG, "Cancel Post Requested: " + name);

        while(nameMap.containsKey(name)) {

            Log.d(TAG, "Post Cancelled: " + name);

            mainHandler.removeCallbacks((Queue) (nameMap.get(name)));
            nameMap.remove(name); // JUDITH replicate
        }
    }

    public void postEvent(String event) {
        postEvent(event, 0);
    }

    public void postEvent(String event, Integer delay) {

        post(event, (long) delay);
    }

    public void postNamed(String name, String command, String target, Long delay) {
        enQueue(new Queue(name, command, target), delay);
    }

    /**
     * Keep a mapping of pending messages so we can flush the queue if we want to terminate
     * the tutor before it finishes naturally.
     *
     * @param qCommand
     */
    private void enQueue(Queue qCommand) {
        enQueue(qCommand, 0);
    }
    private void enQueue(Queue qCommand, long delay) {

        if(!_qDisabled) {
            queueMap.put(qCommand, qCommand);

            if(delay > 0) {
                mainHandler.postDelayed(qCommand, delay);
            }
            else {
                mainHandler.post(qCommand);
            }
        }
    }

    /**
     * Post a command to the tutorgraph queue
     *
     * @param command
     */
    public void post(String command) {
        post(command, 0);
    }
    public void post(String command, long delay) {

        enQueue(new Queue(command), delay);
    }


    public void postNamed(String name, String command, Long delay) {
        enQueue(new Queue(name, command), delay);
    }

    /**
     * Post a command and target to this scenegraph queue
     *
     * @param command
     */
    public void post(String command, Object target) {
        post(command, target, 0);
    }
    public void post(String command, Object target, long delay) {

        enQueue(new Queue(command, target), delay);
    }


    // Component Message Queue  -- End
    //************************************************************************
    //************************************************************************

}



