package com.example.puneeth.compositor;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 *
 */
 class CustomBaseAdapter extends BaseAdapter {
    Context context;
    private List<RowItem> rowItems;

    CustomBaseAdapter(Context context, List<RowItem> items){
        this.context=context;
        this.rowItems=items;
    }

    private class ViewHolder{
        TextView compostId,compostName,sellerName,cost,weight,postedOn,contactSeller;
        ImageView imageView;
    }

    public View getView (int position, View convertView, ViewGroup parent){
        ViewHolder holder;
        LayoutInflater mInflater = (LayoutInflater)
                context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_interface, null);
            holder = new ViewHolder();
            holder.compostId =   convertView.findViewById(R.id.BuyerItemId);
            holder.compostName =   convertView.findViewById(R.id.BuyerItemName);
            holder.sellerName= convertView.findViewById(R.id.ItemSellernameBuyer);
            holder.cost= convertView.findViewById(R.id.ItemCostBuyer);
            holder.weight= convertView.findViewById(R.id.ItemWeightBuyer);
            holder.postedOn= convertView.findViewById(R.id.BuyerPostedDate);
            holder.contactSeller= convertView.findViewById(R.id.sellerContact);
            holder.imageView = convertView.findViewById(R.id.StoreImage);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        RowItem rowItem=(RowItem)getItem(position);
        holder.compostId.setText(rowItem.getCompostId());
        holder.compostName.setText(rowItem.getCompostName());
        holder.sellerName.setText(rowItem.getSellerName());
        holder.cost.setText(rowItem.getCost());
        holder.weight.setText(rowItem.getWeight());
        holder.postedOn.setText(rowItem.getDate());
        holder.contactSeller.setText(rowItem.getContact());
        holder.imageView.setImageBitmap(rowItem.getImage());
        return convertView;
    }

    @Override
    public int getCount() {
        return rowItems==null?0:rowItems.size();
    }

    @Override
    public Object getItem(int position) {
        return rowItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return rowItems.indexOf(getItem(position));
    }
}
