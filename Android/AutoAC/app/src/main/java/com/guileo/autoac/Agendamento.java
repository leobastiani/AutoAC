package com.guileo.autoac;

/**
 * Created by Guilherme on 25/10/2017.
 */

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class Agendamento extends Fragment{

    private HTTPConect httpConect;
    private View rootView;
    private Activity activity;
    private ListView lista;
    private boolean temLista = false;
    private int excluir;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.agendamento_tab, container, false);
        this.rootView = rootView;
        this.activity = this.getActivity();

        this.httpConect = new HTTPConect(this.getActivity(), (TextView) rootView.findViewById(R.id.status) ,"http://143.107.235.41:5000");
        // faz uma requisição pro index pra atualizar o status

        ListView lista = (ListView) rootView.findViewById(R.id.listaAgenda);

        ArrayList<String> agendamentos = new ArrayList<String>();

        agendamentos.add("Carregando Lista... Clique caso demore.");

        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this.activity, android.R.layout.simple_list_item_1, agendamentos);
        lista.setAdapter(adaptador);

        this.lista = lista;

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                atualizaLista();
            }
        });

        atualizaLista();

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               DiaHoraSelet seletor = new DiaHoraSelet();
                seletor.show(Agendamento.this.getActivity().getSupportFragmentManager(), "Agendamento");
                seletor.setTargetFragment(Agendamento.this, 1);

            }
        });

        return rootView;
    }

    private void atualizaLista(){
        httpConect.getAgenda(new Callback() {
            @Override
            public void run(String str) {
                String agenda[] = str.split("\\r?\\n");
                ArrayList<String> agendamentos = new ArrayList<String>(Arrays.asList(agenda));
                ArrayAdapter<String> adaptador = new ArrayAdapter<String>(Agendamento.this.activity, android.R.layout.simple_list_item_1, agendamentos);
                Agendamento.this.lista.setAdapter(adaptador);
                if(temLista == false) {
                    lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            excluir = i;
                            new AlertDialog.Builder(Agendamento.this.getActivity())
                                    .setTitle("Excluir agendamento")
                                    .setMessage("Deseja realmente excluir esse agendamento?")
                                    .setPositiveButton("Excluir", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            httpConect.excluirAgendamento(excluir, new Callback() {
                                                @Override
                                                public void run(String str) {
                                                    Agendamento.this.atualizaLista();
                                                }
                                            });
                                        }})
                                    .setNegativeButton("Cancelar", null).show();
                        }
                    });
                    temLista = true;
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 &&resultCode == Activity.RESULT_OK) {
            Toast.makeText(Agendamento.this.getActivity(), data.getStringExtra("AgendamentoNovo"), Toast.LENGTH_LONG).show();
            httpConect.addAgendamento(data.getStringExtra("AgendamentoNovo"), new Callback() {
                @Override
                public void run(String str) {
                    Agendamento.this.atualizaLista();
                }
            });
        }
    }
}
