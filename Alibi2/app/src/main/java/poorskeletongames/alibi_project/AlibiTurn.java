/* Source:
 * Google Inc. (2014a). Google Play Game Services: Android Samples: SkeletonTbmp [Software].
 * Available from https://github.com/playgameservices/android-basic-samples
 */

package poorskeletongames.alibi_project;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

//This class packages the user responses and turnCounter into a byte Array that can be
//collected with other turn data, then unpersisted on the next player's turn

public class AlibiTurn {

    public static final String TAG = "EBTurn";

    public int turnCounter;
    public int sharedCounter = 0;
    public String response1 = "";
    public String response2 = "";
    public String response3 = "";
    public String response4 = "";
    public String response5 = "";
    public String response6 = "";
    public String response7 = "";
    public String response8 = "";
    public String response9 = "";
    public String response10 = "";
    public String response11 = "";
    public String response12 = "";
    public String response13 = "";
    public String response14 = "";
    public String response15 = "";
    public String response16 = "";

    public AlibiTurn() {
    }

    // This is the byte array we will write out to the TBMP API.
    public byte[] persist() {
        JSONObject retVal = new JSONObject();

        try {
            retVal.put("turnCounter", turnCounter);
            retVal.put("sharedCounter", sharedCounter);
            retVal.put("response1", response1);
            retVal.put("response2", response2);
            retVal.put("response3", response3);
            retVal.put("response4", response4);
            retVal.put("response5", response5);
            retVal.put("response6", response6);
            retVal.put("response7", response7);
            retVal.put("response8", response8);
            retVal.put("response9", response9);
            retVal.put("response10", response10);
            retVal.put("response11", response11);
            retVal.put("response12", response12);
            retVal.put("response13", response13);
            retVal.put("response14", response14);
            retVal.put("response15", response15);
            retVal.put("response16", response16);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String st = retVal.toString();

        Log.d(TAG, "==== PERSISTING\n" + st);

        return st.getBytes(Charset.forName("UTF-8"));
    }

    // Creates a new instance of AlibiTurn.
    static public AlibiTurn unpersist(byte[] byteArray) {

        if (byteArray == null) {
            Log.d(TAG, "Empty array---possible bug.");
            return new AlibiTurn();
        }

        String st = null;
        try {
            st = new String(byteArray, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return null;
        }

        Log.d(TAG, "====UNPERSIST \n" + st);

        AlibiTurn retVal = new AlibiTurn();

        try {
            JSONObject obj = new JSONObject(st);
            if (obj.has("turnCounter")) {
                retVal.turnCounter = obj.getInt("turnCounter");
            }
            if (obj.has("sharedCounter")){
                retVal.sharedCounter = obj.getInt("sharedCounter");
            }
            if (obj.has("response1")){
                retVal.response1 = obj.getString("response1");
            }
            if (obj.has("response2")){
                retVal.response2 = obj.getString("response2");
            }
            if (obj.has("response3")){
                retVal.response3 = obj.getString("response3");
            }
            if (obj.has("response4")){
                retVal.response4 = obj.getString("response4");
            }
            if (obj.has("response5")){
                retVal.response5 = obj.getString("response5");
            }
            if (obj.has("response6")){
                retVal.response6 = obj.getString("response6");
            }
            if (obj.has("response7")){
                retVal.response7 = obj.getString("response7");
            }
            if (obj.has("response8")){
                retVal.response8 = obj.getString("response8");
            }
            if (obj.has("response9")){
                retVal.response9 = obj.getString("response9");
            }
            if (obj.has("response10")){
                retVal.response10 = obj.getString("response10");
            }
            if (obj.has("response11")){
                retVal.response11 = obj.getString("response11");
            }
            if (obj.has("response12")){
                retVal.response12 = obj.getString("response12");
            }
            if (obj.has("response13")){
                retVal.response13 = obj.getString("response13");
            }
            if (obj.has("response14")){
                retVal.response14 = obj.getString("response14");
            }
            if (obj.has("response15")){
                retVal.response15 = obj.getString("response15");
            }
            if (obj.has("response16")){
                retVal.response16 = obj.getString("response16");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return retVal;
    }
}
