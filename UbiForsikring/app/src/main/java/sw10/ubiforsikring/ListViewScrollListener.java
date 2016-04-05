package sw10.ubiforsikring;

import android.content.Context;
import android.widget.AbsListView;

public abstract class ListViewScrollListener implements AbsListView.OnScrollListener {
    Context mContext;
    int mLoadThreshold = 3;
    int mTotalItemCount = 0;
    int mCurrentIndex = 10;
    boolean mLoading = true;

    public ListViewScrollListener(Context context, int visibleThreshold) {
        mContext = context;
        mLoadThreshold = visibleThreshold;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int newTotalItemCount) {
        // If the new total item count is smaller, reset the list
        if (newTotalItemCount < mTotalItemCount) {
            mCurrentIndex = 0;
            mTotalItemCount = newTotalItemCount;

            if (newTotalItemCount == 0) {
                this.mLoading = true;
            }
        }

        // If loading at this point, stop if dataset has changed - That means we're done loading. Then update with current info.
        if (mLoading && newTotalItemCount > mTotalItemCount) {
            mCurrentIndex += 10;
            mTotalItemCount = newTotalItemCount;
            mLoading = false;
        }

        // If not loading, check if more data should be loaded.
        if (!mLoading && (mTotalItemCount - visibleItemCount) <= (firstVisibleItem + mLoadThreshold)) {
            mLoading = true;
            GetMoreTrips(mCurrentIndex);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public abstract void GetMoreTrips(int index);
}