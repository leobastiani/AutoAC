package com.guileo.autoac;

/**
 * Created by Guilherme on 25/10/2017.
 */

import android.app.Activity;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class Controle extends Fragment implements View.OnClickListener{

    private HTTPConect httpConect;
    private View rootView;
    private Activity activity;
    private TabLayout tab;

    public void setTab(TabLayout tab) {
        this.tab = tab;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.controle_tab, container, false);
        this.rootView = rootView;
        this.activity = this.getActivity();
        this.httpConect = new HTTPConect(this.getActivity(), (TextView) rootView.findViewById(R.id.status) ,"http://143.107.235.41:5000");
        // faz uma requisição pro index pra atualizar o status
        httpConect.request("/index", null);

        rootView.findViewById(R.id.ligarBtn).setOnClickListener(this);
        rootView.findViewById(R.id.desligarBtn).setOnClickListener(this);
        rootView.findViewById(R.id.upBtn).setOnClickListener(this);
        rootView.findViewById(R.id.downBtn).setOnClickListener(this);

        //new TempTimer((TextView) rootView.findViewById(R.id.tempAtual), this.httpConect);

        Thread thread = new Thread(){
            @Override
            public void run() {
                while (true) {
                    try {
                        synchronized (this) {
                           Controle.this.activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TabLayout tab = Controle.this.tab;
                                    if(tab!=null && tab.getSelectedTabPosition()==0) {
                                        httpConect.getTemp(new Callback() {
                                            @Override
                                            public void run(String str) {
                                                ((TextView) Controle.this.rootView.findViewById(R.id.tempAtual)).setText(str);
                                            }
                                        });
                                    }
                                }
                            });
                            wait(10000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();

        return rootView;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ligarBtn:
                onClickB(view);
                break;
            case R.id.desligarBtn:
                offClick(view);
                break;
            case R.id.upBtn:
                upClick(view);
                break;
            case R.id.downBtn:
                downClick(view);
                break;
        }
    }

    public void onClickB(View v){
        onOffClick("on");
    }

    public void offClick(View v){
        onOffClick("off");
    }

    public void onOffClick(String onOff){
        httpConect.setOnOff(onOff, new Callback() {
            @Override
            public void run(String str) {
                Toast.makeText(activity, str, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void upDownBtn(int diff) {
        final Callback cb = new Callback() {
            @Override
            public void run(String str) {
                TextView temp = (TextView) rootView.findViewById(R.id.temp);
                temp.setText(str);
            }
        };
        httpConect.incTemp(diff, cb);
    }

    public void upClick(View v) {
        upDownBtn(1);
    }

    public void downClick(View v) {
        upDownBtn(-1);
    }
}

class TempTimer{
    Timer timer;
    TextView tempAtual;
    HTTPConect httpConect;

    public TempTimer(TextView tempAtual, HTTPConect httpConect) {
        this.tempAtual = tempAtual;
        this.httpConect = httpConect;
        this.timer = new Timer();
        timer.schedule(new AtualizaTemp(), 5000);
    }

    class AtualizaTemp extends TimerTask{

        @Override
        public void run() {
            httpConect.getTemp(new Callback() {
                @Override
                public void run(String str) {
                    TempTimer.this.tempAtual.setText(str);
                }
            });
        }
    }
}