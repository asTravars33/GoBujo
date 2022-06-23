package com.wangjessica.jwfinalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>{

    private List<Journal> journalList;
    private ClickListener<Journal> clickListener;

    public RecyclerAdapter(List<Journal> journalList){
        this.journalList = journalList;
    }

    @Override
    public RecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.journals_cardview,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.MyViewHolder holder, final int position) {
        final Journal cur = journalList.get(position);
        holder.title.setText(cur.getTitle());
        holder.image.setBackgroundResource(cur.getCoverImg());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onItemClick(cur);
            }
        });
    }
    @Override
    public int getItemCount() {
        return journalList.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private ImageView image;
        private CardView cardView;
        public MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            image = itemView.findViewById(R.id.image);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    public void setOnItemClickListener(ClickListener<Journal> listener){
        this.clickListener = listener;
    }
}
