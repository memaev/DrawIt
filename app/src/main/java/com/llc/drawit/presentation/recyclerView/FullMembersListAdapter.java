package com.llc.drawit.presentation.recyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.llc.drawit.databinding.WhiteboardItemLayoutBinding;
import com.llc.drawit.domain.entities.User;
import com.llc.drawit.domain.util.callbacks.MembersListener;

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
        WhiteboardItemLayoutBinding binding = WhiteboardItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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

        private final WhiteboardItemLayoutBinding binding;

        public ViewHolder(@NonNull WhiteboardItemLayoutBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }

        public void bind(User user) {
            binding.tvWhiteboardName.setText(user.getName());

            binding.getRoot().setOnClickListener(v -> membersListener.onMemberSelected(user));
        }
    }
}
