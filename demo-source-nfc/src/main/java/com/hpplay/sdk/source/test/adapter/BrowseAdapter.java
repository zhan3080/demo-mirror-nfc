package com.hpplay.sdk.source.test.adapter;

/**
 * Created by Zippo on 2019/3/16.
 * Date: 2019/3/16
 * Time: 13:38:24
 */

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hpplay.sdk.source.browse.api.LelinkServiceInfo;
import com.hpplay.sdk.source.test.R;
import com.hpplay.sdk.source.test.utils.ToastUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BrowseAdapter extends RecyclerView.Adapter<BrowseAdapter.RecyclerHolder> {

    private static final String TAG = "BrowseAdapter";
    private Context mContext;
    private List<LelinkServiceInfo> mDatas;
    private LayoutInflater mInflater;
    private OnItemClickListener mItemClickListener;
    private LelinkServiceInfo mSelectInfo;

    public BrowseAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDatas = new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mItemClickListener = l;
    }

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_browse, parent, false);
        return new RecyclerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        LelinkServiceInfo info = mDatas.get(position);
        if (null == info) {
            return;
        }
        if (info.isConnect()) {
            // 选中
            holder.textView.setBackgroundColor(Color.GREEN);
        } else {
            //
            holder.textView.setBackgroundColor(Color.YELLOW);
        }

        String item;
        if (TextUtils.isEmpty(info.getName())) {
            item = info.getIp() + " : " + info.getPort();
        } else {
            item = info.getName() + ":" + info.getPinCode();
        }
        holder.textView.setText(item);
        holder.textView.setTag(R.id.id_position, position);
        holder.textView.setTag(R.id.id_info, info);
        holder.textView.setOnClickListener(mOnItemClickListener);
        holder.textView.setOnLongClickListener(mOnItemLongClickListener);
    }

    @Override
    public int getItemCount() {
        return null == mDatas ? 0 : mDatas.size();
    }


    public void updateData(LelinkServiceInfo info) {
        if (null == info) {
            return;
        }
        Iterator<LelinkServiceInfo> iterator = mDatas.iterator();
        LelinkServiceInfo next;
        while (iterator.hasNext()) {
            next = iterator.next();
            if (TextUtils.equals(next.getIp(), info.getIp())) {
                if (TextUtils.equals(next.getPinCode(), info.getPinCode())
                        && TextUtils.equals(next.getName(), info.getName())) {
                    ToastUtil.show(mContext, "添加错误：重复设备");
                    return;
                } else {
                    // 有重复，需要更新设备
                    iterator.remove();
                    break;
                }
            }
        }
        mDatas.add(info);
        notifyDataSetChanged();
    }

    public void updateDatas(List<LelinkServiceInfo> infos) {
        if (null != infos) {
            mDatas.clear();
            mDatas.addAll(infos);
            notifyDataSetChanged();
        }
    }

    public void removeData(int position) {
        mDatas.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public LelinkServiceInfo[] getSelectInfos() {
        List<LelinkServiceInfo> selectInfos = new ArrayList<>();
        for (LelinkServiceInfo info : mDatas) {
            if (info.isConnect()) {
                selectInfos.add(info);
            }
        }
        return selectInfos.toArray(new LelinkServiceInfo[0]);
    }

    class RecyclerHolder extends RecyclerView.ViewHolder {

        TextView textView;

        private RecyclerHolder(android.view.View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textview);
        }

    }

    private View.OnClickListener mOnItemClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.id_position);
            LelinkServiceInfo info = (LelinkServiceInfo) v.getTag(R.id.id_info);
            if (null != mItemClickListener) {
                mItemClickListener.onClick(position, info);
            }
        }

    };
    private View.OnLongClickListener mOnItemLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            int position = (int) v.getTag(R.id.id_position);
            LelinkServiceInfo info = (LelinkServiceInfo) v.getTag(R.id.id_info);
            if (null != mItemClickListener) {
                return mItemClickListener.onLongClick(position, info);
            }
            return false;
        }

    };

    public interface OnItemClickListener {

        void onClick(int position, LelinkServiceInfo info);

        boolean onLongClick(int position, LelinkServiceInfo infos);

    }

}