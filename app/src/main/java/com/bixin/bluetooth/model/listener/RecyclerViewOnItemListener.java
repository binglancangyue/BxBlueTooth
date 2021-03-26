package com.bixin.bluetooth.model.listener;

import com.bixin.bluetooth.model.bean.PhoneBook;

public interface RecyclerViewOnItemListener {
    void onItemClick(int position, PhoneBook book);

    void onItemLongClick(int position);
}
