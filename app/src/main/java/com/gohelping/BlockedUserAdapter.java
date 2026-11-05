package com.gohelping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BlockedUserAdapter extends RecyclerView.Adapter<BlockedUserAdapter.ViewHolder> {

    private List<User> userList;
    private OnUnblockClickListener listener;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public interface OnUnblockClickListener {
        void onUnblockClick(User user);
    }

    public BlockedUserAdapter(List<User> userList, OnUnblockClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blocked_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvUserName.setText(user.getName());
        holder.tvUserEmail.setText(user.getEmail());
        
        if (user.getBlockedUntil() == Long.MAX_VALUE) {
            holder.tvBlockedUntil.setText("Diblokir Permanen");
        } else {
            holder.tvBlockedUntil.setText("Diblokir sampai: " + sdf.format(new Date(user.getBlockedUntil())));
        }

        String reason = user.getBlockedReason();
        holder.tvBlockedReason.setText("Alasan: " + (reason != null && !reason.isEmpty() ? reason : "-"));

        holder.btnUnblock.setOnClickListener(v -> listener.onUnblockClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvBlockedUntil, tvBlockedReason;
        Button btnUnblock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvBlockedUntil = itemView.findViewById(R.id.tvBlockedUntil);
            tvBlockedReason = itemView.findViewById(R.id.tvBlockedReason);
            btnUnblock = itemView.findViewById(R.id.btnUnblock);
        }
    }
}
