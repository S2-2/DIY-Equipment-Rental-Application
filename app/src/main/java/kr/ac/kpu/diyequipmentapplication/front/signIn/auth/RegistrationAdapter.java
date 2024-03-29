package kr.ac.kpu.diyequipmentapplication.front.signIn.auth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kr.ac.kpu.diyequipmentapplication.R;
//공급자가 입력한 데이터를 list_equipmentitem.xml파일에 등록하는 Adapter Class 구현
public class RegistrationAdapter extends RecyclerView.Adapter<RegistrationAdapter.ViewHolder> {

    Context context;
    List<EquipmentRegistration> equipmentRegistrationList;

    public RegistrationAdapter(Context context, List<EquipmentRegistration> equipmentRegistrationList) {
        this.context = context;
        this.equipmentRegistrationList = equipmentRegistrationList;
    }

    @NonNull
    @Override
    public RegistrationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_equipmentitem,parent,false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistrationAdapter.ViewHolder holder, int position) {

        EquipmentRegistration equipmentRegistration = equipmentRegistrationList.get(position);
        holder.tvModelText.setText("Model Inform \n"+equipmentRegistration.getModelText()+"\n");
        holder.tvModelName.setText("Model Name \n"+equipmentRegistration.getModelName());

        String imageUri = null;
        imageUri=equipmentRegistration.getImage();
        Picasso.get().load(imageUri).into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return equipmentRegistrationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView tvModelName, tvModelText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_recyclerView_id);
            tvModelName = itemView.findViewById(R.id.modelName_recyclerView_id);
            tvModelText = itemView.findViewById(R.id.modelText_recyclerView_id);

        }
    }
}
