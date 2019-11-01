package com.gyf.cactus

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.gyf.cactus.pix.OnePixActivity
import java.lang.ref.WeakReference

/**
 *
 * 处理前后台切换
 *
 * @author geyifeng
 * @date 2019-11-01 10:36
 */
class AppBackgroundCallbacks @JvmOverloads constructor(
    private var context: Context? = null,
    private var block: ((Boolean) -> Unit)? = null
) :
    Application.ActivityLifecycleCallbacks {

    private val mHandler by lazy {
        Handler()
    }

    private var mContext: WeakReference<Context>? = null

    private var mFrontActivityCount = 0
    private var mIsSend = false
    private var mIsFirst = true

    companion object {
        private const val FIRST_TIME = 1000L
    }

    init {
        mHandler.postDelayed({
            if (mFrontActivityCount == 0) {
                post()
            }
        }, FIRST_TIME)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity !is OnePixActivity) {
            mContext = WeakReference(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity !is OnePixActivity) {
            mFrontActivityCount++
            post()
        }
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity !is OnePixActivity) {
            mFrontActivityCount--
            post()
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    private fun post() {
        (mContext?.get() ?: context)?.apply {
            if (mFrontActivityCount == 0) {
                mIsSend = false
                mHandler.postDelayed {
                    sendBroadcast(Intent().setAction(Cactus.CACTUS_BACKGROUND))
                    block?.let { it(true) }
                }
            } else {
                if (!mIsSend) {
                    mIsSend = true
                    mHandler.postDelayed {
                        sendBroadcast(Intent().setAction(Cactus.CACTUS_FOREGROUND))
                        block?.let { it(false) }
                    }
                }
            }
        }
    }

    private inline fun Handler.postDelayed(crossinline block: () -> Unit) {
        if (mIsFirst) {
            postDelayed(
                {
                    block()
                    mIsFirst = false
                },
                FIRST_TIME
            )
        } else {
            block()
        }
    }
}