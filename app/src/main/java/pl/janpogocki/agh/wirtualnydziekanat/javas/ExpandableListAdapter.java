package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.HashMap;
import java.util.List;
import pl.janpogocki.agh.wirtualnydziekanat.R;

/**
 * Created by Jan on 02.08.2016.
 * Class generating viewing marks
 */

public class ExpandableListAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

    private Context _context;
    // header titles
    private List<List<String>> _listDataHeader;
    // child data in format of header title, child title
    private HashMap<String, List<List<String>>> _listDataChild;

    public ExpandableListAdapter(Context context, List<List<String>> listDataHeader, HashMap<String, List<List<String>>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return null;
    }

    public Object getChild(int groupPosition, int childPosititon, int dataPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition).get(0)).get(childPosititon).get(dataPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String childLecture = (String) getChild(groupPosition, childPosition, 0);
        String childType = (String) getChild(groupPosition, childPosition, 1);
        String childMark1 = (String) getChild(groupPosition, childPosition, 2);
        String childMark2 = (String) getChild(groupPosition, childPosition, 3);
        String childMark3 = (String) getChild(groupPosition, childPosition, 4);
        String childTeacher = (String) getChild(groupPosition, childPosition, 5);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.marks_list_item, null);
        }

        TextView txtListChildLecture = convertView.findViewById(R.id.textViewLecture);
        TextView txtListChildMark1 = convertView.findViewById(R.id.textViewMark1);
        TextView txtListChildMark2 = convertView.findViewById(R.id.textViewMark2);
        TextView txtListChildMark3 = convertView.findViewById(R.id.textViewMark3);
        TextView txtListChildTeacher = convertView.findViewById(R.id.textViewTeacher);
        txtListChildLecture.setText(childLecture + " (" + childType + ")");
        txtListChildMark1.setText(childMark1);
        txtListChildMark2.setText(childMark2);
        txtListChildMark3.setText(childMark3);
        txtListChildTeacher.setText(childTeacher);

        return convertView;
    }

    @Override
    public int getRealChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition).get(0)).size();
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
        String headerECTS = (String) getGroup(groupPosition, 1);
        String headerFinalMark = (String) getGroup(groupPosition, 2);
        String headerExamStatus = (String) getGroup(groupPosition, 3);
        String headerNewMark = (String) getGroup(groupPosition, 4);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.marks_list_group, null);
        }

        TextView textViewTitle = convertView.findViewById(R.id.textViewTitle);
        TextView textViewSubTitle = convertView.findViewById(R.id.textViewSubTitle);
        TextView textViewFinalMark = convertView.findViewById(R.id.textViewFinalMark);
        textViewTitle.setText(headerTitle);

        if (headerExamStatus.equals("yes"))
            textViewSubTitle.setText(headerECTS + " ECTS - EGZAMIN");
        else
            textViewSubTitle.setText(headerECTS + " ECTS");

        textViewFinalMark.setText(headerFinalMark);

        ImageView imageViewNewMark = convertView.findViewById(R.id.imageViewNewMark);
        if (headerNewMark.equals("yes"))
            imageViewNewMark.setVisibility(View.VISIBLE);

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
