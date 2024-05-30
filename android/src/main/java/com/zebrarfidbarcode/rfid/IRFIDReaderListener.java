package com.zebrarfidbarcode.rfid;

import java.util.ArrayList;

public interface IRFIDReaderListener {
    void onRFIDRead(ArrayList<String> listRfid);
}
