package pl.janpogocki.agh.wirtualnydziekanat.javas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan on 08.10.2017.
 * Data schema
 */

public class LabelListAndList<T>{
    List<T> label;
    List<List<T>> list = new ArrayList<>();

    public LabelListAndList(List<T> _label){
        label = _label;
    }

    public void add(List<T> _element){
        list.add(_element);
    }

    public List<T> getLabel(){
        return label;
    }

    public List<List<T>> getList(){
        return list;
    }
}