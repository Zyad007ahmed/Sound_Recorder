package org.horaapps.soundrecorder.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import org.horaapps.soundrecorder.DBHelper;
import org.horaapps.soundrecorder.R;
import org.horaapps.soundrecorder.RecordingItem;
import org.horaapps.soundrecorder.fragments.PlaybackFragment;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.RecordingsViewHolder> {

    private DBHelper dbHelper;
    private Context mContext;
    RecordingItem item;

    public FileViewerAdapter(Context context) {
        super();
        mContext = context;
        dbHelper = new DBHelper(context);
    }

    @NonNull
    @Override
    public FileViewerAdapter.RecordingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        return new FileViewerAdapter.RecordingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordingsViewHolder holder, int position) {
        item = dbHelper.getItemAt(position);

        long itemDuration = item.getmLength();
        long hours = TimeUnit.MILLISECONDS.toHours(itemDuration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration) - TimeUnit.MINUTES.toSeconds(minutes);

        holder.fileName.setText(item.getmName());
        if (hours == 0) {
            holder.fileLength.setText(String.format("%02d:%02d", minutes, seconds));
        } else {
            holder.fileLength.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        }
        holder.fileDate.setText(DateUtils.formatDateTime(mContext, item.getmTime(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR));

        int curPosition = position;
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if file existed
                if(! new File(getItemAt(curPosition).getmFilePath()).exists()){
                    Toast.makeText(mContext,getItemAt(curPosition).getmName() + " doesn't exist",Toast.LENGTH_SHORT).show();
                    dbHelper.removeItemWithId(getItemAt(curPosition).getmId());
                    notifyItemRemoved(curPosition);
                    return;
                }
                PlaybackFragment playbackFragment = PlaybackFragment.newInstance(getItemAt(holder.getLayoutPosition()));

                FragmentTransaction transaction = ((FragmentActivity) mContext)
                        .getSupportFragmentManager()
                        .beginTransaction();

                playbackFragment.show(transaction, "dialog_playback");
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ArrayList<String> entries = new ArrayList<>();
                entries.add(mContext.getString(R.string.dialog_file_share));
                entries.add(mContext.getString(R.string.dialog_file_rename));
                entries.add(mContext.getString(R.string.dialog_file_delete));

                final CharSequence[] items = entries.toArray(new CharSequence[entries.size()]);

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.dialog_title_options));
                builder.setCancelable(true);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int item) {
                        if (item == 0) {
                            shareFileDialog(holder.getLayoutPosition());
                        } else if (item == 1) {
                            renameFileDialog(holder.getLayoutPosition());
                        } else if (item == 2) {
                            deleteFileDialog(holder.getLayoutPosition());
                        }
                    }
                });

                builder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                });

                builder.show();

                return true; // true or false ?
            }
        });
    }

    private void renameFileDialog(int position) {
        AlertDialog.Builder renameDialog = new AlertDialog.Builder(mContext);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_rename_file, null);

        final EditText input = (EditText) view.findViewById(R.id.new_name);
        input.requestFocus();

        renameDialog.setTitle(mContext.getString(R.string.dialog_title_rename));
        renameDialog.setCancelable(true);
        renameDialog.setPositiveButton(mContext.getString(R.string.dialog_action_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String value = input.getText().toString().trim();

                        //if the input is empty
                        if (value.isEmpty()) {
                            Toast.makeText(mContext, "Enter a valid name.", Toast.LENGTH_SHORT).show();
                            return;// why dialog cancel
                        }

                        value += ".mp3";
                        
                        rename(position,value);
                    }
                });
        renameDialog.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        renameDialog.setView(view);
        renameDialog.show();
    }

    private void rename(int position, String name) {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder/" + name;
        File file = new File(filePath);

        if(file.exists() && !file.isDirectory()){
            Toast.makeText(mContext,
                    String.format(mContext.getString(R.string.toast_file_exists), name),
                    Toast.LENGTH_SHORT).show();
        }else {
            File oldFile = new File(getItemAt(position).getmFilePath());
            oldFile.renameTo(file);
            dbHelper.renameItem(getItemAt(position).getmId(),name);
            notifyItemChanged(position);
        }

    }

    private void deleteFileDialog(int position) {
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(mContext);
        deleteDialog.setTitle(mContext.getString(R.string.dialog_title_delete));
        deleteDialog.setMessage(mContext.getString(R.string.dialog_text_delete));
        deleteDialog.setCancelable(true);
        deleteDialog.setPositiveButton(mContext.getString(R.string.dialog_action_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                remove(position);
            }
        });
        deleteDialog.setNegativeButton(mContext.getString(R.string.dialog_action_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        deleteDialog.show();
    }

    private void remove(int position) {
        //delete file from storage
        File file = new File(getItemAt(position).getmFilePath());
        file.delete();

        Toast.makeText(
                mContext,
                String.format(
                        mContext.getString(R.string.toast_file_delete),
                        getItemAt(position).getmName()
                ),
                Toast.LENGTH_SHORT
        ).show();

        //delete file from database
        dbHelper.removeItemWithId(getItemAt(position).getmId());

        notifyItemRemoved(position);
    }

    //not finished
    private void shareFileDialog(int position) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(mContext,"org.horaapps.soundrecorder.adapters.fileprovider",
                new File(getItemAt(position).getmFilePath())));
        shareIntent.setType("audio/mp3");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        mContext.startActivity(Intent.createChooser(shareIntent,mContext.getText(R.string.send_to)));
    }

    private RecordingItem getItemAt(int position) {
        return dbHelper.getItemAt(position);
    }

    @Override
    public int getItemCount() {
        return dbHelper.getCount();
    }

    public static class RecordingsViewHolder extends RecyclerView.ViewHolder {

        private TextView fileName;
        private TextView fileLength;
        private TextView fileDate;
        private View cardView;

        public RecordingsViewHolder(@NonNull View view) {
            super(view);
            fileName = (TextView) view.findViewById(R.id.file_name);
            fileLength = (TextView) view.findViewById(R.id.file_length);
            fileDate = (TextView) view.findViewById(R.id.file_date);
            cardView = view.findViewById(R.id.card_view);
        }
    }
}
