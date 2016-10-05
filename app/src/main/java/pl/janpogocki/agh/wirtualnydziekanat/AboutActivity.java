package pl.janpogocki.agh.wirtualnydziekanat;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutActivity extends Fragment {

    public static Fragment newInstance(Context context) {
        AboutActivity f = new AboutActivity();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.activity_about, null);

        TextView textView6 = (TextView) root.findViewById(R.id.textView6);
        textView6.setText("v. " + textView6.getText());

        return root;
    }

}