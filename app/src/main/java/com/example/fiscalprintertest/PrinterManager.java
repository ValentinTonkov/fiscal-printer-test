package com.example.fiscalprintertest;


import static com.datecs.fiscalprinter.SDK.FiscalException.FPTR_CC_OTHER;

import com.datecs.fiscalprinter.SDK.FiscalDeviceV1;
import com.datecs.fiscalprinter.SDK.FiscalException;
import com.datecs.fiscalprinter.SDK.FiscalPrinterV1;
import com.datecs.fiscalprinter.SDK.model.BGR.DP05_BGR;
import com.datecs.fiscalprinter.SDK.model.BGR.DP150_BGR;
import com.datecs.fiscalprinter.SDK.model.BGR.DP15_BGR;
import com.datecs.fiscalprinter.SDK.model.BGR.DP25_BGR;
import com.datecs.fiscalprinter.SDK.model.BGR.DP35_BGR;
import com.datecs.fiscalprinter.SDK.model.BGR.FDModelDetectorV1;
import com.datecs.fiscalprinter.SDK.model.BGR.FMP10_BGR;
import com.datecs.fiscalprinter.SDK.model.BGR.FP2000_BGR;
import com.datecs.fiscalprinter.SDK.model.BGR.FP550_BGR;
import com.datecs.fiscalprinter.SDK.model.BGR.FP650_BGR;
import com.datecs.fiscalprinter.SDK.model.BGR.FP700_BGR;
import com.datecs.fiscalprinter.SDK.model.BGR.FP800_BGR;
import com.datecs.fiscalprinter.SDK.model.BGR.SK1_21F_BGR;
import com.datecs.fiscalprinter.SDK.model.BGR.SK1_31F_BGR;
import com.datecs.fiscalprinter.SDK.model.BGR.WP50_BGR;
import com.datecs.fiscalprinter.SDK.model.DatecsFiscalDevice;
import com.example.fiscalprintertest.connectivity.AbstractConnector;

import java.io.IOException;

public class PrinterManager {

    private String modelVendorName = "";

    public String getModelVendorName() {
        return modelVendorName;
    }

    public static DatecsFiscalDevice getFiscalDevice() {
        return fiscalDevice;
    }


    public static PrinterManager getInstance() {
        return instance;
    }

    private static DatecsFiscalDevice fiscalDevice;//communication with the connected device occurs only through this object

    private AbstractConnector mConnector;

    public static final PrinterManager instance;

    static {
        instance = new PrinterManager();
    }


