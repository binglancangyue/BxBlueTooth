package com.bixin.bluetooth.model.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bixin.bluetooth.R;
import com.bixin.bluetooth.model.bean.PhoneBook;
import com.bixin.bluetooth.model.listener.RecyclerViewOnItemListener;

import java.util.ArrayList;

public class ContactsRecyclerViewAdapter extends RecyclerView.Adapter<ContactsRecyclerViewAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<PhoneBook> mData;
    private LayoutInflater mLayoutInflater;
    private RecyclerViewOnItemListener mItemListener;
    private int itemCount = 0;

    public ContactsRecyclerViewAdapter(Context context, ArrayList<PhoneBook> books) {
        this.mContext = context;
        this.mData = books;
        this.mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void setLayoutItemCount(int count) {
        this.itemCount = count;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mLayoutInflater.inflate(R.layout.recycleview_item_contacts, parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (mData != null && mData.size() > 0) {
            PhoneBook phoneBook = mData.get(position);
            holder.tvName.setText(phoneBook.getName());
            holder.tvNumber.setText(phoneBook.getNumber());
        }
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            if (mData.size() > itemCount) {
                return mData.size();
            } else {
                return itemCount;
            }
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setRecyclerViewOnItemListener(RecyclerViewOnItemListener listener) {
        this.mItemListener = listener;
    }

    public void updateData(ArrayList<PhoneBook> data) {
        this.mData = data;
    }

    final class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImg;
        TextView tvName;
        TextView tvNumber;
        private View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemListener != null) {
                    int index = getAdapterPosition();
                    if (mData.size()>0){
                        //xRecyclerView 自带头部和尾部,position=position-1;
                        mItemListener.onItemClick(index, mData.get(index - 1));
                    }
                }
            }
        };
        private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mItemListener != null) {
                    mItemListener.onItemLongClick(getAdapterPosition() - 1);
                }
                return true;
            }
        };


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImg = itemView.findViewById(R.id.iv_people);
            tvName = itemView.findViewById(R.id.tv_name);
            tvNumber = itemView.findViewById(R.id.tv_number);
            itemView.setOnClickListener(mOnClickListener);
//            ivImg.setOnLongClickListener(mOnLongClickListener);
        }
    }
}
