# react-native-zebra-rfid-barcode-ef-tech

This React Native module enables seamless integration with Zebra RFID readers and barcode scanners.

## Demo

https://github.com/huukhuong/react-native-zebra-rfid-barcode/assets/78204178/a54c4616-76ce-48da-86cb-6ceaab1beeef

## Installation

```sh
npm install react-native-zebra-rfid-barcode-ef-tech
yarn add react-native-zebra-rfid-barcode-ef-tech
```

## Bluetooth Permissions (Android >= 13)
In AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
```
In MainActivity
```kotlin
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    requestBluetoothPermissions();
  }
}

@RequiresApi(Build.VERSION_CODES.S)
private fun requestBluetoothPermissions() {
  val permissions = arrayOf(
    Manifest.permission.BLUETOOTH_CONNECT,
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.BLUETOOTH_ADVERTISE
  )

  val permissionToRequest = permissions.filter {
    ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
  }

  if (permissionToRequest.isNotEmpty()) {
    ActivityCompat.requestPermissions(
      this,
      permissionToRequest.toTypedArray(),
      1
    )
  }
}
```

## Import

```js
import {
  ZebraEvent,
  ZebraEventEmitter,
  getAllDevices,
  connectToDevice,
  type ZebraResultPayload,
  type ZebraRfidResultPayload,
} from 'react-native-zebra-rfid-barcode-ef-tech';

// ...
```

## Get All RFID Devices

```js
const listDevices = await getAllDevices();
// will return array of devices names like ["RFD+19189414", "RFD+018134912"]
```

## Connect to device

```jsx
useEffect(() => {
  const deviceConnectEvent = ZebraEventEmitter.addListener(
    ZebraEvent.ON_DEVICE_CONNECTED,
    (e: ZebraResultPayload) => {
      console.log(e.data); // "Connect successfully" || "Connect failed"
    }
  );

  return () => {
    deviceConnectEvent.remove();
  };
}, []);

//...

<TouchableOpacity onPress={() => connectToDevice(deviceName)}>
  <Text>{deviceName}</Text>
</TouchableOpacity>
```

## Barcode Scanner

```js
useEffect(() => {
  const barcodeEvent = ZebraEventEmitter.addListener(
    ZebraEvent.ON_BARCODE,
    (e: ZebraResultPayload) => {
      handleBarcodeEvent(e.data); // string
    }
  );

  return () => {
    barcodeEvent.remove();
  };
}, []);

const handleBarcodeEvent = useCallback(
  debounce((newData: string) => {
    setListBarcodes((pre) => [...pre, newData]);
  }, 200),
  []
);
```

## RFID Reader

```js
useEffect(() => {
  const rfidEvent = ZebraEventEmitter.addListener(
    ZebraEvent.ON_RFID,
    (e: ZebraRfidResultPayload) => {
      handleRfidEvent(e.data); // array of string tags
    }
  );

  return () => {
    rfidEvent.remove();
  };
}, []);

const handleRfidEvent = useCallback(
  debounce((newData: string[]) => {
    setListRfid((pre) => [...pre, ...newData]);
  }, 200),
  []
);
```

## Full code example

```jsx
import React, { useCallback, useEffect, useState } from 'react';
import debounce from 'lodash/debounce';
import {
  FlatList,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import {
  ZebraEvent,
  ZebraEventEmitter,
  connectToDevice,
  getAllDevices,
  type ZebraResultPayload,
  type ZebraRfidResultPayload,
} from 'react-native-zebra-rfid-barcode-ef-tech';

export default function App() {
  const [listDevices, setListDevices] = useState<string[]>([]);
  const [listBarcodes, setListBarcodes] = useState<string[]>([]);
  const [listRfid, setListRfid] = useState<string[]>([]);

  useEffect(() => {
    getListRfidDevices();

    const barcodeEvent = ZebraEventEmitter.addListener(
      ZebraEvent.ON_BARCODE,
      (e: ZebraResultPayload) => {
        handleBarcodeEvent(e.data);
      }
    );

    const rfidEvent = ZebraEventEmitter.addListener(
      ZebraEvent.ON_RFID,
      (e: ZebraRfidResultPayload) => {
        handleRfidEvent(e.data);
      }
    );

    const deviceConnectEvent = ZebraEventEmitter.addListener(
      ZebraEvent.ON_DEVICE_CONNECTED,
      (e: ZebraResultPayload) => {
        console.log(e.data); // "Connect successfully" || "Connect failed"
      }
    );

    return () => {
      barcodeEvent.remove();
      rfidEvent.remove();
      deviceConnectEvent.remove();
    };
  }, []);

  const handleRfidEvent = useCallback(
    debounce((newData: string[]) => {
      setListRfid((pre) => [...pre, ...newData]);
    }, 200),
    []
  );

  const handleBarcodeEvent = useCallback(
    debounce((newData: string) => {
      setListBarcodes((pre) => [...pre, newData]);
    }, 200),
    []
  );

  const getListRfidDevices = async () => {
    const listDevices = await getAllDevices();
    setListDevices(listDevices);
  };

  return (
    <View style={styles.container}>
      <View
        style={{
          maxHeight: 200,
        }}
      >
        <Text style={[styles.text, styles.title]}>
          Devices: {listDevices.length}
        </Text>
        <FlatList
          style={{ backgroundColor: '#FEF3C7' }}
          ItemSeparatorComponent={() => <View style={styles.separator} />}
          data={listDevices}
          renderItem={({ item }) => (
            <TouchableOpacity
              onPress={() => connectToDevice(item)}
              style={styles.item}
            >
              <Text style={styles.text}>{item}</Text>
            </TouchableOpacity>
          )}
        />
      </View>

      <View style={styles.partial}>
        <Text style={[styles.text, styles.title]}>
          Barcodes: {listBarcodes.length}
        </Text>
        <FlatList
          style={{ backgroundColor: '#DCFCE7' }}
          ItemSeparatorComponent={() => <View style={styles.separator} />}
          data={listBarcodes}
          renderItem={({ item }) => (
            <View style={styles.item}>
              <Text style={styles.text}>{item}</Text>
            </View>
          )}
        />
      </View>

      <View style={styles.partial}>
        <Text style={[styles.text, styles.title]}>
          RFIDs: {listRfid.length}
        </Text>
        <FlatList
          style={{ backgroundColor: '#E0F2FE', marginBottom: 10 }}
          ItemSeparatorComponent={() => <View style={styles.separator} />}
          data={listRfid}
          renderItem={({ item }) => (
            <View style={styles.item}>
              <Text style={styles.text}>{item}</Text>
            </View>
          )}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingHorizontal: 16,
    backgroundColor: 'white',
  },
  partial: {
    flex: 1,
  },
  item: {
    height: 50,
    paddingHorizontal: 15,
    justifyContent: 'center',
  },
  separator: {
    height: 1,
    backgroundColor: '#ccc',
  },
  text: {
    color: '#333',
  },
  title: {
    fontWeight: 'bold',
    fontSize: 16,
    marginVertical: 5,
  },
});

```

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
