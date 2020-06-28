package com.rb95.likedanimationeffectdemo

import android.animation.TypeEvaluator
import android.graphics.PointF

/**
 * @des 贝塞尔估值器
 * @author RenBing
 * @date 2020/6/24 0024
 */
class BezierEvaluator(private val p1: PointF, private val p2: PointF) : TypeEvaluator<PointF>{

    override fun evaluate(t: Float, p0: PointF?, p3: PointF?): PointF {
        val point = PointF()
        /**
         * kotlin语言中要注意这种分行得写法
         * 因为kotlin中没有分号 所以一个表达式要么写成一行 要么加上一个括号 否则这个估值器不生效
         */
        point.x = (p0!!.x*(1-t)*(1-t)*(1-t)
                +3*p1.x*t*(1-t)*(1-t)
                +3*p2.x*t*t*(1-t)
                +p3!!.x*t*t*t)
        point.y = p0.y*(1-t)*(1-t)*(1-t) +3*p1.y*t*(1-t)*(1-t) +3*p2.y*t*t*(1-t) +p3.y*t*t*t
        return point
    }
}