    /**
     * Depending on the response of the connected device, we c   * List of  all supported device by Protocol V1
     * * ECR:DP-05, DP-25, DP-35 , WP-50, DP-150
     * * Fiscal Printers: FP-800,FP-2000,FP-650,SK1-21F,SK1-31F,FMP-10,FP-550reate the corresponding instance of  Supported Device:
     * <p>
     *
     * @param connector - Provides access to the input streaming stream
     * @throws IOException
     * @throws FiscalException
     */
    public void init(AbstractConnector connector) throws Exception {
        //MainActivity.closeMyFiscalDevice();// If reconnect with different device model
        //fiscalDevice = new DatecsFiscalDevice(FPTR_CC_BULGARIA); //Status Bytes description and Exception messages in Bulgarian language
        fiscalDevice = new DatecsFiscalDevice(FPTR_CC_OTHER); //Status Bytes description and Exception messages in English language

        mConnector = connector;

        //Protocol V1 -Identifies all 6 byte status devices !!!
        /**
         *
         *     This instance is intended to determine the model of the connected device.
         *   In the Datecs JavaSDK for each model, the appropriate instance of a device is created for the use of precision settings.*
         *   If communication has already been established and this "connection" is successful, can be used for later communication with the device.
         *   Use:
         *   deviceClassName(datecsBGRmodelV1.getTransportProtocol()); to create fiscal device instance;
         *   To work with a specified device and not to use (FDModelDetectorV1), create the device with a constructor of the type:
         *   deviceClassName(InputStream in, OutputStream out)
         *
         */

        FDModelDetectorV1 datecsBGRmodelV1 = new FDModelDetectorV1(mConnector.getInputStream(), mConnector.getOutputStream());
        modelVendorName = datecsBGRmodelV1.detectConnectedModel();

        /**
         *
         * Datecs JavaSDK exceptions:
         *
         *  Status Bytes exception.
         *   Datecs JavaSDK throws a Status Bytes exception,
         * method initCriticalStatusPrinter() - enables users to select Status Bytes of device
         * to be a critical exception for their application or not.
         * For example, myCriticalStatusSet[0][3]-"No client display connected"
         * may be a critical error in a POS application running in a store but
         * if printer is an office application and no need of this exception must to turned off.
         *
         *
         * Additional description of the error.
         *
         * *    After the execution of each command, the device returns a response or error message in the format:
         * @DescriptionOfError. This option is turned off by default, use the static method:
         * cmdService().setErrorMessageInResponse(true);  to turn it on if necessary.
         * Valid only at DATECS FP-800 / FP-2000 / FP-650 / SK1-21F / SK1-31F/ FMP-10 / FP-550.
         * Use  setThrowErrorMessage(true) to include these messages in the exception. (see example below)
         * The exception messages has the following format: "Status Byte exception + separator + @Description Of Error".
         * @note: Use fiscalDevice.getConnectedPrinterV1().setMsgSeparator(); to setup desired separator char or string,
         * by default separator is "\n\r"
         * All messages are in Bulgarian.
         *
         * Check last command error.
         *     Another way to get the result of execution the methods in Datecs JavaSDK is to check the error
         * of execution of the last command.
         *   Use setCheckLastError(true) to turn On this functionality.  If there is an error executing a method,
         *an exception is thrown: * ErrorCode +separator+ Description Of Error.
         *   Use only on  DATECS FP-800 / FP-2000 / FP-650 / SK1-21F / SK1-31F/ FMP-10 / FP-550.
         *All Descriptions are in English.
         *
         *   The highest priority is the Status Bytes exception.
         *
         *
         **/

        switch (modelVendorName) {
            case "DP-05":
                //Enables whether a status bytes of the device is a critical exception for user application !
                FiscalDeviceV1.setCriticalStatuses(initCriticalStatusECR());
                fiscalDevice.setConnectedModel(new DP05_BGR(datecsBGRmodelV1.getTransportProtocol()));
                break;
            case "DP-15":
                //Enables whether a status bytes of the device is a critical exception for user application !
                FiscalDeviceV1.setCriticalStatuses(initCriticalStatusECR());
                fiscalDevice.setConnectedModel(new DP15_BGR(datecsBGRmodelV1.getTransportProtocol()));
                break;
            case "DP-25":
                //Enables whether a status bytes of the device is a critical exception for user application !
                FiscalDeviceV1.setCriticalStatuses(initCriticalStatusECR());
                fiscalDevice.setConnectedModel(new DP25_BGR(datecsBGRmodelV1.getTransportProtocol()));
                break;
            case "DP-35":
                //Enables whether a status bytes of the device is a critical exception for user application !
                FiscalDeviceV1.setCriticalStatuses(initCriticalStatusECR());
                fiscalDevice.setConnectedModel(new DP35_BGR(datecsBGRmodelV1.getTransportProtocol()));
                break;
            case "WP-50":
                //Enables whether a status bytes of the device is a critical exception for user application !
                FiscalDeviceV1.setCriticalStatuses(initCriticalStatusECR());
                fiscalDevice.setConnectedModel(new WP50_BGR(datecsBGRmodelV1.getTransportProtocol()));
                break;
            case "DP-150":
                //Enables whether a status bytes of the device is a critical exception for user application !
                FiscalDeviceV1.setCriticalStatuses(initCriticalStatusECR());
                fiscalDevice.setConnectedModel(new DP150_BGR(datecsBGRmodelV1.getTransportProtocol()));
                break;
            case "FP-700":
                //Enables whether a status bytes of the device is a critical exception for user application !
                FiscalPrinterV1.setCriticalStatuses(initCriticalStatusPrinter());
                fiscalDevice.setConnectedModel(new FP700_BGR(datecsBGRmodelV1.getTransportProtocol()));
                break;
            case "FP-800":
                //Enables whether a status bytes of the device is a critical exception for user application !
                FiscalPrinterV1.setCriticalStatuses(initCriticalStatusPrinter());
                fiscalDevice.setConnectedModel(new FP800_BGR(datecsBGRmodelV1.getTransportProtocol()));
                break;
            case "FP-650":
                //Enables whether a status bytes of the device is a critical exception for user application !
                FiscalPrinterV1.setCriticalStatuses(initCriticalStatusPrinter());
                fiscalDevice.setConnectedModel(new FP650_BGR(datecsBGRmodelV1.getTransportProtocol()));
                break;
            case "FMP-10":
                //Enables whether a status bytes of the device is a critical exception for user application !
                FiscalPrinterV1.setCriticalStatuses(initCriticalStatusPrinter());
                fiscalDevice.setConnectedModel(new FMP10_BGR(datecsBGRmodelV1.getTransportProtocol()));
                break;
            case "FP-2000":
                //Enables whether a status bytes of the device is a critical exception for user application !
                FiscalPrinterV1.setCriticalStatuses(initCriticalStatusPrinter());
                fiscalDevice.setConnectedModel(new FP2000_BGR(datecsBGRmodelV1.getTransportProtocol()));
                break;
            case "SK1-21F":
                //Enables whether a status bytes of the device is a critical exception for user application !
                FiscalPrinterV1.setCriticalStatuses(initCriticalStatusPrinter());
                fiscalDevice.setConnectedModel(new SK1_21F_BGR(datecsBGRmodelV1.getTransportProtocol()));
                break;
            case "SK1-31F":
                //Enables whether a status bytes of the device is a critical exception for user application !
                FiscalPrinterV1.setCriticalStatuses(initCriticalStatusPrinter());
                fiscalDevice.setConnectedModel(new SK1_31F_BGR(datecsBGRmodelV1.getTransportProtocol()));
                break;
            case "FP-550":
                //Enables whether a status bytes of the device is a critical exception for user application !
                FiscalPrinterV1.setCriticalStatuses(initCriticalStatusPrinter());
                fiscalDevice.setConnectedModel(new FP550_BGR(datecsBGRmodelV1.getTransportProtocol()));
                break;
            default:
                modelVendorName = "Unsupported model:" + modelVendorName;
                break;
        }

    }


