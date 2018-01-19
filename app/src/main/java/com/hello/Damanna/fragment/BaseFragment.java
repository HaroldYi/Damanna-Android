package com.hello.Damanna.fragment;

import android.support.v4.app.Fragment;

import com.hello.Damanna.common.BaseApplication;

/**
 * Created by TedPark on 2017. 3. 18..
 */

public class BaseFragment extends Fragment {

    public void progressON() {
        BaseApplication.getInstance().progressON(getActivity(), null);
    }

    public void progressON(String message) {
        BaseApplication.getInstance().progressON(getActivity(), message);
    }

    public void progressOFF() {
        BaseApplication.getInstance().progressOFF();
    }

}
