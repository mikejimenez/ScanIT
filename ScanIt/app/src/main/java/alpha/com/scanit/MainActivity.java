package alpha.com.scanit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
    private TextView formatTxt, contentTxt, CounterTxt;
    ListView listView;
    private Integer Counter = 0;

    //String TAG = "MAKE_BARCODES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CreateListView();

        Button scanBtn = (Button) findViewById(R.id.scan_button);
        Button clrBtn = (Button) findViewById(R.id.clr_button);
        formatTxt = (TextView) findViewById(R.id.scan_format);
        contentTxt = (TextView) findViewById(R.id.scan_content);
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
                        emailResults();
                        clrBtnData();
                    }
                }
        );
    }

    public void clrBtnData() {
        SQLite db = new SQLite(this);
        db.deleteBarcodes();
        CounterTxt.setText("0");
        CreateListView();
        db.close();
    }

    public void FormatEmail() {
        // Display all bar codes
        //Log.d(TAG, "Reading - Display Results");
        SQLite db = new SQLite(this);
        List<Barcodes> Barcodes = db.getBarCodes();
        db.close();

        int i = 0;
        for (Barcodes cn : Barcodes) {
            String bar = cn.getBarcode();

            if (bar.contains("1Z")) {
                // UPS
                String Output = bar.substring(0, 2) + " " + bar.substring(2, 5) + " " + bar.substring(5, 8) + " " + bar.substring(8, 10) + " " + bar.substring(10, 14) + " " + bar.substring(14, 18);
                log[i] = "\n" + Output + " / " + cn.getCompany();
                i++;
            } else {
                String Result = FormatString(bar);
                String Output = Result;
                log[i] = "\n" + Output + " / " + cn.getCompany();
                i++;
            }

        }
    }

    public void emailResults() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_SUBJECT, "Tracking Numbers");
        String[] TO = {"Receiving@cdaresort.com"};
        i.putExtra(Intent.EXTRA_EMAIL, TO);
        FormatEmail();
        String newString = Arrays.toString(log);
        String FilterA = newString.replace(", null", "");
        String FilterB = FilterA.replace("[", "");
        String FilterC = FilterB.replace("]", "");
        String FilterD = FilterC.replace(",", "");
        i.putExtra(Intent.EXTRA_TEXT, FilterD);
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

        listView.setAdapter(adapter);
        db.close();
    }

    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public String FilterFedex(String Number) {
        if (Number.length() == 34) {
            final String ScanFromFedEXE = Number.substring(Number.length() - 12, Number.length());
            return ScanFromFedEXE;
        }
        if (Number.length() == 22) {
            String buffer = "MJ";
            final String ScanFromFedEXG = buffer + Number.substring(Number.length() - 22, Number.length());
            return ScanFromFedEXG;
        }
        return Number;
    }

    public String FormatString(String Number) {
        if (Number.length() != 24) {
            String ScanFromFedEXE = Number.substring(0, 4) + " " + Number.substring(4, 8) + " " + Number.substring(8, 12);
            return ScanFromFedEXE;
        }
        else {
            String ScanFromFedEXG = Number.substring(0, 9) + " " + Number.substring(9, 16) + " " + Number.substring(16, 24);
            String Replace = ScanFromFedEXG.replace("MJ", "");
            return Replace;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);


        if (resultCode == RESULT_OK) {
            final String scanContent = scanningResult.getContents();
            //  String scanFormat = scanningResult.getFormatName();
            //  formatTxt.setText("FORMAT: " + scanFormat);
            //  contentTxt.setText("CONTENT: " + scanContent);

            final SQLite db = new SQLite(this);
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setIcon(R.drawable.abc_edit_text_material);
            alertDialog.setMessage("Name/Company");
            final EditText input = new EditText(this);
            alertDialog.setView(input);
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Editable value = input.getText();
                    //Log.d(TAG, value.toString());
                    if (scanContent.contains("1Z")) {
                        db.addBarcodes(new Barcodes(scanContent, value.toString()));
                        Counter++;
                    } else {
                        String Result = FilterFedex(scanContent);
                        db.addBarcodes(new Barcodes(Result, value.toString()));
                        Counter++;
                    }

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
