package com.nicefontaine.matcha.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.nicefontaine.matcha.MatchaApp;
import com.nicefontaine.matcha.R;
import com.nicefontaine.matcha.data.Coordinate;
import com.nicefontaine.matcha.data.DefaultEventBus;
import com.nicefontaine.matcha.data.OnLocationEvent;
import com.nicefontaine.matcha.data.sources.LocationDataSource;
import com.nicefontaine.matcha.data.sources.ShapeDataSource;
import com.nicefontaine.matcha.data.sources.TicketDataSource;
import com.nicefontaine.matcha.network.Place;
import com.nicefontaine.matcha.network.Ticket;
import com.nicefontaine.matcha.services.BestLocationService;
import com.nicefontaine.matcha.utils.DialogManager;
import com.nicefontaine.matcha.utils.MapDrawUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static android.widget.LinearLayout.HORIZONTAL;
import static com.nicefontaine.matcha.utils.LocationUtils.checkLocationPermission;
import static com.nicefontaine.matcha.utils.LocationUtils.checkLocationServices;

public class HomeActivity extends FragmentActivity implements
        OnMapReadyCallback,
        TicketDataSource.TicketCallback,
        LocationDataSource.LocationCallback,
        TicketAdapter.SelectedCallback, ShapeDataSource.ZonesCallback {

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int MAP_PADDING = 100;
    public static final int ANIMATION_DURATION_IN_MS = 400;

    private LocationManager locationManager;
    private GoogleMap googleMap;
    private Intent bestLocationService;
    private List<Ticket> tickets;
    private TicketAdapter adapter;
    private List<Coordinate> viaPointsCoordinates;
    private List<LatLng> viaPointsLatLng;
    private List<Marker> markers;

    @BindView(R.id.coordinator) protected CoordinatorLayout coordinator;
    @BindView(R.id.recycler) protected RecyclerView recycler;
    @BindView(R.id.position_edittext) protected EditText positionEditText;

    @Inject protected DefaultEventBus eventBus;
    @Inject protected TicketDataSource ticketDataSource;
    @Inject protected LocationDataSource locationDataSource;
    @Inject protected ShapeDataSource shapeDataSource;
    private Map<String, List<LatLng>> zones;
    private boolean mapReady;
    private Polygon polygonA;
    private Polygon polygonB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ((MatchaApp) this.getApplicationContext()).getAppComponent().inject(this);
        ButterKnife.bind(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        bestLocationService = new Intent(this, BestLocationService.class);
        initRecycler();
    }

    private void initRecycler() {
        if (tickets == null) tickets = new ArrayList<>();
        this.adapter = new TicketAdapter(this, this, tickets);
        recycler.setLayoutManager(new LinearLayoutManager(this, HORIZONTAL, false));
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        positionEditText.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_GO) {
                        searchGeopointToLocationName();
                        hideKeyboard();
                        return true;
                    }
                    return false;
                });
        shapeDataSource.getShapes(this);
        mapReady = false;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void searchGeopointToLocationName() {
        String query = positionEditText.getText().toString();
        locationDataSource.getLocation(query, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);

        viaPointsCoordinates = new ArrayList<>();
        viaPointsLatLng = new ArrayList<>();
        markers = new ArrayList<>();

        if (checkLocationServicesEnabled()) {
            startService(bestLocationService);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(bestLocationService);
        eventBus.unregister(this);
        MapDrawUtils.clearMarkers(markers);
    }

    @SuppressWarnings("unused")
    public void onEvent(OnLocationEvent event) {
        stopService(bestLocationService);
        Location location = event.getLocation();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        updateTickets(latLng);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        mapReady = true;
        this.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_light));
        this.googleMap.setMyLocationEnabled(checkLocationPermission(this));
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        this.googleMap.setOnMapLongClickListener(this::updateTickets);
    }

    private void updateTickets(LatLng latLng) {
        Coordinate coordinate = new Coordinate();
        coordinate.latitude = latLng.latitude;
        coordinate.longitude = latLng.longitude;
        viaPointsCoordinates.add(coordinate);
        viaPointsLatLng.add(latLng);
        markers.add(MapDrawUtils.drawMarker(googleMap, latLng));
        ticketDataSource.getTickets(viaPointsCoordinates, this);
        if (mapReady) updateCamera();
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
    public void onTickets(List<Ticket> tickets) {
        Timber.e(tickets.toString());
        adapter.setTickets(tickets);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLocation(List<Place> locations) {
        Place place = locations.get(0);
        LatLng latLng = new LatLng(place.latitude, place.longitude);
        updateTickets(latLng);
    }

    private void updateCamera() {
        LatLngBounds bBox = MapDrawUtils.buildBBox(viaPointsLatLng);
        if (mapReady) {
            CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bBox, MAP_PADDING);
            googleMap.animateCamera(update, ANIMATION_DURATION_IN_MS, null);
        }
    }

    @Override
    public void onZones(Map<String, List<LatLng>> zones) {
        this.zones = zones;
        int color = ContextCompat.getColor(this, R.color.colorAccent);
        polygonA = MapDrawUtils.drawPolygon(googleMap, zones.get("VBB_A"), color);
        polygonB = MapDrawUtils.drawPolygon(googleMap, zones.get("VBB_B"), color);
        polygonA.setVisible(false);
        polygonB.setVisible(false);
    }

    @Override
    public void onError() {
        Snackbar.make(coordinator, R.string.connection_error, LENGTH_LONG).show();
    }

    @Override
    public void onSelected(Ticket ticket) {
        Timber.e("blolb");
        String zone = ticket.zone;
        if (zone.equals("A")) {
            if (polygonB != null) polygonB.setVisible(false);
            if (polygonA != null) polygonA.setVisible(true);
        } else {
            if (polygonA != null) polygonA.setVisible(false);
            if (polygonB != null) polygonB.setVisible(true);
        }
    }
}
