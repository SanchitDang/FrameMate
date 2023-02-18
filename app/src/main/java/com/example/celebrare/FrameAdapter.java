package com.example.celebrare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class FrameAdapter extends RecyclerView.Adapter<FrameAdapter.FrameViewHolder> {

    Context context;
    List<Integer> frameList;
    FrameAdapterListner listner;
    int row_selected = -1;

    public FrameAdapter(Context context, FrameAdapterListner listner){
        this.context=context;
        this.frameList=getFrameList();
        this.listner = listner;

    }

    public List<Integer> getFrameList(){
        List<Integer> result = new ArrayList<>();
        result.add(R.drawable.user_image_frame_1);
        result.add(R.drawable.user_image_frame_2);
        result.add(R.drawable.user_image_frame_3);
        result.add(R.drawable.user_image_frame_4);
        return result;
    }

    @NonNull
    @Override
    public FrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.activity_frame_adapter,parent,false);

        return new FrameViewHolder((itemView));
    }

    @Override
    public void onBindViewHolder(@NonNull FrameViewHolder holder, int position) {
        if(row_selected==position){
            holder.img_check.setVisibility(View.VISIBLE);
        }else{
            holder.img_check.setVisibility(View.INVISIBLE);
        }

        holder.img_frame.setImageResource(frameList.get(position));
    }

    @Override
    public int getItemCount() {
        return frameList.size() ;
    }

    public class FrameViewHolder extends RecyclerView.ViewHolder {
        ImageView img_check, img_frame;
        public FrameViewHolder(@NonNull View itemView) {
            super(itemView);

            img_check=itemView
                    .findViewById(R.id.img_check);
            img_frame=itemView
                    .findViewById(R.id.img_frame);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listner.onFrameSelected((frameList.get(getAdapterPosition())));
                    row_selected = getAdapterPosition();
                     notifyDataSetChanged();
                }
            });
        }
    }

    public interface FrameAdapterListner{
        void onFrameSelected(int frame);
    }

}