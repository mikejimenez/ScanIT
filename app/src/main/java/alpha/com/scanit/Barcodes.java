package alpha.com.scanit;

public class Barcodes {

    //private variables
    int _id;
    String _Barcode;
    String _Company;


    public Barcodes() {

    }

    public Barcodes(int id, String Barcode, String _Company) {
        this._id = id;
        this._Barcode = Barcode;
        this._Company = _Company;
    }

    public Barcodes(String Barcode, String _Company) {
        this._Barcode = Barcode;
        this._Company = _Company;
    }

     // getting ID
    public int getID() {
        return this._id;
    }

    // setting id
    public void setID(int id) {
        this._id = id;
    }

    // getting Barcode
    public String getBarcode() {
        return this._Barcode;
    }

    // setting Barcode
    public void setBarcode(String Barcode) {
        this._Barcode = Barcode;
    }

    // getting Company
    public String getCompany() {
        return this._Company;
    }

    // setting Company
    public void setCompany(String Company) {
        this._Company = Company;
    }
}
