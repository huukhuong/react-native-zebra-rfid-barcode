package com.zebrarfidbarcode.rfid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.TagDataArray;
import com.zebra.rfid.api3.TriggerInfo;

import java.util.ArrayList;

public class RFIDReaderInterface implements RfidEventsListener {
  private IRFIDReaderListener listener;

  private final String TAG = "RFIDReaderIml";
  private Readers readers;
  private ArrayList<ReaderDevice> availableRFIDReaderList;
  public static ReaderDevice readerDevice;
  private RFIDReader reader;

  public RFIDReaderInterface(IRFIDReaderListener listener) {
    this.listener = listener;
  }

  public ArrayList<ReaderDevice> getAvailableReaders() {
    try {
      return readers.GetAvailableRFIDReaderList();
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  public void connect(Context context, String scannerName) {
    // Init Readers
    readers = new Readers(context, ENUM_TRANSPORT.ALL);
    try {
      availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
      if (availableRFIDReaderList != null && !availableRFIDReaderList.isEmpty()) {
        // get first reader from list
        for (ReaderDevice rfid : availableRFIDReaderList) {
          if (rfid.getName().equals(scannerName)) {
            readerDevice = rfid;
            reader = readerDevice.getRFIDReader();
            if (!reader.isConnected()) {
              reader.connect();
              configureReader();
            }
          }
        }
      }
    } catch (InvalidUsageException | OperationFailureException e) {
      e.printStackTrace();
    }
  }

  private void configureReader() {
    if (reader.isConnected()) {
      TriggerInfo triggerInfo = new TriggerInfo();
      triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
      triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
      try {
        // receive events from reader
        reader.Events.addEventsListener(this);
        // HH event
        reader.Events.setHandheldEvent(true);
        // tag event with tag data
        reader.Events.setTagReadEvent(true);
        // application will collect tag using getReadTags API
        reader.Events.setAttachTagDataWithReadEvent(false);
        // set start and stop triggers
        reader.Config.setStartTrigger(triggerInfo.StartTrigger);
        reader.Config.setStopTrigger(triggerInfo.StopTrigger);
      } catch (InvalidUsageException | OperationFailureException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void eventReadNotify(RfidReadEvents rfidReadEvents) {
    // Each access belong to a tag.
    // Therefore, as we are performing an access sequence on 3 Memory Banks, each tag could be reported 3 times
    // Each tag data represents a memory bank
    TagDataArray readTags = reader.Actions.getReadTagsEx(100);
    if (readTags != null) {
      ArrayList<String> listTags = new ArrayList<>();
      for (TagData myTag : readTags.getTags()) {
        String tagID = myTag.getTagID();

        if (tagID != null) {
          listTags.add(tagID);
        }
      }
      listener.onRFIDRead(listTags);
    }
  }

  @SuppressLint("StaticFieldLeak")
  public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
    Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());
    if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
      if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
        new AsyncTask<Void, Void, Void>() {
          @Override
          protected Void doInBackground(Void... voids) {
            try {
              reader.Actions.Inventory.perform();
            } catch (InvalidUsageException | OperationFailureException e) {
              e.printStackTrace();
            }
            return null;
          }
        }.execute();
      }
      if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
        new AsyncTask<Void, Void, Void>() {
          @Override
          protected Void doInBackground(Void... voids) {
            try {
              reader.Actions.Inventory.stop();
            } catch (InvalidUsageException | OperationFailureException e) {
              e.printStackTrace();
            }
            return null;
          }
        }.execute();
      }
    }
  }

  private String getMemBankData(String memoryBankData, ACCESS_OPERATION_STATUS opStatus) {
    return (opStatus != ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) ?
      opStatus.toString()
      :
      memoryBankData;
  }

  public void onDestroy() {
    try {
      reader.Events.removeEventsListener(this);
      reader.disconnect();
      reader.Dispose();
      readers.Dispose();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
