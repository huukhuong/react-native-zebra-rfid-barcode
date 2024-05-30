import React, { useCallback, useEffect, useState } from 'react';

import {
  StyleSheet,
  View,
  Text,
  FlatList,
  TouchableOpacity,
} from 'react-native';
import {
  ZebraEvent,
  ZebraEventEmitter,
  getAllDevices,
  connectToDevice,
  type ZebraResultPayload,
  type ZebraRfidResultPayload,
} from 'react-native-zebra-rfid-barcode';
import debounce from 'lodash/debounce';

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
