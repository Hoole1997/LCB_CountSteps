package com.example.lcb.app.ui.components

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import com.android.common.bill.ui.NativeAdStyleType
import com.example.lcb.app.utils.loadNative

@Composable
fun NativeAdSlot(
    modifier: Modifier = Modifier,
    styleType: NativeAdStyleType = NativeAdStyleType.STANDARD,
    active: Boolean = true,
) {
    val activity = LocalView.current.context.findFragmentActivity()

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { context -> FrameLayout(context) },
        update = { container ->
            container.visibility = if (active) View.VISIBLE else View.INVISIBLE
            if (!active || activity == null || container.tag == styleType) return@AndroidView
            container.tag = styleType
            activity.loadNative(
                container = container,
                styleType = styleType,
            )
        },
        onRelease = { container ->
            container.removeAllViews()
            container.tag = null
        },
    )
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? {
    return when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.findFragmentActivity()
        else -> null
    }
}
