package com.shipthis.go

import android.util.Log
import java.lang.reflect.Method

object LogInterceptor {
    // Your custom log handler - implement this interface
    interface LogHandler {
        fun handleLog(level: String, tag: String, message: String)
    }
    
    private var logHandler: LogHandler? = null
    
    // Cache reflection methods to avoid repeated lookups
    private val logClass: Class<*> = Class.forName("android.util.Log")
    private val logV: Method = logClass.getMethod("v", String::class.java, String::class.java)
    private val logD: Method = logClass.getMethod("d", String::class.java, String::class.java)
    private val logI: Method = logClass.getMethod("i", String::class.java, String::class.java)
    private val logW: Method = logClass.getMethod("w", String::class.java, String::class.java)
    private val logE: Method = logClass.getMethod("e", String::class.java, String::class.java)
    private val logWtf: Method = logClass.getMethod("wtf", String::class.java, String::class.java)
    private val logEThrowable: Method = logClass.getMethod("e", String::class.java, String::class.java, Throwable::class.java)
    private val logWThrowable: Method = logClass.getMethod("w", String::class.java, String::class.java, Throwable::class.java)
    private val logIThrowable: Method = logClass.getMethod("i", String::class.java, String::class.java, Throwable::class.java)
    private val logDThrowable: Method = logClass.getMethod("d", String::class.java, String::class.java, Throwable::class.java)
    private val logVThrowable: Method = logClass.getMethod("v", String::class.java, String::class.java, Throwable::class.java)
    
    fun setLogHandler(handler: LogHandler) {
        logHandler = handler
    }
    
    @JvmStatic
    fun v(tag: String, msg: String): Int {
        logHandler?.handleLog("VERBOSE", tag, msg)
        return logV.invoke(null, tag, msg) as Int
    }
    
    @JvmStatic
    fun d(tag: String, msg: String): Int {
        logHandler?.handleLog("DEBUG", tag, msg)
        return logD.invoke(null, tag, msg) as Int
    }
    
    @JvmStatic
    fun i(tag: String, msg: String): Int {
        logHandler?.handleLog("INFO", tag, msg)
        return logI.invoke(null, tag, msg) as Int
    }
    
    @JvmStatic
    fun w(tag: String, msg: String): Int {
        logHandler?.handleLog("WARNING", tag, msg)
        return logW.invoke(null, tag, msg) as Int
    }
    
    @JvmStatic
    fun e(tag: String, msg: String): Int {
        logHandler?.handleLog("ERROR", tag, msg)
        return logE.invoke(null, tag, msg) as Int
    }
    
    @JvmStatic
    fun wtf(tag: String, msg: String): Int {
        logHandler?.handleLog("ERROR", tag, msg)
        return logWtf.invoke(null, tag, msg) as Int
    }
    
    // Add overloads if needed (with Throwable, etc.)
    @JvmStatic
    fun e(tag: String, msg: String, tr: Throwable): Int {
        logHandler?.handleLog("ERROR", tag, "$msg\n${tr.stackTraceToString()}")
        return logEThrowable.invoke(null, tag, msg, tr) as Int
    }
    
    @JvmStatic
    fun w(tag: String, msg: String, tr: Throwable): Int {
        logHandler?.handleLog("WARNING", tag, "$msg\n${tr.stackTraceToString()}")
        return logWThrowable.invoke(null, tag, msg, tr) as Int
    }
    
    @JvmStatic
    fun i(tag: String, msg: String, tr: Throwable): Int {
        logHandler?.handleLog("INFO", tag, "$msg\n${tr.stackTraceToString()}")
        return logIThrowable.invoke(null, tag, msg, tr) as Int
    }
    
    @JvmStatic
    fun d(tag: String, msg: String, tr: Throwable): Int {
        logHandler?.handleLog("DEBUG", tag, "$msg\n${tr.stackTraceToString()}")
        return logDThrowable.invoke(null, tag, msg, tr) as Int
    }
    
    @JvmStatic
    fun v(tag: String, msg: String, tr: Throwable): Int {
        logHandler?.handleLog("VERBOSE", tag, "$msg\n${tr.stackTraceToString()}")
        return logVThrowable.invoke(null, tag, msg, tr) as Int
    }
}

