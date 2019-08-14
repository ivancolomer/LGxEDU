package com.lglab.ivan.lgxeducontroller.legacy;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.lglab.ivan.lgxeducontroller.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class InfoActivityFragment extends Fragment {

    public InfoActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_help, container, false);
        TextView inst = view.findViewById(R.id.import_inst);
        TextView introduction = view.findViewById(R.id.import_introduction);
        TextView complete_inf = view.findViewById(R.id.complete_information);
        inst.setMovementMethod(LinkMovementMethod.getInstance());
        introduction.setMovementMethod(LinkMovementMethod.getInstance());
        complete_inf.setMovementMethod(LinkMovementMethod.getInstance());

        return view;
    }
}
