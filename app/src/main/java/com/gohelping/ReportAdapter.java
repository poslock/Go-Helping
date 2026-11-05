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

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<Report> reportList;
    private OnReportActionListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public interface OnReportActionListener {
        void onBlockUser(Report report, int position);
        void onChatUser(Report report);
        void onDismissReport(Report report, int position);
    }

    public ReportAdapter(List<Report> reportList, OnReportActionListener listener) {
        this.reportList = reportList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);

        holder.tvQuestTitle.setText(report.getQuestTitle() != null ? report.getQuestTitle() : "Unknown Quest");
        holder.tvReason.setText("Alasan: " + (report.getReason() != null ? report.getReason() : "Tidak ada"));
        holder.tvReporterId.setText("Pelapor: " + (report.getReporterId() != null ? report.getReporterId() : "Unknown"));
        holder.tvReportedUserId.setText("Terlapor: " + (report.getReportedUserId() != null ? report.getReportedUserId() : "Unknown"));

        if (report.getTimestamp() != null) {
            holder.tvDate.setText(dateFormat.format(report.getTimestamp().toDate()));
        }

        int pos = holder.getAdapterPosition();

        holder.btnBlockUser.setOnClickListener(v -> {
            if (listener != null) listener.onBlockUser(report, pos);
        });

        holder.btnChatUser.setOnClickListener(v -> {
            if (listener != null) listener.onChatUser(report);
        });

        holder.btnDismiss.setOnClickListener(v -> {
            if (listener != null) listener.onDismissReport(report, pos);
        });
    }

    @Override
    public int getItemCount() {
        return reportList != null ? reportList.size() : 0;
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestTitle, tvReason, tvReporterId, tvReportedUserId, tvDate;
        Button btnBlockUser, btnChatUser, btnDismiss;

        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestTitle = itemView.findViewById(R.id.tvReportQuestTitle);
            tvReason = itemView.findViewById(R.id.tvReportReason);
            tvReporterId = itemView.findViewById(R.id.tvReporterId);
            tvReportedUserId = itemView.findViewById(R.id.tvReportedUserId);
            tvDate = itemView.findViewById(R.id.tvReportDate);
            btnBlockUser = itemView.findViewById(R.id.btnBlockUser);
            btnChatUser = itemView.findViewById(R.id.btnChatUser);
            btnDismiss = itemView.findViewById(R.id.btnDismissReport);
        }
    }
}