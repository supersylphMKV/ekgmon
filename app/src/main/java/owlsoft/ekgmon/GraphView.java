package owlsoft.ekgmon;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class GraphView extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.activity_main, container, false);
        GraphDrawer view = new GraphDrawer(this.getContext());
        return view ;
    }
}