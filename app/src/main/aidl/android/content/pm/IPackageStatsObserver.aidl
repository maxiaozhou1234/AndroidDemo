// IPackageStatsObserver.aidl
package android.content.pm;

// Declare any non-default types here with import statements
import android.content.pm.PackageStats;

interface IPackageStatsObserver {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onGetStatsCompleted(in PackageStats pStats, boolean succeeded);
}
