package com.example.learnizone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.learnizone.R;
import com.example.learnizone.models.Course;
import com.example.learnizone.models.Enrollment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MyCoursesAdapter extends ListAdapter<Enrollment, MyCoursesAdapter.CourseViewHolder> {

    private OnCourseActionListener listener;
    private Context context;

    public interface OnCourseActionListener {
        void onCourseClick(Enrollment enrollment);
        void onContinueLearning(Enrollment enrollment);
        void onUnenroll(Enrollment enrollment);
        void onDownload(Enrollment enrollment);
        void onShare(Enrollment enrollment);
    }

    public MyCoursesAdapter(OnCourseActionListener listener) {
        super(new EnrollmentDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Enrollment enrollment = getItem(position);
        holder.bind(enrollment);
    }

    private static class EnrollmentDiffCallback extends DiffUtil.ItemCallback<Enrollment> {
        @Override
        public boolean areItemsTheSame(@NonNull Enrollment oldItem, @NonNull Enrollment newItem) {
            return oldItem.getEnrollmentId().equals(newItem.getEnrollmentId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Enrollment oldItem, @NonNull Enrollment newItem) {
            return oldItem.equals(newItem);
        }
    }

    class CourseViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView courseImage;
        private TextView courseTitle;
        private TextView courseCategory;
        private TextView progressText;
        private ProgressBar progressBar;
        private TextView timeSpent;
        private TextView lastAccessed;
        private TextView status;
        private MaterialButton primaryButton;
        private ImageView menuButton;
        private View completedBadge;
        private ImageView certificateIcon;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            courseImage = itemView.findViewById(R.id.course_image);
            courseTitle = itemView.findViewById(R.id.course_title);
            courseCategory = itemView.findViewById(R.id.course_category);
            progressText = itemView.findViewById(R.id.progress_text);
            progressBar = itemView.findViewById(R.id.progress_bar);
            timeSpent = itemView.findViewById(R.id.time_spent);
            lastAccessed = itemView.findViewById(R.id.last_accessed);
            status = itemView.findViewById(R.id.status);
            primaryButton = itemView.findViewById(R.id.primary_button);
            menuButton = itemView.findViewById(R.id.menu_button);
            completedBadge = itemView.findViewById(R.id.completed_badge);
            certificateIcon = itemView.findViewById(R.id.certificate_icon);

            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCourseClick(getItem(position));
                }
            });

            primaryButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Enrollment enrollment = getItem(position);
                    if (enrollment.isCompleted()) {
                        listener.onCourseClick(enrollment);
                    } else {
                        listener.onContinueLearning(enrollment);
                    }
                }
            });

            menuButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    showPopupMenu(v, position);
                }
            });
        }

        public void bind(Enrollment enrollment) {
            bindCourseDetails(enrollment);
            bindProgressDetails(enrollment);
            bindStatus(enrollment);
            bindButtons(enrollment);
        }

        private void bindCourseDetails(Enrollment enrollment) {
            // TODO: Replace with actual course data from Firestore
            courseTitle.setText("Cours ID: " + enrollment.getCourseId());
            courseCategory.setText("Programmation");
            
            // Load course image with error handling
            Glide.with(context)
                .load("https://images.unsplash.com/photo-1488590528505-98d2b5aba04b")
                .placeholder(R.drawable.placeholder_course)
                .error(R.drawable.error_course_image)
                .centerCrop()
                .into(courseImage);
        }

        private void bindProgressDetails(Enrollment enrollment) {
            double progress = enrollment.getProgress();
            progressBar.setProgress((int) (progress * 100));
            progressText.setText(String.format(Locale.getDefault(), "%.1f%%", progress * 100));

            // Time spent
            timeSpent.setText(enrollment.getFormattedTimeSpent());

            // Last access
            if (enrollment.getLastAccessDate() != null) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                lastAccessed.setText("Dernier accès: " + formatter.format(enrollment.getLastAccessDate()));
            } else {
                lastAccessed.setText("Jamais consulté");
            }
        }

        private void bindStatus(Enrollment enrollment) {
            switch (enrollment.getStatus()) {
                case COMPLETED:
                    status.setText("Terminé");
                    status.setTextColor(context.getResources().getColor(R.color.success));
                    completedBadge.setVisibility(View.VISIBLE);
                    certificateIcon.setVisibility(enrollment.isCertificateIssued() ? View.VISIBLE : View.GONE);
                    break;
                case ACTIVE:
                    if (enrollment.getProgress() > 0) {
                        status.setText("En cours");
                        status.setTextColor(context.getResources().getColor(R.color.primary));
                    } else {
                        status.setText("Pas commencé");
                        status.setTextColor(context.getResources().getColor(R.color.gray_600));
                    }
                    completedBadge.setVisibility(View.GONE);
                    certificateIcon.setVisibility(View.GONE);
                    break;
                case SUSPENDED:
                    status.setText("Suspendu");
                    status.setTextColor(context.getResources().getColor(R.color.warning));
                    completedBadge.setVisibility(View.GONE);
                    certificateIcon.setVisibility(View.GONE);
                    break;
                default:
                    status.setText(enrollment.getStatus().getValue());
                    status.setTextColor(context.getResources().getColor(R.color.gray_600));
                    completedBadge.setVisibility(View.GONE);
                    certificateIcon.setVisibility(View.GONE);
                    break;
            }
        }

        private void bindButtons(Enrollment enrollment) {
            if (enrollment.isCompleted()) {
                primaryButton.setText("Voir le certificat");
                primaryButton.setIconResource(R.drawable.ic_certificate);
            } else if (enrollment.getProgress() > 0) {
                primaryButton.setText("Continuer");
                primaryButton.setIconResource(R.drawable.ic_play_arrow);
            } else {
                primaryButton.setText("Commencer");
                primaryButton.setIconResource(R.drawable.ic_play_arrow);
            }
        }

        private void showPopupMenu(View anchor, int position) {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(context, anchor);
            popup.getMenuInflater().inflate(R.menu.my_course_menu, popup.getMenu());

            Enrollment enrollment = getItem(position);

            // Hide certain options based on status
            if (enrollment.isCompleted()) {
                popup.getMenu().findItem(R.id.action_unenroll).setVisible(false);
            }

            popup.setOnMenuItemClickListener(item -> {
                if (listener == null) return false;

                int itemId = item.getItemId();
                if (itemId == R.id.action_download) {
                    listener.onDownload(enrollment);
                    return true;
                } else if (itemId == R.id.action_share) {
                    listener.onShare(enrollment);
                    return true;
                } else if (itemId == R.id.action_unenroll) {
                    listener.onUnenroll(enrollment);
                    return true;
                }
                return false;
            });

            popup.show();
        }
    }
} 