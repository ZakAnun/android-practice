package com.zakli.practiceview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

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
 */
class ViewActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.pview_activity)


    }
}