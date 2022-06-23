package com.wangjessica.jwfinalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoomsRecyclerAdapter extends RecyclerView.Adapter<RoomsRecyclerAdapter.MyViewHolder>{

    private List<Room> RoomList;
    private ClickListener<Room> clickListener;

    public RoomsRecyclerAdapter(List<Room> RoomList){
        this.RoomList = RoomList;
    }

    @Override
    public RoomsRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rooms_cardview, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RoomsRecyclerAdapter.MyViewHolder holder, final int position) {
        final Room cur = RoomList.get(position);
        holder.title.setText(cur.getTitle());
        // Get the capacity info
        holder.capacityCnt.setText("");
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onItemClick(cur);
            }
        });
    }
    @Override
    public int getItemCount() {
        return RoomList.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView capacityCnt;
        private CardView cardView;
        public MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            capacityCnt = itemView.findViewById(R.id.capacity_cnt);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    public void setOnItemClickListener(ClickListener<Room> listener){
        this.clickListener = listener;
    }
}
