/* Source:
 * Google Inc. (2014b). Google Play Game Services: Android Samples: TypeANumber [Software].
 * Available from https://github.com/playgameservices/android-basic-samples
 */

package poorskeletongames.alibi_project;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class TutorialFragment extends Fragment implements OnClickListener {
    public interface Listener {
        public void onTutorialScreenDismissed();
    }

    Listener mListener = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_tutorial, container, false);
        v.findViewById(R.id.return_menu).setOnClickListener(this);
        return v;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View view){
        mListener.onTutorialScreenDismissed();
    }
}
