package com.example.mz23zx.deltaerpddrapk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListAdapter extends BaseAdapter {
    private ArrayList<ListItem> listData;
    private LayoutInflater layoutInflater;
    public CustomListAdapter(Context aContext, ArrayList<ListItem> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
    }
    @Override
    public int getCount() {
        return listData.size();
    }
    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View v, ViewGroup vg) {
        ViewHolder holder;
        if (v == null) {
            v = layoutInflater.inflate(R.layout.list_row, null);
            holder = new ViewHolder();
            holder.uPartnumber = (TextView) v.findViewById(R.id.partnumber_lbl);
            holder.uKanban = (TextView) v.findViewById(R.id.kanban_lbl);
            holder.uLocation = (TextView) v.findViewById(R.id.location_lbl);
            holder.uDescription = (TextView) v.findViewById(R.id.description_lbl);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.uPartnumber.setText(listData.get(position).getPartnumber());
        holder.uKanban.setText(listData.get(position).getKanban());
        holder.uLocation.setText(listData.get(position).getLocation());
        holder.uDescription.setText(listData.get(position).getDescription());
        return v;
    }
    static class ViewHolder {
        TextView uPartnumber;
        TextView uKanban;
        TextView uLocation;
        TextView uDescription;
    }
}
