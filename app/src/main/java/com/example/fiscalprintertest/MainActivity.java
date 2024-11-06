package com.example.fiscalprintertest;

import static com.example.fiscalprintertest.PrinterManager.getFiscalDevice;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;

import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdConfig;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdInfo;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdReceipt;
import com.datecs.fiscalprinter.SDK.model.UserLayerV1.cmdReport;
import com.example.fiscalprintertest.connectivity.BluetoothSppConnector;
import com.example.fiscalprintertest.databinding.ActivityMainBinding;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private cmdReceipt.FiscalReceipt.InvoiceClientInfo clientInfo;
    private cmdReceipt.FiscalReceipt fiscalReceipt = new cmdReceipt.FiscalReceipt();
    private cmdReceipt.NonFiscalReceipt noFiscalReceipt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        noFiscalReceipt = new cmdReceipt.NonFiscalReceipt();

         // UI adjustments
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Obtain a reference to the system Bluetooth adapter
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

        // Bluetooth permissions check
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
        } else {

            // App has bluetooth permissions, showing device list
            Toast.makeText(this, "Device list", Toast.LENGTH_SHORT).show();
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                StringBuilder builder = new StringBuilder();

                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    builder.append(deviceName);
                    builder.append("\n");
                    builder.append(deviceHardwareAddress);
                    builder.append("\n\n");

                }
                // setting the text with devices to the TextView
                binding.pairedDevicesTv.setText(builder.toString());

            } else {
                Toast.makeText(this, "No paired devices", Toast.LENGTH_SHORT).show();
            }

            // Used for multithreading
            ExecutorService executorService = Executors.newFixedThreadPool(3);

            binding.cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelReceipt();
                }
            });

            binding.stornoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    makeStorno();
                }
            });

            binding.selectButton.setOnClickListener(v -> {
                String deviceAddress = binding.devAddrEt.getText().toString();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);

                if (device == null) {

                } else {
                    BluetoothSppConnector connector = new BluetoothSppConnector(device);
                    executorService.execute(() -> {
                        try {
                            connector.connect();
                            PrinterManager.getInstance().init(connector);
                            DatecsFiscalDevice fiscalDevice = getFiscalDevice();

                            String modelVendorName = PrinterManager.instance.getModelVendorName();

                            runOnUiThread(() -> {

                                if (fiscalDevice == null){
                                    Toast.makeText(MainActivity.this, "Cannot connect", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (fiscalDevice.isConnectedPrinter()){
                                    Toast.makeText(this, "Printer is connected", Toast.LENGTH_SHORT).show();
                                }
                                Toast.makeText(MainActivity.this, modelVendorName, Toast.LENGTH_SHORT).show();
                            });

                            // here we can test the commands

                            // set date and time


                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    });

                }
            });

            binding.functionButton.setOnClickListener((v) -> {
               /* cmdConfig.DateTime myClock= new cmdConfig.DateTime();
                try {
                    myClock.setDateTime("30-09-24","15:33:00");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }


                cmdConfig.DateTime clock = new cmdConfig.DateTime();

                runOnUiThread(() -> {
                    try {
                        Toast.makeText(this, clock.getDateTime(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                */

                //printInvoice();
                testPrinter();
               /* final cmdReport.ReportSummary reportSummary = new cmdReport.ReportSummary();
                new Thread(() ->{
                    try {
                        cmdReport cmd = new cmdReport();
                        cmd.PrintXreport(reportSummary);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();

                */
            });
        }
    }

    private void generateClientInfo() {
        clientInfo = new cmdReceipt.FiscalReceipt.InvoiceClientInfo(
                "814109779",
                cmdReceipt.InvoiceClientInfo.BulstatType.fromOrdinal(cmdReceipt.InvoiceClientInfo.BulstatType.EIK.ordinal()),
                "Продавач",
                "Боян Тонков",
                "ЕТ Боян Тонков",
                "9999999999",
                "с. Караисен ул. 41 17",
                "",
                "Боян Тонков");

        boolean[] res = clientInfo.isValid();
        for (int i = 0; i < res.length; i++){
            if (!res[i]) {
                Log.d("ERROR      *****    rrrrrr", i + " " + res[i]);
            }
        }
    }

    private void makeStorno() {

    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // let the user know what would happen depending on the option they chose
    }



    private void cancelReceipt(){

        try {
            if (noFiscalReceipt.isOpen()) {
                noFiscalReceipt.closeNonFiscalReceipt();
                return;
            }
            if (new cmdReceipt.FiscalReceipt.Storno().isOpen()) fiscalReceipt.cancel();
            if (fiscalReceipt.isOpen()) {
                final Double owedSum = new cmdReceipt.FiscalReceipt.FiscalTransaction().getNotPaid();//owedSum=Amount-Tender
                Double payedSum = new cmdReceipt.FiscalReceipt.FiscalTransaction().getPaid();
                //If a TOTAL in the opened receipt has not been set, it will be canceled
                if (payedSum == 0.0) {
                    fiscalReceipt.cancel();
                    return;
                }
                //If a TOTAL is set with a partial payment, there is a Amount and Tender is positive.
                //Offer payment of the amount and completion of the sale.
                if (owedSum > 0.0) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle(getString(R.string.app_name));
                    String sQuestion = String.format("Cannot cancel receipt, payment has already started.\n\r" +
                            "Do you want to pay the owed sum: %2.2f -and close it?", owedSum / 100.0);

                    dialog.setMessage(sQuestion);
                    dialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                new cmdReceipt.FiscalReceipt.FiscalSale().saleTotal(
                                        "Тотал:",
                                        "",
                                        cmdReceipt.FiscalReceipt.FiscalSale.PaidMode.fromOrdinal(0).getId(),
                                        ""); //We pays a full amount

                                fiscalReceipt.closeFiscalReceipt();
                            } catch (Exception e) {
                                runOnUiThread(() -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        }
                    });
                    dialog.setNegativeButton("no", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                fiscalReceipt.cancel();
                            } catch (Exception e) {
                                runOnUiThread(() -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                                e.printStackTrace();
                            }
                        }
                    });
                    dialog.show();

                } else fiscalReceipt.closeFiscalReceipt();

            }

        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
            e.printStackTrace();
        }
    }

    private void testPrinter(){

        String operatorCode = "1";//Operator number
        String operatorPassword = null;
        if ( getFiscalDevice().isConnectedPrinter())
            operatorPassword = getFiscalDevice().getConnectedPrinterV1().getDefaultOpPass();

        if ( PrinterManager.instance.getFiscalDevice().isConnectedECR())
            operatorPassword = getFiscalDevice().getConnectedECRV1().getDefaultOpPass();

        String salePoint = "1"; //Number of work place / integer from 1 to 99999 /

        try {
            if (!fiscalReceipt.isOpen()) {
                generateClientInfo();
                //FOR DATECS FP-800 / FP-2000 / FP-650 / SK1-21F / SK1-31F/ FMP-10 / FP-550
                /**
                 *
                 *    UNP Unique sales number format:
                 * - serial number of the fiscal device
                 * - operator code (four digits or Latin characters)
                 * - sequential sales number (seven digits with leading zeros)
                 *   example: DT000600-0001-0001000
                 *
                 *  Note: DATECS FP-800 / FP-2000 / FP-650 / SK1-21F / SK1-31F/ FMP-10 / FP-550 Only!
                 *
                 *  Before the first sale, the UNP must be set at least once
                 *  if then omitted the parameter device will increment with the number
                 *  of the sale automatically.
                 *
                 */

                String unp = new cmdInfo().GetDeviceSerialNumber() + "-" +
                        String.format("%04d", Integer.parseInt(operatorCode)) + "-" + // Pad left with trailing zero
                        String.format("%07d", 1 + Integer.valueOf(fiscalReceipt.getLastUNP()));  //Next Document number pad left with trailing zero

                fiscalReceipt.openFiscalReceipt(operatorCode, operatorPassword, salePoint, unp);    //Internal generated  unp="".
                cmdReceipt.FiscalReceipt.FiscalSale testSale = new cmdReceipt.FiscalReceipt.FiscalSale();

                fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                //Registration of items for sale
                fiscalReceipt.printFreeText("Тест продажба без параметри!", true, true, true, cmdReceipt.FiscalReceipt.FreeFiscalTextType.type32dpiA);
                testSale.add(
                        "Продукт 1",
                        "",
                        "A", //А, Б, В...
                        "1",
                        "1",
                        "kg",
                        cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.noCorecction,
                        "");

                /* fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash_space);
                testSale.add(
                        "Демонстрация",
                        "продажба с количество",
                        "A",  //А, Б, В...
                        "0.01",
                        "1",
                        "Кг", //Note! Units is not supported on DP-05, DP-25, DP-35 , WP-50, DP-150
                        cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.noCorecction,
                        "");

                fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                testSale.add(
                        "Демонстрация",
                        "Процентна отстъпка",
                        "B", //А, Б, В...
                        "0.11",
                        "10",
                        "Кг",  //Note! Units is not supported on DP-05, DP-25, DP-35 , WP-50, DP-150
                        cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.discountPercentage,
                        "-10"); // Use sign !

                fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash_space);
                testSale.add(
                        "Демонстрация",
                        "Отстъпка по стойност",
                        "B", //А, Б, В...
                        "0.11",
                        "10",
                        "Литри", //Note! Units is not supported on DP-05, DP-25, DP-35 , WP-50, DP-150
                        cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.discountSum,
                        ".5");
                fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.equal);
                testSale.add(
                        "Демонстрация",
                        "Надбавка в процент",
                        "B",  //А, Б, В...
                        "0.1",
                        "10",
                        "Литри", //Note! Units is not supported on DP-05, DP-25, DP-35 , WP-50, DP-150
                        cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.surchargePercentage,
                        "1.5");

                fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.equal);
                fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                testSale.add(
                        "",
                        "VOID !!!",
                        "B",  //А, Б, В...
                        "-0.1",
                        "",
                        "",
                        cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.noCorecction,
                        "");
*/

  /*                      fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                        fiscalReceipt.printFreeText("EAN8 - 1234567");
                        fiscalReceipt.printBarcode("1234567", cmdReceipt.BarcodeType.EAN8, "", false);

                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                        fiscalReceipt.printFreeText("EAN13 - 123456789191");
                        fiscalReceipt.printBarcode("123456789191", cmdReceipt.BarcodeType.EAN13, "", false);

                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                        fiscalReceipt.printFreeText("Code128 - 123456789191Code128");
                        fiscalReceipt.printBarcode("01234567890123456789128", cmdReceipt.BarcodeType.Code128, "", false);
*/

                fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                fiscalReceipt.printFreeText("ПОЛУЧАТЕЛ:");
                fiscalReceipt.printFreeText("ЕТ Боян Тонков");
                fiscalReceipt.printFreeText("814109779");
                fiscalReceipt.printFreeText("с. Караисен ул. 41 17");
                //fiscalReceipt.printFreeText("DataMatrix - 123456789191DataMatrix");
                //fiscalReceipt.printBarcode("123456789191DataMatrix", cmdReceipt.BarcodeType.DataMatrix, "", false);

                 /*       fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                        fiscalReceipt.printFreeText("QRcode - 123456789191QR");
                        fiscalReceipt.printBarcode("123456789191QR", cmdReceipt.BarcodeType.QRcode, "", false);

                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                        fiscalReceipt.printFreeText("ITF - 12345678");
                        fiscalReceipt.printBarcode("12345678", cmdReceipt.BarcodeType.ITF2, "", false);

                        fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                        fiscalReceipt.printFreeText("PDF417 - 123456789191");
                        fiscalReceipt.printBarcode("123456789191PDF417", cmdReceipt.BarcodeType.PDF417, "", false);*/


                //SUBTOTAL
                fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.equal);
                testSale.subtotal(true, true, cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.noCorecction, "");
                //fiscalReceipt.printFreeText("Отстъпка в междинната сума", true, true, true, cmdReceipt.FiscalReceipt.FreeFiscalTextType.type32dpiB);
                //testSale.subtotal(true, true, cmdReceipt.FiscalReceipt.FiscalSale.CorrectionType.discountSum, "2.52");

                //TOTAL
                fiscalReceipt.printSeparatingLine(cmdReceipt.SeparatingLine.dash);
                cmdReceipt.TotalResult totalResult = testSale.saleTotal(
                        "Тотал:",
                        "",
                        cmdReceipt.FiscalReceipt.FiscalSale.PaidMode.fromOrdinal(0).getId(),
                        "1");

                fiscalReceipt.printFreeText("Не се дължи плащане!", true, true, true, cmdReceipt.FiscalReceipt.FreeFiscalTextType.type32dpiA);
                //CLOSE
//        public void printClientInfo(BulstatType typeBULSTAT, String bulstat, String seller, String receiver, String client, String clientTaxNo, String clientAddress1, String clientAddress2, String accountablePerson) throws Exception {
                //clientInfo.printClientInfo(cmdReceipt.InvoiceClientInfo.BulstatType.EIK, "814109779", "Продавач", "Боян Тонков", "ЕТ Боян Тонков", "9999999999", "с. Караисен ул. 41 17", "", "Боян Тонков");
                clientInfo.saveClientInfo();
                fiscalReceipt.closeFiscalReceipt();

            } else closeAll();

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * This method attempts to refuse a fiscal or non-fiscal receipt.
     * And if there is a startup payment on the amount and it is not fully paid,
     * it issues a payment message.
     */
    private void closeAll() throws Exception {

        //   cmdReceipt.FiscalReceipt.Storno stornoReceipt = new cmdReceipt.FiscalReceipt.Storno();
        cmdReceipt.FiscalReceipt fiscalReceipt = new cmdReceipt.FiscalReceipt();
        cmdReceipt.FiscalReceipt.NonFiscalReceipt noFiscalReceipt = new cmdReceipt.FiscalReceipt.NonFiscalReceipt();

        if (noFiscalReceipt.isOpen()) {
            noFiscalReceipt.closeNonFiscalReceipt();
            return;
        }

        // if (stornoReceipt.isOpen()) stornoReceipt.cancel();
        //Try to close fiscal receipt of all type: Sale Receipt,Invoice Receipt,Storno of Sale and Storno of Invoice.
        if (fiscalReceipt.isOpen()) tryToCloseFiscalReceipt(fiscalReceipt);
    }


    private void tryToCloseFiscalReceipt(cmdReceipt.FiscalReceipt thisReceipt) throws Exception {

        final Double owedSum = new cmdReceipt.FiscalReceipt.FiscalTransaction().getNotPaid();//owedSum=Amount-Tender
        Double payedSum = new cmdReceipt.FiscalReceipt.FiscalTransaction().getPaid();//Amount that the customer has provided

        //If a TOTAL in the opened receipt has not been set, it will be canceled

                thisReceipt.cancel();


    }

    private void closeFiscalRec(cmdReceipt.FiscalReceipt thisReceipt) throws Exception {
        //Try to enter new client Invoice Info
        //if (thisReceipt.isInvoice())
           // tryToEnterClientInfo();
        thisReceipt.closeFiscalReceipt();
    }


}