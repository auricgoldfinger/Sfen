package gpapez.sfen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by Gregor on 3.8.2014.
 */
public class ProfileActivity extends Activity {

    private static ProfileActivity sInstance = null;
    protected ViewGroup mContainerSoundAndDisplay;
    protected ViewGroup mContainerAction;

    // in case of updating profile
    protected boolean isUpdating = false;
    protected boolean isChanged = false;
    protected int updateKey = -1;
    protected ArrayList<DialogOptions> updatedSoundAndDisplay;
    protected ArrayList<DialogOptions> updatedActions;

    // placeholder for current Profile
    protected Profile profile = null;

    // arrays for conditions and actions
    protected ArrayList<DialogOptions> actions = new ArrayList<DialogOptions>();


    // list of possible Actions in Options
    //context.getResources().getDrawable(R.drawable.ic_launcher)
    static final ArrayList<DialogOptions> optActions = new ArrayList<DialogOptions>() {{
        //add(new DialogOptions("Show notification", "Will show notification in notification area", android.R.drawable.ic_dialog_info, DialogOptions.type.ACT_NOTIFICATION));
        add(new DialogOptions("Show notification", "Will show notification in notification area", R.drawable.ic_notification, DialogOptions.type.ACT_NOTIFICATION));
        add(new DialogOptions("Enable Wifi", "Enable Wifi when conditions met", R.drawable.ic_wifi, DialogOptions.type.ACT_WIFIENABLE));
        add(new DialogOptions("Disable Wifi", "Disable Wifi when conditions met", R.drawable.ic_wifi, DialogOptions.type.ACT_WIFIDISABLE));
        add(new DialogOptions("Enable Mobile Data", "Available for rooted phones only", R.drawable.ic_mobiledata, DialogOptions.type.ACT_MOBILEENABLE));
        add(new DialogOptions("Disable Mobile Data", "Available for rooted phones only", R.drawable.ic_mobiledata, DialogOptions.type.ACT_MOBILEDISABLE));
        add(new DialogOptions("Vibrate", "Vibrate phone when triggered", R.drawable.ic_launcher, DialogOptions.type.ACT_VIBRATE));
        add(new DialogOptions("Play Sfen", "Will make a sheep sound", R.drawable.ic_sound, DialogOptions.type.ACT_PLAYSFEN));
        add(new DialogOptions("Dialog with text", "Will show dialog with text", R.drawable.ic_dialog, DialogOptions.type.ACT_DIALOGWITHTEXT));
        add(new DialogOptions("Open application", "Will open specified application", R.drawable.ic_dialog, DialogOptions.type.ACT_OPENAPPLICATION));
    }};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // set singleton instance
        sInstance = this;


        /**
         * ACTION CONTAINER
         */
        mContainerAction = (ViewGroup) findViewById(R.id.action_container);



