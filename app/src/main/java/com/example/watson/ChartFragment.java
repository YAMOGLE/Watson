package com.example.watson;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChartFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChartFragment extends Fragment implements OnChartGestureListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    PieChart pieChart;
    BarChart barChart;


    private static final String Url = "https://raw.githubusercontent.com/YAMOGLE/Watson/master/test.json";

    private RequestQueue mQueue;
    ArrayList<String> labels;
    LinkedHashMap<String, HashMap<String, Float>> allData;
    SharedPreferences pref;

    public ChartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChartFragment newInstance(String param1, String param2) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chart, container, false);
        mQueue = Volley.newRequestQueue(getContext());
        allData = new LinkedHashMap<>();
        //pref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);


        pieChart = (PieChart) v.findViewById(R.id.chart);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5,10,5,5);
        pieChart.setDragDecelerationFrictionCoef(0.45f);
        pieChart.setDrawHoleEnabled(false);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setOnChartGestureListener(this);

        barChart = (BarChart) v.findViewById(R.id.barChart);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawAxisLine(true);
        barChart.getXAxis().setDrawLabels(true);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawLabels(false);
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisRight().setDrawLabels(false);



//        barChart.getXAxis().setGranularity(0.5f);



        loadJsonData();


        Log.i("sss", "ddd");

        return v;
    }

    public void updateBarChart(int index){
        HashMap<String, Float> currentMap = allData.get((allData.keySet().toArray())[index]);
        ArrayList<BarEntry> yVals = new ArrayList<>();
        labels = new ArrayList<>();
        int i = 0;
        for(String merch : currentMap.keySet()) {
            labels.add(merch);
            yVals.add(new BarEntry(i++, currentMap.get(merch)));
        }
        Log.i("list", String.valueOf(labels.size()));

        BarDataSet set = new BarDataSet(yVals, "Data");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        set.setDrawValues(true);
        BarData data = new BarData(set);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);
        barChart.setData(data);
        barChart.invalidate();
        barChart.animateY(500);
    }


    public void loadJsonData() {
       JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Url, null,
               new Response.Listener<JSONObject>() {
                   @Override
                   public void onResponse(JSONObject response) {
                       try {
                           JSONArray jsonArray = response.getJSONArray("data");
                           ArrayList<PieEntry> values = new ArrayList<>();
                           for (int i = 0; i < jsonArray.length(); i++) {
                               JSONObject item = jsonArray.getJSONObject(i);
                               String category = item.getString("Category");
                               String merchant = item.getString("Merchant");
                               float amount = (float)item.getDouble("Amount");
                            Log.i("money", String.valueOf(amount));
                               if(!allData.containsKey(category)) {
                                   allData.put(category, new HashMap<String, Float>());
                                    allData.get(category).put(merchant, amount);
                               } else {
                                   if(allData.get(category).containsKey(merchant)) {
                                       allData.get(category).put(merchant, allData.get(category).get(merchant) + amount);
                                   } else {
                                       allData.get(category).put(merchant, amount);
                                   }
                               }

                           }


                           SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                            //sharedPrefs.edit().clear().commit();
                           String dataddd = sharedPrefs.getString("newItem", "null");
                           Log.i("newItem", dataddd);
                           if(dataddd != "null") {
                               String[] list = dataddd.split(",");
                               Log.d("list0", list[0]);
                               Log.d("list1", list[1]);
                               Log.d("listsss", String.valueOf(allData.get("Grocery")));
                               allData.get(list[1]).put(list[0], allData.get(list[1]).get(list[0]) + Float.valueOf(list[2]));
                           }

                           for(String cate: allData.keySet()){
                               int amount = 0;
                               for(String merch : allData.get(cate).keySet()) amount += allData.get(cate).get(merch);
                               values.add(new PieEntry((float)amount, cate));

                           }
                           PieDataSet  dataSet = new PieDataSet(values, "Category");
                           dataSet.setSliceSpace(3f);
                           dataSet.setSelectionShift(5f);
                           dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

                           PieData data = new PieData(dataSet);
                           data.setValueTextSize(10f);
                           data.setValueTextColor(Color.YELLOW);

                           pieChart.setData(data);
                            pieChart.invalidate();



                       } catch (JSONException e) {
                           e.printStackTrace();
                       }

                   }
               }, new Response.ErrorListener() {
           @Override
           public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
           }
       });
       mQueue.add(request);


    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("sss", String.valueOf(pieChart.getHighlightByTouchPoint(me.getX(), me.getY()).getX()));

        updateBarChart((int)pieChart.getHighlightByTouchPoint(me.getX(), me.getY()).getX());
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
