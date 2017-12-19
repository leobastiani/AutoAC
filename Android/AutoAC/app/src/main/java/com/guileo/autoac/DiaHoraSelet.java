package com.guileo.autoac;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;

/**
 * Created by Guilherme on 01/11/2017.
 */
public class DiaHoraSelet extends DialogFragment {

    private View rootView;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View rootView = inflater.inflate(R.layout.dia_hora_select, null);
        this.rootView = rootView;
        final Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.semana_array, R.layout.spinner_style);
        adapter.setDropDownViewResource(R.layout.spinner_style);
        spinner.setAdapter(adapter);

        final Spinner spinnerComando = (Spinner) rootView.findViewById(R.id.spinnerComando);
        ArrayAdapter<CharSequence> adapterComando = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.comando_array, R.layout.spinner_style);
        adapterComando.setDropDownViewResource(R.layout.spinner_style);
        spinnerComando.setAdapter(adapterComando);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String retorno = geraString();
                        Intent intent = new Intent();
                        intent.putExtra("AgendamentoNovo", retorno);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                    }
                })
                .setNegativeButton("Sair", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private String geraString(){
        TimePicker timerPicker = (TimePicker) rootView.findViewById(R.id.timePicker);
        Spinner spinner_dia =    (Spinner) rootView.findViewById(R.id.spinner);
        Spinner spiner_comando = (Spinner) rootView.findViewById(R.id.spinnerComando);

        //Horas e minutos
        String retorno = "/cronjob/";

        //Dia da semana
        if (spinner_dia.getSelectedItemPosition() == 7){
            retorno += "day/0/";
        }else{
            retorno += "week/";
            retorno += spinner_dia.getSelectedItemPosition() + "/";
        }

        retorno += String.valueOf(timerPicker.getCurrentHour()) + "/" +
                String.valueOf(timerPicker.getCurrentMinute()) + "/";

        //Comando
        String comando;
        if(spiner_comando.getSelectedItemPosition() == 0){
            comando = "OFF";
        }else{
            int temp = spiner_comando.getSelectedItemPosition() + 17;
            comando = String.valueOf(temp);
        }

        retorno += comando;

        return retorno;
    }

}