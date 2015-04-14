/* Sources:
 * Google Inc. (2014a). Google Play Game Services: Android Samples: SkeletonTbmp [Software].
 * Available from https://github.com/playgameservices/android-basic-samples
 * Google Inc. (2014b). Google Play Game Services: Android Samples: TypeANumber [Software].
 * Available from https://github.com/playgameservices/android-basic-samples
 */

package poorskeletongames.alibi_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements GameplayFragment.Listener,
        GameplayFragment.TextWatcher, MainMenuFragment.Listener,TutorialFragment.Listener,
        WinFragment.Listener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnInvitationReceivedListener,
        OnTurnBasedMatchUpdateReceivedListener {

    // Tag for debug logging
    public final String TAG = "Ali";

    // Client used to interact with Google APIs
    private GoogleApiClient mGoogleApiClient;

    // Not currently resolving a connection failure
    private boolean mResolvingConnectionFailure = false;

    // mSignInClicked has not been selected
    private boolean mSignInClicked = false;

    // Automatically start the sign-in flow when the Activity starts
    private boolean mAutoStartSignInFlow = true;

    //Current turn-based match
    private TurnBasedMatch mTurnBasedMatch;

    private AlertDialog mAlertDialog;

    // Request codes we use when invoking an external activity
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_SELECT_PLAYERS = 10000;
    private static final int RC_LOOK_AT_MATCHES = 10001;

    //Show turn API
    public boolean isDoingTurn = false;

    // How long to show toasts.
    final static int TOAST_DELAY = Toast.LENGTH_SHORT;

    //Current match (null if not loaded)
    public TurnBasedMatch mMatch;

    //This is the current match data after being unpersisted. Do not retain references to match
    //data once you have taken an action on the match, such as takeTurn()
    public AlibiTurn mTurnData;

    //There are four primary fragments seen in the game
    GameplayFragment mGFragment;
    MainMenuFragment mMFragment;
    TutorialFragment mTFragment;
    WinFragment mWFragment;

    //A String representing a single user response
    private String mResponse;
    //Used to retrieve the user's response during the turn and persist it to a byte Array
    private String mSaveResponse;
    //An Array of user responses
    private String[] mResponseArray = new String [16];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the Google API Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        // Create the fragments
        mGFragment = new GameplayFragment();
        mMFragment = new MainMenuFragment();
        mTFragment = new TutorialFragment();
        mWFragment = new WinFragment();

        // Listen to fragment events
        mGFragment.setListener(this);
        mGFragment.setTextWatcher(this);
        mMFragment.setListener(this);
        mTFragment.setListener(this);
        mWFragment.setListener(this);

        //Add FragmentManager and add the MainMenuFragment to start
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mMFragment)
                .commit();
    }

    //Switch UI to the given fragment
    void switchToFragment(Fragment newFrag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newFrag)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart(): connecting");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop(): disconnecting");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        // Set the MainMenu fragment
        Player p = Games.Players.getCurrentPlayer(mGoogleApiClient);
        String displayName;
        if (p == null) {
            Log.w(TAG, "mGamesClient.getCurrentPlayer() is NULL!");
            displayName = "???";
        } else {
            displayName = p.getDisplayName();
        }
        mMFragment.setGreeting("Hello, " + displayName);

        // Retrieve the TurnBasedMatch from the bundle
        if (bundle != null) {
            mTurnBasedMatch = bundle.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);
            if (mTurnBasedMatch != null) {
                if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
                    Log.d(TAG, "Warning: accessing TurnBasedMatch when not connected");
                }
                updateMatch(mTurnBasedMatch);
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): attempting to connect");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed(): attempting to resolve");
        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed(): already resolving");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;
            if (!BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }
    }

    //MainMenuFragment: Google Play sign in/sign out

    //Check whether the player is signed in
    private boolean isSignedIn() {
        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }

    //Orchestrates Google Play sign in
    @Override
    public void onSignInButtonClicked() {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    //Orchestrates Google Play sign out
    @Override
    public void onSignOutButtonClicked() {
        Boolean checkConnection = mGoogleApiClient.isConnected();
        mSignInClicked = false;
        if (checkConnection == true) {
            Games.signOut(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        } else if (checkConnection == false){
            //If the player isn't currently signed in, this button should do nothing
        }
        mMFragment.setGreeting(getString(R.string.signed_out_greeting));
    }

    //MainMenuFragment & TutorialFragment

    //Switch the View from the MainMenu fragment to the Tutorial fragment
    @Override
    public void onTutorialRequested(){
        switchToFragment(mTFragment);
    }

    //From TutorialFragment, switch back to the MainMenu fragment
    @Override
    public void onTutorialScreenDismissed(){
        switchToFragment(mMFragment);
    }

    //MainMenuFragment: Inbox

    //Begin setup for the default Google play inbox UI
    @Override
    public void onCheckInboxRequested(){
        if (isSignedIn()) {
            Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
            startActivityForResult(intent, RC_LOOK_AT_MATCHES);
        } else {
            BaseGameUtils.makeSimpleDialog(this, getString(R.string.inbox_not_available)).show();
        }
    }

    //MainMenuFragment: Alibi

    //Begin setup for the default Google Play player match UI
    @Override
    public void onPlayAlibiRequested() {
        if (isSignedIn()) {
        Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient,
                1, 1, true);
        startActivityForResult(intent, RC_SELECT_PLAYERS);}
        else {
            BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_not_available)).show();
        }
    }

    //GameplayFragment: In-Game Buttons

    //Cancel the game (leave it and remove it from your queue) and return to MainMenuFragment
    public void onCancelClicked() {
        Games.TurnBasedMultiplayer.cancelMatch(mGoogleApiClient, mMatch.getMatchId())
                .setResultCallback(new ResultCallback<TurnBasedMultiplayer.CancelMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.CancelMatchResult result) {
                        processResult(result);
                    }
                });
        isDoingTurn = false;
        switchToFragment(mMFragment);
    }

    // Leave the game during your turn and return to MainMenuFragment
    public void onLeaveClicked() {
        String nextParticipantId = getNextParticipantId();
        Games.TurnBasedMultiplayer.leaveMatchDuringTurn(mGoogleApiClient, mMatch.getMatchId(),
                nextParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.LeaveMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.LeaveMatchResult result) {
                        processResult(result);
                    }
                });
        switchToFragment(mMFragment);
    }

    // Upload the new gamestate, take a turn, and pass it on to the next player
    public void onDoneClicked() {
        String nextParticipantId = getNextParticipantId();
        // Create the next turn
        mTurnData.turnCounter += 1;
        //Increment the other turn counter
        mTurnData.sharedCounter = mTurnData.sharedCounter + 1;

        //If next turn is
        if (mTurnData.sharedCounter != 17 || mTurnData.sharedCounter != 18) {
            //Preserve the player's response
            mSaveResponse = getResponse();
            if (mTurnData.sharedCounter == 1) {
                mTurnData.response1 = mSaveResponse;
            } else if (mTurnData.sharedCounter == 2) {
                mTurnData.response2 = mSaveResponse;
            } else if (mTurnData.sharedCounter == 3) {
                mTurnData.response3 = mSaveResponse;
            } else if (mTurnData.sharedCounter == 4) {
                mTurnData.response4 = mSaveResponse;
            } else if (mTurnData.sharedCounter == 5) {
                mTurnData.response5 = mSaveResponse;
            } else if (mTurnData.sharedCounter == 6) {
                mTurnData.response6 = mSaveResponse;
            } else if (mTurnData.sharedCounter == 7) {
                mTurnData.response7 = mSaveResponse;
            } else if (mTurnData.sharedCounter == 8) {
                mTurnData.response8 = mSaveResponse;
            } else if (mTurnData.sharedCounter == 9) {
                mTurnData.response9 = mSaveResponse;
            } else if (mTurnData.sharedCounter == 10) {
                mTurnData.response10 = mSaveResponse;
            } else if (mTurnData.sharedCounter == 11) {
                mTurnData.response11 = mSaveResponse;
            } else if (mTurnData.sharedCounter == 12) {
                mTurnData.response12 = mSaveResponse;
            } else if (mTurnData.sharedCounter == 13) {
                mTurnData.response13 = mSaveResponse;
            } else if (mTurnData.sharedCounter == 14) {
                mTurnData.response14 = mSaveResponse;
            } else if (mTurnData.sharedCounter == 15) {
                mTurnData.response15 = mSaveResponse;
            } else if (mTurnData.sharedCounter == 16) {
                mTurnData.response2 = mSaveResponse;
            }
        } //If it is turn 17 or 18, do not collect any information

        Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mMatch.getMatchId(),
                mTurnData.persist(), nextParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });
        mTurnData = null;
        switchToFragment(mMFragment);
    }

    //From WinFragment, switch back to the GameplayFragment
    public void onWinScreenDismissed() {
        switchToFragment(mGFragment);
    }

    // Generic warning/info dialog
    public void showWarning(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(title).setMessage(message);

        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close the current activity
                    }
                });

        // create alert dialog
        mAlertDialog = alertDialogBuilder.create();

        // show it
        mAlertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode,
                        R.string.signin_failure, R.string.signin_other_error);
            }
        } else if (requestCode == RC_LOOK_AT_MATCHES) {
            // Returning from the 'Select Match' dialog
            if (resultCode != Activity.RESULT_OK) {
                // user canceled
                return;
        }

        TurnBasedMatch match = intent.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);

        if (match != null) {
            updateMatch(match);
        }

        Log.d(TAG, "Match = " + match);

        } else if (requestCode == RC_SELECT_PLAYERS) {
            // Returned from 'Select players to Invite' dialog
            if (resultCode != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            // get the invitee list
            final ArrayList<String> invitees = intent
                    .getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

            // get automatch criteria
            Bundle autoMatchCriteria = null;

            int minAutoMatchPlayers = intent.getIntExtra(
                    Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = intent.getIntExtra(
                    Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }

            TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                    .addInvitedPlayers(invitees)
                    .setAutoMatchCriteria(autoMatchCriteria).build();

            // Start the match
            Games.TurnBasedMultiplayer.createMatch(mGoogleApiClient, tbmc).setResultCallback(
                    new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                        @Override
                        public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                            processResult(result);
                        }
                    });
        }
    }

    // startMatch() happens in response to the createTurnBasedMatch()
    // above. This is only called on success, so we should have a
    // valid match object. We're taking this opportunity to setup the
    // game, saving our initial state. Calling takeTurn() will
    // callback to OnTurnBasedMatchUpdated(), which will show the game
    // UI.
    public void startMatch(TurnBasedMatch match) {
        mTurnData = new AlibiTurn();
        mMatch = match;
        //Increment the turn to 1
        mTurnData.sharedCounter = mTurnData.sharedCounter++;

        String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);

        Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, match.getMatchId(),
                mTurnData.persist(), myParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });
    }

    /**
     * Get the next participant. In this function, we assume that we are
     * round-robin, with all known players going before all automatch players.
     * This is not a requirement; players can go in any order. However, you can
     * take turns in any order.
     *
     * @return participantId of next player, or null if automatching
     */
    public String getNextParticipantId() {

        String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);

        ArrayList<String> participantIds = mMatch.getParticipantIds();

        int desiredIndex = -1;

        for (int i = 0; i < participantIds.size(); i++) {
            if (participantIds.get(i).equals(myParticipantId)) {
                desiredIndex = i + 1;
            }
        }

        if (desiredIndex < participantIds.size()) {
            return participantIds.get(desiredIndex);
        }

        if (mMatch.getAvailableAutoMatchSlots() <= 0) {
            // You've run out of automatch slots, so we start over.
            return participantIds.get(0);
        } else {
            // You have not yet fully automatched, so null will find a new
            // person to play against.
            return null;
        }
    }

    // This is the main function that gets called when players choose a match from the inbox,
    // or else create a match and want to start it.
    public void updateMatch(TurnBasedMatch match) {
        mMatch = match;

        int status = match.getStatus();
        int turnStatus = match.getTurnStatus();

        switch (status) {
            case TurnBasedMatch.MATCH_STATUS_CANCELED:
                showWarning("Canceled", "This game was canceled");
                return;
            case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                showWarning("Expired", "This game is expired.");
                return;
            case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
                showWarning("Waiting for Auto-Match.",
                        "Waiting for an auto-match partner to join the game.");
                return;
            case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE) {
                    showWarning("Complete!", "This game has been completed");
                    break;
                }
        }
        // Check on turn status.
        switch (turnStatus) {
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                mTurnData = AlibiTurn.unpersist(mMatch.getData());
                if (mTurnData.sharedCounter == 0 || mTurnData.sharedCounter == 1 ||
                        mTurnData.sharedCounter == 2 || mTurnData.sharedCounter == 3 ||
                        mTurnData.sharedCounter == 4 || mTurnData.sharedCounter == 5 ||
                        mTurnData.sharedCounter == 6 || mTurnData.sharedCounter == 7 ||
                        mTurnData.sharedCounter == 8 || mTurnData.sharedCounter == 9 ||
                        mTurnData.sharedCounter == 10 || mTurnData.sharedCounter == 11 ||
                        mTurnData.sharedCounter == 12 || mTurnData.sharedCounter == 13 ||
                        mTurnData.sharedCounter == 14 || mTurnData.sharedCounter == 15){
                    switchToFragment(mGFragment);
                    return;
                } else if (mTurnData.sharedCounter == 16 || mTurnData.sharedCounter == 17) {
                    //If the game is over,
                    //Bundle the responses and pass them to WinFragment
                    Bundle turnBundle = new Bundle();
                    Log.i("response1", mTurnData.response1);
                    mResponseArray[0] = mTurnData.response1;
                    mResponseArray[1] = mTurnData.response2;
                    mResponseArray[4] = mTurnData.response5;
                    mResponseArray[5] = mTurnData.response6;
                    mResponseArray[6] = mTurnData.response7;
                    mResponseArray[7] = mTurnData.response8;
                    mResponseArray[8] = mTurnData.response9;
                    mResponseArray[9] = mTurnData.response10;
                    mResponseArray[10] = mTurnData.response11;
                    mResponseArray[11] = mTurnData.response12;
                    mResponseArray[12] = mTurnData.response13;
                    mResponseArray[13] = mTurnData.response14;
                    mResponseArray[14] = mTurnData.response15;
                    mResponseArray[15] = mTurnData.response16;
                    turnBundle.putStringArray("theResponses", mResponseArray);
                    mWFragment.setArguments(turnBundle);
                    isDoingTurn = true;
                    switchToFragment(mWFragment);
                    return;
                } else if (mTurnData.sharedCounter == 18) {
                    Games.TurnBasedMultiplayer.finishMatch(mGoogleApiClient, mMatch.getMatchId())
                            .setResultCallback
                                    (new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                                @Override
                                public void onResult(TurnBasedMultiplayer.UpdateMatchResult result)
                                {
                                    processResult(result);
                                }
                            });
                    //Reset all data manually here as well, just to be on the safe side
                    mTurnData.sharedCounter = 0;
                    mTurnData.response1 = "";
                    mTurnData.response2 = "";
                    mTurnData.response3 = "";
                    mTurnData.response4 = "";
                    mTurnData.response5 = "";
                    mTurnData.response6 = "";
                    mTurnData.response7 = "";
                    mTurnData.response8 = "";
                    mTurnData.response9 = "";
                    mTurnData.response10 = "";
                    mTurnData.response11 = "";
                    mTurnData.response12 = "";
                    mTurnData.response13 = "";
                    mTurnData.response14 = "";
                    mTurnData.response15 = "";
                    mTurnData.response16 = "";
                    isDoingTurn = false;
                    switchToFragment(mMFragment);
                }
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                // Should return results.
                showWarning("Not Your Turn", "It's not your turn.");
                break;
            case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
                showWarning("Waiting for Invitations", "Still waiting for invitations.");
        }
        mTurnData = null;
    }

    private void processResult(TurnBasedMultiplayer.CancelMatchResult result) {
        switchToFragment(mMFragment);
        if (!checkStatusCode(null, result.getStatus().getStatusCode())) {
            return;
        }
        isDoingTurn = false;
        showWarning("Match",
                "This match is canceled.  All other players will have their game ended.");
    }

    private void processResult(TurnBasedMultiplayer.InitiateMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }
        if (match.getData() != null) {
            // This is a game that has already started, so I'll just start
            updateMatch(match);
            return;
        }
        startMatch(match);
    }

    private void processResult(TurnBasedMultiplayer.LeaveMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }
        isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);
        showWarning("Left", "You've left this match.");
    }

    public void processResult(TurnBasedMultiplayer.UpdateMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }

        isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);

        if (isDoingTurn) {
            updateMatch(match);
            return;
        }
    }

    // Handle notification events.
    @Override
    public void onInvitationReceived(Invitation invitation) {
        Toast.makeText(
                this,
                "An invitation has arrived from "
                        + invitation.getInviter().getDisplayName(), TOAST_DELAY)
                .show();
    }

    @Override
    public void onInvitationRemoved(String invitationId) {
        Toast.makeText(this, "An invitation was removed.", TOAST_DELAY).show();
    }

    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch match) {
        Toast.makeText(this, "A match was updated.", TOAST_DELAY).show();
    }

    @Override
    public void onTurnBasedMatchRemoved(String matchId) {
        Toast.makeText(this, "A match was removed.", TOAST_DELAY).show();
    }

    public void showErrorMessage(TurnBasedMatch match, int statusCode,
                                 int stringId) {
        showWarning("Warning", getResources().getString(stringId));
    }

    // Returns false if something went wrong, probably. This should handle
    // more cases, and probably report more accurate results.
    private boolean checkStatusCode(TurnBasedMatch match, int statusCode) {
        switch (statusCode) {
            case GamesStatusCodes.STATUS_OK:
                return true;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
                return true;
            case GamesStatusCodes.STATUS_MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                showErrorMessage(match, statusCode,
                        R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_FAILED:
                showErrorMessage(match, statusCode,
                        R.string.network_error_operation_failed);
                break;
            case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
                showErrorMessage(match, statusCode,
                        R.string.client_reconnect_required);
                break;
            case GamesStatusCodes.STATUS_INTERNAL_ERROR:
                showErrorMessage(match, statusCode, R.string.internal_error);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_INACTIVE_MATCH:
                showErrorMessage(match, statusCode,
                        R.string.match_error_inactive_match);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_LOCALLY_MODIFIED:
                showErrorMessage(match, statusCode,
                        R.string.match_error_locally_modified);
                break;
            default:
                showErrorMessage(match, statusCode, R.string.unexpected_status);
                Log.d(TAG, "Did not have warning or string to deal with: "
                        + statusCode);
        }
        return false;
    }

    //Retrieves the String value of user's response
    public String getResponse(){
        return mResponse;
    }

    //Retrieves the user's response from GameplayFragment
    public void setResponse(String response){
        mResponse = response;
        Log.i("theResponse", mResponse);
    }
}