    /**
     * Enables whether a status is a critical exception
     *
     * @return
     */
    private boolean[][] initCriticalStatusPrinter() {

        boolean[][] myCriticalStatusSet = new boolean[6][8];
        myCriticalStatusSet[0][7] = false; //"For internal use  1."
        myCriticalStatusSet[0][6] = true;////"Cover is Open"
        myCriticalStatusSet[0][5] = true; // General error - this is OR of all errors marked with #.
        myCriticalStatusSet[0][4] = true;////"# Failure in printing mechanism"
        myCriticalStatusSet[0][3] = false;//"No client display connected"
        myCriticalStatusSet[0][2] = false;//"The real time clock is not synchronized"
        myCriticalStatusSet[0][1] = true; //"# Command code is invalid"
        myCriticalStatusSet[0][0] = true;//"# Syntax error"
        myCriticalStatusSet[1][7] = false;//"For internal use  1"
        myCriticalStatusSet[1][6] = true;//"The built-in tax terminal does not responding"
        myCriticalStatusSet[1][5] = false;//"An non fiscal receipt is opened to print a 90-degree text"
        myCriticalStatusSet[1][4] = false;//"Storno receipt is  Open"
        myCriticalStatusSet[1][3] = false;//"Low battery (Real time clock is RESET)"
        myCriticalStatusSet[1][2] = false;////"RAM Reset is done."
        myCriticalStatusSet[1][1] = true;//"# Command is not permitted"
        myCriticalStatusSet[1][0] = true;//"# Overflow during command execution"
        myCriticalStatusSet[2][7] = false;//"For internal use  1."
        myCriticalStatusSet[2][6] = false;//"EJ nearly full.(only few receipt allowed)"
        myCriticalStatusSet[2][5] = false;//"Nonfiscal receipt is open."
        myCriticalStatusSet[2][4] = false;//"EJ nearly full."
        myCriticalStatusSet[2][3] = false;//"Fiscal receipt is Open."
        myCriticalStatusSet[2][2] = false;//"EJ full."
        myCriticalStatusSet[2][1] = false;//"Near paper end."
        myCriticalStatusSet[2][0] = true;//"#End of paper."
        myCriticalStatusSet[3][7] = false; //"For internal use  1"
        myCriticalStatusSet[3][6] = false; //"Sw7"
        myCriticalStatusSet[3][5] = false; //"Sw6"
        myCriticalStatusSet[3][4] = false; //"Sw5"
        myCriticalStatusSet[3][3] = false; //"Sw4"
        myCriticalStatusSet[3][2] = false; //"Sw3"
        myCriticalStatusSet[3][1] = false; //"Sw2"
        myCriticalStatusSet[3][0] = false; //"Sw1"
        myCriticalStatusSet[4][7] = false;//"For internal use  1"
        myCriticalStatusSet[4][6] = true;//"The printer is overheated"
        myCriticalStatusSet[4][5] = false; //"OR of all errors marked with *  Bytes 4 - 5"
        myCriticalStatusSet[4][4] = true;//"* Fiscal memory is full."
        myCriticalStatusSet[4][3] = false; //"There is space for less then 50 reports in Fiscal memory."
        myCriticalStatusSet[4][2] = false; //"Serial number and number of FM is set"
        myCriticalStatusSet[4][1] = false;//"EIK on BULSTAT is set"
        myCriticalStatusSet[4][0] = true;//"* Error accessing data in the FM"
        myCriticalStatusSet[5][7] = false;//"For internal use  1."
        myCriticalStatusSet[5][6] = false;//"For internal use  0."
        myCriticalStatusSet[5][5] = true; //"Error reading  fiscal memory."
        myCriticalStatusSet[5][4] = false; //"VAT are set at least once."
        myCriticalStatusSet[5][3] = false;//"Device is fiscalized."
        myCriticalStatusSet[5][2] = true; //"The latest fiscal memory record failed."
        myCriticalStatusSet[5][1] = true;//"FM is formated."
        myCriticalStatusSet[5][0] = true;//"The fiscal memory is set in READONLY mode (locked)"
        return myCriticalStatusSet;

    }

