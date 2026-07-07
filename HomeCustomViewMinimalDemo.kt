package org.oksp.launcher.activities

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import org.oksp.launcher.App
import org.oksp.launcher.utils.logD
import org.oksp.launcher.utils.logW
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 桌面自定义 View 回调式接口「最小接入示例」。
 *
 * 仅依赖 [App] 对外暴露的 6 个接口，演示一条完整的 happy-path：
 *   就绪监听 → 预检查 → 注册 provider → 渲染回调 → isRendered 校验 → 主动刷新。
 *
 * 单一真源（single source of truth）：
 * - 所有要展示的动态 View 统一登记在内部的 [views] 表中，由**唯一的一个** provider 遍历提交；
 * - 通过 [addExtraView] / [removeView] 增删条目，它们与主示例 View 共用这同一个 provider，可同时存在；
 * - 因为 [App] 的 provider 是单槽位，绝不在外部再注册第二个 provider，避免相互覆盖。
 *
 * 最佳实践要点：
 * - 状态为进程级（object 单例）：注册给 [App] 的回调与 Application 同生命周期，
 *   只引用本单例状态与 application context，不持有 Activity，避免内存泄漏；
 * - View 用 application context 创建，并在多轮刷新中复用同一实例，避免闪烁与状态丢失；
 * - View 的点击等交互完全由调用方处理（此处用 Toast 演示）。
 *
 * 用法：
 * ```
 * HomeCustomViewMinimalDemo.start(context)                       // 启动接入（加入主示例 View）
 * HomeCustomViewMinimalDemo.addExtraView(context, "k", 0,1,2,2,1,"t") // 追加额外 View（共存）
 * HomeCustomViewMinimalDemo.removeView(context, "k")             // 移除某个 View
 * HomeCustomViewMinimalDemo.clear(context)                       // 清除全部接入
 * ```
 */
object HomeCustomViewMinimalDemo {

    /** 主示例 View 的业务唯一标识：用于派生稳定 runtime id、日志定位与防重复；同一 key 多轮刷新复用同一 View 实例。 */
    private const val KEY = "minimal_demo_view"

    /**
     * 主示例 View 所在的桌面页索引（从 0 开始）。
     * 0 表示桌面第一屏；多屏桌面可改为 1、2…… 指向对应分页。取值需 >= 0，否则预检查按越界处理。
     */
    private const val PAGE = 0

    /**
     * 主示例 View 占位区域左上角的「列」坐标（X 方向，从 0 开始，单位为格子数）。
     * 1 表示从左往右数第 2 列开始。取值需在 [0, 列数-1] 范围内。
     */
    private const val LEFT = 1

    /**
     * 主示例 View 占位区域左上角的「行」坐标（Y 方向，从 0 开始，单位为格子数）。
     * 1 表示从上往下数第 2 行开始。取值需在 [0, 可用行数-1] 范围内（可用行数 = 总行数 - Dock 行数）。
     */
    private const val TOP = 1

    /**
     * 主示例 View 占位区域横向宽度（X 方向跨越的格子数）。
     * 2 表示占据 2 列；实际覆盖列范围为 [LEFT, LEFT + SPAN_X - 1]。取值需 > 0，否则预检查返回「宽高无效」。
     */
    private const val SPAN_X = 2

    /**
     * 主示例 View 占位区域纵向高度（Y 方向跨越的格子数）。
     * 1 表示占据 1 行；实际覆盖行范围为 [TOP, TOP + SPAN_Y - 1]。取值需 > 0，否则预检查返回「宽高无效」。
     */
    private const val SPAN_Y = 1

    private val mainHandler = Handler(Looper.getMainLooper())

    /** 单一真源：所有要展示的动态 View（key -> 配置）；唯一的 provider 即遍历此表提交。 */
    private val views = LinkedHashMap<String, ViewSpec>()

    /** 各 key 的秒表任务（如实时时钟），便于单独取消。 */
    private val clocks = HashMap<String, Runnable>()

    /** provider 与监听是否已注册，避免重复注册。 */
    @Volatile
    private var callbacksRegistered = false

    /** 单个动态 View 的配置（内部数据，不对外暴露）。 */
    private data class ViewSpec(
        val key: String,
        val view: TextView,
        val page: Int,
        val left: Int,
        val top: Int,
        val spanX: Int,
        val spanY: Int,
        val title: String,
    )

    /**
     * 启动最小接入流程：注册回调（幂等）+ 加入主示例 View（带实时时钟）+ 刷新。
     * 可传入任意 [Context]，内部统一取 applicationContext。
     */
    fun start(context: Context) {
        val app = context.applicationContext as? App ?: run {
            logW { "[最小Demo] applicationContext 不是 App 类型，接入失败" }
            return
        }
        val appCtx = app.applicationContext
        ensureCallbacks(app)

        if (!views.containsKey(KEY)) {
            val view = buildView(appCtx, "Minimal Demo", Color.rgb(25, 118, 210))
            views[KEY] = ViewSpec(KEY, view, PAGE, LEFT, TOP, SPAN_X, SPAN_Y, "Minimal Demo")
            startClock(KEY, view, "Minimal Demo")
        }
        app.refreshHomeScreen()
        Toast.makeText(appCtx, "最小接入已启动：打开桌面后自动渲染自定义 View", Toast.LENGTH_SHORT).show()
    }

