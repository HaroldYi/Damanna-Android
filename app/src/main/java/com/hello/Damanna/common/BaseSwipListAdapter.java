package com.hello.Damanna.common;

import android.widget.BaseAdapter;

/**
 * Created by lji5317 on 07/12/2017.
 */

public abstract class BaseSwipListAdapter extends BaseAdapter {

    public boolean getSwipEnableByPosition(int position){
        return true;
    }
}