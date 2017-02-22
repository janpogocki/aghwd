package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import pl.janpogocki.agh.wirtualnydziekanat.javas.Storage;

public class SummaryActivity extends Fragment {

    public static Fragment newInstance(Context context) {
        SummaryActivity f = new SummaryActivity();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.activity_summary, null);

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
                    convertView = infalInflater.inflate(R.layout.summary_list_item, null);
                }

                TextView textViewHeader = (TextView) convertView.findViewById(R.id.textViewHeader);
                TextView textView2ndLine = (TextView) convertView.findViewById(R.id.textView2ndLine);
                TextView textView3rdLine = (TextView) convertView.findViewById(R.id.textView3rdLine);

                textViewHeader.setText(academicYear + " - rok: " + year + ", semestr: " + semester + " (" + type + ")");
                textView2ndLine.setText("Data decyzji: " + dateDecision);
                textView3rdLine.setText(studentStatus);

                return convertView;
            }
        };

        imageViewPhotoUser = (ImageView) root.findViewById(R.id.imageViewPhotoUser);
        textViewNameAndSurname = (TextView) root.findViewById(R.id.textViewNameAndSurname);
        textViewAlbumNumber = (TextView) root.findViewById(R.id.textViewAlbumNumber);
        textViewPeselNumber = (TextView) root.findViewById(R.id.textViewPeselNumber);
        listView = (ListView) root.findViewById(R.id.listView);

        imageViewPhotoUser.setImageBitmap(Storage.photoUser);
        textViewNameAndSurname.setText(Storage.nameAndSurname);
        textViewAlbumNumber.setText(Storage.albumNumber);
        textViewPeselNumber.setText(Storage.peselNumber);
        listView.setAdapter(listAdapter);

        final ScrollView scrollView = (ScrollView) root.findViewById(R.id.scrollView);
        scrollView.post(new Runnable()
        {
            public void run()
            {
                scrollView.fullScroll(View.FOCUS_DOWN);
                scrollView.fullScroll(View.FOCUS_UP);
            }
        });

        return root;
    }

}