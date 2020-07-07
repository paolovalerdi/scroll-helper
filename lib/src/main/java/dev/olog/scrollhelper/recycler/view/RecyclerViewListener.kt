package dev.olog.scrollhelper.recycler.view

import androidx.core.math.MathUtils
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import dev.olog.scrollhelper.Hash
import dev.olog.scrollhelper.ScrollHelper

internal class RecyclerViewListener(
    private val scrollHelper: ScrollHelper
) : RecyclerView.OnScrollListener() {

    // 1) scroll eventual fab
    // 2) scroll tab layout
    // 3) scroll toolbar
    // 4) scroll bottomsheet
    // 5) scroll bottom navigation
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        onScrolledInternal(recyclerView, dy, false)
    }

    internal fun onScrolledInternal(
        recyclerView: RecyclerView,
        dy: Int,
        isOverScroll: Boolean
    ) {
        val hashCode: Hash = recyclerView.hashCode()
        scrollBottomViews(hashCode, dy)

        if (!isOverScroll) {
            scrollTopViews(hashCode, dy)
        }
    }

    private fun scrollBottomViews(
        hashCode: Hash,
        dy: Int
    ) {
        val maxBottom = if (scrollHelper.fullScrollBottom) {
            scrollHelper.findSecondBaseline()
        } else {
            scrollHelper.findFirstBaseline()
        }.toFloat()

        val bottomNavigation = scrollHelper.bottomNavigation
        val bottomSheet = scrollHelper.bottomSheet
        val fab = scrollHelper.fabMap[hashCode]

        // update bottom navigation
        if (bottomNavigation != null &&
            (bottomSheet == null || bottomSheet.behavior.state == STATE_COLLAPSED || bottomSheet.behavior.state == STATE_HIDDEN)
        ) {
            val translation = MathUtils.clamp(
                bottomNavigation.translationY + dy,
                0f,
                maxBottom
            )
            bottomNavigation.translationY = translation
        }

        // update bottom sheet
        if (bottomSheet != null && (bottomSheet.behavior.state == STATE_COLLAPSED || bottomSheet.behavior.state == STATE_HIDDEN)) {
            val bottomSheetView = bottomSheet.view
            val translation = MathUtils.clamp(
                bottomSheetView.translationY + dy,
                0f,
                maxBottom
            )
            bottomSheetView.translationY = translation
        }

        // scroll fab
        if (fab != null && (bottomNavigation != null || bottomSheet != null)) {
            val translation = MathUtils.clamp(
                fab.translationY + dy,
                0f,
                maxBottom
            )
            fab.translationY = translation
        }
    }

    private fun scrollTopViews(
        hashCode: Hash,
        dy: Int
    ) {
        val toolbar = scrollHelper.toolbarMap[hashCode]
        val tabLayout = scrollHelper.tabLayoutMap[hashCode]
        val mergedToolbar = scrollHelper.mergedToolbarTabMap[hashCode]

        val maxTop = if (scrollHelper.fullScrollTop) {
            scrollHelper.findSecondCap(tabLayout, toolbar)
        } else {
            scrollHelper.findFirstCap(tabLayout, toolbar)
        }.toFloat()

        if (mergedToolbar != null) {
            if (toolbar != null) {
                val translation = if (scrollHelper.toolbarHeight < scrollHelper.statusBarHeight) {
                    MathUtils.clamp(
                        toolbar.translationY - dy,
                        -((scrollHelper.toolbarHeight.toFloat() + scrollHelper.statusBarHeight) - scrollHelper.tabLayoutHeight),
                        0f
                    )
                } else {
                    MathUtils.clamp(
                        toolbar.translationY - dy,
                        -scrollHelper.toolbarHeight.toFloat(),
                        0f
                    )
                }
                mergedToolbar.translationY = translation
                toolbar.translationY = translation
            }
           /* if (tabLayout != null) {
                val translation = MathUtils.clamp(
                    tabLayout.translationY - dy,
                    -toolbar?.height?.toFloat()!!,
                    0f
                )
                tabLayout.translationY = translation
            }*/
        } else {
            if (toolbar != null) {
                val translation = MathUtils.clamp(
                    toolbar.translationY - dy,
                    -maxTop,
                    0f
                )
                toolbar.translationY = translation
            }

            // scroll tablayout
            if (tabLayout != null) {
                val translation = MathUtils.clamp(
                    tabLayout.translationY - dy,
                    -maxTop,
                    0f
                )
                tabLayout.translationY = translation
            }
        }
    }
}