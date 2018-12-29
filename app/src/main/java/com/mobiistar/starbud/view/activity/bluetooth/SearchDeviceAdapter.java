package com.mobiistar.starbud.view.activity.bluetooth;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobiistar.starbud.R;
import com.mobiistar.starbud.util.BluetoothUtil;
import com.mobiistar.starbud.util.LogUtil;
import com.mobiistar.starbud.util.StarUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Date：18-11-6-下午3:05
 * Author: black
 */
public class SearchDeviceAdapter extends BaseAdapter {
    private List<BluetoothDevice> mDeviceList;
    private Context mContext;
    private BluetoothHeadset mHeadset;
    private BluetoothA2dp mA2dp;

    public SearchDeviceAdapter(Context context) {
        mDeviceList = new ArrayList<>();
        mContext = context;
    }


    @Override
    public int getCount() {
        return mDeviceList.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return mDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.search_device_item_lay, null);
            holder = new Holder();
            holder.tv_name = convertView.findViewById(R.id.tv_name);
            holder.tv_state = convertView.findViewById(R.id.tv_state);
            holder.iv_config = convertView.findViewById(R.id.iv_config);
            holder.iv_pairing = convertView.findViewById(R.id.iv_pairing);
            holder.iv_connecting = convertView.findViewById(R.id.iv_connecting);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        final BluetoothDevice device = getItem(position);
        holder.tv_name.setText(BluetoothUtil.getAliasName(device));
        holder.tv_state.setText(R.string.connected);
        int visibility = BluetoothUtil.isConnected(device, mA2dp, mHeadset) ? View.VISIBLE : View.GONE;
        holder.tv_state.setVisibility(visibility);
        int connectionState = BluetoothUtil.getConnectionStateEx(device, mA2dp, mHeadset);
        updateVisibility(holder, device.getBondState(), connectionState);
        holder.iv_config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PairedDeviceActivity.class);
                intent.putExtra(StarUtil.KEY_BLUETOOTH_DEVICE, device);
                mContext.startActivity(intent);
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealItemClick(device);
            }
        });
        return convertView;
    }

    private void dealItemClick(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        int bondState = device.getBondState();
        if (bondState == BluetoothDevice.BOND_BONDED) {
            int connectedStateA2dp = BluetoothUtil.getConnectionState(device, mA2dp);
            int connectedStateHeadset = BluetoothUtil.getConnectionState(device, mHeadset);
            // Both connected, disconnect; other connect.
            if (connectedStateA2dp == BluetoothProfile.STATE_CONNECTED &&
                    connectedStateHeadset == BluetoothProfile.STATE_CONNECTED) {
                BluetoothUtil.disconnectDevice(device, mA2dp);
                BluetoothUtil.disconnectDevice(device, mHeadset);
            } else {
                if (connectedStateA2dp == BluetoothProfile.STATE_DISCONNECTED) {
                    BluetoothUtil.connectDevice(device, mA2dp);
                }
                if (connectedStateHeadset == BluetoothProfile.STATE_DISCONNECTED) {
                    BluetoothUtil.connectDevice(device, mHeadset);
                }
            }
        } else if (bondState == BluetoothDevice.BOND_NONE) {
            device.createBond();
        }
    }

    private final class Holder {
        TextView tv_name;
        TextView tv_state;
        ImageView iv_config;
        ImageView iv_pairing;
        ImageView iv_connecting;
    }

    private void updateVisibility(Holder holder, int bondState, int connectionState) {
        printLogE("updateVisibility...updateVisibility...connectionState==" + connectionState);
        if (holder == null) {
            return;
        }

        if (bondState == BluetoothDevice.BOND_BONDED) {
            holder.iv_config.setVisibility(View.VISIBLE);
            holder.iv_pairing.setVisibility(View.GONE);
        } else if (bondState == BluetoothDevice.BOND_NONE) {
            holder.iv_config.setVisibility(View.GONE);
            holder.iv_pairing.setVisibility(View.GONE);
        } else {
            holder.iv_config.setVisibility(View.GONE);
            holder.iv_pairing.setVisibility(View.VISIBLE);
            StarUtil.startAnimation(holder.iv_pairing, R.drawable.anim_connecting_icon);
        }

        if (connectionState == BluetoothProfile.STATE_CONNECTING) {
            holder.iv_connecting.setVisibility(View.VISIBLE);
            StarUtil.startAnimation(holder.iv_connecting, R.drawable.anim_connecting_icon);
        } else if (connectionState == BluetoothProfile.STATE_DISCONNECTING) {
            holder.iv_connecting.setVisibility(View.VISIBLE);
            StarUtil.startAnimation(holder.iv_connecting, R.drawable.anim_disconnecting_icon);
        } else {
            holder.iv_connecting.setVisibility(View.GONE);
        }
    }

    protected void printLogE(String logContent) {
        LogUtil.printLog(Log.ERROR, getClass().getSimpleName(), logContent);
    }

    public void setData(List<BluetoothDevice> deviceList) {
        mDeviceList = deviceList != null ? deviceList : new ArrayList<BluetoothDevice>();
        notifyDataSetChanged();
    }

    public void addDevice(BluetoothDevice device) {
        if (device == null || device.getAddress() == null) {
            return;
        }
        for (BluetoothDevice tempDevice : mDeviceList) {
            if (tempDevice != null && device.getAddress().equalsIgnoreCase(tempDevice.getAddress())) {
                mDeviceList.remove(tempDevice);
                break;
            }
        }
        mDeviceList.add(device);
        notifyDataSetChanged();
    }

    public void removeDevice(BluetoothDevice device) {
        if (device == null || device.getAddress() == null) {
            return;
        }
        boolean needChange = false;
        for (BluetoothDevice tempDevice : mDeviceList) {
            if (tempDevice != null && device.getAddress().equalsIgnoreCase(tempDevice.getAddress())) {
                mDeviceList.remove(tempDevice);
                needChange = true;
                break;
            }
        }
        if (needChange) {
            notifyDataSetChanged();
        }
    }

    public void setBluetoothProfile(BluetoothHeadset headset, BluetoothA2dp a2dp) {
        mHeadset = headset;
        mA2dp = a2dp;
    }
}