    /**
     * 追加一个额外的动态 View，与主示例 View **共用同一个 provider**，因此可同时存在。
     *
     * 会先做放置预检查（[App.canPlaceHomeCustomView]）；不可放置则提示并跳过。
     *
     * @param key 业务唯一标识；同 key 已存在时不重复添加。
     * @param page/left/top/spanX/spanY 目标格子坐标与跨度，含义同主示例常量。
     * @param title 标题（日志/标识用）。
     */
    fun addExtraView(
        context: Context,
        key: String,
        page: Int,
        left: Int,
        top: Int,
        spanX: Int,
        spanY: Int,
        title: String,
    ) {
        val app = context.applicationContext as? App ?: run {
            logW { "[最小Demo] applicationContext 不是 App 类型，追加失败" }
            return
        }
        val appCtx = app.applicationContext
        ensureCallbacks(app)

        if (views.containsKey(key)) {
            Toast.makeText(appCtx, "已存在同 key 的 View：$key", Toast.LENGTH_SHORT).show()
            return
        }

        // App.canPlaceHomeCustomView(page, left, top, spanX, spanY, onReason: ((reason: String) -> Unit)? = null): Boolean
        //   作用：预检查桌面指定区域是否可放置自定义 View，复用与刷新合并一致的校验逻辑。必须在主线程调用。
        //   参数：
        //     - page  : Int 目标桌面页索引（从 0 开始）。
        //     - left  : Int 左上角列坐标（X，从 0 开始，单位格子）。
        //     - top   : Int 左上角行坐标（Y，从 0 开始，单位格子）。
        //     - spanX : Int 横向占据列数（> 0）。
        //     - spanY : Int 纵向占据行数（> 0）。
        //     - onReason: 可选回调，仅在「不可放置」时回调失败原因文案（如「宽高无效」「坐标越界」「目标位置已被占用」）；
        //                 可放置或桌面不存在时不回调。
        //   返回：true=可放置（桌面不存在时默认 true）；false=不可放置，原因经 onReason 给出。
        var reason: String? = null
        val canPlace = app.canPlaceHomeCustomView(page, left, top, spanX, spanY) { reason = it }
        if (!canPlace) {
            logW { "[最小Demo] 追加 $key 预检查不可放置：$reason" }
            Toast.makeText(appCtx, "追加 $key 预检查不可放置：$reason", Toast.LENGTH_SHORT).show()
            return
        }

        val view = buildView(appCtx, title, Color.rgb(67, 160, 71))
        views[key] = ViewSpec(key, view, page, left, top, spanX, spanY, title)
        app.refreshHomeScreen()
        Toast.makeText(appCtx, "已追加 View：$key 并刷新桌面", Toast.LENGTH_SHORT).show()
    }

