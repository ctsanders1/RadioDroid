package net.programmierecke.radiodroid2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;

import net.programmierecke.radiodroid2.interfaces.IFragmentRefreshable;
import net.programmierecke.radiodroid2.interfaces.IFragmentSearchable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ActivityMain extends AppCompatActivity implements SearchView.OnQueryTextListener, IMPDClientStatusChange {
	private SearchView mSearchView;

	private static final String TAG = "RadioDroid";

	DrawerLayout mDrawerLayout;
	NavigationView mNavigationView;
	FragmentManager mFragmentManager;
	FragmentTransaction mFragmentTransaction;

	IFragmentRefreshable fragRefreshable = null;
	IFragmentSearchable fragSearchable = null;

	MenuItem menuItemSearch;
	MenuItem menuItemRefresh;

	private SharedPreferences sharedPref;
    private CastContext mCastContext;
    private MenuItem mediaRouteMenuItem;
    private SessionManager mSessionManager;

    private final SessionManagerListener mSessionManagerListener =
            new SessionManagerListenerImpl();
	private MenuItem menuItemMPDOK;
	private MenuItem menuItemMPDNok;

    @Override
    public void changed() {
        Handler mainHandler = new Handler(getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
            }
        };
        mainHandler.post(myRunnable);
    }

    private class SessionManagerListenerImpl implements SessionManagerListener {
        @Override
        public void onSessionStarting(Session session) {

        }

        @Override
        public void onSessionStarted(Session session, String sessionId) {
            invalidateOptionsMenu();
            Utils.mCastSession = mSessionManager.getCurrentCastSession();
        }

        @Override
        public void onSessionStartFailed(Session session, int i) {

        }

        @Override
        public void onSessionEnding(Session session) {
        }

        @Override
        public void onSessionResumed(Session session, boolean wasSuspended) {
            invalidateOptionsMenu();
            Utils.mCastSession = mSessionManager.getCurrentCastSession();
        }

        @Override
        public void onSessionResumeFailed(Session session, int i) {
            Utils.mCastSession = null;
        }

        @Override
        public void onSessionSuspended(Session session, int i) {
            Utils.mCastSession = null;
        }

        @Override
        public void onSessionEnded(Session session, int error) {
            Utils.mCastSession = null;
        }

        @Override
        public void onSessionResuming(Session session, String s) {

        }
    }


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);

        try {
			File dir = new File(getFilesDir().getAbsolutePath());
			if (dir.isDirectory()) {
				String[] children = dir.list();
				for (int i = 0; i < children.length; i++) {
					Log.e("MAIN", "delete file:" + children[i]);
					try {
						new File(dir, children[i]).delete();
					}
					catch (Exception e){}
				}
			}
		}
		catch (Exception e){}

		final Toolbar myToolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
		setSupportActionBar(myToolbar);

		PlayerServiceUtil.bind(this);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		mFragmentManager = getSupportFragmentManager();

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
		mNavigationView = (NavigationView) findViewById(R.id.my_navigation_view) ;

		mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(MenuItem menuItem) {
				mDrawerLayout.closeDrawers();
				android.support.v4.app.Fragment f = null;

				if (menuItem.getItemId() == R.id.nav_item_player_status) {
					Intent intent = new Intent(ActivityMain.this, ActivityPlayerInfo.class);
					startActivity(intent);
					return false;
				}

				if (menuItem.getItemId() == R.id.nav_item_stations) {
					f = new FragmentTabs();
					menuItemSearch.setVisible(true);
					myToolbar.setTitle(R.string.app_name);
				}

				if (menuItem.getItemId() == R.id.nav_item_starred) {
					f = new FragmentStarred();
					menuItemSearch.setVisible(false);
					myToolbar.setTitle(R.string.nav_item_starred);
				}

				if (menuItem.getItemId() == R.id.nav_item_history) {
					f = new FragmentHistory();
					menuItemSearch.setVisible(false);
					myToolbar.setTitle(R.string.nav_item_history);
				}

				if (menuItem.getItemId() == R.id.nav_item_serverinfo) {
					f = new FragmentServerInfo();
					menuItemSearch.setVisible(false);
					myToolbar.setTitle(R.string.nav_item_statistics);
				}

				if (menuItem.getItemId() == R.id.nav_item_recordings) {
					f = new FragmentRecordings();
					menuItemSearch.setVisible(false);
					myToolbar.setTitle(R.string.nav_item_recordings);
				}

				if (menuItem.getItemId() == R.id.nav_item_alarm) {
					f = new FragmentAlarm();
					menuItemSearch.setVisible(false);
					myToolbar.setTitle(R.string.nav_item_alarm);
				}

				if (menuItem.getItemId() == R.id.nav_item_settings) {
					f = new FragmentSettings();
					menuItemSearch.setVisible(false);
					myToolbar.setTitle(R.string.nav_item_settings);
				}

				if (menuItem.getItemId() == R.id.nav_item_about) {
					f = new FragmentAbout();
					menuItemSearch.setVisible(false);
					myToolbar.setTitle(R.string.nav_item_about);
				}

				FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
				xfragmentTransaction.replace(R.id.containerView,f).commit();
				fragRefreshable = null;
				fragSearchable = null;
				if (f instanceof IFragmentRefreshable) {
					fragRefreshable = (IFragmentRefreshable) f;
				}
				if (f instanceof IFragmentSearchable) {
					fragSearchable = (IFragmentSearchable) f;
				}
				menuItemRefresh.setVisible(fragRefreshable != null);

				return false;
			}
		});

		//myToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.main_toolbar);
		ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, R.string.app_name,R.string.app_name);
		mDrawerLayout.addDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();

        mCastContext = CastContext.getSharedInstance(this);
        mSessionManager = mCastContext.getSessionManager();

        MPDClient.StartDiscovery(this, this);
    }

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		Log.w(TAG,"on request permissions result:"+requestCode);
		switch (requestCode) {
			case Utils.REQUEST_EXTERNAL_STORAGE: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					if (fragRefreshable != null){
						Log.w(TAG,"REFRESH VIEW");
						fragRefreshable.Refresh();
					}
				} else {
					Toast toast = Toast.makeText(this, getResources().getString(R.string.error_record_needs_write), Toast.LENGTH_SHORT);
					toast.show();
				}
				return;
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		PlayerServiceUtil.unBind(this);
        MPDClient.StopDiscovery();
	}

	@Override
	protected void onPause() {
        Log.i(TAG,"PAUSED");
		super.onPause();
        mSessionManager.removeSessionManagerListener(mSessionManagerListener);
        Utils.mCastSession = null;
        MPDClient.StopDiscovery();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);

		menuItemSearch = menu.findItem(R.id.action_search);
		mSearchView = (SearchView) MenuItemCompat.getActionView(menuItemSearch);
		mSearchView.setOnQueryTextListener(this);

		menuItemRefresh = menu.findItem(R.id.action_refresh);
		menuItemMPDNok = menu.findItem(R.id.action_mpd_nok);
		menuItemMPDOK = menu.findItem(R.id.action_mpd_ok);

		if (fragSearchable == null) {
			menuItemSearch.setVisible(false);
		}

		if (fragRefreshable == null) {
			menuItemRefresh.setVisible(false);
		}

		menuItemMPDOK.setVisible(MPDClient.Discovered() && MPDClient.Connected());
		menuItemMPDNok.setVisible(MPDClient.Discovered() && !MPDClient.Connected());

        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                menu,
                R.id.media_route_menu_item);

        return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				mDrawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
				return true;
			case R.id.action_refresh:
				if (fragRefreshable != null){
					fragRefreshable.Refresh();
				}
				return true;
            case R.id.action_mpd_nok:
                MPDClient.Connect(this);
                return true;
            case R.id.action_mpd_ok:
                MPDClient.Disconnect(this, this);
                return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (sharedPref == null) {
			sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		}

		Fragment first = null;
		if (sharedPref.getBoolean("starred_at_startup", false)) {
			FragmentStarred fragStarred = new FragmentStarred();
			getSupportActionBar().setTitle(R.string.nav_item_starred);
			fragSearchable = null;
			fragRefreshable = null;
			first = fragStarred;
		} else {
			FragmentTabs fragTabs = new FragmentTabs();
			getSupportActionBar().setTitle(R.string.nav_item_stations);
			fragRefreshable = fragTabs;
			fragSearchable = fragTabs;
			first = fragTabs;
		}

		mFragmentTransaction = mFragmentManager.beginTransaction();
		mFragmentTransaction.replace(R.id.containerView,first).commit();

        Utils.mCastSession = mSessionManager.getCurrentCastSession();
        mSessionManager.addSessionManagerListener(mSessionManagerListener);

        Log.i(TAG,"RESUMED");
        MPDClient.StartDiscovery(this, this);
    }

	public void Search(String query){
		if (fragSearchable != null) {
			fragSearchable.Search(query);
		}
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		String queryEncoded = null;
		try {
			mSearchView.setQuery("", false);
			mSearchView.clearFocus();
			mSearchView.setIconified(true);
			queryEncoded = URLEncoder.encode(query, "utf-8");
			queryEncoded = queryEncoded.replace("+","%20");
			Search("https://www.radio-browser.info/webservice/json/stations/byname/"+queryEncoded);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}
}
