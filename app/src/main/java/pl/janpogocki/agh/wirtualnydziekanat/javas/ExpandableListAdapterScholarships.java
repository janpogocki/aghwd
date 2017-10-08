package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import pl.janpogocki.agh.wirtualnydziekanat.R;

/**
 * Created by Jan on 08.10.2017.
 * Class generating viewing scholarships
 */

public class ExpandableListAdapterScholarships extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

    private Context _context;
    // header titles
    private List<List<String>> _listDataHeader;
    // child data in format of header title, child title
    private HashMap<String, List<List<String>>> _listDataChild;

    public ExpandableListAdapterScholarships(Context context, List<List<String>> listDataHeader, HashMap<String, List<List<String>>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
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
        String childMonth = (String) getChild(groupPosition, childPosition, 0);
        String childAmount = "PLN " + ((String) getChild(groupPosition, childPosition, 1)).toLowerCase().replace("pln", "").trim();

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.scholarships_list_item, null);
        }

        TextView txtListChild1 = convertView.findViewById(R.id.textView1);
        TextView txtListChild2 = convertView.findViewById(R.id.textView2);
        txtListChild1.setText(childMonth);
        txtListChild2.setText(childAmount);

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
        String headerTitle = ((String) getGroup(groupPosition, 0)).toLowerCase();
        headerTitle = headerTitle.substring(0, 1).toUpperCase() + headerTitle.substring(1);
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
