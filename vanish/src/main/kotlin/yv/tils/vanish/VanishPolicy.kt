package yv.tils.vanish

object VanishPolicy {
    /** Layer zero is a visible player; active staff can see their own layer and lower ones. */
    fun visible(viewerLayer: Int, targetLayer: Int) = targetLayer == 0 || viewerLayer >= targetLayer
}
