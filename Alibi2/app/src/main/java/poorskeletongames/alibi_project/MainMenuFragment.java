/* Source:
 * Google Inc. (2014b). Google Play Game Services: Android Samples: TypeANumber [Software].
 * Available from https://github.com/playgameservices/android-basic-samples
 */

package poorskeletongames.alibi_project;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainMenuFragment extends Fragment implements OnClickListener {
    String mGreeting = "Hello, anonymous user (not signed in)";

     public interface Listener {
         public void onPlayAlibiRequested();
         public void onCheckInboxRequested();
         public void onSignInButtonClicked();
         public void onSignOutButtonClicked();
         public void onTutorialRequested();
     }

    Listener mListener = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_mainmenu, container, false);
        final int[] CLICKABLES = new int[] {
                R.id.play_alibi_button, R.id.check_inbox_button,
                R.id.tutorial_button,
                R.id.sign_in_button, R.id.sign_out_button
        };
        for (int i : CLICKABLES) {
            v.findViewById(i).setOnClickListener(this);
        }
        return v;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUi();
    }

    public void setGreeting(String greeting) {
        mGreeting = greeting;
        updateUi();
    }

    void updateUi() {
        if (getActivity() == null) return;
        TextView tv = (TextView) getActivity().findViewById(R.id.hello);
        if (tv != null) tv.setText(mGreeting);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_alibi_button:
                mListener.onPlayAlibiRequested();
                break;
            case R.id.check_inbox_button:
                mListener.onCheckInboxRequested();
                break;
            case R.id.sign_in_button:
                mListener.onSignInButtonClicked();
                break;
            case R.id.sign_out_button:
                mListener.onSignOutButtonClicked();
                break;
            case R.id.tutorial_button:
                mListener.onTutorialRequested();
                break;
        }
    }
}