package com.nicefontaine.matcha.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nicefontaine.matcha.MatchaApp;
import com.nicefontaine.matcha.R;
import com.nicefontaine.matcha.data.Coordinate;
import com.nicefontaine.matcha.data.DefaultEventBus;
import com.nicefontaine.matcha.data.OnLocationEvent;
import com.nicefontaine.matcha.data.sources.LocationDataSource;
import com.nicefontaine.matcha.data.sources.TicketDataSource;
import com.nicefontaine.matcha.network.TicketResponse;
import com.nicefontaine.matcha.services.BestLocationService;
import com.nicefontaine.matcha.utils.DialogManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static android.widget.LinearLayout.HORIZONTAL;
import static com.nicefontaine.matcha.utils.LocationUtils.checkLocationPermission;
import static com.nicefontaine.matcha.utils.LocationUtils.checkLocationServices;

public class HomeActivity extends FragmentActivity implements
        OnMapReadyCallback, TicketDataSource.TicketCallback {

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private LocationManager locationManager;
    private GoogleMap googleMap;
    private Intent bestLocationService;
    private List<TicketResponse.Ticket> tickets;
    private TicketAdapter adapter;

    @BindView(R.id.coordinator) protected CoordinatorLayout coordinator;
    @BindView(R.id.recycler) protected RecyclerView recycler;

    @Inject protected DefaultEventBus eventBus;
    @Inject protected TicketDataSource ticketDataSource;
    @Inject protected LocationDataSource locationDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ((MatchaApp) this.getApplicationContext()).getAppComponent().inject(this);
        ButterKnife.bind(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        bestLocationService = new Intent(this, BestLocationService.class);
        initRecycler();
    }

    private void initRecycler() {
        if (tickets == null) tickets = new ArrayList<>();
        this.adapter = new TicketAdapter(this, tickets);
        recycler.setLayoutManager(new LinearLayoutManager(this, HORIZONTAL, false));
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
        if (checkLocationServicesEnabled()) {
            startService(bestLocationService);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(bestLocationService);
        eventBus.unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEvent(OnLocationEvent event) {
        stopService(bestLocationService);
        Location location = event.getLocation();
        List<Coordinate> coordinates = new ArrayList<>();
        Coordinate coordinate = new Coordinate();
        coordinate.latitude = location.getLatitude();
        coordinate.longitude = location.getLongitude();
        coordinates.add(coordinate);
        ticketDataSource.getTickets(coordinates, this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng berlin = new LatLng(52, 13);
        this.googleMap.addMarker(new MarkerOptions().position(berlin).title("Marker in Berlin"));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(berlin));

        this.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_light));
        this.googleMap.setMyLocationEnabled(checkLocationPermission(this));
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    private boolean checkLocationServicesEnabled() {
        if (!checkLocationServices(locationManager)) {
            DialogManager.showEnableGPSDialog(this);
            return false;
        }
        if (!checkLocationPermission(this)) {
            getLocationPermissions();
            return false;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void getLocationPermissions() {
        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_FINE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int code, @Nullable String permissions[],
                                           @Nullable int[] result) {
        if (code == PERMISSION_REQUEST_FINE_LOCATION && isGranted(result)) {
            startService(bestLocationService);
        }
    }

    private boolean isGranted(int[] result) {
        return result != null && result.length > 0 &&
                result[0] == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public void onTickets(List<TicketResponse.Ticket> tickets) {
        adapter.setTickets(tickets);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onError() {
        Snackbar.make(coordinator, R.string.connection_error, LENGTH_LONG).show();
    }
}
