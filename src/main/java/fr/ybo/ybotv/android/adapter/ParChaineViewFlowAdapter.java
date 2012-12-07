package fr.ybo.ybotv.android.adapter;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import fr.ybo.ybotv.android.R;
import fr.ybo.ybotv.android.YboTvApplication;
import fr.ybo.ybotv.android.activity.ProgrammeActivity;
import fr.ybo.ybotv.android.modele.Channel;
import fr.ybo.ybotv.android.modele.Programme;
import org.taptwo.android.widget.TitleProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ParChaineViewFlowAdapter extends BaseAdapter implements TitleProvider {


    private LayoutInflater inflater;
    private Activity context;

    private List<Channel> channels;

    private Calendar currentDate;

    public ParChaineViewFlowAdapter(Activity context, List<Channel> channels) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.channels = channels;
        currentDate = Calendar.getInstance();
    }

    public void changeCurrentDate(Calendar newCurrentDate) {
        currentDate = newCurrentDate;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return channels.size();
    }

    @Override
    public Channel getItem(int position) {
        return channels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ProgrammeOnItemClickListener implements AdapterView.OnItemClickListener {

        private List<Programme> programmes;
        private Activity activity;

        public ProgrammeOnItemClickListener(List<Programme> programmes, Activity activity) {
            this.programmes = programmes;
            this.activity = activity;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Programme programme = programmes.get(position);
            Intent intent = new Intent(activity, ProgrammeActivity.class);
            intent.putExtra("programme", (Parcelable)programme);
            activity.startActivity(intent);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_ss_pub, null);
        }

        final ListView listView = (ListView) convertView.findViewById(android.R.id.list);
        listView.setTextFilterEnabled(true);

        final List<Programme> programmes = new ArrayList<Programme>();
        final Channel channel = getItem(position);
        listView.setOnItemClickListener(new ProgrammeOnItemClickListener(programmes, context));
        final ProgrammeByChaineAdapter adapter = new ProgrammeByChaineAdapter(context, programmes);
        listView.setAdapter(adapter);
        context.registerForContextMenu(listView);

        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... params) {
                List<Programme> programmesTmp = Programme.getProgrammes((YboTvApplication) context.getApplication(), channel, currentDate);

                String currentDateChaine = new SimpleDateFormat("yyyyMMddHHmmss").format(currentDate.getTime());

                int currentPosition = 0;
                for (Programme programme : programmesTmp) {
                    if (currentDateChaine.compareTo(programme.getStart()) >= 0
                            && currentDateChaine.compareTo(programme.getStop()) < 0) {
                        break;
                    }
                    currentPosition++;
                }

                programmes.clear();
                programmes.addAll(programmesTmp);

                return currentPosition;
            }

            @Override
            protected void onPostExecute(Integer currentPosition) {
                adapter.notifyDataSetChanged();
                if (currentPosition < programmes.size()) {
                    listView.setSelection(currentPosition);
                }

            }
        }.execute();

        return convertView;
    }


    /* (non-Javadoc)
    * @see org.taptwo.android.widget.TitleProvider#getTitle(int)
    */
    @Override
    public String getTitle(int position) {
        return getItem(position).getDisplayName();
    }
}
