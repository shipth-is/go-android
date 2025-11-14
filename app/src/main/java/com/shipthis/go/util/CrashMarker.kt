package com.shipthis.go.util

import android.content.Context

object CrashMarker {
    private const val PREF = "run_state"
    private const val KEY_CLEAN = "clean_exit"
    private const val KEY_BUILD = "last_build_id"

    /**
     * Call at start of runtime session.
     * buildId = the ID of the build being launched.
     */
    @JvmStatic
    fun markStart(context: Context, buildId: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_CLEAN, false)     // assume crash until marked clean
            .putString(KEY_BUILD, buildId)    // remember the build running
            .apply()
    }

    /**
     * Call when Godot shuts down normally
     * (right before your SIGKILL that finishes the process).
     */
    @JvmStatic
    fun markCleanExit(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_CLEAN, true)
            .apply()
    }

    /** true if last run did NOT exit cleanly */
    @JvmStatic
    fun wasCrashedLastTime(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val clean = prefs.getBoolean(KEY_CLEAN, true)
        return !clean
    }

    /** buildId of the run that crashed (may be null on first launch) */
    @JvmStatic
    fun getCrashedBuildId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BUILD, null)
    }
}

