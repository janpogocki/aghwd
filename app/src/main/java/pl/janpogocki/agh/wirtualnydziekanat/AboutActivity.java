package pl.janpogocki.agh.wirtualnydziekanat;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

public class AboutActivity extends Fragment {

    FirebaseAnalytics mFirebaseAnalytics;
    Context activityContext;

    public static Fragment newInstance(Context context) {
        AboutActivity f = new AboutActivity();
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activityContext);

        View root = inflater.inflate(R.layout.activity_about, container, false);

        TextView textView6 = (TextView) root.findViewById(R.id.textView6);
        textView6.setText("v. " + textView6.getText());

        ImageView imageView2 = (ImageView) root.findViewById(R.id.imageView2);
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "clicked");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "easter_egg");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                // build dialog window
                final AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
                builder.setTitle("Easter Egg")
                        .setMessage("Ku pamięci wszystkich studentów poległych podczas zdawania wszystkich poprzednich sesji. Runda honorowa wokół Ronda Ofiar Warunków. :)")
                        .setPositiveButton("Pamiętajmy na wieki!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {}
                        });

                builder.create().show();
            }
        });

        TextView googlePlayLink = (TextView) root.findViewById(R.id.googlePlayLink);
        googlePlayLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "clicked");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "google_play_link");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                final String appPackageName = "pl.janpogocki.agh.wirtualnydziekanat";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAnalytics.setCurrentScreen(getActivity(), getString(R.string.about_app), null);
    }
}