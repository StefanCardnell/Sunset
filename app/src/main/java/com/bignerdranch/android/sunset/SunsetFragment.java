package com.bignerdranch.android.sunset;


import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class SunsetFragment extends Fragment {

    private static final String TAG = "SunsetFragment";

    private static final String STATE_SUNRISE = "sunrise";
    private static final String STATE_SUNSET = "sunset";
    private static final String STATE_UNSET = "unset";

    private View mSceneView;
    private View mSunView;
    private View mSkyView;

    private int mBlueSkyColor;
    private int mSunsetSkyColor;
    private int mNightSkyColor;

    private long SUNSET_TIME = 3000;
    private long NIGHTSKY_TIME = 1500;

    private ObjectAnimator mHeightAnimator;
    private ObjectAnimator mSunsetSkyAnimator;
    private ObjectAnimator mNightSkyAnimator;

    private String mAnimationState = STATE_UNSET;

    public static SunsetFragment newInstance() {
        return new SunsetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sunset, container, false);

        mSceneView = view;
        mSunView = view.findViewById(R.id.sun);
        mSkyView = view.findViewById(R.id.sky);

        Resources resources = getResources();
        mBlueSkyColor = resources.getColor(R.color.blue_sky);
        mSunsetSkyColor = resources.getColor(R.color.sunset_sky);
        mNightSkyColor = resources.getColor(R.color.night_sky);

        startSunGrowthAnimation();

        mSceneView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                switch (mAnimationState) {
                    case STATE_UNSET:
                        startAnimation();
                        break;
                    case STATE_SUNRISE:
                        sunriseToSunset();
                        break;
                    case STATE_SUNSET:
                        sunsetToSunrise();
                        break;
                }
            }
        });

        return view;

    }

    private void startSunGrowthAnimation() {
        float scaleX = mSunView.getScaleX();
        float scaleY = mSunView.getScaleY();

        float scaleXEnd = scaleX * 1.2f;
        float scaleYEnd = scaleY * 1.2f;

        ObjectAnimator pulseAnimatorX = ObjectAnimator
                .ofFloat(mSunView, "scaleX", scaleX, scaleXEnd)
                .setDuration(3000);
        ObjectAnimator pulseAnimatorY = ObjectAnimator
                .ofFloat(mSunView, "scaleY", scaleY, scaleYEnd)
                .setDuration(3000);

        pulseAnimatorX.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimatorX.setRepeatMode(ValueAnimator.REVERSE);

        pulseAnimatorY.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimatorY.setRepeatMode(ValueAnimator.REVERSE);

        pulseAnimatorX.start();
        pulseAnimatorY.start();

    }

    private void startAnimation() {

        float sunYStart = mSunView.getTop();
        float sunYEnd = mSkyView.getHeight() + 100;

        mHeightAnimator = ObjectAnimator
                .ofFloat(mSunView, "y", sunYStart, sunYEnd)
                .setDuration(SUNSET_TIME);
        mHeightAnimator.setInterpolator(new AccelerateInterpolator());

        mSunsetSkyAnimator = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", mBlueSkyColor, mSunsetSkyColor)
                .setDuration(SUNSET_TIME);
        mSunsetSkyAnimator.setEvaluator(new ArgbEvaluator());

        mNightSkyAnimator = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", mSunsetSkyColor, mNightSkyColor)
                .setDuration(NIGHTSKY_TIME);
        mNightSkyAnimator.setEvaluator(new ArgbEvaluator());

        addSunsetListeners();
        mHeightAnimator.start();
        mSunsetSkyAnimator.start();
        mAnimationState = STATE_SUNSET;
    }

    /**
     * Reverses animation from a sunrise to a sunset.
     */
    private void sunriseToSunset() {
        addSunsetListeners();
        // 1) Night sky is changing. In this case reverse it and nothing else.
        if(mNightSkyAnimator.isRunning()) {
            mNightSkyAnimator.reverse();
        }
        // 2) Sun was moving up, reverse it.
        else if(mHeightAnimator.isRunning()) {
            mHeightAnimator.reverse();
            mSunsetSkyAnimator.reverse();
        }
        // 3) Sunrise had finished, start sunset again
        else {
            mHeightAnimator.start();
            mSunsetSkyAnimator.start();
        }
        mAnimationState = STATE_SUNSET;
    }

    /**
     * Reverses animation from a sunset to a sunrise.
     */
    private void sunsetToSunrise() {
        addSunriseListeners();
        // 1) Sun was still setting. In this case reverse it and nothing else
        if(mHeightAnimator.isRunning()) {
            mHeightAnimator.reverse();
            mSunsetSkyAnimator.reverse();
        }
        // 2) Night Sky was setting or had set already, reverse it.
        else{
            mNightSkyAnimator.reverse();
        }
        mAnimationState = STATE_SUNRISE;
    }
    /**
     * Removes current animation listeners and adds those needed for a sunset.
     */
    void addSunsetListeners() {
        mNightSkyAnimator.removeAllListeners();
        mSunsetSkyAnimator.removeAllListeners();
        mHeightAnimator.removeAllListeners();
        mHeightAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) { }

            @Override
            public void onAnimationEnd(Animator animator) {
                mNightSkyAnimator.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) { }

            @Override
            public void onAnimationRepeat(Animator animator) { }
        });
    }

    /**
     * Removes current animation listeners and adds those needed for a sunrise.
     */
    private void addSunriseListeners() {
        mHeightAnimator.removeAllListeners();
        mSunsetSkyAnimator.removeAllListeners();
        mNightSkyAnimator.removeAllListeners();
        mNightSkyAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) { }

            @Override
            public void onAnimationEnd(Animator animator) {
                // Sun and sky should be finished animating if this is called, so a reverse will
                // make them start from the bottom and go to the start.
                mSunsetSkyAnimator.reverse();
                mHeightAnimator.reverse();
            }

            @Override
            public void onAnimationCancel(Animator animator) { }

            @Override
            public void onAnimationRepeat(Animator animator) { }

        });
    }

}