    /** 移除指定 key 的 View：从单一真源中删除并刷新，provider 不再返回该项，桌面自动回收。 */
    fun removeView(context: Context, key: String) {
        val app = context.applicationContext as? App ?: return
        stopClock(key)
        val removed = views.remove(key) != null
        app.refreshHomeScreen()
        val msg = if (removed) "已移除 $key（刷新后回收）" else "未找到 $key"
        Toast.makeText(app.applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    /** 当前已登记的全部 key（快照），便于调用方查询/遍历。 */
    fun registeredKeys(): List<String> = views.keys.toList()

    /** 查询某 key 当前是否已真实渲染到桌面（需桌面存在；桌面不存在时返回 false）。 */
    fun isRendered(context: Context, key: String): Boolean {
        val app = context.applicationContext as? App ?: return false
        // App.isHomeCustomViewRendered(key: String): Boolean
        //   作用：查询指定 key 的自定义 View 当前是否已添加并渲染到桌面。
        //   参数 key：调用方在 provider 中提交的业务唯一标识。
        //   返回：true=已渲染（已确认渲染且未被回收，结果不受桌面是否在前台影响）；
        //         false=未渲染，或桌面（MainActivity）不存在。
        return app.isHomeCustomViewRendered(key)
    }

    /**
     * 清除接入：清空列表、停止全部秒表、provider 与全部监听置 null，并刷新桌面回收动态 View。
     */
    fun clear(context: Context) {
        val app = context.applicationContext as? App ?: return
        clocks.keys.toList().forEach { stopClock(it) }
        views.clear()
        // 三个 set* 接口传入 null 即清除对应的 provider/监听（参数语义见 ensureCallbacks 中的详细说明）。
        app.setHomeCustomViewProvider(null)
        app.setHomeGridReadyListener(null)
        app.setHomeCustomViewRenderedListener(null)
        callbacksRegistered = false
        // App.refreshHomeScreen()：主线程主动刷新桌面，使被置空的 provider 生效、回收动态 View。
        app.refreshHomeScreen()
        Toast.makeText(app.applicationContext, "已清除最小接入 Demo", Toast.LENGTH_SHORT).show()
    }

    /**
     * 注册 provider 与监听（幂等，唯一一次）。
     * 这些回调由 [App] 长期持有，因此 lambda 只引用本单例状态与 application context，不持有 Activity。
     */
    private fun ensureCallbacks(app: App) {
        if (callbacksRegistered) return
        callbacksRegistered = true
        val appCtx = app.applicationContext

        // App.setHomeCustomViewRenderedListener(listener: ((key: String) -> Unit)?)
        //   作用：注册/清除「自定义 View 渲染完成」监听。当某自定义 View 真正附着到视图树、完成测量布局
        //         且实际可见绘制后，在主线程回调。
        //   参数 listener：
        //     - 函数类型 (key: String) -> Unit；key 即调用方在 provider 中提交的业务唯一标识。
        //     - 同一 key 在一次渲染周期内只回调一次；被回收后再次渲染会再次回调。
        //     - 传 null 表示清除监听。
        app.setHomeCustomViewRenderedListener { key ->
            val rendered = app.isHomeCustomViewRendered(key)
            logD { "[最小Demo] onRendered key=$key isRendered=$rendered" }
            Toast.makeText(appCtx, "已渲染到桌面：$key", Toast.LENGTH_SHORT).show()
        }

        // App.setHomeCustomViewProvider(provider: ((emit: (key, view, page, left, top, spanX, spanY, title) -> Unit) -> Unit)?)
        //   作用：注册/清除「动态自定义 View provider」。每次桌面刷新时在主线程被回调，由调用方通过 emit 逐个
        //         提交当前需展示的动态 View；动态项不写入 Room，刷新时按本次提交结果重建。
        //   参数 provider：外层函数会被传入一个 emit 函数，调用方对每个要展示的 View 调用一次 emit。
        //   emit 的 8 个参数（均由调用方提供）：
        //     - key   : String 业务唯一标识，用于派生稳定 runtime id、日志定位与防重复；同一 key 多轮刷新应复用同一 View 实例。
        //     - view  : View   要展示的 Android 视图（点击等交互由调用方处理；建议用 application context 创建避免泄漏）。
        //     - page  : Int    目标桌面页索引（从 0 开始）。
        //     - left  : Int    左上角列坐标（X，从 0 开始，单位格子）。
        //     - top   : Int    左上角行坐标（Y，从 0 开始，单位格子）。
        //     - spanX : Int    横向占据列数（> 0），覆盖列范围 [left, left+spanX-1]。
        //     - spanY : Int    纵向占据行数（> 0），覆盖行范围 [top, top+spanY-1]。
        //     - title : String 标题（标识/日志用）。
        //   传 null 表示清除 provider；下一轮刷新后桌面动态 View 被回收。
        app.setHomeCustomViewProvider { emit ->
            logD { "[最小Demo] provider 回调，提交 ${views.size} 项" }
            views.values.forEach { spec ->
                emit(spec.key, spec.view, spec.page, spec.left, spec.top, spec.spanX, spec.spanY, spec.title)
            }
        }

        // App.setHomeGridReadyListener(listener: (() -> Unit)?)
        //   作用：注册/清除「桌面就绪」监听。当桌面首帧绘制完成且数据已加载时在主线程回调，告知调用方此刻可安全
        //         注册/预检查自定义 View（此时 canPlaceHomeCustomView 的结果才准确）。
        //   参数 listener：
        //     - 无参函数 () -> Unit；桌面（MainActivity）重建后新桌面再次就绪会再次回调（幂等重注册即可）。
        //     - 传 null 表示清除监听。
        app.setHomeGridReadyListener {
            logD { "[最小Demo] 桌面就绪，触发刷新（当前 ${views.size} 项）" }
            // App.refreshHomeScreen()：在主线程主动触发一次桌面刷新；无参数；桌面（MainActivity）不存在时无操作。
            app.refreshHomeScreen()
        }
    }

    private fun buildView(appCtx: Context, label: String, color: Int): TextView {
        return TextView(appCtx).apply {
            text = label
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f * appCtx.resources.displayMetrics.density
                setColor(color)
            }
            setOnClickListener {
                Toast.makeText(appCtx, "自定义 View [$label] 点击事件由调用方处理", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startClock(key: String, view: TextView, label: String) {
        stopClock(key)
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val runnable = object : Runnable {
            override fun run() {
                view.text = "$label\n${sdf.format(System.currentTimeMillis())}"
                mainHandler.postDelayed(this, 1000)
            }
        }
        clocks[key] = runnable
        mainHandler.post(runnable)
    }

    private fun stopClock(key: String) {
        clocks.remove(key)?.let { mainHandler.removeCallbacks(it) }
    }
}
