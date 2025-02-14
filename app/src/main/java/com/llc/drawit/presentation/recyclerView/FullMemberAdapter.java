package com.llc.drawit.presentation.recyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.llc.drawit.R;
import com.llc.drawit.domain.entities.User;
import com.llc.drawit.domain.util.callbacks.MembersListener;

import java.util.List;
import java.util.stream.Collectors;

public class FullMemberAdapter extends RecyclerView.Adapter<FullMemberAdapter.ViewHolder>{

    private List<User> filteredUsers;
    private List<User> users;
    private MembersListener membersListener;

    public FullMemberAdapter(List<User> users, MembersListener membersListener) {
        this.users = users;
        this.membersListener = membersListener;
        filter("");
    }

    public void filter(String query){
        filteredUsers = users.stream().filter(user -> user.getName()
                .contains(query)).collect(Collectors.toList());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.whiteboard_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = filteredUsers.get(position);
        holder.tvUsername.setText(user.getName());
        holder.itemView.setOnClickListener(v -> membersListener.onMemberSelected(user));
    }

    @Override
    public int getItemCount() {
        return filteredUsers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvUsername;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tv_whiteboard_name);
        }
    }
}
