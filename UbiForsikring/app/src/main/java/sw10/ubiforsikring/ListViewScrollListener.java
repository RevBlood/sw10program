package sw10.ubiforsikring;

import android.content.Context;
import android.widget.AbsListView;

public abstract class ListViewScrollListener implements AbsListView.OnScrollListener {
    Context mContext;
    int mLoadThreshold; // Defines when to load more entries
    int mChunkSize;
    int mTotalItemCount = 0;
    boolean mLoading = false;

    public ListViewScrollListener(Context context, int visibleThreshold, int chunkSize) {
        mContext = context;
        mLoadThreshold = visibleThreshold;
        mChunkSize = chunkSize;
        mLoading = true;
        GetMoreEntries(mTotalItemCount);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int newTotalItemCount) {
        // If previously loading, and a chunk of trips has been added to the listview, update status
        if (mLoading && newTotalItemCount == mTotalItemCount + mChunkSize) {
            mTotalItemCount = newTotalItemCount;
            mLoading = false;
        }

        // If not loading, check if more data should be loaded.
        if (!mLoading && mTotalItemCount - visibleItemCount <= firstVisibleItem + mLoadThreshold) {
            mLoading = true;
            GetMoreEntries(mTotalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public abstract void GetMoreEntries(int index);
}