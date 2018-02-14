package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import pl.janpogocki.agh.wirtualnydziekanat.javas.FetchUniversityStatus;
import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class SummaryActivity extends Fragment {

    FirebaseAnalytics mFirebaseAnalytics;
    Context activityContext;
    View root;

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAnalytics.setCurrentScreen(getActivity(), getString(R.string.summary), null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activityContext);

        root = inflater.inflate(R.layout.activity_summary, container, false);

        ImageView imageViewPhotoUser;
        TextView textViewNameAndSurname, textViewAlbumNumber, textViewPeselNumber;
        ListView listView;

        ListAdapter listAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return Storage.summarySemesters.size();
            }

            @Override
            public Object getItem(int position) {
                return Storage.summarySemesters.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                String academicYear = Storage.summarySemesters.get(position).get(0);
                String year = Storage.summarySemesters.get(position).get(1);
                String semester = Storage.summarySemesters.get(position).get(2);
                String type = Storage.summarySemesters.get(position).get(3);
                String dateDecision = Storage.summarySemesters.get(position).get(4);
                String studentStatus = Storage.summarySemesters.get(position).get(7);

                if (convertView == null) {
                    LayoutInflater infalInflater = (LayoutInflater) root.getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = infalInflater.inflate(R.layout.summary_list_item, parent, false);
                }

                TextView textViewHeader = convertView.findViewById(R.id.textViewHeader);
                TextView textView2ndLine = convertView.findViewById(R.id.textView2ndLine);
                TextView textView3rdLine = convertView.findViewById(R.id.textView3rdLine);

                textViewHeader.setText(academicYear + " - rok: " + year + ", semestr: " + semester + " (" + type + ")");
                textView2ndLine.setText("Data decyzji: " + dateDecision);
                textView3rdLine.setText(studentStatus);

                return convertView;
            }
        };

        imageViewPhotoUser = root.findViewById(R.id.imageViewPhotoUser);
        textViewNameAndSurname = root.findViewById(R.id.textViewNameAndSurname);
        textViewAlbumNumber = root.findViewById(R.id.textViewAlbumNumber);
        textViewPeselNumber = root.findViewById(R.id.textViewPeselNumber);
        listView = root.findViewById(R.id.listView);

        if (Storage.photoUser != null)
            imageViewPhotoUser.setImageBitmap(Storage.photoUser);

        textViewNameAndSurname.setText(Storage.nameAndSurname);
        textViewAlbumNumber.setText(Storage.albumNumber);
        textViewPeselNumber.setText(Storage.peselNumber);
        listView.setAdapter(listAdapter);

        final ScrollView scrollView = root.findViewById(R.id.scrollView);
        scrollView.post(new Runnable()
        {
            public void run()
            {
                scrollView.fullScroll(View.FOCUS_DOWN);
                scrollView.fullScroll(View.FOCUS_UP);
            }
        });

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, root);

        return root;
    }

    private class AsyncTaskRunner extends AsyncTask<View, View, View> {
        Boolean isError = false;

        @Override
        protected View doInBackground(View... params) {
            try {
                if (Storage.universityStatus == null || Storage.universityStatus.size() == 0){
                    new FetchUniversityStatus(false);
                }

                return root;
            } catch (Exception e) {
                Log.i("aghwd", "aghwd", e);
                Storage.appendCrash(e);
                isError = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(View result){
            LinearLayout layoutProgressBar = root.findViewById(R.id.layoutProgressBar);
            LinearLayout layoutAdditionalData = root.findViewById(R.id.layoutAdditionalData);
            TextView textViewWydzial = root.findViewById(R.id.textViewWydzial);
            TextView textViewKierunek = root.findViewById(R.id.textViewKierunek);
            TextView textViewSpecjalnosc = root.findViewById(R.id.textViewSpecjalnosc);
            TextView textViewFormaStudiow = root.findViewById(R.id.textViewFormaStudiow);
            TextView textViewPoziomStudiow = root.findViewById(R.id.textViewPoziomStudiow);
            TextView textViewProfilKsztalcenia = root.findViewById(R.id.textViewProfilKsztalcenia);
            TextView textViewStatusKierunku = root.findViewById(R.id.textViewStatusKierunku);
            TextView textViewDaneOKierunku = root.findViewById(R.id.textViewDaneOKierunku);
            TextView textViewDataRozpoczeciaStudiow = root.findViewById(R.id.textViewDataRozpoczeciaStudiow);
            TextView textViewDataRozpoczeciaStudiowAGH = root.findViewById(R.id.textViewDataRozpoczeciaStudiowAGH);

            if (Storage.universityStatus == null || Storage.universityStatus.size() == 0 || isError){
                layoutProgressBar.setVisibility(View.GONE);
                layoutAdditionalData.setVisibility(View.GONE);
            }
            else {
                // Have it, show it
                textViewWydzial.setText(Storage.universityStatus.get(1));
                textViewKierunek.setText(Storage.universityStatus.get(2));
                textViewSpecjalnosc.setText(Storage.universityStatus.get(3));
                textViewFormaStudiow.setText(Storage.universityStatus.get(4));
                textViewPoziomStudiow.setText(Storage.universityStatus.get(5));
                textViewProfilKsztalcenia.setText(Storage.universityStatus.get(6));
                textViewStatusKierunku.setText(Storage.universityStatus.get(7));
                textViewDaneOKierunku.setText(Storage.universityStatus.get(8));
                textViewDataRozpoczeciaStudiow.setText(Storage.universityStatus.get(9));
                textViewDataRozpoczeciaStudiowAGH.setText(Storage.universityStatus.get(10));

                layoutProgressBar.setVisibility(View.GONE);
                layoutAdditionalData.setVisibility(View.VISIBLE);
            }
        }
    }
}