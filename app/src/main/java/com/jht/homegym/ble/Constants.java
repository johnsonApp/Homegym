/*
 * Copyright 2015 Junk Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jht.homegym.ble;

public interface Constants {
    //Connection state
    int STATE_DISCONNECTED = 0;
    int STATE_CONNECTING = 1;
    int STATE_CONNECTED = 2;
    int STATE_DISCONNECTING = 3;
    int STATE_SCAN_FINISH = 4;

    //Action
    String ACTION_GATT_DISCONNECTED = "com.jht.homegym.ble.ACTION_GATT_DISCONNECTED";
    String ACTION_GATT_CONNECTING = "com.jht.homegym.ble.ACTION_GATT_CONNECTING";
    String ACTION_GATT_CONNECTED = "com.jht.homegym.ble.ACTION_GATT_CONNECTED";
    String ACTION_GATT_DISCONNECTING = "com.jht.homegym.ble.ACTION_GATT_DISCONNECTING";
    String ACTION_GATT_SERVICES_DISCOVERED = "com.jht.homegym.ble.ACTION_GATT_SERVICES_DISCOVERED";
    String ACTION_BLUETOOTH_DEVICE = "com.jht.homegym.ble.ACTION_BLUETOOTH_DEVICE";
    String ACTION_SCAN_FINISHED = "com.jht.homegym.ble.ACTION_SCAN_FINISHED";
}
