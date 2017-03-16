package com.asadmshah.dynanimhelper.sample;

import android.os.Bundle;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringForce;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.asadmshah.dynanimhelper.SpringAnimationGroup;

public class MainActivity extends AppCompatActivity {

    private final SpringAnimationGroup.OnGroupAnimationEndListener onGroupAnimationEndListener = new SpringAnimationGroup.OnGroupAnimationEndListener() {
        @Override
        public void onGroupAnimationEnd(SpringAnimationGroup group, boolean canceled, float value, float velocity) {
            springAnimationGroup.removeEndListener(this);

            fab.setOnClickListener(onFabClickListener);
            fab.setVisibility(View.VISIBLE);
        }
    };

    private final View.OnClickListener onFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            v.setOnClickListener(null);
            v.setVisibility(View.INVISIBLE);

            textView.setTranslationX(0f);
            textView.setTranslationY(0f);
            textView.setScaleX(1f);
            textView.setScaleY(1f);

            textView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    springAnimationGroupBuilder.setFinalPosition(DynamicAnimation.TRANSLATION_X, parseValue(translationXValue, 0f));
                    springAnimationGroupBuilder.setFinalPosition(DynamicAnimation.TRANSLATION_Y, parseValue(translationYValue, 0f));
                    springAnimationGroupBuilder.setFinalPosition(DynamicAnimation.SCALE_X, parseValue(scaleXValue, 1f));
                    springAnimationGroupBuilder.setFinalPosition(DynamicAnimation.SCALE_Y, parseValue(scaleYValue, 1f));

                    springAnimationGroup = springAnimationGroupBuilder.build();
                    springAnimationGroup.addEndListener(onGroupAnimationEndListener);
                    springAnimationGroup.start();
                }
            }, 250);
        }
    };

    private TextView textView;
    private FloatingActionButton fab;

    private EditText translationXValue;
    private EditText translationYValue;
    private EditText scaleXValue;
    private EditText scaleYValue;

    private SpringAnimationGroup.Builder springAnimationGroupBuilder;
    private SpringAnimationGroup springAnimationGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text_view);

        springAnimationGroupBuilder = SpringAnimationGroup.from(textView);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(onFabClickListener);

        Spinner ratios = (Spinner) findViewById(R.id.damping_ratio);
        ratios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                float[] values = {
                        SpringForce.DAMPING_RATIO_HIGH_BOUNCY,
                        SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY,
                        SpringForce.DAMPING_RATIO_LOW_BOUNCY,
                        SpringForce.DAMPING_RATIO_NO_BOUNCY
                };

                springAnimationGroupBuilder.setDampingRatio(values[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner stiffness = (Spinner) findViewById(R.id.stiffness);
        stiffness.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                float[] values = {
                        SpringForce.STIFFNESS_HIGH,
                        SpringForce.STIFFNESS_MEDIUM,
                        SpringForce.STIFFNESS_LOW,
                        SpringForce.STIFFNESS_VERY_LOW
                };

                springAnimationGroupBuilder.setStiffness(values[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        translationXValue = (EditText) findViewById(R.id.translation_x_value);
        translationXValue.setText("0.0");

        translationYValue = (EditText) findViewById(R.id.translation_y_value);
        translationYValue.setText("0.0");

        scaleXValue = (EditText) findViewById(R.id.scale_x_value);
        scaleXValue.setText("1.0");

        scaleYValue = (EditText) findViewById(R.id.scale_y_value);
        scaleYValue.setText("1.0");
    }

    public static float parseValue(EditText editText, float def) {
        String str = editText.getText().toString();
        float flt;
        try {
            flt = Float.parseFloat(str);
        } catch (NumberFormatException e) {
            flt = def;
        }
        editText.setText(String.valueOf(flt));
        return flt;
    }

}
