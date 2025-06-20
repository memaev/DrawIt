package com.llc.drawit.presentation.recyclerView;

import android.content.res.ColorStateList;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.llc.drawit.databinding.RvItemUserBinding;
import com.llc.drawit.domain.entities.User;
import com.llc.drawit.domain.util.callbacks.MembersListener;
import com.llc.drawit.presentation.util.StringUtil;
import com.llc.drawit.presentation.util.color.DefaultProfileImageColorsUtil;

import java.util.List;
import java.util.stream.Collectors;

public class FullMembersListAdapter extends RecyclerView.Adapter<FullMembersListAdapter.ViewHolder>{

    private final List<User> users;
    private final MembersListener membersListener;

    private final DiffUtil.ItemCallback<User> diffUtilCallback = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.equals(newItem);
        }
    };
    private final AsyncListDiffer<User> listDiffer = new AsyncListDiffer<>(this, diffUtilCallback);

    public FullMembersListAdapter(List<User> users, MembersListener membersListener) {
        this.users = users;
        this.listDiffer.submitList(users);
        this.membersListener = membersListener;
    }

    public void filter(String query){
        this.listDiffer.submitList(users.stream().filter(user -> user.getName()
                .contains(query)).collect(Collectors.toList()));
    }

    @NonNull
    @Override
    public FullMembersListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RvItemUserBinding binding = RvItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(this.listDiffer.getCurrentList().get(position));
    }

    @Override
    public int getItemCount() {
        return this.listDiffer.getCurrentList().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final RvItemUserBinding binding;

        public ViewHolder(@NonNull RvItemUserBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }

        public void bind(User user) {
            binding.tvUserName.setText(user.getName());

            // display the profile image
            if (user.getProfileImageUrl().isEmpty()) {
                binding.imageProfileUser.setVisibility(View.GONE);

                // we want to display the initials of the user
                Pair<Integer, Integer> randomColorsPair = DefaultProfileImageColorsUtil.getRandomColorsPair();
                binding.defaultProfileImage.setBackgroundTintList(ColorStateList.valueOf(
                        binding.getRoot().getContext().getColor(randomColorsPair.first)
                ));
                binding.defaultProfileImage.setTextColor(binding.getRoot().getContext().getColor(randomColorsPair.second));

                // display the initials
                binding.defaultProfileImage.setText(StringUtil.getInitials(user.getName()));
            }
            else {
                Glide.with(binding.getRoot().getContext()).load(user.getProfileImageUrl()).into(binding.imageProfileUser);
                binding.defaultProfileImage.setVisibility(View.GONE);
            }

            binding.getRoot().setOnClickListener(v -> membersListener.onMemberSelected(user));
        }
    }
}
