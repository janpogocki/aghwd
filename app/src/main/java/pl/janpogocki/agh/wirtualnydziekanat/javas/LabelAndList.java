package pl.janpogocki.agh.wirtualnydziekanat.javas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan on 19.02.2017.
 * Data schema
 */

public class LabelAndList<T>{
    String label;
    List<T> list = new ArrayList<>();

    public LabelAndList(String _label){
        label = _label;
    }

    public void add(T _element){
        list.add(_element);
    }

    public String getLabel(){
        return label;
    }

    public List<T> getList(){
        return list;
    }
}