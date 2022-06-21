package fr.cjpapps.meschemins;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ArchivesAdapter  extends RecyclerView.Adapter<ArchivesAdapter.MyViewHolder> {

    final List<UnChemin> mesData;
    final LayoutInflater mInflater;
    final RecyclerViewClickListener listener;

    ArchivesAdapter(Context context, List<UnChemin> maList, RecyclerViewClickListener mlistener) {
        mInflater = LayoutInflater.from(context);
        this.mesData = maList;
        this.listener = mlistener;
    }

    @NonNull
    @Override
    public ArchivesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // fournit le ViewHolder avec son layout
        View mItemView = mInflater.inflate(R.layout.trajet_archive, parent, false);
        return new MyViewHolder(mItemView, this, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ArchivesAdapter.MyViewHolder holder, int position) {
        // connecte les données au ViewHolder
        UnChemin mCurrent = mesData.get(position);
        holder.listItemView.setText(mCurrent.getNomchemin());
    }

    @Override
    public int getItemCount() {
        return mesData.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final TextView listItemView;
        final ImageButton iphiItemView;
        final ImageButton emailItemView;
        final ArchivesAdapter mAdapter;
        final RecyclerViewClickListener mListener;
        MyViewHolder(View itemView, ArchivesAdapter adapter, RecyclerViewClickListener listener) {
            super(itemView);
            listItemView = itemView.findViewById(R.id.un_trajet);
            iphiItemView = itemView.findViewById(R.id.iphi_button);
            emailItemView = itemView.findViewById(R.id.email_button);
            this.mAdapter = adapter;
            mListener = listener;
            listItemView.setOnClickListener(this);
            iphiItemView.setOnClickListener(this);
            emailItemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int mPosition = getLayoutPosition();
// ceci renvoie au listener dans l'activité ou le fragment où on va faire le travail :
            mListener.onClick(v, mPosition);
        }
    }   // end ViewHolder

}
