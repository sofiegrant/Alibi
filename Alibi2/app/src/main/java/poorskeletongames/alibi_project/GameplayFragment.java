/* Source:
 * Google Inc. (2014a). Google Play Game Services: Android Samples: SkeletonTbmp [Software].
 * Available from https://github.com/playgameservices/android-basic-samples
 */

package poorskeletongames.alibi_project;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class GameplayFragment extends Fragment implements OnClickListener, TextWatcher {
    private static EditText mWordInput;
    private Button mDoneButton;
    private Button mCancelButton;
    private Button mLeaveButton;

    int turnFromBundle;

    public interface Listener {
        public void onCancelClicked();
        public void onDoneClicked();
        public void onLeaveClicked();
    }

    public interface TextWatcher {
        public void setResponse(String s);
    }

    Listener mListener = null;
    TextWatcher mTextWatcher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gameplay, container, false);
        mDoneButton = (Button) v.findViewById(R.id.done_button);
        mDoneButton.setOnClickListener(this);
        mCancelButton = (Button) v.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(this);
        mLeaveButton = (Button) v.findViewById(R.id.leave_button);
        mLeaveButton.setOnClickListener(this);
        mWordInput = (EditText) v.findViewById(R.id.input_box);
        mWordInput.addTextChangedListener(this);

        return v;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    public void setTextWatcher(TextWatcher t) { mTextWatcher = t;}

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle saveBundle) {
        saveBundle.putInt("myTurnCounter", turnFromBundle);
    }

    @Override
    public void onViewStateRestored(Bundle saveBundle) {
        super.onViewStateRestored(saveBundle);

        if (saveBundle != null) {
            turnFromBundle = saveBundle.getInt("myTurnCounter");
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mDoneButton) {
            mListener.onDoneClicked();
        } else if (view == mCancelButton) {
            mListener.onCancelClicked();
        } else if (view == mLeaveButton) {
            mListener.onLeaveClicked();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        mTextWatcher.setResponse(charSequence.toString());
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }
}