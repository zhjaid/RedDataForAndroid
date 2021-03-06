package com.zhjaid.reddata.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.qmuiteam.qmui.widget.textview.QMUILinkTextView;
import com.rmondjone.locktableview.DisplayUtil;
import com.rmondjone.locktableview.LockTableView;
import com.rmondjone.xrecyclerview.ProgressStyle;
import com.rmondjone.xrecyclerview.XRecyclerView;
import com.zhjaid.reddata.BaseActivity;
import com.zhjaid.reddata.R;
import com.zhjaid.reddata.data.BaseData;
import com.zhjaid.reddata.pojo.ApiPojo;
import com.zhjaid.reddata.pojo.GroupPojo;
import com.zhjaid.reddata.pojo.ResultPojo;
import com.zhjaid.reddata.utils.DialogListener;
import com.zhjaid.reddata.utils.HttpUtils;
import com.zhjaid.reddata.utils.JsonFormatTool;
import com.zhjaid.reddata.utils.MarkdownEntity;
import com.zhjaid.reddata.utils.RequestType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RequestActivity extends BaseActivity {
    private LinearLayout contentView, headerView;
    private EditText apisApi;
    private ArrayList<ArrayList<String>> mTableDatas = new ArrayList<>();
    private ArrayList<ArrayList<String>> mHeaderDatas = new ArrayList<>();
    private LockTableView mLockTableView, mLockHeaderTableView;
    private QMUITopBar qmuiTopbar;
    private TextView requestMethod, parmter;
    private QMUIRoundButton selectMed, sendData, saveData;
    public static GroupPojo.Group group;
    public static ApiPojo.Api apiPojo;
    private BaseData baseData;
    private QMUILinkTextView qlink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        initViews();
        initDisplayOpinion();
        updateData();
        loadHeaderData();
        checkMethod();

        ColorStateList colorStateList = getResources().getColorStateList(R.color.text_color_state_list);
        qlink.setText("RedData????????????????????????????????????????????????????????????\n?????????????????????????????????????????????2372315936\n??????????????????zhjaid@163.com\n?????????????????????http://www.zhjaid.com", TextView.BufferType.SPANNABLE);
        qlink.setLinkTextColor(colorStateList);
        qlink.setOnLinkClickListener(new QMUILinkTextView.OnLinkClickListener() {
            @Override
            public void onTelLinkClick(String phoneNumber) {
                try {
                    String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + phoneNumber;
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) {
                    makeText("??????QQ?????????QQ????????????");
                    copy(phoneNumber);
                }
            }

            @Override
            public void onMailLinkClick(String mailAddress) {
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822"); // ?????????????????????
                    i.putExtra(Intent.EXTRA_EMAIL,
                            new String[]{mailAddress});
                    i.putExtra(Intent.EXTRA_SUBJECT, "????????????");
                    i.putExtra(Intent.EXTRA_TEXT, "?????????????????????????????????????????????");
                    startActivity(Intent.createChooser(i,
                            "??????????????????"));
                } catch (Exception e) {
                    makeText("??????????????????????????????????????????");
                    copy(mailAddress);
                }

            }

            @Override
            public void onWebUrlLinkClick(String url) {
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(Intent.createChooser(intent,
                            "?????????????????????"));
                } catch (Exception e) {
                    makeText("???????????????????????????????????????");
                    copy(url);
                }
            }
        });
    }

    public void copy(String str) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("result", str);
        // ??????????????????????????????????????????
        cm.setPrimaryClip(clipData);
    }

    private void initDisplayOpinion() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        DisplayUtil.density = dm.density;
        DisplayUtil.densityDPI = dm.densityDpi;
        DisplayUtil.screenWidthPx = dm.widthPixels;
        DisplayUtil.screenhightPx = dm.heightPixels;
        DisplayUtil.screenWidthDip = DisplayUtil.px2dip(getApplicationContext(), dm.widthPixels);
        DisplayUtil.screenHightDip = DisplayUtil.px2dip(getApplicationContext(), dm.heightPixels);
    }

    private void checkMethod() {
        String string = requestMethod.getText().toString();
        requestMethod.setTextColor(RequestType.getColor(string));
        if (string.toUpperCase().equals("GET") || string.toUpperCase().equals("HEAD")) {
            contentView.setVisibility(View.GONE);
            parmter.setVisibility(View.GONE);
        } else {
            parmter.setVisibility(View.VISIBLE);
            contentView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ???????????????
     */
    private void loadHeaderData() {
        mHeaderDatas.clear();
        //??????
        ArrayList<String> mfristData = new ArrayList<String>();
        mfristData.add("key");
        mfristData.add("value");
        mfristData.add("description");
        mHeaderDatas.add(mfristData);
        try {
            List<ApiPojo.Api.Request.Header> headers = apiPojo.getRequest().getHeader();
            for (ApiPojo.Api.Request.Header header : headers) {
                ArrayList<String> mRowDatas = new ArrayList<String>();
                //????????????
                mRowDatas.add(header.getKey());
                mRowDatas.add(header.getValue());
                mRowDatas.add(header.getDescription());
                mHeaderDatas.add(mRowDatas);
            }
        } catch (Exception e) {

        } finally {
            ArrayList<String> mRowDatas = new ArrayList<String>();
            //????????????
            mRowDatas.add("????????????");
            mHeaderDatas.add(mRowDatas);
        }
        mLockHeaderTableView = new LockTableView(this, headerView, mHeaderDatas);
        //Log.e("??????????????????", "???????????????" + Thread.currentThread());
        mLockHeaderTableView.setLockFristColumn(false) //?????????????????????
                .setLockFristRow(true) //?????????????????????
                .setMaxColumnWidth(350) //???????????????
                .setMinColumnWidth(60) //???????????????
                .setColumnWidth(2, 200) //???????????????????????????(???0????????????,????????????dp)
                .setMinRowHeight(20)//???????????????
                .setMaxRowHeight(60)//???????????????
                .setTextViewSize(16) //?????????????????????
                .setFristRowBackGroudColor(R.color.table_head)//???????????????
                .setTableHeadTextColor(R.color.beijin)//??????????????????
                .setTableContentTextColor(R.color.border_color)//?????????????????????
                .setCellPadding(8)//????????????????????????(dp)
                .setNullableString("N/A") //???????????????
                .setTableViewListener(new LockTableView.OnTableViewListener() {
                    @Override
                    public void onTableViewScrollChange(int x, int y) {
//                        Log.e("?????????","["+x+"]"+"["+y+"]");
                    }
                })
                .setTableViewRangeListener(new LockTableView.OnTableViewRangeListener() {
                    @Override
                    public void onLeft(HorizontalScrollView view) {
//                        Log.e("????????????","??????????????????");
                    }

                    @Override
                    public void onRight(HorizontalScrollView view) {
//                        Log.e("????????????","??????????????????");
                    }
                })//??????????????????????????????
                .setOnLoadingListener(new LockTableView.OnLoadingListener() {
                    @Override
                    public void onRefresh(final XRecyclerView mXRecyclerView, final ArrayList<ArrayList<String>> mTableDatas) {
                        mLockHeaderTableView.setTableDatas(mTableDatas);
                        mXRecyclerView.refreshComplete();
                    }

                    @Override
                    public void onLoadMore(final XRecyclerView mXRecyclerView, final ArrayList<ArrayList<String>> mTableDatas) {
                        //Log.e("onLoadMore", Thread.currentThread().toString());
                        mXRecyclerView.setNoMore(true);
                    }
                })
                .setOnItemClickListenter(new LockTableView.OnItemClickListenter() {
                    @Override
                    public void onItemClick(View item, int position) {
                        if (position >= mHeaderDatas.size() - 1) {
                            showAddRequest(true);
                        } else {
                            showUpdateRequest(position, true);
                        }
                    }
                })
                .setOnItemLongClickListenter(new LockTableView.OnItemLongClickListenter() {
                    @Override
                    public void onItemLongClick(View item, int position) {
                        //Log.e("????????????",position+"");
                        if (position < mHeaderDatas.size() - 1 && position > 0) {
                            QMUIDialog.MessageDialogBuilder builder = new QMUIDialog.MessageDialogBuilder(getActivity());
                            builder.setTitle("??????").setMessage("????????????????????????????").addAction("??????", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    mHeaderDatas.remove(position);
                                    mLockHeaderTableView.setTableDatas(mHeaderDatas);
                                    dialog.dismiss();
                                }
                            }).addAction("??????", DialogListener.cancelAction).create().show();
                        }
                    }
                })
                .setOnItemSeletor(R.color.OnItemSeletor)//??????Item???????????????
                .show(); //????????????,?????????????????????
        mLockHeaderTableView.getTableScrollView().setPullRefreshEnabled(false);
        mLockHeaderTableView.getTableScrollView().setLoadingMoreEnabled(false);
        mLockHeaderTableView.getTableScrollView().setRefreshProgressStyle(ProgressStyle.SquareSpin);
    }

    /**
     * ??????????????????
     */
    private void updateData() {
        mTableDatas.clear();
        //??????
        ArrayList<String> mfristData = new ArrayList<String>();
        mfristData.add("key");
        mfristData.add("value");
        mfristData.add("description");
        mTableDatas.add(mfristData);
        try {
            List<ApiPojo.Api.Request.Url.Query> query = apiPojo.getRequest().getUrl().getQuery();
            for (ApiPojo.Api.Request.Url.Query query1 : query) {
                ArrayList<String> mRowDatas = new ArrayList<String>();
                //????????????
                mRowDatas.add(query1.getKey());
                mRowDatas.add(query1.getValue());
                mRowDatas.add(query1.getDescription());
                mTableDatas.add(mRowDatas);
            }
        } catch (Exception e) {

        } finally {
            ArrayList<String> mRowDatas = new ArrayList<String>();
            //????????????
            mRowDatas.add("????????????");
            mTableDatas.add(mRowDatas);
        }
        mLockTableView = new LockTableView(this, contentView, mTableDatas);
        //Log.e("??????????????????", "???????????????" + Thread.currentThread());
        mLockTableView.setLockFristColumn(false) //?????????????????????
                .setLockFristRow(true) //?????????????????????
                .setMaxColumnWidth(350) //???????????????
                .setMinColumnWidth(60) //???????????????
                .setColumnWidth(2, 200) //???????????????????????????(???0????????????,????????????dp)
                .setMinRowHeight(20)//???????????????
                .setMaxRowHeight(60)//???????????????
                .setTextViewSize(16) //?????????????????????
                .setFristRowBackGroudColor(R.color.table_head)//???????????????
                .setTableHeadTextColor(R.color.beijin)//??????????????????
                .setTableContentTextColor(R.color.border_color)//?????????????????????
                .setCellPadding(8)//????????????????????????(dp)
                .setNullableString("N/A") //???????????????
                .setTableViewListener(new LockTableView.OnTableViewListener() {
                    @Override
                    public void onTableViewScrollChange(int x, int y) {
//                        Log.e("?????????","["+x+"]"+"["+y+"]");
                    }
                })
                .setTableViewRangeListener(new LockTableView.OnTableViewRangeListener() {
                    @Override
                    public void onLeft(HorizontalScrollView view) {
//                        Log.e("????????????","??????????????????");
                    }

                    @Override
                    public void onRight(HorizontalScrollView view) {
//                        Log.e("????????????","??????????????????");
                    }
                })//??????????????????????????????
                .setOnLoadingListener(new LockTableView.OnLoadingListener() {
                    @Override
                    public void onRefresh(final XRecyclerView mXRecyclerView, final ArrayList<ArrayList<String>> mTableDatas) {
                        mLockTableView.setTableDatas(mTableDatas);
                        mXRecyclerView.refreshComplete();
                    }

                    @Override
                    public void onLoadMore(final XRecyclerView mXRecyclerView, final ArrayList<ArrayList<String>> mTableDatas) {
                        //Log.e("onLoadMore", Thread.currentThread().toString());
                        mXRecyclerView.setNoMore(true);
                    }
                })
                .setOnItemClickListenter(new LockTableView.OnItemClickListenter() {
                    @Override
                    public void onItemClick(View item, int position) {
                        if (position >= mTableDatas.size() - 1) {
                            showAddRequest(false);
                        } else {
                            showUpdateRequest(position, false);
                        }
                    }
                })
                .setOnItemLongClickListenter(new LockTableView.OnItemLongClickListenter() {
                    @Override
                    public void onItemLongClick(View item, int position) {
                        //Log.e("????????????",position+"");
                        if (position < mTableDatas.size() - 1 && position > 0) {
                            QMUIDialog.MessageDialogBuilder builder = new QMUIDialog.MessageDialogBuilder(getActivity());
                            builder.setTitle("??????").setMessage("?????????????????????????").addAction("??????", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    mTableDatas.remove(position);
                                    mLockTableView.setTableDatas(mTableDatas);
                                    dialog.dismiss();
                                }
                            }).addAction("??????", DialogListener.cancelAction).create().show();
                        }
                    }
                })
                .setOnItemSeletor(R.color.OnItemSeletor)//??????Item???????????????
                .show(); //????????????,?????????????????????
        mLockTableView.getTableScrollView().setPullRefreshEnabled(false);
        mLockTableView.getTableScrollView().setLoadingMoreEnabled(false);
        mLockTableView.getTableScrollView().setRefreshProgressStyle(ProgressStyle.SquareSpin);
    }

    private EditText inputKey, inputValue, inputDescription;

    private void showUpdateRequest(int position, boolean isHeader) {
        QMUIDialog.AutoResizeDialogBuilder customDialogBuilder =
                new QMUIDialog.AutoResizeDialogBuilder(getActivity()) {
                    @Override
                    public View onBuildContent(@NonNull QMUIDialog dialog, @NonNull Context context) {
                        return LayoutInflater.from(context).inflate(R.layout.dialog_input, null);
                    }
                };
        final ArrayList<String> mRowDatas = new ArrayList<>();
        if (isHeader) {
            mRowDatas.addAll(mHeaderDatas.get(position));
            customDialogBuilder.setTitle("???????????????");
        } else {
            mRowDatas.addAll(mTableDatas.get(position));
            customDialogBuilder.setTitle("????????????");
        }
        customDialogBuilder.addAction("??????", DialogListener.cancelAction);
        customDialogBuilder.addAction("??????", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                if (inputKey.getText().toString().equals("") || inputValue.getText().toString().equals("")) {
                    makeText("key ??? value????????????");
                } else {
                    mRowDatas.clear();
                    //????????????
                    mRowDatas.add(inputKey.getText().toString());
                    mRowDatas.add(inputValue.getText().toString());
                    mRowDatas.add(inputDescription.getText().toString());
                    if (isHeader) {
                        mHeaderDatas.remove(position);
                        mHeaderDatas.add(position, mRowDatas);
                        mLockHeaderTableView.setTableDatas(mHeaderDatas);
                    } else {
                        mTableDatas.remove(position);
                        mTableDatas.add(position, mRowDatas);
                        mLockTableView.setTableDatas(mTableDatas);
                    }
                    dialog.dismiss();
                }
            }
        });
        qmuiDialog = customDialogBuilder.create();

        inputKey = qmuiDialog.findViewById(R.id.inputKey);
        inputValue = qmuiDialog.findViewById(R.id.inputValue);
        inputDescription = qmuiDialog.findViewById(R.id.inputDescription);
        if (isHeader) {
            if (mRowDatas.size() > 0) {
                inputKey.setText(mRowDatas.get(0) == null ? "" : mRowDatas.get(0));
                inputValue.setText(mRowDatas.get(1) == null ? "" : mRowDatas.get(1));
                inputDescription.setText(mRowDatas.get(2) == null ? "" : mRowDatas.get(2));
            }
        } else {
            if (mRowDatas.size() > 0) {
                inputKey.setText(mRowDatas.get(0) == null ? "" : mRowDatas.get(0));
                inputValue.setText(mRowDatas.get(1) == null ? "" : mRowDatas.get(1));
                inputDescription.setText(mRowDatas.get(2) == null ? "" : mRowDatas.get(2));
            }
        }

        qmuiDialog.show();
    }

    QMUIDialog qmuiDialog = null;

    private void showAddRequest(boolean isHeader) {
        QMUIDialog.AutoResizeDialogBuilder customDialogBuilder =
                new QMUIDialog.AutoResizeDialogBuilder(getActivity()) {
                    @Override
                    public View onBuildContent(@NonNull QMUIDialog dialog, @NonNull Context context) {
                        return LayoutInflater.from(context).inflate(R.layout.dialog_input, null);
                    }
                };
        customDialogBuilder.setTitle("??????");
        customDialogBuilder.addAction("??????", DialogListener.cancelAction);
        customDialogBuilder.addAction("??????", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                EditText inputKey = qmuiDialog.findViewById(R.id.inputKey);
                EditText inputValue = qmuiDialog.findViewById(R.id.inputValue);
                EditText inputDescription = qmuiDialog.findViewById(R.id.inputDescription);
                if (inputKey.getText().toString().equals("") || inputValue.getText().toString().equals("")) {
                    makeText("key ??? value????????????");
                } else {
                    ArrayList<String> mRowDatas = new ArrayList<String>();
                    //????????????
                    mRowDatas.add(inputKey.getText().toString());
                    mRowDatas.add(inputValue.getText().toString());
                    mRowDatas.add(inputDescription.getText().toString());
                    if (isHeader) {
                        //?????????
                        mHeaderDatas.add(mHeaderDatas.size() - 1, mRowDatas);
                        mLockHeaderTableView.setTableDatas(mHeaderDatas);
                    } else {
                        //????????????
                        mTableDatas.add(mTableDatas.size() - 1, mRowDatas);
                        mLockTableView.setTableDatas(mTableDatas);
                    }
                    dialog.dismiss();
                }
            }
        });
        qmuiDialog = customDialogBuilder.create();
        qmuiDialog.show();
    }

    /**
     * ????????????
     */
    public void showDelete() {

    }

    private void initViews() {
        contentView = findViewById(R.id.contentView);
        headerView = findViewById(R.id.headerView);
        apisApi = findViewById(R.id.apisApi);
        qmuiTopbar = findViewById(R.id.qmuiTopbar);
        selectMed = findViewById(R.id.selectMed);
        requestMethod = findViewById(R.id.requestMethod);
        parmter = findViewById(R.id.parmter);
        qlink = findViewById(R.id.qlink);

        qmuiTopbar.setTitle("????????????");

        baseData = new BaseData(getActivity());

        if (apiPojo.getRequest() != null) {
            String method = apiPojo.getRequest().getMethod();
            if (method != null) {
                requestMethod.setText(method);
            }
        }

        if (apiPojo != null && apiPojo.getRequest() != null && apiPojo.getRequest().getUrl() != null && apiPojo.getRequest().getUrl().getRaw() != null)
            apisApi.setText(apiPojo.getRequest().getUrl().getRaw());
        if (apiPojo != null && apiPojo.getName() != null)
            qmuiTopbar.setTitle(apiPojo.getName());
        selectMed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(getActivity());
                builder.setTitle("????????????");
                builder.addItem("GET");
                builder.addItem("POST");
                builder.addItem("PUT");
                builder.addItem("PATCH");
                builder.addItem("DELETE");
                builder.addItem("HEAD");
                builder.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        dialog.dismiss();
                        requestMethod.setText(tag);
                        requestMethod.setTextColor(RequestType.getColor(tag));
                        checkMethod();
                    }
                });
                builder.build().show();
            }
        });
        requestMethod.setTextColor(RequestType.getColor(apiPojo.getRequest().getMethod()));

        sendData = findViewById(R.id.sendData);
        saveData = findViewById(R.id.saveData);
        sendData.setText("????????????");
        sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData.setText("????????????");
                sendData.setEnabled(false);
                try {
                    //????????????
                    String raw = apisApi.getText().toString();
                    //????????????
                    List<HttpUtils.Parameter> parameters = new ArrayList<>();
                    for (int i = 0; i < mTableDatas.size(); i++) {
                        if (i > 0 && i < mTableDatas.size() - 1) {
                            ArrayList<String> strings = mTableDatas.get(i);
                            parameters.add(new HttpUtils.Parameter(strings.get(0), strings.get(1)));
                        }
                    }
                    //????????????
                    String method = requestMethod.getText().toString();
                    new Thread(new Runnable() {
                        String markdown = null;

                        @SneakyThrows
                        @Override
                        public void run() {
                            ResultPojo pojo = HttpUtils.sendData(raw, parameters, getHeaders(), method);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    result(pojo);
                                }
                            });

                        }
                    }).start();
                } catch (Exception e) {
                    ResultPojo pojo = new ResultPojo();
                    pojo.setBody(e.getMessage());
                    pojo.setMessage(e.getMessage());
                    pojo.setCode(5001);
                    result(pojo);
                }
            }
        });


        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfig();
            }
        });
    }


    private List<ApiPojo.Api.Request.Header> getHeaders() {
        List<ApiPojo.Api.Request.Header> headers = new ArrayList<>();
        for (int i = 0; i < mHeaderDatas.size(); i++) {
            if (i > 0 && i < mHeaderDatas.size() - 1) {
                ArrayList<String> strings = mHeaderDatas.get(i);
                headers.add(new ApiPojo.Api.Request.Header(strings.get(0), strings.get(1), strings.get(2)));
            }
        }
        return headers;
    }

    private List<ApiPojo.Api.Request.Url.Query> getQuerys() {
        List<ApiPojo.Api.Request.Url.Query> query = new ArrayList<>();
        for (int i = 0; i < mTableDatas.size(); i++) {
            if (i > 0 && i < mTableDatas.size() - 1) {
                ArrayList<String> strings = mTableDatas.get(i);
                query.add(new ApiPojo.Api.Request.Url.Query(strings.get(0), strings.get(1), strings.get(2)));
            }
        }
        return query;
    }

    private void saveConfig() {
        if (apiPojo == null) {
            apiPojo = new ApiPojo.Api();
        }
        if (apiPojo.getRequest() == null) {
            apiPojo.setRequest(new ApiPojo.Api.Request());
        }
        if (apiPojo.getRequest().getUrl() == null) {
            apiPojo.getRequest().setUrl(new ApiPojo.Api.Request.Url());
        }


        apiPojo.getRequest().setHeader(getHeaders());
        apiPojo.getRequest().setMethod(requestMethod.getText().toString());
        apiPojo.getRequest().getUrl().setQuery(getQuerys());
        apiPojo.getRequest().getUrl().setRaw(apisApi.getText().toString());
        boolean b = baseData.updateRequest(group, apiPojo);
    }

    private void result(ResultPojo pojo) {
        sendData.setText("????????????");
        sendData.setEnabled(true);
        ResultActivity.result = pojo;
        ResultActivity.start(getActivity(), ResultActivity.class);
    }
}
