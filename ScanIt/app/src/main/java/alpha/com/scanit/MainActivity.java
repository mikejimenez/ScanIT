package alpha.com.scanit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
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

    // private Strings
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
        clrBtn.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        if (Counter == 0) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "No scan data received!", Toast.LENGTH_SHORT);

                            toast.show();
                        }
                        else {
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

    public void FormatEmail(String Output) {
        //
        // Display all bar codes
        //Log.d(TAG, "Reading - Display Results");
        //
        SQLite db = new SQLite(this);
        List<Barcodes> Barcodes = db.getBarCodes();
        db.close();
        int i = 0;
        for (Barcodes cn : Barcodes) {
            String bar = cn.getBarcode();
            if (bar.length() == 18) {
                //
                // UPS - 18 Characters
                //System.out.println(" Type UPS");
                //
                String Result = FormatStringUPS(bar);
                Output = Result;
                log[i] = "\n" + Output + " / " + cn.getCompany();
                i++;
            }
            // Fedex Express - 34 Characters
            // Fedex Ground NEW - 34 Characters && TypeE.equals("E")
            if (bar.length() == 12 && TypeE.equals("E")) {
                TypeG = "";
                //
                //System.out.println(" Type E");
                //
                String Result = FormatStringE(bar);
                Output = Result;
                log[i] = "\n" + Output + " / " + cn.getCompany();
                i++;
            }
            // Fedex Ground OLD - 32 Characters
            if (bar.length() == 12 && TypeG.equals("G")) {
                TypeE = "";
                //
                //System.out.println(" Type G");
                //
                String Result = FormatStringG(bar);
                Output = Result;
                log[i] = "\n" + Output + " / " + cn.getCompany();
                i++;
            }
            // Fedex Ground OLD - 22 Characters
            if (bar.length() == 24) {
                //
                //System.out.println(" Type X");
                //System.out.println(bar + " BAR");
                //
                String Result = FormatStringX(bar);
                Output = Result;
                log[i] = "\n" + Output + " / " + cn.getCompany();
                i++;
            }
            else {
                Error = true;
            }
        }

    }

    public String FilterFedex(String Number) {

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

    public String FormatStringUPS(String Number) {
        //
        // 18 Characters
        // UPS
        // Format: XX XXX XXX XX XXXX XXXX
        //
        final String ScanFromUPS = Number.substring(0, 2) + " " + Number.substring(2, 5) + " " + Number.substring(5, 8) + " " + Number.substring(8, 10) + " " + Number.substring(10, 14) + " " + Number.substring(14, 18);
        return ScanFromUPS;
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
            final SQLite db = new SQLite(this);

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setIcon(R.drawable.abc_edit_text_material);
            alertDialog.setMessage("Name/Company");

            final EditText input = new EditText(this);
            alertDialog.setView(input);

            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Editable value = input.getText();
                    String Result = FilterFedex(scanContent);
                    db.addBarcodes(new Barcodes(Result, value.toString()));
                    Counter++;
                    db.close();
                    CreateListView();
                    CounterTxt.setText(Integer.valueOf(Counter).toString());
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing.
                }
            });
            alertDialog.show();


        } else if (resultCode == RESULT_CANCELED) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

}
