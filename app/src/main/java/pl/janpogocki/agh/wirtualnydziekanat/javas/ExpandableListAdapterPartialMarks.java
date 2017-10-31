package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import pl.janpogocki.agh.wirtualnydziekanat.PartialMarksExplorer;
import pl.janpogocki.agh.wirtualnydziekanat.R;

/**
 * Created by Jan on 20.02.2017.
 * Class generating viewing marks
 */

public class ExpandableListAdapterPartialMarks extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

    private Context _context;
    // header titles
    private List<List<String>> _listDataHeader;
    // child data in format of header title, child title
    private HashMap<String, List<List<String>>> _listDataChild;
    private PartialMarksExplorer _partialMarksExplorer;

    public ExpandableListAdapterPartialMarks(Context context, List<List<String>> listDataHeader, HashMap<String, List<List<String>>> listChildData,
                                             PartialMarksExplorer partialMarksExplorer) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this._partialMarksExplorer = partialMarksExplorer;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return null;
    }

    public Object getChild(int groupPosition, int childPosititon, int dataPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition).get(0) + this._listDataHeader.get(groupPosition).get(1)).get(childPosititon).get(dataPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String childCategory = (String) getChild(groupPosition, childPosition, 0);
        String childMark = (String) getChild(groupPosition, childPosition, 1);
        String childData = (String) getChild(groupPosition, childPosition, 2);
        String childProwadzacy = (String) getChild(groupPosition, childPosition, 3);
        String childUwagi = (String) getChild(groupPosition, childPosition, 4);
        String childAghMark = (String) getChild(groupPosition, childPosition, 5);
        String childSubject = (String) getChild(groupPosition, childPosition, 6);
        String childLesson = (String) getChild(groupPosition, childPosition, 7);
        String childCurrentSemester = (String) getChild(groupPosition, childPosition, 8);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.partial_marks_list_item, null);
        }

        TextView txtListChildCategory = convertView.findViewById(R.id.textViewCategory);
        TextView txtListChildData = convertView.findViewById(R.id.textViewData);
        TextView txtListChildUwagi = convertView.findViewById(R.id.textViewUwagi);
        TextView txtListChildMark = convertView.findViewById(R.id.textViewMark);
        txtListChildCategory.setText(childCategory);

        if (!childProwadzacy.equals(""))
            txtListChildData.setText(childData + "\n" + childProwadzacy);
        else
            txtListChildData.setText(childData);

        txtListChildUwagi.setText(childUwagi);
        txtListChildMark.setText(childMark);

        if (childUwagi.trim().equals(""))
            txtListChildUwagi.setVisibility(View.GONE);
        else
            txtListChildUwagi.setVisibility(View.VISIBLE);

        // activate onClickListener
        LinearLayout linearLayoutAllItem = convertView.findViewById(R.id.linearLayoutAllItem);
        ImageView imageViewMyPartialMark = convertView.findViewById(R.id.imageViewMyPartialMark);
        if (!childAghMark.equals("agh_mark")){
            try {
                imageViewMyPartialMark.setVisibility(View.VISIBLE);

                linearLayoutAllItem.setFocusable(true);
                linearLayoutAllItem.setClickable(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    linearLayoutAllItem.setBackground(_context.obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground}).getDrawable(0));
                else
                    linearLayoutAllItem.setBackgroundDrawable(_context.obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground}).getDrawable(0));

                // temp partialmark
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                long timestamp = df.parse(childData).getTime() - TimeZone.getDefault().getOffset(df.parse(childData).getTime());
                final PartialMark partialMark = new PartialMark(childMark, childCategory, childSubject, childLesson, timestamp, childUwagi, childCurrentSemester);

                linearLayoutAllItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        _partialMarksExplorer.showPartialMarkSettings(partialMark);
                    }
                });
            } catch (Exception e){
                Log.i("aghwd", "aghwd", e);
                Storage.appendCrash(e);
            }
        }
        else {
            imageViewMyPartialMark.setVisibility(View.GONE);
            linearLayoutAllItem.setFocusable(false);
            linearLayoutAllItem.setClickable(false);
            linearLayoutAllItem.setOnClickListener(null);
            linearLayoutAllItem.setBackgroundColor(_context.getResources().getColor(android.R.color.transparent));
        }

        return convertView;
    }

    @Override
    public int getRealChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition).get(0) + this._listDataHeader.get(groupPosition).get(1)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    public Object getGroup(int groupPosition, int dataPosition) {
        return this._listDataHeader.get(groupPosition).get(dataPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition, 0);
        String headerLabType = (String) getGroup(groupPosition, 1);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.marks_list_group, null);
        }

        TextView textViewTitle = convertView.findViewById(R.id.textViewTitle);
        TextView textViewSubTitle = convertView.findViewById(R.id.textViewSubTitle);
        textViewTitle.setText(headerTitle);
        textViewSubTitle.setText(headerLabType);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
