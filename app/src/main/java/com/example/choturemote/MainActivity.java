package com.example.choturemote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.choturemote.ui.main.SectionsPagerAdapter;

import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;
/**
 * @file MainActivity.java
 * @brief The main activity
 * @details The first screen to appear when the user launches the app.
 */

/**
 * @brief This is where everything important happens.
 * @details This is the first screen to appear when the user launches the app. This handles everything and calls everything.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * @brief Debugging tool
     * @details Toggle to FALSE when using it with the two HC-05's on a breadboard. Check their MAC Addresses before using.
     */
    private final boolean TESTING = false;

    /**
     * @brief Universally Unique Identifier (UUID)
     * @details
     * @li Creating a UUID which represents a 128-bit value.
     * @li More information on UUIDs by the Internet Engineering Task Force can be found <a href="https://www.ietf.org/rfc/rfc4122.txt">here</a>.
     * @li UUIDs are not tied to particular devices. They identify software services. You just need both sides to use the same one.
     * @li "00001101-0000-1000-8000-00805F9B34FB" is the one and only UUID for SPP (serial port profile). Check out <a href="https://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createRfcommSocketToServiceRecord(java.util.UUID)">the Android Developer's page</a>.
     */
    static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * @brief Handles Bluetooth stuff
     * @details Custom class. Handles Bluetooth stuff.
     */
    public static MyBluetoothService myBluetoothService;

    /**
     * @brief Custom class
     * @details SectionsPagerAdapter extends FragmentPagerAdapter which is an implementation of the PagerAdapter class. It is needed for the ViewPager object, viewPager.
     */
    private SectionsPagerAdapter sectionsPagerAdapter;

    /**
     * @brief TabLayout provides a horizontal layout to display tabs
     * @details
     * @li The class method setupWithViewPager() links the given ViewPager and this TabLayout together so that changes in one are automatically reflected in the other. This includes scroll state changes and clicks. The tabs displayed in this layout will be populated from the ViewPager adapter's page titles.
     * @li The TabLayout object, tabs, is a View in the activity_main.xml layout file with the id "tabs".
     */
    private TabLayout tabs;

    /**
     * @brief Layout manager allowing left/right swipes to get access to more options
     * @details
     * @li ViewPager is a layout manager that allows the user to flip left and right through pages of data.
     * @li You supply an implementation of a PagerAdapter to generate the pages that the view shows. In this case it was supplied a SectionsPagerAdapter object sectionsPagerAdapter. SectionsPagerAdapter extends FragmentPagerAdapter which is an implementation of the PagerAdapter class.
     * @li More information <a href="https://developer.android.com/reference/android/support/v4/view/ViewPager.html">at the ViewPager entry</a> and <a href="https://developer.android.com/reference/android/support/v4/view/PagerAdapter.html">PagerAdapter entry</a> in the Android Developer's documentation.
     * @li The ViewPager object, viewPager, is a View in the activity_main.xml layout file with the id "view_pager".
     */
    private ViewPager viewPager;

    /**
     * @var POKE_SLEEP_CLASSIFIER
     * @brief Used to send sleep or poke instructions
     * @details Command Strings pertaining to sleep or poke instructions classified by this classifier
     *
     * @var SEPARATOR
     * @brief Command instruction separator
     * @details Is used to visually distinguish between parts of a String instruction.
     *
     * @var TERMINATOR
     * @brief Command instruction terminator
     * @details Used to indicate the end of a String instruction.
     *
     * @var POKE
     * @brief Used as a modifier
     * @details Used as a modifier to distinguish a poke command from a sleep command.
     *
     * @var ENABLE
     * Enables poking
     *
     * @var DISABLE
     * Disables poking
     */
    private static String POKE_SLEEP_CLASSIFIER;
    private static String SEPARATOR, TERMINATOR;
    private static String POKE;
    private static String ENABLE, DISABLE;

    /**
     * Debugging tool
     */
    private static String TAG = "MainActivity";

    /**
     * MAC ADDRESS of the Bluetooth device to establish communication with
     */
    private String MAC_ADDRESS;

    /**
     * @brief A BluetoothAdapter object
     * @details A BluetoothAdapter lets you perform fundamental Bluetooth tasks, such as initiate device discovery, query a list of bonded (paired) devices, instantiate a BluetoothDevice using a known MAC address, and create a BluetoothServerSocket to listen for connection requests from other devices, and start a scan for Bluetooth LE devices.
     */
    private BluetoothAdapter bluetoothAdapter;

    /**
     * An integer passed to startActivityForResult() and received by onActivityResult(). If it is greater >= 0, this code will be returned in onActivityResult() when the activity exits. Does nothing of significance at present.
     */
    private int REQUEST_ENABLE_BT = 1;

    /**
     * @brief a BroadcastReceiver type object
     * @details
     * @li Receives and handles broadcast intents sent by <a href="https://developer.android.com/reference/android/content/Context.html#sendBroadcast(android.content.Intent)">Context.sendBroadcast(Intent)</a>.
     * @li This is an implementation of BroadcastReceiver registered to be run in the main activity thread. Its receiver is called with any broadcast Intent that matches filter, in this case is <b>BluetoothAdapter.ACTION_STATE_CHANGED</b> (in other words, when the state of the local Bluetooth adapter has been changed).
     */
    private final BroadcastReceiver mBTStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Lets us know through Toast messages if BT status changes //
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(context, R.string.BT_STATE_OFF_TEXT, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Toast.makeText(context, R.string.BT_STATE_TURNING_OFF_TEXT, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Toast.makeText(context, R.string.BT_STATE_ON_TEXT, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Toast.makeText(context, R.string.BT_STATE_TURNING_ON_TEXT, Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        }
    };

    /**
     * @param savedInstanceState A Bundle object containing the activity's previously saved state. If the activity has never existed before, the value of the Bundle object is null. (Bundle is generally used for passing data between various activities of android.)
     * @brief fires when the system first creates the activity.
     * @details In the onCreate() method, you perform basic application startup logic that should happen only once for the entire life of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // By calling super.onCreate(savedInstanceState);, you tell the Dalvik VM (an android virtual machine optimized for mobile devices) to run your code in addition to the existing code in the onCreate() of the parent class. If you leave out this line, then only your code is run. The existing code is ignored completely. //
        super.onCreate(savedInstanceState);

        // the Activity class takes care of creating a window for you in which you can place your UI with setContentView(View) //
        setContentView(R.layout.activity_main);

        // String code //
        POKE_SLEEP_CLASSIFIER = getString(R.string.POKE_SLEEP_CLASSIFIER);
        SEPARATOR = getString(R.string.SEPARATOR);
        TERMINATOR = getString(R.string.TERMINATOR);
        POKE = getString(R.string.POKE_INSTRUCTION);
        DISABLE = getString(R.string.DISABLE);
        ENABLE = getString(R.string.ENABLE);

        // ViewPager and connected TabLayout setup //
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        // FAB //
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            boolean toggle = true;

            @Override
            public void onClick(View view) {
                // Toggles between POKE ENABLED and POKE DISABLED //
                toggle = !toggle;
                if (toggle) {
                    // Changes the FAB icon //
                    fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_menu_view));
                    writeToBluetooth(POKE_SLEEP_CLASSIFIER, POKE, ENABLE);
                    Snackbar.make(view, POKE + " " + ENABLE + "ED", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_lock_idle_lock));
                    writeToBluetooth(POKE_SLEEP_CLASSIFIER, POKE, DISABLE);
                    Snackbar.make(view, POKE + " " + DISABLE + "ED", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Triggers sleep animation and turns off screen of the "face device" //
                writeToBluetooth(POKE_SLEEP_CLASSIFIER, "SLEEP", "000");
                Snackbar.make(view, "Chotu is now asleep", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return true;
            }
        });

        //Bluetooth code //
        if (TESTING)
            MAC_ADDRESS = getString(R.string.TESTING_MAC_ADDRESS_2); // MAC_ADDRESS IN USE - EASIER TO CHANGE HERE THAN ALL OVER THE PLACE //
        else
            MAC_ADDRESS = getString(R.string.MAC_ADDRESS); // MAC_ADDRESS IN USE - EASIER TO CHANGE HERE THAN ALL OVER THE PLACE //

        myBluetoothService = new MyBluetoothService(MainActivity.this) {
            @Override
            public void showToast(int resourceID) {
                // Will receive a resourceID, convert it into a String, and send it to showToastMethod() //
                showToastMethod(getString(resourceID));
            }

        };

        // Get a handle/reference to the default local Bluetooth adapter of the device being used //
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null)
            Toast.makeText(this, "Error: This device device does not support Bluetooth", Toast.LENGTH_LONG).show();
        else {
            if (!bluetoothAdapter.isEnabled()) {

                // Creating an Intent //
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                // A dialog appears requesting user permission to enable Bluetooth. //
                // If the user responds "Yes", the system begins to enable Bluetooth //
                // Focus returns to your application once the process completes (or fails) //
                // onActivityResult() that gets called upon return of focus //
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);


            } else {
                beginConnection();
            }

            // Informs us when BT condition changes //
            // Registers a BroadcastReceiver to be run in the main activity thread. //
            // The receiver will be called with any broadcast Intent that matches filter //
            // The Broadcast Receiver implementation is outside onCreate() //
            registerReceiver(mBTStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        }
    }

    /**
     * @brief The final call you receive before your activity is destroyed
     * @details This opportunity is used to unregister the BroadcastReceiver, mBTStateBroadcastReceiver
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregistering broadcast listener to free up resources
        unregisterReceiver(mBTStateBroadcastReceiver);
    }

    /**
     * @brief Establishes a Bluetooth serial communication
     * @details Fetches device info from paired devices.
     */
    private void beginConnection() {

        boolean deviceFound = false;

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device. //
            for (BluetoothDevice device : pairedDevices) {
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if (deviceHardwareAddress.equals(MAC_ADDRESS)) {
                    // This bit matches the MAC address of your "face device" to a paired device's //
                    // Then establishes a connection on a separate thread //
                    deviceFound = true;
                    myBluetoothService.startClient(device, MY_UUID);
                    break;
                }
            }
            if (!deviceFound) {
                Toast.makeText(getBaseContext(), "ERROR: Device with MAC address " + MAC_ADDRESS + " not paired", Toast.LENGTH_LONG).show();
                this.finishAffinity();
            }

        } else {
            Toast.makeText(getBaseContext(), "ERROR:" + getString(R.string.NO_PAIRED_DEVICES), Toast.LENGTH_LONG).show();
            this.finishAffinity();
        }
    }

    /**
     * @param classifier  Possible values F, B, R, L, E, etc.
     * @param instruction Parameterizes or explains the classifier. This can be a number or can be something else entirely.
     * @param modifier    Modifies the parameter definition.
     * @brief Forms and sends command Strings through Bluetooth
     * @details Constructs command Strings using SEPARATOR and TERMINATOR then sends command String through Bluetooth. This makes the command construction process easily programmable and editable as it is all in one place.
     */
    static public void writeToBluetooth(String classifier, String instruction, String modifier) {

        if (MyBluetoothService.connectedThreadRunning()) {

            String fullMessage = classifier + SEPARATOR + instruction + SEPARATOR + modifier + TERMINATOR;

            // Converts the String to an Array of byte type //
            byte[] bytes = fullMessage.getBytes(Charset.defaultCharset());
            myBluetoothService.write(bytes);
        }
    }

    /**
     * @param requestCode the requestCode passed as the second parameter to startActivityForResult(), here it is REQUEST_ENABLE_BT
     * @param resultCode  Possible values - RESULT_OK or RESULT_CANCELED
     * @param data        Optional parameter. An Intent, which can return result data to the caller. `@Nullable` denotes that a value can be null.
     * @brief Called back after focus is returned from process started by startActivityForResult()
     * @details If enabling Bluetooth succeeds, this activity receives the RESULT_OK result code in the onActivityResult() callback. If Bluetooth was not enabled due to an error (or the user responded "No") then the result code is RESULT_CANCELED.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            beginConnection();
        }
        if (resultCode == RESULT_CANCELED)
            Toast.makeText(this, "Unable to access Bluetooth", Toast.LENGTH_SHORT).show();
    }

    /**
     * @param message Message to be shown in Toast
     * @brief Toasts for threads other than the main
     * @details Toasts can only be displayed on the main thread. To call Toasts from other threads, a public method in the Activity running on the main thread can be used.
     */
    public void showToastMethod(final String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

}