    /**
     * Enables whether a status is a critical exception
     *
     * @return
     */
    private boolean[][] initCriticalStatusECR() {

        boolean[][] myCriticalStatusSet = new boolean[6][8];
        myCriticalStatusSet[0][7] = false; //"For internal use  1."
        myCriticalStatusSet[0][6] = false;//"For internal use  0."
        myCriticalStatusSet[0][5] = true; // General error - this is OR of all errors marked with #.
        myCriticalStatusSet[0][4] = false;//""For internal use  0."
        myCriticalStatusSet[0][3] = false;//"No client display connected"
        myCriticalStatusSet[0][2] = false;//"The real time clock is not synchronized"
        myCriticalStatusSet[0][1] = true;//"# Command code is invalid"
        myCriticalStatusSet[0][0] = true;//"# Syntax error"
        myCriticalStatusSet[1][7] = false;//"For internal use  1"
        myCriticalStatusSet[1][6] = true;//"The built-in tax terminal does not responding"
        myCriticalStatusSet[1][5] = false;//"An non fiscal receipt is opened to print a 90-degree text"
        myCriticalStatusSet[1][4] = false;//"Storno receipt is  Open"
        myCriticalStatusSet[1][3] = false;//"For internal use  0."
        myCriticalStatusSet[1][2] = false;//"For internal use  0."
        myCriticalStatusSet[1][1] = true;//"# Command is not permitted"
        myCriticalStatusSet[1][0] = true;//"# Overflow during command execution"
        myCriticalStatusSet[2][7] = false;//"For internal use  1."
        myCriticalStatusSet[2][6] = false;//"EJ nearly full.(only few receipt allowed)"
        myCriticalStatusSet[2][5] = false;//"Nonfiscal receipt is openFiscalReceipt."
        myCriticalStatusSet[2][4] = false;//"EJ nearly full."
        myCriticalStatusSet[2][3] = false;//"Fiscal receipt is Open."
        myCriticalStatusSet[2][2] = false;//"EJ full."
        myCriticalStatusSet[2][1] = false;//"Near paper end."
        myCriticalStatusSet[2][0] = true;//"#End of paper."
        myCriticalStatusSet[3][7] = false; //"For internal use  1"
        myCriticalStatusSet[3][6] = false; //"Sw7"
        myCriticalStatusSet[3][5] = false; //"Sw6"
        myCriticalStatusSet[3][4] = false; //"Sw5"
        myCriticalStatusSet[3][3] = false; //"Sw4"
        myCriticalStatusSet[3][2] = false; //"Sw3"
        myCriticalStatusSet[3][1] = false; //"Sw2"
        myCriticalStatusSet[3][0] = false; //"Sw1"
        myCriticalStatusSet[4][7] = false;//"For internal use  1"
        myCriticalStatusSet[4][6] = true;//"The printer is overheated"
        myCriticalStatusSet[4][5] = false; //"OR of all errors marked with *  Bytes 4 - 5"
        myCriticalStatusSet[4][4] = true;//"* Fiscal memory is full."
        myCriticalStatusSet[4][3] = false; //"There is space for less then 50 reports in Fiscal memory."
        myCriticalStatusSet[4][2] = false; //"Serial number and number of FM is set"
        myCriticalStatusSet[4][1] = false;//"EIK on BULSTAT is set"
        myCriticalStatusSet[4][0] = true;//"* Error accessing data in the FM"
        myCriticalStatusSet[5][7] = false;//"For internal use  1."
        myCriticalStatusSet[5][6] = false;//"For internal use  0."
        myCriticalStatusSet[5][5] = true; //"Error reading  fiscal memory."
        myCriticalStatusSet[5][4] = false; //"VAT are not set at least once."
        myCriticalStatusSet[5][3] = false;//"Device is not fiscalized."
        myCriticalStatusSet[5][2] = true; //"The latest fiscal memory record failed."
        myCriticalStatusSet[5][1] = true;//"FM is not formated."
        myCriticalStatusSet[5][0] = true;//"The fiscal memory is set in READONLY mode (locked)"
        return myCriticalStatusSet;

    }

    public void close() {
        if (mConnector != null) {
            try {
                mConnector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
