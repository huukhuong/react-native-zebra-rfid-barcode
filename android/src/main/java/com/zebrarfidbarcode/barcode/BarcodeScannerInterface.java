package com.zebrarfidbarcode.barcode;

import android.content.Context;

import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;
import com.zebra.scannercontrol.SDKHandler;

import java.util.ArrayList;

public class BarcodeScannerInterface implements IDcsSdkApiDelegate {
    private IBarcodeScannedListener listener;
    private SDKHandler sdkHandler;
    private ArrayList<DCSScannerInfo> scannerInfoList = new ArrayList<>();

    public BarcodeScannerInterface(IBarcodeScannedListener listener) {
        this.listener = listener;
    }

    public ArrayList<DCSScannerInfo> getAvailableScanners(Context context) {
        if (sdkHandler == null) {
            sdkHandler = new SDKHandler(context);
        }

        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);
        sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_USB_CDC);

        sdkHandler.dcssdkSetDelegate(this);
        int notificationsMask = 0;
        notificationsMask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value;
        notificationsMask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value;
        notificationsMask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value;
        notificationsMask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value;
        notificationsMask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value;

        // Subscribe to events set in notification mask
        sdkHandler.dcssdkSubsribeForEvents(notificationsMask);
        sdkHandler.dcssdkEnableAvailableScannersDetection(true);

        scannerInfoList.clear();
        sdkHandler.dcssdkGetAvailableScannersList(scannerInfoList);
        return scannerInfoList;
    }

    public boolean connectToScanner(int scannerID) {
        try {
            DCSScannerInfo scanner = null;
            for (DCSScannerInfo info : scannerInfoList) {
                if (info.getScannerID() == scannerID) {
                    scanner = info;
                    break;
                }
            }
            if (scanner != null && scanner.isActive()) {
                return true;
            }

            // Connect
            DCSSDKDefs.DCSSDK_RESULT result = sdkHandler.dcssdkEstablishCommunicationSession(scannerID);
            return result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS;
        } catch (Exception e) {
            return false;
        }
    }

    public void onDestroy() {
        try {
            if (sdkHandler != null) {
                sdkHandler = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dcssdkEventScannerAppeared(DCSScannerInfo p0) {
    }

    @Override
    public void dcssdkEventScannerDisappeared(int p0) {
    }

    @Override
    public void dcssdkEventCommunicationSessionEstablished(DCSScannerInfo p0) {
    }

    @Override
    public void dcssdkEventCommunicationSessionTerminated(int p0) {
    }

    @Override
    public void dcssdkEventBarcode(byte[] p0, int p1, int p2) {
        String barcode = new String(p0);
        listener.onBarcodeScanned(barcode);
    }

    @Override
    public void dcssdkEventImage(byte[] p0, int p1) {
    }

    @Override
    public void dcssdkEventVideo(byte[] p0, int p1) {
    }

    @Override
    public void dcssdkEventBinaryData(byte[] p0, int p1) {
    }

    @Override
    public void dcssdkEventFirmwareUpdate(FirmwareUpdateEvent p0) {
    }

    @Override
    public void dcssdkEventAuxScannerAppeared(DCSScannerInfo p0, DCSScannerInfo p1) {
    }
}


