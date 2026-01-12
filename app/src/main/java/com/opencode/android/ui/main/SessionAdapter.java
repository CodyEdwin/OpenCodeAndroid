package com.opencode.android.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.opencode.android.R;
import com.opencode.android.data.local.entity.SessionEntity;
import com.opencode.android.databinding.ItemSessionBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter for displaying session items in a RecyclerView.
 */
public class SessionAdapter extends ListAdapter<SessionEntity, SessionAdapter.SessionViewHolder> {

    private final SessionClickListener listener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

    public interface SessionClickListener {
        void onSessionClick(SessionEntity session);
        void onSessionLongClick(SessionEntity session);
    }

    public SessionAdapter(SessionClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<SessionEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SessionEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull SessionEntity oldItem, @NonNull SessionEntity newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull SessionEntity oldItem, @NonNull SessionEntity newItem) {
                    return oldItem.getTitle().equals(newItem.getTitle()) &&
                           oldItem.getUpdatedAt().equals(newItem.getUpdatedAt()) &&
                           oldItem.getMessageCount() == newItem.getMessageCount() &&
                           oldItem.isPinned() == newItem.isPinned();
                }
            };

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSessionBinding binding = ItemSessionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SessionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        SessionEntity session = getItem(position);
        holder.bind(session);
    }

    class SessionViewHolder extends RecyclerView.ViewHolder {
        private final ItemSessionBinding binding;

        SessionViewHolder(ItemSessionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SessionEntity session) {
            binding.textTitle.setText(session.getTitle());
            binding.textModel.setText(session.getModelName());
            binding.textTime.setText(formatTime(session.getUpdatedAt()));
            binding.textMessageCount.setText(
                    itemView.getContext().getString(R.string.chat_token_count, session.getMessageCount()));

            // Pin indicator
            binding.iconPin.setVisibility(session.isPinned() ? View.VISIBLE : View.GONE);

            // Click listeners
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSessionClick(session);
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onSessionLongClick(session);
                    return true;
                }
                return false;
            });
        }

        private String formatTime(Date date) {
            if (date == null) {
                return "";
            }

            Calendar now = Calendar.getInstance();
            Calendar then = Calendar.getInstance();
            then.setTime(date);

            // Same day
            if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)) {
                return timeFormat.format(date);
            }

            // Yesterday
            now.add(Calendar.DAY_OF_YEAR, -1);
            if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)) {
                return itemView.getContext().getString(R.string.session_yesterday);
            }

            // This week
            if (now.get(Calendar.WEEK_OF_YEAR) == then.get(Calendar.WEEK_OF_YEAR)) {
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                return dayFormat.format(date);
            }

            // Older
            return dateFormat.format(date);
        }
    }
}
