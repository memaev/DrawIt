package com.llc.drawit.presentation.recyclerView;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.llc.drawit.R;
import com.llc.drawit.domain.entities.Whiteboard;
import com.llc.drawit.domain.util.Constants;
import com.llc.drawit.presentation.activity.WhiteboardActivity;

import java.util.List;

public class WhiteboardsAdapter extends RecyclerView.Adapter<WhiteboardsAdapter.ViewHolder> {

    private List<Whiteboard> whiteboards;

    public WhiteboardsAdapter(List<Whiteboard> whiteboards){
        this.whiteboards = whiteboards;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.whiteboard_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvWhiteboardName.setText(whiteboards.get(position).getName());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), WhiteboardActivity.class);
            intent.putExtra(Constants.WHITEBOARD_ID, whiteboards.get(position).getId());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return whiteboards.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvWhiteboardName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvWhiteboardName = itemView.findViewById(R.id.tv_whiteboard_name);
        }
    }
}
