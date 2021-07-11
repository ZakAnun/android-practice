package com.zakli.practicelayout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewStub
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * 布局加载完后（生成 View 对象），就开始进行绘制
 * Android 要求每帧的绘制时间不超过 16ms，不然会导致丢帧和应用卡顿
 * （原因：Android 显示的屏幕一般是每秒 60 帧刷新，所以 Android 系统每隔 16ms 就会发送 VSYNC 触发对 UI 进行渲染）
 *
 * 想要实现绘制优化需要从以下方面着手
 *  - 监控应用渲染速度
 *   - 手机设置 -> 开发者选项 -> 监控 -> GPU呈现模式分析 -> 在屏幕上显示为条形图，可以看到一些条形图
 *   - 水平线上每个竖条代表一帧，竖条的高度表示该帧渲染所花的时间
 *   - 绿色水平线表示 16ms，要实现每秒 60 帧，需要每帧的竖条都要保持在绿线下（超出此线可能会导致动画暂停）
 *  - 分析并着手进行优化
 *   - 优化点可以分为：测量、布局、绘制、动画、输入处理
 *   - 测量、布局、绘制，所以布局层级过多会占用额外 CPU 资源，所以应该减少 view 数层级，要宽而浅，避免窄而深
 *   - 屏幕上的某个像素在同一帧的时间里被绘制了多次（OverDraw），也会浪费大量 CPU 及 GPU 资源
 *   - 绘制过程中 onDraw() 应尽量避免布局对象的创建，因为该方法会被多次调用，大量局部变量的创建和回收会导致内存抖动
 *   - 合理使用动画
 *   - 避免在事件响应的回调中做耗时操作
 *  - 检测过渡绘制
 *   - 手机设置 -> 开发者选项 -> 硬件加速渲染 -> 调试 GPU 过渡绘制 -> 显示过渡绘制区域 -> 可以看到绘制的 View 都有颜色覆盖
 *   - 原色（无过渡绘制） -> 蓝色（1x 过渡绘制） -> 绿色（2x 过渡绘制） -> 粉色（3x 过渡绘制） -> 红色（4x 过渡绘制）
 *   - 虽然过渡绘制无法完全避免，但在界面优化时，应尽量让大部分的界面显示为原色（无过渡绘制）或者蓝色（仅有一次过渡绘制）
 *   - 如果出现粉色或红色，则需要查看代码能够尽量避免
 *  - 避免过渡绘制
 *   - 移除 window 的背景
 *    - 因为一般会在 view 中设置背景色，如果 windowBackground 和布局文件的 background 同时指定则会出现两次绘制
 *    - xml 设置 <item name="android:windowBackground">@null</item>
 *    - 代码设置 window.backgroundDrawable(null)
 *   - 移除控件中不需要的背景
 *    - 列表及其子控件背景相同，可以移除子控件布局的背景
 *    - 一个 ViewPager 对应多个 Fragment 组成的界面，如果每个 Fragment 都设置了背景色，那 ViewPager 则无须设置
 *   - 减少透明度的使用
 *    - 不透明的 view 只需要一次渲染即可，但如果设置 alpha，那至少需要渲染两次，因为使用了 alpha 的 view 需要先
 *    知道 view 的下一层元素是什么，然后结合上层 view 进行 Blend 混色处理。透明处理、淡入淡出、阴影等效果都涉及到
 *    透明度，就会造成过渡绘制
 *   - 使用 ConstraintLayout 减少布局层级
 *   - 使用 merge 标签减少布局层级
 *    - 在自定义 view 中如果继承自系统提供的 ViewGroup，那么布局文件根标签就可以使用 merge 承载
 *    - （还有个 include 标签可以提高布局的复用，但是不能减少层级）
 *   - 使用 ViewStub 标签延迟加载
 *    - 用于运行时按需懒加载资源，只有调用了 viewStub.inflate() 或者 viewStub.visibility = VISIBLE 内容才可见
 *    注意点
 *     - 当 ViewStub 被 inflate 到 parent 时，就会被 remove 掉，当前的 View 树中不再存在 ViewStub，由对应的布局视图代替
 *     - 用于不常用控件（如：网络请求失败提示、列表为空提示、新手引导等等
 *     - ViewStub 不支持 merge 标签（所以加载出来后会有多余的嵌套结构）
 *     - ViewStub#inflate 只能被调用一次，第二次调用就会抛出异常
 *     - ViewStub 也需要指定 layout_width 和 layout_height
 */
class LayoutDrawOptiActivity: AppCompatActivity() {

    companion object {
        private const val TAG = "LayoutDrawOptiActivity"

        fun startActivity(context: Context) {
            context.startActivity(
                Intent(context, LayoutDrawOptiActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.playout_do_activity)

        findViewById<TextView>(R.id.showViewStub).setOnClickListener {
            kotlin.runCatching {
                findViewById<ViewStub>(R.id.viewStub).inflate()
            }.onFailure {
                Toast.makeText(this, "ViewStub 只能被加载一次噢", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onCreate: view stub can only inflate once, ${it.message}")
            }
        }
    }
}