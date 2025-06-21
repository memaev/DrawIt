package com.llc.drawit.presentation.recyclerView;

import android.content.res.ColorStateList;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.llc.drawit.R;
import com.llc.drawit.databinding.MemberImageItemLayoutBinding;
import com.llc.drawit.domain.entities.User;
import com.llc.drawit.presentation.util.StringUtil;
import com.llc.drawit.presentation.util.color.DefaultProfileImageColorsUtil;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {

    private final List<User> members;

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
        MemberImageItemLayoutBinding binding = MemberImageItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MembersAdapter.ViewHolder holder, int position) {
        holder.bind(members.get(position));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private MemberImageItemLayoutBinding binding;

        public ViewHolder(@NonNull MemberImageItemLayoutBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }

        public void bind(User member) {
            // display the profile image
            if (member.getProfileImageUrl().isEmpty()) {
                binding.ivMemberAvatar.setVisibility(View.GONE);
                binding.defaultProfileImage.setVisibility(View.VISIBLE);

                // we want to display the initials of the user
                Pair<Integer, Integer> randomColorsPair = DefaultProfileImageColorsUtil.getRandomColorsPair();
                binding.defaultProfileImage.setBackgroundTintList(ColorStateList.valueOf(
                        binding.getRoot().getContext().getColor(randomColorsPair.first)
                ));
                binding.defaultProfileImage.setTextColor(binding.getRoot().getContext().getColor(randomColorsPair.second));

                // display the initials
                binding.defaultProfileImage.setText(StringUtil.getInitials(member.getName()));
            }
            else {
                Glide.with(binding.getRoot().getContext()).load(member.getProfileImageUrl()).into(binding.ivMemberAvatar);
                binding.ivMemberAvatar.setVisibility(View.VISIBLE);
                binding.defaultProfileImage.setVisibility(View.GONE);
            }
        }
    }
}
