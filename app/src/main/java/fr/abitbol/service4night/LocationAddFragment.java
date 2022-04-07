package fr.abitbol.service4night;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.abitbol.service4night.databinding.FragmentAddLocationBinding;
import fr.abitbol.service4night.services.ElectricityService;
import fr.abitbol.service4night.services.InternetService;
import fr.abitbol.service4night.services.Service;
import fr.abitbol.service4night.services.WaterService;


public class LocationAddFragment extends Fragment implements OnCompleteListener<Void> {

    private FragmentAddLocationBinding binding;

    private final String TAG = "LocationAddFragment logging";
    private LatLng point;
    private String description;
    private HashMap<String, Service> services;
    private Bitmap picture;
    List<SliderItems> images;
    private Uri uri;
    private DataBase dataBase;
    private ViewPager2 viewPager;
    private ActivityResultLauncher<Uri> mGetcontent = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {



        @Override
        public void onActivityResult(Boolean result) {
            Log.i(TAG, "onActivityResult: picture result : "+result);
            if (result){
                //binding.pictureAddLayout.setBackground(null);
                //binding.imageView.setImageURI(uri);
                images.remove(0);

                try {
                    picture = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),uri);
                    images.add(new SliderItems(picture));
                    viewPager.setAdapter(new SliderAdapter(images,viewPager));

                } catch (IOException e) {
                    Toast.makeText(getContext(), "error while getting bitmap from uri", Toast.LENGTH_LONG).show();
                }

            }
        }
    });

    @Override
    public void onResume() {
        Log.i(TAG, "onResume called ");
        super.onResume();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {


        images = new ArrayList<>();
        binding = FragmentAddLocationBinding.inflate(inflater, container, false);
        if (binding.addWaterCheckBox.isChecked()){
            binding.addDrinkableCheckBox.setEnabled(true);
        }
        else{
            binding.addDrinkableCheckBox.setEnabled(false);
        }
        if (binding.addPublicWifiRadioButton.isChecked()){
            binding.addInternetPriceLabel.setVisibility(View.GONE);
            binding.internetPriceEditText.setVisibility(View.GONE);
        }
        else{
            binding.addInternetPriceLabel.setVisibility(View.VISIBLE);
            binding.internetPriceEditText.setVisibility(View.VISIBLE);
        }
        binding.addWaterCheckBox.setOnClickListener(view -> {
            if (((CheckBox) view).isChecked()){
                binding.addDrinkableCheckBox.setEnabled(true);
            }
            else{
                binding.addDrinkableCheckBox.setEnabled(false);
            }
        });
        binding.addInternetTypeRadioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            Log.i(TAG, "RadioGroup onCheckedChangeListener called; i = " + i);
            if (binding.addPublicWifiRadioButton.isChecked()){
                binding.addInternetPriceLabel.setVisibility(View.GONE);
                binding.internetPriceEditText.setVisibility(View.GONE);
            }
            else{
                binding.addInternetPriceLabel.setVisibility(View.VISIBLE);
                binding.internetPriceEditText.setVisibility(View.VISIBLE);
            }
        });

        viewPager = binding.locationAddViewPager;
//        images.add(0,new SliderItems(R.drawable.add_picture));

//        List<SliderItems> sliderItems = new ArrayList<>();
//        images.add(new SliderItems(BitmapFactory.decodeResource(getResources(),R.drawable._0210621_071025)));
//        images.add(new SliderItems(BitmapFactory.decodeResource(getResources(),R.drawable._0210708_160334)));

        images.add(0,new SliderItems(BitmapFactory.decodeResource(getResources(),R.drawable.test_)));


//        viewPager.setAdapter(new SliderAdapter(sliderItems,viewPager));

//        images.add(BitmapFactory.decodeResource(getResources(),R.drawable.ic_search));
        viewPager.setAdapter(new SliderAdapter(images,viewPager));
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated: called");
        super.onViewCreated(view, savedInstanceState);

//        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (savedInstanceState == null) {

            Log.i(TAG, "onViewCreated: images size : " + images.size());
            dataBase = new DataBase();
            if (getArguments() != null) {
                try {
                    point = getArguments().getParcelable("point");
                    Log.i(TAG, "onCreateView: intent extras: " + point.toString());
                    binding.locationAddTextviewLatitude.setText(Double.toString(point.latitude));
                    binding.locationAddTextviewLongitude.setText(Double.toString(point.longitude));


                }
                catch (Exception e){
                    Log.e(TAG,e.getMessage());
                    Toast.makeText(getContext(), getString(R.string.arguments_missing_error), Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(LocationAddFragment.this).popBackStack();
                }


            }
            else{
                point = new LatLng(0,0);
                // récupérer coordonnées utilisateur
            }
        }


        binding.takePictureButton.setOnClickListener(button -> {
            //File file = File.createTempFile(name,".jpg");
//            mGetcontent.launch(location.getUri());
            try {
                takePicture(LocationBuilder.generateId(point));
            } catch (IOException e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG);
            }
        });

        // ajouter possibilité de selectionner une photo dans le téléphone

        binding.buttonValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                NavHostFragment.findNavController(LocationAddFragment.this)
