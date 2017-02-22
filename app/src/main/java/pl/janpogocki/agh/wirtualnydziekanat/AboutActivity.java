package pl.janpogocki.agh.wirtualnydziekanat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        ImageView imageView2 = (ImageView) root.findViewById(R.id.imageView2);
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // build dialog window
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Easter Egg")
                        .setMessage("Ku pamięci wszystkich studentów poległych podczas zdawania wszystkich poprzednich sesji. Runda honorowa wokół Ronda Ofiar Warunków. :)")
                        .setPositiveButton("Pamiętajmy na wieki!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {}
                        });

                builder.create().show();
            }
        });

        return root;
    }

}