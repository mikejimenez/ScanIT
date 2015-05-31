package alpha.com.scanit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
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
    ListView listView;


    // private String Results;
    private String[] log = new String[100];
    private TextView formatTxt, contentTxt;

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
            String Format = cn.getBarcode();
            String X = Format.substring(0, 2) + " " + Format.substring(2, 5) + " " + Format.substring(5, 8) + " " + Format.substring(8, 10) + " " + Format.substring(10, 14) + " " + Format.substring(14, 18);
            log[i] = "\n" + X + " / " + cn.getCompany();
            i++;
        }

    }

    public void emailResults() {

        Intent i = new Intent(Intent.ACTION_SEND);

        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, "tw1st3dm4st1c@gmail.com");
        i.putExtra(Intent.EXTRA_SUBJECT, "Tracking Numbers");

        FormatEmail();
        String newString = Arrays.toString(log);

        String FilterNull = newString.replace(", null", "");
        String FilterMisc = FilterNull.replace("[", "");
        String FilterComma = FilterMisc.replace("]", "");
        String FilterFinal = FilterComma.replace(",", "");
        i.putExtra(Intent.EXTRA_TEXT, FilterFinal);

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
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                int itemPosition = position;
//
//
//                String itemValue = listView.getItemAtPosition(position).toString();
//
//                if (itemPosition == 0) {
//
////
//                }
//                // Show Alert *Debug*
//               // Toast.makeText(getApplicationContext(),
//               //         "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
//               //         .show();
//
//            }
//
//        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        //if (scanningResult != null) {
        if (resultCode == RESULT_OK) {

            final String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();

            formatTxt.setText("FORMAT: " + scanFormat);
            contentTxt.setText("CONTENT: " + scanContent);

            final SQLite db = new SQLite(this);
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setIcon(R.drawable.abc_edit_text_material);
            alertDialog.setMessage("Company Name:");
            final EditText input = new EditText(this);
            alertDialog.setView(input);
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Editable value = input.getText();
                    //Log.d(TAG, value.toString());
                    db.addBarcodes(new Barcodes(scanContent, value.toString()));
                    db.close();
                    CreateListView();
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