//                        .navigate(R.id.action_AddLocationFragment_to_MenuFragment);
                services = new HashMap<>();
                binding.descriptionEditText.getText().toString();
                description = binding.descriptionEditText.getText().toString();
                Log.i(TAG, "onClick: description = "+ description);
                dataBase.registerLocation(new Location(point,description,services),LocationAddFragment.this);
            }
        });
    }
    public boolean processServices(){
        if (binding.addWaterCheckBox.isChecked()){
            boolean drinkable = binding.addDrinkableCheckBox.isChecked();
            boolean update = services.containsKey(Service.WATER_SERVICE);
            float price;
            if(binding.addWaterPriceEditText.getText().length() == 0){
                Log.i(TAG, "getServices: water price is empty");
                price = 0;
            }
            else{
                try{
                    price = parsePrice(binding.addWaterPriceEditText);

                }catch (NumberFormatException e){
                    return false;
                }
            }
            if (update){
                services.replace(Service.WATER_SERVICE,new WaterService(price,drinkable));
            }
            else{
                services.put(Service.WATER_SERVICE,new WaterService(price,drinkable));
            }

        }
        if (binding.addElectricityCheckBox.isChecked()){
            boolean update = services.containsKey(Service.ELECTRICITY_SERVICE);
            float price;
            if(binding.electricityPriceEditText.getText().length() == 0){
                Log.i(TAG, "getServices: water price is empty");
                price = 0;
            }
            else{
                try{
                    price = parsePrice(binding.electricityPriceEditText);

                }catch (NumberFormatException e){
                    return false;
                }
            }
            if (update){
                services.replace(Service.ELECTRICITY_SERVICE,new ElectricityService(price));
            }
            else{
                services.put(Service.ELECTRICITY_SERVICE,new ElectricityService(price));
            }

        }
        if (binding.addInternetCheckBox.isChecked()){
            boolean update = services.containsKey(Service.INTERNET_SERVICE);
            InternetService.ConnectionType connectionType;
            float price = 0;
            if (binding.addPrivateNetworkRadioButton.isChecked()){
                 connectionType = InternetService.ConnectionType.private_provider;
                if(binding.electricityPriceEditText.getText().length() > 0) {
                    try {
                        price = parsePrice(binding.internetPriceEditText);

                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }
            else{
                connectionType = InternetService.ConnectionType.public_wifi;
            }

            if (update){
                if (connectionType.equals(InternetService.ConnectionType.public_wifi)) {
                    services.replace(Service.INTERNET_SERVICE, new InternetService(connectionType));
                }
                else{
                    services.replace(Service.INTERNET_SERVICE,new InternetService(connectionType,price));
                }
            }
            else{
                if (connectionType.equals(InternetService.ConnectionType.public_wifi)) {
                    services.put(Service.INTERNET_SERVICE, new InternetService(connectionType));
                }
                else{
                    services.put(Service.INTERNET_SERVICE,new InternetService(connectionType,price));
                }
            }

        }
        //réfléchir a remplir services au coup par coup avec des listener

        return true;

    }
    public float parsePrice(EditText editText) throws NumberFormatException{
        float price;
        try{
            price = Float.parseFloat(editText.getText().toString());
        }catch (NumberFormatException e){
            Log.i(TAG, "processServices: " + e.getMessage()+" from " + editText.getTransitionName());
            Toast.makeText(getContext(), getString(R.string.price_parsing_error), Toast.LENGTH_SHORT).show();
            editText.requestFocus();
            throw e;

        }
        return price;
    }

    public void takePicture(String name) throws IOException {
        File mediaStorageDir = new File(getContext().getFilesDir(), "Service4night pics");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }
        Log.i(TAG, "takePicture: file path is: "+ mediaStorageDir.getPath());
        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + name);
        uri = FileProvider.getUriForFile(getContext(),"fr.abitbol.service4night.fileprovider",file);

        mGetcontent.launch(uri);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView called");
        binding = null;
    }

    @Override
    public void onComplete(@NonNull Task task) {
        if (task.isSuccessful()){
            Log.i(TAG, "onComplete: location successfully written. ");
            Toast.makeText(getContext(), getString(R.string.location_add_success), Toast.LENGTH_LONG).show();
        }
        else{
            Log.i(TAG, "onComplete: location failed to be written");
            Log.i(TAG, "onComplete: task to string : " + task.toString());
            Log.i(TAG, "onComplete: task get Exception : "+ task.getException());
            Toast.makeText(getContext(), getString(R.string.location_add_fail), Toast.LENGTH_LONG).show();
        }
        NavHostFragment.findNavController(LocationAddFragment.this).navigate(R.id.action_AddLocationFragment_to_MenuFragment);
    }
}