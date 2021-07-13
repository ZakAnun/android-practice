package com.zakli.practiceview.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * 加载 Bitmap 的四个方法
 *  - decodeFile（从文件加载）、decodeResource（从资源文件加载）、
 *  decodeStream（从流加载）、decodeArray（从字节数组加载）
 *  - decodeFile、decodeResource 间接调用了 decodeStream
 *  - 避免加载 Bitmap OOM（通过 BitmapFactory.Options 可按一定采样率加载缩小后的图片，
 *  显示缩小后的图片可以降低内存占用从而避免 OOM）
 *   - 用到的是 BitmapFactory.Options#inSampleSize（采样率）
 *    - inSampleSize == 1 时，采样后的图片大小和原图大小相等
 *    - inSampleSize > 1 时（如 2），采样后的图片宽/高均为原图的 1/2，像素数为原图的 1/4，
 *    占用内存大小也是原图的 1/4
 *    - 1024 * 1024 像素采用 ARGB_8888 格式存储的图片来说，占用内存为 1024*1024*4（4M），
 *    inSampleSize == 2，那么采样后图片的内存占用只有 512*512*4（1M）
 *    - inSampleSize > 1 时才会有效果，且采样率作用于宽/高，将会导致缩放后的图片大小
 *    以采样率的 2 次方形式递减，即缩放比例为 1/inSampleSiz^2
 *  - 图片内存计算
 *   - ARGB_8888（最站内存），一个像素占 32 位，8 位 = 1 字节，所以一个像素占 4 个字节
 *   - ARGB_4444，一个像素占 2 字节
 *   - RGB_555，一个像素占 2 字节
 *   - ALPHA_8，一个像素占 1 字节
 *
 * 常见的图片压缩方式
 *  - 采样率压缩
 *   - 流程
 *    - 将 BitmapFactory.Options#inJustDecodeBounds 参数设置为 true 并加载图片，
 *    此时 BitmapFactory 只会解析图片的原始宽/高信息，并不会真正加载图片
 *    - 从 BitmapFactory.Options 中取出原始宽/高信息（对应于 outWidth，outHeight）
 *    - 根据采样率的规则并结合目标 View 所需大小计算出采样率 inSampleSize
 *    - 将 BitmapFactory.Options#inJustDecodeBounds 参数设置为 false，根据 inSampleSize 重新加载图片
 *  - 质量压缩
 *   - 保持像素数不变的情况下改变图片的位深及透明度（通过算法抹掉（同化）图片中的一些相近的像素）达到
 *   压缩的目的（因为 png 是无损压缩，所以质量压缩对 png 无效）
 *  - 尺寸压缩
 *   - 通过减少单位尺寸的像素值（真正意义上的降低像素）
 *  - 通过 JNI 调用 libjpeg 库压缩
 *
 * Android 目前常用的图片格式
 *  - png: 无损压缩图片格式，支持 Alpha 通道（一般格式）
 *  - jpeg: 有损压缩图片格式，不支持背景透明，是用于照片等色彩丰富的大图压缩，不适合 logo
 *  - webp: 同时提供有损压缩和无损压缩的图片格式，无损 webp 平均比 png 小 26%，有损 webp 平均比 jpeg 小 25% - 34%
 *
 * 图片优化
 *  - 使用 .9 图代替大图
 *  - 使用绘制背景或者 Drawable 代替图片
 *  - 使用采样率压缩
 *  - 用完就回收 Bitmap
 *
 * ImageView#scaleType
 *  - fixXy 非等比缩放，将图片宽高限制在空间内并且充满控件四周
 *  - fitStart 两边都需要缩放到控件内，至少有一边于控件的某边齐平，缩放后从左上角开始展示
 *  - fitEnd 与 fitStart 类似，但缩放后从右下角开始展示
 *  - fitCenter 与 fitStart 类似，但缩放后剧中展示
 *  - center 不缩放，图片剧中展示
 *  - centerCrop 等比例缩放，图片长宽都需要大于或等于控件长宽，居中展示
 *  - centerInside 等比缩放，图片长宽有一边大于控件尺寸则缩放，两边都需要缩放到控件内，
 *  如果图片长宽都小于控件尺寸则不缩放，居中展示
 *
 *  adjustViewBounds 设置为 true，图片多大，控件就多大，根据图片的尺寸变化，此时图片不缩放，不平移
 *  adjustViewBounds（控制控件大小） 与 scaleType（控制图片的显示大小） 是两种不同的作用
 *
 * LruCache
 *  - 内部采用 LinkedHashMap 以强引用的方式存储外界的缓存对象
 *  - 是线程安全的，put、get 操作都用 synchronized 来保证安全
 *
 * 大图加载使用 BitmapRegionDecoder（图片区域解码）
 *
 */
object BitmapSampleUtil {

    /**
     * 采样率压缩
     *
     * @param res res
     * @param reqWidth reqWidth
     * @param reqHeight reqHeight
     */
    @JvmStatic
    fun decodeSampleBitmapFromResource(res: Resources, resId: Int, reqWidth: Int,reqHeight: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId)

        options.inSampleSize = calculateInSampleSize(options = options, reqWidth = reqWidth, reqHeight = reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(res, resId, options)
    }

    @JvmStatic
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val width = options.outWidth
        val height = options.outHeight

        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * 质量压缩
     *
     * @param bitmap
     * @param file
     * @param quality 0 - 100（100 为不压缩）
     */
    @JvmStatic
    fun qualityCompress(bitmap: Bitmap, file: File, quality: Int = 20) {
        val baos = ByteArrayOutputStream()
        // 压缩后的数据存放到 baos 中
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        kotlin.runCatching {
            val fos = FileOutputStream(file)
            fos.write(baos.toByteArray())
            fos.flush()
            fos.close()
        }.onFailure {
            it.printStackTrace()
        }
    }

    /**
     * 尺寸压缩
     *
     * @param bitmap
     * @param file
     * @param ratio 尺寸压缩倍数，值越大，图片尺寸越小
     */
    fun sizeCompress(bitmap: Bitmap, file: File, ratio: Int = 8) {
        val result = Bitmap.createBitmap(bitmap.width / ratio,
            bitmap.height / ratio, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas()
//        val rect = Rect(0, 0,
//            bitmap.width / ratio, bitmap.height / ratio)
//        canvas.drawBitmap(bitmap, null, rect, null)

        val baos = ByteArrayOutputStream()
        result.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        kotlin.runCatching {
            val fos = FileOutputStream(file)
            fos.write(baos.toByteArray())
            fos.flush()
            fos.close()
        }.onFailure {
            it.printStackTrace()
        }
    }
}