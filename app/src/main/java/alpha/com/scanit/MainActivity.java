package alpha.com.scanit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.alpha.ZXing.android.IntentIntegrator;
import com.alpha.ZXing.android.IntentResult;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends Activity {

    // Private Strings
    private String[] log = new String[100];
    private String TypeG = "";
    private String TypeE = "";
    private Boolean Error = false;
    private TextView CounterTxt;
    ListView listView;
    private Integer Counter = 0;
    //String TAG = "MAKE_BARCODES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // for hiding title
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        CreateListView();

        Button scanBtn = (Button) findViewById(R.id.scan_button);
        Button clrBtn = (Button) findViewById(R.id.clr_button);
        Button mnlBtn = (Button) findViewById(R.id.manual_button);

        CounterTxt = (TextView) findViewById(R.id.textView2);
        CounterTxt.setText("0");


        scanBtn.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        IntentIntegrator scanIntegrator = new IntentIntegrator(MainActivity.this);
                        scanIntegrator.initiateScan();

                    }
                }
        );
        mnlBtn.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        InputManual();

                    }
                }
        );
        clrBtn.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        if (Counter == 0) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "No scan data received!", Toast.LENGTH_SHORT);

                            toast.show();
                        } else {
                            emailResults();
                            clrBtnData();
                        }
                    }
                }
        );
    }

    public void clrBtnData() {
        SQLite db = new SQLite(this);
        db.deleteBarcodes();
        CreateListView();
        db.close();
        CounterTxt.setText("0");
    }

    public void InputManual() {

        LayoutInflater Manual = LayoutInflater.from(this);
        final View textEntryView = Manual.inflate(R.layout.manual_entry, null);

        final EditText infoTrack = (EditText) textEntryView.findViewById(R.id.InfoTrack);
        final EditText infoData = (EditText) textEntryView.findViewById(R.id.InfoData);

        final SQLite db = new SQLite(this);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setIcon(R.drawable.ic_dialog_alert_holo_light).setTitle("Manual Entry").setView(textEntryView).setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                        // Log.i("AlertDialog","Entered "+infoData.getText().toString());
                        // Log.i("AlertDialog", "Entered " + infoTrack.getText().toString());

                        Editable value = infoData.getText();
                        String Result = infoTrack.getText().toString();

                        if (Result.length() == 12 && Result.length() != 21) {
                            TypeE = "E";
                        }
                        if (Result.length() == 21) {
                            String buffer = "MJ";
                            final String ManualScanFedExG = buffer + Result.substring(Result.length() - 21, Result.length());
                            db.addBarcodes(new Barcodes(ManualScanFedExG, value.toString()));
                            Counter++;
                            db.close();
                            CreateListView();
                            CounterTxt.setText(Integer.valueOf(Counter).toString());
                        }
                        else {
                            db.addBarcodes(new Barcodes(Result, value.toString()));
                            Counter++;
                            db.close();
                            CreateListView();
                            CounterTxt.setText(Integer.valueOf(Counter).toString());
                        }
                    }
                }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "No scan data received!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
        alert.show();
    }

    public void FormatEmail(String Output) {
        // Display all bar codes
        // Log.d(TAG, "Reading - Display Results");

        SQLite db = new SQLite(this);
        List<Barcodes> Barcodes = db.getBarCodes();
        db.close();
        int i = 0;
        for (Barcodes cn : Barcodes) {
            String bar = cn.getBarcode();

            // Fedex Ground OLD - 22 Characters add MJ to display on screen
            if (bar.length() == 24) {
                String Result = FormatStringX(bar);
                Output = Result;
                log[i] = "\n" + Output + " / " + cn.getCompany();
                i++;
            }

            // UPS Manual - 23 Characters
            if (bar.length() == 23 && bar.contains("Z")) {
                String Result = FormatStringManualUPS(bar);
                Output = Result;
                log[i] = "\n" + Output + " / " + cn.getCompany();
                i++;
            }

            // Fedex Ground Manual - 23 Characters
            if (bar.length() == 23 && !bar.contains("Z")) {
                String Result = FormatStringManualX(bar);
                Output = Result;
                log[i] = "\n" + Output + " / " + cn.getCompany();
                i++;
            }

            // UPS - 18 Characters
            if (bar.length() == 18) {
                String Result = FormatStringUPS(bar);
                Output = Result;
                log[i] = "\n" + Output + " / " + cn.getCompany();
                i++;
            }

            // Fedex Express Manual - 14 Characters
            if (bar.length() == 14) {

                String Result = FormatStringManualE(bar);
                Output = Result;
                log[i] = "\n" + Output + " / " + cn.getCompany();
                i++;
            }

            // Fedex Express - 34 Characters
            // Fedex Ground NEW - 34 Characters && TypeE.equals("E")
            if (bar.length() == 12 && TypeE.equals("E")) {
                TypeG = "";
                String Result = FormatStringE(bar);
                Output = Result;
                log[i] = "\n" + Output + " / " + cn.getCompany();
                i++;
            }

            // Fedex Ground OLD - 32 Characters
            if (bar.length() == 12 && TypeG.equals("G")) {
                TypeE = "";
                String Result = FormatStringG(bar);
                Output = Result;
                log[i] = "\n" + Output + " / " + cn.getCompany();
                i++;
            }
            else {
                Error = true;
            }
        }

    }

    public String Filter(String Number) {

        if (Number.length() == 34) {
            //
            //System.out.println("Added Type E");
            //
            Error = false;
            final String ScanFromFedEXE = Number.substring(Number.length() - 12, Number.length());
            TypeE = "E";
            TypeG = "";
            return ScanFromFedEXE;
        }
        if (Number.length() == 32) {
            //
            //System.out.println("Added Type G");
            //
            Error = false;
            final String ScanFromFedEXG = Number.substring(Number.length() - 16, Number.length() - 4);
            TypeG = "G";
            TypeE = "";
            return ScanFromFedEXG;
        }
        if (Number.length() == 22) {
            //
            //System.out.println("Added Type X");
            //
            Error = false;
            String buffer = "MJ";
            final String ScanFromFedEXG = buffer + Number.substring(Number.length() - 22, Number.length());

            return ScanFromFedEXG;
        }
        if (Number.length() < 11) {
            Error = true;
            //
            //System.out.println("Added Type UPS");
            //
        }

        return Number;
    }

    public String FormatStringG(String Number) {
        //
        // 32 Characters
        // Fedex Ground
        // Format: XXXX XXXX XXXX
        //
        String ScanFromFedEXGO = Number.substring(0, 4) + " " + Number.substring(4, 8) + " " + Number.substring(8, 12);
        return ScanFromFedEXGO;
    }

    public String FormatStringE(String Number) {
        //
        // 34 Characters
        // Fedex Express
        // Format: XXXX XXXX XXXX
        //
        String ScanFromFedEX = Number.substring(0, 4) + " " + Number.substring(4, 8) + " " + Number.substring(8, 12);
        return ScanFromFedEX;
    }

    public String FormatStringManualE(String Number) {
        // Manual Entry
        // 14 Characters
        // Fedex Express
        // Format: XXXX XXXX XXXX

        String ScanFromFedEXManual = Number.substring(0, 4) + "" + Number.substring(4, 9) + "" + Number.substring(9, 14);
        return ScanFromFedEXManual;
    }

    public String FormatStringUPS(String Number) {
        //
        // 18 Characters
        // UPS
        // Format: XX XXX XXX XX XXXX XXXX
        //
        final String ScanFromUPS = Number.substring(0, 2) + " " + Number.substring(2, 5) + " " + Number.substring(5, 8) + " " + Number.substring(8, 10) + " " + Number.substring(10, 14) + " " + Number.substring(14, 18);
        return ScanFromUPS;
    }

    public String FormatStringManualUPS(String Number) {
        // Manual Entry
        // 23 Characters
        // UPS
        // Format: XX XXX XXX XX XXXX XXXX
        //
        final String ScanFromManualUPS = Number.substring(0, 2) + " " + Number.substring(3, 6) + " " + Number.substring(7, 10) + " " + Number.substring(11, 13) + " " + Number.substring(14, 18) + " " + Number.substring(19, 23);
        return ScanFromManualUPS;
    }

    public String FormatStringX(String Number) {
        //
        // 22 Characters
        // Fedex Ground
        // Format: XXXXXXX XXXXXXX XXXXXXX
        // Replace MJ (Placeholder)
        //System.out.println(Number + " Number");
        //
        String ScanFromFedEXG = Number.substring(0, 9) + " " + Number.substring(9, 16) + " " + Number.substring(16, 24);
        String Replace = ScanFromFedEXG.replace("MJ", "");
        return Replace;
    }

    public String FormatStringManualX(String Number) {
        // 23 Characters
        // Fedex Ground
        // Format: XXXXXXX XXXXXXX XXXXXXX
        // Replace MJ (Placeholder)

        String ScanFromFedEXGManual = Number.substring(0, 9) + " " + Number.substring(9, 16) + " " + Number.substring(16, 23);
        String Replace = ScanFromFedEXGManual.replace("MJ", "");
        return Replace;
    }

    public void emailResults() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_SUBJECT, "Tracking Numbers");
        String[] TO = {"Receiving@cdaresort.com"};
        i.putExtra(Intent.EXTRA_EMAIL, TO);
        FormatEmail("");
        String newString = Arrays.toString(log);
        String FilterA = newString.replace(", null", "");
        String FilterB = FilterA.replace("[", "");
        String FilterC = FilterB.replace("]", "");
        String FilterD = FilterC.replace(",", "");
        i.putExtra(Intent.EXTRA_TEXT, FilterD);
        Arrays.fill(log, null);
        Counter = 0;
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void CreateListView() {

        listView = (ListView) findViewById(R.id.listView);

        final SQLite db = new SQLite(this);
        Cursor cursor = db.getBarcodesRaw();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.two_line_list_item,
                cursor,
                new String[]{"Barcode", "Company"},
                new int[]{android.R.id.text1, android.R.id.text2},
                0);

        listView.setDivider(null);
        listView.setSelector(android.R.color.transparent);
        listView.setAdapter(adapter);

        db.close();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            final String scanContent = scanningResult.getContents();

            LayoutInflater Results = LayoutInflater.from(this);

            final View textEntryView = Results.inflate(R.layout.scan_entry, null);
            final EditText scanData = (EditText) textEntryView.findViewById(R.id.scanData);

            final SQLite db = new SQLite(this);
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setIcon(R.drawable.perm_group_user_dictionary).setTitle("Information").setView(textEntryView).setPositiveButton("Save",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {

                            Editable value = scanData.getText();
                            String Result = Filter(scanContent);

                            db.addBarcodes(new Barcodes(Result, value.toString()));
                            Counter++;
                            db.close();
                            CreateListView();
                            CounterTxt.setText(Integer.valueOf(Counter).toString());
                        }
                    }).setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "No scan data received!", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
            alert.show();
        }
        else if (resultCode == RESULT_CANCELED) {
                 Toast toast = Toast.makeText(getApplicationContext(),
                          "No scan data received!", Toast.LENGTH_SHORT);
                  toast.show();
              }
    }
}