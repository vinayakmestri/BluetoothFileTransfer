package com.vinos.bluetoothfiletransfer.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.vinos.bluetoothfiletransfer.R;

import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    public interface DeviceSelectionListener {
        void onDeviceSelected(BluetoothDevice bluetoothDevice);
    }

    private List<BluetoothDevice> deviceList;
    private DeviceSelectionListener onDeviceSelectionListener;

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView
     */
    public BluetoothDeviceAdapter(List<BluetoothDevice> dataSet, DeviceSelectionListener onDeviceSelectionListener) {
        this.deviceList = dataSet;
        this.onDeviceSelectionListener = onDeviceSelectionListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_device_item, viewGroup, false);

        return new ViewHolder(view);
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView deviceName;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            deviceName = (TextView) view.findViewById(R.id.deviceName);
            view.setOnClickListener(this);
        }

        public TextView getTextView() {
            return deviceName;
        }

        @Override
        public void onClick(View v) {
           int index =  getAdapterPosition();
            onDeviceSelectionListener.onDeviceSelected(deviceList.get(index));
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTextView().setText(deviceList.get(position).getName());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return deviceList.size();
    }
}
