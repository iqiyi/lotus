package com.iqiyi.datareact.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.iqiyi.datareact.cons.DataReactType;

import org.iqiyi.datareact.Data;
import org.iqiyi.datareact.DataReact;

import datareact.iqiyi.com.lotus.R;

/**
 * Created by liangxu on 2018/3/15.
 */

public class CommonTestActivity extends FragmentActivity {
    private EditText mEtData;
    private Button mBtnSend;
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_test);
        initView();
        initListener();
    }

    private void initListener() {
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable text = mEtData.getText();
                if(!TextUtils.isEmpty(text)){
                    DataReact.set(new Data(DataReactType.DATA_TEST_COMMON).setData(text.toString()));
                    finish();
                }else {
                    Toast.makeText(CommonTestActivity.this, "数据为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initView() {
        mBtnSend = findViewById(R.id.btn_send_and_close);
        mEtData = findViewById(R.id.et_test_data);
    }
}
