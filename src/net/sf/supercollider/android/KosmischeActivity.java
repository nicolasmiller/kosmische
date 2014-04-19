package net.sf.supercollider.android;

import net.sf.supercollider.android.ISuperCollider;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

/**
 * An example application which exercises some basic features of a SuperCollider-enabled application.
 * 
 * It creates an ScServiceConnection object through which it gets a SuperCollider.Stub.  The stub
 * object provides access to the SuperCollider Service through IPC- it does not run in the same process 
 * space as this Application.  The stub is then wired simply through the OnTouchEventListener of the
 * main GUI widget, to play a note when you touch it.
 * 
 * The only SC-A class that this activity needs to be directly aware of is the generated ISuperCollider class
 * 
 * @TODO: be more javadoc-friendly
 * 
 * @author Dan Stowell
 * @author Alex Shaw
 *
 */
public class KosmischeActivity extends Activity {
    private ServiceConnection conn = new ScServiceConnection();
    private ISuperCollider.Stub superCollider;
    private TextView mainWidget = null;
    private int defaultNodeId = 999;
	
    /*
     * Gets us a SuperCollider service. 
     */
    private class ScServiceConnection implements ServiceConnection {
        //@Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            KosmischeActivity.this.superCollider = (ISuperCollider.Stub) service;
            try {
                // Kick off the supercollider playback routine
                superCollider.start();
                // Start a synth playing
                superCollider.sendMessage(OscMessage.createSynthMessage("default", defaultNodeId, 0, 1)); 
                setUpControls(); // now we have an audio engine, let the activity hook up its controls
            } catch (RemoteException re) {
                re.printStackTrace();
            }
        }
        //@Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
	
    /**
     * Provide the glue between the user's greasy fingers and the supercollider's shiny metal body
     */
    public void setUpControls() {
        if (mainWidget!=null) mainWidget.setOnTouchListener(new OnTouchListener() {
                //@Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction()==MotionEvent.ACTION_UP) {
                        Log.d("net.sf.supercollider.android", "up");
                        // OSC message right here!
                        OscMessage noteMessage = new OscMessage( new Object[] {
                                "/n_set", defaultNodeId, "amp", 0f
                            });
                        try {
                            // Now send it over the interprocess link to SuperCollider running as a Service
                            superCollider.sendMessage(noteMessage);
                        } catch (RemoteException e) {
                            Log.d("net.sf.supercollider.android", "com", e);
                            Toast.makeText(
                                           KosmischeActivity.this, 
                                           "Failed to communicate with SuperCollider!", 
                                           Toast.LENGTH_SHORT);
                            e.printStackTrace();
                        }
                    } else if ((event.getAction()==MotionEvent.ACTION_DOWN) || (event.getAction()==MotionEvent.ACTION_MOVE)) {
                        Log.d("net.sf.supercollider.android", "down/move");
                        float vol = 1f - event.getY()/mainWidget.getHeight();
                        OscMessage noteMessage = new OscMessage( new Object[] {
                                "/n_set", defaultNodeId, "amp", vol
                            });
                        //float freq = 150+event.getX();
                        //0 to mainWidget.getWidth() becomes sane-ish range of midinotes:
                        float midinote = event.getX() * (70.f / mainWidget.getWidth()) + 28.f;
                        float freq = sc_midicps(Math.round(midinote));
                        OscMessage pitchMessage = new OscMessage( new Object[] {
                                "/n_set", defaultNodeId, "freq", freq
                            });
                        try {
                            superCollider.sendMessage(noteMessage);
                            superCollider.sendMessage(pitchMessage);
                        } catch (RemoteException e) {
                            Log.d("net.sf.supercollider.android", "com", e);
                            Toast.makeText(
                                           KosmischeActivity.this, 
                                           "Failed to communicate with SuperCollider!", 
                                           Toast.LENGTH_SHORT);
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
            });
        try {
            superCollider.openUDP(57110);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainWidget = new TextView(this);
        setContentView(mainWidget);
        // Here's where we request the audio engine
        bindService(new Intent("supercollider.START_SERVICE"),conn,BIND_AUTO_CREATE);
		
        mainWidget.setTypeface(Typeface.MONOSPACE);
        mainWidget.setTextSize(10);
        mainWidget.setText("Welcome to a SuperCollider-Android instrument!\n"
                           +"Y axis is volume, X axis is pitch\n"
                           +"\n"
                           + "                  ~+I777?                \n"
                           + "           :++?I77I====~?I7I             \n"
                           + "     ~=~+I77I??===+?IIII++~+?7           \n"
                           + " 77I~?777+===+?????+++?+II??~==7 ,      \n"
                           + " 7777 I~=II7?++=?+????++=+?IIII~~~+7:   \n"
                           + " I7=I?777 ~~+?7I?+==++?II?++=~~+I7I+++  \n"
                           + " ?7~, ~?=+777~~=+7IIII+~~=+??++++++,,+  \n"
                           + " ?7~      =~+ 77~~~~~=++++++=:,,,,  :+  \n"
                           + " +7= ??=~=      77++++==~~~:,   ,:, ,=  \n"
                           + " +7= +?+???+?=,, 7++:     ,:~~~===, ,=  \n"
                           + " =7= ++=+==???I, I=+   ,,:~=====~=, ,~  \n"
                           + " ~7+ =+=     +I: ?=+,  ==~~     ~=: ,~  \n"
                           + " ~7? ~+=  =~ +I: ?~+,  ~~       ~=: ,:  \n"
                           + " :7? ~++  == =I~ +~+:  ~~   ~::  =~ ,:  \n"
                           + " :7?  +++++= =I~ +~+:  :~   ~~:  =~ ,:  \n"
                           + " ,7I=    =~~ ~I= =:+:  :=~~~~~:  =~  ,  \n"
                           + "  7III~~      I+ ~:+~  :=~~~     ==  ,  \n"
                           + "    I??7II=+=,I+ ~,+~       :~~====     \n"
                           + "        ++=7II?I? :,+=  ,:::~=+==+=:     \n"
                           + "        ,  =~:7I? , +=~=++++=~::,,,      \n"
                           + "              :,  , ++===~~~,            \n"
                           + "              ,     +~     ,             \n"
                           + "                 ,                       \n"
                           );
    }
	
    @Override
    public void onPause() {
        super.onPause();
        try {
            // Free up audio when the activity is not in the foreground
            if (superCollider!=null) superCollider.stop();
            this.finish();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
	
    @Override
    public void onStop() {
        super.onStop();
        try {
            // Free up audio when the activity is not in the foreground
            if (superCollider!=null) superCollider.stop();
            this.finish();
        } catch (RemoteException re) {
            re.printStackTrace();
        } 
    }
	
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            superCollider.closeUDP();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        unbindService(conn);
    }

    /**
     * Convert midi notes to floating point pitches - based on sc_midicps in the SC C++ code
     */
    float sc_midicps(float note)
    {
        return (float) (440.0 * Math.pow((float)2., (note - 69.0) * (float)0.083333333333));
    }

}

