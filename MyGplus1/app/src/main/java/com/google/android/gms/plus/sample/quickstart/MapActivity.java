package com.google.android.gms.plus.sample.quickstart;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.NotifyTransactionStatusRequest;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.BuyButtonText;
import com.google.android.gms.wallet.fragment.Dimension;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.android.gms.wallet.fragment.WalletFragmentStyle;


public class MapActivity extends FragmentActivity implements

        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnInfoWindowClickListener {

    private  boolean clicked = false;
    private  boolean clickedMarker = false;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */


    private SupportWalletFragment mWalletFragment;
    private MaskedWallet mMaskedWallet;

    public static final int MASKED_WALLET_REQUEST_CODE = 888;

   //Full wallet stuff
    public static final int FULL_WALLET_REQUEST_CODE = 889;
    private FullWallet mFullWallet;

    public static final String WALLET_FRAGMENT_ID = "wallet_fragment";
    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
        } else {
            Toast.makeText(this, "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }

        mWalletFragment = (SupportWalletFragment) getSupportFragmentManager().findFragmentByTag(WALLET_FRAGMENT_ID);



        WalletFragmentInitParams startParams;
        WalletFragmentInitParams.Builder startParamsBuilder = WalletFragmentInitParams.newBuilder()
                .setMaskedWalletRequest(generateMaskedWalletRequest())
                .setMaskedWalletRequestCode(MASKED_WALLET_REQUEST_CODE);

        startParams = startParamsBuilder.build();

        if(mWalletFragment == null) {
            WalletFragmentStyle walletFragmentStyle = new WalletFragmentStyle()
                    .setBuyButtonText(BuyButtonText.BUY_WITH_GOOGLE)
                    .setBuyButtonWidth(Dimension.MATCH_PARENT);

            WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
                    .setEnvironment(WalletConstants.ENVIRONMENT_SANDBOX)
                    .setFragmentStyle(walletFragmentStyle)
                    .setTheme(WalletConstants.THEME_HOLO_LIGHT)
                    .setMode(WalletFragmentMode.BUY_BUTTON)
                    .build();

            mWalletFragment = SupportWalletFragment.newInstance(walletFragmentOptions);

            mWalletFragment.initialize(startParams);

        }

/*
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.wallet_button_holder, mWalletFragment, WALLET_FRAGMENT_ID)
                .commit();
*/


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Wallet.API, new Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletConstants.ENVIRONMENT_SANDBOX)
                        .setTheme(WalletConstants.THEME_HOLO_LIGHT)
                        .build())
                .build();

    }

    private void loadFragment() {

        Log.i("android", "in load fragment");
       // FragmentTransaction ft = getFragmentManager().findFragmentByTag(WALLET_FRAGMENT_ID);
       /* FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        if(fm.findFragmentByTag(WALLET_FRAGMENT_ID).isHidden()) {
            ft.show(fm.findFragmentByTag(WALLET_FRAGMENT_ID));
        }
        else {
            ft.hide(fm.findFragmentByTag(WALLET_FRAGMENT_ID));
        }*/

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.wallet_button_holder, mWalletFragment, WALLET_FRAGMENT_ID)
                .commit();

    }


    private void clearFragment() {
        Log.i("android","in clear fragment");

        getSupportFragmentManager().beginTransaction()
                .remove(mWalletFragment)
                .commit();
    }

    private MaskedWalletRequest generateMaskedWalletRequest() {
        MaskedWalletRequest maskedWalletRequest =
                MaskedWalletRequest.newBuilder()
                        .setMerchantName("Google I/O Codelab")
                        .setPhoneNumberRequired(true)
                        .setShippingAddressRequired(true)
                        .setCurrencyCode("USD")
                        .setShouldRetrieveWalletObjects(true)
                        .setEstimatedTotalPrice("10.00")
                        .setCart(Cart.newBuilder()
                                .setCurrencyCode("USD")
                                .setTotalPrice("10.00")
                                .addLineItem(LineItem.newBuilder()
                                        .setCurrencyCode("USD")
                                        .setDescription("Google I/O Sticker")
                                        .setQuantity("1")
                                        .setUnitPrice("10.00")
                                        .setTotalPrice("10.00")
                                        .build())
                                .build())
                        .build();

        return maskedWalletRequest;
    }

    private FullWalletRequest generateFullWalletRequest(String googleTransactionId) {
        FullWalletRequest fullWalletRequest = FullWalletRequest.newBuilder()
                .setGoogleTransactionId(googleTransactionId)
                .setCart(Cart.newBuilder()
                        .setCurrencyCode("USD")
                        .setTotalPrice("10.10")
                        .addLineItem(LineItem.newBuilder()
                                .setCurrencyCode("USD")
                                .setDescription("Google I/O Sticker")
                                .setQuantity("1")
                                .setUnitPrice("10.00")
                                .setTotalPrice("10.00")
                                .build())
                        .addLineItem(LineItem.newBuilder()
                                .setCurrencyCode("USD")
                                .setDescription("Tax")
                                .setRole(LineItem.Role.TAX)
                                .setTotalPrice(".10")
                                .build())
                        .build())
                .build();
        return fullWalletRequest;
    }

    public void requestFullWallet(View view) {
        Wallet.Payments.loadFullWallet(mGoogleApiClient,
                generateFullWalletRequest(mMaskedWallet.getGoogleTransactionId()),
                FULL_WALLET_REQUEST_CODE);
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            //Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
           // map.setMyLocationEnabled(true);

            CameraUpdate center =
                    CameraUpdateFactory.newLatLng(new LatLng(40.807654, -73.962635));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

            map.moveCamera(center);
            map.animateCamera(zoom);
            map.addMarker(new MarkerOptions().position(new LatLng(40.807654, -73.962635))
                                            .title("Columbia")
                                            .snippet("clicked_it"));
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(Marker marker) {
                    marker.setTitle("Columbia_marker");
                    marker.setSnippet("snippet__marker");
                    if(!clickedMarker) {

                        marker.showInfoWindow();

                        clickedMarker=true;
                    }
                    else
                    {
                        marker.hideInfoWindow();
                        clickedMarker=false;
                    }
                    return true;
                }
            });


            map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
            map.setOnInfoWindowClickListener(this);


         // Now that map has loaded, let's get our location!
          /*  mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)

                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();*/

           // connectClient();
        } else {
            Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    protected void connectClient() {
        if (isGooglePlayServicesAvailable() && mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        connectClient();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /*
     * Handle results returned to the FragmentActivity by Google Play services
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                /*
                 * If the result code is Activity.RESULT_OK, try to connect again
                 */
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mGoogleApiClient.connect();
                        break;
                }
                break;
            case MASKED_WALLET_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mMaskedWallet =
                                data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                               // new SendMailTLS(MainActivity.user_name).execute();
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    default:
                        Toast.makeText(this, "An Error Occurred", Toast.LENGTH_LONG).show();
                        break;
                }
                break;
            case FULL_WALLET_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mFullWallet =
                                data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);
                       if(mFullWallet.getProxyCard().getPan().toString()!=null) {
                        Toast.makeText(this,
                                mFullWallet.getProxyCard().getPan().toString(),
                                Toast.LENGTH_LONG).show();
                           new SendMailTLS(MainActivity.user_name).execute();
                       }
                        Wallet.Payments.notifyTransactionStatus(mGoogleApiClient,
                                generateNotifyTransactionStatusRequest(mFullWallet.getGoogleTransactionId(),
                                        NotifyTransactionStatusRequest.Status.SUCCESS));
                        break;
                    default:
                        Toast.makeText(this, "An Error Occurred", Toast.LENGTH_LONG).show();
                        break;
                }
                break;
            case WalletConstants.RESULT_ERROR:
                Toast.makeText(this, "An Error Occurred", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    /*
     * Called by Location Services when the request to connect the client
     * finishes successfully. At this point, you can request the current
     * location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
     //   Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
     //   if (location != null) {
      //      Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
       //     LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
      //      CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
      //      map.animateCamera(cameraUpdate);
      //      startLocationUpdates();
      //  } else {
       //     Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
       // }
    }

    public static NotifyTransactionStatusRequest generateNotifyTransactionStatusRequest(
            String googleTransactionId, int status) {
        return NotifyTransactionStatusRequest.newBuilder()
                .setGoogleTransactionId(googleTransactionId)
                .setStatus(status)
                .build();
    }

    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    /*
     * Called by Location Services if the connection to the location client
     * drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects. If the error
         * has a resolution, try sending an Intent to start a Google Play
         * services activity that can resolve error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if(!clicked) {
            loadFragment();
            marker.getTitle().toString();
            marker.getSnippet();
            //Toast.makeText(this, marker.getTitle() + marker.getSnippet(), Toast.LENGTH_LONG).show();
            clicked = true;
        }
        else {
            Toast.makeText(this, "", Toast.LENGTH_SHORT).cancel();
            clearFragment();
            clicked=false;
        }
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }

    }
}