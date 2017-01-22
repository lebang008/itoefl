package com.iyuce.itoefl.UI.Main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iyuce.itoefl.R;
import com.iyuce.itoefl.Utils.LogUtil;

/**
 * Created by LeBang on 2017/1/22
 */
public class FragmentExercise extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);
        LogUtil.i("FragmentExercise");
        return view;
    }
}