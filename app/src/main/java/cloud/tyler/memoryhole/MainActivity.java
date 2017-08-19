package cloud.tyler.memoryhole;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.R.attr.data;

public class MainActivity extends AppCompatActivity
{
    EditText statusField;
    String status_before = "";
    final private int PERMISSION_READ = 123;
    final private int PERMISSION_WRITE = 123;
    File sdcard = Environment.getExternalStorageDirectory();
    File dir = new File(sdcard.getAbsolutePath() + "/Memory Hole");
    File file = new File(dir, "Memories.csv");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                dir.mkdir();
                file = new File(dir, "Memories.csv");

                if(file.exists())
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "text/csv");

                    try {
                        startActivity(intent);
                    }
                    catch(Exception e)
                    {
                        toast(e.toString());
                    }
                }
                else
                {
                    snackbar("When you have some memories saved, tap again!");
                }

            }
        });

        setTheme(R.style.ThemeOverlay_AppCompat_Dark_ActionBar);

        statusField = (EditText) findViewById(R.id.statusField);

        //add listeners
        statusField.addTextChangedListener(new TextWatcher() {

            int length = 0;

            // the user's changes are saved here
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                TextView counter = (TextView) findViewById(R.id.textCounter);
                length = statusField.getText().toString().length();
                if(length < 141)
                {
                    counter.setText(Integer.toString(length) + "/140");
                }
                else
                {
                    statusField.setText(status_before);
                    statusField.setSelection(length);
                }

            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                status_before = statusField.getText().toString();
            }

            public void afterTextChanged(Editable c) {
                // this one too
            }
        });

        //Allow FAB intent
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_check)
        {
            EditText statusField = (EditText) findViewById(R.id.statusField);
            if(statusField.getText().length() > 0)
            {
                saveStatus(statusField.getText().toString());
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void snackbar(String s)
    {
        Snackbar.make(getCurrentFocus(), s, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void toast(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    public void saveStatus(String s)
    {
        //Year, Day of Year, Time, S

        //variables
        Calendar calendar = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("HH.mm");
        Date timeDate = calendar.getTime();
        String time = df.format(calendar.getTime());
        int year = calendar.get(Calendar.YEAR);
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        //parse Status
        s = s.replaceAll("\"", "\"\"");

        //Permissions
        if(checkPermissions())
        {
            //File Setup

            dir.mkdir(); //if not already created

            String header = "Year, Day of Year, Time, Status\n";
            String status = Integer.toString(year) + "," + Integer.toString(dayOfYear) + ","
                    + time.toString()
                    + ",text," + "\"" + s + "\"\n";

            FileWriter f;
            try
            {
                f = new FileWriter(Environment.getExternalStorageDirectory()+
                        "/Memory Hole/Memories.csv", true);

                if(file.length() == 0)
                {
                    f.write(header);
                }
                f.write(status);
                f.flush();
                f.close();

                //reset status field
                statusField.setText("");
                snackbar("Good job.");
            }
            catch(Exception e)
            {
                snackbar("There was a problem saving your memory- it went down the memory hole." +
                        " Try again!");
            }
        }
    }

    /*
    Checks the permissions for file reading/writing. Asks if they're not granted.
     */
    public boolean checkPermissions()
    {
        //get read and write permissions
        final int permissionRead = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        final int permissionWrite = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //show explanation dialog if either permission missing
        if(permissionRead != PackageManager.PERMISSION_GRANTED ||
                permissionWrite != PackageManager.PERMISSION_GRANTED)
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("To save your memories, I'll need to read and write to" +
                    " your device. You can find these memories in /Memory Hole/Memories.csv.");
            alertDialogBuilder.setTitle("Heads up!");

            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            //request missing permissions
                            if (permissionRead != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]
                                                {Manifest.permission.READ_EXTERNAL_STORAGE},
                                        PERMISSION_READ);
                                return;
                            }

                            if (permissionWrite != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]
                                                {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_WRITE);
                            }
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode) {
            case 123: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //permission granted
                    saveStatus(statusField.getText().toString());
                }
                else //Permission Denied
                {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setMessage("To save a memory, you'll need to enable " +
                            "this permission.");
                    alertDialogBuilder.setTitle("Permission Denied");

                    alertDialogBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // permission denied OK button
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
