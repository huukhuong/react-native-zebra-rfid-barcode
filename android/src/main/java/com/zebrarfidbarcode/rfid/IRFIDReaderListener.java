package com.citek.maison.scanner.rfid;

public interface IRFIDReaderListener {
    void onRFIDRead(String tag);
}
