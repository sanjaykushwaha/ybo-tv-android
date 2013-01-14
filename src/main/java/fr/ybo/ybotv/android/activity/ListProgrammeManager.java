package fr.ybo.ybotv.android.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import fr.ybo.ybotv.android.YboTvApplication;
import fr.ybo.ybotv.android.adapter.ProgrammeAdapter;
import fr.ybo.ybotv.android.modele.ChannelWithProgramme;
import fr.ybo.ybotv.android.modele.Programme;
import fr.ybo.ybotv.android.util.Chrono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListProgrammeManager {

    public interface GetProgramme {
        List<ChannelWithProgramme> getProgrammes();
    }

    private final GetProgramme getProgramme;
    private final ProgrammeAdapter adapter;
    private List<ChannelWithProgramme> channels = new ArrayList<ChannelWithProgramme>();
    private Context context;

    public ListProgrammeManager(AbsListView listView, Activity context, GetProgramme getProgramme) {
        this.getProgramme = getProgramme;
        this.adapter = new ProgrammeAdapter(context, channels);
        this.context = context;
        if (listView instanceof ListView) {
            ((ListView) listView).setAdapter(adapter);
        } else if (listView instanceof GridView) {
            ((GridView) listView).setAdapter(adapter);
        } else {
            listView.setAdapter(adapter);
        }
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Programme programme = channels.get(position).getProgramme();
                Intent intent = new Intent(ListProgrammeManager.this.context, ProgrammeActivity.class);
                intent.putExtra("programme", (Parcelable) programme);
                ListProgrammeManager.this.context.startActivity(intent);
            }
        });
        context.registerForContextMenu(listView);
    }

    @SuppressWarnings("unchecked")
    public void constructAdapter() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Chrono chrono = new Chrono("GetProgrammes").start();
                List<ChannelWithProgramme> newChannels = getProgramme.getProgrammes();
                chrono.stop();

                chrono = new Chrono("Programmes>sort").start();
                // Sort
                Collections.sort(newChannels, new Comparator<ChannelWithProgramme>() {
                    @Override
                    public int compare(ChannelWithProgramme channelWithProgramme, ChannelWithProgramme channelWithProgramme1) {
                        Integer numero1 = channelWithProgramme.getChannel().getNumero();
                        Integer numero2 = channelWithProgramme1.getChannel().getNumero();
                        if (numero1.equals(numero2)) {
                            String start1 = channelWithProgramme.getProgramme().getStart();
                            String start2 = channelWithProgramme1.getProgramme().getStart();
                            return start1.compareTo(start2);
                        }
                        return numero1.compareTo(numero2);
                    }
                });
                chrono.stop();

                chrono = new Chrono("Programmes>listUpdate").start();
                channels.clear();
                channels.addAll(newChannels);
                chrono.stop();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                adapter.notifyDataSetChanged();
                super.onPostExecute(aVoid);
            }
        }.execute();

    }
}
