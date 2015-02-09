
package net.nashlegend.legendexplorer.fragment;

import android.app.Fragment;
import android.content.Intent;

public abstract class BaseFragment extends Fragment {

    public BaseFragment() {

    }

    public abstract boolean doBackAction();

    public abstract boolean doVeryAction(Intent intent);

}
