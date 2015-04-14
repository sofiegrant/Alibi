/* Sources:
 * Google Inc. (2014a). Google Play Game Services: Android Samples: SkeletonTbmp [Software].
 * Available from https://github.com/playgameservices/android-basic-samples
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
import android.widget.TextView;

public class WinFragment extends Fragment implements OnClickListener {
    //This displays the final story
    private TextView storyTV;

    //This contains the text of the story
    private String [] storyArray = {"Detective Lance had blown the case wide open. ", "", " "
            + "after disappearing during ", "", " was found in ", "", " down by ", "", " marked "
            + "with a clear set of fingerprints. \"I\'ve got the fingerprint results right here!\""
            + "She exclaimed. Detective Chase looked up from her ", "", ". \"Already? Okay, time "
            + "to see who committed the robbery. Remember, I bet it was ", "", ".\" \"And I think "
            + "it\'s ", "", ",\" said Detective Lance. The two detectives ripped open ", "", " and "
            + "read the results. Detective Montoya stood behind them smiling. \"You owe me ", "",
            " and ", "", "!\" \"How could ", "", " have done it? They were in ", "", " at " +
            "the time of the robbery,\" exclaimed Detective Chase. \"Simple! They robbed ", "",
            " and agreed to split the profits with ", "", " if they provided an alibi,\" explained "
            + "Detective Montoya. \"Pay up! I'm buying ", "", " and ", "", " to celebrate.\""};

    //This String array contains the users' responses
    String [] turnFromBundle = new String [16];
    //This String will contain the completed story
    String storyString;

    public interface Listener {
            public void onWinScreenDismissed();
    }

    Listener mListener = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_win, container, false);
        v.findViewById(R.id.return_menu_win).setOnClickListener(this);
        //Collect the user responses (passed by MainActivity)
        turnFromBundle = getArguments().getStringArray("theResponses");
        //Insert the user responses into the story Array
        storyArray[1] = turnFromBundle[0];
        storyArray[3] = turnFromBundle[1];
        storyArray[5] = turnFromBundle[2];
        storyArray[7] = turnFromBundle[3];
        storyArray[9] = turnFromBundle[4];
        storyArray[11] = turnFromBundle[5];
        storyArray[13] = turnFromBundle[6];
        storyArray[15] = turnFromBundle[7];
        storyArray[17] = turnFromBundle[8];
        storyArray[19] = turnFromBundle[9];
        storyArray[21] = turnFromBundle[10];
        storyArray[23] = turnFromBundle[11];
        storyArray[25] = turnFromBundle[12];
        storyArray[27] = turnFromBundle[13];
        storyArray[29] = turnFromBundle[14];
        storyArray[31] = turnFromBundle[15];

        //Convert the story Array into a String
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < storyArray.length; i++) {
            result.append( storyArray[i] );
        }
        storyString = result.toString();

        storyTV = ((TextView) v.findViewById(R.id.story));
        //Add the story to the TextView
        storyTV.setText(storyString);
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
        mListener.onWinScreenDismissed();
    }
}