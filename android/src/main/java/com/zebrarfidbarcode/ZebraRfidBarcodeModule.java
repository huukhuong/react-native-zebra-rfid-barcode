package com.zebrarfidbarcode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebrarfidbarcode.barcode.BarcodeScannerInterface;
import com.zebrarfidbarcode.barcode.IBarcodeScannedListener;
import com.zebrarfidbarcode.rfid.IRFIDReaderListener;
import com.zebrarfidbarcode.rfid.RFIDReaderInterface;

import java.util.ArrayList;
import java.util.List;

@ReactModule(name = ZebraRfidBarcodeModule.NAME)
public class ZebraRfidBarcodeModule extends ReactContextBaseJavaModule implements IRFIDReaderListener, IBarcodeScannedListener {
  public static final String NAME = "ZebraRfidBarcode";
  private RFIDReaderInterface rfidInterface;
  private BarcodeScannerInterface scannerInterface;
  private List<DCSScannerInfo> availableScannerList;
  private final String ON_DEVICE_CONNECTED = "onZebraConnected";
  private final String ON_RFID = "onZebraRFIDReaded";
  private final String ON_BARCODE = "onZebraBarcodeScanned";

  public ZebraRfidBarcodeModule(ReactApplicationContext reactContext) {
    super(reactContext);
    configureDevice();
  }

  private void configureDevice() {
    if (scannerInterface == null) {
      scannerInterface = new BarcodeScannerInterface(this);
    }
    availableScannerList = scannerInterface.getAvailableScanners(getReactApplicationContext());
  }

  private void configureScanner(int scannerID, String scannerName) {
    Thread thread = new Thread(() -> {
      scannerInterface.connectToScanner(scannerID);
      configureRFID(scannerName);
      sendConnectStatus(true);
    });
    thread.start();
  }

  private void configureRFID(String name) {
    if (rfidInterface == null)
      rfidInterface = new RFIDReaderInterface(this);
    rfidInterface.connect(getReactApplicationContext(), name);
  }

  @ReactMethod
  public void getAllDevices(Promise promise) {
    try {
      WritableArray listDevices = Arguments.createArray();
      for (DCSScannerInfo scannerInfo : availableScannerList) {
        listDevices.pushString(scannerInfo.getScannerName());
      }
      promise.resolve(listDevices);
    } catch (Exception e) {
      promise.reject("Error", e);
    }
  }

  @ReactMethod
  public void connectToDevice(String deviceName) {
    if (scannerInterface == null) {
      scannerInterface = new BarcodeScannerInterface(this);
      availableScannerList = scannerInterface.getAvailableScanners(getReactApplicationContext());
    }

    for (int i = 0; i < availableScannerList.size(); i++) {
      DCSScannerInfo scanner = availableScannerList.get(i);
      if (scanner.getScannerName().equals(deviceName)) {
        configureScanner(scanner.getScannerID(), scanner.getScannerName());
      }
    }
  }

  @ReactMethod
  public void sendConnectStatus(boolean isConnected) {
    WritableMap params = Arguments.createMap();
    params.putString("data", isConnected ? "Connect successfully" : "Connect failed");
    sendEvent(getReactApplicationContext(), ON_DEVICE_CONNECTED, params);
  }

  @ReactMethod
  public void sendRFID(ArrayList<String> listRfid) {
    WritableMap params = Arguments.createMap();

    WritableArray writableArray = Arguments.createArray();
    for (String rfid : listRfid) {
      writableArray.pushString(rfid);
    }

    params.putArray("data", writableArray);
    sendEvent(getReactApplicationContext(), ON_RFID, params);
  }

  @ReactMethod
  public void sendBarcode(String barcode) {
    WritableMap params = Arguments.createMap();
    params.putString("data", barcode);
    sendEvent(getReactApplicationContext(), ON_BARCODE, params);
  }

  @Override
  public void onRFIDRead(ArrayList<String> listRfid) {
    sendRFID(listRfid);
  }

  @Override
  public void onBarcodeScanned(String barcode) {
    sendBarcode(barcode);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
  }

  @ReactMethod
  public void addListener(String eventName) {
  }

  @ReactMethod
  public void removeListeners(Integer count) {
  }
}
