package com.example.watson;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class ClassifierFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int Image_Capture_Code = 1;
    private ImageView imgCapture;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int PORT = 8000;
    private String SERVERIP = "206.189.224.61";

    private OnFragmentInteractionListener mListener;

//    public ClassifierFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment ClassifierFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static ClassifierFragment newInstance(String param1, String param2) {
//        ClassifierFragment fragment = new ClassifierFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_classifier, container, false);
        TextView text_home= root.findViewById(R.id.text_home);
        text_home.setText("Click to Take Picture!");
        imgCapture = root.findViewById(R.id.imgCapture);

        final Button takePhoto = root.findViewById(R.id.photo);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cInt,Image_Capture_Code);
            }
        });

        return root;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Image_Capture_Code) {
            if (resultCode == RESULT_OK) {
                Bitmap bp = (Bitmap) data.getExtras().get("data");
                Bitmap temp = RotateBitmap(bp,90);
                imgCapture.setImageBitmap(temp);

                MyTaskParams myTaskParams  = new MyTaskParams(temp, "end_of_packet",null);
                SendToServer sendToServer = new SendToServer();
                sendToServer.execute(myTaskParams);


            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static class MyTaskParams {
        String command;
        String information;
        Bitmap bitmap;

        MyTaskParams(Bitmap bitmap, String command, String information) {
            this.bitmap = bitmap;
            this.command = command;
            this.information = information;
        }
    }


    public static Bitmap RotateBitmap(Bitmap source, float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private class SendToServer extends AsyncTask<MyTaskParams,Integer,String> {
        Socket socket;
        String getCommand;
        PrintWriter printWriter;
        BufferedReader bufferedReader;
        byte [] byteArray;
        String output;

        @Override
        protected String doInBackground(MyTaskParams...params){
            try {
                if(params[0].bitmap != null) {
                    try {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        params[0].bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                        byteArray = byteArrayOutputStream.toByteArray();
                        Log.d("ClientActivity", "change to byte success");
                    }
                    catch (Exception e) { Log.d("ClientActivity", e.getMessage()); }
                }
                Log.d("ClientActivity", "socket is null1111" + SERVERIP + ":" + PORT);
                socket = new Socket(SERVERIP, PORT);
                Log.d("ClientActivity", "socket is null2222");
                if (socket == null)Log.d("ClientActivity", "socket is null");
                if (socket != null) {
                    int cont = 1;
                    while (cont == 1) {
                        try {
//                            printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                            printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                            if(params[0].bitmap != null) output = Base64.encodeToString(byteArray,Base64.DEFAULT);
                            else if (params[0].information != null) output = params[0].information;
                            String appendix = params[0].command;
                            printWriter.write(output + appendix);
                            printWriter.flush();

                        } catch (Exception e) { Log.e("ClientAcivtity: Ex", String.valueOf(e)); }
                        try {
                            Log.d("ClientActivity", "receiving message.....");
                            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                final String msg;
                                msg = (line);
                                getCommand = msg;
                                Log.d("DeviceActivity", msg);
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                        cont--;
                    }
                    Log.d("ClientActivity", "C: Closed.");
                }
            } catch (Exception e) { Log.e("ClientAcivtity: Ex", String.valueOf(e)); }
            return getCommand;
        }

        @Override
        protected void onProgressUpdate(Integer ...Values){ }

        @Override
        protected void onPostExecute(String fromserver){
            super.onPostExecute(fromserver);
            try {
                Log.d("received_string",fromserver);
                JSONObject jObject = new JSONObject(fromserver);
            }catch (Exception e){}
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
