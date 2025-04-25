package com.llc.drawit.presentation.recyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.llc.drawit.R;
import com.llc.drawit.domain.entities.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {

    private List<User> members;

    public MembersAdapter(List<User> members) {
        this.members = members;
    }

    public void addMember(User user) {
        members.add(user);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_image_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String memberProfileImageUrl = members.get(position).getProfileImageUrl();

        if (!memberProfileImageUrl.isEmpty())
            Glide.with(holder.itemView.getContext()).load(memberProfileImageUrl).into(holder.memberProfileIv);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView memberProfileIv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            memberProfileIv = itemView.findViewById(R.id.iv_member_avatar);
        }
    }
}
