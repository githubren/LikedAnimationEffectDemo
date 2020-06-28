package com.rb95.likedanimationeffectdemo

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.animation.*
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import java.util.*
import kotlin.collections.ArrayList

/**
 * @des 点赞效果布局
 * @author RenBing
 * @date 2020/6/24 0024
 */
class LikedEffectLayout @JvmOverloads constructor(context: Context,attr:AttributeSet ?= null,defAttr:Int = 0) : RelativeLayout(context,attr,defAttr){
    private lateinit var mRed : Drawable //红心心
    private lateinit var mPink : Drawable //粉心心
    private lateinit var mBlue : Drawable //蓝心心
    private lateinit var mDrawables : ArrayList<Drawable> //图片集合 随机选中一张图片
    private lateinit var mInterpolators : ArrayList<Interpolator> //插值器集合 随机选中一个插值器
    private var mDrawableHeight = 0 //图片高度
    private var mDrawableWidth = 0 //图片宽度
    private var mHeight = 0 //布局高度
    private var mWidth = 0 //布局宽度
    private var mParams : LayoutParams //图片参数
    private var mRandom = Random() //随机数

    init {
        initDrawable() //初始化图片集
        initInterpolator() //初始化插值器集
        mParams = LayoutParams(mDrawableWidth/5,mDrawableHeight/5) //设置图片参数 因为找的图片尺寸太大了  所以缩小了5倍
        mParams.addRule(CENTER_HORIZONTAL, TRUE) //设置图片水平居中
        mParams.addRule(ALIGN_PARENT_BOTTOM, TRUE) //设置图片位于容器底部
    }

    /**
     * 初始化插值器集
     */
    private fun initInterpolator() {
        mInterpolators = arrayListOf()
        mInterpolators.add(LinearInterpolator())
        mInterpolators.add(AccelerateDecelerateInterpolator())
        mInterpolators.add(AccelerateInterpolator())
        mInterpolators.add(DecelerateInterpolator())
    }

    /**
     * 初始化图片集
     */
    private fun initDrawable() {
        mRed = resources.getDrawable(R.drawable.love_red,null)
        mPink = resources.getDrawable(R.drawable.love_pink,null)
        mBlue = resources.getDrawable(R.drawable.love_blue,null)

        mDrawables = arrayListOf()
        mDrawables.apply {
            add(mRed)
            add(mPink)
            add(mBlue)
        }

        mDrawableHeight = mRed.intrinsicHeight //获取图片高度
        mDrawableWidth = mRed.intrinsicWidth //获取图片宽度
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        mWidth = measuredWidth //获取布局宽度
        mHeight = measuredHeight //获取布局高度
    }

    /**
     * 暴露给外部调用的生成图片的方法
     * 添加点赞效果的图片
     * 点击一次生成一张图片 然后沿着贝塞尔曲线移动
     */
    fun addLove(){
        val loveIv = ImageView(context)
        loveIv.setImageDrawable(mDrawables[mRandom.nextInt(mDrawables.size)]) //从图片集随机取出一张
        loveIv.layoutParams = mParams
        addView(loveIv)

        val finalSet = getAnimatorSet(loveIv)//设置动画效果
        finalSet.start() //动画开始
    }

    /**
     * 动画效果
     * @param iv 动画target
     */
    private fun getAnimatorSet(iv: ImageView): AnimatorSet {
        //透明度
        val alphaAni = ObjectAnimator.ofFloat(iv,"alpha",0.3f,1f)
        //x方向缩放
        val scaleX = ObjectAnimator.ofFloat(iv,"scaleX",0.2f,1f)
        //方向缩放
        val scaleY = ObjectAnimator.ofFloat(iv,"scaleY",0.2f,1f)
        val allAnimatorSet = AnimatorSet() //从图片生成到图片移出容器整个动画过程
        val createAnimatorSet = AnimatorSet() //图片生成动画
        createAnimatorSet.playTogether(alphaAni,scaleX,scaleY) //图片的生成伴随着三种动画的同时发生
        createAnimatorSet.duration = 500
        allAnimatorSet.playSequentially(createAnimatorSet,getBezierValueAnimator(iv)) //括号内的动画按顺序先后执行
        return allAnimatorSet
    }

    /**
     * 贝塞尔曲线动画
     * @param iv target
     */
    private fun getBezierValueAnimator(iv: ImageView) : ValueAnimator{
        //起始点 此次是放在屏幕底部水平中央的位置
        val p0 = PointF(mWidth/2 - mDrawableWidth/10*1f,mHeight - mDrawableHeight/5*1f)
        //第一个的拐点 x坐标在屏幕内随机取 y坐标得保证比第二个拐点得要小
        val p1 = PointF(mRandom.nextInt(mWidth)*1f,mRandom.nextInt(mHeight/2)*1f)
        //第二个拐点
        val p2 = PointF(mRandom.nextInt(mWidth)*1f,mRandom.nextInt(mHeight/2)*1f+mHeight/2)
        //终点 屏幕得顶部随机生成
        val p3 = PointF(mRandom.nextInt(mWidth - mDrawableWidth/5)*1f,0f)
        val evaluator = BezierEvaluator(p1,p2) //传入两个拐点生成贝塞尔估值器
        val animator = ValueAnimator.ofObject(evaluator,p0,p3)//生成属性动画
        /**
         * 监听动画执行过程不断改变图片得坐标 达到动画得效果
         */
        animator.addUpdateListener {
            val point = it.animatedValue as PointF
            iv.x = point.x
            iv.y = point.y
            iv.alpha = (1-it.animatedFraction) //伴随一个透明度得变化
        }
        /**
         * 动画结束从容器中移除target 内存优化
         */
        animator.doOnEnd {
            removeView(iv)
        }
        animator.setTarget(iv)
        animator.interpolator = mInterpolators[mRandom.nextInt(4)] //随机生成一个插值器 控制运动速度
        animator.duration = 3000
        return animator
    }
}