package com.bihe0832.android.framework.router

import android.content.Intent
import com.bihe0832.android.framework.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.request.URLUtils
import java.util.*


/**
 * Created by zixie on 2017/6/27.
 *
 */

fun openZixieWeb(url: String) {
    val map = HashMap<String, String>()
    map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = URLUtils.encode(url)
    RouterAction.openFinalURL(
        RouterAction.getFinalURL(RouterConstants.MODULE_NAME_WEB_PAGE, map),
        Intent.FLAG_ACTIVITY_NEW_TASK
    )
}

fun openFeedback(){
    val map = HashMap<String, String>()
    map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = URLUtils.encode(ZixieContext.applicationContext!!.resources.getString(R.string.feedback_url))
    RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_FEEDBACK, map)
}