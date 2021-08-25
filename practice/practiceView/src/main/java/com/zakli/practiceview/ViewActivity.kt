package com.zakli.practiceview

import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zakli.practiceview.view.PViewCircleView
import com.zakli.practiceview.view.PViewHorizontalLayout

/**
 * 自定义 View 分类
 *  - 继承 View 重写 onDraw()
 *   - 主要用于实现一些不规则效果（即通过正常布局组合的方式比较难达到预期效果），
 *   需要自己支持 wrap_content 和 padding 也需要自己处理
 *  - 继承 ViewGroup 派生自定义的 Layout
 *   - 主要用于实现自定义布局，需要重写 onMeasure()、onLayout()，并同时处理子元素的测量和布局过程
 *  - 继承 Android 自有 View（如 TextView）
 *   - 一般用于拓展已有 View 的功能，这种方式不用自己支持 wrap_content 和 padding
 *  - 继承 Android 自有 ViewGroup（如 LinearLayout）
 *   - 一般用于拓展已有 ViewGroup 的功能，这种方式不用自己处理 ViewGroup 的测量和布局这两个过程
 *
 * 自定义 View 注意事项
 *  - 让 View 支持 wrap_content
 *   - 直接继承 View 和 ViewGroup 的控件，如果不在 onMeasure() 对 wrap_content 做处理
 *   那么当外部调用在布局中使用 wrap_content 时就无法到达预期的效果（都是 match_parent）
 *  - 如果有必要，让 View 支持 padding
 *   - 直接继承 View 的控件，如果不在 onDraw() 中处理 padding，那么 padding 属性是无法去作用的，
 *   另外，直接继承 ViewGroup 的控件需要在 onMeasure() 和 onLayout() 中考虑 padding 和
 *   子 View 的 margin 对其造成的影响，不然会导致 padding 和子 View 的 margin 失效
 *  - 尽量不要在 View 中使用 Handler
 *   - 因为在 View 内部本身就提供了 post 系列方法，完全可以替代 Handler 的使用
 *  - View 中如果有线程或动画，需要及时停止
 *  - onMeasure（PViewCircleView demo）
 *   - MeasureSpec
 *    - getMode()
 *     - 获取测量模式
 *      - UNSPECIFIED：不限制子 view 大小（一般不用处理）
 *      - EXACTLY：当 view 宽高设置为 MATCH_PARENT 或固定大小时的测量模式
 *      - AT_MOST：当 view 宽高设置为 WRAP_CONTENT 时的测量模式，如果不处理就会按 EXACTLY 模式测量
 *     - 父控件解析所有子 View 的 LayoutParams，然后把这些参数经过处理包装到 withMeasureSpec、heightMeasureSpec
 *     里传递给子 view（自定义 View#onMeasure 的入参来源）
 *    - getSize()
 *     - 获取控件提供的 view 的最大宽高
 *  - margin 处理
 *   - margin 会写到 LayoutParams 里，逻辑上都是交给父控件 ViewGroup#onLayout() 中处理
 *  - padding 处理
 *   - padding 需要开发者自行处理，能在 view 中获取左、上、右、下四个方向的内边距
 *  - onSizeChange
 *   - 在 view 大小改变时调用，入参分别为宽度、高度、上一次宽度、上一次高度
 *   - 这个回调可能会多次触发，如 setTop、setLeft、addView、removeView 等方法调用时
 *  - 父控件 ViewGroup 对子 View 测量的影响
 *   - ViewGroup 相当于一个放置 View 的容器，他的职能是给 childView 计算出建议的宽高和测量模式，
 *   决定 childView 的位置（建议宽高是因为 childView 自身也有宽高，需要由自己计算）
 *   view 根据测量模式和 ViewGroup 给出的建议宽高，算出自身宽高，还需要在 ViewGroup 指定的
 *   区域内绘制出自己的形态
 *   - ViewGroup 测量过程
 *    - measureChildren(): 遍历所有的 childView
 *    - getChildMeasureSpec(): 确定测量规格
 *    - measureChild(): 调用测量规格
 *
 * 滑动冲突
 *  - 常见的滑动冲突场景
 *   - 外部滑动方向与内部滑动方向不一致
 *    - 常见于 ViewPager 和 Fragment 配合使用的效果，可以左右滑动切换页面，每个页面可以上下滑动（但 ViewPager 内部处理了这种滑动冲突）
 *   - 外部滑动方向与内部滑动方向一致
 *    - 不处理的话系统无法知道用户想要哪一层滑动
 *   - 以上两种情况嵌套
 *  - 处理规则
 *   - 根据滑动是水平滑动还是竖直滑动来判断由谁来拦截（竖直方向滑动距离大就判断为竖直滑动否则判断为水平滑动）
 *   - 如果无法根据滑动的距离差、角度以及速度来判断，则需要在业务上区分（如某种状态需要外部 View 响应滑动，另一种状态需要内部 View 响应滑动）
 *   - 同样需要根据业务定义来区分
 *  - 处理方式
 *   - 外部拦截法（父容器#onInterceptTouchEvent()）
 *    - 指点击事件都先经过父容器的拦截处理，如果父容器需要处理这个事件就拦截，否则就不拦截（符合事件分发机制）
 *    这种方式需要重写父容器的 onInterceptTouchEvent()，在内部做相应的拦截即可
 *    - ACTION_DOWN 父容器必须返回 false（不拦截，因为一旦拦截，那后续的 ACTION_UP 和 ACTION_MOVE 都会直接由父容器处理，无法传递到子元素中）
 *    - ACTION_MOVE 可以根据需要来决定是否拦截，在父容器中 return true 表示拦截，return false 表示不拦截
 *    - ACTION_UP 必须返回 false
 *   - 内部拦截法（子 View#dispatchTouchEvent()）
 *    - 指父容器不拦截任何事件，所有的事件都传递给子 View，如果子 View 需要处理事件就直接消耗掉，否则就交由父容器进行处理
 *    - 这种方式和 Android 中的事件分发机制不一致，需要配合 parent#requestDisallowInterceptTouchEvent()
 *    - 因为子 View 的事件是由父容器传递过来的，在子 View 的 ACTION_DOWN 事件里调用 requestDisallowInterceptTouchEvent()，不会
 *    因为父容器在 ACTION_DOWN 重置而失效，因为 ACTION_DOWN 事件不受 FLAG_DISALLOW_INTERCEPT 这个标志位的控制，所以一旦父容器拦截
 *    了 ACTION_DOWN 事件，那么所有事件都无法传递到子元素中去，这样内部拦截就无法起作用了
 *    所以父 View#onInterceptHoverEvent() 可以修改为 `return event.getAction() != MotionEvent.ACTION_DOWN;`
 *
 * View#post() 解析
 *  - 判断 mAttachInfo 是否为 null（AttachInfo 是 View 的静态内部类（内部持有 Handler 引用），每个 View 都会持有一个 AttachInfo 引用
 *  - 不为 null，直接调用其内部 Handler#post
 *  - 为 null，先缓存 Runnable 任务
 *   - getRunQueue().post()
 *    - 返回 HandlerActionQueue，即调用了 HandlerActionQueue#post
 *     - 将 Runnable 包装成 HandlerAction（仍然缓存起来）
 *     - HandlerAction 表示一个待执行的任务，内部持有要执行的 Runnable 和延时时间
 *     - postDelayed 创建了一个默认长度为 4 的 HandlerAction 用于保存 post 添加的任务（但此时任务并未被执行）
 *   - AttachInfo 持有当前线程的 Handler 和 Window 的 ViewRootImpl 对象
 *    - AttachInfo 在 ViewRootImpl 的构造方法中创建（每个 view 都会共用这个 AttachInfo 对象）
 *    - View#dispatchAttachedToWindow 中，会给每个 View 的 mAttachInfo 赋值并执行之前缓存的任务，通过主线程的 Handler 发送并处理任务
 *     - 给当前 View 赋值 AttachInfo（此时所有的 View 共用一个 AttachInfo（同一个 ViewRootImpl））
 *     - 判断 mRunQueue（HandlerActionQueue） 是否为空，不为空则执行 executeActions（post 到主线程的 Handler 中）
 *      - 拿到任务队列 mActions
 *      - 遍历所有的任务并通过 handler#postDelayed
 *      - 至空 mActions，后续的 post 任务将被加载 AttachInfo 中，直接用 Handler 发送消息
 *     - 回调 View#onAttachedToWindow()
 *     - 调用时机
 *      - ViewRootImpl#dispatchAttachedToWindow 在 performTraversals() 中调用，这个方法会依次完成 View 绘制的三大流程：测量、布局、绘制
 *      - 每个 Activity 都关联一个 Window 对象，用来描述应用程序窗口，每个窗口内部包含一个 DecorView 对象
 *       - 先调用 decorView#dispatchAttachedToWindow()，并且把 mAttach 传递给子 View
 *        - 这里是会调用到 ViewGroup#dispatchAttachedToWindow，会遍历所有子 View，然后调用子 View#dispatchAttachedToWindow，并给每个 View 传递 AttachInfo
 *       - View 绘制流程测量阶段 performMeasure()
 *       - View 绘制流程布局阶段 performLayout()
 *       - View 绘制流程绘制阶段 performDraw()
 *  - 通过 View#post() 添加的任务，是在 View 的绘制流程的开始阶段。post 任务被放到消息队列的末尾，此时 post 的任务已经在绘制之后
 *  即 View 的绘制流程结束后，再去获取宽高就可以正确获取到 View 的宽高
 *
 * View 的事件分发机制（即对 MotionEvent 事件分发的过程）
 *  - dispatchTouchEvent
 *   - 用于进行事件分发，如果事件能传递给当前 view，那这个方法一定会被调用，返回结果受当前 View#onTouchEvent 和
 *   下级 View#dispatchTouchEvent 影响，表示是否消耗当前事件
 *  - onInterceptTouchEvent
 *   - 在 dispatchTouchEvent 内部调用，用来判断是否拦截某个事件，如果当前 View 拦截了某个事件，那么同一事件序列中（
 *   DOWN、MOVE 和 UP 等）此方法不再被调用。返回结果表示是否拦截当前事件
 *  - onTouchEvent
 *   - 在 dispatchTouchEvent 中调用，用来处理点击事件，返回结果表示是否消耗当前事件，
 *   如果不消耗，则在同一个事件序列中（DOWN、MOVE、UP 等），当前 View 无法再次接收到事件
 *  - 伪代码
 *  fun dispatchTouchEvent(ev: MotionEvent): Boolean {
 *    var consume = false
 *    if (onInterceptTouchEvent(ev)) {
 *      consume = onTouchEvent(ev)
 *    } else {
 *      consume = child.dispatchTouchEvent(ev)
 *    }
 *    return consume
 *  }
 *  - 对于根 ViewGroup 而言
 *   - 点击事件产生后，首先会传递给它，此时 ViewGroup#dispatchTouchEvent 会被调用，
 *   如果这个 ViewGroup#onInterceptTouchEvent 为 true 表示需要拦截当前事件，
 *   那么事件就会交给这个 ViewGroup 处理，即它的 onTouchEvent 会被调用
 *   如果这个 ViewGroup#onInterceptTouchEvent 为 false 表示步拦截当前事件，此时当前事件
 *   会被继续传递给它的子 View，接着子 View#dispatchTouchEvent 会被调用
 *   反复直到该事件最终被处理
 *  - 对于 View 需要处理事件时
 *   - 如果它设置了 onTouchListener，那么 OnTouchListener 的 onTouch 就会被回调，
 *   如果 onTouch 返回 false，那么当前的 onTouchEvent 会被调用，
 *   如果 onTouch 返回 true，那么 onTouchEvent 将不会被调用
 *   （View 设置的 OnTouchListener 优先级高于自身的 onTouchEvent）
 *   - onTouchEvent() 中如果当前设置了 OnClickListener，那么它的 onClick() 会被回调（OnClickListener 优先级最低，处于事件传递的尾端）
 *  - 事件传递的顺序
 *   - Activity -> Window -> View（事件总是先传递到 Activity，再传递给 Window，最后传递到 View，
 *   顶层 View 收到事件后会按照事件分发机制进行分发事件
 *   - 如果一个 View 的 OnTouchEvent 返回 false，那么它的父容器 onTouchEvent 将会被调用，
 *   以此类推，如果所有的 View 都不处理这个事件，那么这个事件最终会传递到 Activity，让它处理（Activity 的 onTouchEvent 会被调用）
 *  - 知识点
 *   - 同一个事件序列是指从手指接触屏幕的那一刻开始，到手指离开屏幕的那一刻结束，在这个过程中，产生的一系列事件（DOWN -> MOVE -> UP）
 *   - 正常情况下，一个事件序列只能被一个 View 拦截且消耗，因为一旦一个元素拦截了某次事件，那么同个事件序列内的所有事件都会交给它处理
 *   因此同一事件序列中事件不能分发由两个 View 同时处理
 *   - 某个 View 一旦开始处理事件，如果它不消耗 ACTION_DOWN 事件（onTouchEvent 返回了 false），那么同一个事件序列中的其他事件都不会
 *   交给它来处理
 *   - 如果 View 不消耗 ACTION_DOWN 以外的事件，那么这个点击事件会消失，此时副元素的 onTouchEvent 并不会被调用。并且当 View 可以
 *   持续收到后续事件，最终这些消失的点击事件会传递给 Activity 处理
 *   - 一个 View 一旦决定拦截，那么这个事件序列都只能由他处理
 *   - ViewGroup 默认不拦截任何事件（Android ViewGroup 源码中 onInterceptTouchEvent 默认返回 false）
 *   - View 没有 onInterceptTouchEvent，一旦点击事件传递给他，那么它的 onTouchEvent 就会被调用
 *   - View 的 onTouchEvent 默认都会消耗事件（return true），除非它是不可点击的（clickable 和 longClickable 同时为 false）
 *   View 的 longClickable 默认为 false，button clickable 默认为 true，textView clickable 默认为 false
 *   - View 的 enable 不影响 onTouchEvent 的默认返回值，就算 View 是 disable 状态，只要它的 clickable 或 longClickable 一个为 true
 *   那么它的 onTouchEvent 会返回 true
 *   - onClick 会发生的前提是当前 View 是可点击的，并且能收到 DOWN 和 UP 事件
 *   - 事件传递过程是有外向内，即事件总是先传递给父元素，然后在由父元素分发给子 View，通过 requestDisallowInterceptTouchEvent() 可以
 *   在子元素中干预父元素的事件分发过程，但 ACTION_DOWN 事件除外
 *  - 源码分析
 *   - Activity#dispatchTouchEvent
 *    - 获取 window 并调用其 superDispatchTouchEvent 进行分发
 *    - 如果返回 false，activity 调用自己的 onTouchEvent 来处理事件
 *   - PhoneWindow（是 Window 的唯一实现）
 *    - PhoneWindow#superDispatchTouchEvent() 直接调用 mDecor(DecorView)#superDispatchTouchEvent()
 *   - DecorView
 *    - DecorView#superDispatchTouchEvent() 直接调用 ViewGroup#dispatchTouchEvent()
 *   - ViewGroup#dispatchTouchEvent()（主要实现）
 *    - mFirstTouchTarget 当事件由 ViewGroup 子类处理成功时，mFirstTouchTarget 会被赋值指向子 View，
 *    如果是 ACTION_DOWN 事件，ViewGroup#onInterceptTouchEvent 返回了 true，那么 ACTION_MOVE 和 ACTION_UP
 *    到来时，mFirstTouchTarget 为 null，直接拦截
 *    如果是 ACTION_DOWN 事件，ViewGroup#onInterceptTouchEvent 返回了 false，mFirstTouchTarget 将不为空，
 *    ACTION_MOVE 和 ACTION_UP 来到时，会走执行 if 中的判断
 *    - 当 ACTION_MOVE 和 ACTION_UP 到来，并且 mFirstTouchTarget 为空（没有子 View 消费），
 *    onInterceptTouchEvent 不会被再次调用，并且同一个事件序列的事件都交给它处理
 *    - FLAG_DISALLOW_INTERCEPT 这个标记位通过 requestDisallowInterceptTouchEvent 设置，一般用在子 View 中
 *    - 一旦 requestDisallowInterceptTouchEvent 设置后，ViewGroup 将无法拦截除 ACTION_DOWN 以外的其他点击事件
 *    因为 ViewGroup 在分发事件时，如果是 ACTION_DOWN 就会重置 FLAG_DISALLOW_INTERCEPT 这个标志位，导致子 View 的标志位无效
 *    - 如果 ViewGroup 不拦截事件时，遍历所有子 View，然后判断子 View 是否能够接收点击事件（点击是否在子 View 的区域）
 *    如果某个子 View 可以接收到点击，那么事件就会传递给它来处理
 *    - ViewGroup#dispatchTransformedTouchEvent
 *     - 遍历子 View 时，child 不为 null，直接会调用子 View#dispatchTouchEvent() ，这样事件就交由子 View 处理
 *     如果子 View#dispatchTouchEvent 返回 true，那么 mFirstTouchTarget 就会被赋值同时跳出循环
 *     - 如果遍历所有的子 View 后事件没有被消费
 *      - ViewGroup 没有子元素
 *      - 子 View 处理了点击事件，但 dispatchTouchEvent 返回了 false，此时 dispatchTransformedTouchEvent 的 child 为 null
 *      这里就会转到 View#dispatchTouchEvent（不是 ViewGroup#dispatchTouchEvent）
 *     - 如果子 View#dispatchTouchEvent 返回 true，就会调用 View 或 ViewGroup 的 dispatchTouchEvent()
 *   - View#dispatchTouchEvent（View 的事件分发）
 *    - 因为 View 没有子 View，所有无法向下传递事件，只能自己处理
 *    - 首先判断是否设置了 mOnTouchListener，如果 OnTouchListener#onTouch 返回 true，那么 onTouchEvent 不会被调用
 *    OnTouchListener#onTouch 的优先级高于 onTouchEvent
 *    - View 的 onTouchEvent()，只要 View 的 CLICKABLE、LONG_CLICKABLE 以及 CONTEXT_CLICKABLE 有一个为 true
 *    那么 View 就会消费该事件
 *    - 如果 View 设置了 OnClickListener，那么就会在 ACTION_UP 时调用它的 onClick 方法
 *    - View 的 clickable
 *     - 通过设置 OnClickListener 或 LongClickListener 可以分别改变 CLICKABLE 和 LONG_CLICKABLE 属性值
 *
 * include 标签如果不设置 id 默认会取被引入布局的 root 的 id 作为 id，如果设置了 id 就会取设置的 id
 * merge 标签会取引入的布局的 root 作为根（view id 一样的话，就找不到了）
 */
class ViewActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.pview_activity)

        findViewById<PViewHorizontalLayout>(R.id.horizontalLayout).setOnClickListener {
            Toast.makeText(this, "点击了能滚的", Toast.LENGTH_SHORT).show()
        }
        findViewById<PViewCircleView>(R.id.circleView).setOnClickListener {
            Toast.makeText(this, "点击了第一个圆", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.sameIdTv).text = "修改了 sameIdTv 的值"
    }
}