        // ACTION
        final ViewGroup newAction = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.condition_action_header, mContainerAction, false);

        ((TextView) newAction.findViewById(android.R.id.text1)).setText(getString(R.string.action_new));
        ((TextView) newAction.findViewById(android.R.id.text2)).setText(getString(R.string.action_new_sub));

        // LISTENER for NEW ACTION
        newAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getBaseContext(), "picking new action", Toast.LENGTH_SHORT).show();
                BackgroundService.getInstance().mUtil.openDialog(sInstance, optActions, "Pick action");
            }
        });

        mContainerAction.addView(newAction, 0);


        // stop! hammertime!
        // lets check if we got any event passed to us!
        if (getIntent().getStringExtra("sProfile") != null) {
            isUpdating = true;

            profile = (new Gson()).fromJson(getIntent().getExtras().getString("sProfile"), Profile.class);
            updateKey = getIntent().getIntExtra("sProfileIndexKey", -1);
            updatedActions = new ArrayList<DialogOptions>();

            getActionBar().setTitle("Editing "+ profile.getName());
            //getActionBar().

            //Log.e("EVENT FROM OBJ", event.getName() + " with " + event.getConditions().size() + " conditions- key from all events: " + updateKey);
            refreshView();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home ||
                id == R.id.action_cancel) {

            finish();
            return true;


        }
        if (id == R.id.action_save) {
            //return saveEvent();
            // if event was successfully saved, check if we have to create alarms
            // geofaces if we have such conditions
            if (saveProfile()) {
                //refreshView();
                return true;
            }
            else
                return false;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * SINGLETON INSTANCE
     *
     * Singleton function that returns the current instance of our class
     * if it does not exist, it creates new instance.
     * @return instance of current class
     */
    public static ProfileActivity getInstance() {
        if (sInstance == null) {
            return new ProfileActivity();
        }
        else
            return sInstance;
    }

    /**
     * Saving/updating event!
     */
    private boolean saveProfile() {


        // do we have event name?
        if (((TextView) findViewById(R.id.profile_name)).getText().length() == 0) {
            Util.showMessageBox("And you think you can get away without entering Event name?", true);
            return false;
        }

        if (!isUpdating) {
            profile = new Profile();
        }

        /**
         * get parameters from ui and save it to our Profile object
         */
        profile.setName(((TextView) findViewById(R.id.profile_name)).getText().toString());
        profile.setVibrate(((CheckBox) findViewById(R.id.profile_vibrate)).isChecked());

        /**
         * save actions too
         */
        profile.setActions(actions);



        // finally, save/update profile to profiles array
        if (isUpdating) {
            //events.set(events.indexOf(e), e);
            BackgroundService.getInstance().profiles.set(updateKey, profile);
        }
        else {
            BackgroundService.getInstance().profiles.add(profile);
        }


        finish();
        return true;
    }


    /**
     * refreshView is used only if we passed event from other activity
     * and would like to populate entries in eventactivity
     *
     * we have to update name, conditions and actions!
     */
    public void refreshView() {
        ((TextView) findViewById(R.id.profile_name)).setText(profile.getName());
        ((CheckBox) findViewById(R.id.profile_vibrate)).setChecked(profile.isVibrate());


        // also, would be great if we add all actions to container, no?
        ArrayList<DialogOptions> allAct = profile.getActions();
        for (DialogOptions act : profile.getActions()) {
            addNewAction(sInstance, act, 0);
        }

        actions = updatedActions;

    }


    /**
     *
     * ADD NEW ACTION
     *
     */
    protected void addNewAction(final Activity context, final DialogOptions entry, final int index) {

        // the only thing we have to check if we're editing entry is,
        // if we have at least one setting stored. if so, all is good in our wonderland
        //final boolean isEditing = (cond.getSettings().size() > 0) ? true : false;

        // add condition to list of conditions of Event
        if (isUpdating) {
            updatedActions.add(entry);
        }
        // adding NEW
        else {
            actions.add(entry);
        }

        // get options that we need for interface
        String title = entry.getSetting("text1");
        String description = entry.getSetting("text2");
        int icon = entry.getIcon();

        // add new row to actions/conditions now
        final ViewGroup newRow;

        newRow = (ViewGroup) LayoutInflater.from(context).inflate(
                R.layout.condition_single_item, EventActivity.getInstance().mContainerAction, false);

        ((TextView) newRow.findViewById(android.R.id.text1)).setText(title);
        ((TextView) newRow.findViewById(android.R.id.text2))
                .setText(description);
        //((TextView) newRow.findViewById(android.R.id.text2))
        //        .setMovementMethod(new ScrollingMovementMethod());

        ((ImageButton) newRow.findViewById(R.id.condition_icon))
                .setImageDrawable(context.getResources().getDrawable(icon));

        newRow.findViewById(R.id.condition_single_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: clicking our newly added condition
                int index = ((ViewGroup) newRow.getParent()).indexOfChild(newRow);
                Util util = new Util();
                util.openSubDialog(context, entry, index);
                //showMessageBox("clicked " + entry.getTitle() + ", " + entry.getOptionType() +" type: "+ entry.isItemConditionOrAction() +" on index "+ index, false);
            }
        });

        /**
         * delete button for single item
         */
        newRow.findViewById(R.id.condition_single_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // when clicking recycle bin at condition, remove it from view and
                // from array of all conditions

                int index = ((ViewGroup) newRow.getParent()).indexOfChild(newRow);

                removeAction(index, entry);

            }
        });


        // add action to container
        mContainerAction.addView(newRow, index);

    }

    /**
     *
     * REMOVE ACTION
     *
     */

    /**
     * remove single Condition or Action
     *
     * @param
     */
    protected void removeAction(final int index, final DialogOptions entry) {
        // when clicking recycle bin at condition/action, remove it from view and
        // from array of all conditions/actions

        // remove ACTION from container first
        mContainerAction.removeViewAt(index);


        //container.removeView(newRow);

        // UPDATING SINGLE EVENT!!!
        // remove from conditions, depending on if we're adding to new event
        // or existing event
        if (isUpdating) {

            updatedActions.remove(updatedActions.indexOf(entry));

            // we changed something, so set the changed boolean
            isChanged = true;


        }

        // CREATING SINGLE profile!!!
        else {

                actions.remove(actions.indexOf(entry));

        }
    }


    /**
     * onClick: PROFILE NAME
     */
    public void onClickProfileName(View v) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);


        // Set an EditText view to get user input
        final TextView info = new TextView(this);
        final EditText input = new EditText(this);

//        info.setText("Input text");

        if (isUpdating) {
            input.setText(profile.getName());
        }

        // select all text in edittext
        input.setSelectAllOnFocus(true);

        // auto open soft keyboard
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);



        LinearLayout newView = new LinearLayout(this);
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        newView.setLayoutParams(parms);
        newView.setOrientation(LinearLayout.VERTICAL);
        newView.setPadding(15, 15, 15, 15);
        //newView.addView(info, 0);
        newView.addView(input, 0);



        builder
                .setView(newView)
                .setIcon(R.drawable.ic_launcher)
                .setTitle("Enter Profile name")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        if (input.getText().toString().equals("")) {
                            Util.showMessageBox("You must enter profile name!", false);
                            return;
                        } else {

                            ((TextView) findViewById(R.id.profile_name)).setText(input.getText().toString());

                        }

                        // close the keyboard if any
                        if (imm != null) {
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // just close the dialog if we didn't select the days
                        dialog.dismiss();

                        // close the keyboard if any
                        if (imm != null) {
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        }

                    }
                });


        builder.show();


    }


    /**
     *
     * OnClick: PROFILE ICON
     *
     */
    public void onClickProfileIcon(View v) {

    }


    /**
     *
     * OnClick: PROFILE VOLUMES
     *
     */
    public void onClickVolumes(View v) {

    }

    /**
     *
     * OnClick: PROFILE VIBRATE
     *
     */
    public void onClickProfileVibrate(View v) {

        if (isUpdating) {

            profile.setVibrate(
                    ((CheckBox) findViewById(R.id.profile_vibrate)).isChecked()
            );

        }
    }

    /**
     *
     * OnClick: PROFILE RINGTONE
     *
     */
    public void onClickProfileRingtone(View v) {

    }

    /**
     *
     * OnClick: PROFILE NOTIFICATION SOUND
     *
     */
    public void onClickProfileNotificationSound(View v) {

    }

}